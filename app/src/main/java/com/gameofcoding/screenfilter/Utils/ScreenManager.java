package com.gameofcoding.screenfilter.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.gameofcoding.screenfilter.Utils.Utils;

public class ScreenManager {
	public static final int DEFAULT_NAV_BAR_HEIGHT_DP = 48;
	public static final int DEFAULT_STATUS_BAR_HEIGHT_DP = 25;

	private Context mContext;
	private WindowManager mWindowManager;
    private Resources mResources;

	public ScreenManager(Context context) {
		mContext = context;
		mResources = mContext.getResources();
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	}

	public int getScreenHeight() {
		Display display = mWindowManager.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getRealMetrics(dm);
		if (inPortrait()) {
			return dm.heightPixels + getStatusBarHeightPx()
				+ getNavigationBarHeightPx();
		} else {
			return dm.heightPixels + getStatusBarHeightPx();
		}
	}

	private int getStatusBarHeightPx() {
		int statusBarHeightId = mResources.getIdentifier("status_bar_height", "dimen", "android");
		if (statusBarHeightId > 0) {
			return mResources.getDimensionPixelSize(statusBarHeightId);
		} else {
			return new Utils(mContext).toPx(DEFAULT_STATUS_BAR_HEIGHT_DP);
		}
	}

	private int getNavigationBarHeightPx() {
		int navBarHeightId = mResources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (navBarHeightId > 0) {
			return mResources.getDimensionPixelSize(navBarHeightId);
		} else {
			return new Utils(mContext).toPx(DEFAULT_NAV_BAR_HEIGHT_DP);
		}
	}

	private boolean inPortrait() {
		return mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
}
