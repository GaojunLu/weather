package com.jim.weather.service;

import java.util.Timer;
import java.util.TimerTask;

import com.jim.weather.activity.MainActivity;
import com.jim.weather.global.URL;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.widget.Toast;
/**
 * 定时器更新
 * @author Administrator
 *
 */
public class AutoUpdateService extends Service {
	private String tag = "AutoUpdateService====";
	private AlarmManager alarmManager;
	private PendingIntent operation;
	private PowerManager powerManager;
	private WakeLock newWakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Logger.i(tag, "自动更新服务onCreate");
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		long intervalMillis = getSharedPreferences("config", MODE_PRIVATE).getInt("autoupdate", 0)*1000*3600;
		if(intervalMillis!=0){
			Intent intent = new Intent(this, UpdateService.class);
			operation = PendingIntent.getService(this, 0, intent , PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.currentThreadTimeMillis(), intervalMillis, operation );
		}
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		newWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
		newWakeLock.acquire();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Logger.i(tag, "自动更新服务onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Logger.i(tag, "自动更新服务onDestroy");
		if(alarmManager!=null){
			alarmManager.cancel(operation);
		}
		if(newWakeLock!=null){
			newWakeLock.release();
		}
		super.onDestroy();
	}

}
