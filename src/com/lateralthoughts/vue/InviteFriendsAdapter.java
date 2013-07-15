package com.lateralthoughts.vue;

import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FbGPlusDetails;

public class InviteFriendsAdapter extends ArrayAdapter<FbGPlusDetails> {

	List<FbGPlusDetails> items;
	Activity activity;
	private ImageLoader mImageLoader;

	public InviteFriendsAdapter(Activity activity, int textViewResourceId,
			List<FbGPlusDetails> objects) {
		super(activity, textViewResourceId, objects);
		this.activity = activity;
		items = objects;
		mImageLoader = new ImageLoader(VueApplication.getInstance()
				.getRequestQueue(), new BitmapLruCache(1024));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		InviteFriendHolder holder;
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.invite_friends, null);
			holder = new InviteFriendHolder();
			holder.friendprifilepic = (NetworkImageView) convertView
					.findViewById(R.id.invite_friends_imageView);
			holder.name = (TextView) convertView
					.findViewById(R.id.invite_friends_name);
			holder.invite_friends_addFriends = (Button) convertView
					.findViewById(R.id.invite_friends_addFriends);
			convertView.setTag(holder);
		} else {
			holder = (InviteFriendHolder) convertView.getTag();
		}
		holder.name.setText(items.get(position).getName());
		holder.friendprifilepic.setImageUrl(items.get(position)
				.getProfile_image_url(), mImageLoader);
		final int index = position;
		holder.invite_friends_addFriends
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// Google+ friends
						if (items.get(index).getGoogleplusFriend() != null) {
							Intent i = new Intent(activity,
									VueLoginActivity.class);
							Bundle b = new Bundle();
							b.putInt(VueConstants.GOOGLEPLUS_FRIEND_INDEX,
									index);
							b.putBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE,
									true);
							i.putExtras(b);
							activity.startActivity(i);
						}
						// Facebook friends
						else {
							if (items.get(index).getId() != null) {
								Intent i = new Intent(activity,
										VueLoginActivity.class);
								Bundle b = new Bundle();
								b.putString(VueConstants.FB_FRIEND_ID, items
										.get(index).getId());
								b.putString(VueConstants.FB_FRIEND_NAME, items
										.get(index).getName());
								i.putExtras(b);
								activity.startActivity(i);
							}
						}
					}
				});
		return convertView;
	}

	@Override
	public int getCount() {
		try {
			return items.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static class InviteFriendHolder {
		NetworkImageView friendprifilepic;
		TextView name;
		Button invite_friends_addFriends;
	}
}
