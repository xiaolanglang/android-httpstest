package com.okhttptest.app;

import java.io.IOException;
import java.io.InputStream;

import com.http.common.HttpUtil;
import com.http.common.util.CertificateUtil;

import android.app.Application;

public class MyApp extends Application {
	@Override
	public void onCreate() {
		// 添加https证书
		try {
			String[] certFiles = this.getAssets().list("certs");
			if (certFiles != null) {
				for (String cert : certFiles) {
					InputStream is = getAssets().open("certs/" + cert);
					CertificateUtil.addCertificate(is); // 这里将证书读取出来，放在配置中byte[]里
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		HttpUtil.init(getApplicationContext());
	}
}
