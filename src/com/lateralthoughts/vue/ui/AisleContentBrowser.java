package com.lateralthoughts.vue.ui;

//android imports
import com.lateralthoughts.vue.AisleWindowContent;

import android.content.Context;
import android.util.AttributeSet;

//android UI & graphics imports
import android.widget.AdapterViewFlipper;
import android.widget.ViewFlipper;

public class AisleContentBrowser extends ViewFlipper {
	public AisleContentBrowser(Context context){
		super(context);
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
	}
	
	public AisleContentBrowser(Context context, AttributeSet attribs){
		super(context, attribs);
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
	}
	
	public void setUniqueId(String id){
		mAisleUniqueId = id;
	}
	
	public String getUniqueId(){
		return mAisleUniqueId;
	}
	
	public void setScrollIndex(int scrollIndex){
		mScrollIndex = scrollIndex;
	}
	
	public int getScrollIndex(){
		return mScrollIndex;
	}
	
	private String mAisleUniqueId;
	private int mScrollIndex;

}
