package com.lateralthoughts.vue;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.lateralthoughts.vue.utils.DataentryPageLoader;

public class DataEntryAilsePagerAdapter extends PagerAdapter {

	private ArrayList<DataentryImage> mImagePathsList = null;
	private Context mContext = null;
	private DataEntryFragment mDataEntryFragment = null;
	private DataEntryActivity mDataEntryActivity = null;
	private DataentryPageLoader mImageLoader;
	private boolean mHideDeleteButton = false;

	public DataEntryAilsePagerAdapter(Context mContext,
			ArrayList<DataentryImage> imagePathsList, boolean hideDeleteButton) {
		this.mContext = mContext;
		this.mImagePathsList = imagePathsList;
		mImageLoader = DataentryPageLoader.getInstatnce();
		mHideDeleteButton = hideDeleteButton;
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
		ImageView dataEntryRowAisleImage = (ImageView) view
				.findViewById(R.id.dataentry_row_aisele_image);
		final ImageView deleteIcon = (ImageView) view
				.findViewById(R.id.staricon);
		ProgressBar aisleBgProgressBar = (ProgressBar) view
				.findViewById(R.id.aisle_bg_progressbar);
		aisleBgProgressBar.setVisibility(View.VISIBLE);
		dataEntryRowAisleImage.setVisibility(View.GONE);
		imageDeleteBtn.setVisibility(View.GONE);
		if (mImagePathsList.get(position).isCheckedFlag()) {
			deleteIcon.setImageResource(R.drawable.ic_action_selection);
		} else {
			deleteIcon.setImageResource(R.drawable.ic_action_delete);
		}
		try {
			if (mDataEntryFragment == null) {
				mDataEntryFragment = (DataEntryFragment) ((Activity) mContext)
						.getFragmentManager().findFragmentById(
								R.id.create_aisles_view_fragment);
			}
			dataEntryRowAisleImage.setTag(mImagePathsList.get(position)
					.getResizedImagePath());
			if (mImagePathsList.get(position).isAddedToServerFlag()) {
				imageDeleteBtn.setClickable(true);
			} else {
				imageDeleteBtn.setClickable(false);
			}
			mImageLoader.DisplayImage(mImagePathsList.get(position)
					.getOriginalImagePath(), mImagePathsList.get(position)
					.getImageUrl(), mImagePathsList.get(position)
					.getResizedImagePath(), dataEntryRowAisleImage,
					aisleBgProgressBar, imageDeleteBtn,
					mImagePathsList.get(position).isAddedToServerFlag(),
					mHideDeleteButton);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		((ViewPager) collection).addView(view, 0);
		imageDeleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mImagePathsList.get(position).isCheckedFlag()) {
					if (mDataEntryActivity.mDeletedImagesPositionsList == null) {
						mDataEntryActivity.mDeletedImagesPositionsList = new ArrayList<Integer>();
					}
					mDataEntryActivity.mDeletedImagesPositionsList
							.remove(Integer.valueOf(position));
					mDataEntryActivity.mVueDataentryKeyboardLayout
							.setVisibility(View.GONE);
					mDataEntryActivity.mVueDataentryKeyboardDone
							.setVisibility(View.GONE);
					mDataEntryActivity.mVueDataentryKeyboardCancel
							.setVisibility(View.GONE);
					mDataEntryActivity.mVueDataentryPostLayout
							.setVisibility(View.GONE);
					mDataEntryActivity.mVueDataentryDeleteLayout
							.setVisibility(View.VISIBLE);
					mDataEntryActivity.hideDefaultActionbar();
					if (mDataEntryActivity.mDeletedImagesCount != 0) {
						mDataEntryActivity.mDeletedImagesCount -= 1;
					}
					if (mDataEntryActivity.mDeletedImagesCount == 1) {
						mDataEntryActivity.mActionbarDeleteBtnTextview
								.setText("Delete 1 Image");
					} else if (mDataEntryActivity.mDeletedImagesCount < 1) {
						mDataEntryActivity.mVueDataentryKeyboardLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardDone
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardCancel
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryPostLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryDeleteLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.showDefaultActionbar();
					} else {
						mDataEntryActivity.mActionbarDeleteBtnTextview
								.setText("Delete "
										+ mDataEntryActivity.mDeletedImagesCount
										+ " Images");
					}
					mImagePathsList.get(position).setCheckedFlag(false);
					deleteIcon.setImageResource(R.drawable.ic_action_delete);
				} else {
					if (mDataEntryActivity == null) {
						mDataEntryActivity = (DataEntryActivity) mContext;
					}
					if (!(mDataEntryActivity.mDeletedImagesPositionsList != null && mDataEntryActivity.mDeletedImagesPositionsList
							.contains(position))) {
						mDataEntryActivity.mVueDataentryKeyboardLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardDone
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardCancel
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryPostLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryDeleteLayout
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.hideDefaultActionbar();
						if (mDataEntryActivity.mDeletedImagesPositionsList == null) {
							mDataEntryActivity.mDeletedImagesPositionsList = new ArrayList<Integer>();
						}
						mDataEntryActivity.mDeletedImagesPositionsList
								.add(position);
						mDataEntryActivity.mDeletedImagesCount += 1;
						if (mDataEntryActivity.mDeletedImagesCount == 1) {
							mDataEntryActivity.mActionbarDeleteBtnTextview
									.setText("Delete 1 Image");
						} else {
							mDataEntryActivity.mActionbarDeleteBtnTextview
									.setText("Delete "
											+ mDataEntryActivity.mDeletedImagesCount
											+ " Images");
						}
						mImagePathsList.get(position).setCheckedFlag(true);
						deleteIcon
								.setImageResource(R.drawable.ic_action_selection);
					}
				}
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