/**
 * @author Vinodh Sundararajan
 * 
 * An aisle is a combination of a given user submitting images while looking for
 * for something for an occasion. For example, Joe Dawes is looking for
 * tuxedos for a christmas party.
 * Although this combination makes an AisleWindowContent it is not neccesarily unique.
 * Every aisle is identified by a unique identifier which we will use to keep track
 * of the aisles.
 */
package com.lateralthoughts.vue;

import com.lateralthoughts.vue.VueApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import android.util.Pair;

public class AisleWindowContent
{
	private static final String IMAGE_RES_SPEC_REGEX = "._S"; //this is the string pattern we look for
	private String mImageFormatSpecifier = "._SX%d_SY%d.jpg";
	
	//these two should be based on device with & height
	private int mDesiredImageWidth = 380;
	private int mDesiredImageHeight = 540;
    public AisleWindowContent(String aisleId){ 
    }
    
    public AisleWindowContent(AisleContext context, ArrayList<AisleImageDetails> items){    	
    }
    
    public void addAisleContent(AisleContext context, ArrayList<AisleImageDetails> items){
    	mAisleImagesList = (ArrayList<AisleImageDetails>)items.clone();
    	mContext = context;
    	//lets parse through the image urls and update the image resolution
    	//VueApplication.getInstance().getResources().getString(R.id.image_res_placeholder);
    	udpateImageUrlsForDevice();
    }
    
    public ArrayList<AisleImageDetails> getImageList(){
    	return mAisleImagesList;
    }
    
    public int getSize(){
    	return mAisleImagesList.size();
    }
    
    private boolean udpateImageUrlsForDevice(){
    	String regularUrl;
    	String urlReusablePart;
    	String customFittedSizePart;
    	int index = -1;
    	StringBuilder sb;
    	AisleImageDetails imageDetails;
    	for (int i=0;i<mAisleImagesList.size();i++){
    		sb = new StringBuilder();
    		imageDetails = mAisleImagesList.get(i);
    		regularUrl = imageDetails.mImageUrl;
    		index = regularUrl.indexOf(IMAGE_RES_SPEC_REGEX); 
    		if(-1 != index){
    			//we have a match
    			urlReusablePart = regularUrl.split(IMAGE_RES_SPEC_REGEX)[0];
    			sb.append(urlReusablePart);
    			customFittedSizePart = String.format(mImageFormatSpecifier, mDesiredImageWidth, mDesiredImageHeight);  
    			sb.append(customFittedSizePart);
    			imageDetails.mCustomImageUrl = sb.toString();
    		}else{
    			imageDetails.mCustomImageUrl = regularUrl;
    		}
    	}
    	return true;
    }
    
    public AisleContext getAisleContext(){
    	return mContext;
    }
    
    private AisleContext mContext;
    private ArrayList<AisleImageDetails> mAisleImagesList;
}
