package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.lateralthoughts.vue.TrendingAislesAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.VueMemoryCache;

public class AisleLoader {
    VueMemoryCache<Bitmap> mAisleImagesCache; // = new MemoryCache();
    private static final boolean DEBUG = false;
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    //ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    private int mScreenWidth;
    private int mScreenHeight;
    private VueMemoryCache<Bitmap> mVueImageMemoryCache;
    private Context mContext;
    
    private static AisleLoader sAisleLoaderInstance = null;
    //Design Notes: The SGV is powered by data from the TrendingAislesAdapter. This adapter starts
    //the information flow by requesting top aisles in batches. As the aisle 
    //details start coming through the adapter notifies the view of changes in
    //data set which in turn triggers the getView() callback.
    //The complexity starts at this point: we are dealing with
    //an incredibly large amount of data. Each aisle window makes up
    //one item in the SGV. Each of this window consists of an image, below
    //which we can description of the image, the profile of the owner, the context,
    //the occasion etc. On top of this, the image itself can be flicked to reveal
    //a carousel of images that a user can swipe through. We can't possibly
    //download all of these and more importantly, we want to have top
    //performance for a couple of very important scenarios:
    //1. When the user flings the SGV up & down the scrolling needs to be smooth
    //2. User should be able to swipe across as AisleWindow and browse the
    //content.
    //Here is what we will do:
    //1. When an AisleWindowContent needs to be loaded up, we also get
    //the view into which it goes. Inside this viewFlipper we will store the
    //id of the AisleWindowContent.
    //2. When the viewFlipper is being recycled the if of the AisleWindowContent
    //and the id that ViewFlipper points to will be different. At this point
    //we will cancel all image download requests started for this ViewFlipper
    //3. If the ids match then we don't need to do anything.
    
    //In addition, we should consider keeping a pool of ScaledImageView objects
    //for efficiency. Right now, we are creating ScaledImageView objects everytime
    //we handle getView() and thats definitely hurting us!
    public static AisleLoader getInstance(Context context){
    	if(null == sAisleLoaderInstance){
    		sAisleLoaderInstance = new AisleLoader(context);
    	}
    	return sAisleLoaderInstance;    	
    }
    
    private AisleLoader(Context context){
    	//we don't want everyone creating an instance of this. We will
    	//instead use a factory pattern and return an instance using a
    	//static method
    	mContext = context;
        fileCache = new FileCache(mContext);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels; 
        mAisleImagesCache = VueApplication.getInstance().getAisleImagesMemCache();
    }
    
    //This method adds the intelligence to fetch the contents of this aisle window and
    //updates the relevant view so that the item is visible to the user.
    //Some caveats: This API is aware of the internals of the View - i.e., ViewHolder
    //which represents one item in the staggered grid view.
    //This is less than ideal but it doesn't make sense to aritificially constrain this
    //class from being aware of the UI side of things.
    //The logic itself is reasonably simple: given an AisleWindowContent object we first determine
    //the number of images that need to be in this view item. We then need to determine how to
    //load each of the images in a non-intrusive way.
    //I have implemented a relatively robust asynctask pattern for this: for each image view,
    //start an async task and use a standard DownloadedDrawable object to keep track of the task
    //When the task completes check to make sure that the url for which the task was started is still
    //valid. If so, add the downloaded image to the view object
    public void getAisleContentIntoView(AisleWindowContent content, ViewHolder holder){
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
    				//viewFlipper.addView(imageView);
    			}
    			else{
    				//loadBitmap(itemDetails.mCustomImageUrl, viewFlipper, imageView);
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
            //bmp = getBitmap(url); 
            mAisleImagesCache.put(url, bmp);
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	int count = 0;
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
    
    //utility functions to keep track of all the async tasks that we instantiate
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
    
}
