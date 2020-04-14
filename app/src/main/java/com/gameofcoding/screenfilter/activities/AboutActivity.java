package com.gameofcoding.screenfilter.activities;

import android.content.Context;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.gameofcoding.screenfilter.R;
import android.content.pm.PackageInfo;

public class AboutActivity extends Activity {
	private static final String TAG = "AboutActivity";
	private final Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		String versionName = null;
		try {
			PackageInfo pkInfo = getPackageManager().getPackageInfo(getPackageName(),
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
			versionName = pkInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Could'nt find my own app", e);
			versionName = "unknown";
		}
		versionName = String.format(getResources().getString(R.string.version), versionName);
		((TextView) findViewById(R.id.layout_aboutTextView_app_version))
			.setText(versionName);
		((Button) findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
	}
}
