package com.lateralthoughts.vue.utils;

import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.util.Log;

public class Utils {
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
    	 
		return createBitmap(bitmap,newWidth,newHeight);
    	// return bitmap;
    }
    private static  Bitmap createBitmap(Bitmap bitmap,int width,int height) {
    	Bitmap bmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    	Log.i("width & height", "reqWidth new: "+bmap.getWidth()+" reqHeight2: "+bmap.getHeight());
		return bmap;
    	
    }
}