package com.httpstest.ui;

import com.example.httpstest.R;
import com.google.gson.reflect.TypeToken;
import com.http.common.Connect;
import com.http.common.HttpsConnect;
import com.http.common.ServiceListener;
import com.httpstest.entity.HttpsEntity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Connect connect = HttpsConnect.getInstance();
	private Button req_httpsBtn;
	private String url = "https://192.168.1.101";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		req_httpsBtn = (Button) findViewById(R.id.req_https);
		req_httpsBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.req_https:
			connect.get(new ServiceListener<HttpsEntity>() {

				@Override
				public void preExecute() {

				}

				@Override
				public void onSuccess(HttpsEntity result) {

				}

				@Override
				public void onFailed(int code, String message) {
					Toast.makeText(MainActivity.this, "error code：" + code + "\n msg：" + message, Toast.LENGTH_SHORT)
							.show();
				}

				@Override
				public void onFinally() {

				}
			}, url, null, new TypeToken<HttpsEntity>() {
			});
			break;

		}
	}

}
