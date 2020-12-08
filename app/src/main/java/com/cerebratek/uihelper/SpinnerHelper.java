package com.cerebratek.uihelper;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class SpinnerHelper {
	
	public enum SPINNER_TYPE {
		RECORDING_SESSION_NAME("Recording Session Name"),
		
		RECORDING_METADATA_ACTIVITY_NAME("Recording Metadata Activity Name"),
		RECORDING_METADATA_SUBJECT_NAME("Recording Metadata Subject Name"),
		RECORDING_METADATA_LOCATION_NAME("Recording Metadata Location Name"),
		
		RECORDING_PROGRAM_RUNNING_PROFILE("Recording Program Running Profile"),
		
		REALTIME_GRAPH_UPDATE_INTERVAL("Realtime Graph Update Interval"),
		VISUAL_STIMULI_TYPE("Visual Stimuli Type"),
		VISUAL_STIMULI_DIRECTION("Visual Stimuli Direction"),
		
		MFSSVEP_STIMULI_PROFILE("Mfssvep Stimuli Profile"),
		
		VISUAL_STIMULI_SCREEN_REFRESH_RATE("Visual Stimuli Screen Refresh Rate"),
		
		DATA_STREAM_FORMAT("Data Stream Format"),
		RECORDING_FILE_FORMAT("Recording File Format");
		
	    private final String text;

	    /**
	     * @param text
	     */
        SPINNER_TYPE(final String text) {
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
	
	/**
	 * Update the UI list adapter
	 * @param spinner 
	 * @param list
	 */
	public static void updateListAdapter(final Activity activity, final Spinner spinner, final ArrayList<String> list) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ArrayAdapter<String> arrayAdapter;
				arrayAdapter = new ArrayAdapter<String>(activity,
						android.R.layout.simple_spinner_item, list);
				spinner.setAdapter(arrayAdapter);
			}
		});
	}
	
	/**
	 * Update spinner list with default "Add new Item" as the first row 
	 * @param activity
	 * @param spinner
	 * @param list
	 * @param defaultItemName Default item row name to show in the first row, e.g. Create New Item
	 */
	public static void updateListAdapterWithDefaultAdd(final Activity activity, final Spinner spinner, final ArrayList<String> list, final String defaultItemName) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ArrayAdapter<String> arrayAdapter;

				if (list == null) { 
					ArrayList<String> tmpList = new ArrayList<String>();
					tmpList.add(0, "<< Select a value >>");
					tmpList.add(1, "<<" + defaultItemName + ">>");
					arrayAdapter = new ArrayAdapter<String>(activity,
							android.R.layout.simple_spinner_item, tmpList);
				}
				
				else {
					list.add(0, "<< Select a value >>");
					list.add(1, "<<" + defaultItemName + ">>");
					arrayAdapter = new ArrayAdapter<String>(activity,
							android.R.layout.simple_spinner_item, list);
				}
			
				spinner.setAdapter(arrayAdapter);
			}
		});
	}
	
//	/**
//	 * Populate spinner values automatically, this will implicitly call updateListAdapter(activity, spinner, list)
//	 * @param spinnerType
//	 * @param activity
//	 * @param spinner
//	 */
//	public static void loadSpinnerValues(final SPINNER_TYPE spinnerType, final Activity activity, final Spinner spinner) {
//		final ArrayList<String> list = new ArrayList<String>();
//		
//		switch (spinnerType) {
//		case RECORDING_SESSION_NAME:
//			List<String> recordingSessionMetadatas = null;
//			try {
//				recordingSessionMetadatas = ProtobufEncoder.load(activity, ProtobufEncoder.FileTag.RECORDING_SESSION_METADATA);
//			} catch (ProtobufFileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// don't populate the array adapter if no profile is found!
//			if (recordingSessionMetadatas == null) 
//				break; 
//			
//			for (String sessionName : recordingSessionMetadatas) {
//				list.add(sessionName);
//			}
//			
//			break;
//			
//		case RECORDING_METADATA_ACTIVITY_NAME:
//			List<String> recordingMetadataActivityNameList = null;
//			try {
//				recordingMetadataActivityNameList = ProtobufEncoder.load(activity, ProtobufEncoder.FileTag.RECORDING_METADATA$RECORDING_LABEL);
//			} catch (ProtobufFileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// don't populate the array adapter if no profile is found!
//			if (recordingMetadataActivityNameList == null)
//				return;
//			
//			for (String activityName : recordingMetadataActivityNameList) {
//				list.add(activityName);
//			}
//
//			// sort the names alphabeticaly
//			Collections.sort(list);
//			break;
//			
//		case RECORDING_METADATA_SUBJECT_NAME:
//			List<SubjectProfile> subjectProfileList = null;
//			try {
//				subjectProfileList = ProtobufEncoder.load(activity, ProtobufEncoder.FileTag.SUBJECT_PROFILE);
//			} catch (ProtobufFileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// don't populate the array adapter if no profile is found!
//			if (subjectProfileList == null)
//				return;
//			
//			for (SubjectProfile sp : subjectProfileList) {
//				list.add(sp.getSubjectId());
//			}
//			
//			Collections.sort(list);
//			break;
//			
//		case RECORDING_METADATA_LOCATION_NAME:
//			List<String> recordingMetadataLocationNameList = null;
//			try {
//				recordingMetadataLocationNameList = ProtobufEncoder.load(activity, ProtobufEncoder.FileTag.RECORDING_METADATA$LOCATION_NAME);
//			} catch (ProtobufFileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// don't populate the array adapter if no profile is found!
//			if (recordingMetadataLocationNameList == null)
//				return;
//			
//			for (String locationName : recordingMetadataLocationNameList) {
//				list.add(locationName);
//			}
//				
//			Collections.sort(list);
//			break;
//			
//		case RECORDING_PROGRAM_RUNNING_PROFILE:
//			for (RECORD_RUNNING_PROFILE act : RECORD_RUNNING_PROFILE.values()) {
//				list.add(act.getDisplayName());
//			}
//			break;
//		
//		
//		case REALTIME_GRAPH_UPDATE_INTERVAL:
//			DecimalFormat df = new DecimalFormat("#.#");
//			df.setRoundingMode(RoundingMode.CEILING);
//			
////			list.add("0.01");
////			list.add("0.05");
//			
//			for (float i = 0.09f ; i <= 2.0f ; i+=0.1f) {
//				String r = df.format(i);
//				if (r.length() == 1) // make sure 1 = 1.0 and 2 = 2.0
//					list.add(r + ".0");
//				else
//					list.add(r);
//			}
//			
//			break;
//			
//		case VISUAL_STIMULI_TYPE:
//			for (VisualStimuliSpinnerList.VISUAL_STIMULI_TYPE vsType : VisualStimuliSpinnerList.VISUAL_STIMULI_TYPE.values()) {
//				if (vsType.toString().equalsIgnoreCase(VISUAL_STIMULI_TYPE.INTERPUPIL_DISTANCE_CALIBRATION.toString()))
//					continue;
//				list.add(vsType.toString());
//			}
//			break;
//			
//		case VISUAL_STIMULI_DIRECTION:
//			for (VisualStimuliSpinnerList.STIMULI_DIRECTION stiDir : VisualStimuliSpinnerList.STIMULI_DIRECTION.values()) {
//				list.add(stiDir.toString());
//			}
//			break;
//			
//		case MFSSVEP_STIMULI_PROFILE:
//			List<MfssvepStimuliProfile> mfssvepStimuliProfiles = null;
//			try {
//				mfssvepStimuliProfiles = ProtobufEncoder.load(activity, ProtobufEncoder.FileTag.MFSSVEP_STIMULI_PROFILE);
//			} catch (ProtobufFileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// don't populate the array adapter if no profile is found!
//			if (mfssvepStimuliProfiles == null) 
//				break; 
//			
//			for (MfssvepStimuliProfile msp : mfssvepStimuliProfiles) {
//				list.add(msp.getName());
//			}
//			
//			// Cannot sort since protobuf index are read in order
//			break;
//			
//		case VISUAL_STIMULI_SCREEN_REFRESH_RATE:
//			DecimalFormat dff = new DecimalFormat("#.#");
//			dff.setRoundingMode(RoundingMode.CEILING);
//			
//			for (float i = 58.0f ; i <= 62.0f ; i+=0.1f) {
//				String r = dff.format(i);
//				list.add(r);
//			}
//			break;
//			
//		case DATA_STREAM_FORMAT:
//			for (ADS1299SpinnerList.DATA_STREAM_FORMAT btStreamDataFormat : ADS1299SpinnerList.DATA_STREAM_FORMAT.values()) {
//				list.add(btStreamDataFormat.toString());
//			}
//			break;
//			
//		case RECORDING_FILE_FORMAT:
//			for (ADS1299SpinnerList.RECORDING_FILE_FORMAT recordingFileFormat : ADS1299SpinnerList.RECORDING_FILE_FORMAT.values()) {
//				list.add(recordingFileFormat.toString());
//			}
//			break;
//			
//		default:
//			
//		}
//		
//		updateListAdapter(activity, spinner, list);
//	}
//	
//	public static void setSelection(Spinner spinner, String value) {
//		int selectedItemIndex = 0;
//		
//
//		for (int i = 0; i < spinner.getCount(); i++) {
//			if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
//				selectedItemIndex = i;
//				break;
//			}
//		}
//		
//		spinner.setSelection(selectedItemIndex);
//	}
//	
//	public enum SpinnerListType {
//		RECORD_METADATA_SUBJECT_GENDER,
//		RECORD_METADATA_SUBJECT_YOB,
//		RECORD_METADATA_SUBJECT_HEIGHT,
//		RECORD_METADATA_SUBJECT_WEIGHT,
//		RECORD_METADATA_SUBJECT_HANDEDNESS,
//		RECORD_METADATA_SUBJECT_HEARING,
//		RECORD_METADATA_SUBJECT_VISION,
//		
//		RECORD_METADATA_SUBJECT_STATE_CAFFEINE,
//		RECORD_METADATA_SUBJECT_STATE_ALCOHOL,
//		RECORD_METADATA_SUBJECT_STATE_DROWSINESS,
//		RECORD_METADATA_SUBJECT_STATE_STRESS,
//		
//		RECORD_METADATA_CHANNEL_INFO,
//		RECORD_METADATA_CHANNEL_NUM,
//		RECORD_METADATA_CHANNEL_LABEL,
//		
//		RECORD_FILE_FOLDER_STRUCTURE,
//		RECORD_FILE_NAMING_SCHEME;
//	}
//	
//	public static ArrayList<String> getList(SpinnerListType spinnerListType) {
//		ArrayList<String> list = new ArrayList<String>();
//		
//		switch (spinnerListType) {
//		// ------------------------------------ Subject
//		case RECORD_METADATA_SUBJECT_GENDER:
//			list.add("male");
//			list.add("female");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_YOB:
//			for (int i = 1920 ; i < 2016 ; i++)
//				list.add(String.valueOf(i));
//			break;
//			
//		case RECORD_METADATA_SUBJECT_HANDEDNESS:
//			list.add("left");
//			list.add("right");
//			list.add("ambidextrous");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_HEIGHT:
//			for (int i = 100 ; i < 200 ; i++)
//				list.add(String.valueOf(i));
//			break;
//			
//		case RECORD_METADATA_SUBJECT_WEIGHT:
//			for (int i = 30 ; i < 120 ; i++)
//				list.add(String.valueOf(i));
//			break;
//			
//		case RECORD_METADATA_SUBJECT_HEARING:
//			list.add("normal");
//			list.add("impaired");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_VISION:
//			list.add("normal");
//			list.add("impaired");
//			break;
//			
//		// ------------------------------------ Subject State
//		case RECORD_METADATA_SUBJECT_STATE_CAFFEINE:
//			list.add("no");
//			list.add("low");
//			list.add("medium");
//			list.add("high");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_STATE_ALCOHOL:
//			list.add("no");
//			list.add("low");
//			list.add("medium");
//			list.add("high");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_STATE_DROWSINESS:
//			list.add("no");
//			list.add("low");
//			list.add("medium");
//			list.add("high");
//			break;
//			
//		case RECORD_METADATA_SUBJECT_STATE_STRESS:
//			list.add("no");
//			list.add("low");
//			list.add("medium");
//			list.add("high");
//			break;
//			
//		// ------------------------------------ Channel
//		case RECORD_METADATA_CHANNEL_INFO:
//			list.add("10-20");
//			list.add("10-30");
//			list.add("20-40");
//			break;
//			
//		case RECORD_METADATA_CHANNEL_NUM:
//			for (int i = 1 ; i <= 16 ; i++)
//				list.add(String.valueOf(i));
//			break;
//			
//		case RECORD_METADATA_CHANNEL_LABEL:
//			list.add("O1");
//			list.add("O2");
//			list.add("O3");
//			list.add("O4");
//			list.add("Oz");
//			list.add("C3");
//			list.add("C4");
//			list.add("Cz");
//			list.add("F3");
//			list.add("F4");
//			list.add("F7");
//			list.add("F8");
//			list.add("Fz");
//			list.add("FP1");
//			list.add("FP2");
//			list.add("P3");
//			list.add("P4");
//			list.add("Pz");
//			
//			// older electrode label
//			list.add("EOG1");
//			list.add("EOG2");
//			list.add("POz");
//			list.add("PO3");
//			list.add("PO4");
//			list.add("PO5");
//			list.add("PO6");
//			list.add("CPz");
//			
//			Collections.sort(list);
//			break;
//			
//		case RECORD_FILE_FOLDER_STRUCTURE:
//			list.add("devicename/activity/subject/session");
//			list.add("flat");
//			break;
//			
//		case RECORD_FILE_NAMING_SCHEME:
//			list.add("DateTime");
//			list.add("Subject-Activity-Session-Location-DateTime");
//			break;
//			
//
//		default:
//			break;
//		}
//		return list;
//	}
}
