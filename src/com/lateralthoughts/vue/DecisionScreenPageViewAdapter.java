package com.lateralthoughts.vue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.NetworkImageView;

public class DecisionScreenPageViewAdapter extends DecisionScreenAdapter {
    
    private Context mContext;
    private ViewHolder mHolder;
    private DecisionScreen mDecisionScreen = null;
    
    public DecisionScreenPageViewAdapter(Context context,
            AisleWindowContent aisleWindowContent) {
        super(context, aisleWindowContent);
        mContext = context;
    }
    
    @Override
    public int getCount() {
        int count = mAisleWindowContent.getImageList().size();
        return count;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (null == convertView) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(
                    R.layout.decision_screen_pager_row, null);
            mHolder = new ViewHolder();
            mHolder.decisionScreenImage = (NetworkImageView) convertView
                    .findViewById(R.id.decision_screen_image);
            mHolder.decisionScreenImageLayout = (RelativeLayout) convertView
                    .findViewById(R.id.decision_screen_image_layout);
            mHolder.decisionScreenSelectionImage = (ImageView) convertView
                    .findViewById(R.id.decision_screen_selection_image);
            convertView.setTag(mHolder);
        }
        mHolder = (ViewHolder) convertView.getTag();
        final AisleImageDetails aisleImage = mAisleWindowContent.getImageList()
                .get(position);
        if (aisleImage.mIsChoosen) {
            mHolder.decisionScreenSelectionImage.setVisibility(View.VISIBLE);
        } else {
            mHolder.decisionScreenSelectionImage.setVisibility(View.GONE);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, aisleImage.mTrendingImageHeight);
        mHolder.decisionScreenImageLayout.setLayoutParams(params);
        (mHolder.decisionScreenImage).setImageUrl(aisleImage.mImageUrl,
                VueApplication.getInstance().getImageCacheLoader(),
                aisleImage.mTrendingImageWidth,
                aisleImage.mTrendingImageHeight,
                NetworkImageView.BitmapProfile.ProfileLandingView);
        mHolder.decisionScreenImageLayout
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView image = (ImageView) v
                                .findViewById(R.id.decision_screen_selection_image);
                        if (mDecisionScreen == null) {
                            mDecisionScreen = (DecisionScreen) mContext;
                        }
                        if (aisleImage.mIsChoosen) {
                            mDecisionScreen.hideDefaultActionbar();
                            image.setVisibility(View.GONE);
                            if (mDecisionScreen.mChoosenImagesCount != 0) {
                                mDecisionScreen.mChoosenImagesCount -= 1;
                            }
                            if (mDecisionScreen.mChoosenImagesCount < 1) {
                                mDecisionScreen.showDefaultActionbar();
                            }
                        } else {
                            image.setVisibility(View.VISIBLE);
                            mDecisionScreen.hideDefaultActionbar();
                            mDecisionScreen.mChoosenImagesCount += 1;
                        }
                        aisleImage.mIsChoosen = !aisleImage.mIsChoosen;
                    }
                });
        mHolder.decisionScreenImageLayout
                .setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mDecisionScreen == null) {
                            mDecisionScreen = (DecisionScreen) mContext;
                        }
                        if (mDecisionScreen.mChoosenImagesCount == 0
                                && !aisleImage.mIsChoosen) {
                            mDecisionScreen.hideDefaultActionbar();
                            ImageView image = (ImageView) v
                                    .findViewById(R.id.decision_screen_selection_image);
                            image.setVisibility(View.VISIBLE);
                            mDecisionScreen.mChoosenImagesCount += 1;
                            aisleImage.mIsChoosen = !aisleImage.mIsChoosen;
                        }
                        return true;
                    }
                });
        return convertView;
    }
}