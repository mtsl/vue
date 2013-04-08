package com.lateralthoughts.vue.utils;

//android imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.lateralthoughts.vue.VueApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

public class BitmapLoaderUtils {

	private Context mContext;
	private static BitmapLoaderUtils sBitmapLoaderUtils;
    private FileCache mFileCache;
    private VueMemoryCache<Bitmap> mAisleImagesCache;
    private int mScreenWidth;
    
    private final boolean DEBUG = false;
    
	private BitmapLoaderUtils(Context context){
		mContext = context;
        mFileCache = new FileCache(mContext);
        mAisleImagesCache = VueApplication.getInstance().getAisleImagesMemCache();
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
	}
	
	public static BitmapLoaderUtils getInstance(Context context){
		if(null == sBitmapLoaderUtils){
			sBitmapLoaderUtils = new BitmapLoaderUtils(context);
		}
		return sBitmapLoaderUtils;
	}
    
    /*
     * This function is strictly for use by internal APIs. Not that we have anything external but
     * there is some trickery here! The getBitmap function cannot be invoked from the UI thread.
     * Having to deal with complexity of when & how to call this API is too much for those who
     * just want to have the bitmap. This is a utility function and is public because it is to 
     * be shared by other components in the internal implementation.   
     */
    public Bitmap getBitmap(String url, boolean cacheBitmap) 
    {
        File f = mFileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            if(cacheBitmap)
            	mAisleImagesCache.put(url, bitmap);
            
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
        	   mAisleImagesCache.clear();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1=new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = mScreenWidth/2;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp < REQUIRED_SIZE || height_tmp < REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            if(DEBUG) Log.d("Jaws","using inSampleSizeScale = " + scale + " original width = " + o.outWidth + "screen width = " + mScreenWidth);
            FileInputStream stream2=new FileInputStream(f);
            Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Bitmap getCachedBitmap(String url){
    	return mAisleImagesCache.get(url);    	
    }

    public void clearCache() {
    	mAisleImagesCache.clear();
    	mFileCache.clear();
    }

}
