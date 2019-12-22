package com.gameofcoding.screenfilter.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
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
import android.preference.PreferenceManager;

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
		// TODO: Only in alpha version
	   /*==========================================*/
		try {
			String appVersionName = null;
			try {
				PackageInfo pkInfo = getPackageManager().getPackageInfo(getPackageName(),
																		PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
				appVersionName = pkInfo.versionName;
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "Could'nt find my own app", e);
				appVersionName = "unknown";
			}
			StringBuilder mailBody = new StringBuilder();
			mailBody.append("Android Version=" + Build.VERSION.RELEASE);
			mailBody.append("\nApp Version=" + appVersionName);
			mailBody.append("\nBrand=" + Build.BRAND);
			mailBody.append("\nDevice=(" + Build.DEVICE + ")");
			mailBody.append("\nModel=" + Build.MODEL);
			mailBody.append("\nDisplay=" + Build.DISPLAY);
			mailBody.append("\nLocale=" + Locale.getDefault().getDisplayLanguage() 
							+ "-" + Locale.getDefault().getDisplayCountry());
			mailBody.append("\nSettings=" + PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext())
							.getAll().toString());
			mailBody.append("\nOther settings=" + 
							getSharedPreferences(ScreenFilterService.KEY_SHARED_PREF_SCREEN_FILTER,
												 MODE_PRIVATE).getAll().toString());
			String logCat = "\n=======MainActivity========\n" + mailBody.toString() + "\n===================\n\n" + getLogCat();
			write(getExternalCacheDir().toString() + "/ScreenFilterLog.txt", logCat, true);
			Toast.makeText(getApplicationContext(), "Wrote!", Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(), getExternalCacheDir().toString(), Toast.LENGTH_LONG).show();

			clearLog();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		/*==========================================*/
		setContentView(R.layout.activity_main);
		final TextView tvFilterOpacityPercent = findViewById(R.id.filter_opacity_percent);
		mSharedPrefsScreenFilter = getSharedPreferences(ScreenFilterService.KEY_SHARED_PREF_SCREEN_FILTER, MODE_PRIVATE);
		mToggleBtnFilter = findViewById(R.id.toggle_filter);
		mSeekBarFilterOpacity = findViewById(R.id.filter_opacity);
		mSeekBarFilterOpacity.setMax(225);
		mSeekBarFilterOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int prog, boolean throughTouch) {
					if (prog >= 10) {
						tvFilterOpacityPercent.setText(prog + "%");
						mSharedPrefsScreenFilter.edit()
							.putInt(ScreenFilterService.KEY_COLOR_OPACITY, prog)
							.commit();
					} else
						seekBar.setProgress(10);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		mSeekBarFilterOpacity.setProgress(mSharedPrefsScreenFilter
										  .getInt(ScreenFilterService.KEY_COLOR_OPACITY, 50));
		mToggleBtnFilter.setChecked(isScreenFilterServiceRunning());
	 	mToggleBtnFilter.setOnCheckedChangeListener(mToggleBtnFilterCheckedChangeListener);
		mSharedPrefsScreenFilter
			.registerOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
    }

	private boolean startFilter() {
		if (!isScreenFilterServiceRunning()) {
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
			.setTitle(R.string.permission_denied)
			.setMessage(R.string.permission_request_desc)
			.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
												 Uri.parse("package:" + getPackageName())));
					} catch (Exception e) {
						Log.e(TAG, "grantPermission(): Exception occurred while starting permission activity. ", e);
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
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		mSharedPrefsScreenFilter
			.unregisterOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
		super.onDestroy();
	}

	public boolean hasScreenFilterPermission() {
		View testView = new View(this);
		testView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
												  LayoutParams.MATCH_PARENT));
		try {
			getWindowManager().addView(testView, new WindowManager
									   .LayoutParams(1, 1, 
													 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
													 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
													 PixelFormat.TRANSPARENT));
		} catch (Exception e) {
			if (e instanceof SecurityException) {
				Log.e(TAG, "hasScreenFilterPermission(): Exception is that user has not granted permssion yet.", e);
				return false;
				}
			Log.e(TAG, "hasScreenFilterPermission(): Exception but dont know why!" , e);
		} finally {
			try {
				getWindowManager().removeView(testView);
				return true;
			} catch (Exception e) {
				Log.e(TAG,"hasScreenFilterPermission(): All is undercontrol here!", e);
				return false;
			}
		}
	}
}
