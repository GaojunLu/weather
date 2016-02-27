package com.jim.weather.receiver;

import com.jim.weather.service.AutoUpdateService;
import com.jim.weather.utiles.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;

public class BootReceiver extends BroadcastReceiver {
	private String tag = "BootReceiver====";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Logger.i(tag, "¿ª»úÆô¶¯");
		if (context.getSharedPreferences("config", Context.MODE_PRIVATE).getInt("autoupdate", 0)!=0) {
			Intent service = new Intent(context, AutoUpdateService.class);
			context.startService(service);
		}
	}
}
