package com.gameofcoding.screenfilter.Services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;
import com.gameofcoding.screenfilter.Activities.AppPreferenceActivity;
import com.gameofcoding.screenfilter.Activities.MainActivity;
import com.gameofcoding.screenfilter.R;

public class ScreenFilterService extends Service {
	public static final String TAG = "ScreenFilterService";
	public static final String KEY_COLOR_OPACITY = "color_opacity";
	public static final String KEY_SHARED_PREF_SCREEN_FILTER = "shared_prer_filter";
	public static final String KEY_SERVICE_RUNNING = "screen_filter_service_running";

	public static final String ACTION_FILTER_ON_OFF  = "filter_on_off";
	public static final String ACTION_FILTER_OPACITY_INCREASE  = "filter_opacity_increase";
	public static final String ACTION_FILTER_OPACITY_DECREASE = "filter_opacity_decrease";

	private final int SCREEN_FILTER_NOTIFICATION_ID = 32;

	boolean mFilterStatusBar = true;

	BroadcastReceiver mBCReceiverFilterConfigurationChanged = new BCReceiverFilterConfigurationChanged();
	WindowManager mWindowManager;
    View mWindowFilterView;
	View mNavFilterView;
	SharedPreferences mAppSharedPrefs;
	SharedPreferences mSharedPrefsScreenFilter;
	private SharedPreferences.OnSharedPreferenceChangeListener mSharedScreenFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(SharedPreferences mSharedPrefsScreenFilter, String key) {
			if (key.equals(KEY_COLOR_OPACITY)) {
				updateFilterColor();
				updateNotification();
			}
		}
	};
	private SharedPreferences.OnSharedPreferenceChangeListener mSharedAppPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals(AppPreferenceActivity.KEY_PREF_FILTER_COLOR))
				updateFilterColor();
			else if (key.equals(AppPreferenceActivity.KEY_PREF_FILTER_STATUS_BAR)) {
				mFilterStatusBar = sharedPrefs
					.getBoolean(AppPreferenceActivity.KEY_PREF_FILTER_STATUS_BAR, true);
				if (mSharedPrefsScreenFilter.getBoolean(KEY_SERVICE_RUNNING, false)) {
					stopFilter();
					startFilter();
				}
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_FILTER_ON_OFF);
		intentFilter.addAction(ACTION_FILTER_OPACITY_INCREASE);
		intentFilter.addAction(ACTION_FILTER_OPACITY_DECREASE);
		registerReceiver(mBCReceiverFilterConfigurationChanged, intentFilter);

		mSharedPrefsScreenFilter = getSharedPreferences(KEY_SHARED_PREF_SCREEN_FILTER, MODE_PRIVATE);
		mSharedPrefsScreenFilter.edit()
			.putBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, true)
			.commit();
		mSharedPrefsScreenFilter
			.registerOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
		mAppSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mAppSharedPrefs.registerOnSharedPreferenceChangeListener(mSharedAppPrefsChangeListener);
		mFilterStatusBar = mAppSharedPrefs
			.getBoolean(AppPreferenceActivity.KEY_PREF_FILTER_STATUS_BAR, true);
		mWindowFilterView = new View(this);
		mNavFilterView = new View(this);
		mWindowFilterView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View view, int left, int top, int right, int bottom, 
					int oldLeft, int oldTop, int oldRight, int oldBottom) {
					// FIXME: Is it right way of guesing new height?
					int heightDifference = (top + bottom) - (oldTop + oldBottom);
					if(heightDifference == -getNaviBarHeight())
						// Navigation bar showing
						startNaviBarFilter();
					else if(heightDifference == getNaviBarHeight())
						// Navigation bar hidden
						stopNaviBarFilter();
				}
			});
		updateFilterColor();
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	private void updateFilterColor() {
		if (mNavFilterView == null || mWindowFilterView == null)
			return;
		int color = -1;
		int opacity = mSharedPrefsScreenFilter.getInt(KEY_COLOR_OPACITY, 10);
		String selectedColor = mAppSharedPrefs
			.getString(AppPreferenceActivity.KEY_PREF_FILTER_COLOR, "default");
		switch (selectedColor) {
			case "default":
				color = Color.argb(opacity, 250, 150, 90);
				break;
			case "Gray":
				color = Color.argb(opacity, 58, 58, 40);
				break;
			case "Black":
				color = Color.argb(opacity, 0, 0, 0);
				break;
			case "White":
				color = Color.argb(opacity, 250, 250, 250);
				break;
			case "Brown":
				color = Color.argb(opacity, 200, 120, 0);
				break;
			case "Red":
				color = Color.argb(opacity, 250, 0, 0);
				break;
			case "Green":
				color = Color.argb(opacity, 0, 250, 0);
				break;
			case "Blue":
				color = Color.argb(opacity, 0, 0, 250);
				break;
			default:
				color = Color.argb(opacity, 0, 0, 0);
		}
		mNavFilterView.setBackgroundColor(color);
		mWindowFilterView.setBackgroundColor(color);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		updateNotification();
		startFilter();
		return START_STICKY;
	}
	
	private boolean hasNavigationBar(){
	
		return false;
	}

	private int getNaviBarHeight() {
		Resources resources = getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public void startFilter() {
		// Filter NavigationBar
		startNaviBarFilter();

		// Filter rest of the window
		startWindowFilter();
		
		mSharedPrefsScreenFilter.edit()
			.putBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, true)
			.commit();
	}
	
	public void startNaviBarFilter() {
		if(mNavFilterView.getParent() != null)
			return;
		int navHeight = getNaviBarHeight();
		WindowManager.LayoutParams lPNavFilter = new WindowManager
			.LayoutParams(LayoutParams.MATCH_PARENT, navHeight,
			WindowManager.LayoutParams.TYPE_TOAST, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
			PixelFormat.TRANSLUCENT);
		lPNavFilter.gravity = Gravity.BOTTOM;
		mWindowManager.addView(mNavFilterView, lPNavFilter);
	}
	
	public void stopNaviBarFilter(){
		if(mNavFilterView.getParent() == null)
			return;
		mWindowManager.removeView(mNavFilterView);
	}
	
	public void startWindowFilter(){
		if(mWindowFilterView.getParent() != null)
			return;
		int flagsWindowFilter = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
			| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		if (mFilterStatusBar)
			flagsWindowFilter |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		try {
			WindowManager.LayoutParams lpWindowFilter =
				new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, flagsWindowFilter, PixelFormat.TRANSLUCENT);
			mWindowManager.addView(mWindowFilterView, lpWindowFilter);
		} catch (Exception|Error e) {
			showToast(e.toString());
		}
	}
	
	public void stopWindowFilter(){
		if(mWindowFilterView.getParent() == null)
			return;
		mWindowManager.removeView(mWindowFilterView);
	}

	public void stopFilter() {
		try {
			stopNaviBarFilter();
			stopWindowFilter();
			mSharedPrefsScreenFilter.edit()
				.putBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, false)
				.commit();
		} catch (Exception e) {
			Log.e(TAG, "stopFilter(): Exception occured, don't know why", e);
		}
	}

	@Override
	public void onDestroy() {
		stopFilter();
		mSharedPrefsScreenFilter
			.unregisterOnSharedPreferenceChangeListener(mSharedScreenFilterPrefsChangeListener);
		mAppSharedPrefs.unregisterOnSharedPreferenceChangeListener(mSharedAppPrefsChangeListener);
		unregisterReceiver(mBCReceiverFilterConfigurationChanged);
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {return null;}

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void updateNotification() {
		PendingIntent mainActivityIntent = PendingIntent.getActivity(getApplicationContext(), 0,
			new Intent(getApplicationContext(), MainActivity.class), 0);
		PendingIntent pendingIntentFilterOnOff = PendingIntent.getBroadcast(getApplicationContext(), 0,
			new Intent(ACTION_FILTER_ON_OFF), 0);								 
		PendingIntent pendingIntentIncreaseFilterOpacity = PendingIntent.getBroadcast(getApplicationContext(), 0,
			new Intent(ACTION_FILTER_OPACITY_INCREASE), 0);
		PendingIntent pendingIntentDecreaseFilterOpacity = PendingIntent.getBroadcast(getApplicationContext(), 0,
			new Intent(ACTION_FILTER_OPACITY_DECREASE), 0);
		int opacity = mSharedPrefsScreenFilter.getInt(KEY_COLOR_OPACITY, 50);
		int idOnOff;
		String strOnOff;
		if (mSharedPrefsScreenFilter
			.getBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, false)) {
			idOnOff = R.drawable.on;
			strOnOff = "ON";
		} else {
			idOnOff = R.drawable.off;
			strOnOff = "OFF";
		}

		//init notification 
		Notification screenFilterNotification = new Notification.Builder(this) 
			.setSmallIcon(R.drawable.notification_app_icon) 
			.setContentTitle(getString(R.string.app_name))
			.setContentText(opacity + "%")
			.setContentInfo(strOnOff)
			.setStyle(new Notification.BigTextStyle()
			.bigText(opacity + "%")
			.setBigContentTitle(getString(R.string.app_name)))
			.setPriority(Notification.PRIORITY_HIGH)
			.setContentIntent(mainActivityIntent)
			.addAction(R.drawable.minus, null, pendingIntentDecreaseFilterOpacity)
			.addAction(0, strOnOff, pendingIntentFilterOnOff)
			.addAction(R.drawable.plus, null, pendingIntentIncreaseFilterOpacity)
			.build();
		startForeground(SCREEN_FILTER_NOTIFICATION_ID, screenFilterNotification);
	}

	private class BCReceiverFilterConfigurationChanged extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case ScreenFilterService.ACTION_FILTER_ON_OFF:
					boolean isFilterRunning = mSharedPrefsScreenFilter
						.getBoolean(ScreenFilterService.KEY_SERVICE_RUNNING, false);
					if (isFilterRunning)
						stopFilter();
					else
						startFilter();
					break;
				case ScreenFilterService.ACTION_FILTER_OPACITY_INCREASE:
					int previousOpacity = mSharedPrefsScreenFilter.getInt(KEY_COLOR_OPACITY, 50);
					if (previousOpacity + 10 <= 225)
						mSharedPrefsScreenFilter.edit()
							.putInt(ScreenFilterService.KEY_COLOR_OPACITY, previousOpacity + 10)
							.commit();
					break;
				case ScreenFilterService.ACTION_FILTER_OPACITY_DECREASE:
					previousOpacity = mSharedPrefsScreenFilter.getInt(KEY_COLOR_OPACITY, 50);
					if (previousOpacity - 10 > 10)
						mSharedPrefsScreenFilter.edit()
							.putInt(ScreenFilterService.KEY_COLOR_OPACITY, previousOpacity - 5)
							.commit();
					break;
			}
			updateNotification();
		}
	}
}
