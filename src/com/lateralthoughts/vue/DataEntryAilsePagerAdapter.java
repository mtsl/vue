package com.lateralthoughts.vue;

import java.util.ArrayList;
import com.lateralthoughts.vue.utils.DataentryPageLoader;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DataEntryAilsePagerAdapter extends PagerAdapter {

	private ArrayList<DataentryImage> mImagePathsList = null;
	private Context mContext = null;
	private DataEntryFragment mDataEntryFragment = null;
	private DataentryPageLoader mImageLoader;

	public DataEntryAilsePagerAdapter(Context mContext,
			ArrayList<DataentryImage> imagePathsList) {
		this.mContext = mContext;
		this.mImagePathsList = imagePathsList;
		mImageLoader = DataentryPageLoader.getInstatnce();
	}

	@Override
	public int getCount() {
		if (mImagePathsList != null)
			return mImagePathsList.size();
		else
			return 0;
	}

	@Override
	public Object instantiateItem(View collection, final int position) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dataentry_aisle_pager_row, null);
		LinearLayout imageDeleteBtn = (LinearLayout) view
				.findViewById(R.id.image_delete_btn);
		LinearLayout imageEditBtn = (LinearLayout) view
				.findViewById(R.id.image_edit_btn);
		ImageView dataEntryRowAisleImage = (ImageView) view
				.findViewById(R.id.dataentry_row_aisele_image);
		TextView touchToChangeImage = (TextView) view
				.findViewById(R.id.touchtochangeimage);
		ProgressBar aisleBgProgressBar = (ProgressBar) view
				.findViewById(R.id.aisle_bg_progressbar);
		aisleBgProgressBar.setVisibility(View.VISIBLE);
		dataEntryRowAisleImage.setVisibility(View.GONE);
		touchToChangeImage.setVisibility(View.GONE);
		imageDeleteBtn.setVisibility(View.GONE);
		imageEditBtn.setVisibility(View.GONE);
		try {
			if (mDataEntryFragment == null) {
				mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
						.getSupportFragmentManager().findFragmentById(
								R.id.create_aisles_view_fragment);
			}
			Log.e("Adapter", "file path :::: "
					+ mImagePathsList.get(position).getResizedImagePath());
			dataEntryRowAisleImage.setTag(mImagePathsList.get(position)
					.getResizedImagePath());
			if (mDataEntryFragment.isAisleAddedScreenVisible()) {
				touchToChangeImage.setClickable(false);
				dataEntryRowAisleImage.setClickable(false);
				imageDeleteBtn.setClickable(false);
				imageEditBtn.setClickable(false);
			} else {
				if (mImagePathsList.get(position).isAddedToServerFlag()) {
					touchToChangeImage.setClickable(false);
					dataEntryRowAisleImage.setClickable(false);
					imageDeleteBtn.setClickable(true);
					imageEditBtn.setClickable(true);
				} else {
					touchToChangeImage.setClickable(true);
					dataEntryRowAisleImage.setClickable(true);
					imageDeleteBtn.setClickable(false);
					imageEditBtn.setClickable(false);
				}
			}
			mImageLoader.DisplayImage(mImagePathsList.get(position)
					.getOriginalImagePath(), mImagePathsList.get(position)
					.getImageUrl(), mImagePathsList.get(position)
					.getResizedImagePath(), dataEntryRowAisleImage,
					aisleBgProgressBar, touchToChangeImage, mDataEntryFragment
							.isAisleAddedScreenVisible(), imageDeleteBtn,
					imageEditBtn, mImagePathsList.get(position)
							.isAddedToServerFlag());
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
				mDataEntryFragment.hideAllEditableTextboxes();
			}
		});
		touchToChangeImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.mDataEntryInviteFriendsPopupLayout
						.setVisibility(View.GONE);
				mDataEntryFragment.hideAllEditableTextboxes();
				if (!(mDataEntryFragment.isAisleAddedScreenVisible())) {
					if (!mImagePathsList.get(position).isAddedToServerFlag()) {
						mDataEntryFragment
								.touchToChangeImageClickFunctionality(position);
					}
				}
			}
		});
		dataEntryRowAisleImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.mDataEntryInviteFriendsPopupLayout
						.setVisibility(View.GONE);
				mDataEntryFragment.hideAllEditableTextboxes();
				if (!(mDataEntryFragment.isAisleAddedScreenVisible())) {
					if (!mImagePathsList.get(position).isAddedToServerFlag()) {
						/*mDataEntryFragment
								.touchToChangeImageClickFunctionality(position);*/
					}
				}
			}
		});
		imageDeleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.mDataEntryInviteFriendsPopupLayout
						.setVisibility(View.GONE);
				mDataEntryFragment.hideAllEditableTextboxes();
				mDataEntryFragment.deleteImage(position);
			}
		});
		imageEditBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) ((FragmentActivity) mContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.mDataEntryInviteFriendsPopupLayout
						.setVisibility(View.GONE);
				mDataEntryFragment.hideAllEditableTextboxes();
				mDataEntryFragment
						.showAlertMessageForBackendNotIntegrated("Image Update service at server side is pending.");
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