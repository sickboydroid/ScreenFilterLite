package com.tangledbytes.screenfilter.activities;

import android.content.Context;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.PackageInfo;

import com.tangledbytes.screenfilter.R;

public class AboutActivity extends Activity {
	private static final String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		String versionName;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Couldn't find my own app", e);
			versionName = "unknown";
		}
		versionName = String.format(getResources().getString(R.string.version), versionName);
		((TextView) findViewById(R.id.layout_aboutTextView_app_version))
			.setText(versionName);
		((Button) findViewById(R.id.ok)).setOnClickListener(view -> finish());
	}
}
