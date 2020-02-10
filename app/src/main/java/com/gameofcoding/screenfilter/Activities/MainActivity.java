package com.gameofcoding.screenfilter.Activities;

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
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.gameofcoding.screenfilter.ModifiedClasses.BaseActivity;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Services.ScreenFilterService;
import com.gameofcoding.screenfilter.Utils.AppConstants;
import com.gameofcoding.screenfilter.Utils.FilterUtils;
import java.util.List;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	private final Context mContext = this;
	private final FilterUtils mFilterUtils = new FilterUtils(mContext);

	// Views
	private SeekBar mSeekBarFilterOpacity;
	private ToggleButton mToggleFilter;
	private SharedPreferences mFilterPrefs;

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
			if (key.equals(AppConstants.KEY_COLOR_OPACITY)) {
				// Opacity of filter has been changed
				int filterOpacity = mFilterUtils.getFilterOpacity();
				if (filterOpacity != mSeekBarFilterOpacity.getProgress())
					mSeekBarFilterOpacity.setProgress(filterOpacity);
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize Views.
		final TextView tvFilterOpacity = findViewById(R.id.filter_opacity_percent);
		mSeekBarFilterOpacity = findViewById(R.id.filter_opacity);
		mToggleFilter = findViewById(R.id.toggle_filter);
		mFilterPrefs = 
			getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, MODE_PRIVATE);

		// Setup Views
		mSeekBarFilterOpacity.setMax(FilterUtils.MAX_OPACITY);
		mSeekBarFilterOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int prog, boolean throughTouch) {
					if (prog >= FilterUtils.MIN_OPACITY) {
						tvFilterOpacity.setText(prog + getString(R.string.filter_opacity_percent));
						mFilterUtils.updateFilterOpacity(prog);
					} else {
						seekBar.setProgress(FilterUtils.MIN_OPACITY);
					}
				}
			});
		mSeekBarFilterOpacity.setProgress(mFilterUtils.getFilterOpacity());
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
				startActivity(new Intent(MainActivity.this, AppPreferenceActivity.class));
				return true;
			case R.id.menu_about:
				startActivity(new Intent(MainActivity.this, AboutActivity.class));
				return true;
			case R.id.menu_exit:
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
		List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(999999999);
		int size = runningServices.size();
		for (int i = 0; i < size; i++) {
			ActivityManager.RunningServiceInfo runningService = runningServices.get(i);
			if (runningService.service.getPackageName().equals(getPackageName())) {
				// It is from this application.
				if (runningService.service.getClassName().equals(ScreenFilterService.class.getName()))
				// It is screen filter service.
					return runningService.started;
			}
		}
		return false;
	}
}
