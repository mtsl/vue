package com.lateralthoughts.vue.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

public class CountingStringEntity extends StringEntity {
    
    public CountingStringEntity(String s) throws UnsupportedEncodingException {
        super(s);
        mLength = s.length();
    }
    
    private UploadListener mListener;
    private long mLength;
    
    public interface UploadListener {
        public void onChange(int percent);
    }
    
    public void setUploadListener(UploadListener listener) {
        this.mListener = listener;
    }
    
    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream));
    }
    
    class CountingOutputStream extends OutputStream {
        private long mCounter = 0l;
        private OutputStream mOutputStream;
        
        public CountingOutputStream(OutputStream outputStream) {
            this.mOutputStream = outputStream;
        }
        
        @Override
        public void write(int oneByte) throws IOException {
            this.mOutputStream.write(oneByte);
            mCounter++;
            if (mListener != null) {
                int percent = (int) ((mCounter * 100) / mLength);
                mListener.onChange(percent);
            }
        }
    }
}
