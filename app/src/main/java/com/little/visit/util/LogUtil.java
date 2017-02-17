package com.little.visit.util;

import android.util.Log;

public class LogUtil {
	private static boolean isDebug = false;
	private static final String TAG = "LogUtil";

	public static void out(String msg) {
		if (isDebug)
			System.out.println(msg);
	}

	public static void d(String msg) {
		if (isDebug)
			Log.d(TAG, msg);
	}

	public static void i(String msg) {
		if (isDebug)
			Log.i(TAG, msg);
	}

	public static void v(String msg) {
		if (isDebug)
			Log.v(TAG, msg);
	}

	public static void w(String msg) {
		if (isDebug)
			Log.w(TAG, msg);
	}

	public static void e(String msg) {
		if (isDebug)
			Log.e(TAG, msg);
	}

	public static boolean isDebug() {
		return isDebug;
	}

	public static void setIsDebug(boolean isDebug) {
		LogUtil.isDebug = isDebug;
	}


}
