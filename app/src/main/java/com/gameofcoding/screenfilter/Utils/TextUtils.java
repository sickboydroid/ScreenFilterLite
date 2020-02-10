package com.gameofcoding.screenfilter.Utils;

public class TextUtils {
	public static boolean isEmpty(String cs) {
		return cs != null && cs.isEmpty();
	}

	public static String toString(Object[] arr) {
		return toString(arr, ", ");
	}
	
	public static String toString(Object[] arr, String delimiter) {
		if (arr == null)
			return null;
		else if (isEmpty(delimiter))
			delimiter += ", ";

		String strArr = "";
		for (Object element : arr)
			strArr += element.toString() + ", ";
		if (strArr.length() > 2)
			strArr = strArr.substring(strArr.length(), strArr.length() - 2);
		return strArr;
	}
}
