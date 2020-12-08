package com.cerebratek.ftpclient;

import com.cerebratek.pref.FTPConfig;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class FtpListerCallable implements Callable {

	private final FtpClient mFtpClient;
	private final FTPConfig ftpConfig;
	
	public FtpListerCallable(FtpClient ftpClient, FTPConfig ftpConfig) {
		this.mFtpClient = ftpClient;
		this.ftpConfig = ftpConfig;
	}

	@Override
	public ArrayList<String> call() {
		try {
		mFtpClient.connect(ftpConfig.getHostname(), ftpConfig.getPort());
		mFtpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
	
		} catch (RefusedConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LoginFailureException e) {
			e.printStackTrace();
		}
	
		FTPFile[] files; // store a list of files returned from FTP Server
		
		files = mFtpClient.listFiles(ftpConfig.getHomeDir());
	
		ArrayList<String> fileListArr = new ArrayList<String>();
		for (FTPFile file : files) {
			System.out.println(file.getName());
			fileListArr.add(file.getName());
		}
		
		return fileListArr;
	}
}
