package com.okhttptest.app;

import com.http.common.util.HttpUtil;

import android.app.Application;

public class MyApp extends Application {
	@Override
	public void onCreate() {
		HttpUtil.init(getApplicationContext());
	}
}
