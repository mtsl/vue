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

import java.util.ArrayList;

public class AisleWindowContent
{
	public static final String EMPTY_AISLE_CONTENT_ID = "EmptyAisleWindow";
	private static final String IMAGE_RES_SPEC_REGEX = "._S"; //this is the string pattern we look for
	private String mImageFormatSpecifier = "._SX%d_SY%d.jpg";
	
	//these two should be based on device with & height
	private int mDesiredImageWidth = 360;
	private int mDesiredImageHeight = 360;
	private String mAisleId;
	
    public AisleWindowContent(String aisleId){ 
    	mAisleId = aisleId;
    }
    
    public AisleWindowContent(AisleContext context, ArrayList<AisleImageDetails> items){    	
    }
    
    @SuppressWarnings("unchecked")
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
    			//imageDetails.mCustomImageUrl = regularUrl;
    		}else{
    			imageDetails.mCustomImageUrl = regularUrl;
    		}
    	}
    	return true;
    }
    
    public AisleContext getAisleContext(){
    	return mContext;
    }
    
    public String getAisleId(){
    	return mAisleId;
    }
    
    private AisleContext mContext;
    private ArrayList<AisleImageDetails> mAisleImagesList;
}
