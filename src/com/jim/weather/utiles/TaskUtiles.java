package com.jim.weather.utiles;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class TaskUtiles {
	public static boolean isServiceRunning(Context context, String serviceName) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = manager
				.getRunningServices(100);
		for (RunningServiceInfo runningServiceInfo : runningServices) {
			if (runningServiceInfo.service.getClassName().equals(serviceName)) {
				return true;
			}
		}
		return false;
	}
}
