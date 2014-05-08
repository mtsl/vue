package com.lateralthoughts.vue.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lateralthoughts.vue.R;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        
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
            holder.userImage = (ImageView) convertView
                    .findViewById(R.id.user_image);
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
        return convertView;
        
    }
    
    class ViewHolder {
        ImageView userImage;
        TextView notificationDescription, bookmarks, likes, comments,
                notificationText;
    }
}
