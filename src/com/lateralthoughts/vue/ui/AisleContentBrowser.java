package com.lateralthoughts.vue.ui;

import java.util.ArrayList;
import com.lateralthoughts.vue.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.lateralthoughts.vue.AisleLoader;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.IAisleContentAdapter;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class AisleContentBrowser extends ViewFlipper {
    private String mAisleUniqueId;
    private String mSourceName;
    int mCurrentIndex;
    private ImageView mStarIcon;
    TextView mLikesCountView;
    TextView mBookmarksCountView;
    ImageView likesImage;
    private RelativeLayout mSocialCard;
    private MixpanelAPI mixpanel;
    
    public RelativeLayout getmSocialCard() {
        return mSocialCard;
    }
    
    public void setmSocialCard(RelativeLayout mSocialCard) {
        mixpanel = MixpanelAPI.getInstance(VueApplication.getInstance(),
                VueApplication.getInstance().MIXPANEL_TOKEN);
        this.mSocialCard = mSocialCard;
        if (this.mSocialCard != null) {
            mLikesCountView = (TextView) this.mSocialCard
                    .findViewById(R.id.like_count);
            mBookmarksCountView = (TextView) this.mSocialCard
                    .findViewById(R.id.bookmark_count);
            likesImage = (ImageView) this.mSocialCard
                    .findViewById(R.id.like_img);
            final ImageView bookmarkImage = (ImageView) this.mSocialCard
                    .findViewById(R.id.bookmarkImage);
            RelativeLayout shareLayout = (RelativeLayout) this.mSocialCard
                    .findViewById(R.id.share_layout);
            final TextView shareCount = (TextView) this.mSocialCard
                    .findViewById(R.id.share_count);
            final ImageView shareImage = (ImageView) this.mSocialCard
                    .findViewById(R.id.shareImage);
            RelativeLayout bookmarkLayout = (RelativeLayout) this.mSocialCard
                    .findViewById(R.id.bookmark_layout);
            RelativeLayout rateLayout = (RelativeLayout) this.mSocialCard
                    .findViewById(R.id.rate_layout);
            shareLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = mSpecialNeedsAdapter.getShareCount();
                    count = count + 1;
                    shareCount.setText("" + count);
                    //shareImage.setImageResource(R.drawable.share);
                    mSpecialNeedsAdapter.setShareCount(count);
                   // mSpecialNeedsAdapter.setShareIdicator();
                    if (VueLandingPageActivity.landingPageActivity != null) {
                        VueLandingPageActivity landingPage = (VueLandingPageActivity) VueLandingPageActivity.landingPageActivity;
                        landingPage.share(
                                mSpecialNeedsAdapter.getWindowContent(),
                                mCurrentIndex,shareImage);
                        
                        AisleWindowContent windowContext = mSpecialNeedsAdapter
                                .getWindowContent();
                        String imgOwnerId = windowContext.getAisleContext().mAisleOwnerImageURL;
                        String userId = VueTrendingAislesDataModel
                                .getInstance(mContext).getNetworkHandler()
                                .getUserId();
                        boolean isOwner = false;
                        if (imgOwnerId == userId) {
                            isOwner = true;
                        }
                        JSONObject aisleShareProps = new JSONObject();
                        try {
                            aisleShareProps.put("Aisle_Id",
                                    mSpecialNeedsAdapter.getAisleId());
                            aisleShareProps.put("Is Aisle Owner", isOwner);
                            aisleShareProps.put("Owner Name",
                                    windowContext.getAisleContext().mFirstName);
                            aisleShareProps.put("Share Count",
                                    windowContext.getAisleContext().mShareCount);
                            aisleShareProps.put("Images Count", windowContext
                                    .getImageList().size());
                            
                            aisleShareProps.put("Category",
                                    windowContext.getAisleContext().mCategory);
                            aisleShareProps.put(
                                    "Lookingfor",
                                    windowContext.getAisleContext().mLookingForItem);
                            aisleShareProps.put("Occasion",
                                    windowContext.getAisleContext().mOccasion);
                            aisleShareProps.put("Shared From",
                                    "LandingPage Screen");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mixpanel.track("Aisle Shared", aisleShareProps);
                    }
                }
            });
            // like image click function in Trending screen
            rateLayout.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (loginChcecking()) {
                        boolean imageLikeStatus = mSpecialNeedsAdapter
                                .getImageLikeStatus(mCurrentIndex);
                        int likesCount = Integer.parseInt(mSpecialNeedsAdapter
                                .getImageLikesCount(mCurrentIndex));
                        if (imageLikeStatus) {
                            mSpecialNeedsAdapter.setImageLikeStatus(false,
                                    mCurrentIndex);
                            if (likesCount > 0) {
                                likesCount = likesCount - 1;
                                mSpecialNeedsAdapter.setImageLikesCount(
                                        mCurrentIndex, likesCount);
                            }
                            
                            handleLike_Dislike_Events(mSpecialNeedsAdapter
                                    .getAisleId(), mSpecialNeedsAdapter
                                    .getImageId(mCurrentIndex), false,
                                    likesCount);
                            likesImage.setImageResource(R.drawable.heart_dark);
                            mixpanelTrackImgLikeDislike(false, likesCount);
                            
                        } else {
                            mSpecialNeedsAdapter.setImageLikeStatus(true,
                                    mCurrentIndex);
                            likesCount = likesCount + 1;
                            mSpecialNeedsAdapter.setImageLikesCount(
                                    mCurrentIndex, likesCount);
                            handleLike_Dislike_Events(mSpecialNeedsAdapter
                                    .getAisleId(), mSpecialNeedsAdapter
                                    .getImageId(mCurrentIndex), true,
                                    likesCount);
                            likesImage.setImageResource(R.drawable.heart);
                            
                            mixpanelTrackImgLikeDislike(true, likesCount);
                            
                        }
                        if (mLikesCountView != null) {
                            mLikesCountView.setText(String.valueOf(likesCount));
                        }
                        
                    }
                    if (!AisleLoader.trendingSwipeBlock) {
                        int count = mSpecialNeedsAdapter.getAisleItemsCount();
                        if (count > 1) {
                            moveToNextChild();
                        }
                        
                    }
                }
            });
            // bookmrk image clikc function in Trending screen
            bookmarkLayout.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (loginChcecking()) {
                        int mBookmarksCount = mSpecialNeedsAdapter
                                .getBookmarkCount();
                        boolean bookMarkIndicator;
                        if (mSpecialNeedsAdapter.getBookmarkIndicator()) {
                            // deduct the bookmark count by one.
                            bookMarkIndicator = false;
                            bookmarkImage
                                    .setImageResource(R.drawable.save_dark_small);
                            if (mBookmarksCount > 0) {
                                mBookmarksCount = mBookmarksCount - 1;
                            }
                        } else {
                            // increase the bookmark count by one.
                            bookMarkIndicator = true;
                            bookmarkImage.setImageResource(R.drawable.save);
                            mBookmarksCount = mBookmarksCount + 1;
                        }
                        mSpecialNeedsAdapter
                                .setAisleBookmarkIndicator(bookMarkIndicator);
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler()
                                .modifyBookmarkList(
                                        mSpecialNeedsAdapter.getAisleId(),
                                        bookMarkIndicator);
                        mSpecialNeedsAdapter.setBookmarkCount(mBookmarksCount);
                        mBookmarksCountView.setText("" + mBookmarksCount);
                        handleBookmark(bookMarkIndicator,
                                mSpecialNeedsAdapter.getAisleId());
                        /*
                         * JSONObject aisleBookmarkedProps = new JSONObject();
                         * try { aisleBookmarkedProps.put("Aisle_Id",
                         * mSpecialNeedsAdapter.getAisleId());
                         * aisleBookmarkedProps.put( "Bookmarked From",
                         * "LandingPage Screen"); } catch (JSONException e) {
                         * e.printStackTrace(); }
                         * mixpanel.track("Aisle Bookmarked",
                         * aisleBookmarkedProps);
                         */
                        
                        AisleWindowContent windowContext = mSpecialNeedsAdapter
                                .getWindowContent();
                        String imgOwnerId = windowContext.getAisleContext().mAisleOwnerImageURL;
                        String userId = VueTrendingAislesDataModel
                                .getInstance(mContext).getNetworkHandler()
                                .getUserId();
                        boolean isOwner = false;
                        if (imgOwnerId == userId) {
                            isOwner = true;
                        }
                        JSONObject aisleUnbookmarkProps = new JSONObject();
                        try {
                            
                            aisleUnbookmarkProps.put("AisleId",
                            
                            windowContext.getAisleId());
                            aisleUnbookmarkProps.put("Is Aisle Owner", isOwner);
                            aisleUnbookmarkProps.put("Owner Name",
                                    windowContext.getAisleContext().mFirstName);
                            aisleUnbookmarkProps.put("Share Count",
                                    windowContext.getAisleContext().mShareCount);
                            aisleUnbookmarkProps.put("Images Count",
                                    windowContext.getImageList().size());
                            
                            aisleUnbookmarkProps.put("Category",
                                    windowContext.getAisleContext().mCategory);
                            aisleUnbookmarkProps.put(
                                    "Lookingfor",
                                    windowContext.getAisleContext().mLookingForItem);
                            aisleUnbookmarkProps.put("Occasion",
                            
                            windowContext.getAisleContext().mOccasion);
                            aisleUnbookmarkProps.put("Bookmarked From",
                                    "LandingPage Screen");
                            
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mixpanel.track("Aisle Bookmarked", aisleUnbookmarkProps);
                    }
                }
            });
        }
    }
    
    private void mixpanelTrackImgLikeDislike(boolean isliked, int likesCount) {
        JSONObject aisleLikedProps = new JSONObject();
        try {
            AisleWindowContent ailseWindow = mSpecialNeedsAdapter
                    .getWindowContent();
            String imgOwnerId = ailseWindow.getAisleContext().mAisleOwnerImageURL;
            String userId = VueTrendingAislesDataModel.getInstance(mContext)
                    .getNetworkHandler().getUserId();
            boolean isOwner = false;
            if (imgOwnerId == userId) {
                isOwner = true;
            }
            
            aisleLikedProps.put("Image Id",
                    mSpecialNeedsAdapter.getImageId(mCurrentIndex));
            aisleLikedProps.put("Aisle Id", mSpecialNeedsAdapter.getAisleId());
            aisleLikedProps.put("Image Position", mCurrentIndex);
            aisleLikedProps.put("Is Aisle Owner", isOwner);
            aisleLikedProps.put("Owner Name",
                    ailseWindow.getAisleContext().mFirstName);
            aisleLikedProps.put("Like Count", likesCount);
            aisleLikedProps.put("Category",
                    ailseWindow.getAisleContext().mCategory);
            aisleLikedProps.put("Looking For",
                    ailseWindow.getAisleContext().mLookingForItem);
            aisleLikedProps.put("Occasion",
                    ailseWindow.getAisleContext().mOccasion);
            aisleLikedProps.put("Liked From", "Landing Screen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
            String userName = storedVueUser.getFirstName() + " "
                    + storedVueUser.getLastName();
            if (isliked) {
                aisleLikedProps.put("Image Liked By", userName);
                mixpanel.track("Image Liked", aisleLikedProps);
            } else if (!isliked) {
                aisleLikedProps.put("Image Unliked By", userName);
                mixpanel.track("Image Unliked", aisleLikedProps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public ImageView getmStarIcon() {
        return mStarIcon;
    }
    
    public void setmStarIcon(ImageView mStarIcon) {
        this.mStarIcon = mStarIcon;
    }
    
    public String getmSourceName() {
        return mSourceName;
    }
    
    public void setmSourceName(String mSourceName) {
        this.mSourceName = mSourceName;
    }
    
    private int mScrollIndex;
    private Context mContext;
    
    public boolean mAnimationInProgress;
    private int mDebugTapCount = 0;
    private long mDownPressStartTime = 0;
    private final int MAX_ELAPSED_DURATION_FOR_TAP = 200;
    public static final int SWIPE_MIN_DISTANCE = 30;
    private IAisleContentAdapter mSpecialNeedsAdapter;
    
    public int mFirstX;
    public int mLastX;
    public int mFirstY;
    public int mLastY;
    private boolean mTouchMoved;
    private int mTapTimeout;
    private String holderName;
    private String mBrowserArea;
    private boolean mSetPosition;
    public boolean isLeft;
    public boolean isRight;
    
    public String getmBrowserArea() {
        return mBrowserArea;
    }
    
    public void setmBrowserArea(String mBrowserArea) {
        this.mBrowserArea = mBrowserArea;
    }
    
    public String getHolderName() {
        return holderName;
    }
    
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
    
    private GestureDetector mDetector;
    
    public AisleContentBrowser(Context context) {
        super(context);
        mContext = context;
        mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
        mScrollIndex = 0;
    }
    
    public AisleContentBrowser(Context context, AttributeSet attribs) {
        super(context, attribs);
        mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
        mScrollIndex = 0;
        mAnimationInProgress = false;
        mContext = context;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        mTapTimeout = ViewConfiguration.getTapTimeout();
        this.setBackgroundColor(Color.WHITE);
        mDetector = new GestureDetector(AisleContentBrowser.this.getContext(),
                new mListener());
    }
    
    public void setUniqueId(String id) {
        mAisleUniqueId = id;
    }
    
    public String getUniqueId() {
        return mAisleUniqueId;
    }
    
    public void setScrollIndex(int scrollIndex) {
        mScrollIndex = scrollIndex;
        mCurrentIndex = scrollIndex;
    }
    
    public int getScrollIndex() {
        return mScrollIndex;
    }
    
    public int getCurrentIndex() {
        return mCurrentIndex;
    }
    
    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser) this;
        
        boolean result = mDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCurrentIndex = aisleContentBrowser
                    .indexOfChild(aisleContentBrowser.getCurrentView());
            mAnimationInProgress = false;
            mFirstX = (int) event.getX();
            mFirstY = (int) event.getY();
            mDownPressStartTime = System.currentTimeMillis();
            return super.onTouchEvent(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mTouchMoved) {
                mTouchMoved = false;
                return true;
            }
            mAnimationInProgress = false;
            
            mFirstX = 0;
            mLastX = 0;
            return super.onTouchEvent(event);
        }
        
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mLastX = (int) event.getX();
            mLastY = (int) event.getY();
            if (mFirstY - mLastY > SWIPE_MIN_DISTANCE
                    || mLastY - mFirstY > SWIPE_MIN_DISTANCE) {
                return super.onTouchEvent(event);
            }
            if (mFirstX - mLastX > SWIPE_MIN_DISTANCE) {
                VueApplication.getInstance().isUserSwipeAisle = true;
                // In this case, the user is moving the finger right to left
                // The current image needs to slide out left and the "next"
                // image
                // needs to fade in
                mTouchMoved = true;
                requestDisallowInterceptTouchEvent(true);
                if (false == mAnimationInProgress) {
                    View nextView = null;
                    final int currentIndex = aisleContentBrowser
                            .indexOfChild(aisleContentBrowser.getCurrentView());
                    nextView = (ScaleImageView) aisleContentBrowser
                            .getChildAt(currentIndex + 1);
                    if (null != mSpecialNeedsAdapter && null == nextView) {
                        
                        if (!mSpecialNeedsAdapter.setAisleContent(
                                AisleContentBrowser.this, currentIndex,
                                currentIndex + 1, true)) {
                            mAnimationInProgress = true;
                            
                            Animation cantWrapRight = AnimationUtils
                                    .loadAnimation(mContext,
                                            R.anim.cant_wrap_right);
                            cantWrapRight
                                    .setAnimationListener(new Animation.AnimationListener() {
                                        public void onAnimationEnd(
                                                Animation animation) {
                                            Animation cantWrapRightPart2 = AnimationUtils
                                                    .loadAnimation(
                                                            mContext,
                                                            R.anim.cant_wrap_right2);
                                            aisleContentBrowser
                                                    .getCurrentView()
                                                    .startAnimation(
                                                            cantWrapRightPart2);
                                            if (mSwipeListener != null) {
                                                
                                                mSwipeListener
                                                        .onAllowListResponse();
                                            }
                                            
                                        }
                                        
                                        public void onAnimationStart(
                                                Animation animation) {
                                            
                                        }
                                        
                                        public void onAnimationRepeat(
                                                Animation animation) {
                                            
                                        }
                                    });
                            aisleContentBrowser.getCurrentView()
                                    .startAnimation(cantWrapRight);
                            return super.onTouchEvent(event);
                        }
                    }
                    
                    Animation currentGoLeft = AnimationUtils.loadAnimation(
                            mContext, R.anim.right_out);
                    final Animation nextFadeIn = AnimationUtils.loadAnimation(
                            mContext, R.anim.fade_in);
                    mAnimationInProgress = true;
                    aisleContentBrowser.setInAnimation(nextFadeIn);
                    aisleContentBrowser.setOutAnimation(currentGoLeft);
                    currentGoLeft
                            .setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationEnd(Animation animation) {
                                    if (mSwipeListener != null) {
                                        
                                        mSwipeListener.onAllowListResponse();
                                    }
                                    if (mSwipeListener != null) {
                                        /*
                                         * mSwipeListener .onAisleSwipe(
                                         * VueAisleDetailsViewFragment
                                         * .SWIPE_LEFT_TO_RIGHT, currentIndex +
                                         * 1);
                                         */
                                        
                                    }
                                    mCurrentIndex = currentIndex + 1;
                                    if (detailImgClickListenr != null) {
                                        detailImgClickListenr
                                                .onImageSwipe(currentIndex + 1);
                                    }
                                    
                                    if (null != mSpecialNeedsAdapter) {
                                        String imgLikesCount = mSpecialNeedsAdapter
                                                .getImageLikesCount(currentIndex + 1);
                                        if (mLikesCountView != null) {
                                            mLikesCountView
                                                    .setText(imgLikesCount);
                                        }
                                        boolean likeStatus = mSpecialNeedsAdapter
                                                .getImageLikeStatus(currentIndex + 1);
                                        if (likeStatus) {
                                            likesImage
                                                    .setImageResource(R.drawable.heart);
                                        } else {
                                            likesImage
                                                    .setImageResource(R.drawable.heart_dark);
                                        }
                                        if (mSpecialNeedsAdapter
                                                .hasMostLikes(currentIndex + 1)) {
                                            if (mSpecialNeedsAdapter
                                                    .hasSameLikes(currentIndex + 1)) {
                                                mStarIcon
                                                        .setImageResource(R.drawable.vue_star_light);
                                            } else {
                                                mStarIcon
                                                        .setImageResource(R.drawable.vue_star_theme);
                                            }
                                            mStarIcon
                                                    .setVisibility(View.VISIBLE);
                                            
                                        } else {
                                            mStarIcon.setVisibility(View.GONE);
                                        }
                                    }
                                }
                                
                                public void onAnimationStart(Animation animation) {
                                    
                                }
                                
                                public void onAnimationRepeat(
                                        Animation animation) {
                                    
                                }
                            });
                    
                    aisleContentBrowser.setDisplayedChild(currentIndex + 1);
                    // aisleContentBrowser.invalidate();
                    return super.onTouchEvent(event);
                }
            } else if (mLastX - mFirstX > SWIPE_MIN_DISTANCE) {
                VueApplication.getInstance().isUserSwipeAisle = true;
                requestDisallowInterceptTouchEvent(true);
                mTouchMoved = true;
                if (false == mAnimationInProgress) {
                    final int currentIndex = aisleContentBrowser
                            .indexOfChild(aisleContentBrowser.getCurrentView());
                    View nextView = null;
                    nextView = (ScaleImageView) aisleContentBrowser
                            .getChildAt(currentIndex - 1);
                    if (null != mSpecialNeedsAdapter && null == nextView) {
                        
                        if (!mSpecialNeedsAdapter.setAisleContent(
                                AisleContentBrowser.this, currentIndex,
                                currentIndex - 1, true)) {
                            
                            Animation cantWrapLeft = AnimationUtils
                                    .loadAnimation(mContext,
                                            R.anim.cant_wrap_left);
                            
                            cantWrapLeft
                                    .setAnimationListener(new Animation.AnimationListener() {
                                        public void onAnimationEnd(
                                                Animation animation) {
                                            Animation cantWrapLeftPart2 = AnimationUtils
                                                    .loadAnimation(
                                                            mContext,
                                                            R.anim.cant_wrap_left2);
                                            aisleContentBrowser
                                                    .getCurrentView()
                                                    .startAnimation(
                                                            cantWrapLeftPart2);
                                            if (mSwipeListener != null) {
                                                
                                                mSwipeListener
                                                        .onAllowListResponse();
                                            }
                                        }
                                        
                                        public void onAnimationStart(
                                                Animation animation) {
                                            
                                        }
                                        
                                        public void onAnimationRepeat(
                                                Animation animation) {
                                            
                                        }
                                    });
                            aisleContentBrowser.getCurrentView()
                                    .startAnimation(cantWrapLeft);
                            return super.onTouchEvent(event);
                        }
                    }
                    
                    Animation currentGoRight = AnimationUtils.loadAnimation(
                            mContext, R.anim.left_in);
                    final Animation nextFadeIn = AnimationUtils.loadAnimation(
                            mContext, R.anim.fade_in);
                    mAnimationInProgress = true;
                    aisleContentBrowser.setInAnimation(nextFadeIn);
                    aisleContentBrowser.setOutAnimation(currentGoRight);
                    currentGoRight
                            .setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationEnd(Animation animation) {
                                    if (mSwipeListener != null) {
                                        
                                        mSwipeListener.onAllowListResponse();
                                    }
                                    mCurrentIndex = currentIndex - 1;
                                    if (detailImgClickListenr != null) {
                                        detailImgClickListenr
                                                .onImageSwipe(currentIndex - 1);
                                    }
                                    
                                    if (null != mSpecialNeedsAdapter) {
                                        String imgLikesCount = mSpecialNeedsAdapter
                                                .getImageLikesCount(currentIndex - 1);
                                        if (mLikesCountView != null) {
                                            mLikesCountView
                                                    .setText(imgLikesCount);
                                        }
                                        boolean likeStatus = mSpecialNeedsAdapter
                                                .getImageLikeStatus(currentIndex - 1);
                                        if (likeStatus) {
                                            likesImage
                                                    .setImageResource(R.drawable.heart);
                                        } else {
                                            likesImage
                                                    .setImageResource(R.drawable.heart_dark);
                                        }
                                        if (mSpecialNeedsAdapter
                                                .hasMostLikes(currentIndex - 1)) {
                                            if (mSpecialNeedsAdapter
                                                    .hasSameLikes(currentIndex - 1)) {
                                                mStarIcon
                                                        .setImageResource(R.drawable.vue_star_light);
                                            } else {
                                                mStarIcon
                                                        .setImageResource(R.drawable.vue_star_theme);
                                            }
                                            mStarIcon
                                                    .setVisibility(View.VISIBLE);
                                        } else {
                                            mStarIcon.setVisibility(View.GONE);
                                        }
                                    }
                                    // aisleContentBrowser.setDisplayedChild(currentIndex-1);
                                    
                                }
                                
                                public void onAnimationStart(Animation animation) {
                                    
                                }
                                
                                public void onAnimationRepeat(
                                        Animation animation) {
                                    
                                }
                            });
                    aisleContentBrowser.setDisplayedChild(currentIndex - 1);
                    return super.onTouchEvent(event);
                }
            }
        }
        return super.onTouchEvent(event);
    }
    
    public void setCurrentImage() {
        Utils.sAinmate = false;
        for (int i = 0; i < VueApplication.getInstance()
                .getmAisleImgCurrentPos(); i++) {
            mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this, i,
                    i + 1, true);
        }
        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser) this;
        aisleContentBrowser.setDisplayedChild(VueApplication.getInstance()
                .getmAisleImgCurrentPos());
        Utils.sAinmate = true;
        
    }
    
    public void setCustomAdapter(IAisleContentAdapter adapter) {
        mSpecialNeedsAdapter = adapter;
    }
    
    public IAisleContentAdapter getCustomAdapter() {
        return mSpecialNeedsAdapter;
    }
    
    class mListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
                detailImgClickListenr.onImageDoubleTap();
            }
            if (mClickListener != null && null != mSpecialNeedsAdapter) {
                mClickListener.onDoubleTap(mAisleUniqueId);
            }
            return super.onDoubleTap(e);
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (mClickListener != null && null != mSpecialNeedsAdapter) {
                mClickListener.onAisleClicked(mAisleUniqueId,
                        mSpecialNeedsAdapter.getAisleItemsCount(),
                        mCurrentIndex);
            }
            if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
                detailImgClickListenr.onSetBrowserArea(getmBrowserArea());
                detailImgClickListenr.onImageClicked();
            }
            
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
                detailImgClickListenr.onSetBrowserArea(getmBrowserArea());
                detailImgClickListenr.onImageLongPress();
            }
            super.onLongPress(e);
        }
    }
    
    public interface AisleContentClickListener {
        public void onAisleClicked(String id, int count, int currentPosition);
        
        public boolean isFlingCalled();
        
        public boolean isIdelState();
        
        public boolean onDoubleTap(String id);
        
        public void refreshList();
        
        public void hideProgressBar(int count);
        
        public void showProgressBar(int count);
    }
    
    public interface DetailClickListener {
        public void onImageClicked();
        
        public void onImageLongPress();
        
        public void onImageSwipe(int position);
        
        public void onImageDoubleTap();
        
        public void onSetBrowserArea(String area);
        
        public void onRefreshAdaptaers();
    }
    
    DetailClickListener detailImgClickListenr;
    
    public void setDetailImageClickListener(DetailClickListener detailLestener) {
        detailImgClickListenr = detailLestener;
    }
    
    public void setAisleContentClickListener(AisleContentClickListener listener) {
        mClickListener = listener;
    }
    
    public interface AisleDetailSwipeListener {
        public void onAisleSwipe(String id, int position,
                boolean editLayVisibility, boolean starLayVisibility,
                boolean isMostLikedImage);
        
        public void onReceiveImageCount(int count);
        
        public void onResetAdapter();
        
        public void onAddCommentClick(EditText editText, ImageView commentSend,
                FrameLayout editLay, int position, TextView textCount);
        
        public void onDissAllowListResponse();
        
        public void onAllowListResponse();
        
        public void setFindAtText(String findAt);
        
        public void setOccasion(String occasion);
        
        public void hasToShowEditIcon(boolean hasToShow);
        
        public void onEditAisle();
        
        public void onUpdateLikeStatus(boolean editLayVisibility,
                boolean starLayVisibility, boolean isMostLikedImage);
        
        public void onCloseKeyBoard();
        
        public void onImageAddEvent();
        
        public void finishScreen();
        
    }
    
    public void setAisleDetailSwipeListener(
            AisleDetailSwipeListener swipListener) {
        mSwipeListener = swipListener;
    }
    
    public void setReferedObjectsNull() {
        // To relese the refrence objects form the browser to avoid the memory
        // leaks.
        mSwipeListener = null;
        mClickListener = null;
        detailImgClickListenr = null;
    }
    
    private AisleContentClickListener mClickListener;
    public AisleDetailSwipeListener mSwipeListener;
    
    private void handleBookmark(boolean isBookmarked, String aisleId) {
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .getNetworkHandler().modifyBookmarkList(aisleId, isBookmarked);
        AisleBookmark aisleBookmark = new AisleBookmark(null, isBookmarked,
                Long.parseLong(aisleId));
        ArrayList<AisleBookmark> aisleBookmarkList = DataBaseManager
                .getInstance(VueApplication.getInstance())
                .getAllBookmarkAisleIdsList();
        
        for (AisleBookmark b : aisleBookmarkList) {
            if (aisleId.equals(Long.toString(b.getAisleId().longValue()))) {
                aisleBookmark.setId(b.getId());
                
                break;
            }
        }
        
        try {
            AisleManager.getAisleManager().aisleBookmarkUpdate(aisleBookmark);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void handleLike_Dislike_Events(String aisleId, String imageId,
            boolean likeOrDislike, int likesCount) {
        // aisleId,imageId,likesCount,likeStatus
        int likeCountValue = 0;
        if (likeOrDislike) {
            likeCountValue = 2;
        } else {
            likeCountValue = -2;
        }
        Utils.saveUserPoints(VueConstants.USER_LIKES_POINTS, likeCountValue,
                mContext);
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .getNetworkHandler()
                .modifyImageRatedStatus(imageId, likeOrDislike);
        ArrayList<ImageRating> imgRatingList = DataBaseManager.getInstance(
                mContext).getRatedImagesList(aisleId);
        ImageRating mImgRating = new ImageRating();
        mImgRating.setAisleId(Long.parseLong(aisleId));
        mImgRating.setImageId(Long.parseLong(imageId));
        mImgRating.setLiked(likeOrDislike);
        for (ImageRating imgRat : imgRatingList) {
            if (mImgRating.getImageId().longValue() == imgRat.getImageId()
                    .longValue()) {
                mImgRating.setId(imgRat.getId().longValue());
                break;
            }
        }
        try {
            AisleManager.getAisleManager().updateRating(mImgRating, likesCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean loginChcecking() {
        SharedPreferences sharedPreferencesObj = mContext.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean isUserLoggedInFlag = sharedPreferencesObj.getBoolean(
                VueConstants.VUE_LOGIN, false);
        if (isUserLoggedInFlag) {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(mContext,
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (storedVueUser != null && storedVueUser.getId() != null) {
                return true;
            } else {
                Toast.makeText(
                        mContext,
                        mContext.getResources().getString(
                                R.string.vue_server_login_mesg),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.vue_fb_gplus_login_mesg),
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }
    
    public void moveToNextChild() {
        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser) this;
        VueApplication.getInstance().isUserSwipeAisle = true;
        
        // In this case, the user is moving the finger right to left
        // The current image needs to slide out left and the "next"
        // image
        // needs to fade in
        View nextView = null;
        final int currentIndex = aisleContentBrowser
                .indexOfChild(aisleContentBrowser.getCurrentView());
        nextView = (ScaleImageView) aisleContentBrowser
                .getChildAt(currentIndex + 1);
        if (null != mSpecialNeedsAdapter && null == nextView) {
            
            if (!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this,
                    currentIndex, currentIndex + 1, true)) {
                return;
            }
        }
        AisleLoader.sTrendingSwipeCount++;
        if (AisleLoader.sTrendingSwipeCount > 4) {
            AisleLoader.trendingSwipeBlock = true;
        }
        Animation currentGoLeft = AnimationUtils.loadAnimation(mContext,
                R.anim.right_out);
        final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext,
                R.anim.fade_in);
        mAnimationInProgress = true;
        aisleContentBrowser.setInAnimation(nextFadeIn);
        aisleContentBrowser.setOutAnimation(currentGoLeft);
        currentGoLeft.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (mSwipeListener != null) {
                    mSwipeListener.onAllowListResponse();
                }
                
                mCurrentIndex = currentIndex + 1;
                if (detailImgClickListenr != null) {
                    detailImgClickListenr.onImageSwipe(currentIndex + 1);
                }
                
                if (null != mSpecialNeedsAdapter) {
                    String imgLikesCount = mSpecialNeedsAdapter
                            .getImageLikesCount(currentIndex + 1);
                    if (mLikesCountView != null) {
                        mLikesCountView.setText(imgLikesCount);
                    }
                    boolean likeStatus = mSpecialNeedsAdapter
                            .getImageLikeStatus(currentIndex + 1);
                    if (likeStatus) {
                        likesImage.setImageResource(R.drawable.heart);
                    } else {
                        likesImage.setImageResource(R.drawable.heart_dark);
                    }
                    if (mSpecialNeedsAdapter.hasMostLikes(currentIndex + 1)) {
                        if (mSpecialNeedsAdapter.hasSameLikes(currentIndex + 1)) {
                            mStarIcon
                                    .setImageResource(R.drawable.vue_star_light);
                        } else {
                            mStarIcon
                                    .setImageResource(R.drawable.vue_star_theme);
                        }
                        mStarIcon.setVisibility(View.VISIBLE);
                        
                    } else {
                        mStarIcon.setVisibility(View.GONE);
                    }
                }
            }
            
            public void onAnimationStart(Animation animation) {
                
            }
            
            public void onAnimationRepeat(Animation animation) {
                
            }
        });
        aisleContentBrowser.setDisplayedChild(currentIndex + 1);
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                returnViewBack();
                
            }
        }, 500);
        // aisleContentBrowser.invalidate();
        
    }
    
    private void returnViewBack() {
        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser) this;
        final int currentIndex = aisleContentBrowser
                .indexOfChild(aisleContentBrowser.getCurrentView());
        View nextView = null;
        nextView = (ScaleImageView) aisleContentBrowser
                .getChildAt(currentIndex - 1);
        Animation currentGoRight = AnimationUtils.loadAnimation(mContext,
                R.anim.left_in);
        final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext,
                R.anim.fade_in);
        aisleContentBrowser.setInAnimation(nextFadeIn);
        aisleContentBrowser.setOutAnimation(currentGoRight);
        
        if (null != mSpecialNeedsAdapter && null == nextView) {
            
            if (!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this,
                    currentIndex, currentIndex - 1, true)) {
                
                return;
            }
        }
        currentGoRight.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (mSwipeListener != null) {
                    
                    mSwipeListener.onAllowListResponse();
                }
                mCurrentIndex = currentIndex - 1;
                if (detailImgClickListenr != null) {
                    detailImgClickListenr.onImageSwipe(currentIndex - 1);
                }
                
                if (null != mSpecialNeedsAdapter) {
                    String imgLikesCount = mSpecialNeedsAdapter
                            .getImageLikesCount(currentIndex - 1);
                    if (mLikesCountView != null) {
                        mLikesCountView.setText(imgLikesCount);
                    }
                    boolean likeStatus = mSpecialNeedsAdapter
                            .getImageLikeStatus(currentIndex - 1);
                    if (likeStatus) {
                        likesImage.setImageResource(R.drawable.heart);
                    } else {
                        likesImage.setImageResource(R.drawable.heart_dark);
                    }
                    if (mSpecialNeedsAdapter.hasMostLikes(currentIndex - 1)) {
                        if (mSpecialNeedsAdapter.hasSameLikes(currentIndex - 1)) {
                            mStarIcon
                                    .setImageResource(R.drawable.vue_star_light);
                        } else {
                            mStarIcon
                                    .setImageResource(R.drawable.vue_star_theme);
                        }
                        mStarIcon.setVisibility(View.VISIBLE);
                    } else {
                        mStarIcon.setVisibility(View.GONE);
                    }
                }
                // aisleContentBrowser.setDisplayedChild(currentIndex-1);
                
            }
            
            public void onAnimationStart(Animation animation) {
                
            }
            
            public void onAnimationRepeat(Animation animation) {
                
            }
        });
        aisleContentBrowser.setDisplayedChild(currentIndex - 1);
    }
   
}
