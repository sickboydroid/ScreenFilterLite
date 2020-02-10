package com.gameofcoding.screenfilter.Utils;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class LogCatUtils {
		public static final String TAG = "LogCatUtils";
		public static final String LOGCAT_FILE_NAME = "appLogCat.log";
		File mLogCatFile;
		public LogCatUtils(File filesDir) {
			mLogCatFile = new File(filesDir, LOGCAT_FILE_NAME);
		}

		private static String getLogCat() throws IOException {
			return Utils.shell("logcat -d");
		}

		private void clearLogCat() throws IOException {
			Utils.shell("logcat -c");
		}

		/**
		 * Return object of file storing logcat of app.
		 *
		 * @return file object of logcat file.
		 */
		public File getLogFile() {
			return mLogCatFile;
		}

		/**
		 * Reads logcat file stored in files directory of app.
		 *
		 * @return Logcat of app.
		 *
		 * @see #writeLogCat
		 * @see #deleteLogFile
		 */
		public String readLogCat() {
			if (mLogCatFile.exists())
				return FileUtils.read(mLogCatFile.toString());
			return null;
		}

		/**
		 * Writes logcat of in a file stored in the files dir app.
		 *
		 * @see #readLogCat
		 * @see #deleteLogFile
		 */
		public void writeLogCat() {
			try {
				StringBuffer logCatBuilder = new StringBuffer();
				if (!mLogCatFile.exists()) { 
					// Writing logcat for first time, add some basic device info.
					logCatBuilder.append("============ Device Info =============");
					logCatBuilder.append("\n" + Utils.getDeviceInfo());
					logCatBuilder.append("\n===================================");
				}
				String logCat = getLogCat();
				if (logCat != null && !(logCat.isEmpty())) {
					logCatBuilder.append("\n" + logCat.trim());
					logCatBuilder.append("\n============= END =================");
					FileUtils.write(mLogCatFile.toString(), logCatBuilder.toString(), true);
					clearLogCat();
				}
			} catch (IOException e) {
				Log.e(TAG,
					"writeLogCat():" +
					" Exception occurred while writing logcat of app to (" + mLogCatFile + ")", e);
			}
		}

		/**
		 * Delets file that stores logcat of app.
		 *
		 * @return true if file has been successfully deleted.
		 *
		 * @see #readLogCat
		 * @see #writeLogCat
		 */
		public boolean deleteLogFile() {
			try {
				clearLogCat();
				if (mLogCatFile.exists()) {
					if (mLogCatFile.delete())
						return true;
					else {
						Log.e(TAG, "deleteLogCatFile(): Could not delete logcat file (" + mLogCatFile + ")");
						return false;
					}
				}
				return true;
			} catch (IOException e) {
				Log.e(TAG, "deleteLogFile():"
					+ " Exception occured while deleting logcat file (" + mLogCatFile + ")", e);
				return false;
			}
		}
}
