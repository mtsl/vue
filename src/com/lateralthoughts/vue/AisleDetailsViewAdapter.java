/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
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
	 
	ArrayList<String> mCustomUrls = new ArrayList<String>();
	private LoginWarningMessage mLoginWarningMessage = null;
	@SuppressWarnings("unchecked")
	public AisleDetailsViewAdapter(Context c,
			AisleDetailSwipeListener swipeListner, int listCount,
			ArrayList<AisleWindowContent> content) {
		super(c, content);
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
		if (getItem(mCurrentAislePosition) != null) {
			mBookmarksCount = getItem(mCurrentAislePosition).getmAisleBookmarksCount();
			VueApplication.getInstance().setClickedWindowCount(
					getItem(mCurrentAislePosition).getImageList().size());

			for (int i = 0; i < getItem(mCurrentAislePosition).getImageList().size(); i++) {
				mCustomUrls.add(getItem(mCurrentAislePosition).getImageList().get(i).mCustomImageUrl);
				if (getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight > mBestHeight) {
					mBestHeight = getItem(mCurrentAislePosition).getImageList().get(i).mAvailableHeight;
				}

				mCommentsMapList.put(i, getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList);
				if (getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList == null) {
					// TODO: for temp comments display need to replace this
					getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList = new ArrayList<String>();
					if (i % 2 == 0) {
						for (int k = 0; k < 6; k++) {
							getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList
									.add("Love Love vue the dress! Simple and fabulous.");
						}
					} else {
						for (int k = 0; k < 10; k++) {
							getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList
									.add("vue vue vue  sample test comments");
						}
					}
					mCommentsMapList.put(i,
							getItem(mCurrentAislePosition).getImageList().get(i).mCommentsList);
				}
			}
			mImageDetailsArr = (ArrayList<String>) mCustomUrls.clone();
			Log.i("clone", "clone: "+mImageDetailsArr);
			if(mImageDetailsArr != null){
				for (int i = 0;i<mImageDetailsArr.size();i++){
					Log.i("clone", "clone1: "+mImageDetailsArr.get(i));
				}
			}
			mShowingList = getItem(mCurrentAislePosition).getImageList().get(0).mCommentsList;
			mLikes = getItem(mCurrentAislePosition).getImageList().get(0).mLikesCount;
		}
	}

	@Override
	public AisleWindowContent getItem(int position) {
		return mVueTrendingAislesDataModel.getAisleAt(position);
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
		LinearLayout vueCommentheader, addCommentlay;
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
			FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth(),
					(mBestHeight + mTopBottomMargin));
			mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
			mViewHolder.aisleContentBrowser
					.setAisleDetailSwipeListener(mswipeListner);

			mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
				VueApplication.getInstance().getScreenWidth(), mBestHeight
						+ mTopBottomMargin);
		if(mViewHolder.aisleContentBrowser != null)
		mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);

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
		if (getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus == IMG_LIKE_STATUS) {
			mViewHolder.likeImg.setImageResource(R.drawable.heart);
		} else {
			mViewHolder.likeImg.setImageResource(R.drawable.heart_dark);
		}

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
			//mViewHolder.mWindowContent = mWindowContentTemp;
			try {
				mVueusername = getItem(mCurrentAislePosition).getAisleContext().mFirstName;
				int scrollIndex = 0;
				//mWindowContentTemp = mViewHolder.mWindowContent;
				mViewHolder.tag = TAG;
                if(mImageRefresh) {
                	mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
                	mImageRefresh = false;
				mViewLoader.getAisleContentIntoView(mViewHolder, scrollIndex,
						position, new DetailImageClickListener(),getItem(mCurrentAislePosition));
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
			if (position - 1 < mShowingList.size()) {
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

			@Override
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
				if (getItem(mCurrentAislePosition).getWindowBookmarkIndicator()) {
					mBookmarksCount--;
					getItem(mCurrentAislePosition).setmAisleBookmarksCount(mBookmarksCount);
					getItem(mCurrentAislePosition).setWindowBookmarkIndicator(false);
				} else {
					mBookmarksCount++;
					getItem(mCurrentAislePosition).setmAisleBookmarksCount(mBookmarksCount);
					getItem(mCurrentAislePosition).setWindowBookmarkIndicator(true);
				}
				sendDataToDb(mCurrentDispImageIndex, CHANGE_BOOKMARK);
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
			for (int i = 0; i < getItem(mCurrentAislePosition).getImageList().size(); i++) {
				clsShare obj = new clsShare(getItem(mCurrentAislePosition).getImageList()
						.get(i).mCustomImageUrl,
						ObjFileCache
								.getFile(
										getItem(mCurrentAislePosition).getImageList()
												.get(i).mCustomImageUrl)
								.getPath());
				imageUrlList.add(obj);
			}
			mShare.share(
					imageUrlList,
					getItem(mCurrentAislePosition).getAisleContext().mOccasion,
					(getItem(mCurrentAislePosition).getAisleContext().mFirstName + " " + getItem(mCurrentAislePosition)
							.getAisleContext().mLastName));
		}
		if (getItem(mCurrentAislePosition).getImageList() != null
				&& getItem(mCurrentAislePosition).getImageList().size() > 0) {
			FileCache ObjFileCache1 = new FileCache(context);
			for (int i = 0; i < getItem(mCurrentAislePosition).getImageList().size(); i++) {
				final File f = ObjFileCache1.getFile(getItem(mCurrentAislePosition)
						.getImageList().get(i).mCustomImageUrl);
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
							Log.e(TAG, arg0.getMessage());
						}
					};
					if (getItem(mCurrentAislePosition).getImageList().get(i).mCustomImageUrl != null) {
						@SuppressWarnings("unchecked")
						ImageRequest imagerequestObj = new ImageRequest(
								getItem(mCurrentAislePosition).getImageList().get(i).mCustomImageUrl,
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
					mShowingList = tempCommentList;
				}
				mLikes = getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount;

				notifyDataSetChanged();
			} else {
				return;
			}
		}

		@Override
		public void onImageDoubleTap() {
			Toast.makeText(
					mContext,
					"imgAreaHeight: " + (mTopBottomMargin + mBestHeight)
							+ " AisleID:  " + getItem(mCurrentAislePosition).getAisleId(),
					1500).show();
			Toast.makeText(
					mContext,
					"original image height: "
							+ getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mAvailableHeight
							+ " AisleID:  " + getItem(mCurrentAislePosition).getAisleId(),
					1500).show();
		}

		@Override
		public void onSetBrowserArea(String area) {
			// TODO Auto-generated method stub
			
		}

	}

	private void toggleRatingImage() {
		if (checkLimitForLoginDialog()) {
			if (mLoginWarningMessage == null) {
				mLoginWarningMessage = new LoginWarningMessage(mContext);
			}
			mLoginWarningMessage.showLoginWarningMessageDialog(
					"You need to Login with the app to Like.", true, false, 0, null, null);
		} else {
			if (mCurrentDispImageIndex >= 0
					&& mCurrentDispImageIndex < getItem(mCurrentAislePosition).getImageList().size()) {
				if (getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus == IMG_LIKE_STATUS) {
					getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus = IMG_NONE_STATUS;
					getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikesCount = getItem(mCurrentAislePosition).getImageList()
							.get(mCurrentDispImageIndex).mLikesCount - 1;
				} else {
					getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikeDislikeStatus = IMG_LIKE_STATUS;
					getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikesCount = getItem(mCurrentAislePosition).getImageList()
							.get(mCurrentDispImageIndex).mLikesCount + 1;
				}
				mLikes = getItem(mCurrentAislePosition).getImageList().get(mCurrentDispImageIndex).mLikesCount;
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
					"You need to Login with the app to Like.", true, false, 0, null, null);
		} else {
			if (position >= 0 && position < getItem(mCurrentAislePosition).getImageList().size()) {
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
					"You need to Login with the app to Like.", true, false, 0, null, null);
		} else {
			// increase the likes count
			if (mCurrentDispImageIndex >= 0
					&& mCurrentDispImageIndex < getItem(mCurrentAislePosition).getImageList().size()) {
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
					"You need to Login with the app to Like.", true, false, 0, null, null);
		} else {
			// decrease the likes count
			if (mCurrentDispImageIndex >= 0
					&& mCurrentDispImageIndex < getItem(mCurrentAislePosition).getImageList().size()) {
				mIsLikeImageClicked = true;
				onChangeDislikesCount(mCurrentDispImageIndex);
			}
		}
	}

	public void setAisleBrowserObjectsNull() {
	 
		if (mViewHolder != null && mViewHolder.aisleContentBrowser != null) {
			for(int i=0;i<mCommentsMapList.size();i++) {
				mCommentsMapList.remove(i);
			}
			mCommentsMapList = null;
			mViewHolder.aisleContentBrowser.setReferedObjectsNull();
			mViewHolder.aisleContentBrowser.removeAllViews();
			mViewLoader.clearBrowser();
			mViewHolder.aisleContentBrowser = null;
		}
	}

	public void addAisleToContentWindow(Bitmap addedBitmap, String uri,
			String title) {
		Log.e("Land", "vueland 17");
		AisleImageDetails imgDetails = new AisleImageDetails();
		// TODO:temperory setting remove later these asignments.
		imgDetails.mAvailableHeight = 500;
		imgDetails.mAvailableWidth = 500;
		if (addedBitmap != null) {
			imgDetails.mAvailableHeight = addedBitmap.getHeight();
			imgDetails.mAvailableWidth = addedBitmap.getWidth();
		}
		if (imgDetails.mAvailableHeight > VueApplication.getInstance()
				.getVueDetailsCardHeight()) {
			imgDetails.mAvailableHeight = VueApplication.getInstance()
					.getVueDetailsCardHeight();
		}
		if (imgDetails.mAvailableHeight > mBestHeight) {
			mBestHeight = imgDetails.mAvailableHeight;
		}
		imgDetails.mTitle = title;
		// imgDetails.mImageUrl =
		// "http://ecx.images-amazon.com/images/I/31WPX7Qn3wL.jpg";
		imgDetails.mImageUrl = uri;
		Log.i("added url", "added url aisleDetailsviewadapter " + uri);
		imgDetails.mDetalsUrl = "";
		imgDetails.mId = "";
		imgDetails.mStore = "";
		getItem(mCurrentAislePosition).getImageList().add(mCurrentDispImageIndex, imgDetails);
		getItem(mCurrentAislePosition).addAisleContent(
				getItem(mCurrentAislePosition).getAisleContext(), getItem(mCurrentAislePosition).getImageList());
		FileCache fileCache = new FileCache(mContext);
		File f = fileCache.getFile(getItem(mCurrentAislePosition).getImageList().get(0).mCustomImageUrl);
		Utils.saveBitmap(BitmapFactory.decodeFile(uri), f);
		getItem(mCurrentAislePosition).mIsDataChanged = true;
		//mswipeListner.onResetAdapter();
		mImageRefresh = true;
		if(mViewHolder!= null){
			Log.e("Land", "vueland 18");
		mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		notifyAdapter();
		} else {
			Log.e("Land", "vueland 19");
			mswipeListner.onResetAdapter();
		}
	
		 //
	}

	public void sendDataToDb(int imgPosition, String reqType) {
		String aisleId;
		String imageId;
		AisleImageDetails itemDetails;

		if (getItem(mCurrentAislePosition).getImageList() != null && getItem(mCurrentAislePosition).getImageList().size() != 0) {
			aisleId = getItem(mCurrentAislePosition).getAisleId();
			itemDetails = getItem(mCurrentAislePosition).getImageList().get(imgPosition);
			imageId = itemDetails.mId;
			if (reqType.equals(CHANGE_BOOKMARK)) {
				// aisleId,imageId,bookMarksCount,bookmarkIndicator
				int bookMarksCount = getItem(mCurrentAislePosition)
						.getmAisleBookmarksCount();
				boolean bookmarkIndicator = getItem(mCurrentAislePosition)
						.getWindowBookmarkIndicator();
			} else if (reqType.equals(CHANGE_COMMENT)) {
				// aisleId,imageId,comment
				String commentAdded = itemDetails.mCommentsList.get(0);
			} else if (reqType.equals(CHANGE_LIKES)) {
				// aisleId,imageId,likesCount,likeStatus
				int likeCount = itemDetails.mLikesCount;
				int likeStatus = itemDetails.mLikeDislikeStatus;
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
 private void onChangeLikesCount(int position){
		if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_LIKE_STATUS) {
			getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_LIKE_STATUS;
		} else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_NONE_STATUS) {
			
			getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(mCurrentAislePosition).getImageList()
					.get(position).mLikesCount + 1;
			getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_LIKE_STATUS;
			sendDataToDb(position, CHANGE_LIKES);
		}
		if (position == mCurrentDispImageIndex) {
			mLikes = getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount;
			notifyAdapter();
		}
 }
 private void onChangeDislikesCount(int position){
		if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_LIKE_STATUS) {
			getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_NONE_STATUS;
			getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount = getItem(mCurrentAislePosition).getImageList()
					.get(position).mLikesCount - 1;
			sendDataToDb(position, CHANGE_LIKES);
		} else if (getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus == IMG_NONE_STATUS) {
			getItem(mCurrentAislePosition).getImageList().get(position).mLikeDislikeStatus = IMG_NONE_STATUS;
			sendDataToDb(position, CHANGE_LIKES);
		}
		if (position == mCurrentDispImageIndex) {
			mLikes = getItem(mCurrentAislePosition).getImageList().get(position).mLikesCount;
			notifyAdapter();
		}
 }
}
