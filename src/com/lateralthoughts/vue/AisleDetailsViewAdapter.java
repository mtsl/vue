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
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.widget.Toast;
import android.content.Context;
import android.os.Handler;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;

//java util imports
import java.util.ArrayList;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.utils.Utils;

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
    private int mListCount;
    String mTempComments[] = {"Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.",
    		"Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.",
    		"Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.","Love love love the dress! Simple and fabulous.",
    		"Love love love the dress! Simple and fabulous.",};
    ViewHolder holder;
	

    public AisleDetailsViewAdapter(Context c,AisleDetailSwipeListener swipeListner,int listCount, ArrayList<AisleWindowContent> content) {
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
        mListCount = listCount;
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
        TextView aisleContext,commentCount;
        ImageView profileThumbnail;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        AisleWindowContent mWindowContent;
        LinearLayout imgContentlay,commentContentlay;
  	  TextView userComment,enterComment;
  	  ImageView userPic,commentImg;
    }
    @Override
    public int getCount() {
    	return  mListCount;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder viewHolder;
    	 if(convertView == null) {
    		 viewHolder = new ViewHolder();
    		 LayoutInflater layoutInflator = LayoutInflater.from(mContext);
				convertView = layoutInflator.inflate(R.layout.vue_details_adapter, null);
				
				viewHolder.aisleContentBrowser = (AisleContentBrowser) convertView
						.findViewById(R.id.showpiece);
				viewHolder.imgContentlay = (LinearLayout) convertView.findViewById(R.id.vueimagcontent);
				viewHolder.commentContentlay = (LinearLayout) convertView.findViewById(R.id.vue_user_coment_lay);
				viewHolder.aisleDescription = (TextView) convertView.findViewById(R.id.vue_details_descreption);
				viewHolder.aisleDescription.setTextSize(Utils.SMALL_TEXT_SIZE);
				
				
				viewHolder. userPic = (ImageView) convertView
							.findViewById(R.id.vue_user_img);
				viewHolder.userComment = (TextView) convertView
							.findViewById(R.id.vue_user_comment);
				viewHolder.commentCount = (TextView) convertView
						.findViewById(R.id.vuewndow_bookmark_count);
				
				viewHolder.commentImg = (ImageView) convertView.findViewById(R.id.vuewndow_bookmark_img);
				viewHolder.userComment.setTextSize(VueApplication.getInstance().getmTextSize());
			    	 
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
    	
    	 viewHolder = (ViewHolder) convertView.getTag();
    	 viewHolder.imgContentlay.setVisibility(View.VISIBLE);
    		if(position == 0) {
    			viewHolder.commentContentlay.setVisibility(View.GONE);
    			
    			for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
    				viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
    				if (viewHolder.mWindowContent.getAisleId().equalsIgnoreCase(
    						VueApplication.getInstance().getClickedWindowID())) {
    					viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
    					position = i;
    					break;
    				}
    			}  
    			int scrollIndex = 0;
    			mViewLoader.getAisleContentIntoView(viewHolder, scrollIndex, position);
    		
    			
				//gone comment layoutgone
			} else {
				viewHolder.imgContentlay.setVisibility(View.GONE);
				//image content gone
			}
    		setText(viewHolder.aisleDescription,10);
    		setText(viewHolder.userComment,4);
    		viewHolder.commentImg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("listexpand", "listexpand clicked");
					 mswipeListner.onResetAdapter();
					
				}
			});
    	return convertView;
    }
    
 
	private void setText( final TextView descView,int margin_BT) {
		  SpannableString spannableString; 
					int lineCount = descView.getLineCount();
					int eachLineHeight =descView .getLineHeight();
					int defaultTxtViewHeight =  descView.getHeight();
					Log.i("descr", "descr txtViewHeight sdfif: "+descView.getText().toString());
					LinearLayout.LayoutParams params;
					if((lineCount * eachLineHeight) < defaultTxtViewHeight) {
						params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
						params.setMargins(VueApplication.getInstance().getPixel(12), VueApplication.getInstance().getPixel(margin_BT), VueApplication.getInstance().getPixel(12), VueApplication.getInstance().getPixel(margin_BT));
						descView.setLayoutParams(params);
						
					} else {
						
						int howMany = defaultTxtViewHeight/eachLineHeight;
						 Layout layout =  descView.getLayout();
						 int end;
						 int start = 0;
						 String tot = null;
						 final String s = descView.getText().toString();
						 for(int j = 0;j<howMany;j++){
							 end = layout.getLineEnd(j);
							 String temp = s.substring(start, end);
							 if(tot == null) {  
								 tot = temp;
							 } else {
								 tot = tot + temp;
							 }
							 start = end;
						 }
						 if(tot == null) {
							 return;
						 }
						 tot = tot.substring(0,tot.length()-10);
						 tot = tot+"... more";
						 
						 descView.setText(null);
						 descView.setText(tot);
						 params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
							params.setMargins(VueApplication.getInstance().getPixel(12), VueApplication.getInstance().getPixel(margin_BT), VueApplication.getInstance().getPixel(12), VueApplication.getInstance().getPixel(margin_BT));
							descView.setLayoutParams(params);
							 
							  spannableString = new SpannableString(tot);
							  descView.setText(spannableString);
		    				  int pos = tot.length();
		    				  spannableString.setSpan(new ClickableSpan() {
								
								@Override
								public void onClick(View widget) {
									descView.setText(s);
								}
							},pos-4,pos,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		    				  descView.setText(spannableString);
		    				  descView.setMovementMethod(LinkMovementMethod.getInstance());
					}
	 }
	 
	 
 
 
}
