package com.http.common;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;

public class HttpConnect implements Connect {
	private static HttpConnect connect = new HttpConnect();

	private HttpConnect() {
	}

	public static HttpConnect getInstance() {
		return connect;
	}

	private OkHttpClient getClient() {
		okhttp3.OkHttpClient.Builder builder = new OkHttpClient.Builder();
		OkHttpClient mOkHttpClient = builder.connectTimeout(60, TimeUnit.SECONDS).build();
		return mOkHttpClient;
	}

	@Override
	public <T> HttpTask<T> get(ServiceListener<T> callback, String url, Map<String, Object> param, TypeToken<T> type) {
		HttpTask<T> httpTask = new HttpTask<T>(ConnectType.get, getClient(), callback, type);
		httpTask.execute(url, param, null);
		return httpTask;
	}

	@Override
	public <T> HttpTask<T> post(ServiceListener<T> callback, String url, Map<String, Object> param, TypeToken<T> type) {
		HttpTask<T> httpTask = new HttpTask<T>(ConnectType.post, getClient(), callback, type);
		httpTask.execute(url, param, null);
		return httpTask;
	}

	@Override
	public <T> HttpTask<T> post(ServiceListener<T> callback, String url, Map<String, Object> param, List<File> files,
			TypeToken<T> type) {
		HttpTask<T> httpTask = new HttpTask<T>(ConnectType.post, getClient(), callback, type);
		httpTask.execute(url, param, files);
		return httpTask;
	}

}
