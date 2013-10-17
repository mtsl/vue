package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class DataEntryAilsePagerAdapter extends PagerAdapter {

	private ArrayList<DataentryImage> mImagePathsList = null;
	private Context mContext = null;
	private DataEntryFragment mDataEntryFragment = null;

	public DataEntryAilsePagerAdapter(Context mContext,
			ArrayList<DataentryImage> imagePathsList) {
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
			Log.e("Adapter", "file path :::: " + mImagePathsList.get(position));
			dataEntryRowAisleImage.setImageBitmap(BitmapFactory
					.decodeFile(mImagePathsList.get(position).getImagePath()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		((ViewPager) collection).addView(view, 0);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.mDataEntryInviteFriendsPopupLayout
						.setVisibility(View.GONE);
			}
		});
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