package com.cerebratek.uihelper;

import android.content.Context;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter extends ArrayAdapter<String> {

	public CustomArrayAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
	}

}
