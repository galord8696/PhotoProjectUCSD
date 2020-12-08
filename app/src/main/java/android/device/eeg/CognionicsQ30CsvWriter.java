package android.device.eeg;

import android.content.Context;
import android.extend.basicutility.FileFactory;

import java.io.IOException;

public class CognionicsQ30CsvWriter {

	private final Context context;
	private final FileFactory ff;
	
	public CognionicsQ30CsvWriter(Context context, String filepath) {
		this.context = context;
		ff = new FileFactory(context);
		ff.createOutputStream(filepath);
	}
	
	double channelData;
	
	public void writeLine(CognionicsQ30DataSample[] samples) {
		for (CognionicsQ30DataSample sample : samples) {
			try {
				ff.write(sample.getPacketCounter()+",");
				
				for (int ch = 0 ; ch < sample.getChannel().length ; ch++) {
					ff.write(sample.getChannel()[ch]+",");
				}
				
				ff.write(sample.isImpedenceCheckStatusOn()+",");
				ff.write(sample.getBatteryVoltage()+",");
				ff.write(sample.getTrigger()+"\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void writeHeader() {
	}
	
	public void close() {
		ff.close();
	}
}
