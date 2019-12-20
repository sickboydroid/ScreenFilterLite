package com.gameofcoding.screenfilter.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Services.ScreenFilterService;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import android.content.pm.PackageInfo;
import android.util.TimeUtils;
import android.text.format.Time;

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
		prefSendFeedBack
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
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
						mailBody.append("\nSettings=" + getPreferenceManager()
										.getDefaultSharedPreferences(getApplicationContext())
										.getAll().toString());
						mailBody.append("\nOther settings=" + 
										getSharedPreferences(ScreenFilterService.KEY_SHARED_PREF_SCREEN_FILTER,
															 MODE_PRIVATE).getAll().toString());
						String logCat = "\n=======Preference Activity========\n" + mailBody.toString() + "\n===================\n\n" + getLogCat();
						write(getExternalCacheDir().toString() + "/ScreenFilterLog.txt", logCat, true);
						Toast.makeText(getApplicationContext(), "Wrote!", Toast.LENGTH_LONG).show();
						Toast.makeText(getApplicationContext(), getExternalCacheDir().toString(), Toast.LENGTH_LONG).show();

						clearLog();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
					}
					/*==========================================*/
					// TODO: Un-Comment following in release version.
					/*
					 String appVersionName = null;
					 try {
					 appVersionName =
					 getPackageManager().getPackageInfo(getPackageName(),
					 PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
					 .versionName;
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
					 */
					return true;
				}
				// TODO: Only in alpha version
				/*==========================================*/
				/**
				 * Reads and returns logcat of app by running <b>logcat -d</b>
				 * (-d option means dump) shell command. 
				 * 
				 * @return Logcat of app.
				 *
				 * @throws java.io.IOException.
				 */
				public String getLogCat() throws IOException {
					return shell("logcat -d");
				}

				/**
				 * Clears logcat using <b>logcat -c</b> (-c option means clear) shell command.
				 *
				 * @throws java.io.IOException.
				 */
				public void clearLog() throws IOException {
					shell("logcat -c");
				}

				/**
				 * Runs given linux command using android's built in shell.
				 *
				 * @param cmd Command that you want to execute.
				 *
				 * @return Output of given command.
				 *
				 * @throws IOException it could cause while reading output of command.
				 */
				public String shell(String cmd) throws IOException {
					Process proc;
					proc = Runtime.getRuntime().exec(cmd);
					// Reads result of command
					final InputStream inputStreamOutput  = proc.getInputStream();
					// Reads error (if occured) of command
					final InputStream inputStreamError = proc.getErrorStream();

					String output = "";
					int readedChar;
					while ((readedChar = inputStreamOutput.read()) != -1)
						output += (char) readedChar;
					while ((readedChar = inputStreamError.read()) != -1)
						output += (char) readedChar;
					inputStreamOutput.close();
					inputStreamError.close();
					return output;
				}

				/**
				 * Writes file.
				 * 
				 * @param pathName Path of file on which you want to perform writing.
				 * @param str Data that you want to write in a file.
				 * @param append Append file data or not.
				 *
				 * @throws java.io.FileNotFoundException In case give file is not found
				 */
				public void write(String pathName, String data, boolean append) 
				throws FileNotFoundException, IOException {
					FileOutputStream fileWriter = new FileOutputStream(pathName, append);
					fileWriter.write(data.getBytes());
					fileWriter.close();
				}
				/*==========================================*/
			});
		Preference prefAbout = findPreference(KEY_PREF_ABOUT);
		prefAbout
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					startActivity(new Intent(getApplicationContext(), AboutActivity.class));
					return true;
				}
			});
		Preference prefResetSettings = findPreference(KEY_RESET_PREFS);
		prefResetSettings
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference prefs) {
					if (prefs.getEditor().clear().commit()) {
						Toast.makeText(getApplicationContext(), 
									   R.string.success_prefs_reset, Toast.LENGTH_LONG).show();
						try {
							startActivity(new Intent(getApplicationContext(),
													 AppPreferenceActivity.class));
						} finally {
							;finish();
						}
					} else 
						Toast.makeText(getApplicationContext(),
									   R.string.failure_prefs_reset, Toast.LENGTH_LONG).show();
					return true;
				}
			});
	}
}
