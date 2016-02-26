package com.jim.weather.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jim.weather.R;
import com.jim.weather.utiles.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {
	private static final String TAG = "Splash====";
	
	Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		//复制数据库
		copyDataBase("cityid.db");
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// 跳转
				startActivity(new Intent(Splash.this, MainActivity.class));
				finish();
			}
		}, 1000);
	}
	/**
	 * 复制数据库
	 * @param dbName
	 */
	private void copyDataBase(final String dbName) {
		new Thread() {
			public void run() {
				File file = new File(getFilesDir(), dbName);
				if (file.exists()) {
					Logger.i(TAG, "数据库" + dbName + "已经存在");
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
					e.printStackTrace();
				}
			};
		}.start();
	}
}
