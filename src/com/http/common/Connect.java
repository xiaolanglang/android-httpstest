package com.http.common;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public interface Connect {
	/**
	 * 普通get请求
	 * 
	 * @param callback
	 * @param url
	 * @param param
	 * @param type
	 * @return
	 */
	<T> HttpTask<T> get(ServiceListener<T> callback, String url, Map<String, Object> param, TypeToken<T> type);

	/**
	 * 普通post请求
	 * 
	 * @param callback
	 * @param url
	 * @param param
	 * @param type
	 * @return
	 */
	<T> HttpTask<T> post(ServiceListener<T> callback, String url, Map<String, Object> param, TypeToken<T> type);

	/**
	 * 带文件的post请求
	 * 
	 * @param callback
	 * @param url
	 * @param param
	 * @param files
	 * @param type
	 * @return
	 */
	public <T> HttpTask<T> post(ServiceListener<T> callback, String url, Map<String, Object> param, List<File> files,
			TypeToken<T> type);
}
