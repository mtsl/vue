package com.lateralthoughts.vue.utils;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageCache {
	private static BitmapLruCache mBitmapLruCache;
	private static int maxSize = 15;
    private BitmapLruCache(int maxSize) {
        super(maxSize);
    }
   public static BitmapLruCache getInstance(Context context) {
	   if(mBitmapLruCache == null){
		   int memClass  = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		   int cacheSize = (1024 * 1024) * memClass / maxSize;
		   mBitmapLruCache = new BitmapLruCache(cacheSize); 
	   }
	return mBitmapLruCache;

   }
    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
    }

 /*   @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        if (!oldValue.isRecycled()) {
            oldValue.recycle();
            oldValue = null;
        }
    }*/

    @Override
    public Bitmap getBitmap(String key) {
        return this.get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        this.put(key, bitmap);
    }
}
