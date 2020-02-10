package com.gameofcoding.screenfilter.Utils;

public abstract class AppConstants {
	public static final String SCHEME_PACKAGE = "package:";
	public static final String SHARED_PREFS_FILTER = "shared_pref_filter";
	public static final String KEY_COLOR_OPACITY = "color_opacity";

    // Intent actions...
	public static final String ACTION_FILTER_TOGGLED = "filter_toggled";
	public static final String ACTION_FILTER_OPACITY_INCREASE  = "filter_opacity_increase";
	public static final String ACTION_FILTER_OPACITY_DECREASE = "filter_opacity_decrease";

	public static abstract class PreferenceConstants {
		public static final String KEY_FILTER_STATUS_BAR = "filter_status_bar";
		public static final String KEY_FILTER_COLOR = "filter_color";
        public static final String KEY_FC_DEF = "filter_color_default";
        public static final String KEY_FC_BLACK = "filter_color_black";
        public static final String KEY_FC_RED = "filter_color_red";
		public static final String KEY_SEND_FEEDBACK = "send_feedback";
		public static final String KEY_ABOUT = "about_app";
		public static final String KEY_RESET_PREFS = "reset_prefs";
	}
}
