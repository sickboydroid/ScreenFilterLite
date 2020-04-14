package com.gameofcoding.screenfilter.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.gameofcoding.screenfilter.ModifiedClasses.BaseActivity;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Services.ScreenFilterService;
import com.gameofcoding.screenfilter.Utils.AppConstants;
import com.gameofcoding.screenfilter.Utils.FilterUtils;
import com.google.android.gms.ads.MobileAds;
import java.util.Arrays;
import java.util.List;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	private final Context mContext = this;
	private final FilterUtils mFilterUtils = new FilterUtils(mContext);

	// Views
	Spinner mSpinnerFilterModes;
	private SeekBar mSeekBarFilterColor;
	private SeekBar mSeekBarFilterDarkness;
	private ToggleButton mToggleFilter;
	private SharedPreferences mFilterPrefs;
	private AdView mAdView;
	private InterstitialAd mInterstitialAd;

	// Listeners
	/**
	 * Called when user clicks on #mToggleFilter ToggleButton for turning on/off the
	 * filter.
	 **/
	private final CompoundButton.OnCheckedChangeListener mToggleFilterCheckedChangeListener =
	new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton btn, boolean checked) {
			if (checked)
				btn.setChecked(startFilter());
			else
				btn.setChecked(!stopFilter());
		}
	};

	/**
	 * Called when preferences of filter are changed.
	 */
	private final SharedPreferences.OnSharedPreferenceChangeListener mFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals(AppConstants.KEY_FILTER_SERVICE_RUNNING)) {
				// Filter service toggled (May be from dialog)
				mToggleFilter.setOnCheckedChangeListener(null);
				mToggleFilter.setChecked(mFilterUtils.isFilterOn());
				mToggleFilter.setOnCheckedChangeListener(mToggleFilterCheckedChangeListener);
			} else if (key.equals(AppConstants.KEY_FILTER_COLOR)) {
				// Color of filter has been changed
				int filterColor = mFilterUtils.getFilterColor();
				if (filterColor != mSeekBarFilterColor.getProgress())
					mSeekBarFilterColor.setProgress(filterColor);
			} else if (key.equals(AppConstants.KEY_FILTER_DARKNESS)) {
				// Darkness of filter has been changed
				int filterDarkness = mFilterUtils.getFilterDarkness();
				if (filterDarkness != mSeekBarFilterDarkness.getProgress())
					mSeekBarFilterDarkness.setProgress(filterDarkness);
			} else if (key.equals(AppConstants.KEY_FILTER_MODE)) {
				// Mode of filter has been changed
				int filterMode = mFilterUtils.getFilterMode();
				if (filterMode != mSpinnerFilterModes.getSelectedItemPosition())
					mSpinnerFilterModes.setSelection(mFilterUtils.getFilterMode());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize Views.
		mSpinnerFilterModes = findViewById(R.id.spinner_filter_modes);
		final TextView tvFilterColor = findViewById(R.id.filter_color_percent);
		final TextView tvFilterDarkness = findViewById(R.id.filter_darkness_percent);
		mSeekBarFilterColor = findViewById(R.id.filter_color);
		mSeekBarFilterDarkness = findViewById(R.id.filter_darkness);
		mToggleFilter = findViewById(R.id.toggle_filter);
		
		// Setup ads
		MobileAds.initialize(this, "ca-app-pub-9943487589376556~7982987830");
		mAdView = findViewById(R.id.adView);
		mInterstitialAd = new InterstitialAd(mContext);
		//mInterstitialAd.setAdUnitId("ca-app-pub-9943487589376556/6930648662");
		mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		mInterstitialAd.loadAd(adRequest);
		mFilterPrefs =
			getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, MODE_PRIVATE);

		// Setup Views
		mSpinnerFilterModes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapView, View view, int position, long id) {
					Toast.makeText(mContext, getResources().getStringArray(R.array.filter_modes)[position], Toast.LENGTH_SHORT).show();
					mFilterUtils.setFilterMode(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapView) {
				}
			});  

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.filter_modes));  
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        mSpinnerFilterModes.setAdapter(arrayAdapter);  
		mSpinnerFilterModes.setSelection(mFilterUtils.getFilterMode());
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
						mSpinnerFilterModes.setSelection(FilterUtils.MODE_CUSTOM);
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
						mSpinnerFilterModes.setSelection(FilterUtils.MODE_CUSTOM);
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
		mToggleFilter.setTextOn(getString(R.string.stop_filter));
		mToggleFilter.setTextOff(getString(R.string.start_filter));
		mToggleFilter.setChecked(isFilterServiceRunning());
		mToggleFilter.setOnCheckedChangeListener(mToggleFilterCheckedChangeListener);
		mFilterPrefs
			.registerOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				if(mInterstitialAd.isLoaded())
					mInterstitialAd.show();
				startActivity(new Intent(MainActivity.this, AppPreferenceActivity.class));
				return true;
			case R.id.menu_about:
				startActivity(new Intent(MainActivity.this, AboutActivity.class));
				return true;
			case R.id.menu_exit:
				if(mInterstitialAd.isLoaded())
					mInterstitialAd.show();
				finish();
				return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		mFilterPrefs
			.unregisterOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
		super.onDestroy();
	}

	/**
	 * Starts filter Service if not running.
	 *
	 * @return true if filter Service started.
	 **/
	private boolean startFilter() {
		if (hasOverlayPermission()) {
			if (!isFilterServiceRunning()) {
				startService(new Intent(MainActivity.this, ScreenFilterService.class));
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
			stopService(new Intent(MainActivity.this, ScreenFilterService.class));
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the app has overlay permission or not.
	 *
	 * @return true if has permission.
	 **/
	private boolean hasOverlayPermission() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
			Log.i(TAG, "hasScreenFilterPermission(): Android version '< Kitkat', so no need for checking overly permission!");
			return true;
		} else if (Settings.canDrawOverlays(this)) {
			Log.i(TAG, "Overly Permission Allowed!");
			return true;
		} else {
			Log.i(TAG, "Overly Permission Denied!");
			return false;
		}
	}

	/**
	 * Shows a user dialog which navigates him to settings for granting overlay
	 * permission.
	 **/
	private void grantPermission() {
		if (hasOverlayPermission())
			return;
		Log.i(TAG, "Requesting for permission...");
		new AlertDialog.Builder(mContext)
			.setTitle(R.string.permission_request)
			.setMessage(R.string.permission_request_desc)
			.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent overlayPermissionActivity =	new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
																  Uri.parse(AppConstants.SCHEME_PACKAGE + getPackageName()));
					startActivity(overlayPermissionActivity);
				}
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
