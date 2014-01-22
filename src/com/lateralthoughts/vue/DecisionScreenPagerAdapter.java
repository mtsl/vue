package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lateralthoughts.vue.utils.DataentryPageLoader;

public class DecisionScreenPagerAdapter extends PagerAdapter {
    
    private ArrayList<DataentryImage> mImagePathsList = null;
    private Context mContext = null;
    private DecisionScreen mDecisionScreen = null;
    private DataentryPageLoader mImageLoader;
    
    public DecisionScreenPagerAdapter(Context mContext,
            ArrayList<DataentryImage> imagePathsList) {
        this.mContext = mContext;
        this.mImagePathsList = imagePathsList;
        mImageLoader = DataentryPageLoader.getInstatnce();
    }
    
    @Override
    public int getCount() {
        if (mImagePathsList != null)
            return mImagePathsList.size();
        else
            return 0;
    }
    
    @Override
    public Object instantiateItem(View collection, final int position) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.decision_screen_pager_row, null);
        LinearLayout imageDeleteBtn = (LinearLayout) view
                .findViewById(R.id.image_delete_btn);
        ImageView dataEntryRowAisleImage = (ImageView) view
                .findViewById(R.id.dataentry_row_aisele_image);
        final ImageView deleteIcon = (ImageView) view
                .findViewById(R.id.staricon);
        ProgressBar aisleBgProgressBar = (ProgressBar) view
                .findViewById(R.id.aisle_bg_progressbar);
        aisleBgProgressBar.setVisibility(View.VISIBLE);
        dataEntryRowAisleImage.setVisibility(View.GONE);
        imageDeleteBtn.setVisibility(View.GONE);
        if (mImagePathsList.get(position).isCheckedFlag()) {
            deleteIcon.setImageResource(R.drawable.ic_action_selection);
        } else {
            deleteIcon.setImageResource(0);
        }
        try {
            dataEntryRowAisleImage.setTag(mImagePathsList.get(position)
                    .getResizedImagePath());
            if (mImagePathsList.get(position).isAddedToServerFlag()) {
                imageDeleteBtn.setClickable(true);
            } else {
                imageDeleteBtn.setClickable(false);
            }
            mImageLoader.DisplayImage(mImagePathsList.get(position)
                    .getOriginalImagePath(), mImagePathsList.get(position)
                    .getImageUrl(), mImagePathsList.get(position)
                    .getResizedImagePath(), dataEntryRowAisleImage,
                    aisleBgProgressBar, imageDeleteBtn,
                    mImagePathsList.get(position).isAddedToServerFlag(), false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ((ViewPager) collection).addView(view, 0);
        imageDeleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mImagePathsList.get(position).isCheckedFlag()) {
                    if (mDecisionScreen == null) {
                        mDecisionScreen = (DecisionScreen) mContext;
                    }
                    if (mDecisionScreen.mDeletedImagesPositionsList == null) {
                        mDecisionScreen.mDeletedImagesPositionsList = new ArrayList<Integer>();
                    }
                    mDecisionScreen.mDeletedImagesPositionsList.remove(Integer
                            .valueOf(position));
                    mDecisionScreen.hideDefaultActionbar();
                    if (mDecisionScreen.mDeletedImagesCount != 0) {
                        mDecisionScreen.mDeletedImagesCount -= 1;
                    }
                    if (mDecisionScreen.mDeletedImagesCount < 1) {
                        mDecisionScreen.showDefaultActionbar();
                    }
                    mImagePathsList.get(position).setCheckedFlag(false);
                    deleteIcon.setImageResource(0);
                } else {
                    if (mDecisionScreen == null) {
                        mDecisionScreen = (DecisionScreen) mContext;
                    }
                    if (!(mDecisionScreen.mDeletedImagesPositionsList != null && mDecisionScreen.mDeletedImagesPositionsList
                            .contains(position))) {
                        mDecisionScreen.hideDefaultActionbar();
                        if (mDecisionScreen.mDeletedImagesPositionsList == null) {
                            mDecisionScreen.mDeletedImagesPositionsList = new ArrayList<Integer>();
                        }
                        mDecisionScreen.mDeletedImagesPositionsList
                                .add(position);
                        mDecisionScreen.mDeletedImagesCount += 1;
                        mImagePathsList.get(position).setCheckedFlag(true);
                        deleteIcon
                                .setImageResource(R.drawable.ic_action_selection);
                    }
                }
            }
        });
        return view;
    }
    
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }
    
    @Override
    public void finishUpdate(View arg0) {
    }
    
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }
    
    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }
    
    @Override
    public Parcelable saveState() {
        return null;
    }
    
    @Override
    public void startUpdate(View arg0) {
    }
}