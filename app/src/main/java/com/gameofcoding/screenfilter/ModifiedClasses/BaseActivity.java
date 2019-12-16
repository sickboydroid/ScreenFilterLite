package com.gameofcoding.screenfilter.ModifiedClasses;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * BaseActivity is just a simple {@link android.app.Activity} with some handy 
 * methods like {@link #read(String)} which reads a file and returns what ever
 * is present in it.
 */

public class BaseActivity extends Activity {
    public final String TAG = "BaseActivity";
    private final Context mContext = this;

    /**
     * Handy method for displaying short Toast.
     *
     * @param resIdText Resource ID of text that you want to display in Toast.
     * @see #showToast(int, boolean)
     */
    protected void showToast(int resIdText) {
		showToast(resIdText, true);
    }

    /**
     * Handy method for displaying Toast.
     *
     * <p>Note: If you want to display short Toast use {@link #showToast(int)} instead
     * of this.</p>
     *
     * @param resIdText Resource ID of text that you want to display in Toast.
     * @param small True if you want to display small Toast and false if you want
     * to display long Toast.
     */
    protected void showToast(int resIdText, boolean small) {
        Toast.makeText(mContext, resIdText, 
					   small ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
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
	protected float toDp(int pixels) {
		return pixels * mContext.getResources().getDisplayMetrics().density;
	}

    /**
     * @see #getPreference(String, String)
     */
    protected boolean getPreference(String key, boolean defaultValue) {
        if (key.isEmpty())
            return defaultValue;
        SharedPreferences sharedPref = PreferenceManager
			.getDefaultSharedPreferences(mContext);

        if (!sharedPref.contains(key))
            return defaultValue;
        return sharedPref.getBoolean(key, defaultValue);
    }

    /**
     * Handy method for getting preferences of app.
     *
     * @param key Key of requested preference.
     * @param defaultValue In case preference is not found then this will be returned.
     *
     * @return Value of requested preference.
     */
    protected String getPreference(String key, String defaultValue) {
        if (key.isEmpty())
            return defaultValue;
        SharedPreferences sharedPref = PreferenceManager
			.getDefaultSharedPreferences(mContext);

        if (!sharedPref.contains(key))
            return defaultValue;
        return sharedPref.getString(key, defaultValue);
    }

	/**
     * Reads and returns logcat of app by running <b>logcat -d</b>
     * (-d option means dump) shell command. 
     * 
     * @return Logcat of app.
     *
     * @throws java.io.IOException.
     */
    public String getLogCat() throws IOException {
        return shell("logcat -d");
    }

    /**
     * Clears logcat using <b>logcat -c</b> (-c option means clear) shell command.
     *
     * @throws java.io.IOException.
     */
    public void clearLog() throws IOException {
        shell("logcat -c");
    }

    /**
     * Runs given linux command using android's built in shell.
     *
     * @param cmd Command that you want to execute.
     *
     * @return Output of given command.
     *
     * @throws IOException it could cause while reading output of command.
     */
    public String shell(String cmd) throws IOException {
        Process proc;
		proc = Runtime.getRuntime().exec(cmd);
		// Reads result of command
        final InputStream inputStreamOutput  = proc.getInputStream();
		// Reads error (if occured) of command
        final InputStream inputStreamError = proc.getErrorStream();

        String output = "";
        int readedChar;
        while ((readedChar = inputStreamOutput.read()) != -1)
            output += (char) readedChar;
        while ((readedChar = inputStreamError.read()) != -1)
            output += (char) readedChar;
        inputStreamOutput.close();
        inputStreamError.close();
        return output;
    }

    /**
     * Reads file.
     *
     * <p>Note: If you want to read text file then use {@link #readTextFile(String)}
     * because that reads text files using {@link java.io.BufferedReader} which uses
     * the buffering for reading text files and saves some time and also computing
     * power.</p>
     *
     * @param pathName Path of file from which you want to read data.
     *
     * @return Data that given file contains.
     *
     * @throws java.io.IOException
     */
    public String read(String pathName) throws IOException {
        InputStream fileReader = new FileInputStream(pathName);
        String readedFile = "";
        int readedCharacter;
        while ((readedCharacter = fileReader.read()) != -1)
            readedFile += (char) readedCharacter;
        fileReader.close();
        return readedFile;
    }

    /**
     * Reads text file.
     *
     * @param pathName Path of text file from which you want to read text.
     *
     * @return Text that given file contains.
     *
     * @throws java.io.IOException
     */
    public String readTextFile(String pathName) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(pathName));
        String readedFile = "";
        String readedLine;
        while ((readedLine = fileReader.readLine()) != null)
            readedFile += readedLine;
        fileReader.close();
        return readedFile;
    }

    /**
     * @param pathName Path of file on which you want to perform writing.
     * @param str Data that you want to write in a file.
     *
     * @throws java.io.FileNotFoundException In case give file is not found
     *
     * @see #write(String, String, boolean)
     */
    public void write(String pathName, String str) throws IOException {
		write(pathName, str, false);
    }

    /**
     * Writes file.
     * 
     * @param pathName Path of file on which you want to perform writing.
     * @param str Data that you want to write in a file.
     * @param append Append file data or not.
     *
     * @throws java.io.FileNotFoundException In case give file is not found
     */
    public void write(String pathName, String data, boolean append) 
    throws FileNotFoundException, IOException {
        FileOutputStream fileWriter = new FileOutputStream(pathName, append);
        fileWriter.write(data.getBytes());
        fileWriter.close();
    }

    /**
     *
     * TODO: Delete all the methods below, on the release of app.
     *
     */
    protected void showToast(Object msg) {
        Toast.makeText(mContext, msg.toString(), Toast.LENGTH_LONG).show();
    }
}
