package com.cerebratek.uihelper;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {
	
	private final static String TAG = "ProgressDialogHelper";
	
	public static ProgressDialog createProgressDialog(Context c, String title, String message) {
		 ProgressDialog pd = new ProgressDialog(c);
		 pd.setTitle(title);
		 pd.setMessage(message);    
		 pd.setIndeterminate(true);
		 pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 
		 return pd;
	}
	
	public static ProgressDialog createProgressDialogDeterminate(Context c, String title, String message, int progressMaxCount) {
		 ProgressDialog pd = new ProgressDialog(c);
		 pd.setTitle(title);
		 pd.setMessage(message);    
		 pd.setIndeterminate(false);
		 pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		 pd.setMax(progressMaxCount);
		 
		 return pd;
	}


}
