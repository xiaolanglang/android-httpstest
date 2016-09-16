package com.http.common;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.os.AsyncTask;
import android.util.Log;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpTask<Result> extends AsyncTask<Object, Void, Result> {

	private String TAG = getClass().getSimpleName();
	private OkHttpClient mOkHttpClient;
	private ServiceListener<Result> callback;
	private TypeToken<?> type;
	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
	private int code;
	private ConnectType connectType;

	private String errorMsg = null;

	public HttpTask(ConnectType connectType, OkHttpClient mOkHttpClient, ServiceListener<Result> callback,
			TypeToken<Result> type) {
		this.mOkHttpClient = mOkHttpClient;
		this.callback = callback;
		this.type = type;
		this.connectType = connectType;
	}

	@Override
	protected void onPreExecute() {
		callback.preExecute();
	}

	@Override
	protected Result doInBackground(Object... params) {
		okhttp3.Response response = null;
		try {
			response = mOkHttpClient
					.newCall(getRequest(params[0].toString(), (Map<String, Object>) params[1], (List<File>) params[2]))
					.execute();
			if (response.isSuccessful()) {
				String json = response.body().string();
				Log.e(TAG, "服务器返回：" + json);
				Gson gson = new Gson();
				return gson.fromJson(json, type.getType());
			}

			String a = response.body().string();
			code = response.code();
			Log.e(TAG, response.message());
			System.out.println("responsebody：" + a);

		} catch (Exception e) {
			errorMsg = e.getMessage();
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Result result) {
		if (result == null) {
			callback.onFailed(code, errorMsg);
		} else {
			callback.onSuccess(result);
		}
		callback.onFinally();
	}

	private Request getForReq(String url, Map<String, Object> param) {
		if (param != null && !param.isEmpty()) {
			int position = url.indexOf("?");
			Set<String> keys = param.keySet();
			StringBuilder builder = new StringBuilder();
			if (-1 == position) {
				builder.append("?");
			} else {
				builder.append("&");
			}
			for (String key : keys) {
				Object valObj = param.get(key);
				if (valObj == null) {
					continue;
				}
				builder.append(key);
				builder.append("=");
				builder.append(valObj.toString());
				builder.append("&");
			}
			builder.deleteCharAt(builder.length() - 1);// 删除最后一位&
			url += builder.toString();
		}
		return new Request.Builder().url(url).get().build();
	}

	private Request postForReq(String url, Map<String, Object> param, List<File> files) {
		RequestBody requestBody = null;
		if (files != null && files.size() > 0) {
			okhttp3.MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			if (param != null) {
				Set<String> keys = param.keySet();
				for (String key : keys) {
					builder.addFormDataPart(key, param.get(key).toString());
				}
			}
			for (File file : files) {
				builder.addFormDataPart(file.getName(), file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
			}
			requestBody = builder.build();
		} else {
			okhttp3.FormBody.Builder builder = new FormBody.Builder(); // 表单
			if (param != null) {
				Set<String> keys = param.keySet();
				for (String key : keys) {
					builder.add(key, param.get(key).toString());
				}
			}
			requestBody = builder.build();
		}
		return new Request.Builder().url(url).post(requestBody).build();
	}

	private Request getRequest(String url, Map<String, Object> param, List<File> files) {
		if (connectType == ConnectType.get) {
			return getForReq(url, param);
		} else {
			return postForReq(url, param, files);
		}
	}

}
