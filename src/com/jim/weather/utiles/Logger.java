package com.jim.weather.utiles;

import android.util.Log;

public class Logger {
	static final int VERBOSE = 1;
	static final int DEBUG = 2;
	static final int INFO = 3;
	static final int WARM = 4;
	static final int ERROR = 5;
	private static final int CURRENT = VERBOSE;
	public static void v(String tag, String msg){
		if(CURRENT>=VERBOSE){
			Log.v(tag, msg);
		}
	}
	public static void d(String tag, String msg){
		if(CURRENT>=DEBUG){
			Log.d(tag, msg);
		}
	}
	public static void w(String tag, String msg){
		if(CURRENT>=WARM){
			Log.w(tag, msg);
		}
	}
	public static void e(String tag, String msg){
		if(CURRENT>=ERROR){
			Log.e(tag, msg);
		}
	}
	public static void i(String tag, String msg){
		if(CURRENT>=INFO){
			Log.i(tag, msg);
		}
	}
}
