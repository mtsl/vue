package com.lateralthoughts.vue.ui;

import java.util.ArrayList;

public class StackViews {
	private static StackViews sStackView;
	ArrayList<ViewInfo> mViewStack = new ArrayList<ViewInfo>();

	public static StackViews getInstance() {
		if (sStackView == null) {
			sStackView = new StackViews();
		}
		return sStackView;
	}

	public void push(ViewInfo info) {
		mViewStack.add(info);
	}

	public ViewInfo pull() {
		if (mViewStack.size() > 0) {
			ViewInfo viewInfoo = mViewStack.remove(mViewStack.size() - 1);
			return viewInfoo;
		} else {
			return null;
		}
	}

	public int getStackCount() {
		return mViewStack.size();
	}

	public String getTop() {
		return mViewStack.get(mViewStack.size() - 1).mVueName;
	}

	public ViewInfo getItem(int position) {
		return mViewStack.get(position);
	}

	public boolean categoryCheck(String category) {
		boolean flag = false;
		for (int i = 0; i < mViewStack.size() - 1; i++) {
			if (category.equalsIgnoreCase(mViewStack.get(i).mVueName)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	/*
	 * public void showAllItems(){ for(int i = 0;i<mViewStack.size();i++){
	 * Log.i("item",
	 * "item: "+i+" namenamename:   : "+mViewStack.get(i).mVueName); } }
	 */
}
