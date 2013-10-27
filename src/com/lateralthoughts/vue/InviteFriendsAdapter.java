package com.lateralthoughts.vue;

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

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsAdapter extends BaseAdapter {

	private List<FbGPlusDetails> mItems;
	private Context mContext;
	private ImageLoader mImageLoader;
	private boolean mFromDataentryScreen;

	public InviteFriendsAdapter(Context context, List<FbGPlusDetails> objects,
			boolean fromDataentryScreen) {
		this.mContext = context;
		mItems = objects;
		mImageLoader = new ImageLoader(VueApplication.getInstance()
				.getRequestQueue(),  BitmapLruCache.getInstance(mContext));
		mFromDataentryScreen = fromDataentryScreen;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		InviteFriendHolder holder;
		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.invite_friends, null);
			holder = new InviteFriendHolder();
			holder.friendPrifilePicture = (NetworkImageView) convertView
					.findViewById(R.id.invite_friends_imageView);
			holder.friendName = (TextView) convertView
					.findViewById(R.id.invite_friends_name);
			holder.inviteFriendInviteButton = (Button) convertView
					.findViewById(R.id.invite_friends_addFriends);
			convertView.setTag(holder);
		} else {
			holder = (InviteFriendHolder) convertView.getTag();
		}
		if (mFromDataentryScreen) {
			holder.inviteFriendInviteButton.setVisibility(View.GONE);
		} else {
			holder.inviteFriendInviteButton.setVisibility(View.VISIBLE);
		}
		holder.friendName.setText(mItems.get(position).getName());
		holder.friendPrifilePicture.setImageUrl(mItems.get(position)
				.getProfile_image_url(), mImageLoader, 0, 0, NetworkImageView.BitmapProfile.ProfileLandingView);
		final int index = position;
		holder.inviteFriendInviteButton
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// Google+ friends
						if (mItems.get(index).getGoogleplusFriend() != null) {
							Intent i = new Intent(mContext,
									VueLoginActivity.class);
							Bundle b = new Bundle();
							ArrayList<Integer> indexList = new ArrayList<Integer>();
							indexList.add(index);
							b.putIntegerArrayList(
									VueConstants.GOOGLEPLUS_FRIEND_INDEX,
									indexList);
							b.putBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE,
									true);
							i.putExtras(b);
							mContext.startActivity(i);
						}
						// Facebook friends
						else {
							if (mItems.get(index).getId() != null) {
								Intent i = new Intent(mContext,
										VueLoginActivity.class);
								Bundle b = new Bundle();
								b.putString(VueConstants.FB_FRIEND_ID, mItems
										.get(index).getId());
								b.putString(VueConstants.FB_FRIEND_NAME, mItems
										.get(index).getName());
								i.putExtras(b);
								mContext.startActivity(i);
							}
						}
					}
				});
		return convertView;
	}

	@Override
	public int getCount() {
		try {
			return mItems.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private class InviteFriendHolder {
		NetworkImageView friendPrifilePicture;
		TextView friendName;
		Button inviteFriendInviteButton;
	}

	@Override
	public Object getItem(int position) {
		try {
			return mItems.get(position);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
