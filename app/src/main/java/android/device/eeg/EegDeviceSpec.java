package android.device.eeg;

/**
 * Device spec for EEG device, useful to determine buffer size
 * @author dsync
 *
 */
public enum EegDeviceSpec {
	
	COGNIONICS_Q30( 500 /*sampling rate*/, 30 /*total channels*/, 96 /*header+packetNum+(3*channelNum)+impedance+batteryvoltage+2*(trigger)*/ );

	protected int samplingRate;
	
	/** Packet size in bytes for 1 full sample **/
	protected int packetSize;	
	
	/** Total channel numbers **/
	protected int totalChannels;
	
	EegDeviceSpec(int samplingRate, int totalChannels, int packetSize) {
		this.samplingRate = samplingRate;
		this.packetSize = packetSize;
		this.totalChannels = totalChannels;
	}
	
}
