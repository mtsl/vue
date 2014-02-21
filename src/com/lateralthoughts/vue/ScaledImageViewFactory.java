package com.lateralthoughts.vue;

//java utils
import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lateralthoughts.vue.ui.ScaleImageView;

//Factory class for ScaledImageView to manage memory more efficiently.
//the idea is to create a pool of pre-allocated ScaledImageView objects
//and use them to load up different bitmaps.
//This is particularly handy because the SGV for landing page is powered
//by the adapter which is going keep needing ScaledImageView objects. We
//don't want to be allocating these objects on the fly because that means
//more garbage collection and bad performance.

//the factory itself will be initialized during application startup. We will
//allocate a pre-determined number of ScaledImageViewObjects at that time.
//If more objects are needed we allocate them in chunks rather than create
//one at a time.

public class ScaledImageViewFactory {
    
    private static ScaledImageViewFactory sImageViewFactory = null;
    
    private ArrayList<ScaleImageView> mAvailableObjects = null;
    private ArrayList<ScaleImageView> mObjectsInUse = null;
    
    private final int POOL_SIZE = 500;
    private final int POOL_STEP_SIZE = 200;
    private final int POOL_MAX_SIZE = 2000;
    
    private Context mContext;
    public Drawable mEvenColumnBackground;
    public Drawable mOddColumnBackground;
    
    private ScaledImageViewFactory(Context context) {
        mAvailableObjects = new ArrayList<ScaleImageView>();
        mObjectsInUse = new ArrayList<ScaleImageView>();
        
        // TODO: assert if context is null
        mContext = context;
        
        // at construction time - lets go ahead and create the pool
        // of scaleimageview objects
        for (int i = 0; i < POOL_SIZE; i++) {
            mAvailableObjects.add(new ScaleImageView(mContext));
        }
        
        mOddColumnBackground = mContext.getResources().getDrawable(
                R.drawable.white_background_odd);
        mEvenColumnBackground = mContext.getResources().getDrawable(
                R.drawable.white_background_even);
    }
    
    public static ScaledImageViewFactory getInstance(Context context) {
        if (null == sImageViewFactory) {
            sImageViewFactory = new ScaledImageViewFactory(context);
        }
        return sImageViewFactory;
    }
    
    // TODO: I think this method should throw an exception if exceed some kind
    // of absolute maximum limit for number of ScaledImageView objects.
    public ScaleImageView getEmptyImageView() {
        ScaleImageView imageView = null;
        
        if (mAvailableObjects.isEmpty()) {
            expandPoolOrDie();
        }
        
        synchronized (this) {
            if (!mAvailableObjects.isEmpty()) {
                imageView = mAvailableObjects
                        .remove(mAvailableObjects.size() - 1);
                mObjectsInUse.add(imageView);
            }
        }
        return imageView;
    }
    
    public ScaleImageView getPreconfiguredImageView(int position) {
        ScaleImageView imageView = getEmptyImageView();
        Drawable background = null;
        
        if (0 != position % 2) {
            background = mOddColumnBackground;
        } else {
            // background =mOddColumnBackground;
            background = mEvenColumnBackground;
        }
        
        imageView.setImageDrawable(background);
        
        return imageView;
    }
    
    public void returnUsedImageView(ScaleImageView imageView) {
        int index = -1;
        ScaleImageView view;
        if (null == imageView) {
            return;
        }
        index = mObjectsInUse.indexOf(imageView);
        if (-1 == index) {
            return;
        }
        
        synchronized (this) {
            view = mObjectsInUse.remove(index);
            view.setImageBitmap(null);
            view.setImageDrawable(null);
            mAvailableObjects.add(view);
            // TODO: we have a way to expand the pool once the initial objects
            // get used up - should
            // we have an equivalent to free up some of the objects?
        }
    }
    
    // internal utility functions
    private void expandPoolOrDie() {
        if (mObjectsInUse.size() >= POOL_MAX_SIZE) {
            // TODO: assert & die here!
            // throw new
            // java.lang.OutOfMemoryError("hey watch it! We are running out of objects here!");
        }
        
        int numObjectsToAllocate = POOL_MAX_SIZE - mObjectsInUse.size();
        synchronized (this) {
            if (POOL_STEP_SIZE > POOL_MAX_SIZE - mObjectsInUse.size())
                numObjectsToAllocate = POOL_MAX_SIZE - mObjectsInUse.size();
            else
                numObjectsToAllocate = POOL_STEP_SIZE;
            
            for (int i = 0; i < numObjectsToAllocate; i++) {
                mAvailableObjects.add(new ScaleImageView(mContext));
            }
        }
    }
    
    public void clearAllImageViews() {
        ScaleImageView view;
        for (int i = 0; i < mObjectsInUse.size(); i++) {
            view = mObjectsInUse.remove(i);
            if (view != null) {
                view.setImageBitmap(null);
                
                mAvailableObjects.add(view);
            }
        }
    }
}
