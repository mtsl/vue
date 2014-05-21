package com.lateralthoughts.vue;

//java utils
import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

//vue internal imports

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

public class ContentAdapterFactory {
    
    private static ContentAdapterFactory sContentAdapterFactory = null;
    
    private ArrayList<AisleContentAdapter> mAvailableObjects = null;
    private ArrayList<AisleContentAdapter> mObjectsInUse = null;
    
    //private final int POOL_SIZE = 250;
    private final int POOL_SIZE = 50;
    private final int POOL_STEP_SIZE = 50;
    private final int POOL_MAX_SIZE = 1000;
    
    private Context mContext;
    public Drawable mEvenColumnBackground;
    public Drawable mOddColumnBackground;
    
    private ContentAdapterFactory(Context context) {
        mAvailableObjects = new ArrayList<AisleContentAdapter>();
        mObjectsInUse = new ArrayList<AisleContentAdapter>();
        
        // TODO: assert if context is null
        mContext = context;
        
        for (int i = 0; i < POOL_SIZE; i++) {
            mAvailableObjects.add(new AisleContentAdapter(mContext));
        }
    }
    
    public static ContentAdapterFactory getInstance(Context context) {
        if (null == sContentAdapterFactory) {
            sContentAdapterFactory = new ContentAdapterFactory(context);
        }
        return sContentAdapterFactory;
    }
    
    public IAisleContentAdapter getAisleContentAdapter() {
        AisleContentAdapter aisleContentAdapter = null;
        
        if (mAvailableObjects.isEmpty()) {
            expandPoolOrDie();
        }
        
        synchronized (this) {
            if (!mAvailableObjects.isEmpty()) {
                aisleContentAdapter = mAvailableObjects
                        .remove(mAvailableObjects.size() - 1);
                mObjectsInUse.add(aisleContentAdapter);
            }
        }
        return aisleContentAdapter;
    }
    
    public void returnUsedAdapter(IAisleContentAdapter aisleContentAdapter) {
        int index = -1;
        if (null == aisleContentAdapter) {
            return;
        }
        index = mObjectsInUse.indexOf(aisleContentAdapter);
        if (-1 == index) {
            return;
        }
        
        synchronized (this) {
            AisleContentAdapter ac = mObjectsInUse.remove(index);
            // ac.releaseContentSource();
            mAvailableObjects.add(ac);
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
                mAvailableObjects.add(new AisleContentAdapter(mContext));
            }
        }
    }
}
