package com.jim.weather.utiles;

import android.database.Cursor;

public class CursorUtils {
	public static void printCursor(Cursor cursor) {
		System.out.println("********һ��"+cursor.getCount()+"����¼*********");
		while (cursor.moveToNext()) {
			for (int i = 0; i < cursor.getColumnNames().length; i++) {
				System.out.println(cursor.getColumnName(i)+":"+cursor.getString(i));
			}
			System.out.println("============================");
		}
	}
}
