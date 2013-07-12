/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
	private Context mContext;

	private final String TAG = "AisleDetailsViewAdapter";
	private static final boolean DEBUG = false;

 
	private AisleDetailsViewListLoader mViewLoader;
	private AisleDetailSwipeListener mswipeListner;

	// we need to customize the layout depending on screen height & width which
	// we will get on the fly
	private int mScreenHeight;
	private int mScreenWidth;
	private int mShowPieceHeight;
	private int mShowPieceWidth;
	private int mThumbnailsHeight;
	private int mActionBarHeight;
	private int mListCount;
	private int mLikes = 5;
	private boolean mallowLike = true,mallowDisLike = true;
	private boolean isImageClciked = false;
	AisleWindowContent mWindowContent_temp;
	int mComentTextDefaultHeight;
	public String mVueusername;
	ShareDialog mShare ;
	int mDescriptionDefaultHeight;

	ViewHolder mViewHolder;
	String mTempComments[] = {
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			  };
	String mTempComments2[] = {	"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous."};
	ViewHolder mHolder;

	public AisleDetailsViewAdapter(Context c,
			AisleDetailSwipeListener swipeListner, int listCount,
			ArrayList<AisleWindowContent> content) {
		super(c, content);
		mContext = c;
		mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
		mswipeListner = swipeListner;
		mScreenHeight = VueApplication.getInstance().getScreenHeight();
		mScreenWidth = VueApplication.getInstance().getScreenWidth();
		float scale = mContext.getResources().getDisplayMetrics().density;

		// the action bar height is 50 dp
		mActionBarHeight = (int) (50 * scale + 0.5f);

		TypedValue tv = new TypedValue();
		mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
				true);
		 int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	        if (currentapiVersion >= 11){
	        	int actionBarHeight = mContext.getResources().getDimensionPixelSize(
	    				tv.resourceId);
	        	mShowPieceHeight = (int) ((mScreenHeight - actionBarHeight) * 0.60f);
	        } 
		// the show piece item would occupy about 60% of the screen
		mShowPieceWidth = (int) (mScreenWidth);
		// the thumbnail item would occupy about 25% of the screen
		mThumbnailsHeight = (int) (mScreenHeight - (mShowPieceHeight + mActionBarHeight)); // (int)(mScreenHeight*0.30f);
		mListCount = listCount;
		mComentTextDefaultHeight = VueApplication.getInstance().getPixel(32);
		mDescriptionDefaultHeight = VueApplication.getInstance().getPixel(50);
		if (DEBUG)
			Log.e(TAG, "About to initiate request for trending aisles");
	}

	@Override
	public AisleWindowContent getItem(int position) {
		return mVueTrendingAislesDataModel.getAisleAt(position);
	}
	static class ViewHolder {
		AisleContentBrowser aisleContentBrowser;
		HorizontalScrollView thumbnailContainer;
		// LinearLayout thumbnailScroller;
		TextView aisleDescription;
		TextView aisleOwnersName;
		TextView aisleContext, commentCount, likeCount;
		ImageView profileThumbnail;
		String uniqueContentId;
		LinearLayout aisleDescriptor;
		AisleWindowContent mWindowContent;
		LinearLayout imgContentlay, commentContentlay;
		LinearLayout vueCommentheader, addCommentlay;
		TextView userComment, enterComment;
		TextView vue_user_enterComment;
		ImageView userPic, commentImg, likeImg;
		RelativeLayout exapandHolder;
		EditText edtComment;
		View separator;
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
					.findViewById(R.id.showpiece);
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
			mViewHolder.vue_user_enterComment = (TextView) convertView
					.findViewById(R.id.vue_user_entercomment);
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
			mViewHolder.commentImg = (ImageView) convertView
					.findViewById(R.id.vuewndow_comment_img);
			mViewHolder.userComment.setTextSize(VueApplication.getInstance()
					.getmTextSize());
			mViewHolder.userComment.setTextSize(Utils.SMALL_TEXT_SIZE);
			FrameLayout fl = (FrameLayout) convertView
					.findViewById(R.id.showpiece_container);
			FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth(),
					(VueApplication.getInstance().getScreenHeight() * 60) / 100);
			LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
			mViewHolder.aisleContentBrowser
					.setAisleDetailSwipeListener(mswipeListner);
			FrameLayout.LayoutParams thumbnailParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT, mThumbnailsHeight);
			mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(mViewHolder);
		}
		mViewHolder.likeCount.setText("" + mLikes);
		mViewHolder = (ViewHolder) convertView.getTag();
		mViewHolder.imgContentlay.setVisibility(View.VISIBLE);
		mViewHolder.commentContentlay.setVisibility(View.VISIBLE);
		mViewHolder.vueCommentheader.setVisibility(View.VISIBLE);
		mViewHolder.addCommentlay.setVisibility(View.VISIBLE);
		if (position == 0) {
			mViewHolder.commentContentlay.setVisibility(View.GONE);
			mViewHolder.vueCommentheader.setVisibility(View.GONE);
			mViewHolder.addCommentlay.setVisibility(View.GONE);
			mViewHolder.separator.setVisibility(View.GONE);
			for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
				mViewHolder.mWindowContent = (AisleWindowContent) getItem(i);
				if (mViewHolder.mWindowContent.getAisleId().equalsIgnoreCase(
						VueApplication.getInstance().getClickedWindowID())) {
					mViewHolder.mWindowContent = (AisleWindowContent) getItem(i);
					position = i;
					break;
				}
			}
			try {
				mVueusername = mViewHolder.mWindowContent.getAisleContext().mFirstName;
				int scrollIndex = 0;
				mWindowContent_temp = mViewHolder.mWindowContent;
				mViewLoader.getAisleContentIntoView(mViewHolder, scrollIndex,
						position, new DetailImageClickListener());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// gone comment layoutgone
		} else if (position == 1) {
			if (isImageClciked) {
				isImageClciked = false;
				Animation rotate = AnimationUtils.loadAnimation(mContext,
						R.anim.bounce);
				mViewHolder.likeImg.startAnimation(rotate);
			}
			mViewHolder.imgContentlay.setVisibility(View.GONE);
			mViewHolder.commentContentlay.setVisibility(View.GONE);
			mViewHolder.addCommentlay.setVisibility(View.GONE);
			// image content gone
		} else if (position == mListCount - 1) {
			mViewHolder.separator.setVisibility(View.GONE);
			mViewHolder.imgContentlay.setVisibility(View.GONE);
			mViewHolder.vueCommentheader.setVisibility(View.GONE);
			mViewHolder.commentContentlay.setVisibility(View.GONE);
			mViewHolder.addCommentlay.setVisibility(View.VISIBLE);
			mViewHolder.vue_user_enterComment
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mswipeListner.onAddCommentClick(
									mViewHolder.vue_user_enterComment,
									mViewHolder.edtComment);
						}
					});
		}

		else {
			mViewHolder.userComment.setText(mTempComments2[position - 2]);
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
				if (mTempComments2.length <= 2) {
					mTempComments2 = new String[mTempComments.length];
					for (int i = 0; i < mTempComments.length; i++) {
						mTempComments2[i] = mTempComments[i];
					}
					mListCount = mTempComments2.length;
				} else {
					mTempComments2 = new String[2];
					for (int i = 0; i < 2; i++) {
						mTempComments2[i] = mTempComments[i];
					}
					mListCount = 5;
				}
				notifyDataSetChanged();
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
		if (mWindowContent_temp.getImageList() != null
				&& mWindowContent_temp.getImageList().size() > 0) {
			for (int i = 0; i < mWindowContent_temp.getImageList().size(); i++) {
				clsShare obj = new clsShare(mWindowContent_temp.getImageList()
						.get(i).mCustomImageUrl,
						ObjFileCache
								.getFile(
										mWindowContent_temp.getImageList().get(
												i).mCustomImageUrl).getPath());
				imageUrlList.add(obj);
			}
			mShare.share(
					imageUrlList,
					mWindowContent_temp.getAisleContext().mOccasion,
					(mWindowContent_temp.getAisleContext().mFirstName + " " + mWindowContent_temp
							.getAisleContext().mLastName));
		}
		if (mWindowContent_temp.getImageList() != null
				&& mWindowContent_temp.getImageList().size() > 0) {
			FileCache ObjFileCache1 = new FileCache(context);
			for (int i = 0; i < mWindowContent_temp.getImageList().size(); i++) {
				final File f = ObjFileCache1.getFile(mWindowContent_temp
						.getImageList().get(i).mCustomImageUrl);
				if (!f.exists()) {
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
					ImageRequest imagerequestObj = new ImageRequest(
							mWindowContent_temp.getImageList().get(i).mCustomImageUrl,
							listener, 0, 0, null, errorListener);
					VueApplication.getInstance().getRequestQueue()
							.add(imagerequestObj);
				}
			}
		}
	} 


	/**
	 * 
	 *  
	 *To handle the click and long press event on the imageview in the aisle content
	 *and to allow only one like and one dislike allows
	 */
	   private class DetailImageClickListener implements DetailClickListener{
		@Override
		public void onImageClicked() {
			if(mallowLike) {
			mLikes += 1;
			mallowLike = false;
			mallowDisLike = true;
			}
			isImageClciked = true;
			notifyAdapter();
		}
		@Override
		public void onImageLongPress() {
			if(mLikes != 0 && mallowDisLike) {
			 mLikes -= 1;
			 mallowDisLike = false;
			 mallowLike = true;
			}
			isImageClciked = true;
			notifyAdapter();
			 
			
		}
	       
	    }
 
}
