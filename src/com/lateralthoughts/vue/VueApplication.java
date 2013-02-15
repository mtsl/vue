package com.lateralthoughts.vue;

import android.app.Application;
import android.graphics.Bitmap;

//internal imports
import com.lateralthoughts.vue.utils.VueMemoryCache;


public class VueApplication extends Application {
	private static VueApplication sInstance;
	
	private VueMemoryCache<Bitmap> mVueAisleImagesCache;
	private VueMemoryCache<String> mVueAisleOwnerNamesCache;
	private VueMemoryCache<String> mVueAisleContextInfoCache;
	
	@Override
	public void onCreate(){
		super.onCreate();
		sInstance = this;
		
		mVueAisleImagesCache = new VueMemoryCache<Bitmap>();
		mVueAisleImagesCache.setLimit(25);
		mVueAisleOwnerNamesCache = new VueMemoryCache<String>();
		mVueAisleOwnerNamesCache.setLimit(5);
		mVueAisleContextInfoCache = new VueMemoryCache<String>();
		mVueAisleContextInfoCache.setLimit(5);
	}
	
	public static VueApplication getInstance(){
		return sInstance;
	}
	
	public VueMemoryCache<Bitmap> getAisleImagesMemCache(){
		return mVueAisleImagesCache;
	}

}
