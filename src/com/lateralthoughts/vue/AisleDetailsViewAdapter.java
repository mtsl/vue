/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
	private Context mContext;

	private final String TAG = "AisleDetailsViewAdapter";
	private static final boolean DEBUG = false;

	public int firstX;
	public int lastX;
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
	 
	AisleWindowContent mWindowContent_temp;
	int mComentTextDefaultHeight;
	public String vue_user_name;

	int mDescriptionDefaultHeight;
	ViewHolder viewHolder;
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
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.",
			"Love love love the dress! Simple and fabulous.", };
	ViewHolder holder;

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
		int actionBarHeight = mContext.getResources().getDimensionPixelSize(
				tv.resourceId);

		// the show piece item would occupy about 60% of the screen
		mShowPieceHeight = (int) ((mScreenHeight - actionBarHeight) * 0.60f);
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
		TextView aisleContext, commentCount,likeCount;
		ImageView profileThumbnail;
		String uniqueContentId;
		LinearLayout aisleDescriptor;
		AisleWindowContent mWindowContent;

		LinearLayout imgContentlay, commentContentlay;
		LinearLayout vueCommentheader,addCommentlay;
		TextView userComment, enterComment;
		TextView vue_user_enterComment;
		ImageView userPic, commentImg;
		View separator;
	}

	@Override
	public int getCount() {
		return mListCount;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.vue_details_adapter,
					null);

			viewHolder.aisleContentBrowser = (AisleContentBrowser) convertView
					.findViewById(R.id.showpiece);
			viewHolder.imgContentlay = (LinearLayout) convertView
					.findViewById(R.id.vueimagcontent);
			viewHolder.commentContentlay = (LinearLayout) convertView
					.findViewById(R.id.vue_user_coment_lay);

			viewHolder.vueCommentheader = (LinearLayout) convertView
					.findViewById(R.id.vue_comment_header);
			viewHolder.aisleDescription = (TextView) convertView
					.findViewById(R.id.vue_details_descreption);
			viewHolder.separator = (View) convertView
					.findViewById(R.id.separator);
			viewHolder.vue_user_enterComment = (TextView) convertView
					.findViewById(R.id.vue_user_entercomment);
			
			viewHolder.likeCount = (TextView) convertView.findViewById(R.id.vuewndow_lik_count);
			viewHolder.addCommentlay = (LinearLayout) convertView.findViewById(R.id.addcommentlay);
			
			viewHolder.aisleDescription.setTextSize(Utils.SMALL_TEXT_SIZE);

			viewHolder.userPic = (ImageView) convertView
					.findViewById(R.id.vue_user_img);
			viewHolder.userComment = (TextView) convertView
					.findViewById(R.id.vue_user_comment);
			viewHolder.commentCount = (TextView) convertView
					.findViewById(R.id.vuewndow_comment_count);

			viewHolder.commentImg = (ImageView) convertView
					.findViewById(R.id.vuewndow_comment_img);
			viewHolder.userComment.setTextSize(VueApplication.getInstance()
					.getmTextSize());

			viewHolder.userComment.setTextSize(Utils.SMALL_TEXT_SIZE);

			FrameLayout fl = (FrameLayout) convertView
					.findViewById(R.id.showpiece_container);
			FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
					mShowPieceWidth, mShowPieceHeight);
			LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			viewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
			viewHolder.aisleContentBrowser
					.setAisleDetailSwipeListener(mswipeListner);
			FrameLayout.LayoutParams thumbnailParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT, mThumbnailsHeight);
			viewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;

			convertView.setTag(viewHolder);
		}
		
		viewHolder.likeCount.setText(""+mLikes);
		viewHolder = (ViewHolder) convertView.getTag();

		viewHolder.imgContentlay.setVisibility(View.VISIBLE);
		viewHolder.commentContentlay.setVisibility(View.VISIBLE);
		viewHolder.vueCommentheader.setVisibility(View.VISIBLE);
		viewHolder.addCommentlay.setVisibility(View.VISIBLE);
		if (position == 0) {
			viewHolder.commentContentlay.setVisibility(View.GONE);
			viewHolder.vueCommentheader.setVisibility(View.GONE);
			viewHolder.addCommentlay.setVisibility(View.GONE);
			viewHolder.separator.setVisibility(View.GONE);

			for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
				viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
				if (viewHolder.mWindowContent.getAisleId().equalsIgnoreCase(
						VueApplication.getInstance().getClickedWindowID())) {
					viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
					position = i;
					break;
				}
			}
		
			vue_user_name = viewHolder.mWindowContent.getAisleContext().mFirstName;
			Log.i("name", "name: "+vue_user_name);
			int scrollIndex = 0;
			mWindowContent_temp = viewHolder.mWindowContent;
			mViewLoader.getAisleContentIntoView(viewHolder, scrollIndex,
					position,new DetailImageClickListener());

			// gone comment layoutgone
		} else if (position == 1) {
			viewHolder.imgContentlay.setVisibility(View.GONE);
			viewHolder.commentContentlay.setVisibility(View.GONE);
			viewHolder.addCommentlay.setVisibility(View.GONE);
			// image content gone
		} else if(position == mListCount-1){
			viewHolder.imgContentlay.setVisibility(View.GONE);
			viewHolder.vueCommentheader.setVisibility(View.GONE);
			viewHolder.commentContentlay.setVisibility(View.GONE);
			viewHolder.addCommentlay.setVisibility(View.VISIBLE);
			 
		}
		
		
		else {
			viewHolder.imgContentlay.setVisibility(View.GONE);
			viewHolder.vueCommentheader.setVisibility(View.GONE);
			viewHolder.addCommentlay.setVisibility(View.GONE);
		}
 
		
		if (viewHolder.aisleDescription.getLayout() != null) {
			int h = viewHolder.aisleDescription.getLayout().getHeight();

		}

		viewHolder.commentImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("listexpand", "listexpand clicked");
				mswipeListner.onResetAdapter();

			}
		});
		viewHolder.commentCount.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mswipeListner.onResetAdapter();
			}
		});

		return convertView;
	}

	void setText(final TextView descView, int margin_BT, int defaultHeight) {
		SpannableString spannableString;
		int lineCount = descView.getLineCount();
		int eachLineHeight = descView.getLineHeight();
		int defaultTxtViewHeight = descView.getHeight();

		LinearLayout.LayoutParams params;
		if ((lineCount * eachLineHeight) < defaultTxtViewHeight) {

			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(VueApplication.getInstance().getPixel(12),
					VueApplication.getInstance().getPixel(margin_BT),
					VueApplication.getInstance().getPixel(12), VueApplication
							.getInstance().getPixel(margin_BT));
			descView.setLayoutParams(params);

		} else {
			// Log.i("descr", "descr  IN ELSE CONDITIONS ");
			int howMany = defaultTxtViewHeight / eachLineHeight;
			Layout layout = descView.getLayout();
			int end;
			int start = 0;
			String tot = null;
			final String s = descView.getText().toString();
			// Log.i("descr", "descr  IN ELSE CONDITIONS s "+s);
			for (int j = 0; j < howMany; j++) {
				end = layout.getLineEnd(j);
				String temp = s.substring(start, end);
				if (tot == null) {
					tot = temp;
				} else {
					tot = tot + temp;
				}
				start = end;
			}
			if (tot == null) {
				// Log.i("descr", "descr  IN ELSE CONDITIONS RETURN ");
				return;
			}
			tot = tot.substring(0, tot.length() - 10);
			tot = tot + "... more";

			descView.setText(null);
			descView.setText(tot);
			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(VueApplication.getInstance().getPixel(12),
					VueApplication.getInstance().getPixel(margin_BT),
					VueApplication.getInstance().getPixel(12), VueApplication
							.getInstance().getPixel(margin_BT));
			descView.setLayoutParams(params);

			spannableString = new SpannableString(tot);
			descView.setText(spannableString);
			int pos = tot.length();
			spannableString.setSpan(new ClickableSpan() {

				@Override
				public void onClick(View widget) {
					descView.setText(s);
				}
			}, pos - 4, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			// Log.i("descr",
			// "descr  IN ELSE CONDITIONS SETTEXT "+spannableString.toString());
			descView.setText(spannableString);
			descView.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	private void notifyAdapter() {
		this.notifyDataSetChanged();
	}
	/**
	 * By Krishna.V Sharing content
	 */
	public void share(Context context, Activity activity) {
		ShareDialog share = new ShareDialog(context, activity);

		FileCache ObjFileCache = new FileCache(context);

		List<File> imageUrlList = new ArrayList<File>();

		if (mWindowContent_temp.getImageList() != null
				&& mWindowContent_temp.getImageList().size() > 0) {
			for (int i = 0; i < mWindowContent_temp.getImageList().size(); i++) {
				imageUrlList.add(ObjFileCache.getFile(mWindowContent_temp
						.getImageList().get(i).mCustomImageUrl));
			}

			share.share(
					imageUrlList,
					mWindowContent_temp.getAisleContext().mOccasion,
					(mWindowContent_temp.getAisleContext().mFirstName + " " + mWindowContent_temp
							.getAisleContext().mLastName));
		}

	}
	
	   private class DetailImageClickListener implements DetailClickListener{

		@Override
		public void onImageClicked() {
			mLikes += 1;
			notifyAdapter();
			
			
		}

		@Override
		public void onImageLongPress() {
			if(mLikes != 0)
			 mLikes -= 1;
			 
			notifyAdapter();
			 
			
		}
	       
	    }
	
	
 
}
