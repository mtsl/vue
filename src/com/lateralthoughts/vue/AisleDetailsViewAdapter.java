/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.widget.Toast;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

//java util imports
import java.util.ArrayList;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
    private Context mContext;
    
    private final String TAG = "AisleDetailsViewAdapter";
    private static final boolean DEBUG = false;
    
    public int firstX;
    public int lastX;
    private AisleDetailsViewListLoader mViewLoader;
    private AisleDetailSwipeListener mswipeListner;
    
    //we need to customize the layout depending on screen height & width which
    //we will get on the fly
    private int mScreenHeight;
    private int mScreenWidth;
    private int mShowPieceHeight;
    private int mShowPieceWidth;
    private int mThumbnailsHeight;
    private int mActionBarHeight;

    public AisleDetailsViewAdapter(Context c,AisleDetailSwipeListener swipeListner, ArrayList<AisleWindowContent> content) {
        super(c, content);
        mContext = c;
        mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
        mswipeListner = swipeListner;
        mScreenHeight = VueApplication.getInstance().getScreenHeight();
        mScreenWidth = VueApplication.getInstance().getScreenWidth();
        float scale = mContext.getResources().getDisplayMetrics().density;
        
        //the action bar height is 50 dp
        mActionBarHeight = (int)(50 * scale + 0.5f);
        
        TypedValue tv = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = mContext.getResources().getDimensionPixelSize(tv.resourceId);
        
        //the show piece item would occupy about 60% of the screen
        mShowPieceHeight = (int)((mScreenHeight-actionBarHeight)*0.60f);
        mShowPieceWidth = (int)(mScreenWidth);
        //the thumbnail item would occupy about 25% of the screen
        mThumbnailsHeight = (int)(mScreenHeight - (mShowPieceHeight + mActionBarHeight)); //(int)(mScreenHeight*0.30f);
        
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");      
    }
    @Override
    public AisleWindowContent getItem(int position){     
        return mVueTrendingAislesDataModel.getAisleAt(position);
    }
    static class ViewHolder{
        AisleContentBrowser aisleContentBrowser;
        HorizontalScrollView thumbnailContainer;
      //  LinearLayout thumbnailScroller;
        TextView aisleDescription;
        TextView aisleOwnersName;
        TextView aisleContext;
        ImageView profileThumbnail;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        AisleWindowContent mWindowContent;
    }
    
	public LinearLayout prepareDetailsVue() {
		ViewHolder holder;
		View convertView = null;
		int position = 0;
		LinearLayout vue_details_container = null;
		if (null == convertView) {
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					mScreenWidth, mScreenHeight - 156);
			convertView = layoutInflator.inflate(
					R.layout.aisle_detailed_view_row_item, null);
			vue_details_container = (LinearLayout) convertView
					.findViewById(R.id.vue_details_container);
			//convertView.setLayoutParams(params);
			holder = new ViewHolder();
			holder.aisleContentBrowser = (AisleContentBrowser) convertView
					.findViewById(R.id.showpiece);
			holder.aisleDescription = (TextView) convertView.findViewById(R.id.vue_details_descreption);
			holder.aisleDescription.setTextSize(VueApplication.getInstance().getmTextSize());
			FrameLayout fl = (FrameLayout) convertView
					.findViewById(R.id.showpiece_container);
			FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
					mShowPieceWidth, mShowPieceHeight);
			LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			holder.aisleContentBrowser.setLayoutParams(showpieceParams);
			holder.aisleContentBrowser
					.setAisleDetailSwipeListener(mswipeListner);
			FrameLayout.LayoutParams thumbnailParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT, mThumbnailsHeight);
			holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();
		holder.mWindowContent = (AisleWindowContent) getItem(position);
		for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
			holder.mWindowContent = (AisleWindowContent) getItem(i);
			if (holder.mWindowContent.getAisleId().equalsIgnoreCase(
					VueApplication.getInstance().getClickedWindowID())) {
				holder.mWindowContent = (AisleWindowContent) getItem(i);
				position = i;
				break;
			}
		}

		int scrollIndex = 0;
		mViewLoader.getAisleContentIntoView(holder, scrollIndex, position);
		return vue_details_container;

	}
   /**
    * 
    * @param view
    * TODO: need to used the pooled views to avoid the unnecessary garbage collection
    */
	public void addComments(View view) {
		LayoutInflater layoutInflator = LayoutInflater.from(mContext);
		for (int i = 0; i < 5; i++) {
			View commentView = layoutInflator.inflate(R.layout.comments, null);
			ImageView userImage = (ImageView) commentView
					.findViewById(R.id.vue_user_img);
			TextView userComment = (TextView) commentView
					.findViewById(R.id.vue_user_comment);
			userComment.setTextSize(VueApplication.getInstance().getmTextSize());
			if (i == 4) {
				TextView addComment = (TextView) commentView
						.findViewById(R.id.vue_user_entercomment);
				addComment.setVisibility(View.VISIBLE);
				addComment.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT)
								.show();

					}
				});

			}
			((ViewGroup) view).addView(commentView);
		}
	}
}
