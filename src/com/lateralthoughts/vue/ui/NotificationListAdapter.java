package com.lateralthoughts.vue.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    
    public NotificationAisle removeItem(int position) {
        NotificationAisle notificatinAisle = null;
        if(notificationList != null && notificationList.size() > 0){
            notificatinAisle =   notificationList.remove(position);
        }
        if (notificationList.size() == 0) {
            addTempItem();
        }
        notifyDataSetChanged();
        return notificatinAisle;
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
            holder.bookmarkId = (ImageView) convertView
                    .findViewById(R.id.bookmark_id);
            holder.commentId = (ImageView) convertView
                    .findViewById(R.id.comment_id);
            holder.likeId = (ImageView) convertView.findViewById(R.id.like_id);
            holder.bottom_lay_id = (RelativeLayout) convertView
                    .findViewById(R.id.bottom_lay_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (notificationList.size() == 1
                && notificationList.get(position).ismEmptyNotification() == true) {
            holder.notificationText.setText(notificationList.get(position)
                    .getNotificationText());
            holder.bookmarks.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.bookmarkId.setVisibility(View.GONE);
            holder.commentId.setVisibility(View.GONE);
            holder.likeId.setVisibility(View.GONE);
            holder.bottom_lay_id.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            int pixel = VueApplication.getInstance().getPixel(4);
            holder.notificationText.setMaxLines(4);
            params.setMargins(pixel, pixel, pixel, pixel);
            holder.notificationText.setSingleLine(false);
            holder.notificationText.setLayoutParams(params);
            ((NetworkImageView) holder.userImage).setVisibility(View.GONE);
        } else {
            if (notificationList
                    .get(position)
                    .getNotificationText()
                    .equals(context.getResources().getString(
                            R.string.uploading_aisle_mesg))) {
                holder.bookmarks.setVisibility(View.GONE);
                holder.likes.setVisibility(View.GONE);
                holder.bookmarkId.setVisibility(View.GONE);
                holder.commentId.setVisibility(View.GONE);
                holder.likeId.setVisibility(View.GONE);
                holder.bottom_lay_id.setVisibility(View.GONE);
            } else {
                holder.bookmarks.setVisibility(View.VISIBLE);
                holder.likes.setVisibility(View.VISIBLE);
                holder.bookmarkId.setVisibility(View.VISIBLE);
                holder.commentId.setVisibility(View.VISIBLE);
                holder.likeId.setVisibility(View.VISIBLE);
                holder.bottom_lay_id.setVisibility(View.VISIBLE);
            }
            holder.bookmarks.setText(notificationList.get(position)
                    .getBookmarkCount() + "");
            holder.likes.setText(notificationList.get(position).getLikeCount()
                    + "");
            holder.comments.setText(notificationList.get(position)
                    .getCommentsCount() + "");
            holder.notificationDescription.setText(notificationList.get(
                    position).getAisleTitle());
            holder.notificationText.setText(notificationList.get(position)
                    .getNotificationText());
            ((NetworkImageView) holder.userImage).setImageUrl(notificationList
                    .get(position).getUserProfileImageUrl(), VueApplication
                    .getInstance().getImageCacheLoader(), 62, 72,
                    NetworkImageView.BitmapProfile.ProfileDetailsView);
            if (notificationList.get(position).isReadStatus()) { // read
                holder.overflow_listlayout_layout.setBackgroundColor(Color
                        .parseColor("#C0C0C0"));
            } else { // unread
                holder.overflow_listlayout_layout.setBackgroundColor(Color
                        .parseColor("#FFFFFF"));
            }
        }
        return convertView;
    }
    
    public int getSerialNumber(int position) {
        if (notificationList != null && notificationList.size() > 0) {
            return notificationList.get(position).getId();
        }
        return -1;
    }
    
    public void loadScreenForNotificationAisle(int position) {
        if (!notificationList
                .get(position)
                .getNotificationText()
                .equals(context.getResources().getString(
                        R.string.uploading_aisle_mesg))) {
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
                activity.loadDetailsScreenForNotificationClick(notificationList
                        .get(position).getAisleId(),
                        notificationList.get(position).getImageId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    class ViewHolder {
        NetworkImageView userImage;
        LinearLayout overflow_listlayout_layout;
        TextView notificationDescription, bookmarks, likes, comments,
                notificationText;
        ImageView likeId, bookmarkId, commentId;
        RelativeLayout bottom_lay_id;
    }
    
    private void addTempItem() {
        NotificationAisle notificationAisle = new NotificationAisle();
        notificationAisle.setNotificationText(context
                .getString(R.string.no_notification_text));
        notificationAisle.setReadStatus(false);
        notificationAisle.setAisleId("");
        notificationAisle.setmEmptyNotification(true);
        notificationList.add(notificationAisle);
    }
}
