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
	//TODO: remove this assignment when real count available.
	public int mLikesCount = 5;
	public int mLikeDislikeStatus = AisleDetailsViewAdapter.IMG_NONE_STATUS;
	public ArrayList<String> mCommentsList;
	                         
}
