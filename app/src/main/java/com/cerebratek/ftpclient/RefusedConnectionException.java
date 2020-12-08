package com.cerebratek.ftpclient;

public class RefusedConnectionException extends Exception {

	public RefusedConnectionException() {

	}

	public RefusedConnectionException(String message) {
		super(message);
	}

	public RefusedConnectionException(Throwable cause) {
		super(cause);
	}

	public RefusedConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
