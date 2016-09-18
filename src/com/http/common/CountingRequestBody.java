package com.http.common;

import java.io.IOException;

import com.http.common.callback.ResultCallBack;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Decorates an OkHttp request body to count the number of bytes written when
 * writing it. Can decorate any request body, but is most useful for tracking
 * the upload progress of large multipart requests.
 *
 * @author Leo Nikkilä
 */
public class CountingRequestBody extends RequestBody {

	protected RequestBody requestBody;
	protected ResultCallBack listener;

	protected CountingSink countingSink;

	public CountingRequestBody(RequestBody requestBody, ResultCallBack listener) {
		this.requestBody = requestBody;
		this.listener = listener;
	}

	@Override
	public MediaType contentType() {
		return requestBody.contentType();
	}

	@Override
	public long contentLength() {
		try {
			return requestBody.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {

		countingSink = new CountingSink(sink);
		BufferedSink bufferedSink = Okio.buffer(countingSink);

		requestBody.writeTo(bufferedSink);

		bufferedSink.flush();
	}

	private final class CountingSink extends ForwardingSink {

		private long bytesWritten = 0;

		public CountingSink(Sink delegate) {
			super(delegate);
		}

		@Override
		public void write(Buffer source, long byteCount) throws IOException {
			super.write(source, byteCount);
			bytesWritten += byteCount;
			listener.onRequestProgress(bytesWritten, contentLength());
		}

	}

}