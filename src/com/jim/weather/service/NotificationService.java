package com.jim.weather.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.jim.weather.R;
import com.jim.weather.activity.MainActivity;
import com.jim.weather.bean.WeatherInfo;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.ImageUtiles;
import com.jim.weather.utiles.JsonToBean;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationService extends Service {

	private NotificationManager manager;
	private Notification notification;
	private String county;
	private String city;
	private AutoUpdateReciever autoUpdateReciever;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 从本地拿数据
		getWeatherInfoFromSD();
		Logger.i(this.getClass().getSimpleName(), "通知栏服务onCreate");
		// 注册广播接收更新通知
		autoUpdateReciever = new AutoUpdateReciever();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.jim.weather.autoupdate");
		registerReceiver(autoUpdateReciever, filter);
	}

	/**
	 * 从本地获取数据并显示到通知栏
	 */
	public void getWeatherInfoFromSD() {
		county = getSharedPreferences("config", MODE_PRIVATE).getString(
				"county", null);
		city = getSharedPreferences("config", MODE_PRIVATE).getString("city",
				null);
		String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			return;
		} else {
			File file = new File(getFilesDir(), cityid + ".json");
			if (!file.exists()) {// 本地没有信息，就从网络获取
				return;
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
					String result = StringUtils.getStringByInputStream(fis);
					upDateNotificationUI(result);
					Logger.i(getClass().getSimpleName(), "通知栏从本地读取数据");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 设置通知栏上的显示数据
	 * 
	 * @param result
	 */
	private void upDateNotificationUI(String result) {
		// TODO Auto-generated method stub
		WeatherInfo weatherInfo = JsonToBean.getWeatherInfo(result);
		if ("ok".equalsIgnoreCase(weatherInfo.status)) {
			RemoteViews remoteViews = new RemoteViews(getPackageName(),
					R.layout.notification);
			remoteViews.setTextViewText(R.id.tv_county, weatherInfo.basic.city);
			remoteViews.setTextViewText(R.id.tv_temp, weatherInfo.now.tmp
					+ "°C");
			remoteViews.setTextViewText(R.id.tv_weather,
					weatherInfo.now.cond.txt);
			remoteViews.setTextViewText(R.id.tv_updatetime,
					"更新于"
							+ getSharedPreferences("config", MODE_PRIVATE)
									.getString("updatetime", "未更新"));
			remoteViews.setImageViewBitmap(R.id.iv_weather_icon, ImageUtiles
					.getBitmapIconByWeathercode(NotificationService.this,
							weatherInfo.now.cond.code));
			notification = new Notification();
			notification.flags = Notification.FLAG_NO_CLEAR;
			notification.icon = R.drawable.ic_launcher;
			notification.contentView = remoteViews;
			Intent intent = new Intent();
			intent.setAction("com.jim.weather.splash");
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			notification.contentIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(0, notification);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		manager.cancel(0);
		manager.cancelAll();
		unregisterReceiver(autoUpdateReciever);
		getSharedPreferences("config", MODE_PRIVATE).edit()
				.putBoolean("shownotification", false).commit(); 
		Logger.i(this.getClass().getSimpleName(), "通知栏服务onDestroy");
	}
	
	/**
	 * 接收更新广播，收到后更新
	 * 
	 * @author Administrator
	 *
	 */
	class AutoUpdateReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			getWeatherInfoFromSD();
			Logger.i(getClass().getSimpleName(), "通知栏收到更新广播");
		}

	}

}
