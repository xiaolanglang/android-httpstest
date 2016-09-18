package com.http.common.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.Response;

public abstract class ImageCallback implements ResultCallBack {

	@Override
	public void onSuccess(Response response) {
		success(BitmapFactory.decodeStream(response.body().byteStream()));
	}

	protected abstract void success(Bitmap bitmap);

}
