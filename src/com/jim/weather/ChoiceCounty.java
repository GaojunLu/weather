package com.jim.weather;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.jim.weather.utiles.DbUtiles;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ChoiceCounty extends Activity implements OnClickListener {
	private TextView tv_province;
	private TextView tv_city;
	private TextView tv_county;
	private TextView currentView;
	private PopupWindow popupWindow;
	private ListView listView;
	private MyAdapter adapter;
	private List<String> s = new ArrayList<String>();
	private String TAG = "ChoiceCounty====";
	private TextView tv_title;
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			String resault = (String) msg.obj;
//			Toast.makeText(ChoiceCounty.this, resault, 0).show();
			if (!TextUtils.isEmpty(resault)||!resault.equals("null")) {
				String[] s = resault.split(",");
				Intent data = new Intent();
				data.putExtra("county", s[1]);
				data.putExtra("city", s[0]);
				setResult(10086, data);
				finish();
			}else{
				Toast.makeText(ChoiceCounty.this, "自动获取位置失败，请手动选择", 0).show();
			}
		};
	};
	private LocationClient mLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choicecounty);
		initView();
		initData();
		initListener();
	}

	private void initData() {
		// TODO Auto-generated method stub
		tv_title.setText("选择城市");
	}

	private void initListener() {
		// TODO Auto-generated method stub
		tv_province.setOnClickListener(this);
		tv_city.setOnClickListener(this);
		tv_county.setOnClickListener(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				currentView.setText(s.get(position));
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				popupWindow = null;
			}
		});
	}

	private void initView() {
		tv_province = (TextView) findViewById(R.id.tv_province);
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_county = (TextView) findViewById(R.id.tv_county);
		tv_title = (TextView) findViewById(R.id.tv_title);
		listView = new ListView(this);
		adapter = new MyAdapter();
	}

	public void ok(View v) {
		String resault = (String) tv_county.getText();
		if (!TextUtils.isEmpty(resault)) {
			Intent data = new Intent();
			data.putExtra("county", resault);
			setResult(10086, data);
			finish();
		} else {
			Toast.makeText(this, "请选择县", 0).show();
		}
	}

	public void auto(View view) {
		getCity();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_province:
			showPop(tv_province);
			break;
		case R.id.tv_city:
			showPop(tv_city);
			break;
		case R.id.tv_county:
			showPop(tv_county);
			break;
		}
	}

	public void showPop(TextView tv) {
		currentView = tv;
		s.clear();
		switch (tv.getId()) {
		case R.id.tv_province:
			s.addAll(DbUtiles.getAllProvince(this));
			break;
		case R.id.tv_city:
			String province = tv_province.getText().toString();
			if (TextUtils.isEmpty(province)) {
				Toast.makeText(ChoiceCounty.this, "请选择省", 0).show();
				return;
			}
			s.addAll(DbUtiles.getAllCity(this, province));
			break;
		case R.id.tv_county:
			String city = tv_city.getText().toString();
			if (TextUtils.isEmpty(city)) {
				Toast.makeText(ChoiceCounty.this, "请选择市", 0).show();
				return;
			}
			s.addAll(DbUtiles.getAllCounty(this, city));
			break;
		}
		listView.setAdapter(adapter);
		popupWindow = new PopupWindow(listView, tv.getMeasuredWidth(), -2, true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		popupWindow.showAsDropDown(tv, 0, 0);
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return s.size();
		}

		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return s.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView view;
			if (convertView != null) {
				view = (TextView) convertView;
			} else {
				view = new TextView(ChoiceCounty.this);
			}
			view.setText(s.get(position));
			return view;
		}

	}
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
			 if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
//	                sb.append("\nspeed : ");
//	                sb.append(location.getSpeed());// 单位：公里每小时
//	                sb.append("\nsatellite : ");
//	                sb.append(location.getSatelliteNumber());
//	                sb.append("\nheight : ");
//	                sb.append(location.getAltitude());// 单位：米
//	                sb.append("\ndirection : ");
//	                sb.append(location.getDirection());// 单位度
//	                sb.append("\naddr : ");
//	                sb.append(location.getAddrStr());
//	                sb.append("\ndescribe : ");
//	                sb.append("gps定位成功");
				 sb.append(location.getCity()+","+location.getDistrict());
	 
	            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
//	                sb.append("\naddr : ");
//	                sb.append(location.getAddrStr());
//	                //运营商信息
//	                sb.append("\noperationers : ");
//	                sb.append(location.getOperators());
//	                sb.append("\ndescribe : ");
//	                sb.append("网络定位成功");
	            	sb.append(location.getCity()+","+location.getDistrict());
	            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//	                sb.append("\ndescribe : ");
//	                sb.append("离线定位成功，离线定位结果也是有效的");
	            	sb.append(location.getCity());
	            } else if (location.getLocType() == BDLocation.TypeServerError) {
//	                sb.append("\ndescribe : ");
//	                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
	            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//	                sb.append("\ndescribe : ");
//	                sb.append("网络不同导致定位失败，请检查网络是否通畅");
	            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//	                sb.append("\ndescribe : ");
//	                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
	            }
			Message msg = handler.obtainMessage();
			msg.obj = sb.toString();
			handler.sendMessage(msg);
			mLocationClient.stop();
		}

	}
}
