package com.jim.weather.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.jim.weather.R;
import com.jim.weather.R.id;
import com.jim.weather.R.layout;
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
		listView.setDivider(null);
		adapter = new MyAdapter();
	}

	public void ok(View v) {
		String county = (String) tv_county.getText();
		String city = (String) tv_city.getText();
		if (!TextUtils.isEmpty(county)) {
			Intent data = new Intent();
			data.putExtra("county", county);
			data.putExtra("city", city);
			setResult(10086, data);
			finish();
		} else {
			Toast.makeText(this, "请选择县", 0).show();
		}
	}

	public void auto(View view) {
//		getCity();
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
			View view;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				holder = new ViewHolder();
				view = View.inflate(ChoiceCounty.this, R.layout.popitem, null);
				holder.tv = (TextView) view.findViewById(R.id.tv_item);
				view.setTag(holder);
			}
			holder.tv.setText(s.get(position));
			return view;
		}
		
		class ViewHolder{
			TextView tv;
		}

	}

}
