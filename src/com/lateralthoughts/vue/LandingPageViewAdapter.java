package com.lateralthoughts.vue;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.utils.Logging;


public class LandingPageViewAdapter extends TrendingAislesGenericAdapter {

    private AisleLoader mLoader;
    private Context mContext;

    public LandingPageViewAdapter(Context context,AisleContentClickListener clickListener) {
        super(context, clickListener, null);
        mContext = context;
        mLoader = AisleLoader.getInstance(context);
    }

    @Override
    public int getCount() {
        return mVueTrendingAislesDataModel.getAisleCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            Logging.i("TrendingDataModel",
                    "DataObserver for List Refresh: Left getview if ");
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.staggered_row_item,
                    null);
            holder = new ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView
                    .findViewById(R.id.aisle_content_flipper);
            holder.aisleDescriptor = (LinearLayout) convertView
                    .findViewById(R.id.aisle_descriptor);
            holder.profileThumbnail = (NetworkImageView) holder.aisleDescriptor
                    .findViewById(R.id.profile_icon_descriptor);
            holder.aisleOwnersName = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_owner_name);
            holder.aisleContext = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_context);
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        holder.mWindowContent = (AisleWindowContent) getItem(position);
        int scrollIndex = 0;
        mLoader.getAisleContentIntoView(holder, scrollIndex, position,
                false, mClickListener,"left",null);
        AisleContext context = holder.mWindowContent.getAisleContext();
        String mVueusername = null;
        if (context.mFirstName != null && context.mLastName != null) {
            mVueusername = context.mFirstName + " " + context.mLastName;
        } else if (context.mFirstName != null) {
            if (context.mFirstName.equals("Anonymous")) {
                mVueusername = VueApplication.getInstance().getmUserInitials();
            } else {
                mVueusername = context.mFirstName;
            }
        } else if (context.mLastName != null) {
            mVueusername = context.mLastName;
        }
        if (mVueusername != null
                && mVueusername.trim().equalsIgnoreCase("Anonymous")) {
            if (VueApplication.getInstance().getmUserInitials() != null) {
                mVueusername = VueApplication.getInstance().getmUserInitials();
            }
        }
        if (mVueusername != null && mVueusername.trim().length() > 0) {
            holder.aisleOwnersName.setText(mVueusername);
        } else {
            holder.aisleOwnersName.setText("Anonymous");
        }
        // Title (lookingfor and occasion)
        String occasion = null;
        String lookingFor = null;
        if (context.mOccasion != null && context.mOccasion.length() > 1) {
            occasion = context.mOccasion;
        }
        if (context.mLookingForItem != null
                && context.mLookingForItem.length() > 1) {
            lookingFor = context.mLookingForItem;
        }
        if (occasion != null && occasion.length() > 1) {
            occasion = occasion.toLowerCase();
            occasion = Character.toString(occasion.charAt(0)).toUpperCase()
                    + occasion.substring(1);
        }
        if (lookingFor != null && lookingFor.length() > 1) {
            lookingFor = lookingFor.toLowerCase();
            lookingFor = Character.toString(lookingFor.charAt(0)).toUpperCase()
                    + lookingFor.substring(1);
        }
        String title = "";
        if (occasion != null) {
            title = occasion + " : ";
        }
        if (lookingFor != null) {
            title = title + lookingFor;
        }
        holder.aisleContext.setText(title);
        //if(mVueTrendingAislesDataModel.getNetworkHandler().getmOffset() - position < 10){
            mVueTrendingAislesDataModel.getNetworkHandler().requestMoreAisle(true, mContext.getString(R.string.trending));
        //}
        return convertView;
    }

    private class AisleBrowserScrollListener implements AisleContentBrowser.AilseLeftListLisner {

        @Override
        public void onSwipe(boolean mustShow, String aisleId, boolean sameLikes) {
            mHasToShow = mustShow;
            mShowStarAisle = aisleId;
            mHasSameLikes = sameLikes;
            notifyDataSetChanged();

        }
    }
    private boolean mHasToShow = true;
    private boolean mHasSameLikes = false;
    private String mShowStarAisle = " ";
}