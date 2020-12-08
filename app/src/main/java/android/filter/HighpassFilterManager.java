package android.filter;

import com.myMath.Matrix;

import static java.lang.Math.max;

public class HighpassFilterManager {
    private final double[] A = {1, -5.9514473349844800864616445323918014764785766601563, 14.75841395240426123791621648706495761871337890625, -19.51916438720121504957205615937709808349609375, 14.521482981667393374891616986133158206939697265625, -5.7618919352324642346729888231493532657623291015625, 0.95260672335035034841155265894485637545585632324219};
    private final double[] B = {0.97601573929437757115579188393894582986831665039063, -5.8560944357662654269347513036336749792098999023438, 14.640236089415664011426088109146803617477416992188, -19.5203147858875496467589982785284519195556640625, 14.640236089415664011426088109146803617477416992188, -5.8560944357662654269347513036336749792098999023438, 0.97601573929437757115579188393894582986831665039063};

    private final int Npts;
    private final double[] zi;

    public HighpassFilterManager(int length){
        Npts = length;

        Matrix B_matrix = new Matrix(B, "1");
        Matrix A_matrix = new Matrix(A, "1");

        zi = getCoeffsAndInitialConditions(B_matrix, A_matrix, Npts).getOneColumnArrayCopy(0);
    }

    public double[] filtfilt(double[] x){

        //[b,a,zi,nfact,L] =
        int nb = B.length;
        int na = A.length;

        int nfact = max(1,3*(nb-1));

        return ffOneChan(B, A, x, zi, nfact);

    }

    private Matrix getCoeffsAndInitialConditions(Matrix B, Matrix A, int Npts){
        int L = 1;
        int nb = B.getRowDimension();
        int na = A.getRowDimension();

        int nfact = max(1,3*(nb-1));


        Matrix tmpL = new Matrix(na - 1, na - 1);
        Matrix tmpR = new Matrix(na - 1, 1);

        for (int i = 0; i < na - 1; i++){
            for (int j = 0; j < na -1; j++){
                double elementL;

                if (i == j)
                    elementL = 1;
                else
                    elementL = 0;

                if (j == 0)
                    elementL -= -A.get(i + 1, 0);

                if (j == i + 1)
                    elementL -= 1;

                tmpL.set(i, j, elementL);
            }

            double elementR = B.get(i + 1, 0) - B.get(0, 0) * A.get(i + 1, 0);
            tmpR.set(i, 0, elementR);
        }

        //Matrix debug = tmpL.inverse();
        //Matrix zi = tmpL.inverse().times(tmpR);
        Matrix zi = tmpL.solve(tmpR);


      /*
              zi = ( eye(nfilt-1) - [-a(2:nfilt), [eye(nfilt-2);
                                                zeros(1,nfilt-2)]] )  \ ( b(2:nfilt) - b(1)*a(2:nfilt) );
        */

        return zi;

    }

    private double[] ffOneChan(double[] B, double[] A, double[] xc, double[] zi, int nfact){

        double[] y = new double[nfact + xc.length + nfact];

        int y_index = 0;
        for (int i = 0; i < nfact; i++){
            y[y_index] = -xc[nfact - i] + 2 * xc[0];
            y_index++;
        }
        for (int i = 0; i < xc.length; i++){
            y[y_index] = xc[i];
            y_index++;
        }
        for (int i = 0; i < nfact; i++){
            y[y_index] = -xc[xc.length - 2] + 2 * xc[xc.length - 1];
            y_index++;
        }

        double[] zi_n = new double[zi.length];

        for (int i = 0; i < zi.length; i++){
            zi_n[i] = zi[i] * y[0];
        }

        y = filter(B, A, y, zi_n);

        double[] y_reverse = new double[y.length];

        for (int i = 0; i < y.length; i++){
            y_reverse[i] = y[y.length - 1 - i];
        }

        for (int i = 0; i < zi.length; i++){
            zi_n[i] = zi[i] * y_reverse[0];
        }

        y = filter(B, A, y_reverse, zi_n);

        double[] y_central = new double[xc.length];

        for (int i = 0; i < xc.length; i++){
            y_central[i] = y[y.length - 1 - nfact - i];
        }

        return y_central;

      /*
    % Single channel, data explicitly concatenated into one vector
        y = [2*y(1)-y(nfact+1:-1:2); y; 2*y(end)-y(end-1:-1:end-nfact)]; %#ok<AGROW>

        % filter, reverse data, filter again, and reverse data again
        y = filter(b(:,ii),a(:,ii),y,zi(:,ii)*y(1));
        y = y(end:-1:1);
        y = filter(b(:,ii),a(:,ii),y,zi(:,ii)*y(1));

        % retain reversed central section of y
        y = y(end-nfact:-1:nfact+1);
    */

    }

    private double[] filter(double[] B, double[] A, double[] tmpX, double[] tmpZ){

        double[][] output = new double[2][tmpX.length];

        double[] X = tmpX;
        double[] z = tmpZ;
        int n = A.length;

        double[] Y = new double[X.length];

        for (int m = 0; m < Y.length; m++){
            double Xm = X[m];
            Y[m] = B[0] * Xm + z[0];
            double Ym = Y[m];

            for (int i = 1; i < n; i++){
                if (i != n - 1)
                    z[i - 1] = B[i] * Xm + z[i] - A[i] * Ym;
                else
                    z[i - 1] = B[i] * Xm - A[i] * Ym;
            }
        }

        /*
        double[] new_z = new double[n - 1];

        for (int i = 0; i < n - 2; i++){
            new_z[i] = z[i];
        }
        */
        return Y;

        /*
        % Naive direct form II implementation for vectors in Matlab:
        %   function [Y, z] = emulateFILTER(b, a, X, z)
        %   n    = length(a);
        %   z(n) = 0;
        %   Y    = zeros(size(X));
        %   for m = 1:length(Y)
        %      Xm   = X(m);
        %      Y(m) = b(1) * Xm + z(1);
        %      Ym   = Y(m);
        %      for i = 2:n
        %         z(i - 1) = b(i) * Xm + z(i) - a(i) * Ym;
        %      end
        %   end
        %   z = z(1:n - 1);
        %
         */
    }

}

