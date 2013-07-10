package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.MediaColumns;
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

	private static Bitmap getResizedBitmap(Bitmap bm, int reqWidth,
			int reqHeight) {
		Log.i("bitmaptest", "bitmaptest: reqWidth: "+reqWidth);
		Log.i("bitmaptest", "bitmaptest: reqHeight: "+reqHeight);
		int originalWidth = bm.getWidth();
		int originalHeight = bm.getHeight();
		
		Log.i("bitmaptest", "bitmaptest: originalWidth: "+originalWidth);
		Log.i("bitmaptest", "bitmaptest: originalHeight: "+originalHeight);
		
		float scaleWidth;
		float scaleHeight = 0;
		float aspect = (float) originalWidth / originalHeight;
		if (originalWidth > reqWidth) {
			Log.i("bitmaptest", "bitmaptest: condition1: "+originalWidth);
			scaleWidth = reqWidth;
			scaleHeight = scaleWidth / aspect;
		} else if (originalHeight > reqHeight) {
			Log.i("bitmaptest", "bitmaptest: condition2: "+originalWidth);
			scaleHeight = reqHeight;
			scaleWidth = scaleHeight / aspect;
		} else {
			// expand the image to the screen size
			Log.i("bitmaptest", "bitmaptest: condition3: "+originalWidth);
			scaleWidth = reqWidth;
			scaleHeight = scaleWidth / aspect;
			while (scaleHeight > reqHeight) {
				// if the imagewidht is very less then image height may increase
				// to large number to make sure
				// the height is always below the reqired hieght.
				reqWidth = reqWidth - 10;
				scaleWidth = reqWidth;
				scaleHeight = scaleWidth / aspect;
			}
		}
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(scaleWidth / originalWidth, scaleHeight / originalHeight);

		// recreate the new Bitmap
  System.gc();
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, originalWidth, originalHeight,
				matrix, true);
		//bm.recycle();
		Log.i("bitmaptest", "bitmaptest: final width: "+resizedBitmap.getWidth());
		Log.i("bitmaptest", "bitmaptest: final height: "+resizedBitmap.getHeight());
		return resizedBitmap;

	}
    public static Bitmap getScaledBitMap(Bitmap bitmap,int reqWidth,int reqHeight) {
    	Log.i("bitmaptest", "bitmaptest12: coming reqWidth: "+reqWidth);
    	Log.i("bitmaptest", "bitmaptest12: coming reqHeight: "+reqHeight);
    	bitmap = getResizedBitmap(bitmap, reqWidth, reqHeight);
    	return bitmap;
    	
    	/*
    	int bitmapwidth,bitmapheight;
    	int newWidth,newHeight;
    	bitmapwidth  = bitmap.getWidth();
    	bitmapheight = bitmap.getHeight();
    	Log.i("width & height", "reqWidth1 original: "+bitmapwidth+" reqHeight2: "+bitmapheight);
    	Log.i("width & height", "reqWidth12 original: "+reqWidth+" reqHeight2: "+reqHeight);
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
    	
    	//test commit
		//return createBitmap(bitmap,newWidth,newHeight);
		
    	return createBitmap(bitmap,xnew,ynew);
		
		
//		float aspect = xgiven/ygiven
//
//				if aspect < 1
//
//				    xnew = yrequired * aspect
//				    ynew = yrequired
//				    
//				else
//
//				    ynew = xrequired/aspect
//				    xnew = xrequired

		
		
		
		
    	// return bitmap;
    */}

    private static  Bitmap createBitmap(Bitmap bitmap,int width,int height) {
    	if(width > 0 && height > 0) {
    		try { 
    	  Bitmap bmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    	  Log.i("width & height", "reqWidth1 new: "+bmap.getWidth()+" reqHeight2: "+bmap.getHeight());
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
	
	/**
	 * By Krishna.v
	 * This method is used to save image to sdcard.
	 * @param bmp
	 * @param file
	 */
	public static void saveBitmap(Bitmap bmp, File file)
	{
		FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG,100, fos);
            fos.flush();
            fos.close();
        }catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
	}
	

	// Getting Image file path from URI.
	public static String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);

		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	 
}
