package com.lateralthoughts.vue;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

@SuppressWarnings("rawtypes")
public class BitmapCache extends LruCache implements ImageLoader.ImageCache {
    public BitmapCache(int maxSize) {
        super(maxSize);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Bitmap getBitmap(String url) {
        return (Bitmap) get(url);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
