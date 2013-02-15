package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.os.AsyncTask;
import java.lang.ref.WeakReference;

import com.lateralthoughts.vue.VueApplication;

import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Using LazyList via https://github.com/thest1/LazyList/tree/master/src/com/fedorvlasov/lazylist
 * for the example since its super lightweight
 * I barely modified this file
 */
public class ContentLoader {
    
    VueMemoryCache<Bitmap> mAisleImagesCache; // = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    //ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    private Context mContext;
    private int mScreenWidth;
    private int mScreenHeight;
    
    public ContentLoader(Context context){
    	mContext = context;
        fileCache = new FileCache(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels; 
        mAisleImagesCache = VueApplication.getInstance().getAisleImagesMemCache();
    }
    
    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap = mAisleImagesCache.get(url);
        if(null != bitmap)
            imageView.setImageBitmap(bitmap);
        else
        {
        	loadBitmap(url, imageView);
            imageView.setImageDrawable(null);
        }
    }
    
    public void DisplayContent(String url, LinearLayout rowItem)
    {
        /*imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
        	loadBitmap(url, imageView);
            imageView.setImageDrawable(null);
        }*/
    }
    
    public void loadBitmap(String loc, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(loc);
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f = fileCache.getFile(url);
        
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
            
            /*if(scale>=2){
            	scale/=2;
            }*/
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            Log.d("Jaws","using inSampleSizeScale = " + scale + " original width = " + o.outWidth + "screen width = " + mScreenWidth);
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

    public void clearCache() {
    	mAisleImagesCache.clear();
        fileCache.clear();
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            Bitmap bmp = null;            
            bmp = getBitmap(data); 
            mAisleImagesCache.put(data, bmp);
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}

