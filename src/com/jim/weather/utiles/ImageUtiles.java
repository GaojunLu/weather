package com.jim.weather.utiles;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtiles {
/**
 * 根据天气码获取对应天气图片	
 * @param context
 * @param code
 * @return
 */
	public static Drawable getDrawableIconByWeathercode(Context context, String code){
		try {
			InputStream is = context.getAssets().open(code+".png");
			Drawable drawable = new BitmapDrawable(context.getResources(), is);
			return drawable;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static Bitmap getBitmapIconByWeathercode(Context context, String code){
		try {
			InputStream is = context.getAssets().open(code+".png");
			Bitmap bitmap = new BitmapFactory().decodeStream(is);
			return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
