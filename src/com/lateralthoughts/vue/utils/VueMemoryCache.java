/**
 * 
 * A Parametrized MemoryCache implementation to help us keep track of a
 * variety of data for quick access.
 * Currently only has support for Bitmap & String types and adding support
 * for a new type is as simple as detecting the class type and calculating the
 * size in bytes taken up by the object. The primary function to modify is 
 * getSizeInBytes() and look at the implementation for Bitmap and String cases
 * for reference.
 * 
 * The limit for memory cache is by default set to 5% of the maximum heap available
 * to the app. This is easily changed by using the setLimit() API but do so with caution.
 * There is no free memory - just setting a higher limit does not magically make more
 * memory! It just makes more memory available for MemoryCache and that means less memory
 * for other stuff. If that all sounds scary you shouldn't be changing this code or invoking
 * the setLimit() API.
 * 
 * @author Vinodh Sundararajan
 *
 */

package com.lateralthoughts.vue.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.util.Log;

public class VueMemoryCache<T> {

    private static final String TAG = "VueMemoryCache";
    private Map<String, T> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, T>(10,1.5f,true));//Last argument true for LRU ordering
    private long size = 0;//current allocated size
    private long limit = 1000000;//max memory in bytes
    
    private final String ANDROID_BITMAP_CLASS = "android.graphics.Bitmap";
    private final String STANDARD_STRING_CLASS = "java.lang.String";

    public VueMemoryCache(){
        //by default we will set the limit at 5% of max heap
        setLimit(20);
    }
    
    public void setLimit(int percentOfMax){    	
        limit = (Runtime.getRuntime().maxMemory()*percentOfMax)/100;
        Log.i(TAG, "MemoryCache will use up to " + limit/1024./1024.+"MB");
    }

    public T get(String id){
        try{
            if(!cache.containsKey(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        }catch(NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String id, T object){
        try{
            if(cache.containsKey(id))
                size-=getSizeInBytes(cache.get(id));
            cache.put(id, object);
            size+=getSizeInBytes(object);
            checkSize();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    private void checkSize() {
        Log.i(TAG, "cache size="+size+" length="+cache.size());
        if(size>limit){
            Iterator<Entry<String, T>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<String, T> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                Log.e("Jaws","running out of cache - about to remove an item now");
                iter.remove();
                if(size<=limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size "+cache.size());
        }
    }

    public void clear() {
        try{
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size = 0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

    long getSizeInBytes(T object) {
    	long size = 0;
    	Log.d(TAG,"***** getSizeInBytes invoked for object = " + object);
        if(null == object)
            return 0;
        
        String className = object.getClass().getName();
        if(className.equals(ANDROID_BITMAP_CLASS)){
        	Bitmap bmp = (Bitmap)object;
        	size = bmp.getRowBytes() * bmp.getHeight();        	
        }else if(className.equals(STANDARD_STRING_CLASS)){
        	//should be a string if its not a bitmap but its
        	//worth checking anyway
        	String content = (String)object;
        	size = content.length();
        }
        return size;
    }
}
