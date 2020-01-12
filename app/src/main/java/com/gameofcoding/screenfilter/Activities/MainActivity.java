package com.gameofcoding.screenfilter.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.gameofcoding.screenfilter.ModifiedClasses.BaseActivity;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Services.ScreenFilterService;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import android.app.Service;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.IntentService;

public class MainActivity extends BaseActivity {
	private final String TAG = "MainActivity";
	private final Context mContext = this;
	SeekBar mSeekBarFilterOpacity;
	ToggleButton mToggleBtnFilter;
	SharedPreferences mSharedPrefsScreenFilter;

	private CompoundButton.OnCheckedChangeListener mToggleBtnFilterCheckedChangeListener = new CompoundButton.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton btn, boolean checked) {
			if (checked)
				mToggleBtnFilter.setChecked(startFilter());
			else
				mToggleBtnFilter.setChecked(!stopFilter());
		}
	};
	private SharedPreferences.OnSharedPreferenceChangeListener mSharedScreenFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals(ScreenFilterService.KEY_SERVICE_RUNNING)) {
				mToggleBtnFilter.setOnCheckedChangeListener(null);
				mToggleBtnFilter.setChecked(isScreenFilterServiceRunning());
				mToggleBtnFilter.setOnCheckedChangeListener(mToggleBtnFilterCheckedChangeListener);
			} else if (key.equals(ScreenFilterService.KEY_COLOR_OPACITY))
				mSeekBarFilterOpacity.setProgress(sharedPrefs.getInt(ScreenFilterService.KEY_COLOR_OPACITY, 
						mSeekBarFilterOpacity.getProgress()));
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final TextView tvFilterOpacityPercent = findViewById(R.id.filter_opacity_percent);
		mSharedPrefsScreenFilter = getSharedPreferences(ScreenFilterService.KEY_SHARED_PREF_SCREEN_FILTER, MODE_PRIVATE);
		mToggleBtnFilter = findViewById(R.id.toggle_filter);
		mSeekBarFilterOpacity = findViewById(R.id.filter_opacity);
		mSeekBarFilterOpacity.setMax(225);
		mSeekBarFilterOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int prog, boolean throughTouch) {
					if (prog >= 30) {
						tvFilterOpacityPercent.setText(prog + "%");
						mSharedPrefsScreenFilter.edit()
							.putInt(ScreenFilterService.KEY_COLOR_OPACITY, prog)
							.commit();
					} else
						seekBar.setProgress(30);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		mSeekBarFilterOpacity.setProgress(mSharedPrefsScreenFilter
			.getInt(ScreenFilterService.KEY_COLOR_OPACITY, 80));
		
		// FIXME: Don't start filter here instead know how to know that is filter
		// service runnung or not
		startFilter(true);
		mToggleBtnFilter.setChecked(isScreenFilterServiceRunning());
	 	mToggleBtnFilter.setOnCheckedChangeListener(mToggleBtnFilterCheckedChangeListener);
		mSharedPrefsScreenFilter
			.registerOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
    }
	
	private boolean startFilter() {
		return startFilter(false);
	}
	
	private boolean startFilter(boolean forcebly) {
		if (forcebly || (!isScreenFilterServiceRunning())) {
			if (hasScreenFilterPermission()) {
				startService(new Intent(MainActivity.this, ScreenFilterService.class));
				return true;
			} else {
				grantPermission();
				return false;
			}
		}
		return false;
	}

	private void grantPermission() {
		new AlertDialog.Builder(mContext)
			.setTitle(R.string.permission_request)
			.setMessage(R.string.permission_request_desc)
			.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						Intent overlayPermissionActivity =	new Intent(
							Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
							Uri.parse("package:" + getPackageName()));
						startActivity(overlayPermissionActivity);
					} catch (Exception e) {
						StringBuilder logMessage = new StringBuilder();
						logMessage.append("grantPermission():");
						logMessage
							.append(" Exception occurred while starting overlay permission activity.");
						logMessage.append(" Getting permission in simple way!");
						Log.e(TAG, logMessage.toString(), e);
						startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
						showToast(R.string.grant_permission_instruction, false);
						showToast(R.string.grant_permission_instruction);
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}

	private boolean stopFilter() {
		if (isScreenFilterServiceRunning())
			stopService(new Intent(MainActivity.this, ScreenFilterService.class));
		mSharedPrefsScreenFilter.edit()
			.putBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, false)
			.commit();
		return true;
	}

	public boolean isScreenFilterServiceRunning() {
		return mSharedPrefsScreenFilter
			.getBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
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
		mSharedPrefsScreenFilter
			.unregisterOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
		super.onDestroy();
	}

	private boolean hasScreenFilterPermission() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
			Log.e(TAG, "hasScreenFilterPermission(): Android version '< Kitkat', so no need for checking overly permission!");
			return true;
		} else if (Settings.canDrawOverlays(this)) {
			Log.e(TAG, "Overly Permission Allowed!");
			return true;
		} else {
			Log.e(TAG, "Overly Permission Denied!");
			return false;
		}
	}
}
