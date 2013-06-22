package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

//import com.lateralthoughts.vue.TrendingAislesAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
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

    public void getAisleContentIntoView(AisleDetailsViewAdapter.ViewHolder holder,
            int scrollIndex, int position){
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
            Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mCustomImageUrl);
            if(bitmap != null){
                imageView.setImageBitmap(bitmap);
                contentBrowser.addView(imageView);                  
            }
            else{
                contentBrowser.addView(imageView);
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

        public BitmapWorkerTask(AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mBestHeight = bestHeight;
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
}
