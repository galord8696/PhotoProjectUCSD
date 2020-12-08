package android.device.eeg;

public class CognionicsQ30DataSample {
	
	private int packetCounter;
	
	private double[] channel;
	
	private boolean isImpedenceOn;
	
	private int batteryVoltage;
	
	private int trigger;

	public int getPacketCounter() {
		return packetCounter;
	}

	public void setPacketCounter(int packetCounter) {
		this.packetCounter = packetCounter;
	}

	public double[] getChannel() {
		return channel;
	}

	public void setChannel(double[] channel) {
		this.channel = channel;
	}

	public boolean isImpedenceCheckStatusOn() {
		return isImpedenceOn;
	}
 
	public void setImpedanceCheckStatus(boolean isImpedenceOn) {
		this.isImpedenceOn = isImpedenceOn;
	}

	public int getBatteryVoltage() {
		return batteryVoltage;
	}

	public void setBatteryVoltage(int batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}

	public int getTrigger() {
		return trigger;
	}

	public void setTrigger(int trigger) {
		this.trigger = trigger;
	}

}
