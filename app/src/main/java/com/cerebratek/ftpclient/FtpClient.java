package com.cerebratek.ftpclient;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FtpClient {

	FTPClient ftp = null;
	
	private int totalFileNum = 0;
	
	public FTPClient getFtpClient() {
		return ftp;
	}

	public FtpClient() {
		ftp = new FTPClient();
		FTPClientConfig config = new FTPClientConfig();
		// config.setXXX(YYY); // change required options
		// for example config.setServerTimeZoneId("Pacific/Pitcairn")
		ftp.configure(config);

	}

	public void connect(String ftpServerHostname, int ftpServerPort) throws RefusedConnectionException {
		boolean error = false;
		try {
			int reply;
			ftp.connect(ftpServerHostname, ftpServerPort);
			System.out.println("Connected to " + ftpServerHostname + ".");
			System.out.print(ftp.getReplyString());

			// After connection attempt, you should check the reply code to
			// verify
			// success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				throw new RefusedConnectionException("FTP server refused connection.");
			}

		} catch (IOException e) {
			error = true;
			e.printStackTrace();
		}
	}
	
	public void login(String username, String password) throws IOException, LoginFailureException {
		ftp.login(username, password);
		
		if (ftp.getReplyCode() != FTPReply.USER_LOGGED_IN) {
			throw new LoginFailureException("User failed to login!");
		} 
	}
	
	public FTPFile[] listDirectory(String dir) {
		FTPFile[] ftpFileArr = null;
		try {
			ftpFileArr = ftp.listDirectories(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ftpFileArr;
	}
	
	/**
	 * Return a list of files and directories
	 * @param dir
	 * @return
	 */
	public FTPFile[] listFiles(String dir) {
		FTPFile[] ftpFileArr = null;
		try {
			ftpFileArr = ftp.listFiles(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ftpFileArr;
	}
	
	
	/**
	 * Download a whole directory from a FTP server.
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param parentDir Path of the parent directory of the current directory being
	 * downloaded.
	 * @param currentDir Path of the current directory being downloaded.
	 * @param saveDir path of directory where the whole remote directory will be
	 * downloaded and saved.
	 * @throws IOException if any network or IO error occurred.
	 */
	public synchronized void downloadDirectory(FTPClient ftp, String parentDir,
	        String currentDir, String saveDir) throws IOException {
	    String dirToList = parentDir;
	    if (!currentDir.equals("")) {
	        dirToList += "/" + currentDir;
	    }
	 
	    FTPFile[] subFiles = ftp.listFiles(dirToList);
	 
	    if (subFiles != null && subFiles.length > 0) {
	        for (FTPFile aFile : subFiles) {
	            String currentFileName = aFile.getName();
	            if (currentFileName.equals(".") || currentFileName.equals("..")) {
	                // skip parent directory and the directory itself
	                continue;
	            }
	            String filePath = parentDir + "/" + currentDir + "/"
	                    + currentFileName;
	            if (currentDir.equals("")) {
	                filePath = parentDir + "/" + currentFileName;
	            }
	 
	            String newDirPath = saveDir + parentDir + File.separator
	                    + currentDir + File.separator + currentFileName;
	            if (currentDir.equals("")) {
	                newDirPath = saveDir + parentDir + File.separator
	                          + currentFileName;
	            }
	 
	            if (aFile.isDirectory()) {
	                // create the directory in saveDir
	                File newDir = new File(newDirPath);
	                boolean created = newDir.mkdirs();
	                if (created) {
	                    System.out.println("CREATED the directory: " + newDirPath);
//	                    this.mGuiApp.log("CREATED the directory: " + newDirPath);
	                } else {
	                    System.out.println("COULD NOT create the directory: " + newDirPath);
//	                    this.mGuiApp.log("COULD NOT create the directory: " + newDirPath);
	                }
	 
	                // download the sub directory
	                downloadDirectory(dirToList, currentFileName,
	                        saveDir);
	            } else {
	            	this.totalFileNum++;
	                // download the file
//	                boolean success = downloadSingleFile(ftp, filePath,
//	                        newDirPath);
//	                if (success) {
//	                    System.out.println("DOWNLOADED the file: " + filePath);
//	                    this.mGuiApp.log("DOWNLOADED the file: " + filePath);
//	                } else {
//	                    System.out.println("COULD NOT download the file: "
//	                            + filePath);
//	                    this.mGuiApp.log("COULD NOT download the file: "
//	                            + filePath);
//
//	                }
	            }
	        }
	    }
	    
	}

	public void downloadDirectory(String parentDir,
	        String currentDir, String saveDir) throws IOException {
		downloadDirectory(ftp, parentDir, currentDir, saveDir);
	}
	
	
	public boolean downloadSingleFile(String remoteFilePath, String savePath) throws IOException {
		return downloadSingleFile(ftp, remoteFilePath, savePath);
	}
	
	/**
	 * Download a single file from the FTP server
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteFilePath path of the file on the server
	 * @param savePath path of directory where the file will be stored
	 * @return true if the file was downloaded successfully, false otherwise
	 * @throws IOException if any network or IO error occurred.
	 */
	public boolean downloadSingleFile(FTPClient ftp, String remoteFilePath, String savePath) throws IOException {
	    File downloadFile = new File(savePath);
	     
	    File parentDir = downloadFile.getParentFile();
	    if (!parentDir.exists()) {
	        parentDir.mkdirs();
	    }
	         
	    OutputStream outputStream = new BufferedOutputStream(
	            new FileOutputStream(downloadFile));
	    try {
	        ftp.setFileType(FTP.BINARY_FILE_TYPE);
	        
//			this.mGuiApp.log("Downloading: " + remoteFilePath + " to: " + savePath);

	        
	        return ftp.retrieveFile(remoteFilePath, outputStream);
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (outputStream != null) {
	            outputStream.close();
	        }
	    }
	}
	
	
	public void disconnect() {
		try {
			ftp.logout();
			ftp.disconnect();
		} catch (IOException ioe) {
			// do nothing
		}
	}

	public int getTotalFileNum() {
		return totalFileNum;
	}

	public void setTotalFileNum(int totalFileNum) {
		this.totalFileNum = totalFileNum;
	}
	
}
