package com.lateralthoughts.vue;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.lateralthoughts.vue.utils.GoogleImageBean;
import com.lateralthoughts.vue.utils.GoogleImageLoader;

public class OtherSourcesImageAdapter extends BaseAdapter {

	private Activity mActivity;
	private ArrayList<GoogleImageBean> mListImages;
	private LayoutInflater mInflater = null;
	private GoogleImageLoader mImageLoader;

	public OtherSourcesImageAdapter(Activity a,
			ArrayList<GoogleImageBean> listImages) {
		mActivity = a;
		this.mListImages = listImages;
		mInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageLoader = new GoogleImageLoader(mActivity.getApplicationContext());
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
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;
		if (convertView == null) {
			vi = mInflater.inflate(R.layout.othersource_grid__row, null);
			holder = new ViewHolder();
			holder.imgViewImage = (ImageView) vi
					.findViewById(R.id.other_sources_row_image);
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		holder.imgViewImage.setTag(mListImages.get(position).getOriginUrl());
		mImageLoader.DisplayImage(mListImages.get(position).getOriginUrl(),
				mActivity, holder.imgViewImage);
		return vi;
	}
}