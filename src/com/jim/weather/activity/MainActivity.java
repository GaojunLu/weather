package com.jim.weather.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.PopupWindow.OnDismissListener;
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
import com.jim.weather.service.AutoUpdateService;
import com.jim.weather.service.NotificationService;
import com.jim.weather.utiles.Baidu2Hefeng;
import com.jim.weather.utiles.DbUtiles;
import com.jim.weather.utiles.ImageUtiles;
import com.jim.weather.utiles.JsonToBean;
import com.jim.weather.utiles.Logger;
import com.jim.weather.utiles.StringUtils;
import com.jim.weather.utiles.TaskUtiles;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class MainActivity extends SlidingActivity implements OnClickListener {
	private WeatherInfo weatherInfo;
	private TextView tv_now;
	private List<TextView> tv_next = new ArrayList<TextView>();// ��ʾ�������textview
	private String TAG = "MainActivity====";
	private String county;
	private String city;
	private TextView tv_title;
	private RelativeLayout setting_changecounty;
	private SlidingMenu slidingMenu;
	private ImageView iv_menu;
	private PopupWindow popupWindow;
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
				Toast.makeText(MainActivity.this, "�Զ���ȡλ��ʧ�ܣ����ֶ�ѡ��", 0).show();
				changeCounty();
			}
		};
	};
	private ImageView iv_refresh;
	private TextView tv_updatetime;
	private RotateAnimation rotate;
	private RelativeLayout setting_autoupdate;
	private LinearLayout popView;
	private AutoUpdateReciever autoUpdateReciever;
	private TextView tv_autoupdate_desc;
	private ImageView iv_now_icon;
	private RelativeLayout setting_notification;
	private TextView tv_notification_desc;

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
		setting_autoupdate.setOnClickListener(this);
		setting_notification.setOnClickListener(this);
		tv_notification_desc.setOnClickListener(this);
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_now = (TextView) findViewById(R.id.tv_now);
		tv_next.add((TextView) findViewById(R.id.tv_next1));
		tv_next.add((TextView) findViewById(R.id.tv_next2));
		tv_next.add((TextView) findViewById(R.id.tv_next3));
		setting_changecounty = (RelativeLayout) findViewById(R.id.setting_changecounty);
		setting_autoupdate = (RelativeLayout) findViewById(R.id.setting_autoupdate);
		iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
		tv_updatetime = (TextView) findViewById(R.id.tv_updatetime);
		tv_autoupdate_desc = (TextView) findViewById(R.id.tv_autoupdate_desc);
		iv_now_icon = (ImageView) findViewById(R.id.iv_now_icon);
		setting_notification = (RelativeLayout) findViewById(R.id.setting_notification);
		tv_notification_desc = (TextView) findViewById(R.id.tv_notification_desc);
		// �����
		slidingMenu = getSlidingMenu();
		slidingMenu.setBehindOffset(getWindowManager().getDefaultDisplay()
				.getWidth() / 3);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setMode(SlidingMenu.LEFT);

	}

	public void initData() {
		rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(300);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
		county = getSharedPreferences("config", MODE_PRIVATE).getString(
				"county", null);
		city = getSharedPreferences("config", MODE_PRIVATE).getString("city",
				null);
		upDateWeatherInfoFromSD(city, county);
		if(TaskUtiles.isServiceRunning(this, "com.jim.weather.service.AutoUpdateService")){
			switch (getSharedPreferences("config", MODE_PRIVATE).getInt(
					"autoupdate", 0)) {
					case 0:
						tv_autoupdate_desc.setText("�ر�");
						break;
					case 1:
						tv_autoupdate_desc.setText("1Сʱ");
						break;
					case 3:
						tv_autoupdate_desc.setText("3Сʱ");
						break;
					case 6:
						tv_autoupdate_desc.setText("6Сʱ");
						break;
					case 12:
						tv_autoupdate_desc.setText("12Сʱ");
						break;
			}
		}else{
			tv_autoupdate_desc.setText("�ر�");
		}
		//���֪ͨ�����Ƿ������У�������ʾ�������ǹر�
		if(TaskUtiles.isServiceRunning(this, "com.jim.weather.service.NotificationService")){
			tv_notification_desc.setText("����");
		}else{
			tv_notification_desc.setText("�ر�");
		}
		// ע����¹㲥

		autoUpdateReciever = new AutoUpdateReciever();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.jim.weather.autoupdate");
		registerReceiver(autoUpdateReciever, filter);
	}

	/**
	 * �ӱ��ػ�ȡ������Ϣ
	 * 
	 * @param city
	 * @param county
	 * @return �Ƿ�ɹ�
	 */
	public boolean upDateWeatherInfoFromSD(String city, String county) {
		String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			getCity();
			return false;
		} else {
			File file = new File(getFilesDir(), cityid + ".json");
			if (!file.exists()) {// ����û����Ϣ���ʹ������ȡ
				upDateWeatherInfoOnline(city, county);
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
					String result = StringUtils.getStringByInputStream(fis);
					upDateUI(result);
					Logger.i(TAG, "�ӱ��ض�ȡ����");
					return true;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * �����С��أ����������������Ϣ
	 * 
	 * @param city
	 * @param county
	 */
	public void upDateWeatherInfoOnline(String city, String county) {
		if (county == null || county.equals("null")) {
			getCity();
			return;
		}
		final String cityid = DbUtiles.getCityid(this, city, county);
		if (cityid == null) {
			Toast.makeText(MainActivity.this, "�Զ���ȡ��ַʧ�ܣ����ֶ�ѡ��", 0).show();
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("county", null).commit();
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putString("city", null).commit();
			return;
		}
		HttpUtils httpUtils = new HttpUtils();
		String url = URL.WEATHER_URL + "?cityid=" + cityid + "&key="
				+ URL.key_weather;
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				StringUtils.saveJson(MainActivity.this, cityid,
						responseInfo.result);
				getSharedPreferences("config", MODE_PRIVATE)
						.edit()
						.putString(
								"updatetime",
								(String) DateFormat.format("MM��dd�� HH:mm:ss",
										System.currentTimeMillis())).commit();
				Logger.i(TAG, "�������ȡ����");
//				upDateUI(responseInfo.result);
//				Toast.makeText(MainActivity.this, "���³ɹ�", 0).show();
				//���͸��¹㲥
				Intent intent = new Intent();
				intent.setAction("com.jim.weather.autoupdate");
				sendBroadcast(intent);
			}

			@Override
			public void onFailure(HttpException e, String s) {
				Toast.makeText(MainActivity.this, "����ʧ�ܣ�������������", 0).show();
				e.printStackTrace();
			}
		});
	}

	/**
	 * ����ui
	 * 
	 * @param result
	 *            json�ַ���
	 */
	public void upDateUI(String result) {
		weatherInfo = JsonToBean.getWeatherInfo(result);
		if (!"ok".equalsIgnoreCase(weatherInfo.status)) {// ��ʱ�ò�����ȷ��Ϣ
			Toast.makeText(MainActivity.this, "�������ֶ�ˢ��", 0).show();
			return;
		}
		tv_title.setText(weatherInfo.basic.city);
		tv_now.setText(weatherInfo.now.cond.txt + "\n" + weatherInfo.now.tmp
				+ "��C");
		iv_now_icon.setBackgroundDrawable((ImageUtiles
				.getDrawableIconByWeathercode(MainActivity.this,
						weatherInfo.now.cond.code)));
		for (int i = 0; i < tv_next.size(); i++) {
			WeatherInfo.Daily_forecast daily_forecast = weatherInfo.daily_forecast
					.get(i + 1);
			StringBuilder builder = new StringBuilder();
			builder.append(daily_forecast.date.substring(5) + "\n");
			builder.append(daily_forecast.cond.txt_d + "\\"
					+ daily_forecast.cond.txt_n + "\n" + daily_forecast.tmp.min
					+ "~" + daily_forecast.tmp.max + "��C");
			tv_next.get(i).setText(builder.toString());
		}
		tv_updatetime.setText(getSharedPreferences("config", MODE_PRIVATE)
				.getString("updatetime", "δ����"));
		iv_refresh.clearAnimation();
	}

	/**
	 * ����λ��
	 * 
	 * @param view
	 */
	public void changeCounty() {
		Intent intent = new Intent(MainActivity.this, ChoiceCounty.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * ����¼��Ĵ���
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_title:// �������ĵ��
			showPop4ChoiseCity(tv_title);
			break;
		case R.id.setting_changecounty:// �˵��л����еĵ��
			showPop4ChoiseCity(setting_changecounty);
			break;
		case R.id.setting_autoupdate:// �˵��Զ����µ��
			showPop4SetAutoUpdate(setting_autoupdate);
			break;
		case R.id.iv_menu:// ��ʾ����������
			slidingMenu.toggle();
			break;
		case R.id.tv_choise:// �ֶ�ѡ�����
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.toggle();
			}
			changeCounty();
			popupWindow.dismiss();
			break;
		case R.id.tv_auto:// �Զ���λ��ʾ
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.toggle();
			}
			popupWindow.dismiss();
			getCity();
			break;
		case R.id.iv_refresh:// �ֶ�ˢ��
			if (city != null && county != null) {
				upDateWeatherInfoOnline(city, county);
				iv_refresh.setAnimation(rotate);
				iv_refresh.startAnimation(rotate);
			} else {
				getCity();
			}
			break;
		// ���������Զ��������ݵİ�ť
		case R.id.tv_off:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putInt("autoupdate", 0).commit();
			stopService(new Intent(MainActivity.this, AutoUpdateService.class));
			popupWindow.dismiss();
			slidingMenu.toggle();
			tv_autoupdate_desc.setText("�ر�");
			break;
		case R.id.tv_1h:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putInt("autoupdate", 1).commit();
			startService(new Intent(MainActivity.this, AutoUpdateService.class));
			popupWindow.dismiss();
			slidingMenu.toggle();
			tv_autoupdate_desc.setText("1Сʱ");
			break;
		case R.id.tv_3h:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putInt("autoupdate", 3).commit();
			startService(new Intent(MainActivity.this, AutoUpdateService.class));
			popupWindow.dismiss();
			slidingMenu.toggle();
			tv_autoupdate_desc.setText("3Сʱ");
			break;
		case R.id.tv_6h:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putInt("autoupdate", 6).commit();
			startService(new Intent(MainActivity.this, AutoUpdateService.class));
			popupWindow.dismiss();
			slidingMenu.toggle();
			tv_autoupdate_desc.setText("6Сʱ");
			break;
		case R.id.tv_12h:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putInt("autoupdate", 12).commit();
			startService(new Intent(MainActivity.this, AutoUpdateService.class));
			popupWindow.dismiss();
			slidingMenu.toggle();
			tv_autoupdate_desc.setText("12Сʱ");
			break;
		// ֪ͨ��
		case R.id.setting_notification:
			showPop4SetNotification(v);
			break;
		case R.id.tv_notifiation_on:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putBoolean("shownotification", true).commit();
			startService(new Intent(MainActivity.this,
					NotificationService.class));
			slidingMenu.toggle();
			popupWindow.dismiss();
			tv_notification_desc.setText("����");
			break;
		case R.id.tv_notifiation_off:
			getSharedPreferences("config", MODE_PRIVATE).edit()
					.putBoolean("shownotification", false).commit();
			stopService(new Intent(MainActivity.this, NotificationService.class));
			slidingMenu.toggle();
			popupWindow.dismiss();
			tv_notification_desc.setText("�ر�");
			break;
		}
	}

	/**
	 * ����menu���¼����շŲ����
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
	 * ����ѡ�����
	 * 
	 * @param v
	 *            ��Ҫ������v���·�
	 */
	public void showPop4ChoiseCity(View v) {
		popView = (LinearLayout) View.inflate(this, R.layout.pop4choise_city,
				null);
		tv_auto = (TextView) popView.findViewById(R.id.tv_auto);
		tv_choise = (TextView) popView.findViewById(R.id.tv_choise);
		tv_auto.setOnClickListener(this);
		tv_choise.setOnClickListener(this);
		popupWindow = new PopupWindow(this);
		popupWindow.setContentView(popView);
		popupWindow.setWidth(-2);
		popupWindow.setHeight(-2);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true);
		popupWindow.showAsDropDown(v, 0, 0);
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				popupWindow = null;
				popView = null;
			}
		});
	}

	/**
	 * �����Զ����µ�pop
	 */
	public void showPop4SetAutoUpdate(View v) {
		popView = (LinearLayout) View.inflate(this,
				R.layout.pop4set_autoupdate, null);
		TextView tv_off = (TextView) popView.findViewById(R.id.tv_off);
		TextView tv_1h = (TextView) popView.findViewById(R.id.tv_1h);
		TextView tv_3h = (TextView) popView.findViewById(R.id.tv_3h);
		TextView tv_6h = (TextView) popView.findViewById(R.id.tv_6h);
		TextView tv_12h = (TextView) popView.findViewById(R.id.tv_12h);
		tv_off.setOnClickListener(this);
		tv_1h.setOnClickListener(this);
		tv_3h.setOnClickListener(this);
		tv_6h.setOnClickListener(this);
		tv_12h.setOnClickListener(this);
		popupWindow = new PopupWindow(this);
		popupWindow.setContentView(popView);
		popupWindow.setWidth(-2);
		popupWindow.setHeight(-2);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true);
		popupWindow.showAsDropDown(v, 0, 0);
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				popupWindow = null;
				popView = null;
			}
		});
	}

	/**
	 * ����֪ͨ����ʾ��pop
	 * 
	 * @param v
	 */
	public void showPop4SetNotification(View v) {
		popView = (LinearLayout) View.inflate(this,
				R.layout.pop4set_notification, null);
		TextView tv_notifiation_off = (TextView) popView
				.findViewById(R.id.tv_notifiation_off);
		TextView tv_notifiation_on = (TextView) popView
				.findViewById(R.id.tv_notifiation_on);
		tv_notifiation_off.setOnClickListener(this);
		tv_notifiation_on.setOnClickListener(this);
		popupWindow = new PopupWindow(this);
		popupWindow.setContentView(popView);
		popupWindow.setWidth(-2);
		popupWindow.setHeight(-2);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true);
		popupWindow.showAsDropDown(v, 0, 0);
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				popupWindow = null;
				popView = null;
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
		unregisterReceiver(autoUpdateReciever);
	}

	/**
	 * �Զ���λ���У����أ�
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
			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS��λ���
				sb.append(location.getCity() + "," + location.getDistrict());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// ���綨λ���
				sb.append(location.getCity() + "," + location.getDistrict());
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// ���߶�λ���
				sb.append(location.getCity());
			}
			Message msg = handler.obtainMessage();
			msg.obj = sb.toString();
			handler.sendMessage(msg);
			mLocationClient.stop();
		}

	}

	/**
	 * ����ѡ�������
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

	/**
	 * ���ո��¹㲥
	 * 
	 * @author Administrator
	 *
	 */
	class AutoUpdateReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			upDateWeatherInfoFromSD(city, county);
			Toast.makeText(MainActivity.this, "�Ѹ���", 0).show();
		}

	}

}
