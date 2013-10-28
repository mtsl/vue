package com.lateralthoughts.vue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.Logging;

import java.util.ArrayList;

public class LandingStaggeredAdapter extends TrendingAislesGenericAdapter {
    private Context mContext;

    // public static boolean mIsLeftDataChanged = false;
    AisleContentBrowser.AisleContentClickListener listener;
    LinearLayout.LayoutParams mShowpieceParams, mShowpieceParamsDefault;
    BitmapLoaderUtils mBitmapLoaderUtils;

    public LandingStaggeredAdapter(Context context, ArrayList<AisleWindowContent> content) {
        super(context, content);
        mContext = context;
    }

    public LandingStaggeredAdapter(Context c,
                                           AisleContentBrowser.AisleContentClickListener listener,
                                           ArrayList<AisleWindowContent> content) {
        super(c, listener, content);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mContext = c;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        int count = mVueTrendingAislesDataModel.getAisleCount();
        Logging.e("PerfVue","**** AisleCount = " + count);
        return count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrendingAislesGenericAdapter.ViewHolder holder;
        if (null == convertView) {
            Logging.i("TrendingDataModel",
                    "DataObserver for List Refresh: Left getview if ");
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.staggered_row_item,
                    null);
            holder = new TrendingAislesGenericAdapter.ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView
                    .findViewById(R.id.aisle_content_flipper);
            holder.aisleDescriptor = (LinearLayout) convertView
                    .findViewById(R.id.aisle_descriptor);
            holder.profileThumbnail = (ImageView) holder.aisleDescriptor
                    .findViewById(R.id.profile_icon_descriptor);
            holder.aisleOwnersName = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_owner_name);
            holder.aisleContext = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_context);
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(holder);
        }
        holder = (TrendingAislesGenericAdapter.ViewHolder) convertView.getTag();
        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        holder.mWindowContent = (AisleWindowContent) getItem(position);
        int scrollIndex = 0;
        mLoader.getAisleContentIntoView(holder, scrollIndex, position,
                false, listener);
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
        return convertView;
    }

    @Override
    public void onAisleDataUpdated(int newCount) {
        Logging.i("TrendingDataModel",
                "DataObserver for List Refresh: Right List AisleUpdate Called ");
        notifyDataSetChanged();
    }
}