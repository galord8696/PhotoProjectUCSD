package android.device.eeg;

import android.content.Context;
import android.util.Log;

public class CognionicsQ30CsvWriterRunnable implements Runnable {

	SerialBuffer mSerialBuffer;
	
	CognionicsQ30CsvWriter mWriter;
	
	private final Context context;
	
	private final String TAG = "CognionicsQ30CsvWriterRunnable";
	
	private final int TOTAL_BLOCKS_TO_WRITE = 100*5*60;
	
	private int currWrittenBlockNum = 0;
	
	private final String filename;
	
	private volatile boolean isRunning = true;
	
	public CognionicsQ30CsvWriterRunnable(Context context, SerialBuffer serialBuffer) {
		this.context = context;
		this.mSerialBuffer = serialBuffer;
		this.filename =  "raw-" + System.currentTimeMillis() + ".csv";
		mWriter = new CognionicsQ30CsvWriter(context, filename);
		
		Log.d(TAG, "File created=" + filename);
	}
	
	@Override
	public void run() {
		while (isRunning) {
			CognionicsQ30DataSample[] samples = mSerialBuffer.poll();
			if (samples != null) {	
				if (currWrittenBlockNum++ >= TOTAL_BLOCKS_TO_WRITE) {
//					Log.d(TAG, "Finished writing raw recording file!");
					mWriter.close();
				}
				else {
					Log.d(TAG, "Writing " + samples.length + " samples!");
					mWriter.writeLine(samples);
				}
				
			}
		}
		
	}
	
	public void stop() {
		this.isRunning = false;
	}

}
