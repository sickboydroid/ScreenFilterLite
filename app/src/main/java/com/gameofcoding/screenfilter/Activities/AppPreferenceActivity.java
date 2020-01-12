package com.gameofcoding.screenfilter.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Services.ScreenFilterService;
import java.util.Locale;

public class AppPreferenceActivity extends PreferenceActivity {
	public static final String TAG = "AppPreferenceActivity";
	public static final String KEY_PREF_FILTER_STATUS_BAR = "filter_status_bar";
	public static final String KEY_PREF_FILTER_COLOR = "filter_color";
	public static final String KEY_PREF_SEND_FEEDBACK = "send_feedback";
	public static final String KEY_PREF_ABOUT = "about_app";
	public static final String KEY_RESET_PREFS = "reset_prefs";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference prefSendFeedBack = findPreference(KEY_PREF_SEND_FEEDBACK);
		Preference prefAbout = findPreference(KEY_PREF_ABOUT);
		Preference prefResetSettings = findPreference(KEY_RESET_PREFS);
		prefSendFeedBack
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					String appVersionName = null;
					try {
						appVersionName =
							getPackageManager().getPackageInfo(getPackageName(),
							PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
							.versionName;
					} catch (PackageManager.NameNotFoundException e) {
						Log.e(TAG, "Could'nt find my own app.", e);
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
					mailBody.append("\nSettings=" + getPreferenceManager()
						.getDefaultSharedPreferences(getApplicationContext())
						.getAll().toString());
					mailBody.append("\nOther settings=" + 
						getSharedPreferences(ScreenFilterService.KEY_SHARED_PREF_SCREEN_FILTER,
							MODE_PRIVATE).getAll().toString());
					Intent intentSendFeedback = 
						new Intent(Intent.ACTION_SENDTO,  Uri.fromParts("mailto", 
							getString(R.string.developer_email_id),
							null));
					intentSendFeedback.putExtra(Intent.EXTRA_SUBJECT, "Screen Filter - Feedback");
					intentSendFeedback.putExtra(Intent.EXTRA_TEXT, mailBody.toString());
					startActivity(Intent.createChooser(intentSendFeedback, getString(R.string.chooser_title)));
					return true;
				}
			});
		prefAbout
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					startActivity(new Intent(getApplicationContext(), AboutActivity.class));
					return true;
				}
			});
		prefResetSettings
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					if (prefs.getEditor().clear().commit()) {
						Toast.makeText(getApplicationContext(), 
							R.string.success_prefs_reset, Toast.LENGTH_LONG).show();
						recreate();
					} else 
						Toast.makeText(getApplicationContext(),
							R.string.failure_prefs_reset, Toast.LENGTH_LONG).show();
					return true;
				}
			});
	}
}
