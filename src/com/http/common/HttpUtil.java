package com.http.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.http.common.cookie.ClearableCookieJar;
import com.http.common.cookie.PersistentCookieJar;
import com.http.common.cookie.cache.SetCookieCache;
import com.http.common.cookie.persistence.SharedPrefsCookiePersistor;
import com.http.common.interceptor.LoggerInterceptor;
import com.http.common.util.CertificateUtil;

import android.content.Context;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class HttpUtil {

	private static Context context;
	private static OkHttpClient client;

	public static void init(Context context) {
		HttpUtil.context = context;
	}

	private HttpUtil() {
	}

	public static HttpRequest newConnect() {
		return new HttpRequest();
	}

	public static void cancelTag(Object tag) {
		for (Call call : client.dispatcher().queuedCalls()) {
			if (tag.equals(call.request().tag())) {
				call.cancel();
			}
		}
		for (Call call : client.dispatcher().runningCalls()) {
			if (tag.equals(call.request().tag())) {
				call.cancel();
			}
		}
	}

	public static OkHttpClient getClient() {
		if (client == null) {
			synchronized (HttpUtil.class) {
				if (client == null) {
					client = createClient();
				}
			}
		}
		return client;
	}

	private static OkHttpClient createClient() {
		// 添加证书
		List<InputStream> certificates = new ArrayList<>();
		List<byte[]> certs_data = CertificateUtil.getCertificatesData();
		// 将字节数组转为数组输入流
		if (certs_data != null && !certs_data.isEmpty()) {
			for (byte[] bytes : certs_data) {
				certificates.add(new ByteArrayInputStream(bytes));
			}
		}
		SSLSocketFactory sslSocketFactory = getSocketFactory(certificates);

		okhttp3.OkHttpClient.Builder builder = new OkHttpClient.Builder();

		// TODO 生产环境中要注释掉
		if (sslSocketFactory != null) {
			builder.sslSocketFactory(sslSocketFactory);
		}
		// 发现这段代码注释掉也能正常使用https
		// ConnectionSpec spec = new
		// ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2)
		// .cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
		// CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
		// CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
		// .build();

		// TODO生产环境中要注释掉
		builder.hostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return session.isValid();
			}
		});

		// 设置cookie
		ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(),
				new SharedPrefsCookiePersistor(context));

		OkHttpClient mOkHttpClient = builder/*
											 * .connectionSpecs(Collections.
											 * singletonList(spec))
											 */
				.connectTimeout(4, TimeUnit.SECONDS).readTimeout(4, TimeUnit.SECONDS).writeTimeout(4, TimeUnit.SECONDS)
				.cookieJar(cookieJar).addInterceptor(new LoggerInterceptor(HttpUtil.class.getSimpleName())).build();
		return mOkHttpClient;
	}

	/**
	 * 添加证书
	 * 
	 * @param certificates
	 * 
	 */

	private static SSLSocketFactory getSocketFactory(List<InputStream> certificates) {
		try {
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			try {
				for (int i = 0, size = certificates.size(); i < size;) {
					InputStream certificate = certificates.get(i);
					String certificateAlias = Integer.toString(i++);
					keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
					if (certificate != null) {
						certificate.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
			return sslContext.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}
