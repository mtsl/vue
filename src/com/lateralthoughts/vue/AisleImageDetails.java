package com.lateralthoughts.vue;

import java.util.ArrayList;

public class AisleImageDetails {
	public String mDetalsUrl;
	public String mImageUrl;
	public String mId;
	public String mStore;
	public String mTitle;
	public String mCustomImageUrl;
	public int mAvailableHeight;
	public int mAvailableWidth;

	public int mLikesCount = 0;
	public String mOwnerUserId;
	public String mOwnerAisleId;
	public String mRating;
	public int mLikeDislikeStatus = AisleDetailsViewAdapter.IMG_NONE_STATUS;
	public ArrayList<String> mCommentsList = new ArrayList<String>();
	public int mTrendingImageHeight;
	public int mTrendingImageWidth;
	public int mDetailsImageWidth;
	public int mDetailsImageHeight;
	public boolean mIsFromLocalSystem;
	public int mTempResizeBitmapwidth;
	public int mTempResizedBitmapHeight;
}
