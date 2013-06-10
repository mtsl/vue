package com.lateralthoughts.vue;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

//internal imports
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.VueMemoryCache;

//import crittercism sdk
import com.crittercism.app.Crittercism;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class VueApplication extends Application {
	private static VueApplication sInstance;
	
	private VueMemoryCache<Bitmap> mVueAisleImagesCache;
	private VueMemoryCache<Bitmap> mAisleContentCache;
	private VueMemoryCache<String> mVueAisleOwnerNamesCache;
	private VueMemoryCache<String> mVueAisleContextInfoCache;
	private static final String CRITTERCISM_APP_ID = "5153c41e558d6a2403000009";
	private HttpClient mHttpClient;
	private FileCache mFileCache;
	private ScaleImageView mEmptyImageView;
	
	private int mScreenHeight;
	private int mScreenWidth;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		sInstance = this;
		
		mVueAisleImagesCache = new VueMemoryCache<Bitmap>();
		mVueAisleImagesCache.setLimit(40);
		mVueAisleOwnerNamesCache = new VueMemoryCache<String>();
		mVueAisleOwnerNamesCache.setLimit(1);
		mVueAisleContextInfoCache = new VueMemoryCache<String>();
		mVueAisleContextInfoCache.setLimit(1);
		ScaledImageViewFactory.getInstance(this);
		AisleWindowContentFactory.getInstance(this);

		mAisleContentCache = new VueMemoryCache<Bitmap>();
		mAisleContentCache.setLimit(10);

		mHttpClient = new DefaultHttpClient();
		mFileCache = new FileCache(this);
		
		ContentAdapterFactory.getInstance(this);
		
		// create the JSONObject.  (Do not forget to import org.json.JSONObject!)
		JSONObject crittercismConfig = new JSONObject();
		try
		{
		    crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
		}
		catch (JSONException je){}

		mEmptyImageView = new ScaleImageView(this);
		Drawable d = getResources().getDrawable(R.drawable.aisle_content_empty);
		mEmptyImageView.setImageDrawable(d);
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		
		//R.drawable.aisle_content_empty;
		//Crittercism.init(getApplicationContext(), CRITTERCISM_APP_ID, crittercismConfig);
	}
	
	public static VueApplication getInstance(){
		return sInstance;
	}
	
	public VueMemoryCache<Bitmap> getAisleImagesMemCache(){
		return mVueAisleImagesCache;
	}
	
	public HttpClient getHttpClient(){
	    return mHttpClient;
	}
	
	public VueMemoryCache<Bitmap> getAisleContentCache(){
	    return mAisleContentCache;
	}
	
	public FileCache getFileCache(){
	    return mFileCache;   
	}

	public int getScreenHeight(){
	    return mScreenHeight;   
	}
	
    public int getScreenWidth(){
        return mScreenWidth;   
    }

}
