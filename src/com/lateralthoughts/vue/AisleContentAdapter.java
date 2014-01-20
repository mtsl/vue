package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;

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
    
    // private BitmapLruCache mContentImagesCache;
    
    private ArrayList<AisleImageDetails> mAisleImageDetails;
    private AisleWindowContent mWindowContent;
    
    private Context mContext;
    private ScaledImageViewFactory mImageViewFactory;
    
    Animation myFadeInAnimation;
    
    // private ImageLoader mImageLoader;
    
    public AisleContentAdapter(Context context) {
        mContext = context;
        mImageViewFactory = ScaledImageViewFactory.getInstance(mContext);
        myFadeInAnimation = AnimationUtils.loadAnimation(
                VueApplication.getInstance(), R.anim.fadein);
    }
    
    // ========================= Methods from the inherited IAisleContentAdapter
    // ========================//
    @Override
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent) {
        mWindowContent = windowContent;
        mAisleImageDetails = mWindowContent.getImageList();
    }
    
    public boolean hasMostLikes(int position) {
        
        return mWindowContent.getImageList().get(position).mHasMostLikes;
    }
    
    public boolean hasSameLikes(int position) {
        return mWindowContent.getImageList().get(position).mSameMostLikes;
    }
    
    public void setImageLikesCount(int position, int count) {
        mWindowContent.getImageList().get(position).mLikesCount = count;
    }
    
    public String getImageLikesCount(int position) {
        int count = mWindowContent.getImageList().get(position).mLikesCount;
        return String.valueOf(count);
    }
    
    public void setBookmarkIndicator(boolean bookmarkIndicator) {
        mWindowContent.setWindowBookmarkIndicator(bookmarkIndicator);
    }
    
    public boolean getBookmarkIndicator() {
        return mWindowContent.getWindowBookmarkIndicator();
    }
    
    public void setAisleBookmarkIndicator(boolean bookmarkIndicator) {
        mWindowContent.setWindowBookmarkIndicator(bookmarkIndicator);
    }
    
    public int getBookmarkCount() {
        return mWindowContent.getAisleContext().mBookmarkCount;
    }
    
    public void setBookmarkCount(int bookmarkCount) {
        mWindowContent.getAisleContext().mBookmarkCount = bookmarkCount;
    }
    
    public boolean getImageLikeStatus(int position) {
        if (mWindowContent.getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
            return true;
        } else {
            return false;
        }
    }
    
    public void setImageLikeStatus(boolean status, int position) {
        if (status) {
            mWindowContent.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
        } else {
            mWindowContent.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
        }
    }
    
    public String getImageId(int position) {
        return mWindowContent.getImageList().get(position).mId;
    }
    
    public String getAisleId() {
        return mWindowContent.getAisleContext().mAisleId;
    }
    
    @Override
    public void releaseContentSource() {
        mAisleImageDetails.clear();
        mWindowContent = null;
        
    }
    
    @Override
    public ScaleImageView getItemAt(int index, boolean isPivot) {
        return null;
    }
    
    @Override
    public void setPivot(int index) {
        
    }
    
    @Override
    public void registerAisleDataObserver(IAisleDataObserver observer) {
        
    }
    
    @Override
    public void unregisterAisleDataObserver(IAisleDataObserver observer) {
    }
    
    public int getAisleItemsCount() {
        return mAisleImageDetails.size();
    }
    
    @Override
    public boolean setAisleContent(AisleContentBrowser contentBrowser,
            int currentIndex, int wantedIndex, boolean shiftPivot) {
        ScaleImageView imageView = null;
        AisleImageDetails itemDetails = null;
        if (wantedIndex >= mAisleImageDetails.size())
            return false;
        if (0 >= currentIndex && wantedIndex < currentIndex)
            return false;
        if (null != mAisleImageDetails && mAisleImageDetails.size() != 0) {
            itemDetails = mAisleImageDetails.get(wantedIndex);
            imageView = mImageViewFactory.getEmptyImageView();
            if (contentBrowser.getmSourceName() != null
                    && contentBrowser.getmSourceName().equalsIgnoreCase(
                            AisleDetailsViewAdapterPager.TAG)) {
                int bestHeight = mWindowContent.getBestLargetHeightForWindow();
                loadBitmap(itemDetails, bestHeight, contentBrowser, imageView,
                        wantedIndex);
                contentBrowser.addView(imageView);
            } else {
                loadBitmap(itemDetails, itemDetails.mTrendingImageHeight,
                        contentBrowser, imageView, wantedIndex);
                contentBrowser.addView(imageView);
            }
            
        }
        return true;
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, int bestHeight,
            AisleContentBrowser flipper, ImageView imageView, int wantedIndex) {
        ((NetworkImageView) imageView).setImageUrl(itemDetails.mImageUrl,
                VueApplication.getInstance().getImageCacheLoader(),
                itemDetails.mTrendingImageWidth,
                itemDetails.mTrendingImageHeight,
                NetworkImageView.BitmapProfile.ProfileLandingView);
    }
    
}
