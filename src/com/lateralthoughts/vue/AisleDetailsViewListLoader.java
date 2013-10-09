package com.lateralthoughts.vue;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.volley.toolbox.ImageLoader;
import com.lateralthoughts.vue.TrendingAislesGenericAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.ImageDimension;
import com.lateralthoughts.vue.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class AisleDetailsViewListLoader {
    private static final boolean DEBUG = false;
    private static final String TAG = "AisleDetailsViewListLoader";
    Handler handler = new Handler();
   // private Context mContext;
    private ContentAdapterFactory mContentAdapterFactory;
    
    private static AisleDetailsViewListLoader sAisleDetailsViewLoaderInstance = null;
    private ScaledImageViewFactory mViewFactory = null;
    private BitmapLoaderUtils mBitmapLoaderUtils;
    private HashMap<String, ViewHolder> mContentViewMap = new HashMap<String, ViewHolder>();
   // private ImageDimension mImageDimension;
    private int mBestHeight = 0;
    AisleContentBrowser contentBrowser = null;
    DetailClickListener mDetailListener;
    
   /* public static AisleDetailsViewListLoader getInstance(Context context){
        if(null == sAisleDetailsViewLoaderInstance){
            sAisleDetailsViewLoaderInstance = new AisleDetailsViewListLoader(context);
        }
        return sAisleDetailsViewLoaderInstance;        
    }*/
    
    AisleDetailsViewListLoader(Context context){
        //we don't want everyone creating an instance of this. We will
        //instead use a factory pattern and return an instance using a
        //static method
       // mContext = context;
        mViewFactory = ScaledImageViewFactory.getInstance(VueApplication.getInstance());
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mContentAdapterFactory = ContentAdapterFactory.getInstance(VueApplication.getInstance());
        DisplayMetrics dm = VueApplication.getInstance().getResources().getDisplayMetrics();
       //mBitmapLoaderUtils.clearCache();
        if(DEBUG) Log.e(TAG,"Log something to remove warning");
    }
    public void getAisleContentIntoView(AisleDetailsViewAdapter.ViewHolder holder,
            int scrollIndex, int position,DetailClickListener detailListener,AisleWindowContent windowContent,boolean setPosistion){
    	 mBestHeight = 0;
    	 mDetailListener = detailListener;
        ScaleImageView imageView = null;
        ArrayList<AisleImageDetails> imageDetailsArr = null;
        AisleImageDetails itemDetails = null;
        
        if(null == holder)
            return;
     //   AisleWindowContent windowContent = holder.mWindowContent;
        if(null == windowContent)
            return;
        String desiredContentId = windowContent.getAisleId();

        contentBrowser = holder.aisleContentBrowser;
   
        contentBrowser.setHolderName(VueAisleDetailsViewFragment.SCREEN_NAME);
        if(holder.uniqueContentId.equals(desiredContentId)){
            //we are looking at a visual object that has either not been used
            //before or has to be filled with same content. Either way, no need
            //to worry about cleaning up anything!
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            if(setPosistion){
            	 holder.aisleContentBrowser.setCurrentImage();
            }
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
            adapter.setContentSource(desiredContentId,  windowContent);
           // adapter.setSourceName(holder.tag);
            holder.aisleContentBrowser.setmSourceName(holder.tag);
            holder.aisleContentBrowser.removeAllViews();
            holder.aisleContentBrowser.setUniqueId(desiredContentId);
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            holder.aisleContentBrowser.setCustomAdapter(adapter);
            holder.aisleContentBrowser.setDetailImageClickListener(detailListener);
            holder.uniqueContentId = desiredContentId;
        } 
        Log.i("cardHeight", "cardHeight bestHeight1: "+windowContent.getBestHeightForWindow());
        imageDetailsArr = windowContent.getImageList();
		if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
			 
			 for(int i = 0;i<imageDetailsArr.size();i++) {
				 Log.i("clickedwindow", "TrendingCrop3: width**" + imageDetailsArr.get(i).mAvailableWidth+" height: "+imageDetailsArr.get(i).mAvailableHeight);
				 Log.i("clickedwindow", "TrendingCrop3: imageUrl**" +imageDetailsArr.get(i).mImageUrl);
				  if(mBestHeight < imageDetailsArr.get(i).mAvailableHeight) {
					  mBestHeight = imageDetailsArr.get(i).mAvailableHeight;
				  }
				 
			 }
			holder.aisleContentBrowser.mSwipeListener
					.onReceiveImageCount(imageDetailsArr.size());
			itemDetails = imageDetailsArr.get(0);
			imageView = mViewFactory.getEmptyImageView();
			imageView.setContainerObject(holder);
			Bitmap bitmap = null; 
			bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mImageUrl);
			mBestHeight = getBestHeight(windowContent.getBestLargetHeightForWindow());
			contentBrowser.addView(imageView);
			Log.i("new image", "new image  windowbestHeight:  "+windowContent.getBestLargetHeightForWindow());
			setParams(holder.aisleContentBrowser, imageView, windowContent.getBestLargetHeightForWindow());
			if (bitmap != null) {
				// get the dimensions of the image.
	/*			mImageDimension = Utils.getScalledImage(bitmap,
						itemDetails.mAvailableWidth,
						itemDetails.mAvailableHeight);
				Log.i("window",
						"clickedwindow ID bitmap Height1: "
								+ bitmap.getHeight());
				Log.i("window", "clickedwindow ID  required height1: "
						+ mImageDimension.mImgHeight);
				mBestHeight = mImageDimension.mImgHeight;*/
				
				
				if (bitmap.getHeight() > mBestHeight) {
					loadBitmap(itemDetails, contentBrowser, imageView,
							mBestHeight);

				} else {
					Log.i("setparam", "setparam cache: "+bitmap.getHeight());
					//setParams(holder.aisleContentBrowser, imageView, bitmap.getHeight());
				}
				Log.i("TrendingCrop", "TrendingCrop3:*********************");
				Log.i("TrendingCrop", "TrendingCrop3: Screen Height "
						+ VueApplication.getInstance().getScreenHeight());
				Log.i("TrendingCrop", "TrendingCrop3: Screen Width "
						+ VueApplication.getInstance().getScreenWidth());
				Log.i("TrendingCrop", "TrendingCrop3: Item Height "
						+ itemDetails.mAvailableHeight);
				Log.i("TrendingCrop", "TrendingCrop3: Item Width "
						+ itemDetails.mAvailableWidth);
				Log.i("TrendingCrop", "TrendingCrop3:mBestHeight "
						+ mBestHeight);
				Log.i("TrendingCrop",
						"TrendingCrop3:##########################");

				imageView.setImageBitmap(bitmap);
				//contentBrowser.addView(imageView);
				if (scrollIndex != 0) {
					contentBrowser.setCurrentImage();
				}
			} else {
				
				loadBitmap(itemDetails, contentBrowser, imageView,
						mBestHeight);
				//contentBrowser.addView(imageView);
				if(scrollIndex != 0){
					contentBrowser.setCurrentImage();
					}
			}
		}        
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, AisleContentBrowser flipper, ImageView imageView, int bestHeight) {
    	String loc = itemDetails.mImageUrl;
    	String serverImageUrl = itemDetails.mImageUrl;
 
      /*  ((ScaleImageView) imageView).setImageUrl(serverImageUrl,
                new ImageLoader(VueApplication.getInstance().getRequestQueue(), VueApplication.getInstance().getBitmapCache()));*/


          if (cancelPotentialDownload(loc, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(itemDetails,flipper, imageView, bestHeight);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            String imagesArray[] = {loc, serverImageUrl};
            task.execute(imagesArray);
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
            Log.i("added url", "added url  listloader "+url);
            //we want to get the bitmap and also add it into the memory cache
            bmp = mBitmapLoaderUtils.getBitmap(url, params[1],  true, mBestHeight, VueApplication.getInstance().getScreenWidth(),Utils.DETAILS_SCREEN);
            Log.i("window", "TrendingCrop3 height original: "+bmp.getHeight());
			 
            if(bmp != null) {
 
            	 if(bmp.getHeight()> mBestHeight) {
            		 bmp = mBitmapLoaderUtils.getBitmap(url, params[1],  true, mBestHeight, VueApplication.getInstance().getScreenWidth(),Utils.DETAILS_SCREEN);
				 
            	 }
            }
			Log.i("TrendingCrop", "TrendingCrop3: bitmapheight "
					+ bmp.getHeight());
            return bmp;            
        }
        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	 final ImageView imageView = imageViewReference.get();
        	 // setParams( aisleContentBrowser, imageView,bitmap.getHeight());
            if (imageViewReference != null && bitmap != null) {
               
                //final AisleContentBrowser vFlipper = viewFlipperReference.get();
                BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask) {
                 //setParams( aisleContentBrowser, imageView,bitmap.getHeight());
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
    public void clearBrowser(ArrayList<AisleImageDetails> imageList){
	 if (contentBrowser != null) {
			for (int i = 0; i < contentBrowser.getChildCount(); i++) {
				mViewFactory
				.returnUsedImageView((ScaleImageView)contentBrowser
						.getChildAt(i));
			} 
			 mContentAdapterFactory.returnUsedAdapter(contentBrowser.getCustomAdapter());
			 contentBrowser.setCustomAdapter(null);
			contentBrowser.removeAllViews();
			contentBrowser = null;

		} else {
			Log.i("bitmap reclying", "bitmap reclying  contentBrowser is null ");
		}
	
}
   private void setParams(AisleContentBrowser vFlipper, ImageView imageView,int imgScreenHeight
          ) {
	   Log.i("imageSize", "imageSize params Height: "+imgScreenHeight);
      if (vFlipper != null && imageView != null) {
         FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        		 VueApplication.getInstance().getScreenWidth(), imgScreenHeight+VueApplication.getInstance().getPixel(12));
         //params.gravity = Gravity.CENTER;
         vFlipper.setLayoutParams(params);
         
       /*  FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT, imgScreenHeight);
         params2.gravity = Gravity.CENTER;
         imageView.setLayoutParams(params2);*/
         mDetailListener.onRefreshAdaptaers();
      } 
       
   }
   private void setMaxHeight(int imageHeight,int imageWidth){
	   int screenHeight = VueApplication.getInstance().getScreenHeight();
	   int screenWidth = VueApplication.getInstance().getScreenWidth();
	    int bestWidht;
	    int bestHeight;
	   if(screenHeight < imageHeight){
		   bestHeight = screenHeight;  
		   bestWidht =  (imageWidth * screenHeight)/imageHeight;
		   
	   }
   }
  private int getBestHeight(int largeHeight) {
	   int screenHeight = VueApplication.getInstance().getScreenHeight();
	   int screenWidth = VueApplication.getInstance().getScreenWidth();
	   if(largeHeight > screenHeight){
		   largeHeight = screenHeight;
	   }
	  
	  return largeHeight;
	  
  }
}