package com.cerebratek.ftpclient;

public class FtpConfig {

	String ftpServerHostname;
	int ftpServerPort;
	String ftpLoginUsername;
	String ftpLoginPassword;
	String ftpRecordingFilesDir;
	public String getFtpServerHostname() {
		return ftpServerHostname;
	}
	public void setFtpServerHostname(String ftpServerHostname) {
		this.ftpServerHostname = ftpServerHostname;
	}
	public int getFtpServerPort() {
		return ftpServerPort;
	}
	public void setFtpServerPort(int ftpServerPort) {
		this.ftpServerPort = ftpServerPort;
	}
	public String getFtpLoginUsername() {
		return ftpLoginUsername;
	}
	public void setFtpLoginUsername(String ftpLoginUsername) {
		this.ftpLoginUsername = ftpLoginUsername;
	}
	public String getFtpLoginPassword() {
		return ftpLoginPassword;
	}
	public void setFtpLoginPassword(String ftpLoginPassword) {
		this.ftpLoginPassword = ftpLoginPassword;
	}
	public String getFtpRecordingFilesDir() {
		return ftpRecordingFilesDir;
	}
	public void setFtpRecordingFilesDir(String ftpRecordingFilesDir) {
		this.ftpRecordingFilesDir = ftpRecordingFilesDir;
	}
	
}
