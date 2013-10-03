package com.lateralthoughts.vue;

import android.content.Context;
import android.graphics.Bitmap;
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
    private ImageDimension mImageDimension;
    private int mBestHeight = 0;
    AisleContentBrowser contentBrowser = null;
    
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
    	 Log.i("currentimage", "currentimage: getAisleContentIntoView1 " );
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
            Log.i("currentimage", "currentimage: setPosistion1 " ); 
            if(setPosistion){
            	Log.i("currentimage", "currentimage: setPosistion2 " ); 
            	 holder.aisleContentBrowser.setCurrentImage();
            }
            return;
        }else{
        	 Log.i("currentimage", "currentimage: getAisleContentIntoView2 else part " );
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
		if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
			 
			 for(int i = 0;i<imageDetailsArr.size();i++) {
				 Log.i("clickedwindow", "clickedwindow ID***: width" + imageDetailsArr.get(i).mAvailableWidth+" height: "+imageDetailsArr.get(i).mAvailableHeight);
				 Log.i("clickedwindow", "clickedwindow ID**: imageUrl" +imageDetailsArr.get(i).mImageUrl);
				  if(mBestHeight > imageDetailsArr.get(i).mAvailableHeight) {
					  mBestHeight = imageDetailsArr.get(i).mAvailableHeight;
				  }
				 
			 }
			 Log.i("clickedwindow", "clickedwindow ID***: bestHeight : " +windowContent.getBestHeightForWindow());
		     setParams(holder.aisleContentBrowser, imageView, mBestHeight);
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
			Bitmap bitmap = null; //mBitmapLoaderUtils.getCachedBitmap(itemDetails.mImageUrl);
			
			
			if (bitmap != null) {
				// get the dimensions of the image.
				mImageDimension = Utils.getScalledImage(bitmap,
						itemDetails.mAvailableWidth,
						itemDetails.mAvailableHeight);
				Log.i("window", "clickedwindow ID bitmap Height1: "+bitmap.getHeight());
				Log.i("window", "clickedwindow ID  required height1: "+mImageDimension.mImgHeight);
				mBestHeight = mImageDimension.mImgHeight;
				setParams(holder.aisleContentBrowser, imageView, mBestHeight);
				if (bitmap.getHeight() < mImageDimension.mImgHeight) {
					loadBitmap(itemDetails, contentBrowser, imageView,
							itemDetails.mAvailableHeight);
			 
				}
	 
				 
				imageView.setImageBitmap(bitmap);
				contentBrowser.addView(imageView);
				if(scrollIndex != 0){
				contentBrowser.setCurrentImage();
				}
			} else {
				contentBrowser.addView(imageView);
				loadBitmap(itemDetails, contentBrowser, imageView,
						itemDetails.mAvailableHeight);
				if(scrollIndex != 0){
					contentBrowser.setCurrentImage();
					}
			}
		}        
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, AisleContentBrowser flipper, ImageView imageView, int bestHeight) {
    	String loc = itemDetails.mImageUrl;
    	String serverImageUrl = itemDetails.mImageUrl;
        ((ScaleImageView) imageView).setImageUrl(serverImageUrl,
                new ImageLoader(VueApplication.getInstance().getRequestQueue(), VueApplication.getInstance().getBitmapCache()));
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
               LayoutParams.MATCH_PARENT, imgScreenHeight);
         params.gravity = Gravity.CENTER;
         imageView.setLayoutParams(params);
      } 
       
   }
}