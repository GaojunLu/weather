package com.jim.weather;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.weather.bean.WeatherInfo;
import com.jim.weather.global.URL;
import com.jim.weather.utiles.Baidu2Hefeng;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.JsonToBean;
import com.jim.weather.utiles.Logger;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class MainActivity extends Activity {
	private WeatherInfo weatherInfo;
	private TextView tv_county;
	private TextView tv_now;
	private List<TextView> tv_next = new ArrayList<TextView>();
	private String TAG = "====";
	private String county;
	private String city;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
	}

	private void initView() {
		tv_county = (TextView) findViewById(R.id.tv_county);
		tv_now = (TextView) findViewById(R.id.tv_now);
		tv_next.add((TextView) findViewById(R.id.tv_next1));
		tv_next.add((TextView) findViewById(R.id.tv_next2));
		tv_next.add((TextView) findViewById(R.id.tv_next3));
	}

	public void initData() {
		county = getSharedPreferences("config", MODE_PRIVATE).getString("county", null);
		copyDataBase("cityid.db");//复制数据库
		upDateWeatherInfo(city, county);
	}

	public void upDateWeatherInfo(String city, String county) {
		if(county==null||county.equals("null")){
			changeCounty(null);
			return;
		}
		HttpUtils httpUtils = new HttpUtils();
		String cityid = DbUtiles.getCityid(this, city, county);
		if(cityid == null){
			Toast.makeText(MainActivity.this, "自动获取地址失败，请手动选择", 0).show();
			getSharedPreferences("config", MODE_PRIVATE).edit().putString("county", null).commit();
			return;
		}
		String url = URL.WEATHER_URL + "?cityid=" + cityid + "&key=" + URL.key_weather;
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				weatherInfo = JsonToBean.getWeatherInfo(responseInfo.result);
				tv_county.setText(weatherInfo.basic.city);
				tv_now.setText(weatherInfo.now.cond.txt + "\n"
						+ weatherInfo.now.tmp + "°C");
				for (int i = 0; i < tv_next.size(); i++) {
					WeatherInfo.Daily_forecast daily_forecast = weatherInfo.daily_forecast
							.get(i + 1);
					tv_next.get(i).setText(
							daily_forecast.cond.txt_d + "\\"
									+ daily_forecast.cond.txt_n + "\n"
									+ daily_forecast.tmp.min + "~"
									+ daily_forecast.tmp.max + "°C");
				}
			}

			@Override
			public void onFailure(HttpException e, String s) {
				Toast.makeText(MainActivity.this, "更新失败，请检查网络设置", 0).show();
				e.printStackTrace();
			}
		});
	}

	public void changeCounty(View view) {
		Intent intent = new Intent(MainActivity.this, ChoiceCounty.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 10086) {
			String[] s = Baidu2Hefeng.convert(data.getStringExtra("city"), data.getStringExtra("county"));
			county = s[1];
			city = s[0];
			Logger.v(TAG, county);
			upDateWeatherInfo(city, county);
			getSharedPreferences("config", MODE_PRIVATE).edit().putString("county", county).commit();
		}
	}

	private void copyDataBase(final String dbName) {
		// TODO Auto-generated method stub
//		new Thread() {
//			public void run() {
				File file = new File(getFilesDir(), dbName);
				if (file.exists()) {
					System.out.println("数据库" + dbName + "已经存在");
					return;
				}
				AssetManager assetManager = getAssets();
				try {
					InputStream is = assetManager.open(dbName);
					FileOutputStream fos = new FileOutputStream(file);
					int len = 0;
					byte[] b = new byte[1024];
					while ((len = is.read(b)) != -1) {
						fos.write(b, 0, len);
					}
					is.close();
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			};
//		}.start();
	}
}
