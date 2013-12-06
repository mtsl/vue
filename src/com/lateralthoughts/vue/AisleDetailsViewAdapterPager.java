/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.VueAisleDetailsViewFragment.ShareViaVueListner;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Comment;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.parser.ImageComments;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class AisleDetailsViewAdapterPager extends BaseAdapter {
	private Context mContext;
	public static final String TAG = "AisleDetailsViewAdapter";
	public static final int IMG_LIKE_STATUS = 1;
	public static final int IMG_NONE_STATUS = 0;
	private static final String CHANGE_BOOKMARK = "BOOKMARK";
	public static final String CHANGE_COMMENT = "COMMENT";
	private static final String CHANGE_LIKES = "LIKES";
	private static final boolean DEBUG = false;
	private AisleDetailsViewListLoader mViewLoader;
	private AisleDetailSwipeListener mswipeListner;
	private boolean closeKeyboard = false;
	VueUser storedVueUser = null;
	int mFirstx = 0, mLastx = 0, mFirsty = 0, mLasty = 0;
	public static final int SWIPE_MIN_DISTANCE = 30;
	LayoutInflater mInflater;
	// we need to customize the layout depending on screen height & width which
	// we will get on the fly
	private int mListCount;
	public int mLikes;
	private int mBookmarksCount;
	public int mCurrentDispImageIndex;
	private boolean mIsLikeImageClicked = false;
	private boolean mIsBookImageClciked = false;
	int mShowFixedRowCount = 3;
	int mInitialCommentsToShowSize = 2;
	public String mVueusername;
	ShareDialog mShare;
	public int mCurrentAislePosition;
	public ArrayList<String> mImageDetailsArr = null;
	@SuppressLint("UseSparseArrays")
	Map<Integer, ArrayList<ImageComments>> mCommentsMapList = new HashMap<Integer, ArrayList<ImageComments>>();
	//ArrayList<String> mShowingList;
	//ArrayList<String> mShowingListCommenterUrl;
	ArrayList<Comment> mShowingCommentList = new ArrayList<Comment>();
	private int mBestHeight;
	private int mTopBottomMargin = 24;
	ViewHolder mViewHolder;
	private boolean mSetPosition;
	private static final int mWaitTime = 1000;
	VueTrendingAislesDataModel mVueTrendingAislesDataModel;
	public ArrayList<String> mCustomUrls = new ArrayList<String>();
	private LoginWarningMessage mLoginWarningMessage = null;
	private long mUserId;
	private ImageLoader mImageLoader;
	private ShareViaVueListner mShareViaVueListner;
	private BitmapLoaderUtils mBitmapLoaderUtils;
	int mPrevPosition/* ,mCurrentPosition */;
	PageListener pageListener;
	DetailImageClickListener detailsImageClickListenr;
	Animation myFadeInAnimation;
	private boolean mSetPager = true;

	@SuppressWarnings("unchecked")
	public AisleDetailsViewAdapterPager(Context c,
			AisleDetailSwipeListener swipeListner, int listCount,
			ArrayList<AisleWindowContent> content,
			ShareViaVueListner shareViaVueListner) {
		/* super(c, content); */
		mShareViaVueListner = shareViaVueListner;
		mVueTrendingAislesDataModel = VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance());
		mCurrentDispImageIndex = VueApplication.getInstance()
				.getmAisleImgCurrentPos();
		mSetPosition = true;
		mContext = c;
		myFadeInAnimation = AnimationUtils.loadAnimation(
				VueApplication.getInstance(), R.anim.fadein);
		mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
		mImageLoader = new ImageLoader(VueApplication.getInstance()
				.getRequestQueue(), BitmapLruCache.getInstance(mContext));
		mTopBottomMargin = VueApplication.getInstance().getPixel(
				mTopBottomMargin);
		mViewLoader = new AisleDetailsViewListLoader(mContext);
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
		if (DEBUG)
			Log.e(TAG, "About to initiate request for trending aisles");
		Log.i("bookmarked aisle", "bookmarked persist issue  aisleid: "
				+ VueApplication.getInstance().getClickedWindowID());
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
		Log.i("bestHeight",
				"bestHeight in details adapter: "
						+ getItem(mCurrentAislePosition)
								.getBestHeightForWindow());
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
				Log.i("clone",
						"mCustomImageUrl url: "
								+ getItem(mCurrentAislePosition).getImageList()
										.get(i).mCustomImageUrl);
				if (getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight > mBestHeight) {
					mBestHeight = getItem(mCurrentAislePosition).getImageList()
							.get(i).mAvailableHeight;
				}

				mCommentsMapList.put(i, getItem(mCurrentAislePosition)
						.getImageList().get(i).mCommentsList);
			}

			mImageDetailsArr = (ArrayList<String>) mCustomUrls.clone();
			Log.i("clone", "clone: " + mImageDetailsArr);
			if (mImageDetailsArr != null) {
				for (int i = 0; i < mImageDetailsArr.size(); i++) {
					Log.i("clone", "clone1: " + mImageDetailsArr.get(i));
				}
			}

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
			Log.i("bookmarked aisle", "bookmarked count in window2: "
					+ mBookmarksCount);
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
		Log.i("imageCurrenPosition", "imageCurrenPosition adapter: "
				+ mCurrentDispImageIndex);
		// mswipeListner.setFindAtText(getItem(mCurrentAislePosition).getImageList().get(0).mImageUrl);
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
		// AisleContentBrowser aisleContentBrowser;
		LinearLayout aisleContentBrowser;
		TextView aisleDescription;
		TextView aisleOwnersName;
		TextView aisleContext, commentCount, likeCount,textcount;
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
		ImageView commentSend/* starIcon */;
		LinearLayout /* editImage, */starImage;
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
			/*
			 * mViewHolder.editImage = (LinearLayout) convertView
			 * .findViewById(R.id.editImage);
			 */
			/*
			 * mViewHolder.starIcon = (ImageView) convertView
			 * .findViewById(R.id.staricon);
			 */
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
			mViewHolder.textcount = (TextView) convertView.findViewById(R.id.textcount);
			int bestHeightTempHeight = VueApplication.getInstance().getPixel(
					100);
			mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		Log.i("entercommentclick", "entercommentclick 2 mListCount: "+mListCount);
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
		Log.i("imagedispissue", "imagedispissue0_2");
		mViewHolder.commentCount.setText((mShowingCommentList.size() + " Comments"));
		mViewHolder.bookMarkCount.setText("" + mBookmarksCount);
		mViewHolder.likeCount.setText("" + mLikes);
		mViewHolder.imgContentlay.setVisibility(View.VISIBLE);
		mViewHolder.commentContentlay.setVisibility(View.VISIBLE);
		mViewHolder.vueCommentheader.setVisibility(View.VISIBLE);
		mViewHolder.addCommentlay.setVisibility(View.VISIBLE);

		// mViewHolder.edtCommentLay.setVisibility(View.VISIBLE);
		if (position == 0) {
			boolean hasToShow = false;
			Log.i("imagedispissue", "imagedispissue-1");
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
			// mViewHolder.mWindowContent = mWindowContentTemp;
			try {
				/*
				 * if (getItem(mCurrentAislePosition).getImageList().get(
				 * mCurrentDispImageIndex).mOwnerUserId != null &&
				 * getItem(mCurrentAislePosition).getAisleContext().mUserId !=
				 * null) { if (Long.parseLong(getItem(mCurrentAislePosition)
				 * .getImageList().get(mCurrentDispImageIndex).mOwnerUserId) ==
				 * mUserId || Long.parseLong(getItem(mCurrentAislePosition)
				 * .getAisleContext().mUserId) == mUserId) { hasToShow = true;
				 * mswipeListner.hasToShowEditIcon(hasToShow);
				 * 
				 * } else { hasToShow = false;
				 * mswipeListner.hasToShowEditIcon(hasToShow); } }
				 */
				if (getItem(mCurrentAislePosition).getImageList().get(
						mCurrentDispImageIndex).mHasMostLikes) {
					Log.i("hasMostLikes",
							"hasMostLikes: "
									+ getItem(mCurrentAislePosition)
											.getImageList().get(
													mCurrentDispImageIndex).mHasMostLikes);
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
						// mViewHolder.starIcon.setImageResource(R.drawable.vue_star_light);

					} else {
						// mViewHolder.starIcon.setImageResource(R.drawable.vue_star_theme);
					}
					// mViewHolder.starImage.setVisibility(View.VISIBLE);
				} else {
					// mViewHolder.starImage.setVisibility(View.GONE);
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
				int scrollIndex = VueApplication.getInstance()
						.getmAisleImgCurrentPos();
				detailsImageClickListenr = new DetailImageClickListener();
				// mWindowContentTemp = mViewHolder.mWindowContent;
				mViewHolder.tag = TAG;
				/*
				 * mViewLoader.getAisleContentIntoView(mViewHolder, scrollIndex,
				 * position, new DetailImageClickListener(),
				 * getItem(mCurrentAislePosition),
				 * mSetPosition,mViewHolder.editImage,mViewHolder.starImage);
				 */
				mSetPosition = false;

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
			Log.i("entercommentclick", "entercommentclick 3");
			mViewHolder.separator.setVisibility(View.GONE);
			mViewHolder.imgContentlay.setVisibility(View.GONE);
			mViewHolder.vueCommentheader.setVisibility(View.GONE);
			mViewHolder.commentContentlay.setVisibility(View.GONE);
			// mViewHolder.edtCommentLay.setVisibility(View.GONE);
			if (mViewHolder.enterCommentrellay.getVisibility() == View.VISIBLE) {
				mViewHolder.commentSend.setVisibility(View.GONE);
			}
			if (closeKeyboard) {
				closeKeyboard = false;
				mViewHolder.edtCommentLay.setVisibility(View.GONE);
				mViewHolder.enterCommentrellay.setVisibility(View.VISIBLE);
			}
			mViewHolder.enterCommentrellay
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Log.i("entercommentclick", "entercommentclick 1");
							mViewHolder.edtCommentLay
									.setVisibility(View.VISIBLE);
							mViewHolder.enterCommentrellay
									.setVisibility(View.GONE);
							mswipeListner.onAddCommentClick(
									mViewHolder.enterCommentrellay,
									mViewHolder.edtComment,
									mViewHolder.commentSend,
									mViewHolder.edtCommentLay,mListCount - 1,mViewHolder.textcount);
						}
					});
		} else {
			// first two views are image and comment layout. so use position - 2
			// to display all the comments from start
			if (position - mInitialCommentsToShowSize < mShowingCommentList.size()) {
				Comment comment = mShowingCommentList.get(position
						- mInitialCommentsToShowSize);
				mViewHolder.userComment.setText(comment.mComment);
					mViewHolder.userPic
						.setImageUrl(
								comment.mComenterUrl,
								mImageLoader);
				// TODO: uncomment below code when user pic is available in
				// commenter object.
				// mViewHolder.userPic.setImageUrl(mShowingListCommenterUrl.get(position),
				// mImageLoader);
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
				// mswipeListner.onResetAdapter();
				mSetPager = false;
				if (mListCount == (mShowFixedRowCount + mInitialCommentsToShowSize)) {
					mListCount = mShowingCommentList.size() + mShowFixedRowCount;
				} else {
					if (mShowingCommentList.size() > 2) {
						mListCount = mShowFixedRowCount
								+ mInitialCommentsToShowSize;
					} else {
						mListCount = mShowingCommentList.size() + mShowFixedRowCount;
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
					Log.e("AisleManager",
							"bookmarkfeaturetest: count BOOKMARK RESPONSE: mViewHolder.bookmarklay if called ");
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
				// mSetPager = false;
				toggleRatingImage();
				// setmSetPagerToTrue();
			}
		});
		return convertView;
	}
 
	public void notifyAdapter() {
		Log.i("editiconshowing",
				"editiconshowing in adapter  notify adapters: ");
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
				Log.i("userId",
						"userId not matched imageobj onswipe: "
								+ Long.parseLong(getItem(mCurrentAislePosition)
										.getImageList().get(
												mCurrentDispImageIndex).mOwnerUserId));
				mLikes = getItem(mCurrentAislePosition).getImageList().get(
						position).mLikesCount;
				ArrayList<ImageComments> imgComments = getItem(
						mCurrentAislePosition).getImageList().get(
						mCurrentDispImageIndex).mCommentsList;
				prepareCommentList(imgComments);
			/*	if (mShowingList == null) {
					mShowingList = new ArrayList<String>();
					mShowingListCommenterUrl = new ArrayList<String>();
					mShowingCommentList = new ArrayList<Comment>();
				} else if (mShowingList.size() > 0) {
					mShowingList.clear();
					mShowingListCommenterUrl.clear();
					mShowingCommentList.clear();
				}
				for (ImageComments comment : imgComments) {
					mShowingList.add(comment.mComment);
					mShowingListCommenterUrl.add(comment.mCommenterUrl);
					Comment showComment = new Comment();
					showComment.mComenterUrl = comment.mCommenterUrl;
					showComment.mComment = comment.mComment;
					mShowingCommentList.add(showComment);
				}*/

				if (mShowingCommentList.size() < mShowFixedRowCount) {

					mListCount = mShowFixedRowCount + mShowingCommentList.size();
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
			/* if (mCurrentDispImageIndex == 0) { */
			int resizeWidth = getItem(mCurrentAislePosition).getImageList()
					.get(mCurrentDispImageIndex).mTempResizeBitmapwidth;
			int resizeHeight = getItem(mCurrentAislePosition).getImageList()
					.get(mCurrentDispImageIndex).mTempResizedBitmapHeight;
			int cardWidth = VueApplication.getInstance()
					.getVueDetailsCardWidth();
			int cardHeight = VueApplication.getInstance()
					.getVueDetailsCardHeight();
			String writeToSdCard = "***************DETAILS ADAPTER***********************\n";
			writeToSdCard = writeToSdCard + " Aisle Id: "
					+ getItem(mCurrentAislePosition).getAisleId() + "\n";

			for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
					.size(); i++) {
				writeToSdCard = writeToSdCard
						+ "\n ImageUrl: "
						+ getItem(mCurrentAislePosition).getImageList().get(i).mImageUrl;
				writeToSdCard = writeToSdCard
						+ "\n"
						+ "image Width: "
						+ getItem(mCurrentAislePosition).getImageList().get(i).mAvailableWidth
						+ "image Height: "
						+ getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight;
			}

			writeToSdCard = writeToSdCard + " ReSizeImageWidth: " + resizeWidth
					+ " ReSizedImageHeight: " + resizeHeight + "\n";
			writeToSdCard = writeToSdCard + " CardWidth: " + cardWidth
					+ " CardHeight: " + cardHeight + "\n";
			writeToSdCard = writeToSdCard
					+ " Final card Height will be: "
					+ getItem(mCurrentAislePosition)
							.getBestLargetHeightForWindow() + "\n";
			writeToSdCard = writeToSdCard
					+ "\n***************DETAILS ADAPTER***********************";
			writeToSdcard(writeToSdCard);
			/*
			 * } else { Toast.makeText(mContext,
			 * "Works For Only Starting Image", 1000) .show(); ;
			 * 
			 * }
			 */
		}

		@Override
		public void onSetBrowserArea(String area) {
			// TODO Auto-generated method stub

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

	public void setAisleBrowserObjectsNull() { }

	public void addAisleToContentWindow() {
		if (mViewHolder != null) {
			// mswipeListner.onResetAdapter();
			setAisleBrowserObjectsNull();
			mswipeListner.onResetAdapter();
			Log.i("adaptersettings", "adaptersettings:adapter not null");
			/*
			 * Log.i("adaptersettings", "adaptersettings: if");
			 * mViewHolder.uniqueContentId =
			 * AisleWindowContent.EMPTY_AISLE_CONTENT_ID; notifyAdapter();
			 */
		} else {
			Log.i("adaptersettings", "adaptersettings:adapter   null");
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

	int likeCount = 0;
	ImageRating imgRating;

	public void sendDataToDb(int imgPosition, String reqType,
			boolean likeOrDislike) {
		String aisleId = null;
		String imageId = null;
		AisleImageDetails itemDetails;

		int likeStatus = 0;

		Log.e("ImageRating Resopnse", "SURU ImageRating sendDataToDb() called");
		if (getItem(mCurrentAislePosition).getImageList() != null
				&& getItem(mCurrentAislePosition).getImageList().size() != 0) {
			aisleId = getItem(mCurrentAislePosition).getAisleId();
			itemDetails = getItem(mCurrentAislePosition).getImageList().get(
					imgPosition);
			imageId = itemDetails.mId;
			if (reqType.equals(CHANGE_BOOKMARK)) {
				// aisleId,imageId,bookMarksCount,bookmarkIndicator
				int bookMarksCount = getItem(mCurrentAislePosition)
						.getmAisleBookmarksCount();
				boolean bookmarkIndicator = getItem(mCurrentAislePosition)
						.getWindowBookmarkIndicator();
			} else if (reqType.equals(CHANGE_COMMENT)) {
				// aisleId,imageId,comment
				if (itemDetails.mCommentsList == null) {
					getItem(mCurrentAislePosition).getImageList().get(0).mCommentsList = new ArrayList<ImageComments>();
				}
				String commentAdded = itemDetails.mCommentsList.get(0).mComment;
			} else if (reqType.equals(CHANGE_LIKES)) {
				// aisleId,imageId,likesCount,likeStatus
				likeCount = itemDetails.mLikesCount;
				likeStatus = itemDetails.mLikeDislikeStatus;
				Log.i("likecountissue", "likecountissue: likeCount1: "
						+ likeCount);
				ArrayList<ImageRating> imgRatingList = DataBaseManager
						.getInstance(mContext).getRatedImagesList(aisleId);
				imgRating = new ImageRating();
				imgRating.setAisleId(Long.parseLong(aisleId));
				imgRating.setImageId(Long.parseLong(imageId));
				imgRating.setLiked(likeOrDislike);
				for (ImageRating imgRat : imgRatingList) {
					if (imgRating.getImageId().longValue() == imgRat
							.getImageId().longValue()) {
						imgRating.setId(/* l2 */imgRat.getId().longValue());
						break;
					}
				}
				try {
					AisleManager.getAisleManager().updateRating(imgRating,
							likeCount);
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
		if (storedVueUser == null) {
			try {
				storedVueUser = Utils.readUserObjectFromFile(mContext,
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
		if (storedVueUser != null) {
			articleParams.put("Unique_User_Like", "" + storedVueUser.getId());
		} else {
			articleParams.put("Unique_User_Like", "anonymous");
		}
		FlurryAgent.logEvent("LIKES_DETAILSVIEW", articleParams);
		if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_LIKE_STATUS) {
			getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_LIKE_STATUS;

			Map<String, String> articleParams1 = new HashMap<String, String>();
			articleParams1.put("Unique_Aisle_Likes",
					"" + getItem(mCurrentAislePosition).getAisleId());
			if (storedVueUser != null) {
				articleParams1.put("Unique_User_Like",
						"" + storedVueUser.getId());
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
		closeKeyboard = true;
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
				Log.i("bookmarkissue",
						"bookmarkissue handleBookmark matched Id " + b.getId());
				break;
			}
		}
		VueUser storedVueUser = null;
		try {
			Log.i("bookmarkissue", "bookmarkissue handleBookmark");
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

	private int getBestHeight(int largeHeight) {
		int screenHeight = VueApplication.getInstance().getScreenHeight();
		int screenWidth = VueApplication.getInstance().getScreenWidth();
		if (largeHeight > screenHeight) {
			largeHeight = screenHeight;
		}

		return largeHeight;

	}

	private void setImageRating() {
		ArrayList<AisleImageDetails> aisleImgDetais = getItem(
				mCurrentAislePosition).getImageList();
		ArrayList<ImageRating> imgRatingList = DataBaseManager.getInstance(
				mContext).getRatedImagesList(
				getItem(mCurrentAislePosition).getAisleId());
		Log.i("imageLikestatus",
				"imageLikestatus size: " + imgRatingList.size());
		for (AisleImageDetails imgDetail : aisleImgDetais) {
			Log.i("imageLikestatus", "imageLikestatus#: " + imgDetail.mId);
			for (ImageRating imgRating : imgRatingList) {
				Log.i("imageLikestatus",
						"imageLikestatus*: " + imgRating.getImageId());
				if (imgRating.getImageId() == Long.parseLong(imgDetail.mId)) {
					imgDetail.mLikeDislikeStatus = IMG_LIKE_STATUS;
				}
			}
		}
	}

	public void updateListCount(String newComment) {
		mListCount = mListCount + 1;
		// mShowingList.add(0,newComment);
		Log.i("sizeoflist", "sizeoflist: " + mShowingCommentList.size());
	}

	@SuppressWarnings("unchecked")
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
			commenterUrl =  storedVueUser.getUserImageURL()  ;
		}
         if(commenterUrl != null && commenterUrl.length() < 6){
        	 commenterUrl = "https://lh5.googleusercontent.com/-u5KwAmhVoUI/AAAAAAAAAAI/AAAAAAAAADg/5zfJJy26SNE/photo.jpg?sz=50";
         } else if(commenterUrl == null){
        	 commenterUrl = "https://lh5.googleusercontent.com/-u5KwAmhVoUI/AAAAAAAAAAI/AAAAAAAAADg/5zfJJy26SNE/photo.jpg?sz=50";
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
		
		/*imgComment.setCommenterFirstName(getItem(mCurrentAislePosition)
				.getAisleContext().mFirstName);
		imgComment.setCommenterLastName(getItem(mCurrentAislePosition)
				.getAisleContext().mLastName);*/
		imgComment.setComment(commentString);
		imgComment. setLastModifiedTimestamp(System.currentTimeMillis());
		imgComment.setOwnerUserId(Long.parseLong(VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance()).getNetworkHandler()
				.getUserId()));
		imgComment.setOwnerImageId(Long
				.parseLong(getItem(mCurrentAislePosition).getImageList().get(
						mCurrentDispImageIndex).mId));
/** Save the image comment and verify the save *//*
	 new Thread(new Runnable() {
		
		@Override
		public void run() {
			try {
				testCreateImageComment(imgComment);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}).start();*/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ImageComment createdComment = VueTrendingAislesDataModel
							.getInstance(VueApplication.getInstance())
							.getNetworkHandler().createImageComment(imgComment);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

	@SuppressWarnings("unchecked")
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
	/*	if (mShowingList == null) {
			mShowingList = new ArrayList<String>();
			mShowingListCommenterUrl = new ArrayList<String>();
		} else if (mShowingList.size() > 0) {
			mShowingList.clear();
			mShowingListCommenterUrl.clear();
		}
		for (ImageComments comment : imgComments) {
			mShowingList.add(comment.mComment);
			mShowingListCommenterUrl.add(comment.mCommenterUrl);
		}*/
	}
  private void prepareCommentList(ArrayList<ImageComments> imgComments){
		if (mShowingCommentList == null) {
			mShowingCommentList = new ArrayList<Comment>();
		} else if (mShowingCommentList.size() > 0) {
			mShowingCommentList.clear();
		}
		for (ImageComments comment : imgComments) {
			Comment showComment = new Comment();
			showComment.mComment = comment.mComment;
			showComment.mComenterUrl = comment.mCommenterUrl;
			
			if(showComment.mComenterUrl == null || showComment.mComenterUrl.length()< 8){
					//place holder for the profile picture.
				showComment.mComenterUrl = "https://lh5.googleusercontent.com/-u5KwAmhVoUI/AAAAAAAAAAI/AAAAAAAAADg/5zfJJy26SNE/photo.jpg?sz=50";
			}
			
			mShowingCommentList.add(showComment);
		}
  }
	private void writeToSdcard(String message) {

		String path = Environment.getExternalStorageDirectory().toString();
		File dir = new File(path + "/vueImageDetails/");
		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		File file = new File(dir, "/"
				+ Calendar.getInstance().get(Calendar.DATE) + ".txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			Log.i("pathsaving", "pathsaving in sdcard2 error");
			e.printStackTrace();
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));
			out.write("\n" + message + "\n");
			out.flush();
			out.close();
			Log.i("pathsaving", "pathsaving in sdcard2 success");
		} catch (IOException e) {
			Log.i("pathsaving", "pathsaving in sdcard3 error");
			e.printStackTrace();
		}
	}

	/**
	 * show star to most likes on the image.
	 */
	private void findMostLikesImage() {
		int mostLikePosition = 0, mLikes = 0;
		boolean hasLikes = false, mSameMostLikes;
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
		private GestureDetector gesturedetector = null;

		/**
		 * 
		 * @param context
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

		@SuppressWarnings("deprecation")
		@Override
		public Object instantiateItem(View view, int position) {
			if (mInflater == null) {
				mInflater = (LayoutInflater) view.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			Log.i("pager", "pager instantiateItem");
			View myView = mInflater.inflate(R.layout.detailsbrowser, null);
			ImageView browserImage = (ImageView) myView
					.findViewById(R.id.browserimage);
			gesturedetector = new GestureDetector(this);

			/*
			 * browserImage.setOnTouchListener(new OnTouchListener() {
			 * 
			 * @Override public boolean onTouch(View v, MotionEvent event) {
			 * 
			 * if(event.getAction() == MotionEvent.ACTION_DOWN){ mFirstx = 0;
			 * mFirsty = 0; } else if(event.getAction() ==
			 * MotionEvent.ACTION_UP){ mLastx = (int) event.getX(); mLasty =
			 * (int) event.getY();
			 * 
			 * } else if(event.getAction() == MotionEvent.ACTION_MOVE) { mLastx
			 * = (int) event.getX(); mLasty = (int) event.getY(); int xDiff =
			 * mLastx - mFirstx; if(xDiff < 0){ xDiff = xDiff * -1; } int yDiff
			 * = mLasty - mFirsty; if(yDiff < 0){ yDiff = yDiff * -1; }
			 * Log.i("diff", "diff x: "+xDiff); Log.i("diff", "diff y: "+yDiff);
			 * if(xDiff < yDiff) { mswipeListner.onAllowListResponse(); } else {
			 * mswipeListner.onDissAllowListResponse(); return false; } } return
			 * false; } });
			 */
			browserImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// mSetPager = false;
					detailsImageClickListenr.onImageClicked();
					// setmSetPagerToTrue();

				}
			});
			browserImage.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// mSetPager = false;
					detailsImageClickListenr.onImageLongPress();
					// setmSetPagerToTrue();
					return false;
				}
			});
			// browserImage.setImageResource(R.drawable.ic_launcher);
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
					// TODO Auto-generated method stub
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
						Log.i("editlay", "editlay visible1");

					} else {
						editLay.setVisibility(View.GONE);
						Log.i("editlay", "editlay gone1");
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
				Log.i("editlay", "editlay from bg");
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
			Log.i("ondown", "ondown calling");

			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public void loadBitmap(AisleImageDetails itemDetails, ImageView imageView,
			int bestHeight, int scrollIndex, ProgressBar progressBar,
			int currentPosition, LinearLayout editLay, LinearLayout starLay,
			ImageView starImage) {
		String loc = itemDetails.mImageUrl;
		String serverImageUrl = itemDetails.mImageUrl;
		/*
		 * ((ScaleImageView) imageView).setImageUrl(serverImageUrl, new
		 * ImageLoader(VueApplication.getInstance().getRequestQueue(),
		 * VueApplication.getInstance().getBitmapCache()));
		 */
		Log.i("pager", "pager loadBitmap");
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
		// private final WeakReference<AisleContentBrowser>viewFlipperReference;
		private String url = null;
		private int mBestHeight;
		int mAvailabeWidth, mAvailableHeight;
		AisleImageDetails mItemDetails;
		int mScrollIndex;
		ProgressBar mProgressBar;
		LinearLayout mEditLay, mStarLay;
		int mImageListCurrentPosition;
		ImageView mStarImage;

		public BitmapWorkerTask(AisleImageDetails itemDetails,
				ImageView imageView, int bestHeight, int scrollIndex,
				ProgressBar progressBar, int currentPosition,
				LinearLayout editLay, LinearLayout starLay, ImageView starImage) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			mBestHeight = bestHeight;
			mAvailabeWidth = itemDetails.mAvailableWidth;
			mAvailableHeight = itemDetails.mAvailableHeight;
			mItemDetails = itemDetails;
			mScrollIndex = scrollIndex;
			mProgressBar = progressBar;
			mImageListCurrentPosition = currentPosition;
			mEditLay = editLay;
			mStarLay = starLay;
			mStarImage = starImage;
		}

		@Override
		protected void onPreExecute() {
			if (mProgressBar != null)
				mProgressBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			url = params[0];
			Bitmap bmp = null;
			Log.i("added url", "added url  listloader " + url);
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
			Log.i("pager", "pager doInBackground");
			return bmp;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			Log.i("pager", "pager onPostExecute: " + bitmap);
			final ImageView imageView = imageViewReference.get();
			if (mProgressBar != null)
				mProgressBar.setVisibility(View.GONE);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
				if (mImageListCurrentPosition == mCurrentDispImageIndex) {
					imageView.startAnimation(myFadeInAnimation);
				}
			}

			if (getItem(mCurrentAislePosition).getImageList().get(
					mImageListCurrentPosition).mOwnerUserId != null
					&& getItem(mCurrentAislePosition).getAisleContext().mUserId != null) {
				if (Long.parseLong(getItem(mCurrentAislePosition)
						.getImageList().get(mImageListCurrentPosition).mOwnerUserId) == mUserId
						|| Long.parseLong(getItem(mCurrentAislePosition)
								.getAisleContext().mUserId) == mUserId) {
					mEditLay.setVisibility(View.VISIBLE);
					Log.i("editlay", "editlay visible2");

				} else {
					Log.i("editlay", "editlay gone2");
					mEditLay.setVisibility(View.GONE);
				}
			}

			if (mItemDetails.mHasMostLikes) {
				mStarLay.setVisibility(View.VISIBLE);
				if (mItemDetails.mSameMostLikes) {
					mStarImage.setImageResource(R.drawable.vue_star_light);
				} else {
					mStarImage.setImageResource(R.drawable.vue_star_theme);
				}
			} else {
				mStarLay.setVisibility(View.GONE);
			}

		}
	}

	private void setParams(LinearLayout vFlipper, int imgScreenHeight) {
		Log.i("imageSize", "imageSize params Height: " + imgScreenHeight);
		if (vFlipper != null) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth(),
					imgScreenHeight + VueApplication.getInstance().getPixel(12));
			params.gravity = Gravity.CENTER;
			vFlipper.setLayoutParams(params);
		}

	}

	// utility functions to keep track of all the async tasks that we
	// instantiate
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			Object task = ((ScaleImageView) imageView).getOpaqueWorkerObject();
			if (task instanceof BitmapWorkerTask) {
				BitmapWorkerTask workerTask = (BitmapWorkerTask) task;
				return workerTask;
			}
		}
		return null;
	}

	private class PageListener extends SimpleOnPageChangeListener {
		public void onPageSelected(int position) {
			Log.i(TAG, "page selected " + position);
			mCurrentDispImageIndex = position;
			if (detailsImageClickListenr != null)
				detailsImageClickListenr.onImageSwipe(position);
			mSetPager = false;
			mswipeListner.onAllowListResponse();
			setmSetPagerToTrue();
			if (mPrevPosition == position) {
				// mswipeListner.onAisleSwipe("Right",position);
			} else if (mPrevPosition < position) {
				mswipeListner.onAisleSwipe("Left", position);
				Log.i("positionissue", "positionissue: Left");
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

	private void setmSetPagerToTrue() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mSetPager = true;

			}
		}, 1000);
	}

    public static ImageComment testCreateImageComment(
                     ImageCommentRequest comment ) throws Exception{
    	
    	VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(
					VueApplication.getInstance(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userId = null;
		if (storedVueUser != null) {
			userId = Long.valueOf(storedVueUser.getId()).toString();
			storedVueUser.getUserImageURL();
		}
		 
    	
    	
            ImageComment createdImageComment = null;
            ObjectMapper mapper =
                            new ObjectMapper();

            URL url = new URL(UrlConstants.CREATE_IMAGECOMMENT_RESTURL +
                            "/" + userId);
            HttpPut httpPut = new HttpPut(url.toString());
            StringEntity entity = new StringEntity(mapper.writeValueAsString(comment));
            System.out.println("ImageComment create request: "+mapper.writeValueAsString(comment));
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            httpPut.setEntity(entity);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPut);
            if(response.getEntity()!=null &&
                            response.getStatusLine().getStatusCode() == 200) {
            	    
                    String responseMessage = EntityUtils.toString(response.getEntity());
                	Log.i("imageComment", "imageComment success response1234: "+responseMessage);
                    System.out.println("Response: "+responseMessage);
                    if (responseMessage.length() > 0)
                    {
                            createdImageComment = (new ObjectMapper()).readValue(responseMessage, ImageComment.class);
                    }
            } else {
            	Log.i("imageComment", "imageComment success response: failed "+ response.getStatusLine().getStatusCode());
            }

            return createdImageComment;
    }
}