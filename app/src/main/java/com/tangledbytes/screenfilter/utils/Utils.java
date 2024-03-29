package com.tangledbytes.screenfilter.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Utils {
	private static final String TAG = "Utils";
	private final Context mContext;

	public Utils(Context context) {
		mContext = context;
	}

	public String getAppInfo() {
		String appName = "Screen Filter";
		String appInstallTime = null;
		String appLastUpdateTime = null;
		String appVersionName;
		try {
			PackageInfo pkInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			// TODO: Convert these to actual times.
			appInstallTime = String.valueOf(pkInfo.firstInstallTime);
			appLastUpdateTime = String.valueOf(pkInfo.lastUpdateTime);
			appVersionName = pkInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Could'nt find own app.", e);
			appVersionName = "unknown";
		}
		return "App Name : " + appName +
				"\nApp Version : " + appVersionName +
				"\nInstalled on : " + appInstallTime +
				"\nUpdated on : " + appLastUpdateTime;
	}

	/**
	 * Checks whether the app has overlay permission or not.
	 *
	 * @return true if has permission.
	 **/
	public static boolean hasOverlayPermission(Context context) {
		//TODO: Overlay permission in Lollipop
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.canDrawOverlays(context)) {
				Log.i(TAG, "Overly Permission Allowed!");
				return true;
			} else {
				Log.i(TAG, "Overly Permission Denied!");
				return false;
			}
		}
		return true;
	}

	public static String getDeviceInfo() {
		return "Android Version=" + Build.VERSION.RELEASE +
				"\nBrand=" + Build.BRAND +
				"\nDevice=(" + Build.DEVICE + ")" +
				"\nModel=" + Build.MODEL +
				"\nDisplay=" + Build.DISPLAY +
				"\nBoard=" + Build.BOARD +
				"\nBootloader=" + Build.BOOTLOADER +
				"\nHardware=" + Build.HARDWARE +
				"\nLocale=" + Locale.getDefault().getDisplayLanguage()
				+ "-" + Locale.getDefault().getDisplayCountry();
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to,
		ColorStateList color) {
		return setTextAppearanceSpan(source, from, to, -1, color, null,
			Typeface.NORMAL, null);
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to, 
		ColorStateList color, ColorStateList linkColor) {
		return setTextAppearanceSpan(source, from, to, -1, color, linkColor,
			Typeface.NORMAL, null);
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to, int style) {
		return setTextAppearanceSpan(source, from, to, -1, null, null, style, null);
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to,
		ColorStateList color, int style) {
		return setTextAppearanceSpan(source, from, to, -1, color, null, style, null);
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to,
		String fontFamily) {
		return setTextAppearanceSpan(source, from, to, -1, null, null, 
			Typeface.NORMAL, fontFamily);
	}

	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * @see #setTextAppearance(String, int, int, int, ColorStateList, ColorStateList, int, String)
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to,
		ColorStateList color, String fontFamily) {
		return setTextAppearanceSpan(source, from, to, -1, color, null, 
			Typeface.NORMAL, fontFamily);
	}
	/*
	 * Sets {@link #android.text.TextAppearanceSpan} to given String.
	 * 
	 * @param source Text for which you want to set span.
	 * @param from Starting of span.
	 * @param to Ending of span.
	 * @param size Size for text.
	 * @param color Color for text.
	 * @param linkColor Link color for text.
	 * @param style Style for text. (Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC,
	 *                               Typeface.BOLD_ITALIC)
	 * @param fontFamily Font family for text.
	 *
	 * @return Spanned text.
	 */
	public static Spanned setTextAppearanceSpan(String source, int from, int to,
		int size, ColorStateList color, ColorStateList linkColor,
		int style, String fontFamily) {
		SpannableString spannableString = new SpannableString(source);
		spannableString.setSpan(new TextAppearanceSpan(fontFamily, style , size, color,
				linkColor), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	/**
     * Handy method for displaying short Toasts.
     *
	 * <p>Note: If you want to display short Toast use {@link #showToast(int)} instead
     * of this.</p>
	 *
	 * @param context Context from which you want to display Toast.
     * @param resIdText Resource ID of text that you want to display in Toast.
     *
	 * @see #showToast(Context, int, boolean)
     */
	public static void showToast(Context context, int resIdText) {
		showToast(context, resIdText, false);
	}

	/**
     * Handy method for displaying Toasts.
     *
	 * @param context Context from which you want to display Toast.
     * @param resIdText Resource ID of text that you want to display in Toast.
	 * @param longToast True if you want to display long Toast and false if you want
     * to display short Toast.
	 *
	 * @see #showToast(Context, int)
     */
	public static void showToast(Context context, int resIdText, boolean longToast) {
		Toast.makeText(context, resIdText,
			longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	/**
     * Handy method for displaying short Toasts.
     *
     * @param resIdText Resource ID of text that you want to display in Toast.
	 *
     * @see #showToast(int, boolean)
     */
    public void showToast(int resIdText) {
		showToast(resIdText, false);
    }

    /**
     * Handy method for displaying Toasts.
     *
     * <p>Note: If you want to display short Toast use {@link #showToast(int)} instead
     * of this.</p>
     *
     * @param resIdText Resource ID of text that you want to display in Toast.
     * @param longToast True if you want to display long Toast and false if you want
     * to display short Toast.
	 *
	 * @see #showToast(int)
	 */
    public void showToast(int resIdText, boolean longToast) {
        Toast.makeText(mContext, resIdText, 
			longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

	/**
	 * Converts given pixel values to dp values.
	 *
	 * <p>Converts using given formula:</p>
	 * <pre>
	 * int pixels = 20;
	 * float scale = context.getResources().getDisplayMetrics().density;
	 * int dp = pixels * scale;
	 * </pre>
	 *
	 * @param pixels Pixels that you want to convert.
	 *
	 * @return Converted pixels.
	 */
	public float toDp(int pixels) {
		return pixels * mContext.getResources().getDisplayMetrics().density;
	}

    public int toPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
											  dp, mContext.getResources().getDisplayMetrics());
    }
    /**
     * Runs linux commands using android's built in shell.
     *
     * @param cmd Command that you want to execute.
     *
     * @return Output of given command.
     *
     * @throws IOException it could cause while reading output of command.
     */
    public static String shell(String cmd) throws IOException {
		final Process proc = Runtime.getRuntime().exec(cmd);
		// Reads output of command
        final InputStream inputStreamOutput  = proc.getInputStream();
		// Reads error output (if error occured) of command
        final InputStream inputStreamError = proc.getErrorStream();

        StringBuilder output = new StringBuilder();
        int readedChar;
        while ((readedChar = inputStreamOutput.read()) != -1)
            output.append((char) readedChar);
        while ((readedChar = inputStreamError.read()) != -1)
            output.append((char) readedChar);
        inputStreamOutput.close();
        inputStreamError.close();
        return output.toString();
    }
}
