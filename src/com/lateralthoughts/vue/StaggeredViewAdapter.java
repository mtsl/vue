package com.lateralthoughts.vue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.ContentLoader;
import android.widget.ArrayAdapter;

public class StaggeredViewAdapter extends ArrayAdapter<String> {

	private ContentLoader mLoader;

	public StaggeredViewAdapter(Context context, int textViewResourceId, String[] objects) {
		super(context, textViewResourceId, objects);
		mLoader = new ContentLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(getContext());
			convertView = layoutInflator.inflate(R.layout.staggered_row_item, null);
			holder = new ViewHolder();
			//holder.imageView = (ScaleImageView) convertView .findViewById(R.id.imageView1);
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();

		mLoader.displayImage(getItem(position), holder.imageView);

		return convertView;
	}

	static class ViewHolder {
		ScaleImageView imageView;
	}
}