package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class DataEntryAilsePagerAdapter extends PagerAdapter {

	private ArrayList<String> mImagePathsList = null;
	private Context mContext = null;

	public DataEntryAilsePagerAdapter(Context mContext,
			ArrayList<String> imagePathsList) {
		this.mContext = mContext;
		this.mImagePathsList = imagePathsList;
	}

	@Override
	public int getCount() {
		if (mImagePathsList != null)
			return mImagePathsList.size();
		else
			return 0;
	}

	@Override
	public Object instantiateItem(View collection, int position) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dataentry_aisle_pager_row, null);
		ImageView dataEntryRowAisleImage = (ImageView) view
				.findViewById(R.id.dataentry_row_aisele_image);
		try {
			dataEntryRowAisleImage.setImageURI(Uri.fromFile(new File(
					mImagePathsList.get(position))));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		((ViewPager) collection).addView(view, 0);
		return view;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}
}