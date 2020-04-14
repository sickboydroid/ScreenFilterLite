package com.gameofcoding.screenfilter.services;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.utils.AppConstants;
import com.gameofcoding.screenfilter.utils.FilterUtils;
import com.gameofcoding.screenfilter.utils.ScreenManager;
import com.gameofcoding.screenfilter.activities.FilterDialogActivity;

public class ScreenFilterService extends Service {
	private static final String TAG = "ScreenFilterService";
	private Context mContext;
	final FilterUtils mFilterUtils = new FilterUtils(mContext);

    /**
     * Broadcast receiver send  by the notification for controlling filter.
     */
    private class FilterConfigChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppConstants.ACTION_FILTER_TOGGLED:
                    // Turn on/off filter.
                    if (isFilterOn())
                        stopFilter();
                    else
                        startFilter();
                    break;
            }
			// Hide notification drawer and reset notification
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
			resetForegroundNotification();
        }
    }

    private final FilterConfigChangeReceiver mFilterConfigChangeReceiver =
    new FilterConfigChangeReceiver();

	/**
	 * ID of the notification that is shown while this Service is running.
	 */
	private static final int FILTER_NOTIFICATION_ID = 1;

    /**
     * View that is used as an overlay over whole Window of the screen for filtering it.
     */
    private View mFilterViewColor;

	/**
     * View that is used as an overlay over whole Window of the screen for filtering it.
     */
    private View mFilterViewDark;

	private WindowManager mWindowManager;
	NotificationManager mNotificationManager;
	private SharedPreferences mAppPrefs;
	private boolean mIsFilterOn = false;
    int currentFilterColor = FilterUtils.DEFAULT_COLOR;
	int currentFilterDarkness = FilterUtils.DEFAULT_DARKNESS;

	/**
	 * Called when preferences of filter are changed.
	 */
	private final SharedPreferences.OnSharedPreferenceChangeListener mFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences mSharedPrefsScreenFilter, String key) {
			if (key.equals(AppConstants.KEY_FILTER_COLOR) || key.equals(AppConstants.KEY_FILTER_DARKNESS)) {
				// Color or Darkness of filter has been changed
				resetForegroundNotification();
				resetFilterColor();
			} 
		}
	};

	/**
	 * Called when preferences of whole app are changed (through SettingsActivity).
	 */
	private final SharedPreferences.OnSharedPreferenceChangeListener mAppPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals(AppConstants.PreferenceConstants.KEY_FILTER_COLOR))
				resetFilterColor();
			else if (key.equals(AppConstants.PreferenceConstants.KEY_FILTER_STATUS_BAR)) {
				if (isFilterOn()) {
					stopFilter();
					startFilter();
				}
			}
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (isFilterOn()) {
			stopFilter();
			startFilter();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Initialize Views
        mFilterViewColor = new View(mContext);
		mFilterViewDark = new View(mContext);

        // Register Receiver for receving BroadCastes that are sent through notification.
		IntentFilter intentFilterConfigChanged = new IntentFilter();
		intentFilterConfigChanged.addAction(AppConstants.ACTION_FILTER_TOGGLED);
		registerReceiver(mFilterConfigChangeReceiver, intentFilterConfigChanged);

        // Setup prefs
        mAppPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mAppPrefs.registerOnSharedPreferenceChangeListener(mAppPrefsChangeListener);
	    getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startFilter();
		resetForegroundNotification();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    /**
     * Starts filter.
     */
    private void startFilter() {
        resetFilterColor();
        try {
            startWindowFilter();
            setIsFilterOn(true);
        } catch (Throwable e) {
            Log.e(TAG, "startFilter(): Something went wrong while starting filter!", e);
        }
    }

    /**
     * Stops filter.
     */
    public void stopFilter() {
        try {
            stopWindowFilter();
            setIsFilterOn(false);
        } catch (Throwable e) {
            Log.e(TAG, "startFilter(): Something went wrong while starting filter!", e);
        }
    }

	/**
     * Resets or sets (if prevoiusly not set) filter color.
     */
    private void resetFilterColor() {
		if (mFilterViewColor == null) mFilterViewColor = new View(mContext);
		if (mFilterViewDark == null) mFilterViewDark = new View(mContext);

		FilterUtils filterUtils = new FilterUtils(mContext);
		currentFilterColor = filterUtils.getFilterColor();
		currentFilterDarkness = filterUtils.getFilterDarkness();

        // Convert color percent to actual value.
		int currentFilterColor = Math.round(((float) this.currentFilterColor / 100) * 120);
		int currentFilterDarkness = Math.round(((float) this.currentFilterDarkness / 100) * 180);

        // Finally reset background colors of Views that are filtering screen
	    mFilterViewColor.setBackgroundColor(Color.argb(currentFilterColor, 154, 38, 25));
		mFilterViewDark.setBackgroundColor(Color.argb(currentFilterDarkness, 0, 0, 0));
	}

    /**
     * Starts Window filter.
     *
     * @see stopWindowFilter
     */
	private void startWindowFilter() {
		if (mFilterViewColor.getParent() != null || mFilterViewDark.getParent() != null) {
			// Views has already been added
			return;
		}
		int flagsWindowFilterView = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_FULLSCREEN
			| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
			| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
		WindowManager.LayoutParams filterLytParams = new WindowManager.LayoutParams();
		filterLytParams.width = LayoutParams.MATCH_PARENT;
		filterLytParams.height = new ScreenManager(mContext).getScreenHeight();
		filterLytParams.flags = flagsWindowFilterView;
		filterLytParams.format = PixelFormat.TRANSLUCENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			filterLytParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		else
			filterLytParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

		// Finally add Views to WindoManager
		mWindowManager.addView(mFilterViewColor, filterLytParams);
		mWindowManager.addView(mFilterViewDark, filterLytParams);
	}

    /**
     * Stops Window filter.
     *
     * @see startWindowFilter
     */
    private void stopWindowFilter() {
        if (mFilterViewColor.getParent() == null ||
			mFilterViewDark.getParent() == null)  return;
        mWindowManager.removeView(mFilterViewColor);
        mWindowManager.removeView(mFilterViewDark);
    }

    /**
     * Sets whether filter is running or not.
     *
     * @param on True if filter has been started.
     *
     * @see isFilterOn
     */
    private void setIsFilterOn(boolean on) {
        mIsFilterOn = on;
    }

	/**
     * Checks whether filter is running or not.
     *
     * @return True if filter is running.
     * @see setIsFilterOn
     */
    private boolean isFilterOn() {
		return mIsFilterOn;
	}

	/**
     * Resets or starts (if prevoiusly not started) notification for controlling filter while
     * it is running.
     */
	private void resetForegroundNotification() {
		Notification.Builder notifBuilder = null;
		String channelID = getString(R.string.notification_channel_overlay_id);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			notifBuilder = new Notification.Builder(mContext, channelID);
			// Register a notification channel for Oreo if we don't already have one
			if (mNotificationManager.getNotificationChannel(channelID) == null) {
				String channelName = getString(R.string.notification_channel_filter_name);
				String channelDescription = getString(R.string.notification_channel_filter_description);
				NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_MIN);
				channel.setDescription(channelDescription);
				mNotificationManager.createNotificationChannel(channel);
			}
	    } else notifBuilder = new Notification.Builder(mContext);

		// Create a foreground notification
		int filterOpacity = 1100;
		String actionToggleFilter = isFilterOn() ? getString(R.string.pause_filter)
			: getString(R.string.resume_filter);
		Intent intentSettingsDialog = new Intent(mContext, FilterDialogActivity.class);
		Intent intentFilterToggle = new Intent(AppConstants.ACTION_FILTER_TOGGLED); 
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
			notifBuilder.setContentTitle(getString(R.string.app_name));
		notifBuilder.setContentText("Reading Mode");
		notifBuilder.setContentIntent(PendingIntent.getActivity(mContext, -1,  intentSettingsDialog, 0));
		notifBuilder.setSmallIcon(R.mipmap.ic_launcher);
		notifBuilder.setSubText(filterOpacity + "%");
		notifBuilder.setPriority(Notification.PRIORITY_HIGH);
		notifBuilder.addAction(-1, actionToggleFilter, broadCastPI(intentFilterToggle));
		startForeground(FILTER_NOTIFICATION_ID, notifBuilder.build());
	}

	private PendingIntent broadCastPI(Intent intent) {
		return PendingIntent.getBroadcast(mContext, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

    @Override
    public void onDestroy() {
        stopFilter();
        stopForeground(true);
        unregisterReceiver(mFilterConfigChangeReceiver);
        mAppPrefs.unregisterOnSharedPreferenceChangeListener(mAppPrefsChangeListener);
        getSharedPreferences(AppConstants.SHARED_PREFS_FILTER, MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(mFilterPrefsChangeListener);
        super.onDestroy();
    }
}
