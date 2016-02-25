package com.jim.weather.utiles;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.content.Context;

public class LocationUtiles {

	private Context mContext;

	public void LocationUtiles(Context context) {
		this.mContext = context;
	}

	public String getCity() {
		LocationClient mLocationClient = new LocationClient(mContext);
		BDLocationListener myListener = new MyLocationListener();
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);
		mLocationClient.registerLocationListener(myListener);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		return null;
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			location.getCity();
		}

	}
}
