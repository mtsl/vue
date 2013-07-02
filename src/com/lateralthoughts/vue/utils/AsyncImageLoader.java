package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author Surendra. AsyncImageLoader loads the images from sdCard to gallery
 *         view in the list view asynchronously.
 */
public class AsyncImageLoader {
  Context context;
  private SharedPreferences preferences;

  public static final String TAG = "AsyncImageLoader";
  int cacheSize;
  final int memClass;
  private LruCache<String, Bitmap> bitmapCache;
  
  
  /**
   * Constructor using in FishwrapHomeActivity's NewsArticleRowAdapter to load
   * the images.
   * 
   * @param context Context
   * */
  public AsyncImageLoader(Context context) {

    memClass = ((ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    
    // 1/10th of the available memory for this memory cache.
    cacheSize = 1024 * 1024 * memClass / 10;
    bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap value) {
        return value.getHeight() * value.getRowBytes();
      }
    };
    preferences = PreferenceManager
        .getDefaultSharedPreferences(context);
  }

  Handler handler = new Handler() {

    @Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			LoadImageFromSdCard loadImage = (LoadImageFromSdCard) msg.obj;
			if (loadImage.imageView != null) {
				if (loadImage.imageView.getTag() == loadImage.url) {
					loadImage.imageView.setImageBitmap(loadImage.bmp);
				}
			}
		}
  };


  /**
   * loads the image If the image exists in the cache else load it from SD card.
   * 
   * @param uri String
   * @param imageView ImageView
   * @param context Context
   * @param imageSource String
   * @return Bitmap
   */
  public Bitmap loadImage(Context context, String url, ImageView imageView) {
    this.context = context;

    Bitmap bmp = bitmapCache.get(url);
    Log.e(TAG, "PATH IN SDCARD loadImage() : uri : " + url);
    if (bmp != null) {
      return bmp;
    } else {
      Thread thread = new Thread(new LoadImageFromSdCard(url, imageView));
      thread.setPriority(Thread.NORM_PRIORITY);
      thread.start();
    }
    return null;
  }
  
  /** starts a thread to load the image for sdCard. */
  private class LoadImageFromSdCard implements Runnable {
    ImageView imageView;
    String url;
    Bitmap bmp;

    /**
     * Constructor
     * 
     * @param dir String
     * @param imagename String
     * @param imageView ImageView
     * @param imageSource String
     */
    public LoadImageFromSdCard(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
        
    }

    /** will call automatically when new thread is created. */
    public void run() {
      if (url == null) {
    	  Log.e(TAG, "URI IS NULL...");
        return;
      }
      if (imageView == null) {
        Log.e(TAG, "imageView is NULL...");
        return;
      }
      imageView.setTag(url);
      bmp = downloadImage(url);
      if(bmp == null) {
    	  return;
      }
      bitmapCache.put(url, bmp);
      Message message = new Message();
      message.obj = this;
      handler.sendMessage(message);
    }

  }

  /**
   * this method is to download image from server.
   * 
   * @param imagePath String
   * @param imageSource String
   * @return String image path in SDcard.
   */
  private Bitmap downloadImage(String imagePath) {
    Bitmap bitmap = null;
    InputStream is = null;
    URL url;
	try {
		url = new URL(imagePath);
		URLConnection con = url.openConnection();
	    is = con.getInputStream();
	} catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
    if(is != null) {
    	bitmap = BitmapFactory.decodeStream(is);
    }
    return bitmap;
  }


  /**
   * to save the images into the SDcard.
   * 
   * @param bm the image to store in the SDcard
   * @param dir String directory path
   * @param imageName name with which we want to save the image in SDcard
   * @return returns the image path to save in the data base.
   */
  public boolean saveImageToSdCard(Bitmap bm, String dir, String imageName) {
    FileOutputStream outputStream;
    if (bm == null) {
      return false;
    }
    if (!Environment.MEDIA_MOUNTED
        .equals(Environment.getExternalStorageState())) {
      return false;
    }
    try {
      File fishWrapDir = new File(dir);
      if (!fishWrapDir.isDirectory()) {
        fishWrapDir.mkdirs();
      }
      File imageFile = new File(fishWrapDir, imageName);
      outputStream = new FileOutputStream(imageFile);
      bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
      bm = null;
      outputStream.flush();
      outputStream.close();
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  
  private Boolean checkConnection(Context context, Boolean networkSetting) {
      ConnectivityManager connectivityManager;
      NetworkInfo wifiInfo, mobileInfo;
      boolean isConneted = false;
      try {
          connectivityManager = (ConnectivityManager) context
                  .getSystemService(Context.CONNECTIVITY_SERVICE);
          wifiInfo = connectivityManager
                  .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
          mobileInfo = connectivityManager
                  .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
          if (networkSetting) {
              if (wifiInfo.isConnected()) {
                  isConneted = true;
              }
          } else {
              if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                  isConneted = true;
              }
          }
      } catch (Exception e) {

      }
      return isConneted;
  }
}
