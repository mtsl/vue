package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

//import com.lateralthoughts.vue.TrendingAislesAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.ImageDimention;
import com.lateralthoughts.vue.utils.Utils;
 
import com.lateralthoughts.vue.TrendingAislesGenericAdapter.ViewHolder;

public class AisleDetailsViewListLoader {
    private static final boolean DEBUG = false;
    private static final String TAG = "AisleDetailsViewListLoader";
    Handler handler = new Handler();
    private Context mContext;
    private ContentAdapterFactory mContentAdapterFactory;
    
    private static AisleDetailsViewListLoader sAisleDetailsViewLoaderInstance = null;
    private ScaledImageViewFactory mViewFactory = null;
    private BitmapLoaderUtils mBitmapLoaderUtils;
    private HashMap<String, ViewHolder> mContentViewMap = new HashMap<String, ViewHolder>();
    private ImageDimention mImageDimention;
    //private int mBestHeight;
    
    public static AisleDetailsViewListLoader getInstance(Context context){
        if(null == sAisleDetailsViewLoaderInstance){
            sAisleDetailsViewLoaderInstance = new AisleDetailsViewListLoader(context);
        }
        return sAisleDetailsViewLoaderInstance;        
    }
    
    private AisleDetailsViewListLoader(Context context){
        //we don't want everyone creating an instance of this. We will
        //instead use a factory pattern and return an instance using a
        //static method
        mContext = context;
        mViewFactory = ScaledImageViewFactory.getInstance(context);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(mContext);
        mContentAdapterFactory = ContentAdapterFactory.getInstance(mContext);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        mBitmapLoaderUtils.clearCache();
        if(DEBUG) Log.e(TAG,"Log something to remove warning");
    }
    public void getAisleContentIntoView(AisleDetailsViewAdapter.ViewHolder holder,
            int scrollIndex, int position,DetailClickListener detailListener){
        ScaleImageView imageView = null;
        ArrayList<AisleImageDetails> imageDetailsArr = null;
        AisleImageDetails itemDetails = null;
        AisleContentBrowser contentBrowser = null;
        if(null == holder)
            return;
        AisleWindowContent windowContent = holder.mWindowContent;
        if(null == windowContent)
            return;
        String desiredContentId = windowContent.getAisleId();
        contentBrowser = holder.aisleContentBrowser;
        contentBrowser.setHolderName(VueAisleDetailsViewFragment.SCREEN_NAME);
        if(holder.uniqueContentId.equals(desiredContentId)){
           Log.i("bitmapsize", "bitmapsize: call  return from here: ");
            //we are looking at a visual object that has either not been used
            //before or has to be filled with same content. Either way, no need
            //to worry about cleaning up anything!
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            return;
        }else{
            //we are going to re-use an existing object to show some new content
            //lets release the scaleimageviews first
            for(int i=0;i<contentBrowser.getChildCount();i++){
                //((ScaleImageView)contentBrowser.getChildAt(i)).setContainerObject(null);
                mViewFactory.returnUsedImageView((ScaleImageView)contentBrowser.getChildAt(i));
            }
            IAisleContentAdapter adapter = mContentAdapterFactory.getAisleContentAdapter();
            mContentAdapterFactory.returnUsedAdapter(holder.aisleContentBrowser.getCustomAdapter());
            holder.aisleContentBrowser.setCustomAdapter(null);
            adapter.setContentSource(desiredContentId, holder.mWindowContent);
            adapter.setSourceName(holder.tag);
           // holder.thumbnailScroller.removeAllViews();
            holder.aisleContentBrowser.removeAllViews();
            holder.aisleContentBrowser.setUniqueId(desiredContentId);
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            holder.aisleContentBrowser.setCustomAdapter(adapter);
            holder.aisleContentBrowser.setDetailImageClickListener(detailListener);
            holder.uniqueContentId = desiredContentId;
        }       
        imageDetailsArr = windowContent.getImageList();
		if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
			holder.aisleContentBrowser.mSwipeListener
					.onReceiveImageCount(imageDetailsArr.size());
			itemDetails = imageDetailsArr.get(0);
			imageView = mViewFactory.getEmptyImageView();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.CENTER;
			imageView.setLayoutParams(params);
			imageView.setContainerObject(holder);
			// imgConnectivity.setImageClick(imageView);
			Bitmap bitmap = mBitmapLoaderUtils
					.getCachedBitmap(itemDetails.mCustomImageUrl);
			if (bitmap != null) {
				//get the dimentions of the image.
				 mImageDimention = Utils.getScalledImage(bitmap,
						 itemDetails.mAvailableWidth, itemDetails.mAvailableHeight);
				setParams(holder.aisleContentBrowser, imageView);
				 if(bitmap.getHeight() < mImageDimention.mImgHeight) {
					 bitmap = mBitmapLoaderUtils.getBitmap(itemDetails.mCustomImageUrl, true, mImageDimention.mImgHeight);
				 }
			/*	bitmap = Utils.getScalledImage(bitmap,
						itemDetails.mAvailableWidth,
						itemDetails.mAvailableHeight);*/
				imageView.setImageBitmap(bitmap);
				contentBrowser.addView(imageView);
			} else {
				contentBrowser.addView(imageView);
				loadBitmap(itemDetails, contentBrowser, imageView,
						itemDetails.mAvailableHeight);
			}
		}        
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, AisleContentBrowser flipper, ImageView imageView, int bestHeight) {
    	String loc = itemDetails.mCustomImageUrl;
        if (cancelPotentialDownload(loc, imageView)) {          
            BitmapWorkerTask task = new BitmapWorkerTask(itemDetails,flipper, imageView, bestHeight);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            task.execute(loc);
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        //private final WeakReference<AisleContentBrowser>viewFlipperReference;
        private String url = null;
        private int mBestHeight;
        AisleContentBrowser aisleContentBrowser ;
        int mAvailabeWidth,mAvailableHeight;

        public BitmapWorkerTask(AisleImageDetails itemDetails,AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mBestHeight = bestHeight;
            aisleContentBrowser = vFlipper;
            mAvailabeWidth = itemDetails.mAvailableWidth;
            mAvailableHeight = itemDetails.mAvailableHeight;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            //we want to get the bitmap and also add it into the memory cache
            bmp = mBitmapLoaderUtils.getBitmap(url, true, mBestHeight); 
            if(bmp != null) {
           	 mImageDimention = Utils.getScalledImage(bmp,
           			mAvailabeWidth, mAvailableHeight);
            	
            	 if(bmp.getHeight()<mImageDimention.mImgHeight) {
            		 bmp = mBitmapLoaderUtils.getBitmap(url, true, mImageDimention.mImgHeight);
				 }
            }
            
            
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                //final AisleContentBrowser vFlipper = viewFlipperReference.get();
                BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                
                if (this == bitmapWorkerTask) {
                   //aisleContentBrowser.addView(imageView);
                  setParams( aisleContentBrowser, imageView);
                  // bitmap = Utils.getScalledImage(bitmap, mAvailabeWidth,mAvailableHeight);
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

   private void setParams(AisleContentBrowser vFlipper, ImageView imageView 
          ) {
      int imgCardHeight = (VueApplication.getInstance().getScreenHeight() * 60) / 100;
      FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
            VueApplication.getInstance().getScreenWidth(), (VueApplication.getInstance().getScreenHeight() * 60) / 100);

      if (vFlipper != null)
         vFlipper.setLayoutParams(showpieceParams);
      /*
       * bitmap = Utils.getScaledBitMap(bitmap,
       * (VueApplication.getInstance().getScreenWidth()*80)/100,
       * (VueApplication.getInstance().getScreenHeight()*60)/100);
       */

      if (vFlipper != null) {
         FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
               LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         params.gravity = Gravity.CENTER;
         imageView.setLayoutParams(params);
      } else {
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
               LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         params.gravity = Gravity.CENTER;
         imageView.setScaleType(ScaleType.CENTER_INSIDE);
         imageView.setLayoutParams(params);
         imageView.setScaleType(ScaleType.CENTER_INSIDE);
         /*
          * bitmap = Utils.getScaledBitMap(bitmap,
          * (VueApplication.getInstance().getScreenWidth()*80)/100,
          * (VueApplication.getInstance().getScreenHeight()*60)/100);
          */
      }
       
   }
}