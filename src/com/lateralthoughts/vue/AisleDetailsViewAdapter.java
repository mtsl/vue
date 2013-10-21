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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.AisleComment;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class AisleDetailsViewAdapter extends BaseAdapter {

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
	VueUser storedVueUser = null;
	// we need to customize the layout depending on screen height & width which
	// we will get on the fly
	private int mListCount;
	public int mLikes;
	private int mBookmarksCount;
	public int mCurrentDispImageIndex;
	private boolean mIsLikeImageClicked = false;
	private boolean mIsBookImageClciked = false;
	public String mVueusername;
	ShareDialog mShare;
	public int mCurrentAislePosition;
	public ArrayList<String> mImageDetailsArr = null;
	@SuppressLint("UseSparseArrays")
	Map<Integer, Object> mCommentsMapList = new HashMap<Integer, Object>();
	ArrayList<String> mShowingList;
	private int mBestHeight;
	private int mTopBottomMargin = 24;
	ViewHolder mViewHolder;
	boolean mImageRefresh = true;
	private boolean mSetPosition;
	private static final int mWaitTime = 1000;
	VueTrendingAislesDataModel mVueTrendingAislesDataModel;
	public ArrayList<String> mCustomUrls = new ArrayList<String>();
	private LoginWarningMessage mLoginWarningMessage = null;

	@SuppressWarnings("unchecked")
	public AisleDetailsViewAdapter(Context c,
			AisleDetailSwipeListener swipeListner, int listCount,
			ArrayList<AisleWindowContent> content) {
		/* super(c, content); */
		mVueTrendingAislesDataModel = VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance());

		mSetPosition = true;
		mContext = c;

		mTopBottomMargin = VueApplication.getInstance().getPixel(
				mTopBottomMargin);
		mViewLoader = new AisleDetailsViewListLoader(mContext);
		mswipeListner = swipeListner;
		mListCount = listCount;
		
		mShowingList = new ArrayList<String>();
		if (DEBUG)
			Log.e(TAG, "About to initiate request for trending aisles");

		for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
			if (getItem(i).getAisleId().equalsIgnoreCase(
					VueApplication.getInstance().getClickedWindowID())) {
				mCurrentAislePosition = i;
				break;
			}
		}
		mListCount = getItem(mCurrentAislePosition).getAisleContext().mCommentList.size()+3;
		setImageRating();
		Log.i("bestHeight",
				"bestHeight in details adapter: "
						+ getItem(mCurrentAislePosition)
								.getBestHeightForWindow());
		if (getItem(mCurrentAislePosition) != null) {
			String occasion = getItem(mCurrentAislePosition).getAisleContext().mOccasion;
			if (occasion != null) {
				occasion = occasion.substring(0, 1).toUpperCase()
						+ occasion.substring(1).toLowerCase();
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
				if (getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList == null) {
					// TODO: for temp comments display need to replace this
					getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList = new ArrayList<String>();
					if (i % 2 == 0) {
						for (int k = 0; k < 6; k++) {
							getItem(mCurrentAislePosition).getImageList()
									.get(i).mCommentsList
									.add("Love Love vue the dress! Simple and fabulous.");
						}
					} else {
						for (int k = 0; k < 10; k++) {
							getItem(mCurrentAislePosition).getImageList()
									.get(i).mCommentsList
									.add("vue vue vue  sample test comments");
						}
					}
					mCommentsMapList.put(i, getItem(mCurrentAislePosition)
							.getImageList().get(i).mCommentsList);
				}
			}
			mImageDetailsArr = (ArrayList<String>) mCustomUrls.clone();
			Log.i("clone", "clone: " + mImageDetailsArr);
			if (mImageDetailsArr != null) {
				for (int i = 0; i < mImageDetailsArr.size(); i++) {
					Log.i("clone", "clone1: " + mImageDetailsArr.get(i));
				}
			}
			//mShowingList = getItem(mCurrentAislePosition).getImageList().get(0).mCommentsList;
			mShowingList = getItem(mCurrentAislePosition).getAisleContext().mCommentList;
			mLikes = getItem(mCurrentAislePosition).getImageList().get(0).mLikesCount;
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
		AisleContentBrowser aisleContentBrowser;
		TextView aisleDescription;
		TextView aisleOwnersName;
		TextView aisleContext, commentCount, likeCount;
		TextView bookMarkCount;
		ImageView profileThumbnail;
		ImageView vueWindowBookmarkImg;
		ImageView vueWndowCommentImg;
		String uniqueContentId;
		LinearLayout aisleDescriptor;
		LinearLayout imgContentlay, commentContentlay;
		LinearLayout vueCommentheader, addCommentlay, descriptionlay;
		TextView userComment, enterComment;
		ImageView userPic, commentImg, likeImg;
		RelativeLayout exapandHolder;
		EditText edtComment;
		View separator;
		RelativeLayout enterCommentrellay;
		RelativeLayout likelay, bookmarklay;
		FrameLayout edtCommentLay;
		ImageView commentSend;
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
			convertView = layoutInflator.inflate(R.layout.vue_details_adapter,
					null);
			mViewHolder.aisleContentBrowser = (AisleContentBrowser) convertView
					.findViewById(R.id.showpieceadapter);
			Log.i("nullbug", "nullbug  mViewHolder.aisleContentBrowser "
					+ mViewHolder.aisleContentBrowser);
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
			mViewHolder.userPic = (ImageView) convertView
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
			/*
			 * FrameLayout.LayoutParams showpieceParams = new
			 * FrameLayout.LayoutParams( LayoutParams.MATCH_PARENT,
			 * LayoutParams.MATCH_PARENT );
			 * mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
			 */
			int bestHeightTempHeight = VueApplication.getInstance().getPixel(
					100);

			FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth(),
					bestHeightTempHeight);
			mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
			mViewHolder.aisleContentBrowser
					.setAisleDetailSwipeListener(mswipeListner);

			mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		/*
		 * if (mViewHolder.aisleContentBrowser != null)
		 * mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
		 */

		if (getItem(mCurrentAislePosition).getWindowBookmarkIndicator()) {
			mViewHolder.vueWindowBookmarkImg.setImageResource(R.drawable.save);
		} else {
			mViewHolder.vueWindowBookmarkImg
					.setImageResource(R.drawable.save_dark_small);
		}
		if (mShowingList.size() != 0) {
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
		Log.i("LikeStatus",
				"Like status: "
						+ getItem(mCurrentAislePosition).getImageList().get(
								mCurrentDispImageIndex).mLikeDislikeStatus
						+ " LikeCount: "
						+ getItem(mCurrentAislePosition).getImageList().get(
								mCurrentDispImageIndex).mLikesCount);
		mViewHolder.commentCount.setText((mShowingList.size() + " Comments"));
		mViewHolder.bookMarkCount.setText("" + mBookmarksCount);
		mViewHolder.likeCount.setText("" + mLikes);
		mViewHolder.imgContentlay.setVisibility(View.VISIBLE);
		mViewHolder.commentContentlay.setVisibility(View.VISIBLE);
		mViewHolder.vueCommentheader.setVisibility(View.VISIBLE);
		mViewHolder.addCommentlay.setVisibility(View.VISIBLE);
		// mViewHolder.edtCommentLay.setVisibility(View.VISIBLE);
		if (position == 0) {
			mViewHolder.commentContentlay.setVisibility(View.GONE);
			mViewHolder.vueCommentheader.setVisibility(View.GONE);
			mViewHolder.addCommentlay.setVisibility(View.GONE);
			mViewHolder.separator.setVisibility(View.GONE);
			mViewHolder.edtCommentLay.setVisibility(View.GONE);
			// mViewHolder.mWindowContent = mWindowContentTemp;
			try {

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
				// mWindowContentTemp = mViewHolder.mWindowContent;
				mViewHolder.tag = TAG;
				if (mImageRefresh) {
					mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
					mImageRefresh = false;
					mViewLoader.getAisleContentIntoView(mViewHolder,
							scrollIndex, position,
							new DetailImageClickListener(),
							getItem(mCurrentAislePosition), mSetPosition);
					mSetPosition = false;
				}
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
			// mViewHolder.edtCommentLay.setVisibility(View.GONE);
			if (mViewHolder.enterCommentrellay.getVisibility() == View.VISIBLE) {
				mViewHolder.commentSend.setVisibility(View.GONE);
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
									mViewHolder.edtCommentLay);

						}
					});
		}

		else {
			// first two views are image and comment layout. so use position - 2
			// to display all the comments from start
			if (position - 2 < mShowingList.size()) {
				mViewHolder.userComment.setText(mShowingList.get(position - 2));
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
				int showFixedRowCount = 3;
				if (mListCount == (showFixedRowCount + 2)) {
					mListCount = mShowingList.size() + showFixedRowCount;
				} else {
					mListCount = showFixedRowCount + 2;
				}

				notifyDataSetChanged();
			}
		});
		mViewHolder.bookmarklay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
					Log.e("AisleManager",
							"bookmarkfeaturetest: count BOOKMARK RESPONSE: mViewHolder.bookmarklay else called ");
					handleBookmark(bookmarkStatus,
							getItem(mCurrentAislePosition).getAisleId());
				}
				notifyDataSetChanged();
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

	private void notifyAdapter() {
		this.notifyDataSetChanged();
	}

	public void share(final Context context, Activity activity) {
		mShare = new ShareDialog(context, activity);
		FileCache ObjFileCache = new FileCache(context);
		ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
		if (getItem(mCurrentAislePosition).getImageList() != null
				&& getItem(mCurrentAislePosition).getImageList().size() > 0) {
			for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
					.size(); i++) {
				clsShare obj = new clsShare(getItem(mCurrentAislePosition)
						.getImageList().get(i).mCustomImageUrl, ObjFileCache
						.getFile(
								getItem(mCurrentAislePosition).getImageList()
										.get(i).mCustomImageUrl).getPath());
				imageUrlList.add(obj);
			}
			mShare.share(
					imageUrlList,
					getItem(mCurrentAislePosition).getAisleContext().mOccasion,
					(getItem(mCurrentAislePosition).getAisleContext().mFirstName
							+ " " + getItem(mCurrentAislePosition)
							.getAisleContext().mLastName),
					mCurrentDispImageIndex);
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
				@SuppressWarnings("unchecked")
				ArrayList<String> tempCommentList = (ArrayList<String>) mCommentsMapList
						.get(position);
				if (tempCommentList != null) {
					//mShowingList = tempCommentList;
				}
				mLikes = getItem(mCurrentAislePosition).getImageList().get(
						position).mLikesCount;
				mShowingList = getItem(mCurrentAislePosition).getAisleContext().mCommentList;

				notifyDataSetChanged();
				mswipeListner.setFindAtText(getItem(mCurrentAislePosition)
						.getImageList().get(position).mDetalsUrl);
			} else {
				return;
			}
		}

		@Override
		public void onImageDoubleTap() {
			if (mCurrentDispImageIndex == 0) {
				int resizeWidth = getItem(mCurrentAislePosition).getImageList()
						.get(mCurrentDispImageIndex).mTempResizeBitmapwidth;
				int resizeHeight = getItem(mCurrentAislePosition)
						.getImageList().get(mCurrentDispImageIndex).mTempResizedBitmapHeight;
				int cardWidth = VueApplication.getInstance()
						.getVueDetailsCardWidth();
				int cardHeight = VueApplication.getInstance()
						.getVueDetailsCardHeight();
				String writeToSdCard = "***************DETAILS ADAPTER***********************\n";
				writeToSdCard = writeToSdCard + " Aisle Id: "
						+ getItem(mCurrentAislePosition).getAisleId() + "\n";

				for (int i = 0; i < getItem(mCurrentAislePosition)
						.getImageList().size(); i++) {
					writeToSdCard = writeToSdCard
							+ "\n ImageUrl: "
							+ getItem(mCurrentAislePosition).getImageList()
									.get(i).mImageUrl;
					writeToSdCard = writeToSdCard
							+ "\n"
							+ "image Width: "
							+ getItem(mCurrentAislePosition).getImageList()
									.get(i).mAvailableWidth
							+ "image Height: "
							+ getItem(mCurrentAislePosition).getImageList()
									.get(i).mAvailableHeight;
				}

				writeToSdCard = writeToSdCard + " ReSizeImageWidth: "
						+ resizeWidth + " ReSizedImageHeight: " + resizeHeight
						+ "\n";
				writeToSdCard = writeToSdCard + " CardWidth: " + cardWidth
						+ " CardHeight: " + cardHeight + "\n";
				writeToSdCard = writeToSdCard
						+ " Final card Height will be: "
						+ getItem(mCurrentAislePosition)
								.getBestLargetHeightForWindow() + "\n";
				writeToSdCard = writeToSdCard
						+ "\n***************DETAILS ADAPTER***********************";
				writeToSdcard(writeToSdCard);
			} else {
				Toast.makeText(mContext, "Works For Only Starting Image", 1000)
						.show();
				;

			}

			/*
			 * Toast.makeText( mContext, "imgAreaHeight: " + (mTopBottomMargin +
			 * mBestHeight) + " AisleID:  " +
			 * getItem(mCurrentAislePosition).getAisleId(), 1500) .show();
			 * Toast.makeText( mContext, "original image height: " +
			 * getItem(mCurrentAislePosition).getImageList()
			 * .get(mCurrentDispImageIndex).mAvailableHeight + " AisleID:  " +
			 * getItem(mCurrentAislePosition).getAisleId(), 1500) .show();
			 * 
			 * Log.i("mCustomUrlthispos", "mCustomUrlthispos2:" +
			 * getItem(mCurrentAislePosition).getImageList()
			 * .get(mCurrentDispImageIndex).mCustomImageUrl);
			 * Log.i("mCustomUrlthispos", "mCustomUrlthispos3:" +
			 * getItem(mCurrentAislePosition).getImageList()
			 * .get(mCurrentDispImageIndex).mImageUrl);
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

		if (mViewHolder != null && mViewHolder.aisleContentBrowser != null) {
			for (int i = 0; i < mCommentsMapList.size(); i++) {
				mCommentsMapList.remove(i);
			}
			mCommentsMapList = null;
			mViewHolder.aisleContentBrowser.setReferedObjectsNull();
			mViewLoader.clearBrowser(getItem(mCurrentAislePosition)
					.getImageList());
			ScaledImageViewFactory mViewFactory = ScaledImageViewFactory
					.getInstance(mContext);
			for (int i = 0; i < mViewHolder.aisleContentBrowser.getChildCount(); i++) {
				mViewFactory
						.returnUsedImageView((ScaleImageView) mViewHolder.aisleContentBrowser
								.getChildAt(i));
				Log.i("bitmap reclying", "bitmap reclying  in adapter");
			}
			if (mViewHolder.aisleContentBrowser != null) {
				ContentAdapterFactory mContentAdapterFactory = ContentAdapterFactory
						.getInstance(mContext);
				mContentAdapterFactory
						.returnUsedAdapter(mViewHolder.aisleContentBrowser
								.getCustomAdapter());
				mViewHolder.aisleContentBrowser.setCustomAdapter(null);
				mViewHolder.aisleContentBrowser.removeAllViews();
				mViewHolder.aisleContentBrowser = null;
			}

			// mViewHolder.aisleContentBrowser.removeAllViews();
		}
	}

	public void addAisleToContentWindow(Bitmap addedBitmap, String imagePath,
			String imageUrl, int imageWidth, int imageHeight, String title,
			String detailsUrl, String store, String imageId) {
		Utils.isAisleChanged = true;
		Utils.mChangeAilseId = getItem(mCurrentAislePosition).getAisleId();
		AisleImageDetails imgDetails = new AisleImageDetails();
		imgDetails.mAvailableHeight = imageHeight;
		imgDetails.mAvailableWidth = imageWidth;
		Log.i("new image", "new image height: " + imgDetails.mAvailableHeight);
		if (imgDetails.mAvailableHeight > getItem(mCurrentAislePosition)
				.getBestLargetHeightForWindow()) {
			mBestHeight = imgDetails.mAvailableHeight;
			/*
			 * getItem(mCurrentAislePosition).setBestLargestHeightForWindow(
			 * imgDetails.mAvailableHeight, imgDetails.mAvailableWidth);
			 */

			Log.i("new image", "new image height: changed");
		}
		if (imgDetails.mAvailableHeight < getItem(mCurrentAislePosition)
				.getBestHeightForWindow()) {
			getItem(mCurrentAislePosition).setBestHeightForWindow(
					imgDetails.mAvailableHeight);
			Log.i("bestsamallest", "bestsamallest height1: "
					+ imgDetails.mAvailableHeight);
		} else {
			Log.i("bestsamallest",
					"bestsamallest height1 else: window samallest height has not changed ");
		}
		imgDetails.mTitle = title;
		imgDetails.mImageUrl = imageUrl;
		imgDetails.mDetalsUrl = detailsUrl;
		imgDetails.mId = imageId; // offline imageid
		imgDetails.mStore = store;
		imgDetails.mTrendingImageHeight = imgDetails.mAvailableHeight;
		imgDetails.mTrendingImageWidth = imgDetails.mAvailableWidth;
		imgDetails.mOwnerAisleId = getItem(mCurrentAislePosition).getAisleId();
		imgDetails.mOwnerUserId = getItem(mCurrentAislePosition)
				.getAisleContext().mUserId;
		/*
		 * imgDetails = prepareCustomUrl(imgDetails,
		 * getItem(mCurrentAislePosition).getBestHeightForWindow());
		 */
		if (mCurrentDispImageIndex == 0) {
			getItem(mCurrentAislePosition).getImageList().add(imgDetails);
		}

		getItem(mCurrentAislePosition).addAisleContent(
				getItem(mCurrentAislePosition).getAisleContext(),
				getItem(mCurrentAislePosition).getImageList());
		int bestHeight = Utils.modifyHeightForDetailsView(getItem(
				mCurrentAislePosition).getImageList());
		getItem(mCurrentAislePosition)
				.setBestLargestHeightForWindow(bestHeight);
		/*
		 * FileCache fileCache = new FileCache(mContext); File f =
		 * fileCache.getFile(getItem(mCurrentAislePosition)
		 * .getImageList().get(mCurrentDispImageIndex).mCustomImageUrl); File
		 * sourceFile = new File(imagePath); Bitmap bmp =
		 * BitmapLoaderUtils.getInstance().decodeFile(sourceFile,
		 * getItem(mCurrentAislePosition).getBestHeightForWindow(),
		 * VueApplication.getInstance().getVueDetailsCardWidth() / 2,
		 * Utils.DETAILS_SCREEN); Utils.saveBitmap(bmp, f);
		 */

		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.dataObserver();
		mImageRefresh = true;
		if (mViewHolder != null) {
			Log.i("adaptersettings", "adaptersettings: if");
			mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			notifyAdapter();
		} else {
			Log.i("adaptersettings", "adaptersettings: else");
			mswipeListner.onResetAdapter();
		}

	}

	public ArrayList<AisleImageDetails> getImageList() {
		// ArrayList<String> imageList = new ArrayList<String>();
		/*
		 * for (int i = 0; i < getItem(mCurrentAislePosition).getImageList()
		 * .size(); i++) { imageList
		 * .add(getItem(mCurrentAislePosition).getImageList
		 * ().get(i).mCustomImageUrl); }
		 */
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
					getItem(mCurrentAislePosition).getImageList().get(0).mCommentsList = new ArrayList<String>();
				}
				String commentAdded = itemDetails.mCommentsList.get(0);
			} else if (reqType.equals(CHANGE_LIKES)) {
				// aisleId,imageId,likesCount,likeStatus
				likeCount = itemDetails.mLikesCount;
				likeStatus = itemDetails.mLikeDislikeStatus;
				Log.i("likecountissue", "likecountissue: likeCount1: "
						+ likeCount);
				imgRating = new ImageRating();
				imgRating.setAisleId(Long.parseLong(aisleId));
				imgRating.setImageId(Long.parseLong(imageId));
				imgRating.setLiked(likeOrDislike);
				try {
					Log.e("ImageRating Resopnse",
							"SURU ImageRating sendDataToDb in LIKES condution");
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
		mViewHolder.edtCommentLay.setVisibility(View.GONE);
		mViewHolder.enterCommentrellay.setVisibility(View.VISIBLE);
	}

	private void handleBookmark(boolean isBookmarked, String aisleId) {

		AisleBookmark aisleBookmark = new AisleBookmark(null, isBookmarked,
				Long.parseLong(aisleId));
		VueUser storedVueUser = null;
		try {
			Log.i("bookmarkissue", "bookmarkissue handleBookmark");
			storedVueUser = Utils.readUserObjectFromFile(mContext,
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
			AisleManager.getAisleManager().aisleBookmarkUpdate(aisleBookmark,
					Long.valueOf(storedVueUser.getId()).toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
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
   public void  updateListCount(String newComment){
	  mListCount = mListCount +1; 
	  mShowingList.add(0,newComment);
   }
   public void createComment(String commentString){
	      final AisleComment comment =
                  new AisleComment();
  comment.setComment(commentString);
  comment.setCommenterFirstName(getItem(
			mCurrentAislePosition).getAisleContext().mFirstName);
  comment.setCommenterLastName(getItem(
			mCurrentAislePosition).getAisleContext().mLastName);
  comment.setCreatedTimestamp(System.currentTimeMillis());
  comment.setOwnerUserId(Long.parseLong(VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler().getUserId()));
  comment.setOwnerAisleId(Long.parseLong(getItem(
			mCurrentAislePosition).getAisleId()));
  
  /** Save the aisle and verify the save */
  
  
	  new Thread(new Runnable() {
		
		@Override
		public void run() {
			try {
				VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler().testCreateAisleComment(comment);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}).start();
 
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

	/*
	 * public AisleImageDetails prepareCustomUrl(AisleImageDetails imageDetails,
	 * int mWindowSmallestHeight) { String IMAGE_RES_SPEC_REGEX = ".jpg"; String
	 * mImageFormatSpecifier = "._SY%d.jpg"; StringBuilder sb = new
	 * StringBuilder(); String urlReusablePart; String customFittedSizePart;
	 * String regularUrl = imageDetails.mImageUrl; int index = -1; index =
	 * regularUrl.indexOf(IMAGE_RES_SPEC_REGEX); if (-1 != index) { // we have a
	 * match urlReusablePart = regularUrl.split(IMAGE_RES_SPEC_REGEX)[0];
	 * sb.append(urlReusablePart); customFittedSizePart =
	 * String.format(mImageFormatSpecifier, mWindowSmallestHeight);
	 * sb.append(customFittedSizePart); imageDetails.mCustomImageUrl =
	 * sb.toString(); } else { imageDetails.mCustomImageUrl = regularUrl; }
	 * imageDetails.mCustomImageUrl = Utils.addImageInfo(
	 * imageDetails.mCustomImageUrl, imageDetails.mAvailableWidth,
	 * imageDetails.mAvailableHeight);
	 * 
	 * return imageDetails;
	 * 
	 * }
	 */
}
