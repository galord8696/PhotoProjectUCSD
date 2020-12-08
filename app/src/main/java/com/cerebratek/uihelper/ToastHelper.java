package com.cerebratek.uihelper;

import android.app.Activity;
import android.widget.Toast;

public class ToastHelper {
	
	/**
	 * Display a toast message with SHORT duration
	 * @param activity
	 * @param message
	 */
	public static void displayToast(final Activity activity, final String message) {
		displayToast(activity, message, Toast.LENGTH_SHORT);
	}
	
	public static void displayToast(final Activity activity, final String message, final int duration) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				Toast.makeText(
						activity,
						message,
						duration).show();
			}
		});
	}

}
