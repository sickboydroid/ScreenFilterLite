package com.tangledbytes.screenfilter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class FilterUtils {
	private static final String TAG = "FilterUtils";
	private final Context mContext;

	public static final int MODE_CUSTOM = 0;
	public static final int MODE_NIGHT = 1;
	public static final int MODE_DAY = 2;
	public static final int MODE_READING = 3;
	public static final int MODE_GAMING = 4;

	public static final int MIN_COLOR = 0;
	public static final int MIN_DARKNESS = 0;
	public static final int DEFAULT_COLOR = 80;
	public static final int DEFAULT_DARKNESS = 30;
	public FilterUtils(Context context) {
		mContext = context;
	}

	public void updateFilterColor(int newColor) {
		if (getFilterColor() == newColor)
			return;
		if (getFilterMode() == MODE_CUSTOM)
			updateCustomFilterColor(newColor);
		editPref(AppConstants.KEY_FILTER_COLOR, newColor);
	}

	public void updateFilterDarkness(int newDarkness) {
		if (getFilterDarkness() == newDarkness)
			return;
		if (getFilterMode() == MODE_CUSTOM)
			updateCustomFilterDarkness(newDarkness);
		editPref(AppConstants.KEY_FILTER_DARKNESS, newDarkness);
	}

	public int getFilterColor() {
		return getPref(AppConstants.KEY_FILTER_COLOR, DEFAULT_COLOR);
	}

	public int getFilterDarkness() {
		return getPref(AppConstants.KEY_FILTER_DARKNESS, DEFAULT_DARKNESS);
	}

	private void updateCustomFilterColor(int newCustomColor) {
		if (getCustomFilterColor() == newCustomColor)
			return;
		editPref(AppConstants.KEY_FILTER_CUSTOM_COLOR, newCustomColor);
	}

	private void updateCustomFilterDarkness(int newCustomDarkness) {
		if (getCustomFilterDarkness() == newCustomDarkness)
			return;
		editPref(AppConstants.KEY_FILTER_CUSTOM_DARKNESS, newCustomDarkness);
	}

	private int getCustomFilterColor() {
		return getPref(AppConstants.KEY_FILTER_CUSTOM_COLOR, DEFAULT_COLOR);
	}

	private int getCustomFilterDarkness() {
		return getPref(AppConstants.KEY_FILTER_CUSTOM_DARKNESS, DEFAULT_DARKNESS);
	}

	public int getPref(String key, int defaultValue) {
		if (mContext == null) {
			Log.e(TAG, "Could'nt get peference (" + key + ") because Context is null!");
			return -1;
		} 
		SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
										  Context.MODE_PRIVATE);
		return filterPrefs.getInt(key, 
								  defaultValue);
	}

	public void setFilterMode(int mode) {
		editPref(AppConstants.KEY_FILTER_MODE, mode);
		switch (mode) {
			case MODE_CUSTOM:
				updateFilterColor(getCustomFilterColor());
				updateFilterDarkness(getCustomFilterDarkness());
				break;
			case MODE_NIGHT:
				updateFilterColor(50);
				updateFilterDarkness(60);
				break;
			case MODE_DAY:
				updateFilterColor(40);
				updateFilterDarkness(0);
				break;
			case MODE_READING:
				updateFilterColor(50);
				updateFilterDarkness(60);
				break;
			case MODE_GAMING:
				updateFilterColor(30);
				updateFilterDarkness(30);
				break;
		}
	}

	public int getFilterMode() {
		return getPref(AppConstants.KEY_FILTER_MODE, MODE_NIGHT);
	}

	public void editPref(String key, int newValue) {
		if (mContext == null) {
			Log.e(TAG, "Could'nt store preference (" + key + ") because Context is null!");
			return;
		}
		SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
										  Context.MODE_PRIVATE);
		filterPrefs.edit()
			.putInt(key, newValue)
			.apply();
	}

	public void setIsFilterOn(boolean on) {
		SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
										  Context.MODE_PRIVATE);
		filterPrefs.edit()
			.putBoolean(AppConstants.KEY_FILTER_SERVICE_RUNNING, on)
			.apply();
	}

	public boolean isFilterOn() {
		SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
										  Context.MODE_PRIVATE);
		return filterPrefs.getBoolean(AppConstants.KEY_FILTER_SERVICE_RUNNING,
								  false);
	}
}
