/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

@SuppressLint("UseSparseArrays")
public class AisleDetailsViewAdapterPager extends BaseAdapter {
    private Context mContext;
    public static final String TAG = "AisleDetailsViewAdapter";
    private static final String CHANGE_BOOKMARK = "BOOKMARK";
    public static final String CHANGE_COMMENT = "COMMENT";
    private static final String CHANGE_LIKES = "LIKES";
    private static final String AISLE_STAGE_FOUR = "completed";
    private String mAisleCureentStage;
    private AisleDetailSwipeListener mswipeListner;
    private VueUser mStoredVueUser = null;
    public static final int SWIPE_MIN_DISTANCE = 30;
    private LayoutInflater mInflater;
    MyPagerAdapter mPagerAdapter;
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
    private int mCurrentAislePosition;
    @SuppressLint("UseSparseArrays")
    Map<Integer, ArrayList<ImageComments>> mCommentsMapList = new HashMap<Integer, ArrayList<ImageComments>>();
    private ArrayList<Comment> mShowingCommentList = new ArrayList<Comment>();
    private int mBestHeight;
    private ViewHolder mViewHolder;
    private static final int mWaitTime = 1000;
    private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
    private LoginWarningMessage mLoginWarningMessage = null;
    private long mUserId;
    private ImageLoader mImageLoader;
    private ShareViaVueListner mShareViaVueListner;
    private int mPrevPosition;
    private PageListener pageListener;
    private DetailImageClickListener detailsImageClickListenr;
    public boolean mSetPager = true;
    private Bitmap profileUserBmp;
    private MixpanelAPI mixpanel;
    
    public AisleDetailsViewAdapterPager(Context c,
            AisleDetailSwipeListener swipeListner, int listCount,
            ArrayList<AisleWindowContent> content,
            ShareViaVueListner shareViaVueListner) {
        mixpanel = MixpanelAPI.getInstance(c,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        mShareViaVueListner = shareViaVueListner;
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance());
        mCurrentDispImageIndex = VueApplication.getInstance()
                .getmAisleImgCurrentPos();
        mContext = c;
        mImageLoader = VueApplication.getInstance().getImageCacheLoader();
        pageListener = new PageListener();
        mswipeListner = swipeListner;
        mListCount = listCount;
        profileUserBmp = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.new_profile);
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
                // get the clicked window position.
                mCurrentAislePosition = i;
                break;
            }
        }
        if (VueApplication.getInstance().getmAisleImgCurrentPos() > getItem(
                mCurrentAislePosition).getImageList().size() - 1) {
            VueApplication.getInstance().setmAisleImgCurrentPos(
                    getItem(mCurrentAislePosition).getImageList().size() - 1);
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
            VueApplication.getInstance().setClickedWindowCount(
                    getItem(mCurrentAislePosition).getImageList().size());
            
            for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
                    .size(); i++) {
                if (getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight > mBestHeight) {
                    // find the best height among all images.
                    mBestHeight = getItem(mCurrentAislePosition).getImageList()
                            .get(i).mAvailableHeight;
                }
                
                mCommentsMapList.put(i, getItem(mCurrentAislePosition)
                        .getImageList().get(i).mCommentsList);
            }
            ArrayList<ImageComments> imgComments = getItem(
                    mCurrentAislePosition).getImageList().get(
                    VueApplication.getInstance().getmAisleImgCurrentPos()).mCommentsList;
            prepareCommentList(imgComments);
            Collections.reverse(mShowingCommentList);
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
            // to the aisle is bookmarked already or not.
            boolean isBookmarked = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .isAisleBookmarked(
                            getItem(mCurrentAislePosition).getAisleId());
            
            isBookmarked = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .checkIsAisleBookmarked(
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
                    JSONObject aisleViewedProps = new JSONObject();
                    try {
                        aisleViewedProps.put("AisleId",
                                getItem(mCurrentAislePosition).getAisleId());
                        aisleViewedProps.put("Category",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mCategory);
                        aisleViewedProps.put("Lookingfor",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mLookingForItem);
                        aisleViewedProps.put("Occasion",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mOccasion);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mixpanel.track("AisleViewed", aisleViewedProps);
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
        mPagerAdapter = new MyPagerAdapter();
        
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
        TextView aisleContext, commentCount, likeCount, textcount,
                commentername;
        TextView bookMarkCount;
        ImageView profileThumbnail;
        ImageView vueWindowBookmarkImg;
        ImageView vueWndowCommentImg;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        LinearLayout imgContentlay, commentContentlay;
        LinearLayout vueCommentheader, descriptionlay;
        TextView userComment, enterComment;
        ImageView commentImg, likeImg;
        NetworkImageView userPic;
        RelativeLayout exapandHolder;
        View separator;
        RelativeLayout likelay, bookmarklay;
        LinearLayout starImage;
        RelativeLayout decisionLay;
        TextView decisonText;
        ViewPager myPager;
        String tag;
        View greenbar;
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
            mViewHolder.decisionLay = (RelativeLayout) convertView
                    .findViewById(R.id.decisionlay);
            mViewHolder.decisonText = (TextView) convertView
                    .findViewById(R.id.decisiontext);
            mViewHolder.greenbar = (View) convertView
                    .findViewById(R.id.greenbar);
            
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
            mViewHolder.likeImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_lik_img);
            mViewHolder.likeCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_lik_count);
            mViewHolder.exapandHolder = (RelativeLayout) convertView
                    .findViewById(R.id.exapandholder);
            mViewHolder.aisleDescription.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.userPic = (NetworkImageView) convertView
                    .findViewById(R.id.vue_user_img);
            mViewHolder.userComment = (TextView) convertView
                    .findViewById(R.id.vue_user_comment);
            mViewHolder.commentername = (TextView) convertView
                    .findViewById(R.id.commentername);
            mViewHolder.commentCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_comment_count);
            mViewHolder.bookMarkCount = (TextView) convertView
                    .findViewById(R.id.vuewndow_bookmark_count);
            mViewHolder.commentCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.bookMarkCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            mViewHolder.likeCount.setTextSize(Utils.SMALL_TEXT_SIZE);
            
            mViewHolder.commentImg = (ImageView) convertView
                    .findViewById(R.id.vuewndow_comment_img);
            mViewHolder.myPager = (ViewPager) convertView
                    .findViewById(R.id.myfivepanelpager);
            mViewHolder.textcount = (TextView) convertView
                    .findViewById(R.id.textcount);
            mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            // to show the green bar based on count of the likes and comments.
            findAisleStage();
            int cardWidh = VueApplication.getInstance()
                    .getVueDetailsCardWidth();
            if (mAisleCureentStage.equals(VueConstants.AISLE_STATGE_ONE)) {
                cardWidh = cardWidh * 25 / 100;
            } else if (mAisleCureentStage.equals(VueConstants.AISLE_STAGE_TWO)) {
                cardWidh = cardWidh * 50 / 100;
            } else if (mAisleCureentStage
                    .equals(VueConstants.AISLE_STAGE_THREE)) {
                cardWidh = cardWidh * 75 / 100;
            }
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    cardWidh, VueApplication.getInstance().getPixel(2));
            mViewHolder.greenbar.setLayoutParams(layoutParams);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        // if the current user bookmarked the aisle show theme color icon.
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
                mCurrentDispImageIndex).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
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
        mViewHolder.decisionLay.setVisibility(View.VISIBLE);
        if (position == 0) {
            mViewHolder.commentContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.separator.setVisibility(View.GONE);
            mViewHolder.decisionLay.setVisibility(View.VISIBLE);
            String descisionText = " ";
            if (Long.parseLong(getItem(mCurrentAislePosition).getAisleContext().mUserId) == mUserId) {
                if (mAisleCureentStage.endsWith(VueConstants.AISLE_STATGE_ONE)) {
                    descisionText = "Just posted - ask for opinions by sharing";
                } else if (mAisleCureentStage
                        .endsWith(VueConstants.AISLE_STAGE_TWO)) {
                    descisionText = "Opinions in progress";
                } else if (mAisleCureentStage
                        .endsWith(VueConstants.AISLE_STAGE_THREE)) {
                    descisionText = " Opinions received - decide now";
                } else if (mAisleCureentStage.endsWith(AISLE_STAGE_FOUR)) {
                    descisionText = "Decision complete ";
                }
            } else {
                if (mAisleCureentStage.endsWith(VueConstants.AISLE_STATGE_ONE)) {
                    descisionText = "Just posted - please add your opinion";
                } else if (mAisleCureentStage
                        .endsWith(VueConstants.AISLE_STAGE_TWO)) {
                    descisionText = "Still collecting opinions - please add yours";
                } else if (mAisleCureentStage
                        .endsWith(VueConstants.AISLE_STAGE_THREE)) {
                    descisionText = "Opinions received, decision pending";
                } else if (mAisleCureentStage.endsWith(AISLE_STAGE_FOUR)) {
                    descisionText = " Decision complete";
                }
            }
            mViewHolder.decisonText.setText(descisionText);
            
            if (mSetPager) {
                mViewHolder.myPager.setAdapter(mPagerAdapter);
                mViewHolder.myPager.setOnPageChangeListener(pageListener);
                setParams(mViewHolder.aisleContentBrowser, mBestHeight);
                mViewHolder.myPager.setPageTransformer(true,
                        new ZoomOutPageTransformer());
                mViewHolder.myPager.setCurrentItem(mCurrentDispImageIndex);
            }
            try {
                boolean editLayVisibility = false;
                boolean starLayVisibility = false;
                boolean isMostLikedImage = false;
                if (getItem(mCurrentAislePosition).getImageList().get(position).mOwnerUserId != null
                        && getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
                    if (Long.parseLong(getItem(mCurrentAislePosition)
                            .getImageList().get(position).mOwnerUserId) == mUserId
                            || Long.parseLong(getItem(mCurrentAislePosition)
                                    .getAisleContext().mUserId) == mUserId) {
                        editLayVisibility = true;
                        
                    } else {
                        editLayVisibility = false;
                    }
                }
                
                if ((getItem(mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex).mHasMostLikes)) {
                    starLayVisibility = true;
                    if ((getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mSameMostLikes)) {
                        isMostLikedImage = true;
                    } else {
                        isMostLikedImage = false;
                    }
                } else {
                    starLayVisibility = false;
                }
                mswipeListner.onUpdateLikeStatus(editLayVisibility,
                        starLayVisibility, isMostLikedImage);
                if (getItem(mCurrentAislePosition).getAisleContext().mDescription != null
                        && getItem(mCurrentAislePosition).getAisleContext().mDescription
                                .length() > 1) {
                    mViewHolder.descriptionlay.setVisibility(View.VISIBLE);
                    mViewHolder.aisleDescription
                            .setText(getItem(mCurrentAislePosition)
                                    .getAisleContext().mDescription);
                    mViewHolder.aisleDescription
                            .setOnClickListener(new OnClickListener() {
                                
                                @Override
                                public void onClick(View v) {
                                    mswipeListner.onCloseKeyBoard();
                                    // will be called when press on the
                                    // description, description
                                    // text will be expand and collapse for
                                    // alternative clicks
                                    // get the pixel equivalent to given dp
                                    // value
                                    int descriptionMaxCount = 3;
                                    int leftRightMargin = VueApplication
                                            .getInstance().getPixel(8);
                                    int topBottomMargin = VueApplication
                                            .getInstance().getPixel(6);
                                    TextView expandView = (TextView) v
                                            .findViewById(R.id.vue_details_descreption);
                                    int descLineCount = expandView
                                            .getLineCount();
                                    if (descLineCount == descriptionMaxCount) {
                                        LinearLayout.LayoutParams params;
                                        params = new LinearLayout.LayoutParams(
                                                LayoutParams.MATCH_PARENT,
                                                LayoutParams.WRAP_CONTENT);
                                        params.setMargins(leftRightMargin,
                                                topBottomMargin,
                                                leftRightMargin,
                                                topBottomMargin);
                                        expandView
                                                .setMaxLines(Integer.MAX_VALUE);
                                    } else {
                                        expandView
                                                .setMaxLines(descriptionMaxCount);
                                    }
                                    
                                }
                            });
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
        }
        // gone comment layoutgone }
        else if (position == 1) {
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
            // image content gone
        } else if (position == mListCount - 1) {
            mViewHolder.separator.setVisibility(View.GONE);
            mViewHolder.imgContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.commentContentlay.setVisibility(View.GONE);
            mViewHolder.decisionLay.setVisibility(View.GONE);
        } else {
            // first two views are image and comment layout. so use position - 2
            // to display all the comments from start
            if (position - mInitialCommentsToShowSize < mShowingCommentList
                    .size()) {
                Comment comment = mShowingCommentList.get(position
                        - mInitialCommentsToShowSize);
                if (comment.mComment.length() > 2) {
                    comment.mComment = comment.mComment.substring(0, 1)
                            .toUpperCase() + comment.mComment.substring(1);
                }
                mViewHolder.userComment.setText(comment.mComment);
                String commenterName = "";
                if (comment.mCommenterFirstName != null
                        && !comment.mCommenterFirstName.equals("")) {
                    commenterName = comment.mCommenterFirstName;
                } else if (comment.mCommenterLastName != null
                        && !comment.mCommenterLastName.equals("")) {
                    commenterName = comment.mCommenterLastName;
                }
                mViewHolder.commentername.setText(commenterName);
                int urlLength = 5;
                if (comment.mComenterUrl != null
                        && comment.mComenterUrl.length() > urlLength) {
                    mViewHolder.userPic.setImageUrl(comment.mComenterUrl,
                            mImageLoader);
                } else {
                    // when no user profile set the default one
                    if (profileUserBmp == null) {
                        profileUserBmp = BitmapFactory
                                .decodeResource(mContext.getResources(),
                                        R.drawable.new_profile);
                    }
                    mViewHolder.userPic.setImageBitmap(profileUserBmp);
                }
            }
            
            mViewHolder.imgContentlay.setVisibility(View.GONE);
            mViewHolder.vueCommentheader.setVisibility(View.GONE);
            mViewHolder.decisionLay.setVisibility(View.GONE);
            mViewHolder.separator.setVisibility(View.VISIBLE);
            if (position == mListCount - 2) {
                mViewHolder.separator.setVisibility(View.GONE);
            }
            
        }
        mViewHolder.exapandHolder.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                mSetPager = false;
                if (mListCount == (mShowFixedRowCount + mInitialCommentsToShowSize)) {
                    // expand the comments when user clicks on comment to view
                    // all.
                    // show the comments in the same order as the users entered
                    // in expand view.
                    ArrayList<ImageComments> imgComments = getItem(
                            mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mCommentsList;
                    prepareCommentList(imgComments);
                    mListCount = mShowingCommentList.size()
                            + mShowFixedRowCount;
                } else {
                    // shrink the comments and show only recently adde 2
                    // comments only.
                    Collections.reverse(mShowingCommentList);
                    if (mShowingCommentList.size() > 2) {
                        mListCount = mShowFixedRowCount
                                + mInitialCommentsToShowSize;
                    } else {
                        mListCount = mShowingCommentList.size()
                                + mShowFixedRowCount;
                    }
                }
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
                    JSONObject aisleBookmarkProps = new JSONObject();
                    try {
                        aisleBookmarkProps.put("AisleId",
                                getItem(mCurrentAislePosition).getAisleId());
                        aisleBookmarkProps.put("Category",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mCategory);
                        aisleBookmarkProps.put("Lookingfor",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mLookingForItem);
                        aisleBookmarkProps.put("Occasion",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mOccasion);
                        aisleBookmarkProps.put("ScreenName",
                                "Detail View Screen");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mixpanel.track("Aisle-UnBookmarked", aisleBookmarkProps);
                    FlurryAgent.logEvent("BOOKMARK_DETAILSVIEW");
                    if (mBookmarksCount > 0) {
                        mBookmarksCount--;
                    }
                    getItem(mCurrentAislePosition).setWindowBookmarkIndicator(
                            bookmarkStatus);
                    handleBookmark(bookmarkStatus,
                            getItem(mCurrentAislePosition).getAisleId());
                } else {
                    bookmarkStatus = true;
                    JSONObject aisleUnbookmarkProps = new JSONObject();
                    try {
                        aisleUnbookmarkProps.put("AisleId",
                                getItem(mCurrentAislePosition).getAisleId());
                        aisleUnbookmarkProps.put("Category",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mCategory);
                        aisleUnbookmarkProps.put("Lookingfor",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mLookingForItem);
                        aisleUnbookmarkProps.put("Occasion",
                                getItem(mCurrentAislePosition)
                                        .getAisleContext().mOccasion);
                        aisleUnbookmarkProps.put("ScreenName",
                                "Detail View Screen");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mixpanel.track("Aisle-Bookmarked", aisleUnbookmarkProps);
                    FlurryAgent.logEvent("UNBOOKMARK_DETAILSVIEW");
                    mBookmarksCount++;
                    getItem(mCurrentAislePosition).setWindowBookmarkIndicator(
                            bookmarkStatus);
                    handleBookmark(bookmarkStatus,
                            getItem(mCurrentAislePosition).getAisleId());
                }
                getItem(mCurrentAislePosition).getAisleContext().mBookmarkCount = mBookmarksCount;
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .modifyBookmarkList(
                                getItem(mCurrentAislePosition).getAisleId(),
                                bookmarkStatus);
                notifyDataSetChanged();
                setmSetPagerToTrue();
            }
        });
        mViewHolder.likelay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSetPager = false;
                toggleRatingImage();
                setmSetPagerToTrue();
            }
        });
        mViewHolder.likelay.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View arg0) {
                ArrayList<String> userNamesOfImageLikes = new ArrayList<String>();
                AisleImageDetails aisleImageDetails = getItem(
                        mCurrentAislePosition).getImageList().get(
                        mCurrentDispImageIndex);
                if (aisleImageDetails != null
                        && aisleImageDetails.mRatingsList != null
                        && aisleImageDetails.mRatingsList.size() > 0) {
                    for (int i = 0; i < aisleImageDetails.mRatingsList.size(); i++) {
                        if (aisleImageDetails.mRatingsList.get(i).mLiked) {
                            String userName = "";
                            if (aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerFirstName != null
                                    && !(aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerFirstName
                                            .equals("null"))) {
                                userName = userName
                                        + aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerFirstName;
                            }
                            if (aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerLastName != null
                                    && !(aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerLastName
                                            .equals("null"))) {
                                userName = userName
                                        + aisleImageDetails.mRatingsList.get(i).mImageRatingOwnerLastName;
                            }
                            userNamesOfImageLikes.add(userName);
                        }
                    }
                }
                if (userNamesOfImageLikes.size() > 0) {
                    showUserNamesOfImageLikes(mContext, userNamesOfImageLikes);
                } else {
                    Toast.makeText(mContext, "No Likes for this image.",
                            Toast.LENGTH_LONG).show();
                }
                return true;
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
                Collections.reverse(mShowingCommentList);
                
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
                        mCurrentDispImageIndex).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
                    getItem(mCurrentAislePosition).getImageList().get(
                            mCurrentDispImageIndex).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
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
                            mCurrentDispImageIndex).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
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
        profileUserBmp = null;
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
    
    private void sendDataToDb(int imgPosition, String reqType,
            boolean likeOrDislike) {
        getItem(mCurrentAislePosition).findAisleStage(
                getItem(mCurrentAislePosition).getImageList());
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
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .modifyImageRatedStatus(imageId, likeOrDislike);
                mLikeCount = itemDetails.mLikesCount;
                ArrayList<ImageRating> imgRatingList = DataBaseManager
                        .getInstance(mContext).getRatedImagesList(aisleId);
                mImgRating = new ImageRating();
                mImgRating.mAisleId = Long.parseLong(aisleId);
                mImgRating.mImageId = Long.parseLong(imageId);
                mImgRating.mLiked = likeOrDislike;
                VueUser storedVueUser = null;
                try {
                    storedVueUser = Utils.readUserObjectFromFile(mContext,
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                if (storedVueUser != null) {
                    mImgRating.mImageRatingOwnerFirstName = storedVueUser
                            .getFirstName();
                    mImgRating.mImageRatingOwnerLastName = storedVueUser
                            .getLastName();
                }
                for (ImageRating imgRat : imgRatingList) {
                    if (mImgRating.mImageId.longValue() == imgRat.mImageId
                            .longValue()) {
                        mVueTrendingAislesDataModel
                                .setImageLikeOrDisLikeForImage(
                                        getItem(mCurrentAislePosition)
                                                .getImageList().get(
                                                        mCurrentDispImageIndex),
                                        imgRat.mId, likeOrDislike);
                        mImgRating.mId = imgRat.mId.longValue();
                        mImgRating.mLastModifiedTimestamp = imgRat.mLastModifiedTimestamp;
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
        JSONObject aisleLikedProps = new JSONObject();
        try {
            aisleLikedProps.put("ImageId", getItem(mCurrentAislePosition)
                    .getImageList().get(position).mId);
            aisleLikedProps.put("AisleId", getItem(mCurrentAislePosition)
                    .getAisleId());
            aisleLikedProps.put("Category", getItem(mCurrentAislePosition)
                    .getAisleContext().mCategory);
            aisleLikedProps.put("Lookingfor", getItem(mCurrentAislePosition)
                    .getAisleContext().mLookingForItem);
            aisleLikedProps.put("Occasion", getItem(mCurrentAislePosition)
                    .getAisleContext().mOccasion);
            aisleLikedProps.put("ScreenName", "Detail View Screen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Image-Liked", aisleLikedProps);
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
        if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
            
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
        } else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_NONE_STATUS) {
            
            getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(
                    mCurrentAislePosition).getImageList().get(position).mLikesCount + 1;
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
            getItem(mCurrentAislePosition).mTotalLikesCount = getItem(mCurrentAislePosition).mTotalLikesCount + 1;
            
            sendDataToDb(position, CHANGE_LIKES, true);
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = getItem(mCurrentAislePosition).getImageList()
                    .get(position).mLikesCount;
            mSetPager = false;
            notifyAdapter();
            setmSetPagerToTrue();
        }
    }
    
    private void onChangeDislikesCount(int position) {
        JSONObject aisleUnLikedProps = new JSONObject();
        try {
            aisleUnLikedProps.put("ImageId", getItem(mCurrentAislePosition)
                    .getImageList().get(position).mId);
            aisleUnLikedProps.put("AisleId", getItem(mCurrentAislePosition)
                    .getAisleId());
            aisleUnLikedProps.put("Category", getItem(mCurrentAislePosition)
                    .getAisleContext().mCategory);
            aisleUnLikedProps.put("Lookingfor", getItem(mCurrentAislePosition)
                    .getAisleContext().mLookingForItem);
            aisleUnLikedProps.put("Occasion", getItem(mCurrentAislePosition)
                    .getAisleContext().mOccasion);
            aisleUnLikedProps.put("ScreenName", "Detail View Screen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Image-Unliked", aisleUnLikedProps);
        FlurryAgent.logEvent("DIS_LIKES_DETAILSVIEW");
        if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
            // false
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
            if (getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount > 0) {
                getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(
                        mCurrentAislePosition).getImageList().get(position).mLikesCount - 1;
                getItem(mCurrentAislePosition).mTotalLikesCount = getItem(mCurrentAislePosition).mTotalLikesCount - 1;
            }
            sendDataToDb(position, CHANGE_LIKES, false);
        } else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_NONE_STATUS) {
            getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = getItem(mCurrentAislePosition).getImageList()
                    .get(position).mLikesCount;
            mSetPager = false;
            notifyAdapter();
            setmSetPagerToTrue();
        }
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
        // get the rated images by this user from the db.
        // this table contains all the rated image list by this user.
        // if any image id exist in the table means it is a rated image by this
        // user.
        ArrayList<ImageRating> imgRatingList = DataBaseManager.getInstance(
                mContext).getRatedImagesList(
                getItem(mCurrentAislePosition).getAisleId());
        
        for (AisleImageDetails imgDetail : aisleImgDetais) {
            
            for (ImageRating imgRating : imgRatingList) {
                if (imgRating.mImageId == Long.parseLong(imgDetail.mId)
                        && imgRating.mLiked && imgRating.mIsUserRating == 1) {
                    imgDetail.mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
                }
            }
        }
    }
    
    public void updateListCount(String newComment) {
        mListCount = mListCount + 1;
    }
    
    // create the comment for the image
    public void createComment(String commentString) {
        VueUser storedVueUser = null;
        String firstName = "";
        String lastName = "";
        String userId = null;
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
            firstName = storedVueUser.getFirstName();
            lastName = storedVueUser.getLastName();
            userId = Long.valueOf(storedVueUser.getId()).toString();
        }
        if (commenterUrl != null && commenterUrl.length() < 6) {
            commenterUrl = null;
        }
        ImageComments comments = new ImageComments();
        comments.mComment = commentString;
        comments.mCommenterUrl = commenterUrl;
        comments.mCommenterFirstName = firstName;
        comments.mCommenterLastName = lastName;
        
        if (commentString == null || commentString.length() < 1) {
            return;
        }
        getItem(mCurrentAislePosition).getImageList().get(
                mCurrentDispImageIndex).mCommentsList.add(comments);
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
        Collections.reverse(mShowingCommentList);
        if (mShowingCommentList.size() < mShowFixedRowCount) {
            mListCount = mShowingCommentList.size() + mShowFixedRowCount;
        } else {
            mListCount = mShowFixedRowCount + mInitialCommentsToShowSize;
        }
        // send the created comment to the server.
        final ImageCommentRequest imgComment = new ImageCommentRequest();
        imgComment.setComment(commentString);
        imgComment.setLastModifiedTimestamp(System.currentTimeMillis());
        imgComment.setOwnerUserId(Long.parseLong(userId));
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
            showComment.mCommenterFirstName = comment.mCommenterFirstName;
            showComment.mCommenterLastName = comment.mCommenterLastName;
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
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
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
            NetworkImageView browserImage = (NetworkImageView) myView
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
                    // these listener does nothing. But to give the
                    // control to the
                    // listview when touch out side of the image in the browser.
                    detailsImageClickListenr.onImageClicked();
                }
            });
            full_bg_image.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    detailsImageClickListenr.onImageLongPress();
                    return false;
                }
            });
            AisleImageDetails imageDetails = getItem(mCurrentAislePosition)
                    .getImageList().get(position);
            if (mCurrentDispImageIndex == position) {
                boolean editLayVisibility = false;
                boolean starLayVisibility = false;
                boolean isMostLikedImage = false;
                if (getItem(mCurrentAislePosition).getImageList().get(position).mOwnerUserId != null
                        && getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
                    if (Long.parseLong(getItem(mCurrentAislePosition)
                            .getImageList().get(position).mOwnerUserId) == mUserId
                            || Long.parseLong(getItem(mCurrentAislePosition)
                                    .getAisleContext().mUserId) == mUserId) {
                        editLayVisibility = true;
                        
                    } else {
                        editLayVisibility = false;
                    }
                }
                
                if (imageDetails.mHasMostLikes) {
                    starLayVisibility = true;
                    if (imageDetails.mSameMostLikes) {
                        isMostLikedImage = true;
                    } else {
                        isMostLikedImage = false;
                    }
                } else {
                    starLayVisibility = false;
                }
                mswipeListner.onUpdateLikeStatus(editLayVisibility,
                        starLayVisibility, isMostLikedImage);
            }
            loadBitmap(browserImage, getItem(mCurrentAislePosition)
                    .getImageList().get(position).mImageUrl, VueApplication
                    .getInstance().getVueDetailsCardWidth(), mBestHeight);
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
            mSetPager = false;
            mCurrentDispImageIndex = position;
            if (detailsImageClickListenr != null)
                detailsImageClickListenr.onImageSwipe(position);
            mswipeListner.onAllowListResponse();
            setmSetPagerToTrue();
            if (getItem(mCurrentAislePosition).getImageList().size() == position) {
                position = getItem(mCurrentAislePosition).getImageList().size() - 1;
            }
            boolean editLayVisibility = false;
            boolean starLayVisibility = false;
            boolean isMostLikedImage = false;
            if (getItem(mCurrentAislePosition).getImageList().get(position).mOwnerUserId != null
                    && getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
                if (Long.parseLong(getItem(mCurrentAislePosition)
                        .getImageList().get(position).mOwnerUserId) == mUserId
                        || Long.parseLong(getItem(mCurrentAislePosition)
                                .getAisleContext().mUserId) == mUserId) {
                    editLayVisibility = true;
                } else {
                    editLayVisibility = false;
                }
            }
            if (getItem(mCurrentAislePosition).getImageList().get(position).mHasMostLikes) {
                starLayVisibility = true;
                
                if (getItem(mCurrentAislePosition).getImageList().get(position).mSameMostLikes) {
                    isMostLikedImage = true;
                } else {
                    isMostLikedImage = false;
                }
            } else {
                starLayVisibility = false;
            }
            if (mPrevPosition == position) {
            } else if (mPrevPosition < position) {
                mswipeListner.onAisleSwipe("Left", position, editLayVisibility,
                        starLayVisibility, isMostLikedImage);
                
            } else {
                mswipeListner.onAisleSwipe("Right", position,
                        editLayVisibility, starLayVisibility, isMostLikedImage);
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
    
    public void setmSetPagerToTrue() {
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                // to make sure the pager is not refresh when like and dislike
                // the image and when bookmarked the aislse.
                mSetPager = true;
            }
        }, mLikePageDelay);
    }
    
    private void loadBitmap(NetworkImageView imageView, String url, int width,
            int height) {
        ((NetworkImageView) imageView).setImageUrl(url, VueApplication
                .getInstance().getImageCacheLoader(), width, height,
                NetworkImageView.BitmapProfile.ProfileDetailsView);
    }
    
    private void findAisleStage() {
        int likesCount = 0, commentsCount = 0, totalCount;
        ArrayList<AisleImageDetails> imageDetailsList = getItem(
                mCurrentAislePosition).getImageList();
        for (int index = 0; index < imageDetailsList.size(); index++) {
            AisleImageDetails imageDetails = imageDetailsList.get(index);
            int tempLikesCount = imageDetails.mLikesCount;
            if (tempLikesCount > likesCount) {
                likesCount = tempLikesCount;
            }
            int tempCommentsCount = imageDetails.mCommentsList.size();
            if (tempCommentsCount > commentsCount) {
                commentsCount = tempCommentsCount;
            }
        }
        
        totalCount = likesCount + commentsCount;
        if (totalCount == 0) {
            mAisleCureentStage = VueConstants.AISLE_STATGE_ONE;
        } else if (likesCount >= 3 || commentsCount >= 3) {
            mAisleCureentStage = VueConstants.AISLE_STAGE_THREE;
        } else if (likesCount < 3 || commentsCount < 3) {
            mAisleCureentStage = VueConstants.AISLE_STAGE_TWO;
        }
    }
    
    private void showUserNamesOfImageLikes(Context context,
            ArrayList<String> userNamesOfImageLikes) {
        final Dialog dialog = new Dialog(context,
                R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sharedialogue);
        ListView listview = (ListView) dialog.findViewById(R.id.networklist);
        TextView okbuton = (TextView) dialog.findViewById(R.id.shownetworkok);
        TextView dialogtitle = (TextView) dialog.findViewById(R.id.dialogtitle);
        dialogtitle.setText("People Who Like This");
        okbuton.setText("OK");
        okbuton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        listview.setAdapter(new UserNamesAdapter(userNamesOfImageLikes, context));
        listview.setDivider(mContext.getResources().getDrawable(
                R.drawable.share_dialog_divider));
        dialog.show();
    }
    
    private class UserNamesAdapter extends BaseAdapter {
        ArrayList<String> mUserNameList;
        Context mContext = null;
        
        public UserNamesAdapter(ArrayList<String> userNameList, Context context) {
            mUserNameList = userNameList;
            mContext = context;
        }
        
        @Override
        public int getCount() {
            return mUserNameList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return position;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            Holder holder = null;
            if (convertView == null) {
                
                holder = new Holder();
                LayoutInflater mLayoutInflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mLayoutInflater.inflate(R.layout.hintpopup, null);
                holder.textone = (TextView) convertView
                        .findViewById(R.id.gmail);
                holder.texttwo = (TextView) convertView.findViewById(R.id.vue);
                holder.imageone = (ImageView) convertView
                        .findViewById(R.id.shareicon);
                holder.imagetwo = (ImageView) convertView
                        .findViewById(R.id.shareicon2);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.imageone.setVisibility(View.GONE);
            holder.imagetwo.setVisibility(View.GONE);
            holder.texttwo.setVisibility(View.GONE);
            holder.textone.setText(mUserNameList.get(position));
            return convertView;
        }
    }
    
    private class Holder {
        TextView textone, texttwo;
        ImageView imageone, imagetwo;
    }
}