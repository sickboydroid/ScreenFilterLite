package com.gameofcoding.screenfilter.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Utils.AppConstants;
import com.gameofcoding.screenfilter.Utils.LogCatUtils;
import com.gameofcoding.screenfilter.Utils.Utils;
import java.util.PriorityQueue;

public class AppPreferenceActivity extends PreferenceActivity {
	private static final String TAG = "AppPreferenceActivity";
	private final Context mContext = this;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize prefs
		Preference prefSendFeedBack =
			findPreference(AppConstants.PreferenceConstants.KEY_SEND_FEEDBACK);
		Preference prefAbout =
			findPreference(AppConstants.PreferenceConstants.KEY_ABOUT);
		Preference prefResetSettings =
			findPreference(AppConstants.PreferenceConstants.KEY_RESET_PREFS);

		// Watch for preference clicks
		prefSendFeedBack
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					LogCatUtils log = new LogCatUtils(getExternalCacheDir());
					log.writeLogCat();
					Intent intentSendFeedback = new Intent(Intent.ACTION_SENDTO,
						Uri.fromParts("mailto", getString(R.string.developer_email_id), null));
					intentSendFeedback.putExtra(Intent.EXTRA_SUBJECT, "Screen Filter Lite - Feedback");
					StringBuilder mailBody = new StringBuilder();
					mailBody.append("* App {\n");
					mailBody.append(new Utils(getApplicationContext()).getAppInfo());
					mailBody.append("\n}\n* Device {\n");
					mailBody.append(new Utils(getApplicationContext()).getDeviceInfo());
					mailBody.append("\n}");
					intentSendFeedback.putExtra(Intent.EXTRA_TEXT, mailBody.toString());
					startActivity(Intent.createChooser(intentSendFeedback, getString(R.string.chooser_title)));
					return true;
				}
			});
		prefAbout
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					startActivity(new Intent(getApplicationContext(), AboutActivity.class));
					return true;
				}
			});
		prefResetSettings
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					if (prefs.getEditor().clear().commit()) {
						Utils.showToast(mContext, R.string.success_prefs_reset);
						recreate();
					} else 
						Utils.showToast(mContext, R.string.failure_prefs_reset);
					return true;
				}
			});
	}
}
