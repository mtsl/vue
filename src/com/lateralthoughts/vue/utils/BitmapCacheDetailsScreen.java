package com.lateralthoughts.vue.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCacheDetailsScreen extends LruCache<String, Bitmap>
        implements ImageCache {
    
    private static BitmapCacheDetailsScreen mBitmapLruCache;
    private static int maxSize = 10;
    
    private BitmapCacheDetailsScreen(int maxSize) {
        super(maxSize);
    }
    
    public static BitmapCacheDetailsScreen getInstance(Context context) {
        if (mBitmapLruCache == null) {
            int memClass = ((ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            int cacheSize = (1024 * 1024) * memClass / maxSize;
            mBitmapLruCache = new BitmapCacheDetailsScreen(cacheSize);
        }
        return mBitmapLruCache;
        
    }
    
    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
            Bitmap newValue) {
        if (!oldValue.isRecycled()) {
            oldValue.recycle(); //
            oldValue = null;
        }
    }
    
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
    }
    
    @Override
    public Bitmap getBitmap(String key) {
        return this.get(key);
    }
    
    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        this.put(key, bitmap);
        
    }
    
}
