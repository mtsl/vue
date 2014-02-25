package com.lateralthoughts.vue;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;

public class LandingPageViewAdapter extends TrendingAislesGenericAdapter {
    
    private AisleLoader mLoader;
    private Context mContext;
    AisleContentClickListener mClickListener;
    private static final String AISLE_STAGE_FOUR = "completed";
    
    public LandingPageViewAdapter(Context context,
            AisleContentClickListener clickListener) {
        super(context, clickListener, null);
        mContext = context;
        mLoader = AisleLoader.getInstance(context);
        mClickListener = clickListener;
    }
    
    @Override
    public int getCount() {
        int count = mVueTrendingAislesDataModel.getAisleCount();
        if (count == 0) {
            mClickListener.showProgressBar(count);
        } else {
            mClickListener.hideProgressBar(count);
        }
        return count;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int actualPosition = position;
        if (null == convertView) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.staggered_row_item,
                    null);
            holder = new ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView
                    .findViewById(R.id.aisle_content_flipper);
            holder.starIcon = (ImageView) convertView
                    .findViewById(R.id.staricon);
            holder.viewBar = (View) convertView.findViewById(R.id.greenbar);
            holder.likeCount = (TextView) convertView
                    .findViewById(R.id.like_count);
            holder.bookMarkCount = (TextView) convertView
                    .findViewById(R.id.bookmark_count);
            holder.share_count = (TextView) convertView
                    .findViewById(R.id.share_count);
            holder.shareImage = (ImageView) convertView
                    .findViewById(R.id.shareImage);
            holder.bookmarkImageView = (ImageView) convertView
                    .findViewById(R.id.bookmarkImage);
            holder.socialCard = (RelativeLayout) convertView
                    .findViewById(R.id.social_card);
            holder.aisleDescriptor = (LinearLayout) convertView
                    .findViewById(R.id.aisle_descriptor);
            holder.profileThumbnail = (NetworkImageView) holder.aisleDescriptor
                    .findViewById(R.id.profile_icon_descriptor);
            holder.aisleOwnersName = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_owner_name);
            holder.aisleContext = (TextView) holder.aisleDescriptor
                    .findViewById(R.id.descriptor_aisle_context);
            holder.aisleselectlay = (LinearLayout) convertView
                    .findViewById(R.id.aisleselectlay);
            holder.no_image_lay = (LinearLayout) convertView
                    .findViewById(R.id.no_image_lay);
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
       
        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        holder.mWindowContent = (AisleWindowContent) getItem(position);
        int scrollIndex = 0;
        if (VueLandingPageActivity.mOtherSourceImagePath != null) {
            if (VueLandingPageActivity.mOtherSourceAddImageAisleId != null
                    && VueLandingPageActivity.mOtherSourceAddImageAisleId
                            .equals(holder.mWindowContent.getAisleId())) {
                holder.aisleselectlay.setVisibility(View.VISIBLE);
            } else {
                holder.aisleselectlay.setVisibility(View.GONE);
            }
        } else {
            holder.aisleselectlay.setVisibility(View.GONE);
        }
       
        AisleContext context = holder.mWindowContent.getAisleContext();
        if (context.mIsEmptyAisle) {
            final AisleWindowContent mWindowContent = holder.mWindowContent;
            holder.no_image_lay.setVisibility(View.VISIBLE);
            holder.aisleContentBrowser.setVisibility(View.GONE);
            holder.starIcon.setVisibility(View.GONE);
            holder.no_image_lay.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    VueApplication.getInstance()
                            .setPendingAisle(mWindowContent);
                    Intent intent = new Intent();
                    intent.setClass(VueApplication.getInstance(),
                            AisleDetailsViewActivity.class);
                    VueApplication.getInstance().setClickedWindowID(
                            mWindowContent.getAisleContext().mAisleId);
                    VueApplication.getInstance().setClickedWindowCount(0);
                    VueApplication.getInstance().setmAisleImgCurrentPos(0);
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder.no_image_lay.setVisibility(View.GONE);
            holder.aisleContentBrowser.setVisibility(View.VISIBLE);
            holder.starIcon.setVisibility(View.VISIBLE);
            mLoader.getAisleContentIntoView(holder, scrollIndex,
                    actualPosition, false, mClickListener, "left",
                    holder.starIcon, holder.socialCard);
        }
       
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
        int cardWidh = VueApplication.getInstance().getVueDetailsCardWidth() / 2;
        if (holder.mWindowContent.mAisleCureentStage
                .equals(VueConstants.AISLE_STATGE_ONE)) {
            cardWidh = cardWidh * 25 / 100;
        } else if (holder.mWindowContent.mAisleCureentStage
                .equals(VueConstants.AISLE_STAGE_TWO)) {
            cardWidh = cardWidh * 50 / 100;
        } else if (holder.mWindowContent.mAisleCureentStage
                .equals(VueConstants.AISLE_STAGE_THREE)) {
            cardWidh = cardWidh * 75 / 100;
        }
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                cardWidh, VueApplication.getInstance().getPixel(2));
        holder.viewBar.setLayoutParams(layoutParams);
        holder.bookMarkCount.setText(String.valueOf(holder.mWindowContent
                .getAisleContext().mBookmarkCount));
        if (holder.mWindowContent.getWindowBookmarkIndicator()) {
            holder.bookmarkImageView.setImageResource(R.drawable.save);
        } else {
            holder.bookmarkImageView
                    .setImageResource(R.drawable.save_dark_small);
        }
        if (holder.mWindowContent.ismShareIndicator()) {
            holder.shareImage.setImageResource(R.drawable.share);
        } else {
            holder.shareImage.setImageResource(R.drawable.share_gray);
        }
        holder.share_count.setText(String.valueOf(holder.mWindowContent
                .getAisleContext().mShareCount));
        return convertView;
    }
}