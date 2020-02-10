package com.gameofcoding.screenfilter.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.gameofcoding.screenfilter.Activities.MainActivity;
import com.gameofcoding.screenfilter.Utils.AppConstants;

public class FilterUtils {
	private static final String TAG = "FilterUtils";
	private Context mContext;
	/* Constants */
	/**
	 * Maximum opactity that user can set to filter.
	 **/
	public static final int MAX_OPACITY = 225;
	/**
	 * Minimum opactity that user can set to filter.
	 **/
	public static final int MIN_OPACITY = 30;
	/**
	 * Default opacity of filter. It is used when user opens application for the first
	 * time.
	 **/
	public static final int DEFAULT_OPACITY = 80;
	public FilterUtils(Context context) {
		mContext = context;
	}

	public boolean updateFilterOpacity(int newOpacity) {
		if (mContext == null) {
			Log.e(TAG, "Could'nt store opacity because Context is null!");
			return false;
		} else if (getFilterOpacity() == newOpacity) {
			return true;
		}
      SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
			Context.MODE_PRIVATE);
		boolean updatedOpacity = filterPrefs.edit()
			.putInt(AppConstants.KEY_COLOR_OPACITY, newOpacity)
			.commit();
		if (updatedOpacity)
			Log.i(TAG, "Opacity updated, opacity=" + newOpacity);
		else
			Log.e(TAG, "Could'nt update opacity, opacity=" + newOpacity + ", context=" + mContext.toString());
		return updatedOpacity;
	}

	public int getFilterOpacity() {
		if (mContext == null) {
			Log.e(TAG, "Could'nt get opacity because Context is null!");
			return -1;
		} 
		SharedPreferences filterPrefs =
			mContext.getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, 
			Context.MODE_PRIVATE);
		return filterPrefs.getInt(AppConstants.KEY_COLOR_OPACITY, 
			DEFAULT_OPACITY);
	}
}
