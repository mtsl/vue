package com.lateralthoughts.vue;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.OtherSourceImageLoader;

public class OtherSourcesImageAdapter extends BaseAdapter {

	private Activity mActivity;
	private ArrayList<OtherSourceImageDetails> mListImages;
	private LayoutInflater mInflater = null;
	private OtherSourceImageLoader mImageLoader;

	public OtherSourcesImageAdapter(Activity a,
			ArrayList<OtherSourceImageDetails> listImages) {
		mActivity = a;
		this.mListImages = listImages;
		mInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageLoader = OtherSourceImageLoader.getInstatnce();
	}

	public int getCount() {
		return mListImages.size();
	}

	public Object getItem(int position) {
		return mListImages.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		public ImageView imgViewImage;
		public ProgressBar othersourceAisleBgProgressbar;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;
		if (convertView == null) {
			vi = mInflater.inflate(R.layout.othersource_grid__row, null);
			holder = new ViewHolder();
			holder.imgViewImage = (ImageView) vi
					.findViewById(R.id.other_sources_row_image);
			holder.othersourceAisleBgProgressbar = (ProgressBar) vi
					.findViewById(R.id.othersource_aisle_bg_progressbar);
			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}
		if (mListImages.get(position).getImageUri() != null) {
			holder.othersourceAisleBgProgressbar.setVisibility(View.GONE);
			holder.imgViewImage.setVisibility(View.VISIBLE);
			holder.imgViewImage.setImageBitmap(BitmapFactory
					.decodeFile(mListImages.get(position).getImageUri()
							.getPath()));
		} else {
			holder.othersourceAisleBgProgressbar.setVisibility(View.VISIBLE);
			holder.imgViewImage.setVisibility(View.GONE);
			holder.imgViewImage
					.setTag(mListImages.get(position).getOriginUrl());
			mImageLoader.DisplayImage(mListImages.get(position).getOriginUrl(),
					holder.imgViewImage, holder.othersourceAisleBgProgressbar);
		}
		return vi;
	}
}