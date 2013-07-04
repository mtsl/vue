package com.lateralthoughts.vue.utils;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utils {
	private static final String CURRENT_FONT_SIZE = "currentFontSize";
	public static final String NETWORK_SETTINGS = "networkSettings";
	public static final int LARGE_TEXT_SIZE = 22;
	public static final int MEDIUM_TEXT_SIZE = 18;
	public static final int SMALL_TEXT_SIZE = 14;
	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    public static String addImageInfo(String url,int width,int height) {
		if(url.contains("width")&& url.contains("height")) {
			return url;
		}
    	StringBuilder modifiedUrl = new StringBuilder(url);
    	modifiedUrl.append("?width="+width);
    	modifiedUrl.append("&height="+height);
    	return modifiedUrl.toString();
    	
    }
    public static Bitmap getScaledBitMap(Bitmap bitmap,int reqWidth,int reqHeight) {
    	int bitmapwidth,bitmapheight;
    	int newWidth,newHeight;
    	bitmapwidth  = bitmap.getWidth();
    	bitmapheight = bitmap.getHeight();
    	Log.i("width & height", "reqWidth old: "+bitmapwidth+" reqHeight2: "+bitmapheight);
    	if(bitmapwidth > reqWidth) {
    		 
    		newWidth = (bitmapwidth * reqWidth)/bitmapwidth;
    		newHeight = (bitmapheight * reqWidth)/bitmapwidth;
    	} else {
    		newWidth = reqWidth;
    		newHeight = reqHeight;
    	}
    	if(newHeight > reqHeight) {
    		newHeight = (bitmapheight * reqHeight)/bitmapheight;
    		newWidth = (bitmapwidth * reqHeight)/bitmapheight;
    		 
    	}  
    	  int aspect = bitmapwidth / bitmapheight;
          int xnew,ynew;
          if(aspect < 1) {
                   ynew = reqHeight;
                    xnew = reqHeight * aspect;

          } else {
                  xnew = reqWidth;
                   ynew = reqWidth/aspect;
          }

		return createBitmap(bitmap,newWidth,newHeight);
    	// return bitmap;
    }

	private static  Bitmap createBitmap(Bitmap bitmap,int width,int height) {
        if(width > 0 && height > 0) {
                try {
          Bitmap bmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
          
          return bmap;
                }catch(Exception e){
                        e.printStackTrace();

                } catch(Throwable e){

                }
        }


        return bitmap;
    }

    

    /**
     * To get the CURRENT_FONT_SIZE value stored in SharedPreferences.
     * it will return default value which is MEDIUM_TEXT_SIZE (18sp)
     * if not value is stored in SharedPreferences.
     * 
     * @param Context context.
     * @return int Current font size from SharedPreferences.
     * */
	public static int getCurrentFontSize(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getInt(CURRENT_FONT_SIZE, MEDIUM_TEXT_SIZE);
    }

	/**
	 * To change the CURRENT_FONT_SIZE value stored in SharedPreferences.
	 * 
	 * @param Context context
	 * @param int newFontSize
	 * */
	public static void changeCurrentFontSize(Context context, int newFontSize) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putInt(CURRENT_FONT_SIZE, newFontSize);
		editor.commit();
	}
	
	/**
	 * To save the network settings in SharedPreferances.
	 * 
	 * @param Context context.
	 * @param boolean isWifiOnly, if isWifiOnly is true then network excess
	 *         should be made only if wifi is on. 
	 * */
	public static void saveNetworkSettings(Context context, boolean isWifiOnly) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean(NETWORK_SETTINGS, isWifiOnly);
		editor.commit();
	}
	
	/**
	 * To get the network settings  value from SharedPreferances.
	 * 
	 * @param Context context.
	 * @return boolean, if true then network excess should be made only if wifi
	 *          is on.
	 * */
	public static boolean isWifiOnly(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean(NETWORK_SETTINGS, false);
	}
}
