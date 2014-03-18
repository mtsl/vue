package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class InviteFriendsAdapter extends BaseAdapter {
    
    private List<FbGPlusDetails> mItems;
    private Context mContext;
    private ImageLoader mImageLoader;
    private boolean mFromDataentryScreen;
    private MixpanelAPI mixpanel;
    private MixpanelAPI.People people;
    
    public InviteFriendsAdapter(Context context, List<FbGPlusDetails> objects,
            boolean fromDataentryScreen) {
        mixpanel = MixpanelAPI.getInstance(context,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(context,
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (storedVueUser != null
                && storedVueUser.getGooglePlusId().equals(
                        VueUser.DEFAULT_GOOGLEPLUS_ID)
                && storedVueUser.getFacebookId().equals(
                        VueUser.DEFAULT_FACEBOOK_ID)) {
            mixpanel.identify(String.valueOf(storedVueUser.getId()));
            people = mixpanel.getPeople();
            people.identify(String.valueOf(storedVueUser.getId()));
            SharedPreferences sharedPreferencesObj = VueApplication
                    .getInstance().getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
            people.setPushRegistrationId(sharedPreferencesObj.getString(
                    VueConstants.MIXPANEL_REGISTRATION_ID, null));
        }
        this.mContext = context;
        mItems = objects;
        mImageLoader = VueApplication.getInstance().getImageCacheLoader();
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
            holder.inviteFriendInviteButton = (RelativeLayout) convertView
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
                .getProfile_image_url(), mImageLoader);
        final int index = position;
        holder.inviteFriendInviteButton
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        JSONObject inviteFrndsProps = new JSONObject();
                        try {
                            inviteFrndsProps.put("Friend Name",
                                    mItems.get(index).getName());
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        // Google+ friends
                        if (mItems.get(index).getGoogleplusFriend() != null) {
                            try {
                                inviteFrndsProps.put("Friend Network",
                                        "GooglePlus");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                                try {
                                    inviteFrndsProps.put("Friend Network",
                                            "Facebook");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                        mixpanel.track("Friend Invited", inviteFrndsProps);
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
        RelativeLayout inviteFriendInviteButton;
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
