package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;

public class VueComparisionAdapter extends TrendingAislesGenericAdapter{
  Context mContext;
  private AisleDetailsViewListLoader mViewLoader;
	public VueComparisionAdapter(Context c, AisleContentClickListener listener,
			ArrayList<AisleWindowContent> content) {
		super(c, listener, content);
		mContext = c;
		mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
		
		 
	}
 @Override
public AisleWindowContent getItem(int position) {
	 return mVueTrendingAislesDataModel.getAisleAt(position);
}
 public void setUpImages(LinearLayout topScroller,LinearLayout bottomScroller){
	 ViewHolder viewHolder = new ViewHolder();
	 viewHolder.topScroller  = topScroller;
	 viewHolder.bottomScroller = bottomScroller;
	 Log.i("scrollwidth", "viewHolder.topScroller_width:  "+viewHolder.topScroller.getWidth());
	 Log.i("scrollwidth", "viewHolder.topScroller_height:  "+viewHolder.topScroller.getHeight());
	 
	 int position = 0;
	 viewHolder.mWindowContent = getItem(position);
	 for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
			viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
			if (viewHolder.mWindowContent.getAisleId().equalsIgnoreCase(
					VueApplication.getInstance().getClickedWindowID())) {
				viewHolder.mWindowContent = (AisleWindowContent) getItem(i);
				position = i;
				break;
			}
		}
	 //mViewLoader.getContentIntoCompareview(viewHolder);
	 
 }
 class ViewHolder {
	 LinearLayout topScroller;
	 LinearLayout bottomScroller;
	 AisleWindowContent mWindowContent;
 }
}