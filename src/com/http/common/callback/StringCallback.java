package com.http.common.callback;

import java.io.IOException;

import okhttp3.Response;

public abstract class StringCallback implements ResultCallBack {

	@Override
	public void onSuccess(Response response) {
		try {
			success(response.body().string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void success(String result);

}
