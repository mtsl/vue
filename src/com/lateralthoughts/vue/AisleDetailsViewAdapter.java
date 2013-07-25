/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.AisleContentBrowser.DetailClickListener;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
   private Context mContext;

   public static final String TAG = "AisleDetailsViewAdapter";
   private static final boolean DEBUG = false;

 
   private AisleDetailsViewListLoader mViewLoader;
   private AisleDetailSwipeListener mswipeListner;

   // we need to customize the layout depending on screen height & width which
   // we will get on the fly
  
 
 
   private int mListCount;
   public int mLikes = 5;
   private int mBookmarksCount;
   public int mInitialImageLikeCounts[];
   public int mTempInitialImageLikeCounts[];
   public int mCurrentDispImageIndex;
   private boolean mallowLike = true,mallowDisLike = true;
   private boolean mIsLikeImageClciked = false;
   private boolean mIsBookImageClciked = false;
   private boolean mIsBorowserSet = false;
   
   private AisleWindowContent mWindowContentTemp;
   public String mVueusername;
   ShareDialog mShare ;
   private String mAisleWindowId;
   private ScaledImageViewFactory mViewFactory = null;
   private ContentAdapterFactory mContentAdapterFactory;
   @SuppressLint("UseSparseArrays")
   Map<Integer, Object> mCommentsMapList = new HashMap<Integer, Object>();
   ViewHolder mViewHolder;
   ArrayList<String> mShowingList;
   
   public AisleDetailsViewAdapter(Context c,
         AisleDetailSwipeListener swipeListner, int listCount,
         ArrayList<AisleWindowContent> content) {
      super(c, content);
      mContext = c;
      mViewFactory = ScaledImageViewFactory.getInstance(mContext);
      mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
      mContentAdapterFactory = ContentAdapterFactory.getInstance(mContext);
      mswipeListner = swipeListner;
      mListCount = listCount;
      mShowingList = new ArrayList<String>();
      if (DEBUG)
         Log.e(TAG, "About to initiate request for trending aisles");
      
      for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
    	  mWindowContentTemp = (AisleWindowContent) getItem(i);
          if (mWindowContentTemp.getAisleId().equalsIgnoreCase(
                VueApplication.getInstance().getClickedWindowID())) {
        	  mWindowContentTemp = (AisleWindowContent) getItem(i);
        	  mAisleWindowId = mWindowContentTemp.getAisleId();
             break;
          }
      }
    if (mWindowContentTemp != null) {
    	mBookmarksCount = mWindowContentTemp.getmAisleBookmarksCount();
      ArrayList<AisleImageDetails> imageDetailsArr = mWindowContentTemp
          .getImageList();

      mInitialImageLikeCounts = new int[imageDetailsArr.size()];
      mTempInitialImageLikeCounts = new int[imageDetailsArr.size()];
      for (int i = 0; i < imageDetailsArr.size(); i++) {
        mInitialImageLikeCounts[i] = imageDetailsArr.get(i).mLikesCount;
        mTempInitialImageLikeCounts[i] = imageDetailsArr.get(i).mLikesCount;
        
        mCommentsMapList.put(i, imageDetailsArr.get(i).mCommentsList);
          if(imageDetailsArr.get(i).mCommentsList == null) {
                //TODO: for temp comments display need to replace this 
        	  imageDetailsArr.get(i).mCommentsList = new ArrayList<String>();
        	  if(i %2 == 0){
        		   for(int k=0;k< 6;k++){
          	    	 imageDetailsArr.get(i).mCommentsList.add("Love Love vue the dress! Simple and fabulous.");
          	    }
        	  } else {
        	    for(int k=0;k< 10;k++){
        	    	 imageDetailsArr.get(i).mCommentsList.add("vue vue vue  sample test comments");
        	    }
        	  }
        	    mCommentsMapList.put(i, imageDetailsArr.get(i).mCommentsList);
          }
          
        if (mInitialImageLikeCounts[i] == 0) {
          // 5 is the temporary   count 
          mInitialImageLikeCounts[i] = 5;
          mTempInitialImageLikeCounts[i] = 5;
        }
      }
      mShowingList = imageDetailsArr.get(0).mCommentsList;
    }
   }

   @Override
   public AisleWindowContent getItem(int position) {
      return mVueTrendingAislesDataModel.getAisleAt(position);
   }
   static class ViewHolder {
      AisleContentBrowser aisleContentBrowser;
      HorizontalScrollView thumbnailContainer;
      TextView aisleDescription;
      TextView aisleOwnersName;
      TextView aisleContext, commentCount, likeCount;
      TextView bookMarkCount;
      ImageView profileThumbnail;
      ImageView vueWindowBookmarkImg;
      String uniqueContentId;
      LinearLayout aisleDescriptor;
      AisleWindowContent mWindowContent;
      LinearLayout imgContentlay, commentContentlay;
      LinearLayout vueCommentheader, addCommentlay;
      TextView userComment, enterComment;
     // TextView vue_user_enterComment;
      ImageView userPic, commentImg, likeImg;
      RelativeLayout exapandHolder;
      EditText edtComment;
      View separator;
      RelativeLayout enterCommentrellay;
      FrameLayout edtCommentLay;
      ImageView commentSend;
      String tag;
   }

   @Override
   public int getCount() {
      return mListCount;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {

      if (convertView == null) {
         mViewHolder = new ViewHolder();
         LayoutInflater layoutInflator = LayoutInflater.from(mContext);
         convertView = layoutInflator.inflate(R.layout.vue_details_adapter,
               null);
         mViewHolder.aisleContentBrowser = (AisleContentBrowser) convertView
               .findViewById(R.id.showpiece);
         mViewHolder.imgContentlay = (LinearLayout) convertView
               .findViewById(R.id.vueimagcontent);
         mViewHolder.commentContentlay = (LinearLayout) convertView
               .findViewById(R.id.vue_user_coment_lay);
         mViewHolder.vueCommentheader = (LinearLayout) convertView
               .findViewById(R.id.vue_comment_header);
         mViewHolder.aisleDescription = (TextView) convertView
               .findViewById(R.id.vue_details_descreption);
         mViewHolder.separator = (View) convertView
               .findViewById(R.id.separator);
         mViewHolder.vueWindowBookmarkImg = (ImageView) convertView
                 .findViewById(R.id.vuewndow_bookmark_img);
          
       /*  mViewHolder.vue_user_enterComment = (TextView) convertView
               .findViewById(R.id.vue_user_entercomment);*/
         mViewHolder.enterCommentrellay = (RelativeLayout) convertView.findViewById(R.id.entercmentrellay);
         
         mViewHolder.edtComment = (EditText) convertView
               .findViewById(R.id.edtcomment);
         mViewHolder.likeImg = (ImageView) convertView
               .findViewById(R.id.vuewndow_lik_img);
         mViewHolder.likeCount = (TextView) convertView
               .findViewById(R.id.vuewndow_lik_count);
         mViewHolder.addCommentlay = (LinearLayout) convertView
               .findViewById(R.id.addcommentlay);
         mViewHolder.exapandHolder = (RelativeLayout) convertView
               .findViewById(R.id.exapandholder);
         mViewHolder.aisleDescription.setTextSize(Utils.SMALL_TEXT_SIZE);
         mViewHolder.userPic = (ImageView) convertView
               .findViewById(R.id.vue_user_img);
         mViewHolder.userComment = (TextView) convertView
               .findViewById(R.id.vue_user_comment);
         mViewHolder.commentCount = (TextView) convertView
               .findViewById(R.id.vuewndow_comment_count);
         mViewHolder.bookMarkCount = (TextView) convertView
                 .findViewById(R.id.vuewndow_bookmark_count);
         mViewHolder.commentCount.setTextSize(Utils.SMALL_TEXT_SIZE);
         mViewHolder.bookMarkCount.setTextSize(Utils.SMALL_TEXT_SIZE);
         mViewHolder.likeCount.setTextSize(Utils.SMALL_TEXT_SIZE);
         
         
         mViewHolder.commentImg = (ImageView) convertView
               .findViewById(R.id.vuewndow_comment_img);
         mViewHolder.commentSend = (ImageView) convertView.findViewById(R.id.sendcomment);
         
         mViewHolder.edtCommentLay = (FrameLayout) convertView.findViewById(R.id.edtcommentlay);
         mViewHolder.userComment.setTextSize(VueApplication.getInstance()
               .getmTextSize());
         mViewHolder.userComment.setTextSize(Utils.SMALL_TEXT_SIZE);
    /*     FrameLayout fl = (FrameLayout) convertView
               .findViewById(R.id.showpiece_container);*/
         FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
               VueApplication.getInstance().getScreenWidth(),
               (VueApplication.getInstance().getScreenHeight() * 60) / 100);
         mViewHolder.aisleContentBrowser.setLayoutParams(showpieceParams);
         mViewHolder.aisleContentBrowser
               .setAisleDetailSwipeListener(mswipeListner);
     
         mViewHolder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
         convertView.setTag(mViewHolder);
      }
      mViewHolder.commentCount.setText((mShowingList.size()+" Comments"));
      mViewHolder.bookMarkCount.setText(""+mBookmarksCount);
      mViewHolder.likeCount.setText("" + mLikes);
      mViewHolder = (ViewHolder) convertView.getTag();
      mViewHolder.imgContentlay.setVisibility(View.VISIBLE);
      mViewHolder.commentContentlay.setVisibility(View.VISIBLE);
      mViewHolder.vueCommentheader.setVisibility(View.VISIBLE);
      mViewHolder.addCommentlay.setVisibility(View.VISIBLE);
     // mViewHolder.edtCommentLay.setVisibility(View.VISIBLE);
      if (position == 0) {
         mViewHolder.commentContentlay.setVisibility(View.GONE);
         mViewHolder.vueCommentheader.setVisibility(View.GONE);
         mViewHolder.addCommentlay.setVisibility(View.GONE);
         mViewHolder.separator.setVisibility(View.GONE);
         mViewHolder.edtCommentLay.setVisibility(View.GONE);
         mViewHolder.mWindowContent = mWindowContentTemp;
         try {
            mVueusername = mViewHolder.mWindowContent.getAisleContext().mFirstName;
            int scrollIndex = 0;
            mWindowContentTemp = mViewHolder.mWindowContent;
            mViewHolder.tag = TAG;
       /*     if(!mIsBorowserSet) {
            	mIsBorowserSet = true;*/
            mViewLoader.getAisleContentIntoView(mViewHolder, scrollIndex,
                  position, new DetailImageClickListener());
          /*  }*/
            Log.i("returnsused imageview", "returnsused imageview adapeterclass count: "+  mViewHolder.aisleContentBrowser.getChildCount());
            Log.i("returnsused imageview", "returnsused imageview adapeterclass obj: "+ mViewHolder.aisleContentBrowser.getCustomAdapter());
         } catch (Exception e) {
            e.printStackTrace();
         }
         // gone comment layoutgone
      } else if (position == 1) {
         if (mIsLikeImageClciked) {
            mIsLikeImageClciked = false;
            Animation rotate = AnimationUtils.loadAnimation(mContext,
                  R.anim.bounce);
            mViewHolder.likeImg.startAnimation(rotate);
         
         }
         if(mIsBookImageClciked) {
        	 mIsBookImageClciked = false;
             Animation rotate = AnimationUtils.loadAnimation(mContext,
                     R.anim.bounce);
        	   mViewHolder.vueWindowBookmarkImg.startAnimation(rotate); 
         }
         mViewHolder.imgContentlay.setVisibility(View.GONE);
         mViewHolder.commentContentlay.setVisibility(View.GONE);
         mViewHolder.addCommentlay.setVisibility(View.GONE);
         mViewHolder.edtCommentLay.setVisibility(View.GONE);
         mViewHolder.likeImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ratingImage( );
			}
		});
         
         mViewHolder.likeCount.setOnClickListener(new OnClickListener() {
			
			@Override
				public void onClick(View v) {
				ratingImage( );
				}
		});
         
         // image content gone
    } else if (position == mListCount - 1) {
      mViewHolder.separator.setVisibility(View.GONE);
      mViewHolder.imgContentlay.setVisibility(View.GONE);
      mViewHolder.vueCommentheader.setVisibility(View.GONE);
      mViewHolder.commentContentlay.setVisibility(View.GONE);
      // mViewHolder.edtCommentLay.setVisibility(View.GONE);
      if (mViewHolder.enterCommentrellay.getVisibility() == View.VISIBLE) {
        mViewHolder.commentSend.setVisibility(View.GONE);
      }
      mViewHolder.enterCommentrellay.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          mViewHolder.edtCommentLay.setVisibility(View.VISIBLE);
          mViewHolder.enterCommentrellay.setVisibility(View.GONE);
          mswipeListner.onAddCommentClick(mViewHolder.enterCommentrellay,
              mViewHolder.edtComment, mViewHolder.commentSend,
              mViewHolder.edtCommentLay);

        }
      });
    }

      else {
    	  //first two views are image and comment layout. so use position - 2 to display all the comments from start
    	  if(position -1 < mShowingList.size()) {
         mViewHolder.userComment.setText(mShowingList.get(position - 2));
    	  }
         mViewHolder.imgContentlay.setVisibility(View.GONE);
         mViewHolder.vueCommentheader.setVisibility(View.GONE);
         mViewHolder.addCommentlay.setVisibility(View.GONE);
         mViewHolder.separator.setVisibility(View.VISIBLE);
         if (position == mListCount - 2) {
            mViewHolder.separator.setVisibility(View.GONE);
         }

      }
      mViewHolder.exapandHolder.setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
            // mswipeListner.onResetAdapter();
        	 int showFixedRowCount = 3;
        	 if(mListCount == (showFixedRowCount +2)) {
        		 mListCount = mShowingList.size()+showFixedRowCount;
        	 } else {
        		 mListCount = showFixedRowCount +2;
        	 }
           
            notifyDataSetChanged();
         }
      });
      mViewHolder.vueWindowBookmarkImg.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mIsBookImageClciked = true;
			if(mWindowContentTemp.getWindowBookmarkIndicator()) {
				mBookmarksCount--;
				mWindowContentTemp.setmAisleBookmarksCount(mBookmarksCount);
				mWindowContentTemp.setWindowBookmarkIndicator(false);
			} else {
				mBookmarksCount++;
				mWindowContentTemp.setmAisleBookmarksCount(mBookmarksCount);
				mWindowContentTemp.setWindowBookmarkIndicator(true);
			}
			/*if(mWindowContentTemp.getmAisleBookmarksCount() == mBookmarksCount) {
				mBookmarksCount++;
				} else {
					mBookmarksCount = mWindowContentTemp.getmAisleBookmarksCount();
				}*/
			notifyDataSetChanged();
		}
	});
      mViewHolder.bookMarkCount.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mIsBookImageClciked = true;
			if(mWindowContentTemp.getWindowBookmarkIndicator()) {
				mBookmarksCount--;
				mWindowContentTemp.setmAisleBookmarksCount(mBookmarksCount);
				mWindowContentTemp.setWindowBookmarkIndicator(false);
			} else {
				mBookmarksCount++;
				mWindowContentTemp.setmAisleBookmarksCount(mBookmarksCount);
				mWindowContentTemp.setWindowBookmarkIndicator(true);
			}
		/*	if(mWindowContentTemp.getmAisleBookmarksCount() == mBookmarksCount) {
				mBookmarksCount++;
				} else {
					mBookmarksCount = mWindowContentTemp.getmAisleBookmarksCount();
				}*/
				notifyDataSetChanged(); 
			
		}
	});

      return convertView;
   }
	private void notifyAdapter() {
		this.notifyDataSetChanged();
	}

	public void share(final Context context, Activity activity) {
		mShare = new ShareDialog(context, activity);
		FileCache ObjFileCache = new FileCache(context);
		ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
		if (mWindowContentTemp.getImageList() != null
				&& mWindowContentTemp.getImageList().size() > 0) {
			for (int i = 0; i < mWindowContentTemp.getImageList().size(); i++) {
				clsShare obj = new clsShare(mWindowContentTemp.getImageList()
						.get(i).mCustomImageUrl,
						ObjFileCache
								.getFile(
										mWindowContentTemp.getImageList().get(
												i).mCustomImageUrl).getPath());
				imageUrlList.add(obj);
			}
			mShare.share(
					imageUrlList,
					mWindowContentTemp.getAisleContext().mOccasion,
					(mWindowContentTemp.getAisleContext().mFirstName + " " + mWindowContentTemp
							.getAisleContext().mLastName));
		}
		if (mWindowContentTemp.getImageList() != null
				&& mWindowContentTemp.getImageList().size() > 0) {
			FileCache ObjFileCache1 = new FileCache(context);
			for (int i = 0; i < mWindowContentTemp.getImageList().size(); i++) {
				final File f = ObjFileCache1.getFile(mWindowContentTemp
						.getImageList().get(i).mCustomImageUrl);
				if (!f.exists()) {
					@SuppressWarnings("rawtypes")
					Response.Listener listener = new Response.Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap bmp) {
							Utils.saveBitmap(bmp, f);
						}
					};
					Response.ErrorListener errorListener = new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.e(TAG, arg0.getMessage());
						}
					};
					@SuppressWarnings("unchecked")
					ImageRequest imagerequestObj = new ImageRequest(
							mWindowContentTemp.getImageList().get(i).mCustomImageUrl,
							listener, 0, 0, null, errorListener);
					VueApplication.getInstance().getRequestQueue()
							.add(imagerequestObj);
				}
			}
		}
	} 
 


   /**
    * 
    *  
    *To handle the click and long press event on the imageview in the aisle content
    *and to allow only one like and one dislike allows
    */
	private class DetailImageClickListener implements DetailClickListener {
		@Override
		public void onImageClicked() {
			onHandleLikeEvent();
		}

		@Override
		public void onImageLongPress() {
			onHandleDisLikeEvent();
		}

		@Override
		public void onImageSwipe(int position) {
			mCurrentDispImageIndex = position;
			int likeCount = 0;
			if (position >= 0
					&& position < VueApplication.getInstance()
							.getClickedWindowCount()) {
				@SuppressWarnings("unchecked")
				ArrayList<String> tempCommentList = (ArrayList<String>) mCommentsMapList.get(position);
				if(tempCommentList != null) {
					mShowingList = tempCommentList;
				}
				likeCount = mTempInitialImageLikeCounts[position];
				mLikes = likeCount;
				notifyDataSetChanged();
			} else {
				return;
			}
		}

	}

	private void ratingImage() {
		if (mallowLike) {
			mLikes += 1;
			mallowLike = false;
			mallowDisLike = true;
		} else if (mLikes != 0 && mallowDisLike) {
			mLikes -= 1;
			mallowLike = true;
			mallowDisLike = false;
		}
		mIsLikeImageClciked = true;
		notifyAdapter();
	}

	public void changeLikesCount(int position, String eventType) {
		if (eventType.equalsIgnoreCase(AisleDetailsViewActivity.CLICK_EVENT)) {
			// increase the like count
			int initalLikesCount = mInitialImageLikeCounts[position];
			int presentLikesCount = mTempInitialImageLikeCounts[position];
			if (presentLikesCount == initalLikesCount
					|| presentLikesCount == initalLikesCount - 1) {
				mTempInitialImageLikeCounts[position] = mTempInitialImageLikeCounts[position] + 1;
				sendDataToDb(mAisleWindowId, position,
						mTempInitialImageLikeCounts[position]);
			}
			if (position == mCurrentDispImageIndex) {
				mLikes = mTempInitialImageLikeCounts[position];
				notifyAdapter();
			}
		} else {
			// decrease the like count
			int initalLikesCount = mInitialImageLikeCounts[position];
			int presentLikesCount = mTempInitialImageLikeCounts[position];
			if (presentLikesCount == initalLikesCount
					|| presentLikesCount == initalLikesCount + 1) {
				mTempInitialImageLikeCounts[position] = mTempInitialImageLikeCounts[position] - 1;
				sendDataToDb(mAisleWindowId, position,
						mTempInitialImageLikeCounts[position]);
			}
			if (position == mCurrentDispImageIndex) {
				mLikes = mTempInitialImageLikeCounts[position];
				notifyAdapter();
			}
		}
	}

	private void onHandleLikeEvent() {
		// increase the likes count
		if (mCurrentDispImageIndex >= 0
				&& mCurrentDispImageIndex < mInitialImageLikeCounts.length) {
			int initalLikesCount = mInitialImageLikeCounts[mCurrentDispImageIndex];
			int presentLikesCount = mTempInitialImageLikeCounts[mCurrentDispImageIndex];
			if (presentLikesCount == initalLikesCount
					|| presentLikesCount == initalLikesCount - 1) {
				mLikes += 1;
				mTempInitialImageLikeCounts[mCurrentDispImageIndex] = mLikes;
				sendDataToDb(mAisleWindowId, mCurrentDispImageIndex, mLikes);
			}
			mIsLikeImageClciked = true;
			notifyAdapter();
		}
	}

	private void onHandleDisLikeEvent() {
		//decrease the likes count
		if(mCurrentDispImageIndex>= 0 && mCurrentDispImageIndex < mInitialImageLikeCounts.length) {
		mIsLikeImageClciked = true;
		int initalLikesCount = mInitialImageLikeCounts[mCurrentDispImageIndex];
		int presentLikesCount = mTempInitialImageLikeCounts[mCurrentDispImageIndex];
		if (presentLikesCount == initalLikesCount
				|| presentLikesCount == initalLikesCount + 1) {
			mLikes -= 1;
			mTempInitialImageLikeCounts[mCurrentDispImageIndex] = mLikes;
			sendDataToDb(mAisleWindowId,mCurrentDispImageIndex,mLikes);
		}
		notifyAdapter();
		}
	}
	public void setAisleBrowerObjectsNull(){
		 Log.i("returnsused imageview", "returnsused imageview1: "+ mViewHolder.aisleContentBrowser.getCustomAdapter());
		if(mViewHolder != null && mViewHolder.aisleContentBrowser != null) {
			 Log.i("returnsused imageview", "returnsused imageview2"+ mViewHolder.aisleContentBrowser.getChildCount());
			 
			
			// mContentAdapterFactory.returnUsedAdapter(mViewHolder.aisleContentBrowser.getCustomAdapter());
			  for(int i=0;i< mViewHolder.aisleContentBrowser.getChildCount();i++){
	                //((ScaleImageView)contentBrowser.getChildAt(i)).setContainerObject(null);
	                mViewFactory.returnUsedImageView((ScaleImageView) mViewHolder.aisleContentBrowser.getChildAt(i));
	                Log.i("returnsused imageview", "returnsused imageview");
	            }
			  mContentAdapterFactory.returnUsedAdapter(mViewHolder.aisleContentBrowser.getCustomAdapter());
		mViewHolder.aisleContentBrowser.setReferedObjectsNull();
		mViewHolder.aisleContentBrowser.removeAllViews();
		}
	}
	public void sendDataToDb(String windowId,int imgPosition,int likesCount) {
		
	}
}
