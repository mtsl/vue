package com.lateralthoughts.vue;

//java utils
import java.util.ArrayList;

import android.content.Context;

public class AisleWindowContentFactory {
    
    private static AisleWindowContentFactory sAisleWindowContentFactory = null;
    
    private ArrayList<AisleWindowContent> mAvailableObjects = null;
    private ArrayList<AisleWindowContent> mObjectsInUse = null;
    
    //private final int POOL_SIZE = 100;
    private final int POOL_SIZE = 50;
    private final int POOL_STEP_SIZE = 100;
    private final int POOL_MAX_SIZE = 500;
    public final String EMPTY_AISLE_ID = "-1";
    
    private AisleWindowContentFactory(Context context) {
        mAvailableObjects = new ArrayList<AisleWindowContent>();
        mObjectsInUse = new ArrayList<AisleWindowContent>();
        
        // TODO: assert if context is null
        // mContext = context;
        
        // at construction time - lets go ahead and create the pool
        // of scaleimageview objects
        for (int i = 0; i < POOL_SIZE; i++) {
            mAvailableObjects
                    .add(new AisleWindowContent(EMPTY_AISLE_ID, false));
        }
    }
    
    public static AisleWindowContentFactory getInstance(Context context) {
        if (null == sAisleWindowContentFactory) {
            sAisleWindowContentFactory = new AisleWindowContentFactory(context);
        }
        return sAisleWindowContentFactory;
    }
    
    // TODO: I think this method should throw an exception if exceed some kind
    // of absolute maximum limit for number of ScaledImageView objects.
    public AisleWindowContent getEmptyAisleWindow() {
        AisleWindowContent windowContent = null;
        
        if (mAvailableObjects.isEmpty()) {
            // try{
            expandPoolOrDie();
            // }catch (java.lang.OutOfMemoryError mem){
            
            // }
        }
        
        synchronized (this) {
            if (!mAvailableObjects.isEmpty()) {
                windowContent = mAvailableObjects.remove(mAvailableObjects
                        .size() - 1);
                mObjectsInUse.add(windowContent);
                
            }
        }
        
        return windowContent;
    }
    
    public void returnUsedAisleWindow(AisleWindowContent content) {
        int index = -1;
        AisleWindowContent windowContent;
        if (null == content) {
            return;
        }
        index = mObjectsInUse.indexOf(content);
        if (-1 == index) {
            return;
        }
        
        synchronized (this) {
            windowContent = mObjectsInUse.remove(index);
            mAvailableObjects.add(windowContent);
            
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
        
        int numObjectsToAllocate = POOL_STEP_SIZE;
        if (POOL_MAX_SIZE - mObjectsInUse.size() < POOL_STEP_SIZE) {
            numObjectsToAllocate = POOL_MAX_SIZE - mObjectsInUse.size();
        }
        synchronized (this) {
            for (int i = 0; i < numObjectsToAllocate; i++) {
                mAvailableObjects.add(new AisleWindowContent(EMPTY_AISLE_ID,
                        false));
            }
        }
    }
    
    public void clearObjectsInUse() {
        for (int i = 0; i < mObjectsInUse.size(); i++) {
            AisleWindowContent windowContent = mObjectsInUse.remove(i);
            mAvailableObjects.add(windowContent);
        }
    }
    
}
