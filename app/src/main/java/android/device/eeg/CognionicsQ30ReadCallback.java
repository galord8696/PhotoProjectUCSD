package android.device.eeg;

public interface CognionicsQ30ReadCallback {

	void onDataReceived(CognionicsQ30DataSample[] data);
}
