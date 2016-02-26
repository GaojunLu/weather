package com.jim.weather.utiles;

import android.database.Cursor;
/**
 * 打印cursor中的内容
 * @author Administrator
 *
 */
public class CursorUtils {
	public static void printCursor(Cursor cursor) {
		System.out.println("********一共"+cursor.getCount()+"条记录*********");
		while (cursor.moveToNext()) {
			for (int i = 0; i < cursor.getColumnNames().length; i++) {
				System.out.println(cursor.getColumnName(i)+":"+cursor.getString(i));
			}
			System.out.println("============================");
		}
	}
}
