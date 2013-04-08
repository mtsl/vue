package com.lateralthoughts.vue;

import android.app.Application;
import android.graphics.Bitmap;

//internal imports
import com.lateralthoughts.vue.utils.VueMemoryCache;

//import crittercism sdk
import com.crittercism.app.Crittercism;
import org.json.JSONException;
import org.json.JSONObject;

public class VueApplication extends Application {
	private static VueApplication sInstance;
	
	private VueMemoryCache<Bitmap> mVueAisleImagesCache;
	private VueMemoryCache<String> mVueAisleOwnerNamesCache;
	private VueMemoryCache<String> mVueAisleContextInfoCache;
	private static final String CRITTERCISM_APP_ID = "5153c41e558d6a2403000009";
	
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
		ScaledImageViewFactory.getInstance(this);
		
		// create the JSONObject.  (Do not forget to import org.json.JSONObject!)
		JSONObject crittercismConfig = new JSONObject();
		try
		{
		    crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
		}
		catch (JSONException je){}

		//Crittercism.init(getApplicationContext(), CRITTERCISM_APP_ID, crittercismConfig);
	}
	
	public static VueApplication getInstance(){
		return sInstance;
	}
	
	public VueMemoryCache<Bitmap> getAisleImagesMemCache(){
		return mVueAisleImagesCache;
	}

}
