package com.lateralthoughts.vue;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.NetworkImageView;

public class DecisionScreenAdapter extends BaseAdapter implements
        IAisleDataObserver {
    
    public boolean mAnimationInProgress;
    protected boolean mIsScrolling;
    protected AisleWindowContent mAisleWindowContent;
    
    public DecisionScreenAdapter(Context c, AisleWindowContent content) {
        mIsScrolling = false;
        mAisleWindowContent = content;
    }
    
    public int getCount() {
        return mAisleWindowContent.getImageList().size();
    }
    
    public AisleImageDetails getItem(int position) {
        return mAisleWindowContent.getImageList().get(position);
    }
    
    public long getItemId(int position) {
        return 0;
    }
    
    public boolean hasStableIds() {
        return false;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }
    
    @Override
    public void onAisleDataUpdated(int newCount) {
        notifyDataSetChanged();
        
    }
    
    public void setIsScrolling(boolean isScrolling) {
        
    }
    
    static class ViewHolder {
        NetworkImageView decisionScreenImage;
        RelativeLayout decisionScreenImageLayout;
        ImageView decisionScreenSelectionImage;
    }
}
