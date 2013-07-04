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
    public void getContentIntoCompareview(VueComparisionAdapter.ViewHolder holder){
    	  ScaleImageView imageView = null;
    	  ScaleImageView imageView1 = null;
          ArrayList<AisleImageDetails> imageDetailsArr = null;
          AisleImageDetails itemDetails = null;
          int position = 0;
          AisleContentBrowser contentBrowser = null;
          if(null == holder)
              return;
          AisleWindowContent windowContent = holder.mWindowContent;
          
          if(null == windowContent)
              return;
          imageDetailsArr = windowContent.getImageList();
           int pixel = VueApplication.getInstance().getPixel(5);
          if(null != imageDetailsArr && imageDetailsArr.size() != 0){  
                 for(int k=0;k<imageDetailsArr.size();k++){
          itemDetails = imageDetailsArr.get(k);
          imageView = mViewFactory.getPreconfiguredImageView(position);
          imageView1 = mViewFactory.getPreconfiguredImageView(position+5);
          
          LinearLayout.LayoutParams params = 
                  new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, VueApplication.getInstance().getScreenHeight());
        
         // imageView.setPadding(3, 3, 3, 3);
          imageView.setContainerObject(holder);
          
          imageView1 = mViewFactory.getPreconfiguredImageView(position);
          LinearLayout.LayoutParams params1 = 
                  new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, VueApplication.getInstance().getScreenHeight());
          
          
          LinearLayout toplay = new LinearLayout(mContext);
          LinearLayout bottomlay = new LinearLayout(mContext);
          toplay.setLayoutParams(params);
          bottomlay.setLayoutParams(params1);
          toplay.setBackgroundColor(Color.parseColor("#ffffff"));
          bottomlay.setBackgroundColor(Color.parseColor("#ffffff"));
          
          
          //imageView1.setPadding(3, 3, 3, 3);
          imageView1.setContainerObject(holder);
          
          params.setMargins(pixel, 0, pixel, 0);
          params1.setMargins(pixel, 0,pixel, 0);
          params.gravity = Gravity.CENTER;
          params1.gravity = Gravity.CENTER;
          imageView1.setLayoutParams(params1);
          imageView.setLayoutParams(params);
          
          
          Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mCustomImageUrl);
          if(bitmap != null){
        	  int width = imageView.getWidth();
        	  int height = imageView.getHeight();
        	 //  if(bitmap.getHeight() >  VueApplication.getInstance().getScreenHeight()/3 || bitmap.getWidth() >VueApplication.getInstance().getScreenWidth() ) {
           		  Log.i("width & height", "reqWidth1: Bitmap is greater than card size21" );
           		 bitmap =  Utils.getScaledBitMap(bitmap,(VueApplication.getInstance().getScreenWidth() * 10)/100,  (VueApplication.getInstance().getScreenHeight()* 10)/100);
           	 // }
        	  toplay.addView(imageView);
        	  bottomlay.addView(imageView1);
        	  imageView.setScaleType(ScaleType.CENTER_INSIDE);
        	  imageView1.setScaleType(ScaleType.CENTER_INSIDE);
              imageView.setImageBitmap(bitmap);
              imageView1.setImageBitmap(bitmap);
              holder.topScroller.addView(toplay);  
              holder.bottomScroller.addView(bottomlay);
          }
          else{
        	  toplay.addView(imageView);
        	  bottomlay.addView(imageView1);
              holder.topScroller.addView(toplay);
              holder.bottomScroller.addView(bottomlay);
              imageView.setScaleX(0.9f);
              imageView1.setScaleY(0.9f);
        	  imageView.setScaleType(ScaleType.CENTER_INSIDE);
        	  imageView1.setScaleType(ScaleType.CENTER_INSIDE);
              loadBitmap(itemDetails.mCustomImageUrl, null, imageView, 400);
              loadBitmap(itemDetails.mCustomImageUrl, null, imageView1, 400);
          }
      }  
          }
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
        
        if(holder.uniqueContentId.equals(desiredContentId)){
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
            
          /*  for(int i=0;i<holder.thumbnailScroller.getChildCount();i++){
                mViewFactory.returnUsedImageView((ScaleImageView)holder.thumbnailScroller.getChildAt(i));
            }*/
           // holder.thumbnailScroller.removeAllViews();
            holder.aisleContentBrowser.removeAllViews();
            holder.aisleContentBrowser.setUniqueId(desiredContentId);
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            holder.aisleContentBrowser.setCustomAdapter(adapter);
            holder.aisleContentBrowser.setDetailImageClickListener(detailListener);
            holder.uniqueContentId = desiredContentId;
        }       
        
        imageDetailsArr = windowContent.getImageList();
        
        if(null != imageDetailsArr && imageDetailsArr.size() != 0){  
        	 holder.aisleContentBrowser.mSwipeListener.onReceiveImageCount(imageDetailsArr.size());
            itemDetails = imageDetailsArr.get(0);
            imageView = mViewFactory.getEmptyImageView();
            LinearLayout.LayoutParams params = 
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            imageView.setLayoutParams(params);
            imageView.setContainerObject(holder);
           // imgConnectivity.setImageClick(imageView);
            Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mCustomImageUrl);
            if(bitmap != null){
            	 Log.i("width & height", "reqWidth1: Bitmap  set directly here" );
            	bitmap =  setParams(holder.aisleContentBrowser, imageView, bitmap);
            	// bitmap = Utils.getScaledBitMap(bitmap, VueApplication.getInstance().getScreenWidth(), VueApplication.getInstance().getScreenHeight());
                imageView.setImageBitmap(bitmap);
                contentBrowser.addView(imageView);                  
            }
            else{
                contentBrowser.addView(imageView);
             /*   if(!VueConnectivityManager.isNetworkConnected(VueApplication.getInstance())) {
                  Log.e("VueContentRestService", "network connection No");
                  return;
                }*/
                loadBitmap(itemDetails.mCustomImageUrl, contentBrowser, imageView, windowContent.getBestHeightForWindow());
            }
            
     /*       for(int k=0;k<imageDetailsArr.size();k++){
                itemDetails = imageDetailsArr.get(k);
                imageView = mViewFactory.getPreconfiguredImageView(position);
                //LinearLayout.LayoutParams params = 
                //        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(params);
                imageView.setPadding(3, 3, 3, 3);
                imageView.setContainerObject(holder);
                Bitmap b = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mCustomImageUrl);
                if(bitmap != null){
                    imageView.setImageBitmap(b);
                    holder.thumbnailScroller.addView(imageView);                  
                }
                else{
                    holder.thumbnailScroller.addView(imageView);
                    imageView.setScaleX(0.9f);
                    imageView.setScaleY(0.9f);
                    loadBitmap(itemDetails.mCustomImageUrl, null, imageView, 400);
                }
            } */           
        }        
        //we also need to set up the horizontal image views
    }
    
    public void loadBitmap(String loc, AisleContentBrowser flipper, ImageView imageView, int bestHeight) {
        if (cancelPotentialDownload(loc, imageView)) {          
            BitmapWorkerTask task = new BitmapWorkerTask(flipper, imageView, bestHeight);
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

        public BitmapWorkerTask(AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mBestHeight = bestHeight;
            aisleContentBrowser = vFlipper;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            //we want to get the bitmap and also add it into the memory cache
            bmp = mBitmapLoaderUtils.getBitmap(url, true, mBestHeight); 
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
                	bitmap =  setParams( aisleContentBrowser, imageView, bitmap);
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
    private Bitmap setParams(AisleContentBrowser vFlipper,ImageView imageView,Bitmap bitmap) {
    	Log.i("width & height", "reqWidth1: Bitmap is greater than card size0" );
    	int imgCardHeight =   (VueApplication.getInstance().getScreenHeight() *60) /100;
    	FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
				VueApplication.getInstance().getScreenWidth(),imgCardHeight);
    	showpieceParams.setMargins(0, 50, 0, 50);
    	if(vFlipper != null)
    	vFlipper.setLayoutParams(showpieceParams);
    	 /* if(bitmap.getHeight() > imgCardHeight || bitmap.getWidth() >VueApplication.getInstance().getScreenWidth() ) {*/
    	 Log.i("width & height", "reqWidth1: Bitmap is less than card size" );
    		 bitmap =  Utils.getScaledBitMap(bitmap, (VueApplication.getInstance().getScreenWidth()*90)/100, (imgCardHeight*90)/100);
    	/*  } else {
    		  Log.i("width & height", "reqWidth1: Bitmap is less than card size" );
    	  }*/
    	if(vFlipper != null) {
    	FrameLayout.LayoutParams params = 
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
    	} else {
    		LinearLayout.LayoutParams params = 
                    new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            //params.setMargins(100, 0, 100, 100);
             imageView.setScaleType(ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(params);
      	  imageView.setScaleType(ScaleType.CENTER_INSIDE);
    	   
            Log.i("width & height", "reqWidth1: Bitmap is greater than card size1" );
           // if(bitmap.getHeight() >  VueApplication.getInstance().getScreenHeight()/3 || bitmap.getWidth() >VueApplication.getInstance().getScreenWidth() ) {
      		  Log.i("width & height", "reqWidth1: Bitmap is greater than card size2" );
      		 bitmap =  Utils.getScaledBitMap(bitmap,(VueApplication.getInstance().getScreenWidth() * 10)/100,  (VueApplication.getInstance().getScreenHeight()* 10)/100);
      	 // }
    	}
        return bitmap;
    }
}