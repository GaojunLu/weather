package com.jim.weather.service;

import com.jim.weather.global.URL;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateFormat;
/**
 * 从网络获取数据，写到本地，发出更新广播，然后自我销毁
 * @author Administrator
 *
 */
public class UpdateService extends Service {

	private String county;
	private String city;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Logger.i(getClass().getSimpleName(), "更新服务onCreate");
		county = getSharedPreferences("config", MODE_PRIVATE).getString(
				"county", null);
		city = getSharedPreferences("config", MODE_PRIVATE).getString("city",
				null);
		upDateWeatherInfo();
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
		Logger.i(getClass().getSimpleName(), "更新服务onDestroy");
	}
	
	/**
	 * 先从sp读取城市，再去请求数据，写到本地，完成后发出更新完成的广播
	 */
	protected void upDateWeatherInfo() {
		// 出现下列两种情况就不用更新了
		if (county == null || county.equals("null") || city == null
				|| city.equals("null")) {
			return;
		}
		final String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			return;
		}

		HttpUtils httpUtils = new HttpUtils();
		String url = URL.WEATHER_URL + "?cityid=" + cityid + "&key="
				+ URL.key_weather;
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				StringUtils.saveJson(UpdateService.this, cityid,
						responseInfo.result);
				getSharedPreferences("config", MODE_PRIVATE)
						.edit()
						.putString(
								"updatetime",
								(String) DateFormat.format("MM月dd日 HH:mm:ss",
										System.currentTimeMillis())).commit();
				Logger.i(getClass().getSimpleName(), "成功从网络读取数据");
				// 发送更新广播
				Intent intent = new Intent();
				intent.setAction("com.jim.weather.autoupdate");
				sendBroadcast(intent);
				stopSelf();
			}

			@Override
			public void onFailure(HttpException e, String s) {
				Logger.i(getClass().getSimpleName(), "更新失败");
				e.printStackTrace();
			}
		});
	}

}
