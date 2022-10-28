package com.tangledbytes.screenfilter.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tangledbytes.screenfilter.R;
import com.tangledbytes.screenfilter.services.ScreenFilterService;
import com.tangledbytes.screenfilter.utils.AppConstants;
import com.tangledbytes.screenfilter.utils.FilterUtils;
import com.tangledbytes.screenfilter.utils.Utils;

import java.util.List;

public class FilterDialogActivity extends Activity {
	private static final String TAG = "FilterDialogActivity";
	private final Context mContext = this;
	FilterUtils mFilterUtils = new FilterUtils(mContext);


	// Views
	private SeekBar mSeekBarFilterColor;
	private SeekBar mSeekBarFilterDarkness;
	private SharedPreferences mFilterPrefs;

	// Listeners
	/**
	 * Called when user clicks on #mToggleFilter ToggleButton for turning on/off the
	 * filter.
	 **/
	private final CompoundButton.OnCheckedChangeListener mToggleFilterCheckedChangeListener =
			(btn, checked) -> {
				mFilterUtils.setIsFilterOn(checked);
				if (checked)
					btn.setChecked(startFilter());
				else
					btn.setChecked(!stopFilter());
			};
	/**
	 * Called when preferences of filter are changed.
	 */
	private final SharedPreferences.OnSharedPreferenceChangeListener mFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals(AppConstants.KEY_FILTER_COLOR)) {
				// Color of filter has been changed
				int filterColor = mFilterUtils.getFilterColor();
				if (filterColor != mSeekBarFilterColor.getProgress())
					mSeekBarFilterColor.setProgress(filterColor);
			} else if (key.equals(AppConstants.KEY_FILTER_DARKNESS)) {
				// Darkness of filter has been changed
				int filterDarkness = mFilterUtils.getFilterDarkness();
				if (filterDarkness != mSeekBarFilterDarkness.getProgress())
					mSeekBarFilterDarkness.setProgress(filterDarkness);
			}
		}
	};
	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_filter_settings);
		// Initialize Views.
		final Spinner spinnerFilterModes = findViewById(R.id.spinner_filter_modes);
		final TextView tvFilterColor = findViewById(R.id.filter_color_percent);
		final TextView tvFilterDarkness = findViewById(R.id.filter_darkness_percent);
		mSeekBarFilterColor = findViewById(R.id.filter_color);
		mSeekBarFilterDarkness = findViewById(R.id.filter_darkness);
		AdView mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		SwitchCompat mToggleFilter = findViewById(R.id.toggle_filter);
		mFilterPrefs =
			getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, MODE_PRIVATE);

		// Setup Views
		spinnerFilterModes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapView, View view, int position, long id) {
					Toast.makeText(mContext, getResources().getStringArray(R.array.filter_modes)[position], Toast.LENGTH_SHORT).show();
					mFilterUtils.setFilterMode(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapView) {
				}
			});  
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.filter_modes));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        spinnerFilterModes.setAdapter(arrayAdapter);  
		spinnerFilterModes.setSelection(mFilterUtils.getFilterMode());
		mSeekBarFilterColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
					if (fromUser && mFilterUtils.getFilterMode() != FilterUtils.MODE_CUSTOM) {
						mFilterPrefs.unregisterOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
						mFilterUtils.setFilterMode(FilterUtils.MODE_CUSTOM);
						spinnerFilterModes.setSelection(FilterUtils.MODE_CUSTOM);
						tvFilterColor.setText(prog + "%");
						mFilterUtils.updateFilterColor(prog);
						mFilterPrefs.registerOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
					} else {
						tvFilterColor.setText(prog + "%");
						mFilterUtils.updateFilterColor(prog);
					}
				}
			});
		mSeekBarFilterDarkness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
					if (fromUser && mFilterUtils.getFilterMode() != FilterUtils.MODE_CUSTOM) {
						mFilterPrefs.unregisterOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
						mFilterUtils.setFilterMode(FilterUtils.MODE_CUSTOM);
						spinnerFilterModes.setSelection(FilterUtils.MODE_CUSTOM);
						tvFilterDarkness.setText(prog + "%");
						mFilterUtils.updateFilterDarkness(prog);
						mFilterPrefs.registerOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
					} else {
						tvFilterDarkness.setText(prog + "%");
						mFilterUtils.updateFilterDarkness(prog);
					}
				}
			});
		mSeekBarFilterColor.setProgress(mFilterUtils.getFilterColor());
		mSeekBarFilterDarkness.setProgress(mFilterUtils.getFilterDarkness());
		mToggleFilter.setChecked(isFilterServiceRunning());
		mToggleFilter.setOnCheckedChangeListener(mToggleFilterCheckedChangeListener);
		mFilterPrefs
			.registerOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
	}
	
	public void ok(View v) {
		finish();
	}
	
	public void openApp(View v) {
		try{
			finish();
		} finally {
			startActivity(new Intent(this, MainActivity.class));
		}
	}
	
	/**
	 * Starts filter Service if not running.
	 *
	 * @return true if filter Service started.
	 **/
	private boolean startFilter() {
		if (Utils.hasOverlayPermission(this)) {
			if (!isFilterServiceRunning()) {
				startService(new Intent(FilterDialogActivity.this, ScreenFilterService.class));
				return true;
			} 
		} else {
			grantPermission();
			return false;
		}
		return false;
	}

	/**
	 * Stops filter Service if running.
	 *
	 * @return true if filter Service stoped.
	 **/
	private boolean stopFilter() {
		if (isFilterServiceRunning()) {
			stopService(new Intent(mContext, ScreenFilterService.class));
			return true;
		}
		return false;
	}

	/**
	 * Shows a user dialog which navigates him to settings for granting overlay
	 * permission.
	 **/
	private void grantPermission() {
		if (Utils.hasOverlayPermission(this))
			return;
		Log.i(TAG, "Requesting for permission...");
		new AlertDialog.Builder(mContext)
			.setTitle(R.string.permission_request)
			.setMessage(R.string.permission_request_desc)
			.setPositiveButton(R.string.go_to_settings, (dialog, whichButton) -> {
				Intent overlayPermissionActivity =	new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
															  Uri.parse(AppConstants.SCHEME_PACKAGE + getPackageName()));
				startActivity(overlayPermissionActivity);
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}

	/**
	 * Checks whether the filter Service is running or not. It get all available Services
	 * that are currently running on device. Then it finds filter Serrvice and checks if
	 * it is running.
	 **/
	private boolean isFilterServiceRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
		int size = runningServices.size();
		for (int i = 0; i < size; i++) {
			if (runningServices.get(i).service.getPackageName().equals(getPackageName())) {
				// It is from this application.
				if (runningServices.get(i).service.getClassName().equals(ScreenFilterService.class.getName()))
				// It is screen filter service.
					return runningServices.get(i).started;
			}
		}
		return false;
	}
}
