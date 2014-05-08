package com.lateralthoughts.vue.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.NotificationAisle;

public class NotificationListAdapter extends BaseAdapter {
    ArrayList<NotificationAisle> notificationList;
    Context context;
    
    public NotificationListAdapter(Context context,
            ArrayList<NotificationAisle> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }
    
    @Override
    public int getCount() {
        try {
            return notificationList.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public Object getItem(int position) {
        return null;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.notification_popup_window_row, null);
            holder = new ViewHolder();
            holder.notificationDescription = (TextView) convertView
                    .findViewById(R.id.notification_description);
            holder.notificationText = (TextView) convertView
                    .findViewById(R.id.notification_text);
            holder.bookmarks = (TextView) convertView
                    .findViewById(R.id.bookmarks);
            holder.likes = (TextView) convertView.findViewById(R.id.likes);
            holder.comments = (TextView) convertView
                    .findViewById(R.id.comments);
            holder.userImage = (NetworkImageView) convertView
                    .findViewById(R.id.user_image);
            holder.overflow_listlayout_layout = (LinearLayout) convertView
                    .findViewById(R.id.overflow_listlayout_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bookmarks.setText(notificationList.get(position)
                .getBookmarkCount() + "");
        holder.likes
                .setText(notificationList.get(position).getLikeCount() + "");
        holder.comments.setText(notificationList.get(position)
                .getCommentsCount() + "");
        holder.notificationDescription.setText(notificationList.get(position)
                .getAisleTitle());
        holder.notificationText.setText(notificationList.get(position)
                .getNotificationText());
        ((NetworkImageView) holder.userImage).setImageUrl(
                notificationList.get(position).getUserProfileImageUrl(),
                VueApplication.getInstance().getImageCacheLoader(), 62, 72,
                NetworkImageView.BitmapProfile.ProfileDetailsView);
        if (notificationList.get(position).isReadStatus()) { // read
            holder.overflow_listlayout_layout.setBackgroundColor(Color
                    .parseColor("#C0C0C0"));
        } else { // unread
            holder.overflow_listlayout_layout.setBackgroundColor(Color
                    .parseColor("#FFFFFF"));
        }
        convertView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                try {
                    if (!notificationList.get(position).isReadStatus()) {
                        DataBaseManager.getInstance(context)
                                .updateNotificationAisleAsRead(
                                        notificationList.get(position).getId());
                        notificationList.get(position).setReadStatus(true);
                        notifyDataSetChanged();
                    }
                    notificationList.get(position).setReadStatus(true);
                    VueLandingPageActivity activity = (VueLandingPageActivity) context;
                    activity.loadDetailsScreenForNotificationClick(
                            notificationList.get(position).getAisleId(),
                            notificationList.get(position).getImageId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return convertView;
        
    }
    
    class ViewHolder {
        NetworkImageView userImage;
        LinearLayout overflow_listlayout_layout;
        TextView notificationDescription, bookmarks, likes, comments,
                notificationText;
    }
}
