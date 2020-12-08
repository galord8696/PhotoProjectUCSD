package com.cerebratek.ftpclient;

public class LoginFailureException extends Exception {

	public LoginFailureException() {

	}

	public LoginFailureException(String message) {
		super(message);
	}

	public LoginFailureException(Throwable cause) {
		super(cause);
	}

	public LoginFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
