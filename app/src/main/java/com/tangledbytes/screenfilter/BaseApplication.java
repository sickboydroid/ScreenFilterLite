package com.tangledbytes.screenfilter;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class BaseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		MobileAds.initialize(this, initializationStatus -> {
			// Ads initialized
		});
	}
}
