package com.gameofcoding.screenfilter.Activities;

import android.content.Context;
import android.os.Bundle;
import com.gameofcoding.screenfilter.ModifiedClasses.BaseActivity;
import com.gameofcoding.screenfilter.R;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class MainActivity extends BaseActivity {
	private final String TAG = "MainActivity";
	private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
				return true;
			case R.id.menu_exit:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
