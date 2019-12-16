package com.gameofcoding.screenfilter.Activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.gameofcoding.screenfilter.R;

public class AppPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
