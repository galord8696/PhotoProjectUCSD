package com.cerebratek.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;

/**
 * Countdown timer in alert dialog
 * @author dsync
 *
 */
public class CountdownTimerDialog {
	
	static AlertDialog alertDialog;
	
	static CountDownTimer timer;

	public static void create(Context c, long duration) {
		alertDialog = new AlertDialog.Builder(c).create();  
		alertDialog.setTitle("Countdown Timer");  
		alertDialog.setMessage("00:10");
		alertDialog.show();   // 

		timer = new CountDownTimer(duration + 1000, 1000) {
		    @Override
		    public void onTick(long millisUntilFinished) {
		    	long sec = millisUntilFinished / 1000;
		    	String hhmm = String.format("%02d:%02d", sec/60, sec%60);
		    	
		    	if (alertDialog != null)
		    		alertDialog.setMessage(hhmm);
		    }

		    @Override
		    public void onFinish() {
		    	if (alertDialog != null)
		    		close();
		    }
		}.start();
	}
	
	public static void show() {
		if (alertDialog != null)
			alertDialog.show();
	}
	
	public static void close() {
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
			timer.cancel();
		}
	}
	
	/**
	 * Is the timer thread still running?
	 * @return
	 */
	public static boolean isTimerRunning() {
		return alertDialog != null;
	}
}
