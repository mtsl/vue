package com.lateralthoughts.vue.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lateralthoughts.vue.CreateAisleSelectionActivity;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.utils.Utils;

public class ArcMenu extends RelativeLayout {
    public ArcLayout arcLayout;
    
    public RelativeLayout cameraLayout;
    public RelativeLayout galleryLayout;
    public RelativeLayout etsyLayout;
    public RelativeLayout browserLayout;
    public RelativeLayout moreLayout;
    public TextView etsyText;
    
    public ImageView hintView;
    
    public Context context;
    
    public ArcMenu(Context context) {
        super(context);
        init(context);
    }
    
    public ArcMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        applyAttrs(attrs);
    }
    
    private void init(Context context) {
        this.context = context;
        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.arc_menu, this);
        
        arcLayout = (ArcLayout) findViewById(R.id.item_layout);
        arcLayout.setVisibility(View.INVISIBLE);
        
        browserLayout = (RelativeLayout) findViewById(R.id.browser_layout);
        cameraLayout = (RelativeLayout) findViewById(R.id.camera_layout);
        galleryLayout = (RelativeLayout) findViewById(R.id.gallery_layout);
        moreLayout = (RelativeLayout) findViewById(R.id.more_layout);
        etsyLayout = (RelativeLayout) findViewById(R.id.etsy_layout);
        etsyText = (TextView) findViewById(R.id.etsy_text);
        
        if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
                && VueApplication.getInstance().mShoppingApplicationDetailsList
                        .size() > 0) {
            etsyText.setText(VueApplication.getInstance().mShoppingApplicationDetailsList
                    .get(0).getAppName());
        }
        
        browserLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                
                itemClickFunctionality(browserLayout, 2);
            }
        });
        cameraLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                itemClickFunctionality(cameraLayout, 0);
            }
        });
        galleryLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                itemClickFunctionality(galleryLayout, 4);
            }
        });
        moreLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) ArcMenu.this.context;
                createAisleSelectionActivity.moreClickFunctionality();
            }
        });
        etsyLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                itemClickFunctionality(etsyLayout, 1);
            }
        });
        
        hintView = (ImageView) findViewById(R.id.control_hint);
    }
    
    private void applyAttrs(AttributeSet attrs) {
        if (attrs != null) {
            float fromDegrees = ArcLayout.DEFAULT_FROM_DEGREES;
            float toDegrees = ArcLayout.DEFAULT_TO_DEGREES;
            arcLayout.setArc(fromDegrees, toDegrees);
            int newChildSize = ArcLayout.sChildSize;
            arcLayout.setChildSize(newChildSize);
        }
    }
    
    public void addItem(int position, LayoutParams lp, View item,
            OnClickListener listener) {
        arcLayout.addView(item, lp);
        item.setOnClickListener(getItemClickListener(position, listener));
    }
    
    private OnClickListener getItemClickListener(final int position,
            final OnClickListener listener) {
        return new OnClickListener() {
            
            @Override
            public void onClick(final View viewClicked) {
                // More
                if (position == 3) {
                    CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) context;
                    createAisleSelectionActivity.moreClickFunctionality();
                } else {
                    Animation animation = bindItemAnimation(viewClicked, true,
                            400);
                    animation.setAnimationListener(new AnimationListener() {
                        
                        @Override
                        public void onAnimationStart(Animation animation) {
                            
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            postDelayed(new Runnable() {
                                
                                @Override
                                public void run() {
                                    itemDidDisappear(position);
                                }
                            }, 0);
                        }
                    });
                    
                    final int itemCount = arcLayout.getChildCount();
                    for (int i = 0; i < itemCount; i++) {
                        View item = arcLayout.getChildAt(i);
                        if (viewClicked != item) {
                            bindItemAnimation(item, false, 300);
                        }
                    }
                    
                    arcLayout.invalidate();
                    hintView.startAnimation(createHintSwitchAnimation(true));
                    
                    if (listener != null) {
                        listener.onClick(viewClicked);
                    }
                }
            }
        };
    }
    
    private Animation bindItemAnimation(final View child,
            final boolean isClicked, final long duration) {
        Animation animation = createItemDisapperAnimation(duration, isClicked);
        child.setAnimation(animation);
        
        return animation;
    }
    
    private void itemClickFunctionality(View viewClicked, final int position) {
        
        Animation animation = bindItemAnimation(viewClicked, true, 400);
        animation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                postDelayed(new Runnable() {
                    
                    @Override
                    public void run() {
                        itemDidDisappear(position);
                    }
                }, 0);
            }
        });
        
        final int itemCount = arcLayout.getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View item = arcLayout.getChildAt(i);
            if (viewClicked != item) {
                bindItemAnimation(item, false, 300);
            }
        }
        arcLayout.invalidate();
        hintView.startAnimation(createHintSwitchAnimation(true));
    }
    
    private void itemDidDisappear(int position) {
        final int itemCount = arcLayout.getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View item = arcLayout.getChildAt(i);
            item.clearAnimation();
        }
        arcLayout.setVisibility(View.INVISIBLE);
        CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) context;
        switch (position) {
        // Camera
        case 0:
            createAisleSelectionActivity.cameraFunctionality();
            break;
        // Etsy
        case 1:
            if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
                    && VueApplication.getInstance().mShoppingApplicationDetailsList
                            .size() > 0) {
                createAisleSelectionActivity
                        .loadShoppingApplication(
                                VueApplication.getInstance().mShoppingApplicationDetailsList
                                        .get(0).getActivityName(),
                                VueApplication.getInstance().mShoppingApplicationDetailsList
                                        .get(0).getPackageName(),
                                VueApplication.getInstance().mShoppingApplicationDetailsList
                                        .get(0).getAppName());
            } else {
                if (Utils.appInstalledOrNot(
                        VueApplication.SHOPPINGAPP_PACKAGES_ARRAY[0], context)) {
                    createAisleSelectionActivity.loadShoppingApplication(
                            VueApplication.SHOPPINGAPP_ACTIVITIES_ARRAY[0],
                            VueApplication.SHOPPINGAPP_PACKAGES_ARRAY[0],
                            VueApplication.SHOPPINGAPP_NAMES_ARRAY[0]);
                } else {
                    createAisleSelectionActivity
                            .showAlertMessageForAppInstalation(
                                    VueApplication.SHOPPINGAPP_PACKAGES_ARRAY[0],
                                    VueApplication.SHOPPINGAPP_NAMES_ARRAY[0]);
                }
            }
            break;
        // Browser
        case 2:
            createAisleSelectionActivity.trackBrowserClickEvent();
            Utils.setLoadDataentryScreenFlag(context, true);
            // Load Browser...
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("http://www.google.com")));
            createAisleSelectionActivity.finish();
            break;
        // Gallery
        case 4:
            createAisleSelectionActivity.galleryFunctionality();
            break;
        default:
            createAisleSelectionActivity.finish();
            break;
        }
        arcLayout.expanded = !arcLayout.expanded;
        arcLayout.removeAllViews();
    }
    
    static Animation createItemDisapperAnimation(final long duration,
            final boolean isClicked) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f
                : 0.0f, 1.0f, isClicked ? 2.0f : 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
        
        animationSet.setDuration(duration);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setFillAfter(true);
        
        return animationSet;
    }
    
    public static Animation createHintSwitchAnimation(final boolean expanded) {
        Animation animation = new RotateAnimation(expanded ? 45 : 0,
                expanded ? 0 : 45, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setStartOffset(0);
        animation.setDuration(100);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setFillAfter(true);
        
        return animation;
    }
    
}
