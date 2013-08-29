package com.lateralthoughts.vue.ui;

import java.util.ArrayList;

public class StackViews {
	private static StackViews sStackView; 
	ArrayList<ViewInfo> mViewStack = new ArrayList<ViewInfo>();
	String mCurrentItem = null;
	public static StackViews getInstance(){
		if(sStackView == null){
			sStackView = new StackViews();
		}
		return sStackView;
	}
	
	public void push(ViewInfo info){
		if(mViewStack.size() == 0){
			ViewInfo trendingScreen = new ViewInfo();
			trendingScreen.mVueName = "Trending";
			trendingScreen.position = 0;
			mViewStack.add(trendingScreen);
		}
		mViewStack.add(info);
		mCurrentItem = info.mVueName;
	}
    public ViewInfo pull(){
		if(mViewStack.size()>0){
				ViewInfo viewInfoo   = mViewStack.remove(mViewStack.size()-1);
				if(mCurrentItem.equals(viewInfoo.mVueName)){
					if(mViewStack.size()>0){
					viewInfoo   = mViewStack.remove(mViewStack.size()-1);
					} else {
						return null;
					}
				}
    	return viewInfoo;
		} else {
    	return null;
		}
    }
    public int getStackCount(){
    	return mViewStack.size();
    }
    public String getTop(){
		return mCurrentItem;
    }
    public ViewInfo getItem(int position){
    	return mViewStack.get(position);
    }
}
