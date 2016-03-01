package com.jim.weather.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.jim.weather.R;
import com.jim.weather.bean.WeatherInfo;
import com.jim.weather.service.UpdateService;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.ImageUtiles;
import com.jim.weather.utiles.JsonToBean;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

public class MyAppWidgetProvider extends AppWidgetProvider {
	private String county;
	private String city;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		if ("com.jim.weather.autoupdate".equals(intent.getAction())) {
			upDateWiget(context, AppWidgetManager.getInstance(context));
		}
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
		Logger.i(getClass().getSimpleName(), "控件onEnabled");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Logger.i(getClass().getSimpleName(), "控件onUpdate");
		upDateWiget(context, appWidgetManager);

	}

	public void upDateWiget(Context context, AppWidgetManager appWidgetManager) {
		county = context.getSharedPreferences("config", Context.MODE_PRIVATE)
				.getString("county", null);
		city = context.getSharedPreferences("config", Context.MODE_PRIVATE)
				.getString("city", null);
		String cityid = DbUtiles.getCityid(context, city, county);
		if (cityid == null) {
			return;
		} else {
			File file = new File(context.getFilesDir(), cityid + ".json");
			if (!file.exists()) {// 本地没有信息，就从网络获取
				return;
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
					String result = StringUtils.getStringByInputStream(fis);
					Logger.i(getClass().getSimpleName(), "控件从本地读取数据");
					WeatherInfo weatherInfo = JsonToBean.getWeatherInfo(result);
					if ("ok".equalsIgnoreCase(weatherInfo.status)) {
						Intent intent = new Intent(context, UpdateService.class);
						PendingIntent pendingIntent = PendingIntent
								.getService(context, 0, intent, 0);
						RemoteViews remoteViews = new RemoteViews(
								context.getPackageName(), R.layout.widget);
						remoteViews.setTextViewText(R.id.tv_county,
								weatherInfo.basic.city);
						remoteViews.setTextViewText(R.id.tv_temp,
								weatherInfo.now.tmp + "°C");
						remoteViews.setTextViewText(R.id.tv_weather,
								weatherInfo.now.cond.txt);
						remoteViews.setTextViewText(
								R.id.tv_updatetime,
								context.getSharedPreferences("config",
										Context.MODE_PRIVATE).getString(
										"updatetime", "未更新"));
						remoteViews.setImageViewBitmap(R.id.iv_weather_icon,
								ImageUtiles.getBitmapIconByWeathercode(context,
										weatherInfo.now.cond.code));
						remoteViews.setOnClickPendingIntent(R.id.iv_refresh, pendingIntent);
						ComponentName componentName = new ComponentName(
								context, MyAppWidgetProvider.class);
						appWidgetManager.updateAppWidget(componentName,
								remoteViews);
						Logger.i(getClass().getSimpleName(), "桌面控件更新");
					}

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Logger.i(getClass().getSimpleName(), "控件onDisabled");
	}
}
