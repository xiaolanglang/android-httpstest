package com.okhttptest.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.example.httpstest.R;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.http.common.HttpUtil;
import com.http.common.callback.FileCallback;
import com.http.common.callback.ImageCallback;
import com.http.common.callback.ResultCallBack;
import com.http.common.callback.StringCallback;
import com.http.common.cookie.ClearableCookieJar;
import com.okhttptest.entity.User;
import com.okhttptest.util.ByteUtil;
import com.okhttptest.util.DigestsUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.CookieJar;
import okhttp3.Response;

public class MainActivity extends Activity {

	private String hosts = "https://192.168.0.102";
	private String host = "http://192.168.0.102:8080";
	private String url = host + "/mvc/admin/home/search";
	private String uploadUrl = host + "/mvc/admin/home/upload";
	private String imageUrl = "http://pic15.nipic.com/20110616/7034149_164010530126_2.jpg";
	private String fileUrl = "http://sw.bos.baidu.com/sw-search-sp/software/19de58890ffb8/QQ_8.6.18804.0_setup.exe";
	private String loginUrl = "http://m.cgotravel.com/travel/login";
	private String loginOutUrl = "http://m.cgotravel.com/travel/loginout";
	private String testLoginUrl = "http://m.cgotravel.com/travel/mine";
	private String deviceLogin = "http://pre.mcomm.com.cn/user/device/deviceLogin";
	private Map<String, Object> params = new HashMap<>();

	private ProgressBar mProgressBar;
	private ImageView mImageView;
	private TextView mTv;
	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTv = (TextView) findViewById(R.id.id_textview);
		mImageView = (ImageView) findViewById(R.id.id_imageview);
		mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
		mProgressBar.setMax(10);

		params.put("username", "张三");
		params.put("id", "id1234dfe");
		params.put("age", 123);
	}

	public void getString(View view) {
		HttpUtil.newConnect().get(url).setParams(params).execute(callBack);
	}

	public void postString(View view) {
		HttpUtil.newConnect().get(url).setParams(params).execute(callBack);
	}

	public void getUser(View view) {
		HttpUtil.newConnect().get(url).setParams(params).execute(new ResultCallBack() {

			@Override
			public void preExecute() {
				runOnUiThread(new Runnable() {
					public void run() {
						clearUi();
					}
				});
			}

			@Override
			public void onFinally() {
				runOnUiThread(new Runnable() {
					public void run() {
						mTv.append("\n请求结束");
					}
				});
			}

			@Override
			public void onFailed(final int code, final String message) {
				runOnUiThread(new Runnable() {
					public void run() {
						mTv.setText("code：" + code + "  msg：" + message);
					}
				});
			}

			@Override
			public void onSuccess(Response response) {
				Gson gson = new Gson();
				User userJson = null;
				try {
					userJson = gson.fromJson(response.body().string(), User.class);
				} catch (JsonSyntaxException | IOException e) {
					e.printStackTrace();
				}
				final User user = userJson;
				runOnUiThread(new Runnable() {
					public void run() {
						mTv.setText("");
						mTv.append("id：" + user.getId() + "\n");
						mTv.append("username：" + user.getUsername() + "\n");
						mTv.append("age：" + user.getAge());
					}
				});
			}

			@Override
			public void onRequestProgress(long bytesWritten, long contentLength) {

			}
		});
	}

	public void getHttpsHtml(View view) {
		HttpUtil.newConnect().get(hosts).setParams(params).execute(callBack);
	}

	public void getImage(View view) {
		HttpUtil.newConnect().get(imageUrl).execute(imageCallback);
	}

	private Object uploadTag = new Object();

	public void uploadFile(View view) {
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ "QQ_8.6.18804.0_setup.exe";
		File file = new File(filePath);
		if (!file.exists()) {
			Toast.makeText(this, "文件并存在,请先下载", Toast.LENGTH_SHORT).show();
		}
		HttpUtil.newConnect().post(uploadUrl).setParams(params).setTag(uploadTag).addFile(file).execute(callBack);

	}

	public void cancleUploadFile(View view) {
		HttpUtil.cancelTag(uploadTag);
	}

	public void multiFileUpload(View view) {
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ "QQ_8.6.18804.0_setup.exe";
		String filePath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ "QQ_8.6.18804.0_setup2.exe";
		File file = new File(filePath);
		File file2 = new File(filePath2);
		if (!file.exists() || !file2.exists()) {
			Toast.makeText(this, "文件并存在,请先下载", Toast.LENGTH_SHORT).show();
		}
		HttpUtil.newConnect().post(uploadUrl).setParams(params).addFile(file2).addFile(file).execute(callBack);
	}

	private Object tag = new Object();

	public void downloadFile(View view) {
		HttpUtil.newConnect().get(fileUrl).setParams(params).setTag(tag).execute(fileCallback);
	}

	public void cancleDownloadFile(View view) {
		HttpUtil.cancelTag(tag);
	}

	public void login(View view) {
		Map<String, Object> params = new HashMap<>();
		params.put("username", "test");
		params.put("password", "qwertyuio");
		analysisIP(loginUrl);
		HttpUtil.newConnect().post(loginUrl).setParams(params).execute(callBack);
	}

	public void loginOut(View view) {
		analysisIP(loginOutUrl);
		HttpUtil.newConnect().get(loginOutUrl).execute(callBack);
	}

	public void testLogin(View view) {
		analysisIP(testLoginUrl);
		HttpUtil.newConnect().get(testLoginUrl).execute(callBack);
	}

	public void clearCookie(View view) {
		CookieJar cookieJar = HttpUtil.getClient().cookieJar();
		if (cookieJar instanceof ClearableCookieJar) {
			((ClearableCookieJar) cookieJar).clear();
		}
	}

	public void deviceLogin(View view) throws UnsupportedEncodingException {
		Map<String, Object> params = new HashMap<>();
		String json = "{\"deviceId\":\"1111111134121111\",\"type\":\"0\",\"client\":\"pc\"}";
		Base64 base64 = new Base64();
		byte[] as = base64.encode(json.getBytes("UTF-8"));
		String base64Str = new String(as, "UTF-8");
		long timestamp = System.currentTimeMillis() / 1000l;
		as = DigestsUtil.md5((json + timestamp + "7B8AE5032B6A48DFAE1B0ED0C6E01D51").getBytes("UTF-8"));
		params.put("encodeParams", base64Str);
		params.put("sign", ByteUtil.toString(as).replace(" ", ""));
		params.put("accountId", "account001");
		params.put("timestamp", timestamp);
		analysisIP(deviceLogin);
		HttpUtil.newConnect().post(deviceLogin).setParams(params).execute(callBack);
	}

	private void clearUi() {
		mTv.setText("");
		mImageView.setImageBitmap(null);
		mProgressBar.setProgress(0);
		mProgressBar.setMax(10);

	}

	/** string结果回调 */
	private StringCallback callBack = new StringCallback() {

		@Override
		public void preExecute() {
			runOnUiThread(new Runnable() {
				public void run() {
					clearUi();
				}
			});
		}

		@Override
		public void onFinally() {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.append("\n请求结束");
				}
			});
		}

		@Override
		public void onFailed(final int code, final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.setText("code：" + code + "  msg：" + message);
				}
			});
		}

		@Override
		protected void success(final String result) {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.setText(result);
				}
			});
		}

		@Override
		public void onRequestProgress(final long bytesWritten, final long contentLength) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mProgressBar.getMax() != contentLength) {
						mProgressBar.setMax((int) contentLength);
					}
					mProgressBar.setProgress((int) bytesWritten);
				}
			});
		}

	};

	/** 下载文件回调 */
	private FileCallback fileCallback = new FileCallback(Environment.getExternalStorageDirectory().getAbsolutePath(),
			"QQ_8.6.18804.0_setup.exe") {

		@Override
		public void preExecute() {
			runOnUiThread(new Runnable() {
				public void run() {
					clearUi();
				}
			});
		}

		@Override
		public void onFailed(final int code, final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.setText("code：" + code + "  msg：" + message);
				}
			});
		}

		@Override
		public void onFinally() {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.append("\n请求结束");
				}
			});
		}

		protected void onProgress(final float f, final long total) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mProgressBar.getMax() != total) {
						mProgressBar.setMax((int) total);
					}
					mProgressBar.setProgress((int) f);
				}
			});
		}

		protected void downloadSuccess(final File file) {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.setText("文件路径：" + file.getAbsolutePath());
				}
			});
		}

		@Override
		public void onRequestProgress(long bytesWritten, long contentLength) {
			// TODO Auto-generated method stub

		}

	};

	/** 下载图片 */
	private ImageCallback imageCallback = new ImageCallback() {

		@Override
		public void preExecute() {
			runOnUiThread(new Runnable() {
				public void run() {
					clearUi();
				}
			});
		}

		@Override
		public void onFinally() {

		}

		@Override
		public void onFailed(final int code, final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					mTv.setText("code：" + code + "  msg：" + message);
				}
			});
		}

		@Override
		protected void success(final Bitmap bitmap) {
			runOnUiThread(new Runnable() {
				public void run() {
					mImageView.setImageBitmap(bitmap);
				}
			});
		}

		@Override
		public void onRequestProgress(long bytesWritten, long contentLength) {
			// TODO Auto-generated method stub

		}
	};

	private void analysisIP(final String url) {
		new Thread(new Runnable() {
			public void run() {
				InetAddress id = null;
				String str = null;
				try {
					id = InetAddress.getByName(url.replace("http://", "").split("/")[0]);
				} catch (UnknownHostException e) {
					str = e.getMessage();
				}
				if (str == null) {
					str = id.getHostAddress();
				}
				final String res = str;
				runOnUiThread(new Runnable() {
					public void run() {
						if (toast != null) {
							toast.cancel();
						}
						toast = Toast.makeText(MainActivity.this, "访问IP：" + res, Toast.LENGTH_SHORT);
						toast.show();
					}
				});
			}
		}).start();
	}
}
