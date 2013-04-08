package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lateralthoughts.vue.TrendingAislesAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

public class AisleLoader {
    private static final boolean DEBUG = false;
    //ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    private Context mContext;
    
    private static AisleLoader sAisleLoaderInstance = null;
    private ScaledImageViewFactory mViewFactory = null;
    private BitmapLoaderUtils mBitmapLoaderUtils;
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
        mViewFactory = ScaledImageViewFactory.getInstance(context);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(mContext);
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
    public void getAisleContentIntoView(AisleWindowContent windowContent, final ViewHolder holder,
    		int scrollIndex, int position){
    	ScaleImageView imageView = null;
    	ArrayList<AisleImageDetails> imageDetailsArr = null;
    	AisleImageDetails itemDetails = null;
    	AisleContentBrowser contentBrowser = null;
    	
    	if(null == windowContent || null == holder)
    		return;
		
    	//String currentContentId = holder.aisleContentBrowser.getUniqueId();
    	String desiredContentId = windowContent.getAisleId();
    	contentBrowser = holder.aisleContentBrowser;
		if(1 == position){
			if(DEBUG) Log.e("Vinodh","position = " + position + " currentId = " + holder.uniqueContentId + 
							" desired id = " + desiredContentId);
		}
		
    	//if(currentContentId.equals(desiredContentId)){
		if(holder.uniqueContentId.equals(desiredContentId)){
    		//we are looking at a visual object that has either not been used
    		//before or has to be filled with same content. Either way, no need
    		//to worry about cleaning up anything!
    		return;
    	}else{
    		//we are going to re-use an existing object to show some new content
    		//lets release the scaleimageviews first
    		for(int i=0;i<contentBrowser.getChildCount();i++){
    			mViewFactory.returnUsedImageView((ScaleImageView)contentBrowser.getChildAt(i));
    		}
    		holder.aisleContentBrowser.removeAllViews();
    		holder.aisleContentBrowser.setUniqueId(desiredContentId);
    		holder.aisleContentBrowser.setScrollIndex(scrollIndex);
    		holder.uniqueContentId = desiredContentId;
    	}    	
    	
    	imageDetailsArr = windowContent.getImageList();
    	if(null != imageDetailsArr){    		
    		itemDetails = imageDetailsArr.get(0);
			
			//imageView = mViewFactory.getEmptyImageView();
			imageView = mViewFactory.getPreconfiguredImageView(position);
			imageView.setContainerObject(holder);
			Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mCustomImageUrl);
			
			if(bitmap!=null){
				imageView.setImageBitmap(bitmap);
				contentBrowser.addView(imageView);    				
			}
			else{
				contentBrowser.setBackgroundColor(Color.WHITE);
				loadBitmap(itemDetails.mCustomImageUrl, contentBrowser, imageView);
			}
			prefetchImages(imageDetailsArr, 1);
        }
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
        //private final ImageView mImageView; //imageViewReference;
        private String url = null;

        public BitmapWorkerTask(AisleContentBrowser vFlipper, ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            viewFlipperReference = new WeakReference<AisleContentBrowser>(vFlipper); 
            //mImageView = imageView;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            //we want to get the bitmap and also add it into the memory cache
            bmp = mBitmapLoaderUtils.getBitmap(url, true); 
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
                	ViewHolder holder = (ViewHolder)((ScaleImageView)imageView).getContainerObject();
                	if(null != holder){
                		holder.aisleContext.setVisibility(View.VISIBLE);
                		holder.aisleOwnersName.setVisibility(View.VISIBLE);
                		holder.profileThumbnail.setVisibility(View.VISIBLE);
                		holder.aisleDescriptor.setVisibility(View.VISIBLE);
                	}
                	imageView.setImageBitmap(bitmap);
                	vFlipper.addView(imageView);
                	
                    vFlipper.setDisplayedChild(holder.aisleContentBrowser.getScrollIndex());
                }else{
                	Log.e("Jaws","imageView is NULL! vFlipper = " + vFlipper);
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
    
    @SuppressWarnings("unchecked")
	public void prefetchImages(ArrayList<AisleWindowContent> sourceForFetch, int startIndex, int length){
    	if(null == sourceForFetch)
    		return;
    	
    	if(startIndex < 0)
    		return;
    	
    	if(length < 0)
    		return;
    	
    	int size = sourceForFetch.size();
    	if(size < (startIndex + length))
    		return;
    	
    	AisleWindowContent content = null;
    	for(int i=startIndex; i<startIndex+length; i++){
    		content = sourceForFetch.get(i);
    		BitmapPrefetcherTask task = new BitmapPrefetcherTask();
    		task.execute(content.getImageList());    		
    	}
    }
    
    @SuppressWarnings("unchecked")
	public void prefetchImages(ArrayList<AisleImageDetails> imageSources, int startIndex){
    	if(null == imageSources)
    		return;
    	
    	if(startIndex < 0)
    		return;
    	
    	if(startIndex < imageSources.size())
    		return;
    	BitmapPrefetcherTask task = new BitmapPrefetcherTask();
    	task.execute(imageSources);
    }
    
    class BitmapPrefetcherTask extends AsyncTask<ArrayList<AisleImageDetails>, Void, Bitmap> {
        private ArrayList<AisleImageDetails> prefetchImageList;

        public BitmapPrefetcherTask() {
            // Use a WeakReference to ensure the ImageView can be garbage collected
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(ArrayList<AisleImageDetails>... params) {
        	prefetchImageList = params[0];
            String url = null;
            for(int i=0;i<prefetchImageList.size();i++){
            	url = prefetchImageList.get(i).mCustomImageUrl;
            	Log.e("Smartie Pants","about to invoke get bitmap for url = " + url);
            	mBitmapLoaderUtils.getBitmap(url, false);
            }
            return null;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	
        }
    }
    
}
