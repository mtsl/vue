package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.Utils;

public class AisleDetailsViewListLoader {
    private static final boolean DEBUG = false;
    private static final String TAG = "AisleDetailsViewListLoader";
    Handler handler = new Handler();
   // private Context mContext;
    private ContentAdapterFactory mContentAdapterFactory;
    
    //private static AisleDetailsViewListLoader sAisleDetailsViewLoaderInstance = null;
    private ScaledImageViewFactory mViewFactory = null;
    private BitmapLoaderUtils mBitmapLoaderUtils;
  //  private HashMap<String, ViewHolder> mContentViewMap = new HashMap<String, ViewHolder>();
   // private ImageDimension mImageDimension;
    private int mBestHeight = 0;
    AisleContentBrowser contentBrowser = null;
    DetailClickListener mDetailListener;
    Animation myFadeInAnimation;
    
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
    	myFadeInAnimation = AnimationUtils.loadAnimation(VueApplication.getInstance(), R.anim.fadein);
        mViewFactory = ScaledImageViewFactory.getInstance(VueApplication.getInstance());
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mContentAdapterFactory = ContentAdapterFactory.getInstance(VueApplication.getInstance());
        //DisplayMetrics dm = VueApplication.getInstance().getResources().getDisplayMetrics();
       //mBitmapLoaderUtils.clearCache();
        if(DEBUG) Log.e(TAG,"Log something to remove warning");
    }
    public void getAisleContentIntoView(AisleDetailsViewAdapter.ViewHolder holder,
            int scrollIndex, int position,DetailClickListener detailListener,AisleWindowContent windowContent,boolean setPosistion,/*LinearLayout editImageLay,*/LinearLayout starImageLay){
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
        imageDetailsArr = windowContent.getImageList();
        Log.e("clickedwindow", "clickedwindow Aisle Id : " + windowContent.getAisleId());
		if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
			 
			 for(int i = 0;i<imageDetailsArr.size();i++) {
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
			//bitmap = mBitmapLoaderUtils.getCachedBitmap(itemDetails.mImageUrl);
			mBestHeight =Utils.modifyHeightForDetailsView(imageDetailsArr);
			windowContent.setBestLargestHeightForWindow(mBestHeight);
			contentBrowser.addView(imageView);
			setParams(holder.aisleContentBrowser, imageView,mBestHeight);
			setStarIconParams(windowContent,starImageLay);
			//setEditIconParams(windowContent,editImageLay);
			if (bitmap != null) {
				if (bitmap.getHeight() > mBestHeight) {
					loadBitmap(itemDetails, contentBrowser, imageView,
							mBestHeight,scrollIndex);
				} else {
				}
				imageView.setImageBitmap(bitmap);
				if (scrollIndex != 0) {
					contentBrowser.setCurrentImage();
				}
			} else {
				loadBitmap(itemDetails, contentBrowser, imageView,
						mBestHeight, scrollIndex);
				if(scrollIndex != 0){
					
					contentBrowser.setCurrentImage();
					}
			}
		}        
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, AisleContentBrowser flipper, ImageView imageView, int bestHeight,int scrollIndex) {
    	String loc = itemDetails.mImageUrl;
    	String serverImageUrl = itemDetails.mImageUrl;
    	
 
      /*  ((ScaleImageView) imageView).setImageUrl(serverImageUrl,
                new ImageLoader(VueApplication.getInstance().getRequestQueue(), VueApplication.getInstance().getBitmapCache()));*/

         // if (cancelPotentialDownload(loc, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(itemDetails,flipper, imageView, bestHeight,scrollIndex);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            String imagesArray[] = {loc, serverImageUrl};
            task.execute(imagesArray);
       // }
 
    }
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        //private final WeakReference<AisleContentBrowser>viewFlipperReference;
        private String url = null;
        private int mBestHeight;
        AisleContentBrowser aisleContentBrowser ;
        int mAvailabeWidth,mAvailableHeight;
        AisleImageDetails mItemDetails;
        int mScrollIndex;
        

        public BitmapWorkerTask(AisleImageDetails itemDetails,AisleContentBrowser vFlipper, ImageView imageView, int bestHeight,int scrollIndex) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mBestHeight = bestHeight;
            aisleContentBrowser = vFlipper;
            mAvailabeWidth = itemDetails.mAvailableWidth;
            mAvailableHeight = itemDetails.mAvailableHeight;
            mItemDetails = itemDetails;
            mScrollIndex = scrollIndex;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            url = params[0];
            Bitmap bmp = null; 
            //we want to get the bitmap and also add it into the memory cache
            boolean cacheBitmap = false;
            bmp = mBitmapLoaderUtils.getBitmap(url, params[1],  cacheBitmap, mBestHeight, VueApplication.getInstance().getVueDetailsCardWidth(),Utils.DETAILS_SCREEN);
			if(bmp != null){
            mItemDetails.mTempResizeBitmapwidth = bmp.getWidth();
			mItemDetails.mTempResizedBitmapHeight = bmp.getHeight();
			} else {
			}
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
                    if(mScrollIndex == 0){
                    imageView.startAnimation(myFadeInAnimation);
                    mDetailListener.onRefreshAdaptaers();
                    }
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
			    
                 try{
				ImageView image = (ScaleImageView)contentBrowser
				.getChildAt(i);
				Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
				bitmap.recycle();
				bitmap = null;
                 }catch (Exception e) {
					 
				}

				
				mViewFactory
				.returnUsedImageView((ScaleImageView)contentBrowser
						.getChildAt(i));
			} 
			 mContentAdapterFactory.returnUsedAdapter(contentBrowser.getCustomAdapter());
			 contentBrowser.setCustomAdapter(null);
			contentBrowser.removeAllViews();
			contentBrowser = null;

		} else {
		}
	
}
   private void setParams(AisleContentBrowser vFlipper, ImageView imageView,int imgScreenHeight
          ) {
      if (vFlipper != null && imageView != null) {
         FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        		 VueApplication.getInstance().getScreenWidth(), imgScreenHeight+VueApplication.getInstance().getPixel(12));
         params.gravity = Gravity.CENTER;
         vFlipper.setLayoutParams(params);
         
       /*  FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT, imgScreenHeight);
         params2.gravity = Gravity.CENTER;
         imageView.setLayoutParams(params2);*/
         mDetailListener.onRefreshAdaptaers();
      } 
       
   }
  /**
   * 
   * @param windowContent AisleWindowContent
   * @param editImageLay LinearLayout
   * set the params for the editImage layout on the image displayed in the detailsview.
   */
  private void setEditIconParams(AisleWindowContent windowContent,LinearLayout editImageLay){
	  if( null == windowContent || null == windowContent){
		  return;
	  }
		int browserHeight = mBestHeight;
		int browserWidth = VueApplication.getInstance()
				.getVueDetailsCardWidth();
		int imageHeight =windowContent
				.getImageList().get(VueApplication.getInstance().getmAisleImgCurrentPos()).mDetailsImageHeight;
		int imageWidth = windowContent
				.getImageList().get(VueApplication.getInstance().getmAisleImgCurrentPos()).mDetailsImageWidth;
		int imageRightSpace = browserWidth - imageWidth;
		int imageTopAreaHeight = (browserHeight / 2)
				- (imageHeight / 2);
		FrameLayout.LayoutParams editIconParams = new FrameLayout.LayoutParams(
				VueApplication.getInstance().getPixel(32),
				VueApplication.getInstance().getPixel(32));
		editIconParams.setMargins(0, VueApplication
				.getInstance().getPixel(10)
				+ imageTopAreaHeight, VueApplication
				.getInstance().getPixel(4)
				+ imageRightSpace / 2, 0);
		editIconParams.gravity = Gravity.RIGHT;
		editImageLay
				.setLayoutParams(editIconParams);
  }
  private void setStarIconParams(AisleWindowContent windowContent,LinearLayout starImageLay){
	  if( null == windowContent || null == windowContent){
		  return;
	  }
		int browserHeight = mBestHeight;
		int browserWidth = VueApplication.getInstance()
				.getVueDetailsCardWidth();
		int imageHeight =windowContent
				.getImageList().get(VueApplication.getInstance().getmAisleImgCurrentPos()).mDetailsImageHeight;
		int imageWidth = windowContent
				.getImageList().get(VueApplication.getInstance().getmAisleImgCurrentPos()).mDetailsImageWidth;
		int imageRightSpace = browserWidth - imageWidth;
		int imageTopAreaHeight = (browserHeight / 2)
				- (imageHeight / 2);
		FrameLayout.LayoutParams starIconParams = new FrameLayout.LayoutParams(
				VueApplication.getInstance().getPixel(32),
				VueApplication.getInstance().getPixel(32));
		starIconParams.setMargins(VueApplication
				.getInstance().getPixel(4)
				+ imageRightSpace / 2, VueApplication
				.getInstance().getPixel(10)
				+ imageTopAreaHeight,0,0);
	 
		starImageLay
				.setLayoutParams(starIconParams);
  }
}