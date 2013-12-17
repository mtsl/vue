/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.VueAisleDetailsViewFragment.ShareViaVueListner;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Comment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.parser.ImageComments;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

@SuppressLint("UseSparseArrays")
public class AisleDetailsViewAdapterPager extends BaseAdapter {
    private Context mContext;
    public static final String TAG = "AisleDetailsViewAdapter";
    public static final int IMG_LIKE_STATUS = 1;
    public static final int IMG_NONE_STATUS = 0;
    private static final String CHANGE_BOOKMARK = "BOOKMARK";
    public static final String CHANGE_COMMENT = "COMMENT";
    private static final String CHANGE_LIKES = "LIKES";
    private AisleDetailSwipeListener mswipeListner;
    private boolean mCloseKeyboard = false;
    private VueUser mStoredVueUser = null;
    public static final int SWIPE_MIN_DISTANCE = 30;
    private LayoutInflater mInflater;
    // we need to customize the layout depending on screen height & width which
    // we will get on the fly
    private int mListCount;
    public int mLikes;
    private int mBookmarksCount;
    public int mCurrentDispImageIndex;
    private boolean mIsLikeImageClicked = false;
    private boolean mIsBookImageClciked = false;
    private int mShowFixedRowCount = 3;
    private int mInitialCommentsToShowSize = 2;
    public String mVueusername;
    ShareDialog mShare;
    public int mCurrentAislePosition;
    public ArrayList<String> mImageDetailsArr = null;
    @SuppressLint("UseSparseArrays")
    Map<Integer, ArrayList<ImageComments>> mCommentsMapList = new HashMap<Integer, ArrayList<ImageComments>>();
    private ArrayList<Comment> mShowingCommentList = new ArrayList<Comment>();
    private int mBestHeight;
    private int mTopBottomMargin = 24;
    private ViewHolder mViewHolder;
    private static final int mWaitTime = 1000;
    private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
    public ArrayList<String> mCustomUrls = new ArrayList<String>();
    private LoginWarningMessage mLoginWarningMessage = null;
    private long mUserId;
    private ImageLoader mImageLoader;
    private ShareViaVueListner mShareViaVueListner;
    private BitmapLoaderUtils mBitmapLoaderUtils;
    private int mPrevPosition;
    private PageListener pageListener;
    private DetailImageClickListener detailsImageClickListenr;
    private Animation myFadeInAnimation;
    private boolean mSetPager = true;
    
    @SuppressWarnings("unchecked")
    public AisleDetailsViewAdapterPager(Context c,
            AisleDetailSwipeListener swipeListner, int listCount,
            ArrayList<AisleWindowContent> content,
            ShareViaVueListner shareViaVueListner) {
        mShareViaVueListner = shareViaVueListner;
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance());
        mCurrentDispImageIndex = VueApplication.getInstance()
                .getmAisleImgCurrentPos();
        mContext = c;
        myFadeInAnimation = AnimationUtils.loadAnimation(
                VueApplication.getInstance(), R.anim.fadein);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mImageLoader = new ImageLoader(VueApplication.getInstance()
                .getRequestQueue(), BitmapLruCache.getInstance(mContext));
        mTopBottomMargin = VueApplication.getInstance().getPixel(
                mTopBottomMargin);
        pageListener = new PageListener();
        mswipeListner = swipeListner;
        mListCount = listCount;
        if (VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getUserId() != null) {
            mUserId = Long.parseLong(VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler().getUserId());
        }
        
        mShowingCommentList = new ArrayList<Comment>();
        
        for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
            if (getItem(i).getAisleId().equalsIgnoreCase(
                    VueApplication.getInstance().getClickedWindowID())) {
                mCurrentAislePosition = i;
                break;
            }
        }
        if (VueApplication.getInstance().getmAisleImgCurrentPos() > getItem(
                mCurrentAislePosition).getImageList().size() - 1) {
            VueApplication.getInstance().setmAisleImgCurrentPos(
                    getItem(mCurrentAislePosition).getImageList().size() - 1);
        }
        if (getItem(mCurrentAislePosition).getAisleContext().mCommentList == null) {
            getItem(mCurrentAislePosition).getAisleContext().mCommentList = new ArrayList<String>();
        }
        
        setImageRating();
        if (getItem(mCurrentAislePosition) != null) {
            String occasion = getItem(mCurrentAislePosition).getAisleContext().mOccasion;
            if (occasion != null) {
                if (occasion.length() > 0) {
                    occasion = occasion.substring(0, 1).toUpperCase()
                            + occasion.substring(1).toLowerCase();
                }
                String lookingFor = getItem(mCurrentAislePosition)
                        .getAisleContext().mLookingForItem;
                lookingFor = lookingFor.substring(0, 1).toUpperCase()
                        + lookingFor.substring(1).toLowerCase();
                mswipeListner.setOccasion(occasion + " " + lookingFor);
            }
            
            mBookmarksCount = getItem(mCurrentAislePosition).getAisleContext().mBookmarkCount;
            getItem(mCurrentAislePosition).setmAisleBookmarksCount(
                    mBookmarksCount);
            VueApplication.getInstance().setClickedWindowCount(
                    getItem(mCurrentAislePosition).getImageList().size());
            
            for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                    .size(); i++) {
                mCustomUrls.add(getItem(mCurrentAislePosition).getImageList()
                        .get(i).mCustomImageUrl);
                if (getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight > mBestHeight) {
                    mBestHeight = getItem(mCurrentAislePosition).getImageList()
                            .get(i).mAvailableHeight;
                }
                
                mCommentsMapList.put(i, getItem(mCurrentAislePosition)
                        .getImageList().get(i).mCommentsList);
            }
            
            mImageDetailsArr = (ArrayList<String>) mCustomUrls.clone();
            ArrayList<ImageComments> imgComments = getItem(
                    mCurrentAislePosition).getImageList().get(
                    VueApplication.getInstance().getmAisleImgCurrentPos()).mCommentsList;
            prepareCommentList(imgComments);
            int imgPosition = 0;
            if (VueApplication.getInstance().getmAisleImgCurrentPos() < getItem(
                    mCurrentAislePosition).getImageList().size()) {
                imgPosition = VueApplication.getInstance()
                        .getmAisleImgCurrentPos();
            }
            mCurrentDispImageIndex = VueApplication.getInstance()
                    .getmAisleImgCurrentPos();
            mLikes = getItem(mCurrentAislePosition).getImageList().get(
                    imgPosition).mLikesCount;
            boolean isBookmarked = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .isAisleBookmarked(
                            getItem(mCurrentAislePosition).getAisleId());
            
            if (isBookmarked) {
                getItem(mCurrentAislePosition).setWindowBookmarkIndicator(
                        isBookmarked);
            }
            mBookmarksCount = getItem(mCurrentAislePosition).getAisleContext().mBookmarkCount;
            if (getItem(mCurrentAislePosition).getImageList().get(
                    mCurrentDispImageIndex).mCommentsList.size() < mShowFixedRowCount) {
                mListCount = getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mCommentsList.size()
                        + mShowFixedRowCount;
            } else {
                mListCount = mShowFixedRowCount + mInitialCommentsToShowSize;
            }
            findMostLikesImage();
            new Handler().postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    Map<String, String> articleParams = new HashMap<String, String>();
                    articleParams
                            .put("Category", getItem(mCurrentAislePosition)
                                    .getAisleContext().mCategory);
                    articleParams
                            .put("Lookingfor", getItem(mCurrentAislePosition)
                                    .getAisleContext().mLookingForItem);
                    articleParams
                            .put("Occasion", getItem(mCurrentAislePosition)
                                    .getAisleContext().mOccasion);
                    FlurryAgent.logEvent("Visited_Categories", articleParams);
                    
                }
                // wait time for flurry session starts
            }, mWaitTime);
            
        }
        mBestHeight = Utils.modifyHeightForDetailsView(getItem(
                mCurrentAislePosition).getImageList());
    }
    
    @Override
    public AisleWindowContent getItem(int position) {
        try {
            return mVueTrendingAislesDataModel.getAisleAt(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    static class ViewHolder {
        LinearLayout aisleContentBrowser;
        TextView aisleDescription;
        TextView aisleOwnersName;
        TextView aisleContext, commentCount, likeCount, textcount;
        TextView bookMarkCount;
        ImageView profileThumbnail;
        ImageView vueWindowBookmarkImg;
        ImageView vueWndowCommentImg;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        LinearLayout imgContentlay, commentContentlay;
        LinearLayout vueCommentheader, addCommentlay, descriptionlay;
        TextView userComment, enterComment;
        ImageView commentImg, likeImg;
        NetworkImageView userPic;
        RelativeLayout exapandHolder;
        EditText edtComment;
        View separator;
        RelativeLayout enterCommentrellay;
        RelativeLayout likelay, bookmarklay;
        FrameLayout edtCommentLay;
        ImageView commentSend;
        LinearLayout starImage;
        ViewPager myPager;
        String tag;
    }
    
    @Override
    public int getCount() {
        return mListCount;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(
                    R.layout.vue_details_adapter_pager, null);
            mViewHolder.aisleContentBrowser = (LinearLayout) convertView
                    .findViewById(R.id.showpieceadapter);
            mViewHolder.starImage = (LinearLayout) convertView
                    .findViewById(R.id.starImage);
            mViewHolder.imgContentlay = (LinearLayout) convertView
                    .findViewById(R.id.vueimagcontent);
            mViewHolder.commentContentlay = (LinearLayout) convertView
                    .findViewById(R.id.vue_user_coment_lay);
            mViewHolder.vueCommentheader = (LinearLayout) convertView
                    .findViewById(R.id.vue_comment_header);
            mViewHolder.descriptionlay = (LinearLayout) convertView
                    .findViewById(R.id.descriptionlayout);
            mViewHolder.aisleDescription = (TextView) convertView
                    .findViewById(R.id.vue_details_descreption);
            mViewHolder.separator = (View) convertView
                    .findViewById(R.id.separator);
            mViewHolder.vueWindowBookmarkImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_bookmark_img);
            mViewHolder.vueWndowCommentImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_comment_img);
            mViewHolder.likelay = (RelativeLayout) convertView
                    .findViewById(R.id.likelay);
            mViewHolder.bookmarklay = (RelativeLayout) convertView
                    .findViewById(R.id.bookmarklay);
            mViewHolder.enterCommentrellay = (RelativeLayout) convertView
                    .findViewById(R.id.entercmentrellay);
            
            mViewHolder.edtComment = (EditText) convertView
                    .findViewById(R.id.edtcomment);
            mViewHolder.likeImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_lik_img);
            mViewHolder.likeCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_lik_count);
            mViewHolder.addCommentlay = (LinearLayout) convertView
                    .findViewById(R.id.addcommentlay);
            mViewHolder.exapandHolder = (RelativeLayout) convertView
                    .findViewById(R.id.exapandholder);
            mViewHolder.aisleDescription.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.userPic = (NetworkImageView) convertView
                    .findViewById(R.id.vue_user_img);
            mViewHolder.userComment = (TextView) convertView
                    .findViewById(R.id.vue_user_comment);
            mViewHolder.commentCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_comment_count);
            mViewHolder.bookMarkCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_bookmark_count);
            mViewHolder.commentCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.bookMarkCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.likeCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            
            mViewHolder.commentImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_comment_img);
            mViewHolder.commentSend = (ImageView) convertView
                    .findViewById(R.id.sendcomment);
            
            mViewHolder.edtCommentLay = (FrameLayout) convertView
                    .findViewById(R.id.edtcommentlay);
            mViewHolder.userComment.setTextSize(VueApplication.getInstance()
                    .getmTextSize());
            mViewHolder.userComment.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.myPager = (ViewPager) convertView
                    .findViewById(R.id.myfivepanelpager);
            mViewHolder.textcount = (TextView) convertView
                    .findViewById(R.id.textcount);
            mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        if (getItem(mCurrentAislePosition).getWindowBookmarkIndicator()) {
            mViewHolder.vueWindowBookmarkImg.setImageResource(R.drawable.save);
        } else {
            mViewHolder.vueWindowBookmarkImg
                    .setImageResource(R.drawable.save_dark_small);
        }
        if (mShowingCommentList.size() != 0) {
            mViewHolder.vueWndowCommentImg.setImageResource(R.drawable.comment);
        } else {
            mViewHolder.vueWndowCommentImg
                    .setImageResource(R.drawable.comment_light);
        }
        if (getItem(mCurrentAislePosition).getImageList().get(
                mCurrentDispImageIndex).mLikeDislikeStatus == IMG_LIKE_STATUS) {
            mViewHolder.likeImg.setImageResource(R.drawable.heart);
        } else {
            mViewHolder.likeImg.setImageResource(R.drawable.heart_dark);
        }
        if (mLikes <= 0) {
            mLikes = 0;
            mViewHolder.likeImg.setImageResource(R.drawable.heart_dark);
        }
        
        mViewHolder.commentCount
                .setText((mShowingCommentList.size() + " Comments"));
        mViewHolder.bookMarkCount.setText("" + mBookmarksCount);
        mViewHolder.likeCount.setText("" + mLikes);
        mViewHolder.imgContentlay.setVisibility(View.VISIBLE);
        mViewHolder.commentContentlay.setVisibility(View.VISIBLE);
        mViewHolder.vueCommentheader.setVisibility(View.VISIBLE);
        mViewHolder.addCommentlay.setVisibility(View.VISIBLE);
        if (position == 0) {
            mViewHolder.commentContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.addCommentlay.setVisibility(View.GONE);
            mViewHolder.separator.setVisibility(View.GONE);
            mViewHolder.edtCommentLay.setVisibility(View.GONE);
            if (mSetPager) {
                mViewHolder.myPager.setAdapter(new MyPagerAdapter());
                mViewHolder.myPager.setOnPageChangeListener(pageListener);
                setParams(mViewHolder.aisleContentBrowser, mBestHeight);
                mViewHolder.myPager.setPageTransformer(true,
                        new ZoomOutPageTransformer());
                mViewHolder.myPager.setCurrentItem(mCurrentDispImageIndex);
            }
            try {
                if (getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mHasMostLikes) {
                    int browserHeight = getItem(mCurrentAislePosition)
                            .getBestLargetHeightForWindow();
                    int browserWidth = VueApplication.getInstance()
                            .getVueDetailsCardWidth();
                    int imageHeight = getItem(mCurrentAislePosition)
                            .getImageList().get(mCurrentDispImageIndex).mDetailsImageHeight;
                    int imageWidth = getItem(mCurrentAislePosition)
                            .getImageList().get(mCurrentDispImageIndex).mDetailsImageWidth;
                    int imageRightSpace = browserWidth - imageWidth;
                    int imageTopAreaHeight = (browserHeight / 2)
                            - (imageHeight / 2);
                    FrameLayout.LayoutParams editIconParams = new FrameLayout.LayoutParams(
                            VueApplication.getInstance().getPixel(32),
                            VueApplication.getInstance().getPixel(32));
                    editIconParams.setMargins(VueApplication.getInstance()
                            .getPixel(4) + imageRightSpace / 2, VueApplication
                            .getInstance().getPixel(10) + imageTopAreaHeight,
                            0, 0);
                    mViewHolder.starImage.setLayoutParams(editIconParams);
                    
                    if (getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mSameMostLikes) {
                    }
                }
                
                if (getItem(mCurrentAislePosition).getAisleContext().mDescription != null
                        && getItem(mCurrentAislePosition).getAisleContext().mDescription
                                .length() > 1) {
                    mViewHolder.descriptionlay.setVisibility(View.VISIBLE);
                    mViewHolder.aisleDescription
                            .setText(getItem(mCurrentAislePosition)
                                    .getAisleContext().mDescription);
                } else {
                    mViewHolder.descriptionlay.setVisibility(View.GONE);
                }
                
                if (getItem(mCurrentAislePosition).getAisleContext().mFirstName != null
                        && getItem(mCurrentAislePosition).getAisleContext().mLastName != null) {
                    mVueusername = getItem(mCurrentAislePosition)
                            .getAisleContext().mFirstName
                            + getItem(mCurrentAislePosition).getAisleContext().mLastName;
                } else if (getItem(mCurrentAislePosition).getAisleContext().mFirstName != null) {
                    if (getItem(mCurrentAislePosition).getAisleContext().mFirstName
                            .equals("Anonymous")) {
                        mVueusername = VueApplication.getInstance()
                                .getmUserInitials();
                    } else {
                        mVueusername = getItem(mCurrentAislePosition)
                                .getAisleContext().mFirstName;
                    }
                } else if (getItem(mCurrentAislePosition).getAisleContext().mLastName != null) {
                    mVueusername = getItem(mCurrentAislePosition)
                            .getAisleContext().mLastName;
                }
                
                if (mVueusername != null
                        && mVueusername.trim().equalsIgnoreCase("Anonymous")) {
                    if (VueApplication.getInstance().getmUserInitials() != null) {
                        mVueusername = VueApplication.getInstance()
                                .getmUserInitials();
                    }
                }
                if (mVueusername != null && mVueusername.trim().length() > 0) {
                    // Nothing...
                } else {
                    mVueusername = "Anonymous";
                }
                detailsImageClickListenr = new DetailImageClickListener();
                mViewHolder.tag = TAG;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // gone comment layoutgone
        } else if (position == 1) {
            if (mIsLikeImageClicked) {
                mIsLikeImageClicked = false;
                Animation rotate = AnimationUtils.loadAnimation(mContext,
                        R.anim.bounce);
                mViewHolder.likeImg.startAnimation(rotate);
                
            }
            if (mIsBookImageClciked) {
                mIsBookImageClciked = false;
                Animation rotate = AnimationUtils.loadAnimation(mContext,
                        R.anim.bounce);
                mViewHolder.vueWindowBookmarkImg.startAnimation(rotate);
            }
            
            mViewHolder.imgContentlay.setVisibility(View.GONE);
            mViewHolder.commentContentlay.setVisibility(View.GONE);
            mViewHolder.addCommentlay.setVisibility(View.GONE);
            mViewHolder.edtCommentLay.setVisibility(View.GONE);
            // image content gone
        } else if (position == mListCount - 1) {
            
            mViewHolder.separator.setVisibility(View.GONE);
            mViewHolder.imgContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.commentContentlay.setVisibility(View.GONE);
            if (mViewHolder.enterCommentrellay.getVisibility() == View.VISIBLE) {
                mViewHolder.commentSend.setVisibility(View.GONE);
            }
            if (mCloseKeyboard) {
                mCloseKeyboard = false;
                mViewHolder.edtCommentLay.setVisibility(View.GONE);
                mViewHolder.enterCommentrellay.setVisibility(View.VISIBLE);
            }
            mViewHolder.enterCommentrellay
                    .setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            
                            mViewHolder.edtCommentLay
                                    .setVisibility(View.VISIBLE);
                            mViewHolder.enterCommentrellay
                                    .setVisibility(View.GONE);
                            mswipeListner.onAddCommentClick(
                                    mViewHolder.enterCommentrellay,
                                    mViewHolder.edtComment,
                                    mViewHolder.commentSend,
                                    mViewHolder.edtCommentLay, mListCount - 1,
                                    mViewHolder.textcount);
                        }
                    });
        } else {
            // first two views are image and comment layout. so use position - 2
            // to display all the comments from start
            if (position - mInitialCommentsToShowSize < mShowingCommentList
                    .size()) {
                Comment comment = mShowingCommentList.get(position
                        - mInitialCommentsToShowSize);
                mViewHolder.userComment.setText(comment.mComment);
                int urlLength = 5;
                if (comment.mComenterUrl != null
                        && comment.mComenterUrl.length() > urlLength) {
                    mViewHolder.userPic.setImageUrl(comment.mComenterUrl,
                            mImageLoader);
                } else {
                    mViewHolder.userPic
                            .setImageResource(R.drawable.ic_launcher);
                }
            }
            
            mViewHolder.imgContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.addCommentlay.setVisibility(View.GONE);
            mViewHolder.separator.setVisibility(View.VISIBLE);
            if (position == mListCount - 2) {
                mViewHolder.separator.setVisibility(View.GONE);
            }
            
        }
        mViewHolder.exapandHolder.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                mSetPager = false;
                if (mListCount == (mShowFixedRowCount + mInitialCommentsToShowSize)) {
                    mListCount = mShowingCommentList.size()
                            + mShowFixedRowCount;
                } else {
                    if (mShowingCommentList.size() > 2) {
                        mListCount = mShowFixedRowCount
                                + mInitialCommentsToShowSize;
                    } else {
                        mListCount = mShowingCommentList.size()
                                + mShowFixedRowCount;
                    }
                }
                mViewHolder.enterCommentrellay.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
                setmSetPagerToTrue();
            }
        });
        mViewHolder.bookmarklay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSetPager = false;
                mIsBookImageClciked = true;
                boolean bookmarkStatus = false;
                if (getItem(mCurrentAislePosition).getWindowBookmarkIndicator()) {
                    FlurryAgent.logEvent("BOOKMARK_DETAILSVIEW");
                    if (mBookmarksCount > 0) {
                        mBookmarksCount--;
                        getItem(mCurrentAislePosition).setmAisleBookmarksCount(
                                mBookmarksCount);
                    }
                    getItem(mCurrentAislePosition).getAisleContext().mBookmarkCount = mBookmarksCount;
                    
                    getItem(mCurrentAislePosition).setWindowBookmarkIndicator(
                            bookmarkStatus);
                    handleBookmark(bookmarkStatus,
                            getItem(mCurrentAislePosition).getAisleId());
                } else {
                    bookmarkStatus = true;
                    FlurryAgent.logEvent("UNBOOKMARK_DETAILSVIEW");
                    mBookmarksCount++;
                    getItem(mCurrentAislePosition).setmAisleBookmarksCount(
                            mBookmarksCount);
                    getItem(mCurrentAislePosition).getAisleContext().mBookmarkCount = mBookmarksCount;
                    getItem(mCurrentAislePosition).setWindowBookmarkIndicator(
                            bookmarkStatus);
                    handleBookmark(bookmarkStatus,
                            getItem(mCurrentAislePosition).getAisleId());
                }
                notifyDataSetChanged();
                setmSetPagerToTrue();
            }
        });
        mViewHolder.likelay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                toggleRatingImage();
            }
        });
        return convertView;
    }
    
    public void notifyAdapter() {
        this.notifyDataSetChanged();
    }
    
    public void share(final Context context, Activity activity) {
        mShare = new ShareDialog(context, activity);
        FileCache ObjFileCache = new FileCache(context);
        ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
        if (getItem(mCurrentAislePosition).getImageList() != null
                && getItem(mCurrentAislePosition).getImageList().size() > 0) {
            String isUserAisle = "0";
            if (String
                    .valueOf(VueApplication.getInstance().getmUserId())
                    .equals(getItem(mCurrentAislePosition).getAisleContext().mUserId)) {
                isUserAisle = "1";
            }
            for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                    .size(); i++) {
                clsShare obj = new clsShare(
                        getItem(mCurrentAislePosition).getImageList().get(i).mCustomImageUrl,
                        ObjFileCache.getFile(
                                getItem(mCurrentAislePosition).getImageList()
                                        .get(i).mCustomImageUrl).getPath(),
                        getItem(mCurrentAislePosition).getAisleContext().mLookingForItem,
                        getItem(mCurrentAislePosition).getAisleContext().mFirstName
                                + " "
                                + getItem(mCurrentAislePosition)
                                        .getAisleContext().mLastName,
                        isUserAisle,
                        getItem(mCurrentAislePosition).getAisleContext().mAisleId,
                        getItem(mCurrentAislePosition).getImageList().get(i).mId);
                imageUrlList.add(obj);
            }
            mShare.share(
                    imageUrlList,
                    getItem(mCurrentAislePosition).getAisleContext().mOccasion,
                    (getItem(mCurrentAislePosition).getAisleContext().mFirstName
                            + " " + getItem(mCurrentAislePosition)
                            .getAisleContext().mLastName),
                    mCurrentDispImageIndex, mShareViaVueListner, null);
        }
        if (getItem(mCurrentAislePosition).getImageList() != null
                && getItem(mCurrentAislePosition).getImageList().size() > 0) {
            FileCache ObjFileCache1 = new FileCache(context);
            for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                    .size(); i++) {
                final File f = ObjFileCache1
                        .getFile(getItem(mCurrentAislePosition).getImageList()
                                .get(i).mCustomImageUrl);
                if (!f.exists()) {
                    @SuppressWarnings("rawtypes")
                    Response.Listener listener = new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bmp) {
                            Utils.saveBitmap(bmp, f);
                        }
                    };
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                        }
                    };
                    if (getItem(mCurrentAislePosition).getImageList().get(i).mCustomImageUrl != null) {
                        @SuppressWarnings("unchecked")
                        ImageRequest imagerequestObj = new ImageRequest(
                                getItem(mCurrentAislePosition).getImageList()
                                        .get(i).mCustomImageUrl, listener, 0,
                                0, null, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(imagerequestObj);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * 
     * To handle the click and long press event on the imageview in the aisle
     * content and to allow only one like and one dislike allows
     */
    private class DetailImageClickListener implements DetailClickListener {
        @Override
        public void onImageClicked() {
            onHandleLikeEvent();
        }
        
        @Override
        public void onImageLongPress() {
            onHandleDisLikeEvent();
        }
        
        @Override
        public void onImageSwipe(int position) {
            // int likeCount = 0;
            if (position >= 0
                    && position < VueApplication.getInstance()
                            .getClickedWindowCount()) {
                mCurrentDispImageIndex = position;
                
                mLikes = getItem(mCurrentAislePosition).getImageList().get(
                        position).mLikesCount;
                ArrayList<ImageComments> imgComments = getItem(
                        mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mCommentsList;
                prepareCommentList(imgComments);
                
                if (mShowingCommentList.size() < mShowFixedRowCount) {
                    
                    mListCount = mShowFixedRowCount
                            + mShowingCommentList.size();
                } else {
                    mListCount = mShowFixedRowCount
                            + mInitialCommentsToShowSize;
                }
                notifyDataSetChanged();
                mswipeListner.setFindAtText(getItem(mCurrentAislePosition)
                        .getImageList().get(position).mDetalsUrl);
            } else {
                return;
            }
        }
        
        @Override
        public void onImageDoubleTap() {
        }
        
        @Override
        public void onSetBrowserArea(String area) {
        }
        
        @Override
        public void onRefreshAdaptaers() {
            notifyAdapter();
            
        }
        
    }
    
    private void toggleRatingImage() {
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(mContext);
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to Like.", true, false, 0,
                    null, null);
        } else {
            if (mCurrentDispImageIndex >= 0
                    && mCurrentDispImageIndex < getItem(mCurrentAislePosition)
                            .getImageList().size()) {
                if (getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mLikeDislikeStatus == IMG_LIKE_STATUS) {
                    getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikeDislikeStatus = IMG_NONE_STATUS;
                    if (getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikesCount > 0) {
                        getItem(mCurrentAislePosition).getImageList().get(
                                mCurrentDispImageIndex).mLikesCount = getItem(
                                mCurrentAislePosition).getImageList().get(
                                mCurrentDispImageIndex).mLikesCount - 1;
                        sendDataToDb(mCurrentDispImageIndex, CHANGE_LIKES,
                                false);
                    }
                    
                } else {
                    getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikeDislikeStatus = IMG_LIKE_STATUS;
                    getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikesCount = getItem(
                            mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikesCount + 1;
                    sendDataToDb(mCurrentDispImageIndex, CHANGE_LIKES, true);
                    
                }
                mLikes = getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mLikesCount;
            }
            mIsLikeImageClicked = true;
            findMostLikesImage();
            notifyAdapter();
        }
    }
    
    public void changeLikesCountFromCopmareScreen(int position, String eventType) {
        
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(mContext);
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to Like.", true, false, 0,
                    null, null);
        } else {
            if (position >= 0
                    && position < getItem(mCurrentAislePosition).getImageList()
                            .size()) {
                if (eventType
                        .equalsIgnoreCase(AisleDetailsViewActivity.CLICK_EVENT)) {
                    // increase the like count
                    onChangeLikesCount(position);
                } else {
                    // decrease the like count
                    onChangeDislikesCount(position);
                }
            }
            
        }
        
    }
    
    private void onHandleLikeEvent() {
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(mContext);
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to Like.", true, false, 0,
                    null, null);
        } else {
            // increase the likes count
            if (mCurrentDispImageIndex >= 0
                    && mCurrentDispImageIndex < getItem(mCurrentAislePosition)
                            .getImageList().size()) {
                onChangeLikesCount(mCurrentDispImageIndex);
                mIsLikeImageClicked = true;
            }
        }
    }
    
    private void onHandleDisLikeEvent() {
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(mContext);
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to Like.", true, false, 0,
                    null, null);
        } else {
            // decrease the likes count
            if (mCurrentDispImageIndex >= 0
                    && mCurrentDispImageIndex < getItem(mCurrentAislePosition)
                            .getImageList().size()) {
                mIsLikeImageClicked = true;
                onChangeDislikesCount(mCurrentDispImageIndex);
            }
        }
    }
    
    public void setAisleBrowserObjectsNull() {
    }
    
    public void addAisleToContentWindow() {
        if (mViewHolder != null) {
            setAisleBrowserObjectsNull();
            mswipeListner.onResetAdapter();
        } else {
            setAisleBrowserObjectsNull();
            mswipeListner.onResetAdapter();
        }
        
    }
    
    public void updateAisleListAdapter() {
        
        int imageListSize = getItem(mCurrentAislePosition).getImageList()
                .size();
        VueApplication.getInstance().setClickedWindowCount(imageListSize);
        VueApplication.getInstance().setmAisleImgCurrentPos(0);
        setAisleBrowserObjectsNull();
        mswipeListner.onResetAdapter();
    }
    
    public ArrayList<AisleImageDetails> getImageList() {
        return getItem(mCurrentAislePosition).getImageList();
    }
    
    int mLikeCount = 0;
    ImageRating mImgRating;
    
    public void sendDataToDb(int imgPosition, String reqType,
            boolean likeOrDislike) {
        String aisleId = null;
        String imageId = null;
        AisleImageDetails itemDetails;
        if (getItem(mCurrentAislePosition).getImageList() != null
                && getItem(mCurrentAislePosition).getImageList().size() != 0) {
            aisleId = getItem(mCurrentAislePosition).getAisleId();
            itemDetails = getItem(mCurrentAislePosition).getImageList().get(
                    imgPosition);
            imageId = itemDetails.mId;
            if (reqType.equals(CHANGE_BOOKMARK)) {
                // aisleId,imageId,bookMarksCount,bookmarkIndicator
            } else if (reqType.equals(CHANGE_COMMENT)) {
                // aisleId,imageId,comment
                if (itemDetails.mCommentsList == null) {
                    getItem(mCurrentAislePosition).getImageList().get(0).mCommentsList = new ArrayList<ImageComments>();
                }
            } else if (reqType.equals(CHANGE_LIKES)) {
                // aisleId,imageId,likesCount,likeStatus
                mLikeCount = itemDetails.mLikesCount;
                ArrayList<ImageRating> imgRatingList = DataBaseManager
                        .getInstance(mContext).getRatedImagesList(aisleId);
                mImgRating = new ImageRating();
                mImgRating.setAisleId(Long.parseLong(aisleId));
                mImgRating.setImageId(Long.parseLong(imageId));
                mImgRating.setLiked(likeOrDislike);
                for (ImageRating imgRat : imgRatingList) {
                    if (mImgRating.getImageId().longValue() == imgRat
                            .getImageId().longValue()) {
                        mImgRating.setId(imgRat.getId().longValue());
                        break;
                    }
                }
                try {
                    AisleManager.getAisleManager().updateRating(mImgRating,
                            mLikeCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    public AisleContext getAisleContext() {
        return getItem(mCurrentAislePosition).getAisleContext();
    }
    
    public boolean checkLimitForLoginDialog() {
        if (mContext != null) {
            SharedPreferences sharedPreferencesObj = mContext
                    .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
            boolean isUserLoggedInFlag = sharedPreferencesObj.getBoolean(
                    VueConstants.VUE_LOGIN, false);
            if (!isUserLoggedInFlag) {
                int createdAisleCount = sharedPreferencesObj.getInt(
                        VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
                int commentsCount = sharedPreferencesObj.getInt(
                        VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);
                if (createdAisleCount >= VueConstants.CREATE_AISLE_LIMIT_FOR_LOGIN
                        || commentsCount >= VueConstants.COMMENTS_LIMIT_FOR_LOGIN) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    private void onChangeLikesCount(int position) {
        if (mStoredVueUser == null) {
            try {
                mStoredVueUser = Utils.readUserObjectFromFile(mContext,
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        Map<String, String> articleParams = new HashMap<String, String>();
        articleParams.put("Category", getItem(mCurrentAislePosition)
                .getAisleContext().mCategory);
        articleParams.put("Lookingfor", getItem(mCurrentAislePosition)
                .getAisleContext().mLookingForItem);
        articleParams.put("Occasion", getItem(mCurrentAislePosition)
                .getAisleContext().mOccasion);
        if (mStoredVueUser != null) {
            articleParams.put("Unique_User_Like", "" + mStoredVueUser.getId());
        } else {
            articleParams.put("Unique_User_Like", "anonymous");
        }
        FlurryAgent.logEvent("LIKES_DETAILSVIEW", articleParams);
        if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_LIKE_STATUS) {
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_LIKE_STATUS;
            
            Map<String, String> articleParams1 = new HashMap<String, String>();
            articleParams1.put("Unique_Aisle_Likes",
                    "" + getItem(mCurrentAislePosition).getAisleId());
            if (mStoredVueUser != null) {
                articleParams1.put("Unique_User_Like",
                        "" + mStoredVueUser.getId());
            } else {
                articleParams1.put("Unique_User_Like", "anonymous");
            }
            FlurryAgent.logEvent("Aisle_Likes", articleParams1);
        } else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_NONE_STATUS) {
            
            getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(
                    mCurrentAislePosition).getImageList().get(position).mLikesCount + 1;
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_LIKE_STATUS;
            sendDataToDb(position, CHANGE_LIKES, true);
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = getItem(mCurrentAislePosition).getImageList()
                    .get(position).mLikesCount;
            notifyAdapter();
        }
    }
    
    private void onChangeDislikesCount(int position) {
        FlurryAgent.logEvent("DIS_LIKES_DETAILSVIEW");
        if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_LIKE_STATUS) {
            // false
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_NONE_STATUS;
            if (getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount > 0) {
                getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(
                        mCurrentAislePosition).getImageList().get(position).mLikesCount - 1;
            }
            sendDataToDb(position, CHANGE_LIKES, false);
        } else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_NONE_STATUS) {
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_NONE_STATUS;
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = getItem(mCurrentAislePosition).getImageList()
                    .get(position).mLikesCount;
            notifyAdapter();
        }
    }
    
    public void closeKeyboard() {
        final InputMethodManager mInputMethodManager = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(
                mViewHolder.edtComment.getWindowToken(), 0);
        mViewHolder.edtCommentLay.setVisibility(View.VISIBLE);
        mViewHolder.enterCommentrellay.setVisibility(View.GONE);
        mCloseKeyboard = true;
        notifyAdapter();
    }
    
    private void handleBookmark(boolean isBookmarked, String aisleId) {
        
        AisleBookmark aisleBookmark = new AisleBookmark(null, isBookmarked,
                Long.parseLong(aisleId));
        ArrayList<AisleBookmark> aisleBookmarkList = DataBaseManager
                .getInstance(VueApplication.getInstance())
                .getBookmarkAisleIdsList();
        
        for (AisleBookmark b : aisleBookmarkList) {
            if (aisleId.equals(Long.toString(b.getAisleId().longValue()))) {
                aisleBookmark.setId(b.getId());
                
                break;
            }
        }
        VueUser storedVueUser = null;
        try {
            
            storedVueUser = Utils.readUserObjectFromFile(mContext,
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
            AisleManager.getAisleManager().aisleBookmarkUpdate(aisleBookmark,
                    Long.valueOf(storedVueUser.getId()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    private void setImageRating() {
        ArrayList<AisleImageDetails> aisleImgDetais = getItem(
                mCurrentAislePosition).getImageList();
        ArrayList<ImageRating> imgRatingList = DataBaseManager.getInstance(
                mContext).getRatedImagesList(
                getItem(mCurrentAislePosition).getAisleId());
        
        for (AisleImageDetails imgDetail : aisleImgDetais) {
            
            for (ImageRating imgRating : imgRatingList) {
                
                if (imgRating.getImageId() == Long.parseLong(imgDetail.mId)) {
                    imgDetail.mLikeDislikeStatus = IMG_LIKE_STATUS;
                }
            }
        }
    }
    
    public void updateListCount(String newComment) {
        mListCount = mListCount + 1;
    }
    
    public void createComment(String commentString) {
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String commenterUrl = null;
        if (storedVueUser != null) {
            commenterUrl = storedVueUser.getUserImageURL();
        }
        if (commenterUrl != null && commenterUrl.length() < 6) {
            commenterUrl = null;
        } else if (commenterUrl == null) {
            commenterUrl = null;
        }
        
        ImageComments comments = new ImageComments();
        comments.mComment = commentString;
        comments.mCommenterUrl = commenterUrl;
        
        if (commentString == null || commentString.length() < 1) {
            return;
        }
        getItem(mCurrentAislePosition).getImageList().get(
                mCurrentDispImageIndex).mCommentsList.add(0, comments);
        if (mCommentsMapList == null) {
            getCommentList();
        }
        mCommentsMapList.put(
                mCurrentDispImageIndex,
                getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mCommentsList);
        
        ArrayList<ImageComments> imgComments = mCommentsMapList
                .get(mCurrentDispImageIndex);
        
        prepareCommentList(imgComments);
        if (mShowingCommentList.size() < mShowFixedRowCount) {
            mListCount = mShowingCommentList.size() + mShowFixedRowCount;
        } else {
            mListCount = mShowFixedRowCount + mInitialCommentsToShowSize;
        }
        final ImageCommentRequest imgComment = new ImageCommentRequest();
        imgComment.setComment(commentString);
        imgComment.setLastModifiedTimestamp(System.currentTimeMillis());
        imgComment.setOwnerUserId(Long.parseLong(VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getUserId()));
        imgComment.setOwnerImageId(Long
                .parseLong(getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mId));
        /** Save the image comment and verify the save */
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    VueTrendingAislesDataModel
                            .getInstance(VueApplication.getInstance())
                            .getNetworkHandler().createImageComment(imgComment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
    }
    
    private void getCommentList() {
        mCommentsMapList = new HashMap<Integer, ArrayList<ImageComments>>();
        for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                .size(); i++) {
            mCommentsMapList.put(i, getItem(mCurrentAislePosition)
                    .getImageList().get(i).mCommentsList);
        }
        ArrayList<ImageComments> imgComments = (ArrayList<ImageComments>) mCommentsMapList
                .get(mCurrentDispImageIndex);
        prepareCommentList(imgComments);
    }
    
    private void prepareCommentList(ArrayList<ImageComments> imgComments) {
        if (mShowingCommentList == null) {
            mShowingCommentList = new ArrayList<Comment>();
        } else if (mShowingCommentList.size() > 0) {
            mShowingCommentList.clear();
        }
        for (ImageComments comment : imgComments) {
            Comment showComment = new Comment();
            showComment.mComment = comment.mComment;
            showComment.mComenterUrl = comment.mCommenterUrl;
            mShowingCommentList.add(showComment);
        }
    }
    
    /**
     * show star to most likes on the image.
     */
    private void findMostLikesImage() {
        int mostLikePosition = 0, mLikes = 0;
        boolean hasLikes = false;
        for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                .size(); i++) {
            getItem(mCurrentAislePosition).getImageList().get(i).mHasMostLikes = false;
            getItem(mCurrentAislePosition).getImageList().get(i).mSameMostLikes = false;
            if (mLikes < getItem(mCurrentAislePosition).getImageList().get(i).mLikesCount) {
                mLikes = getItem(mCurrentAislePosition).getImageList().get(i).mLikesCount;
                hasLikes = true;
                mostLikePosition = i;
            }
        }
        if (hasLikes) {
            getItem(mCurrentAislePosition).getImageList().get(mostLikePosition).mHasMostLikes = true;
        }
        if (mLikes == 0) {
            return;
        }
        for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                .size(); i++) {
            if (mostLikePosition == i) {
                continue;
            }
            if (mLikes == getItem(mCurrentAislePosition).getImageList().get(i).mLikesCount) {
                getItem(mCurrentAislePosition).getImageList().get(i).mSameMostLikes = true;
                getItem(mCurrentAislePosition).getImageList().get(i).mHasMostLikes = true;
                getItem(mCurrentAislePosition).getImageList().get(
                        mostLikePosition).mSameMostLikes = true;
            }
        }
    }
    
    private class MyPagerAdapter extends PagerAdapter implements
            GestureDetector.OnGestureListener {
        
        /**
         * 
         * @param mContext
         *            Context
         */
        
        @Override
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((RelativeLayout) object);
        }
        
        @Override
        public void finishUpdate(View arg0) {
            
        }
        
        @Override
        public int getCount() {
            return getItem(mCurrentAislePosition).getImageList().size();
        }
        
        @Override
        public Object instantiateItem(View view, int position) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) view.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            
            View myView = mInflater.inflate(R.layout.detailsbrowser, null);
            ImageView browserImage = (ImageView) myView
                    .findViewById(R.id.browserimage);
            browserImage.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    detailsImageClickListenr.onImageClicked();
                }
            });
            browserImage.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    detailsImageClickListenr.onImageLongPress();
                    return false;
                }
            });
            ImageView full_bg_image = (ImageView) myView
                    .findViewById(R.id.full_bg_image);
            full_bg_image.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // these listener does nothing. But inorder to give the
                    // control to the
                    // listview when touch out side of the image in the browser.
                }
            });
            full_bg_image.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            
            LinearLayout starLay = (LinearLayout) myView
                    .findViewById(R.id.starImage);
            ImageView starImage = (ImageView) myView
                    .findViewById(R.id.staricon);
            LinearLayout editLay = (LinearLayout) myView
                    .findViewById(R.id.editImage);
            AisleImageDetails imageDetails = getItem(mCurrentAislePosition)
                    .getImageList().get(position);
            ProgressBar progressBar = (ProgressBar) myView
                    .findViewById(R.id.progressBar1);
            editLay.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mswipeListner.onEditAisle();
                }
            });
            starLay.setVisibility(View.GONE);
            editLay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            Bitmap bitmap = null;
            bitmap = BitmapLoaderUtils.getInstance().getCachedBitmap(
                    imageDetails.mImageUrl);
            if (bitmap != null) {
                browserImage.setImageBitmap(bitmap);
                if (getItem(mCurrentAislePosition).getImageList().get(position).mOwnerUserId != null
                        && getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
                    if (Long.parseLong(getItem(mCurrentAislePosition)
                            .getImageList().get(position).mOwnerUserId) == mUserId
                            || Long.parseLong(getItem(mCurrentAislePosition)
                                    .getAisleContext().mUserId) == mUserId) {
                        editLay.setVisibility(View.VISIBLE);
                        
                    } else {
                        editLay.setVisibility(View.GONE);
                        
                    }
                }
                
                if (imageDetails.mHasMostLikes) {
                    starLay.setVisibility(View.VISIBLE);
                    if (imageDetails.mSameMostLikes) {
                        starImage.setImageResource(R.drawable.vue_star_light);
                    } else {
                        starImage.setImageResource(R.drawable.vue_star_theme);
                    }
                } else {
                    starLay.setVisibility(View.GONE);
                }
                
            } else {
                
                loadBitmap(
                        getItem(mCurrentAislePosition).getImageList().get(
                                position), browserImage, mBestHeight, 0,
                        progressBar, position, editLay, starLay, starImage);
            }
            ((ViewPager) view).addView(myView);
            return myView;
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            
        }
        
        @Override
        public Parcelable saveState() {
            return null;
        }
        
        @Override
        public void startUpdate(View arg0) {
            
        }
        
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            
            return false;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return false;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return false;
        }
        
        @Override
        public void onShowPress(MotionEvent e) {
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }
    
    public void loadBitmap(AisleImageDetails itemDetails, ImageView imageView,
            int bestHeight, int scrollIndex, ProgressBar progressBar,
            int currentPosition, LinearLayout editLay, LinearLayout starLay,
            ImageView starImage) {
        String loc = itemDetails.mImageUrl;
        String serverImageUrl = itemDetails.mImageUrl;
        // if (cancelPotentialDownload(loc, imageView)) {
        BitmapWorkerTask task = new BitmapWorkerTask(itemDetails, imageView,
                bestHeight, scrollIndex, progressBar, currentPosition, editLay,
                starLay, starImage);
        
        String imagesArray[] = { loc, serverImageUrl };
        task.execute(imagesArray);
        // }
        
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ImageView> mStarImageReference;
        private final WeakReference<LinearLayout> starLayoutReference;
        private final WeakReference<LinearLayout> editLayoutReference;
        private final WeakReference<ProgressBar> progressBarReference;
        private String url = null;
        private int mBestHeight;
        int mAvailabeWidth, mAvailableHeight;
        AisleImageDetails mItemDetails;
        int mScrollIndex;
        int mImageListCurrentPosition;
        
        public BitmapWorkerTask(AisleImageDetails itemDetails,
                ImageView imageView, int bestHeight, int scrollIndex,
                ProgressBar progressBar, int currentPosition,
                LinearLayout editLay, LinearLayout starLay, ImageView starImage) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mStarImageReference = new WeakReference<ImageView>(starImage);
            starLayoutReference = new WeakReference<LinearLayout>(starLay);
            editLayoutReference = new WeakReference<LinearLayout>(editLay);
            progressBarReference = new WeakReference<ProgressBar>(progressBar);
            mBestHeight = bestHeight;
            mAvailabeWidth = itemDetails.mAvailableWidth;
            mAvailableHeight = itemDetails.mAvailableHeight;
            mItemDetails = itemDetails;
            mScrollIndex = scrollIndex;
            mImageListCurrentPosition = currentPosition;
        }
        
        @Override
        protected void onPreExecute() {
            ProgressBar pb = progressBarReference.get();
            if (pb != null)
                pb.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            url = params[0];
            Bitmap bmp = null;
            
            // we want to get the bitmap and also add it into the memory cache
            boolean cacheBitmap = true;
            bmp = mBitmapLoaderUtils.getBitmap(url, params[1], cacheBitmap,
                    mBestHeight, VueApplication.getInstance()
                            .getVueDetailsCardWidth(), Utils.DETAILS_SCREEN);
            if (bmp != null) {
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
            LinearLayout editLay = editLayoutReference.get();
            ProgressBar pb = progressBarReference.get();
            LinearLayout starLay = starLayoutReference.get();
            ImageView starImage = mStarImageReference.get();
            
            if (pb != null) {
                pb.setVisibility(View.GONE);
            }
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
                if (mImageListCurrentPosition == mCurrentDispImageIndex) {
                    imageView.startAnimation(myFadeInAnimation);
                }
            }
            if (getItem(mCurrentAislePosition).getImageList().size() == mImageListCurrentPosition) {
                mImageListCurrentPosition = getItem(mCurrentAislePosition)
                        .getImageList().size() - 1;
            }
            if (getItem(mCurrentAislePosition).getImageList().get(
                    mImageListCurrentPosition).mOwnerUserId != null
                    && getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
                if (Long.parseLong(getItem(mCurrentAislePosition)
                        .getImageList().get(mImageListCurrentPosition).mOwnerUserId) == mUserId
                        || Long.parseLong(getItem(mCurrentAislePosition)
                                .getAisleContext().mUserId) == mUserId) {
                    if (bitmap != null) {
                        if (editLay != null) {
                            editLay.setVisibility(View.VISIBLE);
                        }
                    }
                    
                } else {
                    if (editLay != null) {
                        editLay.setVisibility(View.GONE);
                    }
                }
            }
            if (bitmap != null) {
                if (mItemDetails.mHasMostLikes) {
                    if (starLay != null) {
                        starLay.setVisibility(View.VISIBLE);
                    }
                    
                    if (starImage != null) {
                        if (mItemDetails.mSameMostLikes) {
                            starImage
                                    .setImageResource(R.drawable.vue_star_light);
                        } else {
                            starImage
                                    .setImageResource(R.drawable.vue_star_theme);
                        }
                    }
                } else {
                    if (starLay != null) {
                        starLay.setVisibility(View.GONE);
                    }
                }
            }
            
        }
    }
    
    private void setParams(LinearLayout vFlipper, int imgScreenHeight) {
        
        if (vFlipper != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    VueApplication.getInstance().getScreenWidth(),
                    imgScreenHeight + VueApplication.getInstance().getPixel(12));
            params.gravity = Gravity.CENTER;
            vFlipper.setLayoutParams(params);
        }
        
    }
    
    private class PageListener extends SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            
            mCurrentDispImageIndex = position;
            if (detailsImageClickListenr != null)
                detailsImageClickListenr.onImageSwipe(position);
            mSetPager = false;
            mswipeListner.onAllowListResponse();
            setmSetPagerToTrue();
            if (mPrevPosition == position) {
            } else if (mPrevPosition < position) {
                mswipeListner.onAisleSwipe("Left", position);
                
            } else {
                mswipeListner.onAisleSwipe("Right", position);
            }
            mPrevPosition = mCurrentDispImageIndex;
        }
    }
    
    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;
        
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
                
            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as
                // well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                
                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                
                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
                
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
    
    int mLikePageDelay = 1000;
    
    private void setmSetPagerToTrue() {
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                // to make sure the pager is not refresh when like and dislike
                // the image and when bookmarked the aislse.
                mSetPager = true;
            }
        }, mLikePageDelay);
    }
}