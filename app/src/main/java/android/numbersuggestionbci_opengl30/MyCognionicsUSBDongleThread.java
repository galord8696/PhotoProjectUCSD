package android.numbersuggestionbci_opengl30;


import android.app.Activity;
import android.device.eeg.CognionicsQ30;
import android.device.eeg.CognionicsQ30CsvWriter;
import android.device.eeg.CognionicsQ30DataSample;
import android.device.eeg.CognionicsQ30ReadCallback;
import android.extend.basicutility.FileFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.photoproject.Train;
import com.ftdi.j2xx.FT_Device;

import static android.util.Log.d;

public class MyCognionicsUSBDongleThread extends Thread {
    private final Object lock;
    private final String TAG = "CogUSBDongleThread";
    private final int APPEND_INTEGER = 0;
    private final int APPEND_MESSAGE = 1;
    private final int SET_VISUAL_STIMULUS = 2;

    //since we don't have to store all the data in the buffer, these tags are used to identify if we need to store them.
    public final static int drawRawQueue = 0;
    //public final static int motorImageryDataBuffer=1;
    //public final static int Power2DQueue = 2;
    private boolean[] bufferTag;

    //*******
    Handler mHandler;
    //private RehabLogger log;
    public boolean bReadThreadGoing;
    private int iavailable;
    //***********
    private final int bytesPerSample = 96;//198;//header+counter+3*64ch+trunk*4=198
    private final int samplesPerChunk = 100;//must be bigger than 80
    private final int CHANNEL = 30;
    private final int HEADER = 255;
    public final int readLength = bytesPerSample * samplesPerChunk;//32768;//2048;
    private int[] data;
    private final int[] dataChunkBuffer;// = new int[bytesPerSample*samplesPerChunk];
    private int numOfRead;
    private int readSamples;
    private int lastIndex;
    private int tempIndex;
    private final int previousCounter;//we need to handle packet lost badly!
    private int lostPacket;//we need to handle packet lost badly!
    private final boolean impendanceCheck;
    private final boolean iniImpendanceCheck;
    //long time;
    //-----------
    private final byte[] readData;
    private FT_Device ftDev;

    private final Activity activity;
    private static CognionicsQ30 cognionicsQ30Dev;
    private final CognionicsQ30CsvWriter writer;
    private final FileFactory ff;

    private int mSubjectNum = 0;

    //public static final int[] Q30_CHANNEL = {21, 22, 23, 24, 25, 26, 27, 28, 29};
    public static final int[] Q30_CHANNEL = {24, 25, 26, 27, 28, 29};
    //private final int[] Q30_CHANNEL = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}; //Q20 actually
    private final int Q30_REF_CHANNEL = 8;
    private final int CHANNEL_NUM = Q30_CHANNEL.length;//one is for re-reference

    public MyCognionicsUSBDongleThread(Handler h, Activity activity) {
        this.activity = activity;

        lock = new Object();
        mHandler = h;
        this.setPriority(Thread.MAX_PRIORITY);
        //log = logger;
        bReadThreadGoing = true;
        data = new int[CHANNEL + 3];//one is for counter, one is for serial event, last one is for parallel event
        dataChunkBuffer = new int[bytesPerSample * samplesPerChunk];//for the data shifting
        readData = new byte[bytesPerSample * samplesPerChunk];//for the shifted data from dataChunkBuffer
        numOfRead = 0;
        readSamples = 0;
        lastIndex = 0;
        tempIndex = 0;
        previousCounter = -1;
        lostPacket = 0;
        impendanceCheck = false;
        iniImpendanceCheck = false;
        iniBufferTag();

        cognionicsQ30Dev = new CognionicsQ30(activity);
        writer = new CognionicsQ30CsvWriter(activity, "raw-" + System.currentTimeMillis() + ".csv");
        ff = new FileFactory(activity.getApplicationContext());
        ff.createOutputStream("filter-" + System.currentTimeMillis() + ".csv");

//		((MyRenderer)mSlide.getRender()).setDevice(cognionicsQ30Dev);
        //Flag.dev = cognionicsQ30Dev;
    }

    public MyCognionicsUSBDongleThread(Handler h, Activity activity, int subjectNum) {
        this.activity = activity;
        this.mSubjectNum = subjectNum;
        lock = new Object();
        mHandler = h;
        this.setPriority(Thread.MAX_PRIORITY);
        //log = logger;
        bReadThreadGoing = true;
        data = new int[CHANNEL + 3];//one is for counter, one is for serial event, last one is for parallel event
        dataChunkBuffer = new int[bytesPerSample * samplesPerChunk];//for the data shifting
        readData = new byte[bytesPerSample * samplesPerChunk];//for the shifted data from dataChunkBuffer
        numOfRead = 0;
        readSamples = 0;
        lastIndex = 0;
        tempIndex = 0;
        previousCounter = -1;
        lostPacket = 0;
        impendanceCheck = false;
        iniImpendanceCheck = false;
        iniBufferTag();

        cognionicsQ30Dev = new CognionicsQ30(activity);
        writer = new CognionicsQ30CsvWriter(activity, "raw-" + System.currentTimeMillis() + ".csv");
        ff = new FileFactory(activity.getApplicationContext());
        ff.createOutputStream("filter-" + System.currentTimeMillis() + ".csv");

//		((MyRenderer)mSlide.getRender()).setDevice(cognionicsQ30Dev);
        //Flag.dev = cognionicsQ30Dev;
    }

    private void iniBufferTag() {
        bufferTag = new boolean[1];
        bufferTag[drawRawQueue] = true;
        //bufferTag[this.motorImageryDataBuffer] = false;
        //bufferTag[this.Power2DQueue] = true;
    }

    public void setBufferTag(int index, boolean value) {
        synchronized (lock) {
            if (index < bufferTag.length)
                bufferTag[index] = value;
        }
    }

    public static void setStop() {
        if (cognionicsQ30Dev != null)
            cognionicsQ30Dev.stopReading();
    }

    public static void pauseSampling() {
        if (cognionicsQ30Dev != null) {
            cognionicsQ30Dev.stopSampling();
        }
    }

    public static void startSampling() {
        if (cognionicsQ30Dev != null) {
            cognionicsQ30Dev.startSampling();
        }
    }

    //just return "1" in every second
    public void run_debug() {
        try {
            while (true) {
                mHandler.obtainMessage(0, ftDev.getQueueStatus() + numOfRead).sendToTarget();
                sleep(1000);
                numOfRead++;
            }
        } catch (InterruptedException e) {
            mHandler.obtainMessage(0, "InterruptedException: " + e.getMessage()).sendToTarget();
        }
    }

    public void run() {
        final Activity parent = this.activity;
//		parent.runOnUiThread(new Runnable() {
//			public void run() {
//				Toast.makeText(parent, "Q30 Thread Run!", Toast.LENGTH_LONG).show();
//			}
//		});

        // read from Cognionics FTDI buffer every 20 ms
        cognionicsQ30Dev.setPollingRate(100);

//		parent.runOnUiThread(new Runnable() {
//			public void run() {
//				Toast.makeText(parent, "Q30 Set Polling Rate!", Toast.LENGTH_LONG).show();
//			}
//		});

        // callback when 1s data is fully received
        cognionicsQ30Dev.setReadCallback(new MyCognionicsQ30ReadCallback(this.activity, this.mSubjectNum));

//		parent.runOnUiThread(new Runnable() {
//			public void run() {
//				Toast.makeText(parent, "Q30 Set Call Back!", Toast.LENGTH_LONG).show();
//			}
//		});

        cognionicsQ30Dev.connect();

        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent, "Q30 Connect!", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 2014/6/26,
     * Debug signal
     */
    public void run_yute() {
        try {
            while (bReadThreadGoing) {
                try {
                    sleep(1 / 500);
                } catch (InterruptedException e) {
                    mHandler.obtainMessage(APPEND_MESSAGE, "InterruptedException: " + e.getMessage()).sendToTarget();
                }
                for (int index = 0; index < data.length; index++) {
                    data[index] = index;
                }
                //MyBuffer.setIntQueue(data.clone());
            }
        } catch (Exception e) {
            mHandler.obtainMessage(APPEND_MESSAGE, "Exp in run(): " + e.getMessage()).sendToTarget();
        }
    }

    /**
     * Unit: byte
     * data format: header counter ch1*3byte ch2*3byte...ch64*3byte dummy1 dummy2 trigger1(serial) trigger2(parallel)   ->header+counter+3*64ch+trunk*4=198
     * This version saves the trigger bytes. (oct/3/14)
     */
    public void run__() {
        int i;
        try {
            while (bReadThreadGoing) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    mHandler.obtainMessage(APPEND_MESSAGE, "InterruptedException: " + e.getMessage()).sendToTarget();
                }
                synchronized (ftDev) {
                    iavailable = ftDev.getQueueStatus();
                    Log.i(TAG, iavailable + "");
                    if (iavailable > 0) {
                        if (!iniImpendanceCheck)
//							impendanceCheck(impendanceCheck);
                            if (iavailable > readLength)
                                iavailable = readLength;
                        numOfRead = ftDev.read(readData, iavailable);
                        //mHandler.obtainMessage(APPENDMESSAGE,"avi/rd: "+iavailable+"/"+numOfRead).sendToTarget();
                        for (i = 0; i < numOfRead; i++) {
                            dataChunkBuffer[i] = readData[i] & 0xFF;
                            tempIndex = (i + lastIndex) % bytesPerSample;

                            //tempIndex==0, skip header XDDD

                            //counter
                            if ((tempIndex) == 1) {
                                data[0] = dataChunkBuffer[i];
                            }

                            //64ch
                            else if (tempIndex > 1 && tempIndex < 92 && (tempIndex + 1) % 3 == 0)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 24;//bcz 2's compliment, we need positive sign
                            else if (tempIndex > 1 && tempIndex < 92 && (tempIndex + 1) % 3 == 1)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 17;
                            else if (tempIndex > 1 && tempIndex < 92 && (tempIndex + 1) % 3 == 2)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 9;

                                //skip two bytes here...

                                //triggers-serial
                            else if (tempIndex == (bytesPerSample - 2))//else if(tempIndex>193 && tempIndex<=196 && (tempIndex+1)%3==2)//trigger1, serial
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i];

                                //triggers-parallel
                                //else if(tempIndex>193 && tempIndex<=196 && (tempIndex+1)%3==2)
                                //data[(tempIndex+1)/3] |=dataChunkBuffer[i];//for trigger2, parallel

                                //in the end..
                            else if (tempIndex == (bytesPerSample - 1)) {
                                //triggers-parallel
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i];//put parallel event into last data[last]
                                /**
                                 * In the end of this byte, we check whether the next byte is a HEADER, if so, then the current sample is complete.
                                 * Otherwise we drop current sample, and try to search the next HEADER.
                                 * Note that, the recover algorithm below can't check the case that i is the end of this packet.*/

                                if ((i + 1 < numOfRead) && (readData[i + 1] & 0xFF) != HEADER) {
                                    mHandler.obtainMessage(APPEND_MESSAGE, "Sample lost: " + (++lostPacket)).sendToTarget();
                                    while ((i + 1 < numOfRead) && (readData[i + 1] & 0xFF) != HEADER)
                                        i++;
                                } else {
                                    synchronized (lock) {
                                        //if(bufferTag[this.drawRawQueue])

                                        //MyBuffer.setIntQueue(data.clone());
                                    }
                                    data = new int[CHANNEL + 3];
                                    readSamples++;
                                }
                            }
                        }
                        if (tempIndex != (bytesPerSample - 1))//incomplete sample in one packet
                            lastIndex = tempIndex + 1;
                        else
                            lastIndex = 0;
                        //mHandler.obtainMessage(APPEND_MESSAGE,"Sec: "+readSamples/ MyBuffer.sRate).sendToTarget();
                    }
                    //else
                    //	mHandler.obtainMessage(APPENDMESSAGE,"< 0").sendToTarget();
                }
            }
        } catch (Exception e) {
            mHandler.obtainMessage(APPEND_MESSAGE, TAG + ", Exp in run(): " + e.getMessage()).sendToTarget();
        }
    }

    /**
     * Unit: byte
     * data format: header counter ch1*3byte ch2*3byte...ch64*3byte trigger1 trigger2 dummy1 dummy2   ->header+counter+3*64ch+trunk*4=198
     * Note that, this version doesn't save the Trigger bytes. Please reference the highspeedbcispeller.HBCICognionicsUSBDongleThread for saving the triggers events.
     */
    public void runNoTrigger() {
        int i;
        try {
            while (bReadThreadGoing) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    mHandler.obtainMessage(APPEND_MESSAGE, "InterruptedException: " + e.getMessage()).sendToTarget();
                }
                synchronized (ftDev) {
                    iavailable = ftDev.getQueueStatus();
                    if (iavailable > 0) {
                        if (!iniImpendanceCheck)
//							impendanceCheck(impendanceCheck);
                            if (iavailable > readLength)
                                iavailable = readLength;
                        numOfRead = ftDev.read(readData, iavailable);
                        //mHandler.obtainMessage(APPENDMESSAGE,"avi/rd: "+iavailable+"/"+numOfRead).sendToTarget();
                        for (i = 0; i < numOfRead; i++) {
                            dataChunkBuffer[i] = readData[i] & 0xFF;
                            tempIndex = (i + lastIndex) % bytesPerSample;
                            if ((tempIndex) == 1) {
                                data[0] = dataChunkBuffer[i];
                            } else if (tempIndex > 1 && tempIndex < 194 && (tempIndex + 1) % 3 == 0)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 24;
                            else if (tempIndex > 1 && tempIndex < 194 && (tempIndex + 1) % 3 == 1)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 17;
                            else if (tempIndex > 1 && tempIndex < 194 && (tempIndex + 1) % 3 == 2)
                                data[(tempIndex + 1) / 3] |= dataChunkBuffer[i] << 9;
                            else if (tempIndex == (bytesPerSample - 1)) {
                                /**
                                 * In the end of this byte, we check whether the next byte is a HEADER, if so, then the current sample is complete.
                                 * Otherwise we drop current sample, and try to search the next HEADER.
                                 * Note that, the recover algorithm below can't check the case that i is the end of this packet.*/

                                if ((i + 1 < numOfRead) && (readData[i + 1] & 0xFF) != HEADER) {
                                    mHandler.obtainMessage(APPEND_MESSAGE, "Sample lost: " + (++lostPacket)).sendToTarget();
                                    while ((i + 1 < numOfRead) && (readData[i + 1] & 0xFF) != HEADER)
                                        i++;
                                } else {
                                    //previousCounter = data[0];
                                    //log.pushData(data.clone());
                                    //MyBuffer.setIntQueue(data.clone());
                                    //MyPrivateBuffer.Power2DQueue.add(data.clone());
                                    //RehabilitationActivity.loggerThread.pushRawData(data.clone());
                                    data = new int[CHANNEL + 1];
                                    readSamples++;
                                }
                            }
                        }
                        if (tempIndex != (bytesPerSample - 1))//incomplete sample in one packet
                            lastIndex = tempIndex + 1;
                        else
                            lastIndex = 0;
                        mHandler.obtainMessage(APPEND_MESSAGE, "Sec: " + readSamples / 300).sendToTarget();
                    }
                    //else
                    //	mHandler.obtainMessage(APPENDMESSAGE,"< 0").sendToTarget();
                }
            }
        } catch (Exception e) {
            mHandler.obtainMessage(APPEND_MESSAGE, "Exp in run(): " + e.getMessage()).sendToTarget();
        }
    }

    public final int TOTAL_SAMPLES_TO_WRITE = 96 * 500 * 10;
    public int writeCounter = 0;
    public int sampleCounter = 0;

    int currSample = 0;
    int sampleToSkip = 2;

    private class MyCognionicsQ30ReadCallback implements CognionicsQ30ReadCallback {

        private final Activity activity;
        private int mSubjectNum;

        public MyCognionicsQ30ReadCallback(Activity activity) {
            this.activity = activity;
        }

        public MyCognionicsQ30ReadCallback(Activity activity, int subjectNum) {
            this.activity = activity;
            this.mSubjectNum = subjectNum;

        }

        @Override
        public void onDataReceived(CognionicsQ30DataSample[] data) {
//			Log.d(TAG, "Received bytes=" + data.length);
//			Log.d(TAG, "Received " + data.length + " parsed samples!");

//			for (CognionicsQ30DataSample s : data) {
//				Log.d(TAG, "Packet counter=" + s.getPacketCounter());
//			}

//			if (writer != null)
//				writer.writeLine(data);
//			
//			writeCounter += data.length;
//			if (writeCounter >= TOTAL_SAMPLES_TO_WRITE) {
//				writer.close();
//				writer = null;
//				Log.d(TAG, "File written!");
//				ToastHelper.displayToast(activity, "Data file written successfully!");
//				
//				ff.close();
//				ff = null;
//			}

//			Log.d(TAG, "Total channels in original samples=" + data[0].getChannel().length);
//			for (int i = 0 ; i < data.length ; i++)
//				Log.d(TAG, "Channel length=" + data[i].getChannel().length);
//			
//			CognionicsQ30DataSample[] filteredSamples = filterChannel(data); 
//			Log.d(TAG, "Total channels in filtered samples=" + filteredSamples[0].getChannel().length);

            //TODO: Insert into buffer for algorithm...
//			final Activity parent = this.activity;
//			parent.runOnUiThread(new Runnable() {
//				public void run() {
//					Toast.makeText(parent, "Call Back!", Toast.LENGTH_LONG).show();
//				}
//			});
            final Activity parent = this.activity;

            if (data.length == 0) {
                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(parent, "Data Len = 0!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

//			int filteredChannels[] = new int[7];
            double[] filteredChannels = new double[CHANNEL_NUM + 1]; //DATA_CHANNEL + REF_CHANNEL
            for (CognionicsQ30DataSample s : data) {


                if (++currSample % 2 == 0) {
                    currSample = 0;
                    continue;
                }

                for (int ch = 0; ch < CHANNEL_NUM; ch++) {
                    filteredChannels[ch] = s.getChannel()[Q30_CHANNEL[ch] - 1];
                }

                filteredChannels[CHANNEL_NUM] = s.getChannel()[Q30_REF_CHANNEL - 1];

                d("PRINT DATA", s.getChannel().toString());
                Train.Companion.addToQueue(s.getChannel());

//				if (ff != null) {
//					for (int i = 0 ; i < filteredChannels.length ; i++)
//						try {
//							ff.write(filteredChannels[i] + ",");
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					try {
//						ff.write("\n");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				MyBuffer.setDoubleQueue(filteredChannels.clone());
//                TrainingDataUploadManager.addSample(filteredChannels.clone());

            }
            //TrainingDataUploadManager.addSample(filteredChannels.clone());
            //sendPost(filteredChannels);
        }

    }

    private CognionicsQ30DataSample[] filterChannel(CognionicsQ30DataSample[] data) {
        double[] filteredChannel = new double[7]; // only select 7 channels
        for (int i = 0; i < data.length; i++) {
            d(TAG, "channel length=" + data[i].getChannel().length);
            filteredChannel[0] = data[i].getChannel()[7];
            filteredChannel[1] = data[i].getChannel()[23];
            filteredChannel[2] = data[i].getChannel()[24];
            filteredChannel[3] = data[i].getChannel()[25];
            filteredChannel[4] = data[i].getChannel()[26];
            filteredChannel[5] = data[i].getChannel()[27];
            filteredChannel[6] = data[i].getChannel()[28];

            data[i].setChannel(filteredChannel);
        }

        return data;
    }


}