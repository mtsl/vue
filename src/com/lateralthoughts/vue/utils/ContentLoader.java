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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import android.widget.ViewFlipper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import android.os.AsyncTask;
import java.lang.ref.WeakReference;

import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.ui.ScaleImageView;

import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Using LazyList via https://github.com/thest1/LazyList/tree/master/src/com/fedorvlasov/lazylist
 * for the example since its super lightweight
 * I barely modified this file
 */
public class ContentLoader {
    
    VueMemoryCache<Bitmap> mAisleImagesCache; // = new MemoryCache();
    private static final boolean DEBUG = false;
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    //ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    private Context mContext;
    private int mScreenWidth;
    
    public ContentLoader(Context context){
    	mContext = context;
        fileCache = new FileCache(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels; 
        mAisleImagesCache = VueApplication.getInstance().getAisleImagesMemCache();
    }
    
    public void displayImage(String url, ImageView imageView){
        imageViews.put(imageView, url);
        Bitmap bitmap = mAisleImagesCache.get(url);
        if(null != bitmap)
            imageView.setImageBitmap(bitmap);
        else{
            imageView.setImageDrawable(null);
        }
    }
    
    /**
     * 
     * @param position - this parameter is being sent in just for debugging purposes and is otherwise not used
     * @param content
     * @param viewFlipper
     */
    public void displayContent(int position, AisleWindowContent content, ViewFlipper viewFlipper){
    	int count = 0;
    	ScaleImageView imageView = null;
    	ArrayList<AisleImageDetails> imageDetailsArr = null;
    	AisleImageDetails itemDetails = null;
    	
    	if(null == content)
    		return;
    	
    	imageDetailsArr = content.getImageList();
    	if(null != imageDetailsArr){
    		count = imageDetailsArr.size();
    		
    		for(int j=0;j<count;j++){
    			itemDetails = imageDetailsArr.get(j);
    			imageView = new ScaleImageView(mContext);
    			imageViews.put(imageView, itemDetails.mCustomImageUrl);
    			Bitmap bitmap = mAisleImagesCache.get(itemDetails.mCustomImageUrl);
    			
    			if(bitmap!=null){
    				imageView.setImageBitmap(bitmap);
    				viewFlipper.addView(imageView);
    			}
    			else{
    				loadBitmap(itemDetails.mCustomImageUrl, viewFlipper, imageView);
    				//imageView.setImageDrawable(new DownloadDrawable());
    			}
            }
        }
    }
    
    public void loadBitmap(String loc, ViewFlipper flipper, ImageView imageView) {
        if (cancelPotentialDownload(loc, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(flipper, imageView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(loc);
        }
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

    public void clearCache() {
    	mAisleImagesCache.clear();
        //fileCache.clear();
    }
    
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapWorkerTask bitmapDownloaderTask = getBitmapWorkerTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }
    
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerReference;

        public DownloadedDrawable(BitmapWorkerTask bitmapWorkerTask) {
            super(Color.BLACK);
            bitmapWorkerReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerReference.get();
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ViewFlipper>viewFlipperReference;
        //private final ImageView mImageView; //imageViewReference;
        private String url = null;

        public BitmapWorkerTask(ViewFlipper vFlipper, ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            viewFlipperReference = new WeakReference<ViewFlipper>(vFlipper); 
            //mImageView = imageView;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            bmp = getBitmap(url); 
            mAisleImagesCache.put(url, bmp);
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (viewFlipperReference != null && 
            		imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final ViewFlipper vFlipper = viewFlipperReference.get();
                BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                
                if (this == bitmapWorkerTask) {
                    imageView.setImageBitmap(bitmap);
                    vFlipper.addView(imageView);
                }else{
                	Log.e("Jaws","imageView is NULL! vFlipper = " + vFlipper);
                }
            }
        }
    }
    
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}

