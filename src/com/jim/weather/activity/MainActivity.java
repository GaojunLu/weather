package com.jim.weather.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.jim.weather.R;
import com.jim.weather.bean.WeatherInfo;
import com.jim.weather.global.URL;
import com.jim.weather.utiles.Baidu2Hefeng;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.JsonToBean;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class MainActivity extends SlidingActivity implements OnClickListener {
	private WeatherInfo weatherInfo;
	private TextView tv_now;
	private List<TextView> tv_next = new ArrayList<TextView>();// 显示后三天的textview
	private String TAG = "MainActivity====";
	private String county;
	private String city;
	private TextView tv_title;
	private LinearLayout setting_changecounty;
	private SlidingMenu slidingMenu;
	private ImageView iv_menu;
	private PopupWindow popupWindow;
	private LinearLayout popView;
	private TextView tv_auto;
	private TextView tv_choise;
	private LocationClient mLocationClient;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String resault = (String) msg.obj;
			// Toast.makeText(ChoiceCounty.this, resault, 0).show();
			if (!TextUtils.isEmpty(resault) || resault.equals("null")) {
				String[] s = resault.split(",");
				city = s[0];
				county = s[1];
				s = Baidu2Hefeng.convert(city, county);
				city = s[0];
				county = s[1];
				getSharedPreferences("config", MODE_PRIVATE).edit()
						.putString("county", county).commit();
				getSharedPreferences("config", MODE_PRIVATE).edit()
						.putString("city", city).commit();
				upDateWeatherInfoFromSD(city, county);
			} else {
				Toast.makeText(MainActivity.this, "自动获取位置失败，请手动选择", 0).show();
				changeCounty();
			}
		};
	};
	private ImageView iv_refresh;
	private TextView tv_updatetime;
	private RotateAnimation rotate;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.leftmenu);
		initView();
		initData();
		intiListener();
	}

	private void intiListener() {
		// TODO Auto-generated method stub
		tv_title.setOnClickListener(this);
		setting_changecounty.setOnClickListener(this);
		iv_menu.setOnClickListener(this);
		iv_refresh.setOnClickListener(this);
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_now = (TextView) findViewById(R.id.tv_now);
		tv_next.add((TextView) findViewById(R.id.tv_next1));
		tv_next.add((TextView) findViewById(R.id.tv_next2));
		tv_next.add((TextView) findViewById(R.id.tv_next3));
		setting_changecounty = (LinearLayout) findViewById(R.id.setting_changecounty);
		iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
		tv_updatetime = (TextView) findViewById(R.id.tv_updatetime);
		// 侧边栏
		slidingMenu = getSlidingMenu();
		slidingMenu.setBehindOffset(getWindowManager().getDefaultDisplay().getWidth()/3);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setMode(SlidingMenu.LEFT);
		// 气泡
		popupWindow = new PopupWindow(this);
	}

	public void initData() {
		rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(300);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
		county = getSharedPreferences("config", MODE_PRIVATE).getString(
				"county", null);
		city = getSharedPreferences("config", MODE_PRIVATE).getString("city",
				null);
		upDateWeatherInfoFromSD(city, county);
	}

	/**
	 * 从本地获取天气信息
	 * 
	 * @param city
	 * @param county
	 * @return 是否成功
	 */
	public boolean upDateWeatherInfoFromSD(String city, String county) {
		String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			getCity();
			return false;
		} else {
			File file = new File(getFilesDir(), cityid + ".json");
			if (!file.exists()) {// 本地没有信息，就从网络获取
				upDateWeatherInfoOnline(city, county);
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
					String result = StringUtils.getStringByInputStream(fis);
					upDateUI(result);
					Logger.i(TAG, "从本地读取数据");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 根据市、县，从网络更新天气信息
	 * 
	 * @param city
	 * @param county
	 */
	public void upDateWeatherInfoOnline(String city, String county) {
		if (county == null || county.equals("null")) {
			getCity();
			return;
		}
		HttpUtils httpUtils = new HttpUtils();
		final String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			Toast.makeText(MainActivity.this, "自动获取地址失败，请手动选择", 0).show();
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("county", null).commit();
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("city", null).commit();
			return;
		}
		String url = URL.WEATHER_URL + "?cityid=" + cityid + "&key="
				+ URL.key_weather;
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				StringUtils.saveJson(MainActivity.this, cityid,
						responseInfo.result);
				getSharedPreferences("config", MODE_PRIVATE).edit()
						.putString("updatetime", (String) DateFormat.format("MM月dd日 HH:mm", System.currentTimeMillis())).commit();
				upDateUI(responseInfo.result);
				Logger.i(TAG, "从网络读取数据");
				Toast.makeText(MainActivity.this, "更新成功", 0).show();
			}

			@Override
			public void onFailure(HttpException e, String s) {
				Toast.makeText(MainActivity.this, "更新失败，请检查网络设置", 0).show();
				e.printStackTrace();
			}
		});
	}

	/**
	 * 更新ui
	 * 
	 * @param result
	 *            json字符串
	 */
	public void upDateUI(String result) {
		weatherInfo = JsonToBean.getWeatherInfo(result);
		tv_title.setText(weatherInfo.basic.city);
		tv_now.setText(weatherInfo.now.cond.txt + "\n" + weatherInfo.now.tmp
				+ "°C");
		for (int i = 0; i < tv_next.size(); i++) {
			WeatherInfo.Daily_forecast daily_forecast = weatherInfo.daily_forecast
					.get(i + 1);
			tv_next.get(i).setText(
					daily_forecast.cond.txt_d + "\\"
							+ daily_forecast.cond.txt_n + "\n"
							+ daily_forecast.tmp.min + "~"
							+ daily_forecast.tmp.max + "°C");
		}
		tv_updatetime.setText(getSharedPreferences("config", MODE_PRIVATE).getString("updatetime", "未更新"));
		iv_refresh.clearAnimation();
	}

	/**
	 * 更换位置
	 * 
	 * @param view
	 */
	public void changeCounty() {
		Intent intent = new Intent(MainActivity.this, ChoiceCounty.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * 点击事件的处理
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_title:// 标题栏的点击
			showPop4Choise(tv_title);
			break;
		case R.id.setting_changecounty:// 菜单切换城市的点击
			showPop4Choise(setting_changecounty);
			break;
		case R.id.iv_menu:// 显示、收起侧边栏
			slidingMenu.toggle();
			break;
		case R.id.tv_choise:// 手动选择城市
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.toggle();
			}
			changeCounty();
			popupWindow.dismiss();
			break;
		case R.id.tv_auto:// 自动定位显示
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.toggle();
			}
			popupWindow.dismiss();
			getCity();
			break;
		case R.id.iv_refresh://手动刷新
			if(city!=null&&county!=null){
				upDateWeatherInfoOnline(city, county);
				iv_refresh.setAnimation(rotate);
				iv_refresh.startAnimation(rotate);
			}else{
				getCity();
			}
			break;
		}
	}

	/**
	 * 处理menu键事件，收放侧边栏
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			slidingMenu.toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 气泡选择城市
	 * 
	 * @param v
	 *            将要挂载在v的下方
	 */
	public void showPop4Choise(View v) {
		if (popView == null) {
			popView = (LinearLayout) View.inflate(this, R.layout.pop4choise,
					null);
			tv_auto = (TextView) popView.findViewById(R.id.tv_auto);
			tv_choise = (TextView) popView.findViewById(R.id.tv_choise);
			tv_auto.setOnClickListener(this);
			tv_choise.setOnClickListener(this);
		}
		popupWindow.setContentView(popView);
		popupWindow.setWidth(-2);
		popupWindow.setHeight(-2);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true);
		popupWindow.showAsDropDown(v, 0, 0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	/**
	 * 自动定位城市（市县）
	 */
	public void getCity() {
		mLocationClient = new LocationClient(getApplicationContext());
		BDLocationListener myListener = new MyLocationListener();
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);
		option.setIsNeedAddress(true);
		mLocationClient.registerLocationListener(myListener);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			StringBuffer sb = new StringBuffer();
			int i = location.getLocType();
			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
				sb.append(location.getCity() + "," + location.getDistrict());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
				sb.append(location.getCity() + "," + location.getDistrict());
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
				sb.append(location.getCity());
			}
			Message msg = handler.obtainMessage();
			msg.obj = sb.toString();
			handler.sendMessage(msg);
			mLocationClient.stop();
		}

	}

	/**
	 * 接收选择的市县
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 10086) {
			String[] s = Baidu2Hefeng.convert(data.getStringExtra("city"),
					data.getStringExtra("county"));
			county = s[1];
			city = s[0];
			Logger.v(TAG, county);
			upDateWeatherInfoFromSD(city, county);
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("county", county).commit();
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("city", city).commit();
		}
	}
}
