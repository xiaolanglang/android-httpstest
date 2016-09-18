package com.http.common.callback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

public abstract class FileCallback implements ResultCallBack {

	/**
	 * 目标文件存储的文件夹路径
	 */
	private String destFileDir;
	/**
	 * 目标文件存储的文件名
	 */
	private String destFileName;

	public FileCallback(String destFileDir, String destFileName) {
		this.destFileDir = destFileDir;
		this.destFileName = destFileName;
	}

	@Override
	public void onSuccess(Response response) {
		try {
			downloadSuccess(saveFile(response));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File saveFile(Response response) throws IOException {
		InputStream is = null;
		byte[] buf = new byte[2048];
		int len = 0;
		FileOutputStream fos = null;
		try {
			is = response.body().byteStream();
			final long total = response.body().contentLength();

			long sum = 0;

			File dir = new File(destFileDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(dir, destFileName);
			fos = new FileOutputStream(file);
			while ((len = is.read(buf)) != -1) {
				sum += len;
				fos.write(buf, 0, len);
				final long finalSum = sum;
				onProgress(finalSum, total);
			}
			fos.flush();

			return file;

		} finally {
			try {
				response.body().close();
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	protected abstract void onProgress(float f, long total);

	protected abstract void downloadSuccess(File file);
}
