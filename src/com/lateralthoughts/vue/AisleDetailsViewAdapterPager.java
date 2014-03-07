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
import android.content.Intent;
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
import com.lateralthoughts.vue.VueAisleDetailsViewFragment.ShareViaVueListner;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Comment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.parser.ImageComments;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
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
    private long mUserId;
    private ImageLoader mImageLoader;
    private ShareViaVueListner mShareViaVueListner;
    private int mPrevPosition;
    private PageListener pageListener;
    private DetailImageClickListener detailsImageClickListenr;
    public boolean mSetPager = true;
    private Bitmap profileUserBmp;
    private MixpanelAPI mixpanel;
    AisleWindowContent mCurrentAisle = null;
    private boolean isFromPendingScreen = false;
    private boolean isAisleOwner = false;
    
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
        if (VueApplication.getInstance().getPedningAisle() != null) {
            mCurrentAisle = VueApplication.getInstance().getPedningAisle();
            // VueApplication.getInstance().setPendingAisle(null);
            mCurrentAislePosition = 0;
            if (mCurrentAisle != null) {
                ArrayList<AisleImageDetails> imageList = mCurrentAisle
                        .getImageList();
                if (imageList != null && imageList.size() > 0) {
                    for (int index = 0; index < imageList.size(); index++) {
                        AisleImageDetails imageDetails = imageList.get(index);
                        if (imageDetails.mImageUrl
                                .equalsIgnoreCase(VueConstants.NO_IMAGE_URL)) {
                            isFromPendingScreen = true;
                        }
                    }
                } else {
                    isFromPendingScreen = true;
                }
            }
            
        } else {
            for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
                if (getItem(i).getAisleId().equalsIgnoreCase(
                        VueApplication.getInstance().getClickedWindowID())) {
                    // get the clicked window position.
                    mCurrentAislePosition = i;
                    isFromPendingScreen = false;
                    break;
                }
            }
            mCurrentAisle = getItem(mCurrentAislePosition);
        }
        // Some times when the user returning from the other apps or from the
        // browser
        // apps lost data to avoid force close checking the current ailse.
        if (mCurrentAisle == null || mCurrentAisle.getAisleContext() == null) {
            mswipeListner.finishScreen();
            return;
        }
        if (mCurrentAisle.getAisleContext().mUserId
                .equalsIgnoreCase(VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler().getUserId())) {
            isAisleOwner = true;
        } else {
            isAisleOwner = false;
        }
        if (VueApplication.getInstance().getmAisleImgCurrentPos() > mCurrentAisle
                .getImageList().size() - 1) {
            VueApplication.getInstance().setmAisleImgCurrentPos(
                    mCurrentAisle.getImageList().size() - 1);
        }
        if (!isFromPendingScreen)
            setImageRating();
        if (mCurrentAisle != null) {
            String occasion = mCurrentAisle.getAisleContext().mOccasion;
            if (occasion != null) {
                if (occasion.length() > 0) {
                    occasion = occasion.substring(0, 1).toUpperCase()
                            + occasion.substring(1).toLowerCase();
                }
                String lookingFor = mCurrentAisle.getAisleContext().mLookingForItem;
                lookingFor = lookingFor.substring(0, 1).toUpperCase()
                        + lookingFor.substring(1).toLowerCase();
                mswipeListner.setOccasion(occasion + " " + lookingFor);
            }
            mBookmarksCount = mCurrentAisle.getAisleContext().mBookmarkCount;
            VueApplication.getInstance().setClickedWindowCount(
                    mCurrentAisle.getImageList().size());
            
            for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
                if (mCurrentAisle.getImageList().get(i).mAvailableHeight > mBestHeight) {
                    // find the best height among all images.
                    mBestHeight = mCurrentAisle.getImageList().get(i).mAvailableHeight;
                }
                
                mCommentsMapList.put(i,
                        mCurrentAisle.getImageList().get(i).mCommentsList);
            }
            ArrayList<ImageComments> imgComments = mCurrentAisle.getImageList()
                    .get(VueApplication.getInstance().getmAisleImgCurrentPos()).mCommentsList;
            prepareCommentList(imgComments);
            Collections.reverse(mShowingCommentList);
            int imgPosition = 0;
            if (VueApplication.getInstance().getmAisleImgCurrentPos() < mCurrentAisle
                    .getImageList().size()) {
                imgPosition = VueApplication.getInstance()
                        .getmAisleImgCurrentPos();
            }
            mCurrentDispImageIndex = VueApplication.getInstance()
                    .getmAisleImgCurrentPos();
            mLikes = mCurrentAisle.getImageList().get(imgPosition).mLikesCount;
            // to the aisle is bookmarked already or not.
            boolean isBookmarked = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .isAisleBookmarked(mCurrentAisle.getAisleId());
            
            isBookmarked = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .checkIsAisleBookmarked(mCurrentAisle.getAisleId());
            
            if (isBookmarked) {
                mCurrentAisle.setWindowBookmarkIndicator(isBookmarked);
            }
            mBookmarksCount = mCurrentAisle.getAisleContext().mBookmarkCount;
            if (mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mCommentsList
                    .size() < mShowFixedRowCount) {
                mListCount = mCurrentAisle.getImageList().get(
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
                        aisleViewedProps.put("Aisle Id",
                                mCurrentAisle.getAisleId());
                        aisleViewedProps.put("Owner Id",
                                mCurrentAisle.getAisleContext().mUserId);
                        aisleViewedProps.put("Owner Name",
                                mCurrentAisle.getAisleContext().mFirstName);
                        aisleViewedProps.put("Share Count",
                                mCurrentAisle.getAisleContext().mShareCount);
                        aisleViewedProps.put("Images Count", mCurrentAisle
                                .getImageList().size());
                        aisleViewedProps.put("Category",
                                mCurrentAisle.getAisleContext().mCategory);
                        aisleViewedProps.put("Looking For",
                                mCurrentAisle.getAisleContext().mLookingForItem);
                        aisleViewedProps.put("Occasion",
                                mCurrentAisle.getAisleContext().mOccasion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mixpanel.track("Aisle Viewed", aisleViewedProps);
                }
                // wait time for flurry session starts
            }, mWaitTime);
            
        }
        mBestHeight = Utils.modifyHeightForDetailsView(mCurrentAisle
                .getImageList());
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
            // mViewHolder.myPager.setOffscreenPageLimit(4);
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
        if (mCurrentAisle.getWindowBookmarkIndicator()) {
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
        if (mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
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
            mViewHolder.decisionLay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    VueUser storedVueUser = null;
                    boolean isUserAisleFlag = false;
                    try {
                        storedVueUser = Utils.readUserObjectFromFile(mContext,
                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                        if (mCurrentAisle.getAisleContext().mUserId
                                .equals(String.valueOf(storedVueUser.getId()))) {
                            isUserAisleFlag = true;
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    if (isUserAisleFlag) {
                        Intent intent = new Intent(mContext,
                                DecisionScreen.class);
                        
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(
                                mContext,
                                "Sorry, You can't make decision on another person aisle.",
                                Toast.LENGTH_LONG).show();
                    }
                    
                }
            });
            String descisionText = " ";
            if (Long.parseLong(mCurrentAisle.getAisleContext().mUserId) == mUserId) {
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
                if (mCurrentAisle.getImageList().get(position).mOwnerUserId != null
                        && mCurrentAisle.getAisleContext().mUserId != null) {
                    if (Long.parseLong(mCurrentAisle.getImageList().get(
                            position).mOwnerUserId) == mUserId
                            || Long.parseLong(mCurrentAisle.getAisleContext().mUserId) == mUserId
                            || (VueApplication.getInstance().getmUserEmail() != null && VueApplication
                                    .getInstance().getmUserEmail()
                                    .equals(VueConstants.ADMIN_MAIL_ADDRESS))) {
                        editLayVisibility = true;
                        
                    } else {
                        editLayVisibility = false;
                    }
                }
                
                if ((mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mHasMostLikes)) {
                    starLayVisibility = true;
                    if ((mCurrentAisle.getImageList().get(
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
                if (mCurrentAisle.getAisleContext().mDescription != null
                        && mCurrentAisle.getAisleContext().mDescription
                                .length() > 1) {
                    mViewHolder.descriptionlay.setVisibility(View.VISIBLE);
                    mViewHolder.aisleDescription.setText(mCurrentAisle
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
                
                if (mCurrentAisle.getAisleContext().mFirstName != null
                        && mCurrentAisle.getAisleContext().mLastName != null) {
                    mVueusername = mCurrentAisle.getAisleContext().mFirstName
                            + mCurrentAisle.getAisleContext().mLastName;
                } else if (mCurrentAisle.getAisleContext().mFirstName != null) {
                    if (mCurrentAisle.getAisleContext().mFirstName
                            .equals("Anonymous")) {
                        mVueusername = VueApplication.getInstance()
                                .getmUserInitials();
                    } else {
                        mVueusername = mCurrentAisle.getAisleContext().mFirstName;
                    }
                } else if (mCurrentAisle.getAisleContext().mLastName != null) {
                    mVueusername = mCurrentAisle.getAisleContext().mLastName;
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
                    ArrayList<ImageComments> imgComments = mCurrentAisle
                            .getImageList().get(mCurrentDispImageIndex).mCommentsList;
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
                if (loginChcecking()) {
                    mSetPager = false;
                    mIsBookImageClciked = true;
                    boolean bookmarkStatus = false;
                    if (mCurrentAisle.getWindowBookmarkIndicator()) {
                        String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
                        String userId = VueTrendingAislesDataModel
                                .getInstance(mContext).getNetworkHandler()
                                .getUserId();
                        boolean isOwner = false;
                        if (imgOwnerId == userId) {
                            isOwner = true;
                        }
                        JSONObject aisleBookmarkProps = new JSONObject();
                        try {
                            aisleBookmarkProps.put("AisleId",
                                    mCurrentAisle.getAisleId());
                            aisleBookmarkProps.put("Is Aisle Owner", isOwner);
                            aisleBookmarkProps.put("Owner Name",
                                    mCurrentAisle.getAisleContext().mFirstName);
                            aisleBookmarkProps.put("Share Count",
                                    mCurrentAisle.getAisleContext().mShareCount);
                            aisleBookmarkProps.put("Images Count",
                                    mCurrentAisle.getImageList().size());
                            aisleBookmarkProps.put("Category",
                                    mCurrentAisle.getAisleContext().mCategory);
                            aisleBookmarkProps.put(
                                    "Looking For",
                                    mCurrentAisle.getAisleContext().mLookingForItem);
                            aisleBookmarkProps.put("Occasion",
                                    mCurrentAisle.getAisleContext().mOccasion);
                            aisleBookmarkProps.put("Shared From",
                                    "Detail View Screen");
                            
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mixpanel.track("Bookmark Removed", aisleBookmarkProps);
                        if (mBookmarksCount > 0) {
                            mBookmarksCount--;
                        }
                        mCurrentAisle
                                .setWindowBookmarkIndicator(bookmarkStatus);
                        handleBookmark(bookmarkStatus,
                                mCurrentAisle.getAisleId());
                    } else {
                        bookmarkStatus = true;
                        String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
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
                            
                            mCurrentAisle.getAisleId());
                            aisleUnbookmarkProps.put("Is Aisle Owner", isOwner);
                            aisleUnbookmarkProps.put("Owner Name",
                                    mCurrentAisle.getAisleContext().mFirstName);
                            aisleUnbookmarkProps.put("Share Count",
                                    mCurrentAisle.getAisleContext().mShareCount);
                            aisleUnbookmarkProps.put("Images Count",
                                    mCurrentAisle.getImageList().size());
                            
                            aisleUnbookmarkProps.put("Category",
                                    mCurrentAisle.getAisleContext().mCategory);
                            aisleUnbookmarkProps.put(
                                    "Lookingfor",
                                    mCurrentAisle.getAisleContext().mLookingForItem);
                            aisleUnbookmarkProps.put("Occasion",
                            
                            mCurrentAisle.getAisleContext().mOccasion);
                            aisleUnbookmarkProps.put("Bookmarked From",
                                    "Detail View Screen");
                            
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mixpanel.track("Aisle Bookmarked", aisleUnbookmarkProps);
                        mBookmarksCount++;
                        mCurrentAisle
                                .setWindowBookmarkIndicator(bookmarkStatus);
                        handleBookmark(bookmarkStatus,
                                mCurrentAisle.getAisleId());
                    }
                    mCurrentAisle.getAisleContext().mBookmarkCount = mBookmarksCount;
                    VueTrendingAislesDataModel
                            .getInstance(VueApplication.getInstance())
                            .getNetworkHandler()
                            .modifyBookmarkList(mCurrentAisle.getAisleId(),
                                    bookmarkStatus);
                    notifyDataSetChanged();
                    setmSetPagerToTrue();
                }
            }
        });
        mViewHolder.likelay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (loginChcecking()) {
                    mSetPager = false;
                    toggleRatingImage();
                    setmSetPagerToTrue();
                }
            }
        });
        mViewHolder.likelay.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View arg0) {
                ArrayList<String> userNamesOfImageLikes = new ArrayList<String>();
                AisleImageDetails aisleImageDetails = mCurrentAisle
                        .getImageList().get(mCurrentDispImageIndex);
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
        String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
        String userId = VueTrendingAislesDataModel.getInstance(mContext)
                .getNetworkHandler().getUserId();
        boolean isOwner = false;
        if (imgOwnerId == userId) {
            isOwner = true;
        }
        JSONObject aisleSharedProps = new JSONObject();
        try {
            aisleSharedProps.put("Aisle Id", mCurrentAisle.getAisleId());
            aisleSharedProps.put("Is Aisle Owner", isOwner);
            aisleSharedProps.put("Owner Name",
                    mCurrentAisle.getAisleContext().mFirstName);
            aisleSharedProps.put("Share Count",
                    mCurrentAisle.getAisleContext().mShareCount);
            aisleSharedProps.put("Images Count", mCurrentAisle.getImageList()
                    .size());
            aisleSharedProps.put("Category",
                    mCurrentAisle.getAisleContext().mCategory);
            aisleSharedProps.put("Lookingfor",
                    mCurrentAisle.getAisleContext().mLookingForItem);
            aisleSharedProps.put("Occasion",
                    mCurrentAisle.getAisleContext().mOccasion);
            aisleSharedProps.put("Shared From", "Detail View Screen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        mCurrentAisle.getAisleContext().mShareCount = mCurrentAisle
                .getAisleContext().mShareCount + 1;
        mCurrentAisle.setmShareIndicator(true);
        mShare = new ShareDialog(context, activity, mixpanel, aisleSharedProps);
        
        FileCache ObjFileCache = new FileCache(context);
        ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
        if (mCurrentAisle.getImageList() != null
                && mCurrentAisle.getImageList().size() > 0) {
            String isUserAisle = "0";
            if (String.valueOf(VueApplication.getInstance().getmUserId())
                    .equals(mCurrentAisle.getAisleContext().mUserId)) {
                isUserAisle = "1";
            }
            for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
                clsShare obj = new clsShare(
                        mCurrentAisle.getImageList().get(i).mImageUrl,
                        ObjFileCache.getFile(
                                mCurrentAisle.getImageList().get(i).mImageUrl)
                                .getPath(),
                        mCurrentAisle.getAisleContext().mLookingForItem,
                        mCurrentAisle.getAisleContext().mFirstName + " "
                                + mCurrentAisle.getAisleContext().mLastName,
                        isUserAisle, mCurrentAisle.getAisleContext().mAisleId,
                        mCurrentAisle.getImageList().get(i).mId);
                imageUrlList.add(obj);
            }
            mShare.share(
                    imageUrlList,
                    mCurrentAisle.getAisleContext().mOccasion,
                    (mCurrentAisle.getAisleContext().mFirstName + " " + mCurrentAisle
                            .getAisleContext().mLastName),
                    mCurrentDispImageIndex, mShareViaVueListner, null, null);
        }
        if (mCurrentAisle.getImageList() != null
                && mCurrentAisle.getImageList().size() > 0) {
            FileCache ObjFileCache1 = new FileCache(context);
            for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
                final File f = ObjFileCache1.getFile(mCurrentAisle
                        .getImageList().get(i).mImageUrl);
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
                    if (mCurrentAisle.getImageList().get(i).mImageUrl != null) {
                        @SuppressWarnings("unchecked")
                        ImageRequest imagerequestObj = new ImageRequest(
                                mCurrentAisle.getImageList().get(i).mImageUrl,
                                listener, 0, 0, null, errorListener);
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
            if (loginChcecking()) {
                onHandleLikeEvent();
            }
        }
        
        @Override
        public void onImageLongPress() {
            if (loginChcecking()) {
                onHandleDisLikeEvent();
            }
        }
        
        @Override
        public void onImageSwipe(int position) {
            // int likeCount = 0;
            if (position >= 0
                    && position < VueApplication.getInstance()
                            .getClickedWindowCount()) {
                mCurrentDispImageIndex = position;
                
                mLikes = mCurrentAisle.getImageList().get(position).mLikesCount;
                ArrayList<ImageComments> imgComments = mCurrentAisle
                        .getImageList().get(mCurrentDispImageIndex).mCommentsList;
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
                mswipeListner.setFindAtText(mCurrentAisle.getImageList().get(
                        position).mDetailsUrl);
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
        if (mCurrentDispImageIndex >= 0
                && mCurrentDispImageIndex < mCurrentAisle.getImageList().size()) {
            if (mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
                mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
                if (mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikesCount > 0) {
                    mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikesCount = mCurrentAisle
                            .getImageList().get(mCurrentDispImageIndex).mLikesCount - 1;
                    sendDataToDb(mCurrentDispImageIndex, CHANGE_LIKES, false);
                }
                String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
                String userId = VueTrendingAislesDataModel
                        .getInstance(mContext).getNetworkHandler().getUserId();
                boolean isOwner = false;
                if (imgOwnerId == userId) {
                    isOwner = true;
                }
                
                JSONObject aisleLikedProps = new JSONObject();
                try {
                    VueUser storedVueUser = null;
                    storedVueUser = Utils.readUserObjectFromFile(
                            VueApplication.getInstance(),
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                    String userName = storedVueUser.getFirstName() + " "
                            + storedVueUser.getLastName();
                    aisleLikedProps.put("Image Id", mCurrentAisle
                            .getImageList().get(mCurrentDispImageIndex).mId);
                    aisleLikedProps.put("Aisle Id", mCurrentAisle.getAisleId());
                    aisleLikedProps.put("Is Aisle Owner", isOwner);
                    aisleLikedProps.put("Image Position",
                            mCurrentDispImageIndex);
                    aisleLikedProps.put("Owner Name",
                            mCurrentAisle.getAisleContext().mFirstName);
                    aisleLikedProps.put(
                            "Like Count",
                            mCurrentAisle.getImageList().get(
                                    mCurrentDispImageIndex).mLikesCount);
                    aisleLikedProps.put("Category",
                            mCurrentAisle.getAisleContext().mCategory);
                    aisleLikedProps.put("Looking For",
                            mCurrentAisle.getAisleContext().mLookingForItem);
                    aisleLikedProps.put("Occasion",
                            mCurrentAisle.getAisleContext().mOccasion);
                    aisleLikedProps.put("Unliked From", "Detail View Screen");
                    aisleLikedProps.put("Image Unliked By", userName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mixpanel.track("Image Unliked", aisleLikedProps);
                
            } else {
                mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
                mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikesCount = mCurrentAisle
                        .getImageList().get(mCurrentDispImageIndex).mLikesCount + 1;
                sendDataToDb(mCurrentDispImageIndex, CHANGE_LIKES, true);
                String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
                String userId = VueTrendingAislesDataModel
                        .getInstance(mContext).getNetworkHandler().getUserId();
                boolean isOwner = false;
                if (imgOwnerId == userId) {
                    isOwner = true;
                }
                JSONObject aisleLikedProps = new JSONObject();
                try {
                    VueUser storedVueUser = null;
                    storedVueUser = Utils.readUserObjectFromFile(
                            VueApplication.getInstance(),
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                    String userName = storedVueUser.getFirstName() + " "
                            + storedVueUser.getLastName();
                    aisleLikedProps.put("Image Id", mCurrentAisle
                            .getImageList().get(mCurrentDispImageIndex).mId);
                    aisleLikedProps.put("Aisle Id", mCurrentAisle.getAisleId());
                    aisleLikedProps.put("Image Position",
                            mCurrentDispImageIndex);
                    aisleLikedProps.put("Is Aisle Owner", isOwner);
                    aisleLikedProps.put("Owner Name",
                            mCurrentAisle.getAisleContext().mFirstName);
                    aisleLikedProps.put(
                            "Like Count",
                            mCurrentAisle.getImageList().get(
                                    mCurrentDispImageIndex).mLikesCount);
                    aisleLikedProps.put("Category",
                            mCurrentAisle.getAisleContext().mCategory);
                    aisleLikedProps.put("LookingfFor",
                            mCurrentAisle.getAisleContext().mLookingForItem);
                    aisleLikedProps.put("Occasion",
                            mCurrentAisle.getAisleContext().mOccasion);
                    aisleLikedProps.put("Liked From", "Detail View Screen");
                    aisleLikedProps.put("Image Liked By", userName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mixpanel.track("Image Liked", aisleLikedProps);
                
            }
            mLikes = mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mLikesCount;
        }
        mIsLikeImageClicked = true;
        findMostLikesImage();
        notifyAdapter();
    }
    
    public void changeLikesCountFromCopmareScreen(int position, String eventType) {
        if (loginChcecking()) {
            if (position >= 0 && position < mCurrentAisle.getImageList().size()) {
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
        // increase the likes count
        if (mCurrentDispImageIndex >= 0
                && mCurrentDispImageIndex < mCurrentAisle.getImageList().size()) {
            onChangeLikesCount(mCurrentDispImageIndex);
            mIsLikeImageClicked = true;
        }
    }
    
    private void onHandleDisLikeEvent() {
        // decrease the likes count
        if (mCurrentDispImageIndex >= 0
                && mCurrentDispImageIndex < mCurrentAisle.getImageList().size()) {
            mIsLikeImageClicked = true;
            onChangeDislikesCount(mCurrentDispImageIndex);
        }
    }
    
    public void setAisleBrowserObjectsNull() {
        profileUserBmp = null;
    }
    
    public void addAisleToContentWindow() {
        setAisleBrowserObjectsNull();
        mswipeListner.onResetAdapter();
    }
    
    public void updateAisleListAdapter() {
        int imageListSize = mCurrentAisle.getImageList().size();
        VueApplication.getInstance().setClickedWindowCount(imageListSize);
        VueApplication.getInstance().setmAisleImgCurrentPos(0);
        setAisleBrowserObjectsNull();
        mswipeListner.onResetAdapter();
    }
    
    public ArrayList<AisleImageDetails> getImageList() {
        return mCurrentAisle.getImageList();
    }
    
    int mLikeCount = 0;
    ImageRating mImgRating;
    
    private void sendDataToDb(int imgPosition, String reqType,
            boolean likeOrDislike) {
        if (isFromPendingScreen) {
            return;
        }
        mCurrentAisle.findAisleStage(mCurrentAisle.getImageList());
        String aisleId = null;
        String imageId = null;
        AisleImageDetails itemDetails;
        if (mCurrentAisle.getImageList() != null
                && mCurrentAisle.getImageList().size() != 0) {
            aisleId = mCurrentAisle.getAisleId();
            itemDetails = mCurrentAisle.getImageList().get(imgPosition);
            imageId = itemDetails.mId;
            if (reqType.equals(CHANGE_BOOKMARK)) {
                // aisleId,imageId,bookMarksCount,bookmarkIndicator
            } else if (reqType.equals(CHANGE_COMMENT)) {
                // aisleId,imageId,comment
                if (itemDetails.mCommentsList == null) {
                    mCurrentAisle.getImageList().get(0).mCommentsList = new ArrayList<ImageComments>();
                }
            } else if (reqType.equals(CHANGE_LIKES)) {
                // aisleId,imageId,likesCount,likeStatus
                int likeCountValue = 0;
                if (likeOrDislike) {
                    likeCountValue = 2;
                } else {
                    likeCountValue = -2;
                }
                // add user point 2 for every like
                Utils.saveUserPoints(VueConstants.USER_LIKES_POINTS,
                        likeCountValue, mContext);
                // update the like ids list
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .modifyImageRatedStatus(imageId, likeOrDislike);
                mLikeCount = itemDetails.mLikesCount;
                // get all liked images list form db.
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
                // if image have already an rate id get it from db.
                for (ImageRating imgRat : imgRatingList) {
                    if (mImgRating.mImageId.longValue() == imgRat.mImageId
                            .longValue()) {
                        mVueTrendingAislesDataModel
                                .setImageLikeOrDisLikeForImage(
                                        mCurrentAisle.getImageList().get(
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
        return mCurrentAisle.getAisleContext();
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
        String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
        String userId = VueTrendingAislesDataModel.getInstance(mContext)
                .getNetworkHandler().getUserId();
        boolean isOwner = false;
        if (imgOwnerId == userId) {
            isOwner = true;
        }
        JSONObject aisleLikedProps = new JSONObject();
        try {
            VueUser storedVueUser = null;
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
            String userName = storedVueUser.getFirstName() + " "
                    + storedVueUser.getLastName();
            aisleLikedProps.put("Image Id",
                    mCurrentAisle.getImageList().get(position).mId);
            aisleLikedProps.put("Aisle Id", mCurrentAisle.getAisleId());
            aisleLikedProps.put("Image Position", position);
            aisleLikedProps.put("Is Aisle Owner", isOwner);
            aisleLikedProps.put("Owner Name",
                    mCurrentAisle.getAisleContext().mFirstName);
            aisleLikedProps
                    .put("Like Count",
                            mCurrentAisle.getImageList().get(
                                    mCurrentDispImageIndex).mLikesCount);
            aisleLikedProps.put("Category",
                    mCurrentAisle.getAisleContext().mCategory);
            aisleLikedProps.put("Looking For",
                    mCurrentAisle.getAisleContext().mLookingForItem);
            aisleLikedProps.put("Occasion",
                    mCurrentAisle.getAisleContext().mOccasion);
            aisleLikedProps.put("Liked From", "Detail View Screen");
            aisleLikedProps.put("Image Liked By", userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mixpanel.track("Image Liked", aisleLikedProps);
        if (mCurrentAisle.getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
            mCurrentAisle.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
        } else if (mCurrentAisle.getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_NONE_STATUS) {
            
            mCurrentAisle.getImageList().get(position).mLikesCount = mCurrentAisle
                    .getImageList().get(position).mLikesCount + 1;
            mCurrentAisle.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
            mCurrentAisle.mTotalLikesCount = mCurrentAisle.mTotalLikesCount + 1;
            
            sendDataToDb(position, CHANGE_LIKES, true);
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = mCurrentAisle.getImageList().get(position).mLikesCount;
            mSetPager = false;
            notifyAdapter();
            setmSetPagerToTrue();
        }
    }
    
    private void onChangeDislikesCount(int position) {
        String imgOwnerId = mCurrentAisle.getAisleContext().mAisleOwnerImageURL;
        String userId = VueTrendingAislesDataModel.getInstance(mContext)
                .getNetworkHandler().getUserId();
        boolean isOwner = false;
        if (imgOwnerId == userId) {
            isOwner = true;
        }
        JSONObject aisleUnLikedProps = new JSONObject();
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
            String userName = storedVueUser.getFirstName() + " "
                    + storedVueUser.getLastName();
            aisleUnLikedProps.put("Image Id",
                    mCurrentAisle.getImageList().get(position).mId);
            aisleUnLikedProps.put("Aisle Id", mCurrentAisle.getAisleId());
            aisleUnLikedProps.put("Image Position", mCurrentDispImageIndex);
            aisleUnLikedProps.put("Is Aisle Owner", isOwner);
            aisleUnLikedProps.put("Owner Name",
                    mCurrentAisle.getAisleContext().mFirstName);
            aisleUnLikedProps.put("Like Count", mCurrentAisle.getImageList()
                    .get(mCurrentDispImageIndex).mLikesCount);
            aisleUnLikedProps.put("Category",
                    mCurrentAisle.getAisleContext().mCategory);
            aisleUnLikedProps.put("Looking For",
                    mCurrentAisle.getAisleContext().mLookingForItem);
            aisleUnLikedProps.put("Occasion",
                    mCurrentAisle.getAisleContext().mOccasion);
            aisleUnLikedProps.put("Unlike From", "Detail View Screen");
            aisleUnLikedProps.put("Image Unliked By", userName);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        mixpanel.track("Image Unliked", aisleUnLikedProps);
        if (mCurrentAisle.getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
            // false
            mCurrentAisle.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
            if (mCurrentAisle.getImageList().get(position).mLikesCount > 0) {
                mCurrentAisle.getImageList().get(position).mLikesCount = mCurrentAisle
                        .getImageList().get(position).mLikesCount - 1;
                mCurrentAisle.mTotalLikesCount = mCurrentAisle.mTotalLikesCount - 1;
            }
            sendDataToDb(position, CHANGE_LIKES, false);
        } else if (mCurrentAisle.getImageList().get(position).mLikeDislikeStatus == VueConstants.IMG_NONE_STATUS) {
            mCurrentAisle.getImageList().get(position).mLikeDislikeStatus = VueConstants.IMG_NONE_STATUS;
        }
        findMostLikesImage();
        if (position == mCurrentDispImageIndex) {
            mLikes = mCurrentAisle.getImageList().get(position).mLikesCount;
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
                .getAllBookmarkAisleIdsList();
        
        for (AisleBookmark b : aisleBookmarkList) {
            if (aisleId.equals(Long.toString(b.getAisleId().longValue()))) {
                aisleBookmark.setId(b.getId());
                break;
            }
        }
        if (aisleBookmark.getId() != null && aisleBookmark.getId() == 0) {
            aisleBookmark.setId(null);
        }
        try {
            AisleManager.getAisleManager().aisleBookmarkUpdate(aisleBookmark);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    private void setImageRating() {
        ArrayList<AisleImageDetails> aisleImgDetais = mCurrentAisle
                .getImageList();
        // get the rated images by this user from the db.
        // this table contains all the rated image list by this user.
        // if any image id exist in the table means it is a rated image by this
        // user.
        ArrayList<ImageRating> imgRatingList = DataBaseManager.getInstance(
                mContext).getRatedImagesList(mCurrentAisle.getAisleId());
        
        for (AisleImageDetails imgDetail : aisleImgDetais) {
            
            for (ImageRating imgRating : imgRatingList) {
                if (imgRating.mImageId == Long.parseLong(imgDetail.mId)
                        && imgRating.mLiked) {
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
        mCurrentAisle.getImageList().get(mCurrentDispImageIndex).mCommentsList
                .add(comments);
        if (mCommentsMapList == null) {
            getCommentList();
        }
        mCommentsMapList.put(mCurrentDispImageIndex, mCurrentAisle
                .getImageList().get(mCurrentDispImageIndex).mCommentsList);
        
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
        imgComment.setOwnerImageId(Long.parseLong(mCurrentAisle.getImageList()
                .get(mCurrentDispImageIndex).mId));
        Utils.saveUserPoints(VueConstants.USER_COMMENTS_POINTS, 2, mContext);
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
        for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
            mCommentsMapList.put(i,
                    mCurrentAisle.getImageList().get(i).mCommentsList);
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
        for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
            mCurrentAisle.getImageList().get(i).mHasMostLikes = false;
            mCurrentAisle.getImageList().get(i).mSameMostLikes = false;
            if (mLikes < mCurrentAisle.getImageList().get(i).mLikesCount) {
                mLikes = mCurrentAisle.getImageList().get(i).mLikesCount;
                hasLikes = true;
                mostLikePosition = i;
            }
        }
        if (hasLikes) {
            mCurrentAisle.getImageList().get(mostLikePosition).mHasMostLikes = true;
        }
        if (mLikes == 0) {
            return;
        }
        for (int i = 0; i < mCurrentAisle.getImageList().size(); i++) {
            if (mostLikePosition == i) {
                continue;
            }
            if (mLikes == mCurrentAisle.getImageList().get(i).mLikesCount) {
                mCurrentAisle.getImageList().get(i).mSameMostLikes = true;
                mCurrentAisle.getImageList().get(i).mHasMostLikes = true;
                mCurrentAisle.getImageList().get(mostLikePosition).mSameMostLikes = true;
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
        public void destroyItem(View view, int position, Object object) {
            
            RelativeLayout detailsBrowser = (RelativeLayout) object;
            ScaleImageView browserImage = (ScaleImageView) detailsBrowser
                    .findViewById(R.id.details_view_image_identifier);
            ScaledImageViewFactory imageViewFactory = ScaledImageViewFactory
                    .getInstance(mContext);
            
            browserImage = null;
            // imageViewFactory.returnUsedImageView(browserImage);
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
            return mCurrentAisle.getImageList().size();
        }
        
        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) view.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            
            RelativeLayout detailsViewLayout = (RelativeLayout) mInflater
                    .inflate(R.layout.detailsbrowser, null);
            NetworkImageView browserImage = (NetworkImageView) detailsViewLayout
                    .findViewById(R.id.details_view_image_identifier);
            // NetworkImageView existingView =
            // (NetworkImageView)detailsViewLayout.findViewById(R.id.details_view_image_identifier);
            // if(null != existingView)
            // detailsViewLayout.removeView(existingView);
            // detailsViewLayout.addView(browserImage);
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
            ImageView full_bg_image = (ImageView) detailsViewLayout
                    .findViewById(R.id.full_bg_image);
            TextView transperentTextView = (TextView) detailsViewLayout
                    .findViewById(R.id.transperenText);
            RelativeLayout suggestionLay = (RelativeLayout) detailsViewLayout
                    .findViewById(R.id.suggestionLay);
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
            AisleImageDetails imageDetails = mCurrentAisle.getImageList().get(
                    position);
            if (mCurrentDispImageIndex == position) {
                boolean editLayVisibility = false;
                boolean starLayVisibility = false;
                boolean isMostLikedImage = false;
                if (mCurrentAisle.getImageList().get(position).mOwnerUserId != null
                        && mCurrentAisle.getAisleContext().mUserId != null) {
                    if (Long.parseLong(mCurrentAisle.getImageList().get(
                            position).mOwnerUserId) == mUserId
                            || Long.parseLong(mCurrentAisle.getAisleContext().mUserId) == mUserId) {
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
            if (isFromPendingScreen) {
                suggestionLay.getBackground().setAlpha(50);
                suggestionLay.setVisibility(View.VISIBLE);
                suggestionLay.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        mswipeListner.onImageAddEvent();
                        
                    }
                });
                if (isAisleOwner) {
                    transperentTextView.setText(mContext.getResources()
                            .getString(R.string.suggest_something_owner));
                } else {
                    transperentTextView.setText(mContext.getResources()
                            .getString(R.string.suggest_something_guest));
                }
            } else {
                suggestionLay.setVisibility(View.GONE);
                
            }
            if (mCurrentAisle.getImageList().get(position).mIsFromLocalSystem) {
                String url = mCurrentAisle.getImageList().get(position).mImageUrl;
                Bitmap bmp = BitmapLoaderUtils
                        .getInstance()
                        .getBitmap(
                                url,
                                url,
                                false,
                                mBestHeight,
                                VueApplication.getInstance()
                                        .getVueDetailsCardWidth(),
                                Utils.DETAILS_SCREEN,
                                mCurrentAisle.getImageList().get(position).mIsFromLocalSystem);
                if (bmp != null) {
                    full_bg_image.setImageBitmap(bmp);
                }
            } else {
                loadBitmap(browserImage,
                        mCurrentAisle.getImageList().get(position).mImageUrl,
                        VueApplication.getInstance().getVueDetailsCardWidth(),
                        mBestHeight);
            }
            ((ViewPager) view).addView(detailsViewLayout);
            return detailsViewLayout;
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
            if (mCurrentAisle.getImageList().size() == position) {
                position = mCurrentAisle.getImageList().size() - 1;
            }
            boolean editLayVisibility = false;
            boolean starLayVisibility = false;
            boolean isMostLikedImage = false;
            if (mCurrentAisle.getImageList().get(position).mOwnerUserId != null
                    && mCurrentAisle.getAisleContext().mUserId != null) {
                if (Long.parseLong(mCurrentAisle.getImageList().get(position).mOwnerUserId) == mUserId
                        || Long.parseLong(mCurrentAisle.getAisleContext().mUserId) == mUserId) {
                    editLayVisibility = true;
                } else {
                    editLayVisibility = false;
                }
            }
            if (mCurrentAisle.getImageList().get(position).mHasMostLikes) {
                starLayVisibility = true;
                
                if (mCurrentAisle.getImageList().get(position).mSameMostLikes) {
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
        if (!isFromPendingScreen
                && (!url.equalsIgnoreCase(VueConstants.NO_IMAGE_URL))) {
            ((NetworkImageView) imageView).setImageUrl(url, VueApplication
                    .getInstance().getImageCacheLoader(), width, height,
                    NetworkImageView.BitmapProfile.ProfileDetailsView);
        }
    }
    
    private void findAisleStage() {
        int likesCount = 0, commentsCount = 0, totalCount;
        ArrayList<AisleImageDetails> imageDetailsList = mCurrentAisle
                .getImageList();
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
}
