package com.http.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.http.common.HttpRequest;
import com.http.common.cookie.ClearableCookieJar;
import com.http.common.cookie.PersistentCookieJar;
import com.http.common.cookie.cache.SetCookieCache;
import com.http.common.cookie.persistence.SharedPrefsCookiePersistor;
import com.http.common.interceptor.LoggerInterceptor;

import android.content.Context;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class HttpUtil {

	private static Context context;
	private static OkHttpClient client;
	private static final Object object = new Object();
	private static final String KEY_STORE_TYPE_P12 = "PKCS12";// 证书类型

	public static void init(Context context) {
		HttpUtil.context = context;
	}

	private HttpUtil() {
	}

	public static OkHttpClient getClient() {
		if (client == null) {
			synchronized (object) {
				if (client == null) {
					try {
						client = createClient();
					} catch (GeneralSecurityException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return client;
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

	private static OkHttpClient createClient() throws GeneralSecurityException, IOException {
		Builder builder = new OkHttpClient.Builder();

		setSocketFactory2(builder);
		// setSocketFactory(serverCert, builder);

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
	 * 添加证书,实现客户端和服务器的双向验证
	 * 
	 * @param certificates
	 * @param clientCert
	 * @param builder
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * 
	 */

	private static void setSocketFactory2(Builder builder) throws GeneralSecurityException, IOException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

		char[] password = "ufueo*&2434%jfiah1234".toCharArray(); // 任意密码
		char[] password2 = "123456".toCharArray();// 这密钥就是在生成PKCS12的时候设置的密码
		KeyStore serverkeyStore = newEmptyKeyStore(password);
		KeyStore clientkeyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);

		serverkeyStore.setCertificateEntry("1",
				certificateFactory.generateCertificate(context.getAssets().open("certs/cacert.pem")));

		clientkeyStore.load(context.getAssets().open("certs/server.p12"), password2);

		// Use it to build an X509 trust manager.
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(clientkeyStore, password);
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(serverkeyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
			throw new IllegalStateException("Unexpected default X509 trust managers:" + Arrays.toString(trustManagers));
		}

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

		builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
	}

	/**
	 * 添加证书,实现客户端和服务器的单向验证
	 * 
	 * @param certificates
	 * @param builder
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * 
	 */
	private static void setSocketFactory(List<InputStream> certificates, Builder builder)
			throws GeneralSecurityException, IOException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		char[] password = "ufueo*&2434%jfiah1234".toCharArray(); // 任意密码
		KeyStore keyStore = newEmptyKeyStore(password);
		for (int i = 0, size = certificates.size(); i < size;) {
			InputStream certificate = certificates.get(i);
			String certificateAlias = Integer.toString(i++);
			keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
			if (certificate != null) {
				certificate.close();
			}
		}

		// Use it to build an X509 trust manager.
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password);
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
			throw new IllegalStateException("Unexpected default X509 trust managers:" + Arrays.toString(trustManagers));
		}

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustManagers, null);

		builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
	}

	private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream in = null; // By convention, 'null' creates an empty key
									// store.
			keyStore.load(in, password);
			return keyStore;
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

}
