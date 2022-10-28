package com.tangledbytes.screenfilter.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
	public static final String TAG = "FileUtils";
	/*
	 TODO: Make auto detection mechanism for differentating between text files and other
	 media files. Apply that to the following read write methods.
	 */
	/**
	 * Reads any type of given file.
	 *
	 * @param filePath Path of file from which you want to perform reading.
	 *
	 * @return Data that given file contains.
	 */
	public static String read(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			Log.e(TAG, "File \"" + file + "\" does not exist.");
			return null;
		} else if (file.isDirectory()) {
			Log.e(TAG, "File \"" + file + "\" is a driectory.");
			return null;
		}
		try {
			InputStream fileReader = new FileInputStream(file);
			StringBuilder readedFile = new StringBuilder();
			int readedCharacter;
			while ((readedCharacter = fileReader.read()) != -1)
				readedFile.append((char) readedCharacter);
			fileReader.close();
			return readedFile.toString();
		} catch (IOException e) {
			Log.e(TAG, 
				"Failed to read file \"" + file + "\".", e);
			return null;
		}
	}

	/**
	 * Reads text file.
	 *
	 * @param filePath Path of text file from which you want to read text.
	 *
	 * @return Text that given file contains.
	 */
	public static String readTextFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			Log.e(TAG, "File \"" + file + "\" does not exist.");
			return null;
		} else if (file.isDirectory()) {
			Log.e(TAG, "File \"" + file + "\" is a driectory.");
			return null;
		}
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuilder readedFile = new StringBuilder();
			String readedLine;
			while ((readedLine = bufferedReader.readLine()) != null)
				readedFile.append("\n").append(readedLine);
			readedFile = new StringBuilder(readedFile.substring(1));
			fileReader.close();
			bufferedReader.close();
			return readedFile.toString();
		} catch (IOException e) {
			Log.e(TAG, 
				"Failed to read text file \"" + file + "\".", e);
			return null;
		}
	}

	/**
	 * @see #write(String, String, boolean)
	 */
	public static boolean write(String filePath, String data) {
		return write(filePath, data, false);
	}

	/**
	 * @see #writeTextFile(String, String, boolean)
	 */
	public static boolean writeTextFile(String filePath, String data) {
		return writeTextFile(filePath, data, false);
	}

	/**
	 * Writes file with given data.
	 * 
	 * @param filePath Path of file on which you want to perform writing.
	 * @param data Data that you want to write in a file.
	 * @param append Append file data or not.
	 *
	 * @return True if given file has been wrote successfully.
	 */
	public static boolean write(String filePath, String data, boolean append) {
		File file = new File(filePath);
		if (file.isDirectory()) {
			Log.e(TAG, "File \"" + file + "\" is a driectory.");
			return false;
		}
		try {
			FileOutputStream fileWriter = new FileOutputStream(file, append);
			fileWriter.write(data.getBytes());
			fileWriter.close();
			return true;
		} catch (IOException e) {
			Log.e(TAG,
				"Failed to write file \"" + file + "\"", e);
			return false;
		}
	}

	/**
	 * Writes text file with given data.
	 * 
	 * @param filePath Path of file on which you want to perform writing.
	 * @param data Data that you want to write in a file.
	 * @param append Append file data or not.
	 *
	 * @return True if given file has been wrote successfully.
	 */
	public static boolean writeTextFile(String filePath, String data, boolean append) {
		File file = new File(filePath);
		if (file.isDirectory()) {
			Log.e(TAG, "File \"" + file + "\" is a driectory.");
			return false;
		}
		try {
			FileWriter fileWriter = new FileWriter(file, append);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(data);
			bufferedWriter.close();
			fileWriter.close();
			return true;
		} catch (IOException e) {
			Log.e(TAG,
				"Failed to write file \"" + file + "\"", e);
			return false;
		}
	}
}
