package android.device.eeg;

import android.app.Activity;
import android.content.Context;
import android.extend.basicutility.FileFactory;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.DriverParameters;
import com.ftdi.j2xx.FT_Device;

import java.util.concurrent.ScheduledExecutorService;

//import android.numbersuggestionbci_opengl30.Flag;
//import android.numbersuggestionbci_opengl30.newSlide;

//import android.application.numbersuggestionbci.Flag;
//import android.application.numbersuggestionbci.Slide;

public class CognionicsQ30 {

	private final String TAG = "CognionicsQ30";
	//a signal from visual stimulus, so clean everything in serial buffer
	public static volatile boolean isVisualStimulusReady = false;

	public static D2xxManager ftdid2xx = null;
	FT_Device ftDevice = null;
	DriverParameters d2xxDrvParameter = null;

	Activity mActivity;
	Context mContext;

	/** Read buffer param **/
	private int readSizeFromBuffer; // how many bytes to read from FTDI internal buffer

	private int readBufferPollingMillis = 500; // how many millis to poll from FTDI internal buffer

	/** File logger **/
	private final boolean LOG_TO_FILE = false;
	private final String LOG_FILENAME_PREFIX = "mobi_"; // will append with timestamp
	String filename;
	FileFactory ff;

	Thread readSerialThread;
	ReadSerialRunnable readSerialRunnable;

    CognionicsQ30ReadCallback callback = null;
    
    /** Periodic thread scheduler **/
    ScheduledExecutorService scheduler;
    
    /** Serial buffer (used by Csv writer) **/
    SerialBuffer mSerialBuffer;
    
    Thread serialBufferCsvWriterThread;
    Runnable serialBufferCsvWriterRunnable;
    
    public static volatile boolean isStartSampling = false; // wait for signal from Onset.

	public CognionicsQ30(Activity activity) {

		this.mActivity = activity;
		this.mContext = activity.getApplicationContext();

		this.mSerialBuffer = new SerialBuffer();
	}

	private void initD2xx() {
		try {
			ftdid2xx = D2xxManager.getInstance(mContext);
		} catch (D2xxManager.D2xxException ex) {
			ex.printStackTrace();
		}	

		d2xxDrvParameter = new D2xxManager.DriverParameters();
	}


	private void SetupD2xxLibrary () {
		/*
        PackageManager pm = getPackageManager();

        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
          Log.d("PackageList", "package: " + app.packageName + ", sourceDir: " + app.nativeLibraryDir);
          if (app.packageName.equals(R.string.app_name)) {
        	  System.load(app.nativeLibraryDir + "/libj2xx-utils.so");
        	  Log.i("ftd2xx-java","Get PATH of FTDI JIN Library");
        	  break;
          }
        }
		 */
		// Specify a non-default VID and PID combination to match if required

		if(!ftdid2xx.setVIDPID(0x0403, 0xada1))
			Log.i("ftd2xx-java","setVIDPID Error");

	}

	private void setParameter() {
		//		d2xxDrvParameter.setBufferNumber(ibufnum);
		//		d2xxDrvParameter.setMaxBufferSize(ibufsize);
		//		d2xxDrvParameter.setMaxTransferSize(itransize);
		//		d2xxDrvParameter.setReadTimeout(ireadtimeout);
	}

	private void configureUart() {
		// reset to UART mode for 232 devices
		ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

		ftDevice.setBaudRate(3000000);

		// set 8 data bits, 1 stop bit, no parity
		ftDevice.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
				D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);

		// set RTS/CTS flow control
		ftDevice.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, (byte) 0x0b, (byte) 0x0c);
	}

	public void connect() {
		/** Initialize ftdi driver **/
		initD2xx();
		SetupD2xxLibrary();
		setParameter();

		int devCount = 0;
		devCount = ftdid2xx.createDeviceInfoList(mContext);
		Log.i("Misc Function Test ",
				"Device number = " + devCount);

		final int dc = devCount;
//		final Activity parent = this.mActivity;
//		parent.runOnUiThread(new Runnable() {
//			public void run() {
//				Toast.makeText(parent, Integer.toString(dc), Toast.LENGTH_LONG).show();
//			}
//		});

		if (devCount > 0) 
		{			
			D2xxManager.FtDeviceInfoListNode deviceList = ftdid2xx.getDeviceInfoListDetail(0);

			// openByIndex. other open options: Description, Location, Serial Number etc.
			// TODO: Use other open option if more than 1 USB device is connected, e.g. through USB hub,
			//		 Current setup assume there is only 1 USB device connected to the host
			ftDevice = ftdid2xx.openByIndex(mContext, 0, d2xxDrvParameter);
//			if (ftDevice.isOpen()) {
//				Log.d(TAG, "Open By Index: Pass");
//				parent.runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(parent, "Open By Index: Pass", Toast.LENGTH_LONG).show();
//					}
//				});
//			}
//			else {
//				Log.d(TAG, "Open By Index: Fail");
//				parent.runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(parent, "Open By Index: Pass", Toast.LENGTH_LONG).show();
//					}
//				});
//			}

			Log.d(TAG, "Configuring UART settings...");
			configureUart();

			// create new thread
			Log.d(TAG, "Starting readSerialThread...");
			readSerialRunnable = new ReadSerialRunnable(ftDevice, this.mActivity);
			readSerialThread = new Thread(readSerialRunnable);
			readSerialThread.start();

			//Log.d(TAG, "Starting serialBufferCsvWriterThread...");
			//serialBufferCsvWriterRunnable = new CognionicsQ30CsvWriterRunnable(mContext, mSerialBuffer);
			//serialBufferCsvWriterThread = new Thread(serialBufferCsvWriterRunnable);
			//serialBufferCsvWriterThread.start();

			//			Log.d(TAG, "Starting readSerialThread that will run every " + readBufferPollingMillis + " ms");
			//			scheduler = Executors.newScheduledThreadPool(1);
			//			scheduler.scheduleAtFixedRate(readSerialRunnable, 100 /*Initial delay*/, readBufferPollingMillis, TimeUnit.MILLISECONDS);
		}	
	}

	public void setReadCallback(CognionicsQ30ReadCallback callback) {
		this.callback = callback;
	}

	/**
	 * Set how often to read from FTDI buffer
	 * @param pollingRateMillis
	 */
	public void setPollingRate(int pollingRateMillis) {
		this.readBufferPollingMillis = pollingRateMillis;
		this.readSizeFromBuffer = (int) ( (float)pollingRateMillis/1000 * EegDeviceSpec.COGNIONICS_Q30.packetSize * EegDeviceSpec.COGNIONICS_Q30.samplingRate );
		Log.d(TAG, "Read from buffer every " + pollingRateMillis + " ms");
		Log.d(TAG, "Buffer size=" + this.readSizeFromBuffer);
	}

	//	public void setReadBufferSize(int readSizeFromBuffer) {
	//		this.readSizeFromBuffer = readSizeFromBuffer;
	//	}

	public void stopReading() {
		if (readSerialRunnable != null)
			readSerialRunnable.setRunning(false);
		//scheduler.shutdown();

		if (LOG_TO_FILE && ff != null) {
			ff.close();
			Log.i(TAG, "File successfully saved at: " + filename);
		}
	}

	
	/**
	 * Start to read from serial buffer
	 */
	public void startSampling() {
		
		// clear buffer in runnable
		//if (readSerialRunnable != null && ftDevice != null)
		//	this.readSerialRunnable.clearSerialBuffer();
            //ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
		
		CognionicsQ30.isStartSampling = true;

	}
	
	public void stopSampling() {
		CognionicsQ30.isStartSampling = false;
	}
	
	public boolean isSampling() {
		return CognionicsQ30.isStartSampling;
	}

	public class ReadSerialRunnable implements Runnable {

		public Activity activity;
		private volatile boolean isRunning = true;
		FT_Device ftDev;

		byte[] readData;
		int iavailable;
		int bytesRead;

		private volatile boolean isFirstRun = true;

		public ReadSerialRunnable(FT_Device ftDev, Activity activity) {
			this.activity = activity;
			this.ftDev = ftDev;
			//readData = new byte[96*200];
            readData = new byte[readSizeFromBuffer];
			Log.d(TAG, "Allocated " + readSizeFromBuffer + " read buffer size!");

			Log.d(TAG, "Setting latency timer from 16ms to 1ms...");
			ftDev.setLatencyTimer((byte)1);
			// ftDev.setReadTimeout(1000);
			ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

			// create file
			if (LOG_TO_FILE) {
				String filename = LOG_FILENAME_PREFIX + System.currentTimeMillis();
				ff = new FileFactory(mActivity);
				ff.createOutputStream(filename);
			}
		}

		public void setRunning(boolean value) {
			this.isRunning = value;
		}

		public void clearSerialBuffer() {
			Log.d(TAG, "Clearing Serial RX Buffer,size=" + ftDev.getQueueStatus() + ", clearing it now!");

			byte[] tmp = new byte[ftDev.getQueueStatus()];
			ftDev.read(tmp, tmp.length);
			
			Log.d(TAG, "Current Serial RX Buffer,size=" + ftDev.getQueueStatus());

		}
		
		public void run() {
			//readSizeFromBuffer = 96*100;
            //readSizeFromBuffer = 96*200;

			final Activity parent = this.activity;

//			parent.runOnUiThread(new Runnable() {
//				public void run() {
//					Toast.makeText(parent, "Read Serial!", Toast.LENGTH_LONG).show();
//				}
//			});

			while (isRunning) {
//<<<<<<< HEAD
//				//				Log.d(TAG, "Reading data from FTDI internal queue...");
//
//				if (isFirstRun) {
//					if(ftDev.getQueueStatus()>0){//buffer is ready to ready
//
//						try {
//							while(!CognionicsQ30.isVisualStimulusReady)
//								Thread.sleep(100);
//						} catch (InterruptedException e) {
//							Log.e(TAG,"InterruptedException, "+e.getMessage());
//						}
//						
//						// clear both RX and TX buffer
//						//ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
//						//Log.d(TAG, "Serial RX Buffer cleared,size=" + ftDev.getQueueStatus());
//
//						//Read all the data in the serial buffer
//						byte[] readForCleaning = new byte[ftDev.getQueueStatus()];
//						ftDev.read(readForCleaning,readForCleaning.length);;
//						
//						isFirstRun = false;
//						Log.d(TAG, "Letting slide know that Q30 is reading!");
//						mSlide.isQ30SerialReady = true;
//					}
//
//					// if RX buffer is not full
//					else
//						continue;
//				}
//
//=======
//				Log.d(TAG, "Reading data from FTDI internal queue...");
				
				// only let slide know after there are at least some samples in buffer
				if (isFirstRun) {

					final int ia = ftDev.getQueueStatus();

//					parent.runOnUiThread(new Runnable() {
//						public void run() {
//							Toast.makeText(parent, Integer.toString(ia), Toast.LENGTH_LONG).show();
//						}
//					});

					if ( ia > 0) {
                        byte[] junk = new byte[ia];
                        ftDev.read(junk, ia);
						// wait 2 sec before letting slide know...
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//Flag.isCognionicsQ30SerialReady = true;
						Log.d(TAG, "CognionicsQ30 serial ready! Letting slide know...");
						isFirstRun = false;
					}
					else
						continue;
				}
				
				
//				if (isFirstRun) {
//					// wait until RX buffer is full
//					if (ftDev.readBufferFull()) {
//						Log.d(TAG, "Serial RX Buffer full,size=" + ftDev.getQueueStatus() + ", clearing it now!");
//						// clear both RX and TX buffer
////						ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
////						Log.d(TAG, "Serial RX Buffer cleared,size=" + ftDev.getQueueStatus());
//						
//						byte[] tmp = new byte[ftDev.getQueueStatus()];
//						ftDev.read(tmp, ftDev.getQueueStatus());
//						
//						Log.d(TAG, "Serial RX Buffer read,size=" + ftDev.getQueueStatus());
//						
//						Log.d(TAG, "Letting slide know that Q30 Serial Read Thread is ready!");
//						mSlide.isQ30SerialReady = true;
//						Flag.isCognionicsQ30SerialReady = true;
//						isFirstRun = false;
//
//					}
//					
//					// if RX buffer is not full
//					else
//						continue;
//				}
				
				if (CognionicsQ30.isStartSampling == false) {
                    iavailable = ftDev.getQueueStatus();
				    byte[] junk = new byte[iavailable];
				    ftDev.read(junk, iavailable);
                    continue;
                }
				
//>>>>>>> 4bfead0ab7e0a0b007933ccdb4cce459c44ecd55
				// get how many bytes in the RX buffer
				iavailable = ftDev.getQueueStatus();

				/*
				final int ia = iavailable;

				parent.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(parent, Integer.toString(ia), Toast.LENGTH_LONG).show();
					}
				});
				*/

				if(iavailable > 0)
				{	 
					if(iavailable > readSizeFromBuffer) // always read n bytes at a time
						iavailable = readSizeFromBuffer;

					else
						continue;

					bytesRead = ftDev.read(readData,iavailable);
					//					Log.d(TAG, "Read bytes=" + bytesRead);

					//if (LOG_TO_FILE && ff != null)
					//	ff.write(readData);

					// Start parsing
					CognionicsQ30DataSample[] samples = CognionicsQ30DataParser.parse(readData);

//					mSerialBuffer.add(samples);
					callback.onDataReceived(samples);
				}
			}
		}

		//public void startSampling(boolean isStartSampling) {
		//	CognionicsQ30.isStartSampling = isStartSampling;
		//}

		//		public void run2() {
		//			int iavailable;
		//			int bytesRead;
		//			
		//			// create file
		//			if (LOG_TO_FILE) {
		//				String filename = LOG_FILENAME_PREFIX + System.currentTimeMillis();
		//				ff = new FileFactory(mActivity);
		//				ff.createOutputStream(filename);
		//			}
		//			
		//			Log.d(TAG, "Setting latency timer from 16ms to 1ms...");
		//			ftDev.setLatencyTimer((byte)1);
		//			// ftDev.setReadTimeout(1000);
		//			ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
		//						
		//			while (isRunning) {
		//				Log.d(TAG, "Reading data from FTDI internal queue...");
		//				iavailable = ftDev.getQueueStatus();
		//				if(iavailable > 0)
		//				{	 
		//					if(iavailable > readSizeFromBuffer) // only read n bytes at a time
		//						iavailable = readSizeFromBuffer;
		//					bytesRead = ftDev.read(readData,iavailable);
		//					
		//					Log.d(TAG, "Read bytes=" + bytesRead);
		//					
		//					if (LOG_TO_FILE && ff != null)
		//						ff.write(readData);
		//					
		//					callback.onDataReceived(readData);
		//					
		////					for (int i = 0 ; i < readData.length ; i++) {
		////						String msg = String.format("data[%d]=%02X", i, readData[i]);
		//////						Log.d(TAG, msg);
		////						ff.write(readData);
		////					}
		//				}
		////				
		//				try {
		//					Thread.sleep(readBufferPollingMillis);
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}
		//			}
		//			
		//			if (LOG_TO_FILE && ff != null) {
		//				ff.close();
		//				Log.i(TAG, "File successfully saved at: " + filename);
		//
		//			}
		//			
		//		}

	}

	public enum Packet {
		HEADER((byte)0xFF),
		IMPEDENCE_ON((byte)0x11),
		IMPEDENCE_OFF((byte)0x12);

		byte hex;

		Packet(byte hex) {
			this.hex = hex;
		}
	}

}
