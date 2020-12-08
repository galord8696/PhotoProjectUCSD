package com.cerebratek.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class PreferenceManager {
	
	private final static String TAG = "PreferenceManager";
	
	private final static boolean DEBUG = false;
	
//	static Context mContext = null;
	
	public enum PREFERENCE_TAG {
		FTP_CONFIG("FtpConfig"),
		
		TRAINING_DATA_CONFIG("TrainingDataConfig");
		
	    private final String text;

	    /**
	     * @param text
	     */
        PREFERENCE_TAG(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }	
	}
	
	public static Object loadSettings(Context c, PREFERENCE_TAG prefTag) {
		Object obj = null;
		SharedPreferences settings = c.getSharedPreferences(prefTag.toString(), 0);
		Log.d(TAG, "Loading settings from " + prefTag.toString());
		
		switch (prefTag) {
			case FTP_CONFIG:
				FTPConfig ftpConfig = new FTPConfig();
//				//YT
//				ftpConfig.setHostname(settings.getString("ftp_server_hostname", "169.228.38.2"));//"140.113.194.5"));//169.228.38.2
//				ftpConfig.setPort(settings.getInt("ftp_server_port", 21));
//				ftpConfig.setUsername(settings.getString("ftp_server_username", "anonymous"));//"gary"));//"gary"));
//				ftpConfig.setPassword(settings.getString("ftp_server_password", "j;6u.32k6"));//"eck8ib5ja"));//"eck8ib5ja"));
// 				ftpConfig.setHomeDir(settings.getString("ftp_server_home_dir", "/pub/YT/trainingdata"));//"trainingdata"));

				//Gary. This one works
//				ftpConfig.setHostname(settings.getString("ftp_server_hostname", "140.113.194.5"));//169.228.38.2
//				ftpConfig.setPort(settings.getInt("ftp_server_port", 21));
//				ftpConfig.setUsername(settings.getString("ftp_server_username", "gary"));
//				ftpConfig.setPassword(settings.getString("ftp_server_password", "eck8ib5ja"));
//				ftpConfig.setHomeDir(settings.getString("ftp_server_home_dir", "trainingdata"));
//
				//james
				ftpConfig.setHostname(settings.getString("ftp_server_hostname", "137.110.244.89"));
				ftpConfig.setPort(settings.getInt("ftp_server_port", 21));
				ftpConfig.setUsername(settings.getString("ftp_server_username", "james"));
				ftpConfig.setPassword(settings.getString("ftp_server_password", "james"));
				ftpConfig.setHomeDir(settings.getString("ftp_server_home_dir", "trainingdata"));
				obj = ftpConfig;
				break;
		case TRAINING_DATA_CONFIG:
				TrainingDataConfig trainingDataConfig = new TrainingDataConfig();
				trainingDataConfig.setTrainingDataDir(settings.getString("dir", Environment.getExternalStorageDirectory() + "/trainingdata"));

				obj = trainingDataConfig;
			break;
		default:
			break;
				
				
		}	
		
		return obj;
	}

	public synchronized static boolean saveSettings(Context c, PREFERENCE_TAG prefTag, Object obj) {
		SharedPreferences settings = c.getSharedPreferences(prefTag.toString(), 0);
		Log.d(TAG, "Saving settings to " + prefTag.toString());
		SharedPreferences.Editor editor = settings.edit();
		
		switch (prefTag) {
			case FTP_CONFIG:
				FTPConfig ftpConfig = (FTPConfig)obj;
				editor.putString("ftp_server_hostname", ftpConfig.getHostname());
				editor.putInt("ftp_server_port", ftpConfig.getPort());
				editor.putString("ftp_server_username", ftpConfig.getUsername());
				editor.putString("ftp_server_password", ftpConfig.getPassword());
				editor.putString("ftp_server_home_dir", ftpConfig.getHomeDir());
				break;
		case TRAINING_DATA_CONFIG:
				TrainingDataConfig trainingDataConfig = (TrainingDataConfig)obj;
				editor.putString("dir", trainingDataConfig.getTrainingDataDir());
			break;
		default:
			break;
			
		}	
		
		return editor.commit();
	}
	
	/**
	 * Removes all shared preferences in the application.
	 * @param c Activity context.
	 * @return true if all the shared preference is removed successfully
	 */
	public static boolean removeAllSharedPreferences(Context c) {
		for (PREFERENCE_TAG prefTag : PREFERENCE_TAG.values()) {
			SharedPreferences preferences = c.getSharedPreferences(prefTag.toString(), Context.MODE_PRIVATE);
			boolean clearSuccess = preferences.edit().clear().commit();
			
			if (!clearSuccess) {
				return false;
			}
		}
		return true;
	}

}
