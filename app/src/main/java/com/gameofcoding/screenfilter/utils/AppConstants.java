package com.gameofcoding.screenfilter.utils;

public abstract class AppConstants {
	public static final String SCHEME_PACKAGE = "package:";
	public static final String SHARED_PREFS_FILTER = "shared_pref_filter";
	public static final String KEY_FILTER_SERVICE_RUNNING = "filter_service_running";
	public static final String KEY_FILTER_COLOR = "filter_color";
	public static final String KEY_FILTER_DARKNESS = "filter_darkness";
	public static final String KEY_FILTER_CUSTOM_COLOR = "filter_custom_color";
	public static final String KEY_FILTER_CUSTOM_DARKNESS = "filter_custom_darkness";
	public static final String KEY_FILTER_MODE = "filter_mode";
	public static final String EXTRA_IS_FILTER_ON = "is_filter_on";
	
    // Intent actions...
	public static final String ACTION_FILTER_TOGGLED = "filter_toggled";

	public static abstract class PreferenceConstants {
		public static final String KEY_FILTER_STATUS_BAR = "filter_status_bar";
		public static final String KEY_FILTER_COLOR = "filter_color";
        public static final String KEY_FC_DEF = "filter_color_default";
        public static final String KEY_FC_RED = "filter_color_red";
		public static final String KEY_SEND_FEEDBACK = "send_feedback";
		public static final String KEY_ABOUT = "about_app";
		public static final String KEY_RESET_PREFS = "reset_prefs";
	}
}
