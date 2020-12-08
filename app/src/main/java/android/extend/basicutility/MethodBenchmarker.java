package android.extend.basicutility;

import android.util.Log;

public class MethodBenchmarker {
	
	private static final String TAG = "MethodBenchmarker";
	
	private static final String SEPERATOR = "-----------------------";
	
	private static long startTime;
	private static long endTime;
	
	private static String methodName; 
	
	public static void startBenchmark(String mName) {
		methodName = mName;
		startTime = System.nanoTime();
	}
	
	/**
	 * End the benchmark and print the time
	 */
	public static void endBenchmark() {
		endTime = System.nanoTime() - startTime;
//		Log.d(TAG, SEPERATOR);
		Log.d(TAG, "Benchmark end for Method=" + methodName + ", Time spent=" + (endTime-startTime) );
//		Log.d(TAG, SEPERATOR);
	}

}
