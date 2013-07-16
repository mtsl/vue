package com.lateralthoughts.vue;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FbGPlusDetails;

public class InviteFriendsAdapter extends BaseAdapter {

	List<FbGPlusDetails> items;
	Context context;
	private ImageLoader mImageLoader;

	public InviteFriendsAdapter(Context			
			context,
			List<FbGPlusDetails> objects) {
		this.context = context;
		items = objects;
		mImageLoader = new ImageLoader(VueApplication.getInstance()
				.getRequestQueue(), new BitmapLruCache(1024));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		InviteFriendHolder holder;
		if (convertView == null) {
			 LayoutInflater layoutInflator = LayoutInflater.from(context);
			convertView = layoutInflator.inflate(R.layout.invite_friends, null);
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
							Intent i = new Intent(context,
									VueLoginActivity.class);
							Bundle b = new Bundle();
							b.putInt(VueConstants.GOOGLEPLUS_FRIEND_INDEX,
									index);
							b.putBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE,
									true);
							i.putExtras(b);
							context.startActivity(i);
						}
						// Facebook friends
						else {
							if (items.get(index).getId() != null) {
								Intent i = new Intent(context,
										VueLoginActivity.class);
								Bundle b = new Bundle();
								b.putString(VueConstants.FB_FRIEND_ID, items
										.get(index).getId());
								b.putString(VueConstants.FB_FRIEND_NAME, items
										.get(index).getName());
								i.putExtras(b);
								context.startActivity(i);
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

	private class InviteFriendHolder {
		NetworkImageView friendprifilepic;
		TextView name;
		Button invite_friends_addFriends;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		try {
			return items.get(position);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}
