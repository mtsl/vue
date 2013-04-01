package com.lateralthoughts.vue.ui;

//android imports
import android.content.Context;
import android.util.AttributeSet;

//android UI & graphics imports
import android.widget.ViewFlipper;

public class AisleContentBrowser extends ViewFlipper {
	public AisleContentBrowser(Context context){
		super(context);
		mAisleUniqueId = "-1";
	}
	
	public AisleContentBrowser(Context context, AttributeSet attribs){
		super(context, attribs);
		mAisleUniqueId = "-1";
	}
	
	public void setUniqueId(String id){
		mAisleUniqueId = id;
	}
	
	public String getUniqueId(int id){
		return mAisleUniqueId;
	}
	
	private String mAisleUniqueId;

}
