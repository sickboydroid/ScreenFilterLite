package com.gameofcoding.screenfilter.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.gameofcoding.screenfilter.Activities.MainActivity;
import com.gameofcoding.screenfilter.R;
import com.gameofcoding.screenfilter.Utils.AppConstants;
import com.gameofcoding.screenfilter.Utils.FilterUtils;

public class ScreenFilterService extends Service {
	private static final String TAG = "ScreenFilterService";
	private Context mContext;

    /**
     * Broadcast receiver send  by the notification for controlling filter.
     */
    private class FilterConfigChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FilterUtils filterUtils = new FilterUtils(mContext);
            switch (intent.getAction()) {
                case AppConstants.ACTION_FILTER_TOGGLED:
                    // Turn on/off filter.
                    if (isFilterOn())
                        stopFilter();
                    else
                        startFilter();
                    break;
                case AppConstants.ACTION_FILTER_OPACITY_INCREASE:
                    // Increase filter opacity.
                    int newOpacity = filterUtils.getFilterOpacity() + 10;
                    while (newOpacity % 10 != 0) {
                        if (newOpacity % 10 <= 5)
                            newOpacity--;
                        else
                            newOpacity++;
                    }
                    if (newOpacity <= FilterUtils.MAX_OPACITY)
                        filterUtils.updateFilterOpacity(newOpacity);
                    break;
                case AppConstants.ACTION_FILTER_OPACITY_DECREASE:
                    // Decrease filter opacity.
                    newOpacity = filterUtils.getFilterOpacity() - 10;
                    while (newOpacity % 10 != 0) {
                        if (newOpacity % 10 <= 5)
                            newOpacity--;
                        else
                            newOpacity++;
                    }
                    if (newOpacity >= FilterUtils.MIN_OPACITY)
                        filterUtils.updateFilterOpacity(newOpacity);
                    break;
            }

            // Update notification.
            resetForegroundNotification();
        }
    }

    private final FilterConfigChangeReceiver mFilterConfigChangeReceiver = 
    new FilterConfigChangeReceiver();

	/**
	 * ID of the notification that is shown while this Service is running.
	 */
	private static final int FILTER_NOTIFICATION_ID = 309092;

    /**
     * View that is used as an overlay over whole Window of the screen (except navigation
     * bar) for filtering it.
     */
    private View mWindowFilterView;

    /**
	 * View that is used as an overlay over navigation bar for filtering it.
	 * <p>Note: Due to some android limitations we can't draw Window over navigation bar
	 *          but here we are just using a hack to filter;)
	 */
	private View mNavFilterView;

	private WindowManager mWindowManager;
	private SharedPreferences mAppPrefs;
	private boolean mIsFilterOn = false;
    int currentFilterOpacity = FilterUtils.DEFAULT_OPACITY;

	/**
	 * Called when preferences of filter are changed.
	 */
	private final SharedPreferences.OnSharedPreferenceChangeListener mFilterPrefsChangeListener =
	new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences mSharedPrefsScreenFilter, String key) {
			if (key.equals(AppConstants.KEY_COLOR_OPACITY)) {
				resetForegroundNotification();
				int newOpacity = new FilterUtils(mContext).getFilterOpacity();
                if (newOpacity > currentFilterOpacity) {
                    // Opacity increased
                    if (newOpacity - currentFilterOpacity <= 5) {
                        // Opacity has not increased too much, so we will not update 
                        // filter opacity for performenance.
                        return;
                    }
                } else {
                    // Opacity decreased
                    if (currentFilterOpacity - newOpacity <= 5) {
                        // Opacity has not decreased too much, so we will not update 
                        // filter opacity for performenance.
                        return;
                    }
                }
                Log.i(TAG, "Filter opacity has changed too much, so reseting filter opacity.");
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
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Initialize Views
        mWindowFilterView = new View(mContext);
        mNavFilterView = new View(mContext);

        // Setup Views
        mWindowFilterView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom, 
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    // FIXME: Is it right way of guesing new height?
                    int heightDifference = (top + bottom) - (oldTop + oldBottom);
                    if (heightDifference == -getNaviBarHeight()) {
                        // Navigation bar is visible, so filter it.
                        startNaviBarFilter();
                    } else if (heightDifference == getNaviBarHeight()) {
                        // Navigation bar is hidden, so don't filter it.
                        stopNaviBarFilter();
                    }
                }
            });

        // Register Receiver for receving BroadCastes that are sent through notification.
		IntentFilter intentFilterConfigChanged = new IntentFilter();
		intentFilterConfigChanged.addAction(AppConstants.ACTION_FILTER_TOGGLED);
		intentFilterConfigChanged.addAction(AppConstants.ACTION_FILTER_OPACITY_INCREASE);
		intentFilterConfigChanged.addAction(AppConstants.ACTION_FILTER_OPACITY_DECREASE);
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
            startNaviBarFilter(); 
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
            stopNaviBarFilter();
            setIsFilterOn(false);
        } catch (Throwable e) {
            Log.e(TAG, "startFilter(): Something went wrong while starting filter!", e);
        }
    }

	/**
     * Resets or sets (if prevoiusly not set) filter color.
     */
    private void resetFilterColor() {
		if (mWindowFilterView == null) {
            Log.w(TAG, "resetFilterColor(): 'mWindowFilterView' is null; reinitializing...");
            mWindowFilterView = new View(mContext);
        }
        if (mNavFilterView == null) {
            Log.w(TAG, "resetFilterColor(): 'mNavFilterView' is null; reinitializing...");
            mNavFilterView = new View(mContext);
        } 
		int filterColor;
        currentFilterOpacity = new FilterUtils(mContext).getFilterOpacity();
		String choosedFilterColor = mAppPrefs.getString(AppConstants.PreferenceConstants.KEY_FILTER_COLOR,
            AppConstants.PreferenceConstants.KEY_FC_DEF);

        // Create color according to what user has choosed.
        switch (choosedFilterColor) {
			case AppConstants.PreferenceConstants.KEY_FC_DEF:
                // Orange Color
			    // FIXME: Enter real orange color here.
            	filterColor = Color.argb(currentFilterOpacity, 255, 150, 50);
				break;
			case AppConstants.PreferenceConstants.KEY_FC_BLACK:
                // Black Color
                filterColor = Color.argb(currentFilterOpacity, 20, 20, 5);
				break;
			case AppConstants.PreferenceConstants.KEY_FC_RED:
                // Red Color
				filterColor = Color.argb(currentFilterOpacity, 255, 30, 30);
				break;
			default:
                // Should'nt reach
				filterColor = -1;
		}

        // Finally reset background colors of Views that are filtering screen
	    mWindowFilterView.setBackgroundColor(filterColor);
		mNavFilterView.setBackgroundColor(filterColor);
	}

    /**
     * Checks whether the device has navigation bar or not.
     *
     * @return True if device has navigation bar.
     *
     * @see getNaviBarHeight
     */
	private boolean hasNaviBar() {
		return true;
	}

    /**
     * Gets height of navigation bar.
     *
     * @return height of naviagtion bar.
     * @see hasNaviBar
     */
	private int getNaviBarHeight() {
		if (!hasNaviBar())
            return 0;
        Resources resources = getResources();
		int resourceId = 
            resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
            // Valid resource Id.
			return resources.getDimensionPixelSize(resourceId);
		} else {
            // Invalid resource Id.
            Log.w(TAG, "Failed to get the height of navigation bar!");
            return 0;
        }
	}

    /**
     * Starts Window filter.
     *
     * @see stopWindowFilter
     */
    private void startWindowFilter() {
        if (mWindowFilterView == null) {
            mWindowFilterView = new View(mContext);
            resetFilterColor();
        } else if (mWindowFilterView.getParent() != null) {
            // mWindowFilterView has already a parent!
            return;
        }
        // FIXME: Add flag FLAG_SHOW_WHEN_LOCKED for where it is available.
        int flagsWindowFilterView = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (hasFilterStatusBar())
            flagsWindowFilterView |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        WindowManager.LayoutParams lytParamsWindowFilter = new WindowManager.LayoutParams();
        lytParamsWindowFilter.width = LayoutParams.MATCH_PARENT;
        lytParamsWindowFilter.height = LayoutParams.MATCH_PARENT;
        lytParamsWindowFilter.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lytParamsWindowFilter.flags = flagsWindowFilterView;
        lytParamsWindowFilter.format = PixelFormat.TRANSLUCENT;
        mWindowManager.addView(mWindowFilterView, lytParamsWindowFilter);
    }

    /**
     * Stops Window filter.
     *
     * @see startWindowFilter
     */
    private void stopWindowFilter() {
        if (mWindowFilterView == null) return;
        if (mWindowFilterView.getParent() == null)  return;
        mWindowManager.removeView(mWindowFilterView);
    }

    /**
     * Starts navigation bar filter.
     *
     * @see stopNaviBarFilter
     */
	private void startNaviBarFilter() {
        if (!hasNaviBar())
            return;
		else if (mNavFilterView == null) {
            mNavFilterView = new View(mContext);
        } else if (mNavFilterView.getParent() != null) {
            // mNavFilterView has already a parent!
			return;
        }
        int navHeight = getNaviBarHeight();
        WindowManager.LayoutParams lytParamsNavFilter = new WindowManager.LayoutParams();
        lytParamsNavFilter.width = LayoutParams.MATCH_PARENT;
        lytParamsNavFilter.height = navHeight;
        lytParamsNavFilter.type = WindowManager.LayoutParams.TYPE_TOAST;
        lytParamsNavFilter.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        lytParamsNavFilter.format = PixelFormat.TRANSLUCENT;
        lytParamsNavFilter.gravity = Gravity.BOTTOM;
        mWindowManager.addView(mNavFilterView, lytParamsNavFilter);
	}

    /**
     * Stops navigation bar filter.
     *
     * @see startNaviBarFilter
     */
	private void stopNaviBarFilter() {
        if (mNavFilterView == null) return;
		else if (mNavFilterView.getParent() == null) return;
        mWindowManager.removeView(mNavFilterView);
	}

    /**
     * Checks whether user has choosed to filter status bar or not (in settings).
     *
     * @return True if user choosed to filter status bar.
     */
	private boolean hasFilterStatusBar() {
		return mAppPrefs
			.getBoolean(AppConstants.PreferenceConstants.KEY_FILTER_STATUS_BAR, true);
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
		PendingIntent intentMainActivity = PendingIntent.getActivity(mContext, -1,
			new Intent(getApplicationContext(), MainActivity.class), 0);
		PendingIntent intentFilterToggle = PendingIntent.getBroadcast(mContext, -1,
			new Intent(AppConstants.ACTION_FILTER_TOGGLED), 0);								 
		PendingIntent intentIncreaseOpacity = PendingIntent.getBroadcast(mContext, -1,
			new Intent(AppConstants.ACTION_FILTER_OPACITY_INCREASE), 0);
		PendingIntent intentDecreaseOpacity = PendingIntent.getBroadcast(mContext, -1,
			new Intent(AppConstants.ACTION_FILTER_OPACITY_DECREASE), 0);
		int filterOpacity = new FilterUtils(mContext).getFilterOpacity();
        String strOnOff;
		String actionOnOff;
        if (isFilterOn()) {
            strOnOff = getString(R.string.on);
            actionOnOff = getString(R.string.stop_filter);
        } else {
            strOnOff = getString(R.string.off);
            actionOnOff = getString(R.string.start_filter);
        }

		// Build notification
		Notification foregroundFilterNotif = new Notification.Builder(mContext) 
			.setSmallIcon(R.drawable.notification_app_icon) 
			.setContentTitle(getString(R.string.app_name))
			.setContentText(filterOpacity + "%")
			.setContentInfo(strOnOff)
			.setStyle(new Notification.BigTextStyle()
			.bigText(filterOpacity + "%")
			.setBigContentTitle(getString(R.string.app_name)))
			.setPriority(Notification.PRIORITY_HIGH)
			.setContentIntent(intentMainActivity)
			.addAction(R.drawable.minus, null, intentDecreaseOpacity)
			.addAction(-1, actionOnOff, intentFilterToggle)
			.addAction(R.drawable.plus, null, intentIncreaseOpacity)
			.build();
		startForeground(FILTER_NOTIFICATION_ID, foregroundFilterNotif);
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
