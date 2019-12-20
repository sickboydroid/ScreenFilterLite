package com.gameofcoding.screenfilter.Activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.gameofcoding.screenfilter.ModifiedClasses.BaseActivity;
import com.gameofcoding.screenfilter.R;
import android.view.View;

public class AboutActivity extends BaseActivity {
	String TAG = "AboutActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		String versionName = null;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(),
															 PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Could'nt find my own app", e);
			versionName = "unknown";
		}
		((TextView)findViewById(R.id.layout_aboutTextView_app_version))
			.setText(getResources().getString(R.string.version)
					 + ": "
					 + versionName);
		((Button)findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view) {
					finish();
				}
			});
	}
}
