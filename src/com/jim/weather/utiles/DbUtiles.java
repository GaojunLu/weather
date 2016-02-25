package com.jim.weather.utiles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class DbUtiles {
	static String table = "id";

	/**
	 * 获取所有省
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getAllProvince(Context context) {
		List<String> allProvince = new ArrayList<String>();
		String path = context.getFilesDir().getAbsolutePath() + "/cityid.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select distinct province from id", null);
		while (cursor.moveToNext()) {
			String province = cursor.getString(0);
			if (!TextUtils.isEmpty(province)) {
				allProvince.add(province);
			}
		}
		db.close();
		cursor.close();
		return allProvince;
	}

	public static List<String> getAllCity(Context context, String province) {
		List<String> allCity = new ArrayList<String>();
		String path = context.getFilesDir().getAbsolutePath() + "/cityid.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery(
				"select distinct city from id where province = ?",
				new String[] { province });
		while (cursor.moveToNext()) {
			String city = cursor.getString(0);
			if (!TextUtils.isEmpty(city)) {
				allCity.add(city);
			}
		}
		db.close();
		cursor.close();
		return allCity;
	}

	public static List<String> getAllCounty(Context context, String city) {
		List<String> allCounty = new ArrayList<String>();
		String path = context.getFilesDir().getAbsolutePath() + "/cityid.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery(
				"select distinct county from id where city = ?",
				new String[] { city });
		while (cursor.moveToNext()) {
			String county = cursor.getString(0);
			if (!TextUtils.isEmpty(county)) {
				allCounty.add(county);
			}
		}
		db.close();
		cursor.close();
		return allCounty;
	}

	public static String getCityidByCounty(Context context, String county) {
		if (county == null) {
			return null;
		}
		String path = context.getFilesDir().getAbsolutePath() + "/cityid.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select _id from id where county = ?",
				new String[] { county });
		while (cursor.moveToNext()) {
			String id = cursor.getString(0);
			if (!TextUtils.isEmpty(id)) {
				return id;
			}
		}
		db.close();
		cursor.close();
		return null;
	}

	public static String getCityid(Context context, String city, String county) {
		if (county == null || city == null) {
			return null;
		}
		String path = context.getFilesDir().getAbsolutePath() + "/cityid.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor1 = db.rawQuery("select _id from id where county = ?",
				new String[] { county });
		if (cursor1.moveToNext()) {
			String id = cursor1.getString(0);
			if (!TextUtils.isEmpty(id)) {
				return id;
			}
		}else{
			Cursor cursor2 = db.rawQuery("select _id from id where city = ?",
					new String[] { city });
			if (cursor2.moveToNext()) {
				String id = cursor2.getString(0);
				if (!TextUtils.isEmpty(id)) {
					return id;
				}
			}
			cursor2.close();
		}
		db.close();
		cursor1.close();
		return null;
	}
}
