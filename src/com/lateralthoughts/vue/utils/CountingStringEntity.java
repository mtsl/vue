package com.lateralthoughts.vue.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.http.entity.StringEntity;

public class CountingStringEntity extends StringEntity {

	public CountingStringEntity(String s) throws UnsupportedEncodingException {
		super(s);
		length = s.length();
	}

	private UploadListener listener;
	private long length;

	public interface UploadListener {
		public void onChange(int percent);
	}

	public void setUploadListener(UploadListener listener) {
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream));
	}

	class CountingOutputStream extends OutputStream {
		private long counter = 0l;
		private OutputStream outputStream;

		public CountingOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public void write(int oneByte) throws IOException {
			this.outputStream.write(oneByte);
			counter++;
			if (listener != null) {
				int percent = (int) ((counter * 100) / length);
				listener.onChange(percent);
			}
		}
	}
}
