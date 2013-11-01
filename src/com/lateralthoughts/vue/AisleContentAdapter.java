package com.lateralthoughts.vue;

//android imports
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.*;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//java imports
//internal imports

/**
 * The AisleContentAdapter object will be associated with aisles on a per-aisle
 * basis. Within each aisle's content, this object will manage resources such as
 * determining the number of images per aisle that need to be kept in memory
 * when to fetch these images etc. Here is the general outline: The AisleLoader
 * class is associated with the TrendingAislesPage and will help load up the
 * first images in each of the ViewFlipper. During startup, we don't want to
 * consume more resources (threads, CPU cycles) to download content that is not
 * visible right away. For example, even though an aisle has multiple images
 * these images are visible one at a time. The AisleContentAdapter will instead
 * perform this function. At startup, this object will download either max of N
 * items or the most number of items in the aisle - whichever is lowest. Say an
 * aisle contains 10 images, we will download just N where N is set to 1 or 2 or
 * other number based on trial & error. When the user swipes through images and
 * the ViewFlipper instance will seek the next item from this adapter. At this
 * point, we will adjust the currentPivotIndex and ensure that we have 1 image
 * on either side readily available in the content cache. This should help with
 * performance and also optimize how much we need to do during startup time.
 */
public class AisleContentAdapter implements IAisleContentAdapter {

	// private VueMemoryCache<Bitmap> mContentImagesCache;
	private BitmapLruCache mContentImagesCache;
 
    private ArrayList<AisleImageDetails> mAisleImageDetails;
    private AisleWindowContent mWindowContent;
    
    private ExecutorService mExecutorService;
    private Context mContext;
    private FileCache mFileCache;
    private ScaledImageViewFactory mImageViewFactory;
    private ColorDrawable mColorDrawable;
    private int mCurrentPivotIndex;
    private BitmapLoaderUtils mBitmapLoaderUtils;
   // public String mSourceName;
    private ImageDimension mImageDimension;
    Animation myFadeInAnimation;
    
    public AisleContentAdapter(Context context){
        mContext = context;
        //mContentImagesCache = VueApplication.getInstance().getAisleContentCache();
        mContentImagesCache = BitmapLruCache.getInstance(VueApplication.getInstance());
        mFileCache = VueApplication.getInstance().getFileCache();
        mCurrentPivotIndex = -1;
        mImageViewFactory  = ScaledImageViewFactory.getInstance(mContext);     
        mExecutorService = Executors.newFixedThreadPool(5);
        mColorDrawable = new ColorDrawable(Color.WHITE);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        myFadeInAnimation = AnimationUtils.loadAnimation(VueApplication.getInstance(), R.anim.fadein);
    }
    
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    @Override
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent) {
        // TODO Auto-generated method stub
        mWindowContent = windowContent;
        mAisleImageDetails = mWindowContent.getImageList();
    }
    
    @Override
    public void releaseContentSource() {
        // TODO Auto-generated method stub
        mCurrentPivotIndex = -1;
        mAisleImageDetails.clear();
        mWindowContent = null;
        
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
    
    public int getAisleItemsCount(){
        return mAisleImageDetails.size();
    }
    
    @Override
    public boolean setAisleContent(AisleContentBrowser contentBrowser, int currentIndex, int wantedIndex, 
                                            boolean shiftPivot){
        ScaleImageView imageView = null;
        AisleImageDetails itemDetails = null;
        if(wantedIndex >= mAisleImageDetails.size())
            return false;
        if(0 >= currentIndex && wantedIndex < currentIndex)
            return false;
        if(null != mAisleImageDetails && mAisleImageDetails.size() != 0){         
            itemDetails = mAisleImageDetails.get(wantedIndex);
            imageView = mImageViewFactory.getEmptyImageView();
            Bitmap bitmap = null;
            if(contentBrowser.getmSourceName()!= null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            	 //bitmap = getCachedBitmap(itemDetails.mImageUrl);
            } else {
             // bitmap = getCachedBitmap(itemDetails.mCustomImageUrl);
              Logging.i("imageResize", "imageResize custom from cache 1");
               
            }
            if(bitmap != null){
            	 if(contentBrowser.getmSourceName() != null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            			mImageDimension = Utils.getScalledImage(bitmap, itemDetails.mAvailableWidth, itemDetails.mAvailableHeight);
            			if(bitmap.getHeight() < mImageDimension.mImgHeight) {
            				 Logging.i("detailscrop", "detailscrop resize agian");
            				   loadBitmap(itemDetails,mImageDimension.mImgHeight,contentBrowser, imageView,wantedIndex);
            			}
            	 }
            
                imageView.setImageBitmap(bitmap);
                contentBrowser.addView(imageView);
                 
            }
            else{
            	if(contentBrowser.getmSourceName() != null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            		int bestHeight = mWindowContent.getBestLargetHeightForWindow();
                    contentBrowser.addView(imageView);
            		loadBitmap(itemDetails,bestHeight,contentBrowser, imageView,wantedIndex);
                    /*((NetworkImageView)imageView).setImageUrl(itemDetails.mImageUrl,VueApplication.getInstance().getImageCacheLoader(),
                            itemDetails.mTrendingImageWidth, bestHeight, NetworkImageView.BitmapProfile.ProfileLandingView);*/
            	} else {
            		int bestHeight = mWindowContent.getBestHeightForWindow();
            		 //
                     contentBrowser.addView(imageView);
                    loadBitmap(itemDetails,itemDetails.mTrendingImageHeight,contentBrowser, imageView,wantedIndex);
                    /*((NetworkImageView)imageView).setImageUrl(itemDetails.mImageUrl,VueApplication.getInstance().getImageCacheLoader(),
                            itemDetails.mTrendingImageWidth, itemDetails.mTrendingImageHeight, NetworkImageView.BitmapProfile.ProfileLandingView);*/
                            
            	}
            }
        }
        return true;
    }
    
    public void loadBitmap( AisleImageDetails itemDetails, int bestHeight, AisleContentBrowser flipper, ImageView imageView,int wantedIndex) {
    	String loc = itemDetails.mImageUrl;
    	String serverUrl = itemDetails.mImageUrl;
    	 if(flipper.getmSourceName() != null && flipper.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)){
    		 loc = itemDetails.mImageUrl;
    	 } else {
    		 loc = itemDetails.mCustomImageUrl;
    	 }
        if(flipper.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)){
            ((ScaleImageView) imageView).setImageUrl(loc, VueApplication.getInstance().getImageCacheLoader(),
                    VueApplication.getInstance().getVueDetailsCardWidth(),bestHeight, NetworkImageView.BitmapProfile.ProfileDetailsView);
        }else{
            ((ScaleImageView) imageView).setImageUrl(loc, VueApplication.getInstance().getImageCacheLoader(),
                    VueApplication.getInstance().getVueDetailsCardWidth(),bestHeight, NetworkImageView.BitmapProfile.ProfileLandingView);
        }
    }
}
