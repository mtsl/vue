/**
 * 
 * @author Vinodh Sundararajan
 * One of the more complex parts of the adapter is the mechanism for interacting
 * with content gateway and keeping track of aisle window content.
 * An AisleWindowContent is made up of a bunch of images all belonging to one
 * category but contributed by several different users.
 * We are going to be dealing with humongous amounts of data so need to careful
 * about this.
 * As soon as the adapter goes live we will initiate a request to get the top trending
 * aisles. But we have no idea how many are top trending; meaning, if there are hundreds of
 * them it will take forever for the data to come back We will therefore use the limit
 * and offset parameters to get data in smaller chunks.
 * The adapter keep an array of AisleWindowContent each of which contains array of content
 * When a new item comes in, it the category has already been created we add its content
 * to an existing AisleWindowContent. Otherwise, create a new one.
 * 
 * Relationship between AisleWindowContent and grid item: Each AisleWindowContent object will
 * take up one spot in the StaggeredGridView. This spot consists of a ViewFlipper so there will
 * many many images. This spot also contains a "meta" field with information relating to the person
 * who added the item, thumbnail image of the person, context and occasion for the category.
 *
 */

package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AilseRighttRightLisner;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

public class TrendingAislesRightColumnAdapter extends
        TrendingAislesGenericAdapter {
    private Context mContext;
    
    private final String TAG = "TrendingAislesRightColumnAdapter";
    
    private AisleLoader mLoader;
    
    AisleContentClickListener listener;
    LinearLayout.LayoutParams mShowpieceParams, mShowpieceParamsDefault;
    BitmapLoaderUtils mBitmapLoaderUtils;
    private boolean mHasToShow = true;
    private String mShowStarAisle = " ";
    private boolean mHasSameLikes = false;
    
    public TrendingAislesRightColumnAdapter(Context c,
            ArrayList<AisleWindowContent> content) {
        super(c, content);
        mContext = c;
        
        mLoader = AisleLoader.getInstance(mContext);
    }
    
    public TrendingAislesRightColumnAdapter(Context c,
            AisleContentClickListener listener,
            ArrayList<AisleWindowContent> content) {
        super(c, listener, content);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mContext = c;
        mLoader = AisleLoader.getInstance(mContext);
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return mVueTrendingAislesDataModel.getAisleCount() / 2;
    }
    
    @Override
    public AisleWindowContent getItem(int position) {
        int positionFactor = 2;
        int actualPosition = 1;
        if (0 != position)
            actualPosition = (positionFactor * position) + actualPosition;
        
        return mVueTrendingAislesDataModel.getAisleAt(actualPosition);
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;
        
        if (null == convertView) {
            
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.staggered_row_item,
                    null);
            holder = new ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView
                    .findViewById(R.id.aisle_content_flipper);
            holder.starIcon = (ImageView) convertView
                    .findViewById(R.id.staricon);
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
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            holder.aisleContentBrowser
                    .setAilseRighttListLisner(new RightList());
            convertView.setTag(holder);
            ;
        }
        holder = (ViewHolder) convertView.getTag();
        holder.mWindowContent = (AisleWindowContent) getItem(position);
        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        int scrollIndex = 0;
        if (mHasToShow) {
            if (holder.mWindowContent != null
                    && mShowStarAisle
                            .equals(holder.mWindowContent.getAisleId())) {
                if (mHasSameLikes) {
                    holder.starIcon.setImageResource(R.drawable.vue_star_light);
                } else {
                    holder.starIcon.setImageResource(R.drawable.vue_star_theme);
                }
                holder.starIcon.setVisibility(View.VISIBLE);
                
            }
        } else {
            if (holder.mWindowContent != null
                    && mShowStarAisle
                            .equals(holder.mWindowContent.getAisleId()))
                holder.starIcon.setVisibility(View.GONE);
        }
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
        mLoader.getAisleContentIntoView(holder, scrollIndex, position, false,
                listener, "RightAdapter", holder.starIcon);
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
        notifyDataSetChanged();
    }
    
    private int calculateActualPosition(int position) {
        int positionFactor = 2;
        int actualPosition = 1;
        if (0 != position)
            actualPosition = (positionFactor * position) + actualPosition;
        return actualPosition;
    }
    
    private class RightList implements AilseRighttRightLisner {
        
        @Override
        public void onSwipe(boolean hasToShwo, String aisleId, boolean sameLikes) {
            mHasToShow = hasToShwo;
            mShowStarAisle = aisleId;
            mHasSameLikes = sameLikes;
            notifyDataSetChanged();
            
        }
        
    }
}