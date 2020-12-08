package android.extend.basicutility;

import android.util.Log;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MatFileLoader {
	
	private static final boolean D = false;

	public static double[][] parse(String inputFilePath, String variableName) throws IOException{
		System.out.println("Reading .mat from=" + inputFilePath);
		MatFileReader reader = new MatFileReader();
		
		double[][] data = null;
		try {  
			Map<String,MLArray> map = reader.read(new File(inputFilePath));
			
			MLDouble matrix =(MLDouble)map.get(variableName);
			data = matrix.getArray();
			
			if (D) {
				System.out.println("Data length = " + data.length + "x");
				
				for (int i = 0 ; i < data.length ; i++)
					for (int j = 0 ; j < data[i].length ; j++) {
						String str = String.format("Data[%d][%d]: %2f", i, j, data[i][j]);
						System.out.println(str);
					}
			}
			

		} catch (IOException e) {
			Log.d("MatFileLoader", e.toString());
		}
		return data;
	}
}
