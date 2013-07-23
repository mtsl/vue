/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
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
   public int mInitialImageLikeCounts[];
   public int mTempInitialImageLikeCounts[];
   public int mCurrentDispImageIndex;
   private boolean mallowLike = true,mallowDisLike = true;
   private boolean isImageClciked = false;
   private AisleWindowContent mWindowContentTemp;
   public String mVueusername;
   ShareDialog mShare ;
   private String mAisleWindowId;

   ViewHolder mViewHolder;
   String mTempComments[] = {
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous.",
           };
   String mTempComments2[] = {   "Love love love the dress! Simple and fabulous.",
         "Love love love the dress! Simple and fabulous."};
   ViewHolder mHolder;

   public AisleDetailsViewAdapter(Context c,
         AisleDetailSwipeListener swipeListner, int listCount,
         ArrayList<AisleWindowContent> content) {
      super(c, content);
      mContext = c;
      mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
      mswipeListner = swipeListner;
      mListCount = listCount;
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
      ArrayList<AisleImageDetails> imageDetailsArr = mWindowContentTemp
          .getImageList();

      mInitialImageLikeCounts = new int[imageDetailsArr.size()];
      mTempInitialImageLikeCounts = new int[imageDetailsArr.size()];
      for (int i = 0; i < imageDetailsArr.size(); i++) {
        mInitialImageLikeCounts[i] = imageDetailsArr.get(i).mLikesCount;
        mTempInitialImageLikeCounts[i] = imageDetailsArr.get(i).mLikesCount;
        if (mInitialImageLikeCounts[i] == 0) {
          // 5 is the temporary   count 
          mInitialImageLikeCounts[i] = 5;
          mTempInitialImageLikeCounts[i] = 5;
        }
      }
    }
   }

   @Override
   public AisleWindowContent getItem(int position) {
      return mVueTrendingAislesDataModel.getAisleAt(position);
   }
   static class ViewHolder {
      AisleContentBrowser aisleContentBrowser;
      HorizontalScrollView thumbnailContainer;
      // LinearLayout thumbnailScroller;
      TextView aisleDescription;
      TextView aisleOwnersName;
      TextView aisleContext, commentCount, likeCount;
      TextView bookMarkCount;
      ImageView profileThumbnail;
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
            mViewLoader.getAisleContentIntoView(mViewHolder, scrollIndex,
                  position, new DetailImageClickListener());
         } catch (Exception e) {
            e.printStackTrace();
         }
         // gone comment layoutgone
      } else if (position == 1) {
         if (isImageClciked) {
            isImageClciked = false;
            Animation rotate = AnimationUtils.loadAnimation(mContext,
                  R.anim.bounce);
            mViewHolder.likeImg.startAnimation(rotate);
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
         mViewHolder.userComment.setText(mTempComments2[position - 2]);
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
            if (mTempComments2.length <= 2) {
               mTempComments2 = new String[mTempComments.length];
               for (int i = 0; i < mTempComments.length; i++) {
                  mTempComments2[i] = mTempComments[i];
               }
               mListCount = mTempComments2.length;
            } else {
               mTempComments2 = new String[2];
               for (int i = 0; i < 2; i++) {
                  mTempComments2[i] = mTempComments[i];
               }
               mListCount = 5;
            }
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
		isImageClciked = true;
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
			isImageClciked = true;
			notifyAdapter();
		}
	}

	private void onHandleDisLikeEvent() {
		//decrease the likes count
		if(mCurrentDispImageIndex>= 0 && mCurrentDispImageIndex < mInitialImageLikeCounts.length) {
		isImageClciked = true;
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
		if(mViewHolder != null && mViewHolder.aisleContentBrowser != null) {
			// mContentAdapterFactory.returnUsedAdapter(mViewHolder.aisleContentBrowser.getCustomAdapter());
			
		mViewHolder.aisleContentBrowser.setReferedObjectsNull();
		mViewHolder.aisleContentBrowser.removeAllViews();
		}
	}
	private void sendDataToDb(String windowId,int imgPosition,int likesCount) {
		
	}
}
