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

import android.util.Log;
import com.lateralthoughts.vue.utils.Utils;

import java.util.ArrayList;

public class AisleWindowContent {
	public static final String EMPTY_AISLE_CONTENT_ID = "EmptyAisleWindow";
	private static final String IMAGE_RES_SPEC_REGEX = ".jpg"; // this is the
																// string
																// pattern we
																// look for
	private String mImageFormatSpecifier = "._SY%d.jpg";
	private int mAisleBookmarksCount = 0;
	private boolean mAisleBookmarkIndicator = false;
	public boolean mIsDataChanged = false;
	public int mTrendingBestHeight = 0;
	
	public int mTrendingTestBestHeight,mTrendingTestBestWidth;

	public boolean getWindowBookmarkIndicator() {
		return mAisleBookmarkIndicator;
	}

	public void setWindowBookmarkIndicator(boolean windowBookmarkIndicator) {
		this.mAisleBookmarkIndicator = windowBookmarkIndicator;
	}

	public int getmAisleBookmarksCount() {
		return mAisleBookmarksCount;
	}

	public void setmAisleBookmarksCount(int mAisleBookmarksCount) {
		this.mAisleBookmarksCount = mAisleBookmarksCount;
	}

	// these two should be based on device with & height
	private String mAisleId;

	int mWindowSmallestHeight = 0;
	int mWindowSamllestWidth = 0;
	private int mWindowLargestHeight = 0;
	private int mWindowLargestWidth = 0;

	public AisleWindowContent(String aisleId) {
		mAisleId = aisleId;
	}

	public AisleWindowContent(String aisleId, boolean createPlaceHolders) {
		mAisleId = aisleId;
		if (createPlaceHolders) {
			mContext = new AisleContext();
			mAisleImagesList = new ArrayList<AisleImageDetails>();
		}
	}

	public AisleWindowContent(AisleContext context,
			ArrayList<AisleImageDetails> items) {
	}

	public void setAisleId(String aisleId) {
		mAisleId = aisleId;
	}

	@SuppressWarnings("unchecked")
	public void addAisleContent(AisleContext context,
			ArrayList<AisleImageDetails> items) {
		if (null != mAisleImagesList) {
			mAisleImagesList = null;
		}
		if (null != mContext) {
			mContext = null;
		}
		if (items != null) {
			mAisleImagesList = (ArrayList<AisleImageDetails>) items.clone();
		}
		mAisleId = context.mAisleId;
		mContext = context;
		// lets parse through the image urls and update the image resolution
		// VueApplication.getInstance().getResources().getString(R.id.image_res_placeholder);
		udpateImageUrlsForDevice();
	}

	public ArrayList<AisleImageDetails> getImageList() {
		return mAisleImagesList;
	}

	public int getSize() {
		return mAisleImagesList.size();
	}

	private boolean udpateImageUrlsForDevice() {
		AisleImageDetails imageDetails;
		// TODO: when more images available set this variable to smallest height
		// among all
		int smallestHeightPosition = 0;
		mWindowSmallestHeight = /* 34 */0;
		for (int i = 0; i < mAisleImagesList.size(); i++) {
			imageDetails = mAisleImagesList.get(i);
			if (imageDetails.mAvailableHeight < mWindowSmallestHeight
					|| mWindowSmallestHeight == 0) {
				mWindowSmallestHeight = imageDetails.mAvailableHeight;
				mWindowSamllestWidth =  imageDetails.mAvailableWidth;
				smallestHeightPosition = i;
			}
		}
		for(int i = 0;i<mAisleImagesList.size();i++){
			imageDetails = mAisleImagesList.get(i);
			if (imageDetails.mAvailableHeight > mWindowLargestHeight
					|| mWindowLargestHeight == 0) {
				mWindowLargestHeight = imageDetails.mAvailableHeight;
				mWindowLargestWidth = imageDetails.mAvailableWidth;
			}
			
		}
		mWindowLargestHeight = getBestHeightForDetailsScreen(mWindowLargestHeight,mWindowLargestWidth);
		mWindowSmallestHeight = getBestHeightForTrendingScreen(mWindowSmallestHeight,mWindowSamllestWidth);
/*		mWindowSmallestHeight = getBestHeight(
				mAisleImagesList.get(smallestHeightPosition).mAvailableHeight,
				mAisleImagesList.get(smallestHeightPosition).mAvailableWidth, mWindowSmallestHeight);*/
		for (int i = 0; i < mAisleImagesList.size(); i++) {
			prepareCustomUrl(mAisleImagesList.get(i));
		}
		return true;
	}

	public void prepareCustomUrl(AisleImageDetails imageDetails) {
		StringBuilder sb = new StringBuilder();
		String urlReusablePart;
		String customFittedSizePart;
		String regularUrl = imageDetails.mImageUrl;
		int index = -1;
		index = regularUrl.indexOf(IMAGE_RES_SPEC_REGEX);
		if (-1 != index) {
			// we have a match
			urlReusablePart = regularUrl.split(IMAGE_RES_SPEC_REGEX)[0];
			sb.append(urlReusablePart);
			customFittedSizePart = String.format(mImageFormatSpecifier,
					mWindowSmallestHeight);
			sb.append(customFittedSizePart);
			imageDetails.mCustomImageUrl = sb.toString();
		} else {
			imageDetails.mCustomImageUrl = regularUrl;
		}
		imageDetails.mCustomImageUrl = Utils.addImageInfo(
				imageDetails.mCustomImageUrl, imageDetails.mAvailableWidth,
				imageDetails.mAvailableHeight);
		Log.i("AisleWindowContent", "Image Url and CustominageUrl : "
				+ imageDetails.mCustomImageUrl + " ??? "
				+ imageDetails.mImageUrl);

	}

	public AisleContext getAisleContext() {
		return mContext;
	}

	public void setAisleContext(AisleContext context) {
		mContext = context;
	}

	public String getAisleId() {
		return mAisleId;
	}

	public int getBestHeightForWindow() {
		return mWindowSmallestHeight;
	}

	public void setBestHeightForWindow(int height) {
		mWindowSmallestHeight = height;
		
	}

	public int getBestLargetHeightForWindow() {
		return mWindowLargestHeight;
	}
    public void setBestLargestHeightForWindow(int largestHeight,int width){
    	mWindowLargestHeight = largestHeight;
    	mWindowLargestWidth = width;
    	mWindowLargestHeight = getBestHeightForDetailsScreen(largestHeight,width);
    }
	public int getBestHeight(int height, int width, int bestHeight) {
		int trendingCardWidth = VueApplication.getInstance()
				.getScreenWidth()/2;
		try {
			int newwidth = (width * trendingCardWidth) / width;
			int newHeight = (height * trendingCardWidth) / width;

			if (newHeight > bestHeight) {
				newHeight = (newHeight * bestHeight) / newHeight;
			}
			if (newHeight <= bestHeight) {
				bestHeight = newHeight;
			}
			Log.e("getBestHeight", "BestHeight ???" + bestHeight + "??width??"
					+ width + "??height??" + height + "??trending card width??"
					+ trendingCardWidth);
			mTrendingTestBestHeight = bestHeight;
			mTrendingTestBestWidth = trendingCardWidth;
			Log.i("TrendingCrop", "TrendingCrop1:*********************");
			Log.i("TrendingCrop", "TrendingCrop1: bestHeight "+mTrendingTestBestHeight);
			Log.i("TrendingCrop", "TrendingCrop1: bestWidth "+mTrendingTestBestWidth);
			Log.i("TrendingCrop", "TrendingCrop1: aisle "+mAisleId);
			Log.i("TrendingCrop", "TrendingCrop1:##########################");
			return bestHeight;
		} catch (Exception e) {
			Log.e("getBestHeight", "BestHeight ??? catch" + bestHeight
					+ "??width??" + width + "??height??" + height + "??url??");
			e.printStackTrace();
		}

		return bestHeight;
	}
  private int getBestHeightForDetailsScreen(int height,int width){
	int bestLargestHeight;
	int bestWidth = 0;
	  int screenWidth = VueApplication.getInstance().getScreenWidth();
	  int screenHeight = VueApplication.getInstance().getScreenHeight();
	  if(height >= screenHeight){
		  bestLargestHeight =( height*screenHeight)/height;
		  bestWidth = (width * screenHeight)/height;
	  } else {
		  bestLargestHeight = height;
		  bestWidth = width;
	  }
	  if(bestWidth > screenWidth){
		  bestLargestHeight = (bestLargestHeight *screenWidth) /bestWidth;
				  bestWidth = screenWidth ;
		  
	  }
	  return bestLargestHeight;
	  
  }
  private int getBestHeightForTrendingScreen(int height,int width){
	int bestHeight;
	int trendingCardWidth = VueApplication.getInstance().getScreenWidth()/2;
	if(width > trendingCardWidth){
		bestHeight = (height * trendingCardWidth)/width;
	} else {
		bestHeight = height;
	}
	  
	  
	  return bestHeight;
	  
  }
	private AisleContext mContext;
	private ArrayList<AisleImageDetails> mAisleImagesList;
}
