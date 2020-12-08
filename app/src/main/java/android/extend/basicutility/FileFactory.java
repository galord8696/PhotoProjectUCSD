package android.extend.basicutility;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileFactory {
	
	private final String TAG = "FileFactory";
	
	private final String PROGRAM_APP_DATA_FOLDER_NAME = "mobi";

	// public static final String FILE_NAME = ProfileSettings.FILE_NAME;

	private String fileName = null;

	FileOutputStream outputStream = null;
	FileInputStream inputStream = null;

	private Context c = null;

	public FileFactory(Context c) {
		this.c = c;
	}
	
	
	/**
	 * Read a file. The default filepath is <b>Environment.getExternalStorageDirectory()/nGoggleAppData</b>
	 * @param filename e.g. abc.txt
	 * @return
	 * @throws FileNotFoundException
	 */
	public FileInputStream read(String filename) throws FileNotFoundException {
		// check whether directory exist
		boolean success = false;
		File folder = new File(Environment.getExternalStorageDirectory() + "/" + PROGRAM_APP_DATA_FOLDER_NAME);

		if (!folder.exists()) {
		    success = folder.mkdir();
		    if (success)
				Log.i(TAG, "Directory created at INTERNALMEMORY/" + PROGRAM_APP_DATA_FOLDER_NAME);
		    else
		    	Log.w(TAG, "Failed to create directory at INTERNALMEMORY/" + PROGRAM_APP_DATA_FOLDER_NAME);
		}
		
		// write to sdcard
		File file = new File(Environment.getExternalStorageDirectory() + "/" + PROGRAM_APP_DATA_FOLDER_NAME + "/", filename);
		
//		File file = new File(c.getFilesDir(), filename);
//		File file = new File(Environment.getExternalStorageDirectory(), filename);

		try {
			this.inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(filename + " not found!");
		}
		return this.inputStream;
	}

	public void open(String filename) {
		this.fileName = filename;

		try {
			this.outputStream = c.openFileOutput(filename,
					Context.MODE_PRIVATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (this.outputStream != null)
				this.outputStream.close();
			if (this.inputStream != null)
				this.inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String content) throws IOException {
		this.outputStream.write(content.getBytes());
	}
	
	public void write(byte[] content) {
		try {
			this.outputStream.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileOutputStream getOutputStream() {
		return this.outputStream;
	}

	public void setOutputStream(FileOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public FileInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(FileInputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public FileOutputStream createOutputStream(String filename) {
//		File file = new File(c.getFilesDir(), filename);
		File dir = new File(Environment.getExternalStorageDirectory() + "/" + PROGRAM_APP_DATA_FOLDER_NAME);
		
		if (!dir.exists())
			dir.mkdirs();
		
		File file = new File(Environment.getExternalStorageDirectory() + "/" + PROGRAM_APP_DATA_FOLDER_NAME + "/", filename);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		
		try {
			this.outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Filename=" + filename + " not found! Creating new one...");
			
			try {
				file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return this.outputStream;
	}
	
//	public boolean createFile(String fileName) {
//		
//	}

	/**
	 * Remove a file
	 * @param fileName
	 * @return
	 */
	public boolean deleteFile(String fileName) {
		File file = new File(c.getFilesDir(), fileName);
		return file.delete();
		
		
	}

}
