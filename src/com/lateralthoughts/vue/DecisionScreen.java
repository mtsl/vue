package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lateralthoughts.vue.utils.FileCache;

public class DecisionScreen extends Activity {
    
    private View mVueDecisionScreenActionbarView;
    private FrameLayout mVueDecisionscreenCancel, mVueDecisionscreenChoose;
    private boolean mHideDefaultActionbar = false;
    private ViewPager mDecisionScreenViewpager = null;
    private ArrayList<DataentryImage> mAisleImagePathList = null;
    public int mDeletedImagesCount = 0;
    public ArrayList<Integer> mDeletedImagesPositionsList = null;
    private TextView mBookmarkCount, mLikeCount;
    private ImageView mLikeImg;
    private DecisionScreenPagerAdapter mDecisionScreenPagerAdapter = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_screen);
        initialize();
        getActionBar().setTitle(
                getResources().getString(R.string.decision_screen_title));
        mBookmarkCount = (TextView) findViewById(R.id.bookmark_count);
        mLikeCount = (TextView) findViewById(R.id.like_count);
        mLikeImg = (ImageView) findViewById(R.id.like_img);
        mVueDecisionScreenActionbarView = LayoutInflater.from(this).inflate(
                R.layout.vue_decisionscreen_custom_actionbar, null);
        mVueDecisionscreenCancel = (FrameLayout) mVueDecisionScreenActionbarView
                .findViewById(R.id.vue_decisionscreen_cancel);
        mVueDecisionscreenChoose = (FrameLayout) mVueDecisionScreenActionbarView
                .findViewById(R.id.vue_decisionscreen_choose);
        mVueDecisionscreenCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardChangesDialog();
            }
        });
        mVueDecisionscreenChoose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO waiting for Backend functionality.
            }
        });
        invalidateOptionsMenu();
        mDecisionScreenViewpager = (ViewPager) findViewById(R.id.decision_screen_viewpager);
        showDetailsScreenImagesInDataentryScreen();
        mDecisionScreenPagerAdapter = new DecisionScreenPagerAdapter(this,
                mAisleImagePathList);
        mDecisionScreenViewpager.setAdapter(mDecisionScreenPagerAdapter);
        mDecisionScreenViewpager
                .setOnPageChangeListener(new OnPageChangeListener() {
                    
                    @Override
                    public void onPageSelected(int position) {
                        if (mAisleImagePathList != null
                                && mAisleImagePathList.size() > 0) {
                            AisleImageDetails aisleImageDetails = VueTrendingAislesDataModel
                                    .getInstance(DecisionScreen.this)
                                    .getAisleImageForImageId(
                                            mAisleImagePathList.get(position)
                                                    .getImageId(),
                                            mAisleImagePathList.get(position)
                                                    .getAisleId(), false);
                            if (aisleImageDetails != null) {
                                mLikeCount
                                        .setText(aisleImageDetails.mLikesCount
                                                + "");
                                if (aisleImageDetails.mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
                                    mLikeImg.setImageResource(R.drawable.heart);
                                } else {
                                    mLikeImg.setImageResource(R.drawable.heart_dark);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }
                    
                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
    }
    
    private void initialize() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_bg));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            showDiscardChangesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDiscardChangesDialog();
        }
        return false;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setCustomView(null);
        getActionBar().setDisplayShowTitleEnabled(true);
        if (mHideDefaultActionbar) {
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setCustomView(mVueDecisionScreenActionbarView);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    private void showDiscardChangesDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText(getResources().getString(
                R.string.discard_dataentry_screen_changes));
        yesButton.setText("Yes");
        noButton.setText("No");
        yesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    public void showDefaultActionbar() {
        mHideDefaultActionbar = false;
        invalidateOptionsMenu();
    }
    
    public void hideDefaultActionbar() {
        mHideDefaultActionbar = true;
        invalidateOptionsMenu();
    }
    
    private void showDetailsScreenImagesInDataentryScreen() {
        ArrayList<AisleImageDetails> aisleImageDetailsList = null;
        AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
                .getInstance(this).getAisleAt(
                        VueApplication.getInstance().getClickedWindowID());
        if (aisleWindowContent != null) {
            mBookmarkCount
                    .setText(aisleWindowContent.getAisleContext().mBookmarkCount
                            + "");
            aisleImageDetailsList = aisleWindowContent.getImageList();
            if (aisleImageDetailsList != null
                    && aisleImageDetailsList.size() > 0) {
                mLikeCount.setText(aisleImageDetailsList.get(0).mLikesCount
                        + "");
                if (aisleImageDetailsList.get(0).mLikeDislikeStatus == VueConstants.IMG_LIKE_STATUS) {
                    mLikeImg.setImageResource(R.drawable.heart);
                } else {
                    mLikeImg.setImageResource(R.drawable.heart_dark);
                }
            }
        }
        if (aisleImageDetailsList != null && aisleImageDetailsList.size() > 0) {
            mAisleImagePathList = new ArrayList<DataentryImage>();
            FileCache fileCache = new FileCache(this);
            for (int i = 0; i < aisleImageDetailsList.size(); i++) {
                String originalImagePath = null;
                if (fileCache.getFile(aisleImageDetailsList.get(i).mImageUrl)
                        .exists()) {
                    originalImagePath = fileCache.getFile(
                            aisleImageDetailsList.get(i).mImageUrl).getPath();
                }
                String resizedImagePath = null;
                if (originalImagePath != null) {
                    resizedImagePath = fileCache.getVueAppResizedPictureFile(
                            String.valueOf(originalImagePath.hashCode()))
                            .getPath();
                } else {
                    resizedImagePath = fileCache
                            .getVueAppResizedPictureFile(
                                    String.valueOf(aisleImageDetailsList.get(i).mImageUrl
                                            .hashCode())).getPath();
                }
                DataentryImage aisleImage = new DataentryImage(
                        aisleImageDetailsList.get(i).mOwnerAisleId,
                        aisleImageDetailsList.get(i).mId, resizedImagePath,
                        originalImagePath,
                        aisleImageDetailsList.get(i).mImageUrl,
                        aisleImageDetailsList.get(i).mDetalsUrl,
                        aisleImageDetailsList.get(i).mAvailableWidth,
                        aisleImageDetailsList.get(i).mAvailableHeight,
                        aisleImageDetailsList.get(i).mStore, true);
                mAisleImagePathList.add(aisleImage);
            }
        }
    }
}
