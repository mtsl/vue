package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.TrendingAislesGenericAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;

public class AisleLoader {
    Handler handler = new Handler();
    private Context mContext;
    private ContentAdapterFactory mContentAdapterFactory;
    
    private static AisleLoader sAisleLoaderInstance = null;
    private ScaledImageViewFactory mViewFactory = null;
    AisleContentClickListener mListener;
    
    // private HashMap<String, ViewHolder> mContentViewMap = new HashMap<String,
    // ViewHolder>();
    // private List<ViewHolder> browserList = new
    // ArrayList<TrendingAislesGenericAdapter.ViewHolder>();
    
    // Design Notes: The SGV is powered by data from the TrendingAislesAdapter.
    // This adapter starts
    // the information flow by requesting top aisles in batches. As the aisle
    // details start coming through the adapter notifies the view of changes in
    // data set which in turn triggers the getView() callback.
    // The complexity starts at this point: we are dealing with
    // an incredibly large amount of data. Each aisle window makes up
    // one item in the SGV. Each of this window consists of an image, below
    // which we can description of the image, the profile of the owner, the
    // context,
    // the occasion etc. On top of this, the image itself can be flicked to
    // reveal
    // a carousel of images that a user can swipe through. We can't possibly
    // download all of these and more importantly, we want to have top
    // performance for a couple of very important scenarios:
    // 1. When the user flings the SGV up & down the scrolling needs to be
    // smooth
    // 2. User should be able to swipe across as AisleWindow and browse the
    // content.
    // Here is what we will do:
    // 1. When an AisleWindowContent needs to be loaded up, we also get
    // the view into which it goes. Inside this viewFlipper we will store the
    // id of the AisleWindowContent.
    // 2. When the viewFlipper is being recycled the if of the
    // AisleWindowContent
    // and the id that ViewFlipper points to will be different. At this point
    // we will cancel all image download requests started for this ViewFlipper
    // 3. If the ids match then we don't need to do anything.
    
    // In addition, we should consider keeping a pool of ScaledImageView objects
    // for efficiency. Right now, we are creating ScaledImageView objects
    // everytime
    // we handle getView() and thats definitely hurting us!
    public static AisleLoader getInstance(Context context) {
        if (null == sAisleLoaderInstance) {
            sAisleLoaderInstance = new AisleLoader(context);
        }
        return sAisleLoaderInstance;
    }
    
    private AisleLoader(Context context) {
        // we don't want everyone creating an instance of this. We will
        // instead use a factory pattern and return an instance using a
        // static method
        mContext = context;
        mViewFactory = ScaledImageViewFactory.getInstance(context);
        mContentAdapterFactory = ContentAdapterFactory.getInstance(mContext);
    }
    
    // This method adds the intelligence to fetch the contents of this aisle
    // window and
    // updates the relevant view so that the item is visible to the user.
    // Some caveats: This API is aware of the internals of the View - i.e.,
    // ViewHolder
    // which represents one item in the staggered grid view.
    // This is less than ideal but it doesn't make sense to aritificially
    // constrain this
    // class from being aware of the UI side of things.
    // The logic itself is reasonably simple: given an AisleWindowContent object
    // we first determine
    // the number of images that need to be in this view item. We then need to
    // determine how to
    // load each of the images in a non-intrusive way.
    // I have implemented a relatively robust asynctask pattern for this: for
    // each image view,
    // start an async task and use a standard DownloadedDrawable object to keep
    // track of the task
    // When the task completes check to make sure that the url for which the
    // task was started is still
    // valid. If so, add the downloaded image to the view object
    public void getAisleContentIntoView(ViewHolder holder, int scrollIndex,
            int position, boolean placeholderOnly,
            AisleContentClickListener listener, String whichAdapter,
            ImageView startImageLay) {
        ScaleImageView imageView = null;
        ArrayList<AisleImageDetails> imageDetailsArr = null;
        AisleImageDetails itemDetails = null;
        AisleContentBrowser contentBrowser = null;
        
        if (null == holder)
            return;
        AisleWindowContent windowContent = holder.mWindowContent;
        
        if (null == windowContent)
            return;
        
        // String currentContentId = holder.aisleContentBrowser.getUniqueId();
        
        String desiredContentId = windowContent.getAisleId();
        contentBrowser = holder.aisleContentBrowser;
        
        if (holder.uniqueContentId.equals(desiredContentId)) {
            // we are looking at a visual object that has either not been used
            // before or has to be filled with same content. Either way, no need
            // to worry about cleaning up anything!
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            
            return;
        } else {
            // we are going to re-use an existing object to show some new
            // content
            // lets release the scaleimageviews first
            for (int i = 0; i < contentBrowser.getChildCount(); i++) {
                mViewFactory
                        .returnUsedImageView((ScaleImageView) contentBrowser
                                .getChildAt(i));
            }
            IAisleContentAdapter adapter = mContentAdapterFactory
                    .getAisleContentAdapter();
            mContentAdapterFactory.returnUsedAdapter(holder.aisleContentBrowser
                    .getCustomAdapter());
            holder.aisleContentBrowser.setCustomAdapter(null);
            adapter.setContentSource(desiredContentId, holder.mWindowContent);
            holder.aisleContentBrowser.setCustomAdapter(adapter);
            holder.uniqueContentId = desiredContentId;
            holder.aisleContentBrowser.removeAllViews();
            holder.aisleContentBrowser.setmStarIcon(null);
            holder.aisleContentBrowser.setUniqueId(desiredContentId);
            holder.aisleContentBrowser.setScrollIndex(scrollIndex);
            holder.aisleContentBrowser.setCustomAdapter(adapter);
            if (whichAdapter.equalsIgnoreCase("LeftAdapter")) {
                holder.aisleContentBrowser.isLeft = true;
            } else {
                holder.aisleContentBrowser.isRight = true;
            }
        }
        mListener = listener;
        imageDetailsArr = windowContent.getImageList();
        if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
            ImageView image = (ImageView) startImageLay
                    .findViewById(R.id.staricon);
            holder.aisleContentBrowser.setmStarIcon(image);
            itemDetails = imageDetailsArr.get(0);
            if (itemDetails.mHasMostLikes) {
                
                if (itemDetails.mSameMostLikes) {
                    image.setImageResource(R.drawable.vue_star_light);
                } else {
                    image.setImageResource(R.drawable.vue_star_theme);
                }
                startImageLay.setVisibility(View.VISIBLE);
            } else {
                startImageLay.setVisibility(View.GONE);
            }
            imageView = mViewFactory.getPreconfiguredImageView(position);
            imageView.setContainerObject(holder);
            int bestHeight = windowContent.getBestHeightForWindow();
            FrameLayout.LayoutParams mShowpieceParams2 = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, itemDetails.mTrendingImageHeight);
            contentBrowser.setLayoutParams(mShowpieceParams2);
            String profleUrl = windowContent.getAisleContext().mAisleOwnerImageURL;
            contentBrowser.addView(imageView);
            if (!placeholderOnly) {
                loadBitmap(itemDetails.mCustomImageUrl, itemDetails.mImageUrl,
                        contentBrowser, imageView, bestHeight,
                        windowContent.getAisleId(), itemDetails, listener);
                holder.profileThumbnail.setImageUrl(profleUrl, VueApplication
                        .getInstance().getImageCacheLoader());
            }
        }
    }
    
    public void loadBitmap(String loc, String serverImageUrl,
            AisleContentBrowser flipper, ImageView imageView, int bestHeight,
            String asileId, AisleImageDetails itemDetails,
            AisleContentClickListener listener) {
        ((NetworkImageView) imageView).setImageUrl(itemDetails.mImageUrl,
                VueApplication.getInstance().getImageCacheLoader(),
                itemDetails.mTrendingImageWidth,
                itemDetails.mTrendingImageHeight,
                NetworkImageView.BitmapProfile.ProfileLandingView);
        
    }
}
