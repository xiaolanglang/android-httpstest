package com.http.common.callback;

import okhttp3.Response;

public interface ResultCallBack {
	void preExecute();

	void onSuccess(Response response);

	void onFailed(int code, String message);

	void onFinally();

	void onRequestProgress(long bytesWritten, long contentLength);
}
