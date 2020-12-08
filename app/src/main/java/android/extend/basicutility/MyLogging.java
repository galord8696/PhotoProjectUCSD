package android.extend.basicutility;
/**
 * @author yute wang <yute@sccn.ucsd.edu>
 * 2014/March/23
 * This is a logging thread that saves its queued data into a file. It has a default location but you need to 
 * change it if this is not used in Android. By default, this thread starts every XX sec to query its queue,
 * if there is data in its queue it starts logging; otherwise it sleeps for XXsec. 
 * 
 * 2014/Sep/15
 * - This thread writes the eventQueue's data in the back of rawQueue's data in the logged file. 
 * - I always assume the sampling rate for raw data is definitely higher than events, so whenever this class receives one sample from raw data, 
 *   it checks if there is any data in the eventQueue. If eventQueue is empty, we manually add one empty array in to eventQueue (plz see pushRawData() ).
 * 
 * format: 
 * 		  time ch1 ch2 ch3 ... chN event1 event2 ... event N
 * 
 * example:
 * 			logger = new MyLogging(pBuffer.channels,pBuffer.sRate,(long)pBuffer.TIME_START_STAMP,null);
			logger.start();
			logger.pushRawData(dataToPush);
 *   
 */

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MyLogging extends Thread {
	public boolean STOP_TAG;
	public String TAG = "LoggingThread";
	public final boolean DEBUG = true;

	private final ConcurrentLinkedQueue<float[]> rawDataQueue;
	//Three events: [0]=cue, [1]= feedback, [2]= not used
	private final ConcurrentLinkedQueue<int[]>eventQueue;
	
	public int channel;
	public int sRate;

	//for saving location
	public static String dataPath;
	public static File dataFile,fileFolder;
	public static String fileTime;//Example: RawData Tue 15 2011 13:21:23.txt
	public static OutputStream out;
	public static Date todayDate;
	
	public float[] tempBufferForLogging;
	private int[] tempEventBufferForLogging;//size=3
	//for converting time to specific format
	public SimpleDateFormat formatter ;//= new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
	public Calendar calendar;// = Calendar.getInstance();
	public long iniBuffertime;//since phonesensor'data type(float) doesn't fit System.time(long), we need to convert it.
	
	public MyLogging(int ch, int srate, long initime,String folderNameSlashFileName){
		channel = ch;
		sRate = srate;
		STOP_TAG = false;
		tempBufferForLogging = new float[channel+1];
		tempEventBufferForLogging = new int[3];
		rawDataQueue = new ConcurrentLinkedQueue<float[]>();
		eventQueue = new ConcurrentLinkedQueue<int[]>();
		if(folderNameSlashFileName == null)
			iniDefaultLocation();
		else
			setFolderFileName(folderNameSlashFileName);
		//for log
		formatter = new SimpleDateFormat("HH mm ss.SSS");
		calendar = Calendar.getInstance();
		iniBuffertime = initime;
	}
	/**
	 * This method receives samples and pushes them into the rawDataQueue.
	 * @param v.format: time ch1 ch2 ch3 ....
	 */
	public void pushRawData(float[]v){
		rawDataQueue.add(v);
		if(eventQueue.size()==0)
			eventQueue.add(new int[3]);
	}
	/**
	 * 2014/Sep/15
	 * @param event
	 */
	public void pushEventData(int[]event){
		eventQueue.add(event);
	}
	public int getRawDataQueueSize(){
		return this.rawDataQueue.size();
	}
	public void setSTOP(){
		STOP_TAG=true;
	}
	public void run(){
		do{
			try {
				if(rawDataQueue.isEmpty())
					sleep(1000);
				else{//logging the data
					tempBufferForLogging = rawDataQueue.poll();
					tempEventBufferForLogging = eventQueue.poll();
					//for raw data
					for(int ch=0;ch<(channel+1);ch++){
						if(ch==0){
							calendar.setTimeInMillis((long) tempBufferForLogging[ch]+iniBuffertime);
							out.write(formatter.format(calendar.getTime()).getBytes());
						}
						else{
							out.write(Float.toString(tempBufferForLogging[ch]).getBytes());
						}
						out.write(" ".getBytes());
					}
					//for events
					for(int eventIndex=0;eventIndex<3;eventIndex++){
						out.write(Integer.toString(tempEventBufferForLogging[eventIndex]).getBytes());
						out.write(" ".getBytes());
					}
					out.write("\r\n".getBytes());
					out.flush();
				}
			}catch (Exception e) {
				if(DEBUG)Log.e(TAG,"EXP. in Logging.run(), "+e.getMessage());
				STOP_TAG = true;
			}
		}while(!STOP_TAG);
		//Log.e(TAG,"Logging is going to die");
	}
	/**
	 * This function initial a location in Android platform. 
	 * For other platforms, you should write yourself. Good luck!
	 */
	public void iniDefaultLocation(){
		dataPath = Environment.getExternalStorageDirectory().toString();
		todayDate = new Date();
		fileTime = todayDate.toGMTString();
		char[]a = fileTime.toCharArray();
		for(int i=0;i<a.length;i++){
			if(a[i]==':')
				a[i] = '_';
		}
		fileTime = String.copyValueOf(a);

		//create a folder called "EEGMusic"
		fileFolder = new File(dataPath,"EEGMusic");
		if(!fileFolder.exists())
			fileFolder.mkdir();

		dataFile = new File(fileFolder,"RawData_"+fileTime+".txt");
		if(DEBUG)Log.i(TAG,"File being created: RawData_"+fileTime);
		try{
			if(dataFile.createNewFile())			
				out = new FileOutputStream(dataFile);
			else
				if(DEBUG)Log.i(TAG, "rawDataOut stream can't be created");
		}catch (Exception e){
			if(DEBUG)Log.e(TAG, "Excep in creating raw data: "+e.getMessage());
		}
	}
	/**
	 * @param folderName_fileName, seperate by a space
	 * EX: arg= "EEGRehab userYute"
	 * 	   EEGRehab is filder name
	 * 	   userYute is file name
	 */
	public void setFolderFileName(String folderName_fileName){
		String folderName = folderName_fileName.substring(0, folderName_fileName.indexOf(" "));
		String fileName = folderName_fileName.substring(folderName_fileName.indexOf(" ")+1);
		dataPath = Environment.getExternalStorageDirectory().toString();
		todayDate = new Date();
		fileTime = todayDate.toGMTString();
		char[]a = fileTime.toCharArray();
		for(int i=0;i<a.length;i++){
			if(a[i]==':')
				a[i] = '_';
		}
		fileTime = String.copyValueOf(a);

		//create a folder called folderName
		fileFolder = new File(dataPath,folderName);
		if(!fileFolder.exists())
			fileFolder.mkdir();
		
		dataFile = new File(fileFolder,fileName+".txt");
		if(DEBUG)Log.i(TAG,"File being created: "+fileName);
		try{
			if(dataFile.createNewFile())			
				out = new FileOutputStream(dataFile);
			else
				if(DEBUG)Log.i(TAG, "rawDataOut stream can't be created");
		}catch (Exception e){
			if(DEBUG)Log.e(TAG, "Excep in creating raw data: "+e.getMessage());
		}
	}
}
