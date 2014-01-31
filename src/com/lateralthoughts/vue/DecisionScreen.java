package com.lateralthoughts.vue;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.origamilabs.library.views.StaggeredGridView;

public class DecisionScreen extends Activity {
    
    private View mVueDecisionScreenActionbarView;
    private FrameLayout mVueDecisionscreenCancel, mVueDecisionscreenChoose;
    private boolean mHideDefaultActionbar = false;
    private AisleWindowContent mAisleWindowContent;
    private StaggeredGridView mStaggeredView;
    private DecisionScreenPageViewAdapter mStaggeredAdapter;
    public int mChoosenImagesCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_screen);
        mStaggeredView = (StaggeredGridView) findViewById(R.id.decision_images_grid);
        int margin = getResources().getDimensionPixelSize(R.dimen.margin);
        mStaggeredView.setItemMargin(margin);
        mStaggeredView.setPadding(margin, 0, margin, 0);
        initialize();
        getActionBar().setTitle(
                getResources().getString(R.string.decision_screen_title));
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
        mAisleWindowContent = VueTrendingAislesDataModel.getInstance(this)
                .getAisleAt(VueApplication.getInstance().getClickedWindowID());
        if (mAisleWindowContent != null
                && mAisleWindowContent.getImageList() != null
                && mAisleWindowContent.getImageList().size() > 0) {
            mStaggeredAdapter = new DecisionScreenPageViewAdapter(this,
                    mAisleWindowContent);
            mStaggeredView.setAdapter(mStaggeredAdapter);
        }
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
    
}