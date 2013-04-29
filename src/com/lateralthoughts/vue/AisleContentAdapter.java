package com.lateralthoughts.vue;

//android imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.ImageView;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

//java imports
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//internal imports
import com.lateralthoughts.vue.AisleLoader.BitmapWorkerTask;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ScaledImageViewFactory;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.VueMemoryCache;
import com.lateralthoughts.vue.utils.FileCache;

/**
 * The AisleContentAdapter object will be associated with aisles on a per-aisle
 * basis. Within each aisle's content, this object will manage resources such
 * as determining the number of images per aisle that need to be kept in memory
 * when to fetch these images etc.
 * Here is the general outline:
 * The AisleLoader class is associated with the TrendingAislesPage and will help
 * load up the first images in each of the ViewFlipper.
 * During startup, we don't want to consume more resources (threads, CPU cycles) to
 * download content that is not visible right away. For example, even though an aisle
 * has multiple images these images are visible one at a time.
 * The AisleContentAdapter will instead perform this function. At startup, this object
 * will download either max of N items or the most number of items in the aisle - whichever
 * is lowest. Say an aisle contains 10 images, we will download just N where N is set to 1 or 2
 * or other number based on trial & error.
 * When the user swipes through images and the ViewFlipper instance will seek the next item from
 * this adapter. At this point, we will adjust the currentPivotIndex and ensure that we have 1 image
 * on either side readily available in the content cache. This should help with performance and also
 * optimize how much we need to do during startup time.
 */
public class AisleContentAdapter implements IAisleContentAdapter {

    private VueMemoryCache<Bitmap> mContentImagesCache;
    private ArrayList<AisleImageDetails> mAisleImageDetails;
    private AisleWindowContent mWindowContent;
    private int mCurrentPivotIndex;
    private String mAisleId;
    
    private int mScreenWidth;
    private ExecutorService mExecutorService;
    private Context mContext;
    private FileCache mFileCache;
    private ScaledImageViewFactory mImageViewFactory;
    private ColorDrawable mColorDrawable;
    
    public AisleContentAdapter(Context context){
        mContext = context;
        mContentImagesCache = VueApplication.getInstance().getAisleContentCache();
        mFileCache = VueApplication.getInstance().getFileCache();
        mCurrentPivotIndex = -1;
        mImageViewFactory  = ScaledImageViewFactory.getInstance(mContext);
        
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;        
        mExecutorService = Executors.newFixedThreadPool(5);
        mColorDrawable = new ColorDrawable(Color.WHITE);
    }
    
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    @Override
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent) {
        // TODO Auto-generated method stub
        mWindowContent = windowContent;
        mAisleImageDetails = mWindowContent.getImageList();
        mAisleId = uniqueAisleId;
        
        //lets file cache the first two items in the list
        queueImagePrefetch(mAisleImageDetails, 1,2);
    }
    
    @Override
    public void releaseContentSource() {
        // TODO Auto-generated method stub
        mCurrentPivotIndex = -1;
        
    }

    @Override
    public ScaleImageView getItemAt(int index, boolean isPivot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPivot(int index) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void registerAisleDataObserver(IAisleDataObserver observer){
        
    }
    
    @Override
    public void unregisterAisleDataObserver(IAisleDataObserver observer){
    }
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    
    
    public void queueImagePrefetch(ArrayList<AisleImageDetails> imageList, int startIndex, int count){
        BitmapsToFetch p = new BitmapsToFetch(imageList, startIndex, count);
        mExecutorService.submit(new ImagePrefetcher(p));
    }
    
    //Task for the queue
    private class BitmapsToFetch
    {
        public ArrayList<AisleImageDetails> mImagesList;
        public int mStartIndex;
        private int mCount;
        public BitmapsToFetch(ArrayList<AisleImageDetails> imagesList, int startIndex, int count){
            mImagesList = imagesList; 
            mStartIndex = startIndex;
            mCount = count;
        }
    }
    
    class ImagePrefetcher implements Runnable {
        BitmapsToFetch mBitmapsToFetch;
        ImagePrefetcher(BitmapsToFetch details){
            this.mBitmapsToFetch = details;
        }
        
        @Override
        public void run() {
            int startIndex = mBitmapsToFetch.mStartIndex;
            int count = mBitmapsToFetch.mCount;
            if(count+startIndex >= mBitmapsToFetch.mImagesList.size())
                return;
            
            for(int i = startIndex; i<count+startIndex; i++){
                //Log.e("FileCacher","about to cache file for index = " + mBitmapsToFetch.mImagesList.get(i).mCustomImageUrl);
                cacheBitmapToLocal(mBitmapsToFetch.mImagesList.get(i).mCustomImageUrl);
            }
        }
    }
    
    
    public void cacheBitmapToLocal(String url) 
    {
        File f = mFileCache.getFile(url);        
        //from SD cache
        if(isBitmapCachedLocally(f)){
            return;
        }
        
        //from web
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            return;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               mContentImagesCache.clear();
           return;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private boolean isBitmapCachedLocally(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = mScreenWidth/2;
            int width_tmp = o.outWidth, height_tmp=o.outHeight;
            int scale = 1;
            while(true){
                if(width_tmp < REQUIRED_SIZE || height_tmp < REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return true;
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isBitmapCached(String url){
        if(null != mContentImagesCache.get(url)){
            return true;
        }else{
            return false;
        }
    }
    
    @Override
    public boolean setAisleContent(AisleContentBrowser contentBrowser,ScaleImageView reuseView, int currentIndex, int wantedIndex, 
                                            boolean shiftPivot){
        ScaleImageView imageView = null;
        AisleImageDetails itemDetails = null;
        
        if(wantedIndex >= mAisleImageDetails.size())
            return false;
        
        if(0 >= currentIndex && wantedIndex < currentIndex)
            return false;
        
        if(null != mAisleImageDetails && mAisleImageDetails.size() != 0){         
            itemDetails = mAisleImageDetails.get(wantedIndex);
            if(null == reuseView)
                imageView = mImageViewFactory.getEmptyImageView();
            else
                imageView = reuseView;
            
            imageView.setContainerObject(contentBrowser);            
            Bitmap bitmap = getCachedBitmap(itemDetails.mCustomImageUrl);
            
            if(bitmap != null){
                //Log.e("AisleContentAdapter","Bitmap is cached so we will just set it and add it to the imageview");
                imageView.setImageBitmap(bitmap);
                contentBrowser.addView(imageView);
                imageView.invalidate();
                //loadBitmap(itemDetails.mCustomImageUrl, contentBrowser, imageView);
            }
            else{                
                mColorDrawable.setBounds(0, 0, 240, 400);
                imageView.setBackground(mColorDrawable);
                //Log.e("AisleContentAdapter","Adding the imageview now...the bitmap needs to be fetched. wantedIndex = " + wantedIndex);
                contentBrowser.addView(imageView);
                loadBitmap(itemDetails.mCustomImageUrl, contentBrowser, imageView);
            }
        }
        return true;
    }
    
    public Bitmap getCachedBitmap(String url){
        return mContentImagesCache.get(url);      
    }
    
    public void loadBitmap(String loc, AisleContentBrowser flipper, ImageView imageView) {
        if (cancelPotentialDownload(loc, imageView)) {          
            BitmapWorkerTask task = new BitmapWorkerTask(flipper, imageView);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            task.execute(loc);
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<AisleContentBrowser>viewFlipperReference;
        private String url = null;

        public BitmapWorkerTask(AisleContentBrowser vFlipper, ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            viewFlipperReference = new WeakReference<AisleContentBrowser>(vFlipper); 
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            //we want to get the bitmap and also add it into the memory cache
            bmp = getBitmap(url, true); 
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (viewFlipperReference != null && 
                    imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final AisleContentBrowser vFlipper = viewFlipperReference.get();
                BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                
                if (this == bitmapWorkerTask) {
                    /*ViewHolder holder = (ViewHolder)((ScaleImageView)imageView).getContainerObject();
                    if(null != holder){
                        holder.aisleContext.setVisibility(View.VISIBLE);
                        holder.aisleOwnersName.setVisibility(View.VISIBLE);
                        holder.profileThumbnail.setVisibility(View.VISIBLE);
                        holder.aisleDescriptor.setVisibility(View.VISIBLE);
                    }*/
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    
    //utility functions to keep track of all the async tasks that we instantiate
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Object task = ((ScaleImageView)imageView).getOpaqueWorkerObject();
            if (task instanceof BitmapWorkerTask) {
                BitmapWorkerTask workerTask = (BitmapWorkerTask)task;
                return workerTask;
            }
        }
        return null;
    }
    
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        
        if (bitmapWorkerTask != null) {
            String bitmapUrl = bitmapWorkerTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapWorkerTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
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
        if(b != null){
            //if(cacheBitmap)
                mContentImagesCache.put(url, b);
            return b;
        }
        
        //from web
        try {
            Bitmap bitmap=null;
            System.setProperty("http.keepAlive", "false");
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
            //if(cacheBitmap)
                mContentImagesCache.put(url, bitmap);
            
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               mContentImagesCache.clear();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
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
}
