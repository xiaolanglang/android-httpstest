package com.http.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.http.common.callback.ResultCallBack;
import com.http.common.util.HttpUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
	private ConnectType connectType;
	private List<File> files;
	private Map<String, Object> params;
	private String url;
	private ResultCallBack mlistener;
	private Object tag;
	private Builder headers = new Headers.Builder();

	public HttpRequest get(String url) {
		this.connectType = ConnectType.get;
		this.url = url;
		return this;
	}

	public HttpRequest post(String url) {
		this.connectType = ConnectType.post;
		this.url = url;
		return this;
	}

	public HttpRequest setParams(Map<String, Object> params) {
		this.params = params;
		return this;
	}

	public HttpRequest addHeader(Map<String, String> header) {
		List<String> list = new ArrayList<>(header.keySet());
		for (String name : list) {
			headers.add(name, header.get(name));
		}
		return this;
	}

	public HttpRequest setTag(Object tag) {
		this.tag = tag;
		return this;
	}

	public HttpRequest addFile(File file) {
		if (files == null) {
			files = new ArrayList<>();
		}
		files.add(file);
		return this;
	}

	public HttpRequest setFiles(List<File> files) {
		this.files = files;
		return this;
	}

	public <Result> Call execute(final ResultCallBack listener) {
		this.mlistener = listener;
		Call call = null;
		call = HttpUtil.getClient().newCall(getRequest());
		mlistener.preExecute();
		call.enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					mlistener.onSuccess(response);
				} else {
					mlistener.onFailed(response.code(), response.message());
				}
				mlistener.onFinally();
			}

			@Override
			public void onFailure(Call call, IOException e) {
				mlistener.onFailed(-1, e.getMessage());
				mlistener.onFinally();
			}
		});

		return call;

	}

	private Request getForReq() {
		if (params != null && !params.isEmpty()) {
			int position = url.indexOf("?");
			Set<String> keys = params.keySet();
			StringBuilder builder = new StringBuilder();
			if (-1 == position) {
				builder.append("?");
			} else {
				builder.append("&");
			}
			for (String key : keys) {
				Object valObj = params.get(key);
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
		return new Request.Builder().url(url).headers(headers.build()).get().tag(tag).build();
	}

	private Request postForReq() {
		RequestBody requestBody = null;
		if (files != null && files.size() > 0) {
			okhttp3.MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			if (params != null) {
				Set<String> keys = params.keySet();
				for (String key : keys) {
					builder.addFormDataPart(key, params.get(key).toString());
				}
			}
			for (File file : files) {
				builder.addFormDataPart(file.getName(), file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
			}
			requestBody = builder.build();
		} else {
			okhttp3.FormBody.Builder builder = new FormBody.Builder(); // 表单
			if (params != null) {
				Set<String> keys = params.keySet();
				for (String key : keys) {
					builder.add(key, params.get(key).toString());
				}
			}
			requestBody = builder.build();
		}
		return new Request.Builder().url(url).headers(headers.build())
				.post(new CountingRequestBody(requestBody, mlistener)).tag(tag).build();
	}

	private Request getRequest() {
		if (connectType == ConnectType.get) {
			return getForReq();
		} else {
			return postForReq();
		}
	}

}
