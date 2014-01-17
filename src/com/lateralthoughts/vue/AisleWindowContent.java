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

import com.lateralthoughts.vue.utils.Utils;

public class AisleWindowContent {
    public static final String EMPTY_AISLE_CONTENT_ID = "EmptyAisleWindow";
    private static final String IMAGE_RES_SPEC_REGEX = ".jpg"; // this is the
                                                               // string
                                                               // pattern we
                                                               // look for
    private String mImageFormatSpecifier = "._SY%d.jpg";
    boolean mAisleBookmarkIndicator = false;
    public int mTrendingBestHeight = 0;
    public int mAisleCardHeight;
    
    private static final String AISLE_STAGE_FOUR = "completed";
    public int mTotalBookmarkCount;
    public int mTotalLikesCount;
    public String mAisleCureentStage;
    
    public boolean getWindowBookmarkIndicator() {
        return mAisleBookmarkIndicator;
    }
    
    public void setWindowBookmarkIndicator(boolean windowBookmarkIndicator) {
        this.mAisleBookmarkIndicator = windowBookmarkIndicator;
    }
    // these two should be based on device with & height
    private String mAisleId;
    
    int mWindowSmallestHeight = 0;
    int mWindowSamllestWidth = 0;
    private int mWindowLargestHeight = 0;
    
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
        findMostLikesImage(mAisleImagesList);
        findAisleStage(mAisleImagesList);
        mTotalBookmarkCount = context.mBookmarkCount;
        this.mAisleBookmarkIndicator = VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .checkIsAisleBookmarked(mAisleId);
        for (int index = 0; index < mAisleImagesList.size(); index++) {
            boolean status = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .getImageRateStatus(mAisleImagesList.get(index).mId);
            if (status) {
                mAisleImagesList.get(index).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
            }
        }
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
        mWindowSmallestHeight = 0;
        if (mAisleImagesList == null) {
            return true;
        }
        for (int i = 0; i < mAisleImagesList.size(); i++) {
            imageDetails = mAisleImagesList.get(i);
            if (imageDetails.mAvailableHeight < mWindowSmallestHeight
                    || mWindowSmallestHeight == 0) {
                mWindowSmallestHeight = imageDetails.mAvailableHeight;
                mWindowSamllestWidth = imageDetails.mAvailableWidth;
            }
        }
        for (int i = 0; i < mAisleImagesList.size(); i++) {
            prepareCustomUrl(mAisleImagesList.get(i));
        }
        mAisleImagesList = modifyHeights(mAisleImagesList);
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
    
    public void setBestLargestHeightForWindow(int largestHeight) {
        mWindowLargestHeight = largestHeight;
    }
    
    /* */
    private ArrayList<AisleImageDetails> modifyHeights(
            ArrayList<AisleImageDetails> imageList) {
        if (imageList.size() == 0) {
            return null;
        }
        float[] imageHeightList = new float[imageList.size()];
        float availableScreenHeight = VueApplication.getInstance()
                .getScreenHeight();
        float adjustedImageHeight, adjustedImageWidth;
        float imageHeight, imageWidth;
        float cardWidth = VueApplication.getInstance().getVueDetailsCardWidth() / 2;
        float aisleHeightOnCard = 0;
        
        for (int i = 0; i < imageList.size(); i++) {
            imageHeight = imageList.get(i).mAvailableHeight;
            imageWidth = imageList.get(i).mAvailableWidth;
            if (imageHeight > availableScreenHeight) {
                adjustedImageHeight = availableScreenHeight;
                adjustedImageWidth = (adjustedImageHeight / imageHeight)
                        * imageWidth;
                imageHeight = adjustedImageHeight;
                imageWidth = adjustedImageWidth;
            }
            if (imageWidth > cardWidth) {
                adjustedImageWidth = cardWidth;
                adjustedImageHeight = (adjustedImageWidth / imageWidth)
                        * imageHeight;
                imageHeight = adjustedImageHeight;
                imageWidth = adjustedImageWidth;
            }
            imageList.get(i).mTrendingImageHeight = Math.round(imageHeight);
            imageList.get(i).mTrendingImageWidth = Math.round(imageWidth);
            imageHeightList[i] = imageHeight;
            
        }
        
        aisleHeightOnCard = imageHeightList[0];
        mWindowSmallestHeight = (int) imageHeightList[0];
        int smallestHeightPosition = 0;
        for (int i = 0; i < imageHeightList.length; i++) {
            if (aisleHeightOnCard > imageHeightList[i]) {
                aisleHeightOnCard = imageHeightList[i];
                mWindowSmallestHeight = (int) imageHeightList[i];
                smallestHeightPosition = i;
            }
        }
        AisleImageDetails smallestItem = imageList
                .remove(smallestHeightPosition);
        imageList.add(0, smallestItem);
        mAisleCardHeight = Math.round(aisleHeightOnCard);
        return imageList;
        
    }
    
    public void getAisleImageForImageId(String imageId, String imageUrl,
            String newServerResponseImageId) {
        if (imageId != null) {
            if (mAisleImagesList != null && mAisleImagesList.size() > 0) {
                for (int i = 0; i < mAisleImagesList.size(); i++) {
                    if (imageId.equals(mAisleImagesList.get(i).mId)) {
                        mAisleImagesList.get(i).mId = newServerResponseImageId;
                        if (mAisleImagesList.get(i).mIsFromLocalSystem) {
                            mAisleImagesList.get(i).mImageUrl = imageUrl;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * show star to most likes on the image.
     */
    private void findMostLikesImage(ArrayList<AisleImageDetails> itemsList) {
        int mostLikePosition = 0, mLikes = 0;
        boolean hasLikes = false;
        for (int i = 0; i < itemsList.size(); i++) {
            itemsList.get(i).mHasMostLikes = false;
            itemsList.get(i).mSameMostLikes = false;
            if (mLikes < itemsList.get(i).mLikesCount) {
                mLikes = itemsList.get(i).mLikesCount;
                hasLikes = true;
                mostLikePosition = i;
            }
        }
        if (hasLikes) {
            itemsList.get(mostLikePosition).mHasMostLikes = true;
        }
        if (mLikes == 0) {
            return;
        }
        for (int i = 0; i < itemsList.size(); i++) {
            if (mostLikePosition == i) {
                continue;
            }
            if (mLikes == itemsList.get(i).mLikesCount) {
                itemsList.get(i).mSameMostLikes = true;
                itemsList.get(i).mHasMostLikes = true;
                itemsList.get(mostLikePosition).mSameMostLikes = true;
            }
        }
    }
    
    public void findAisleStage(ArrayList<AisleImageDetails> itemsList) {
        int likesCount = 0, commentsCount = 0, totalCount;
        for (int index = 0; index < itemsList.size(); index++) {
            AisleImageDetails imageDetails = itemsList.get(index);
            int tempLikesCount = imageDetails.mLikesCount;
            if (tempLikesCount > likesCount) {
                likesCount = tempLikesCount;
            }
            int tempCommentsCount = imageDetails.mCommentsList.size();
            if (tempCommentsCount > commentsCount) {
                commentsCount = tempCommentsCount;
            }
        }
        mTotalLikesCount = likesCount;
        int totalCommentCount = commentsCount;
        totalCount = likesCount + totalCommentCount;
        if (totalCount == 0) {
            mAisleCureentStage = VueConstants.AISLE_STATGE_ONE;
        } else if (likesCount >= 3 || commentsCount >= 3) {
            mAisleCureentStage = VueConstants.AISLE_STAGE_THREE;
        } else if (likesCount < 3 || commentsCount < 3) {
            mAisleCureentStage = VueConstants.AISLE_STAGE_TWO;
        }
    }
    
    private AisleContext mContext;
    private ArrayList<AisleImageDetails> mAisleImagesList;
}
