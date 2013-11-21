package com.lateralthoughts.vue.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DetailsImageView  extends ImageView{
     private boolean mIsAnimate;
	public DetailsImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public DetailsImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public DetailsImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public void setAnimStatus(boolean animate){
		mIsAnimate = animate;
	}
	public boolean isAnimated(){
		return mIsAnimate;
	}

}
