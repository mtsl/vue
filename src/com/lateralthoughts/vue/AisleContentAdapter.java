package com.lateralthoughts.vue;

//android imports
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.volley.toolbox.ImageLoader;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.*;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//java imports
//internal imports

/**
 * The AisleContentAdapter object will be associated with aisles on a per-aisle
 * basis. Within each aisle's content, this object will manage resources such as
 * determining the number of images per aisle that need to be kept in memory
 * when to fetch these images etc. Here is the general outline: The AisleLoader
 * class is associated with the TrendingAislesPage and will help load up the
 * first images in each of the ViewFlipper. During startup, we don't want to
 * consume more resources (threads, CPU cycles) to download content that is not
 * visible right away. For example, even though an aisle has multiple images
 * these images are visible one at a time. The AisleContentAdapter will instead
 * perform this function. At startup, this object will download either max of N
 * items or the most number of items in the aisle - whichever is lowest. Say an
 * aisle contains 10 images, we will download just N where N is set to 1 or 2 or
 * other number based on trial & error. When the user swipes through images and
 * the ViewFlipper instance will seek the next item from this adapter. At this
 * point, we will adjust the currentPivotIndex and ensure that we have 1 image
 * on either side readily available in the content cache. This should help with
 * performance and also optimize how much we need to do during startup time.
 */
public class AisleContentAdapter implements IAisleContentAdapter {

	// private VueMemoryCache<Bitmap> mContentImagesCache;
	private BitmapLruCache mContentImagesCache;
 
    private ArrayList<AisleImageDetails> mAisleImageDetails;
    private AisleWindowContent mWindowContent;
    
    private ExecutorService mExecutorService;
    private Context mContext;
    private FileCache mFileCache;
    private ScaledImageViewFactory mImageViewFactory;
    private ColorDrawable mColorDrawable;
    private int mCurrentPivotIndex;
    private BitmapLoaderUtils mBitmapLoaderUtils;
   // public String mSourceName;
    private ImageDimension mImageDimension;
    Animation myFadeInAnimation;
    
    public AisleContentAdapter(Context context){
        mContext = context;
        //mContentImagesCache = VueApplication.getInstance().getAisleContentCache();
        mContentImagesCache = BitmapLruCache.getInstance(VueApplication.getInstance());
        mFileCache = VueApplication.getInstance().getFileCache();
        mCurrentPivotIndex = -1;
        mImageViewFactory  = ScaledImageViewFactory.getInstance(mContext);     
        mExecutorService = Executors.newFixedThreadPool(5);
        mColorDrawable = new ColorDrawable(Color.WHITE);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        myFadeInAnimation = AnimationUtils.loadAnimation(VueApplication.getInstance(), R.anim.fadein);
    }
    
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    @Override
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent) {
        // TODO Auto-generated method stub
        mWindowContent = windowContent;
        mAisleImageDetails = mWindowContent.getImageList();
        
        
        //lets file cache the first two items in the list
       // queueImagePrefetch(mAisleImageDetails, mWindowContent.getBestHeightForWindow(), 1,2);
    }
    
    @Override
    public void releaseContentSource() {
        // TODO Auto-generated method stub
        mCurrentPivotIndex = -1;
        mAisleImageDetails.clear();
        mWindowContent = null;
        
    }

    @Override
    public ScaleImageView getItemAt(int index, boolean isPivot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPivot(int index) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void registerAisleDataObserver(IAisleDataObserver observer){
        
    }
    
    @Override
    public void unregisterAisleDataObserver(IAisleDataObserver observer){
    }
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    
    
    public void queueImagePrefetch(ArrayList<AisleImageDetails> imageList, int bestHeight, int startIndex, int count){
        BitmapsToFetch p = new BitmapsToFetch(imageList, bestHeight, startIndex, count);
        mExecutorService.submit(new ImagePrefetcher(p));
    }
/*    public void setSourceName(String name) {
    	mSourceName = name;
    }
    public String getSourceName(){
    	return mSourceName;
    }*/
    //Task for the queue
    private class BitmapsToFetch
    {
        public ArrayList<AisleImageDetails> mImagesList;
        public int mStartIndex;
        private int mCount;
        public int mBestHeight;
        
        public BitmapsToFetch(ArrayList<AisleImageDetails> imagesList, int bestHeight, int startIndex, int count){
            mImagesList = imagesList; 
            mStartIndex = startIndex;
            mCount = count;
            mBestHeight = bestHeight;
        }
    }
    
    class ImagePrefetcher implements Runnable {
        BitmapsToFetch mBitmapsToFetch;
        ImagePrefetcher(BitmapsToFetch details){
            this.mBitmapsToFetch = details;
        }
        
        @Override
        public void run() {
            int startIndex = mBitmapsToFetch.mStartIndex;
            int count = mBitmapsToFetch.mCount;
            if(count+startIndex >= mBitmapsToFetch.mImagesList.size())
                return;
            
            for(int i = startIndex; i<count+startIndex; i++){
                //Log.e("FileCacher","about to cache file for index = " + mBitmapsToFetch.mImagesList.get(i).mCustomImageUrl);
                cacheBitmapToLocal(mBitmapsToFetch.mImagesList.get(i).mCustomImageUrl,  mBitmapsToFetch.mImagesList.get(i).mImageUrl, mBitmapsToFetch.mBestHeight);
            }
        }
    }
    
    
    public void cacheBitmapToLocal(String url, String serverUrl, int bestHeight) 
    {
        File f = mFileCache.getFile(url);        
        //from SD cache
        if(isBitmapCachedLocally(f, bestHeight)){
            return;
        }
        
        //from web
        try {
            URL imageUrl = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            return;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError) {
        	   mContentImagesCache.evictAll();
           }
           return;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private boolean isBitmapCachedLocally(File f, int bestHeight){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();
            
            //Find the correct scale value. It should be the power of 2.
            //final int REQUIRED_SIZE = mScreenWidth/2;
            int height=o.outHeight;
            int scale = 1;

            if (height > bestHeight) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) height / (float) bestHeight);
               // final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                scale = heightRatio; // < widthRatio ? heightRatio : widthRatio;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return true;
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isBitmapCached(String url){
        if(null != mContentImagesCache.get(url)){
            return true;
        }else{
            return false;
        }
    }
    
    public int getAisleItemsCount(){
        return mAisleImageDetails.size();
    }
    
    @Override
    public boolean setAisleContent(AisleContentBrowser contentBrowser, int currentIndex, int wantedIndex, 
                                            boolean shiftPivot){
        ScaleImageView imageView = null;
        AisleImageDetails itemDetails = null;
        if(wantedIndex >= mAisleImageDetails.size())
            return false;
        if(0 >= currentIndex && wantedIndex < currentIndex)
            return false;
        if(null != mAisleImageDetails && mAisleImageDetails.size() != 0){         
            itemDetails = mAisleImageDetails.get(wantedIndex);
            imageView = mImageViewFactory.getEmptyImageView();
            Bitmap bitmap = null;
            if(contentBrowser.getmSourceName()!= null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            	 bitmap = getCachedBitmap(itemDetails.mImageUrl);
            } else {
             // bitmap = getCachedBitmap(itemDetails.mCustomImageUrl);
              Log.i("imageResize", "imageResize custom from cache 1");
               
            }
            if(bitmap != null){
            	 if(contentBrowser.getmSourceName() != null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            			mImageDimension = Utils.getScalledImage(bitmap, itemDetails.mAvailableWidth, itemDetails.mAvailableHeight);
            		/*	FrameLayout.LayoutParams mShowpieceParams = new FrameLayout.LayoutParams(
    							VueApplication.getInstance().getScreenWidth() ,
    							bitmap.getHeight());
                		contentBrowser .setLayoutParams(mShowpieceParams);*/
            			if(bitmap.getHeight() < mImageDimension.mImgHeight) {
            				 Log.i("detailscrop", "detailscrop resize agian");
            				   loadBitmap(itemDetails,mImageDimension.mImgHeight,contentBrowser, imageView);
            			}
            	 }
            	
            
                imageView.setImageBitmap(bitmap);
                	contentBrowser.addView(imageView);
                 
            }
            else{
            	if(contentBrowser.getmSourceName() != null && contentBrowser.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            		int bestHeight = mWindowContent.getBestLargetHeightForWindow();
            		loadBitmap(itemDetails,bestHeight,contentBrowser, imageView);
                contentBrowser.addView(imageView);
            	} else {
            		int bestHeight = mWindowContent.getBestHeightForWindow();
            		 loadBitmap(itemDetails,itemDetails.mTrendingImageHeight,contentBrowser, imageView);
                           	contentBrowser.addView(imageView);
                            
            	}
            }
        }
        return true;
    }
    
    public Bitmap getCachedBitmap(String url){
        return mContentImagesCache.get(url);      
    }
    
    public void loadBitmap( AisleImageDetails itemDetails, int bestHeight, AisleContentBrowser flipper, ImageView imageView) {
    	String loc = itemDetails.mImageUrl;
    	String serverUrl = itemDetails.mImageUrl;
    	 if(flipper.getmSourceName() != null && flipper.getmSourceName().equalsIgnoreCase(AisleDetailsViewAdapter.TAG)){
    		 loc = itemDetails.mImageUrl;
    	 } else {
    		 loc = itemDetails.mCustomImageUrl;
    	 }

     /*   ((ScaleImageView) imageView).setImageUrl(loc,
                new ImageLoader(VueApplication.getInstance().getRequestQueue(), VueApplication.getInstance().getBitmapCache()));*/

            BitmapWorkerTask task = new BitmapWorkerTask(itemDetails,flipper, imageView, bestHeight);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            task.execute(loc,serverUrl);
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<AisleContentBrowser>viewFlipperReference;
        private String url = null;
        private int mBestHeightForImage;
        AisleContentBrowser aisleContentBrowser ;
        private int mAVailableWidth,mAvailabeHeight;
        AisleImageDetails mItemDetails;

        public BitmapWorkerTask( AisleImageDetails itemDetails,AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            viewFlipperReference = new WeakReference<AisleContentBrowser>(vFlipper); 
            mBestHeightForImage = bestHeight;
            aisleContentBrowser = vFlipper;
            mAVailableWidth = itemDetails.mAvailableWidth;
            mAvailabeHeight = itemDetails.mAvailableHeight;
            mItemDetails = itemDetails;
        }

        // Decode image in background.
        @Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			Bitmap bmp = null;
			// we want to get the bitmap and also add it into the memory cache
			// bmp = getBitmap(url, true, mBestHeightForImage);
			if (aisleContentBrowser.getmSourceName() != null
					&& aisleContentBrowser.getmSourceName().equalsIgnoreCase(
							AisleDetailsViewAdapter.TAG)) {
				boolean cacheval = false;
				bmp = mBitmapLoaderUtils
						.getBitmap(url, params[1], cacheval, mBestHeightForImage,
								VueApplication.getInstance()
										.getVueDetailsCardWidth(),
								Utils.DETAILS_SCREEN);
				Log.i("imagenotshowing", "imagenotshowing: "+bmp);
				if(bmp != null){
					Log.i("imagenotshowing", "imagenotshowing: height: "+bmp.getHeight());
				}
				
		/*		if (bmp != null) {
					mImageDimension = Utils.getScalledImage(bmp,
							mAVailableWidth, mAvailabeHeight);
					mAvailabeHeight = mImageDimension.mImgHeight;
					if (bmp.getHeight() > mImageDimension.mImgHeight) {
						bmp = mBitmapLoaderUtils.getBitmap(url, params[1],
								cacheval, mImageDimension.mImgHeight,
								VueApplication.getInstance()
										.getVueDetailsCardWidth(),
								Utils.DETAILS_SCREEN);
					}
				}*/
				mItemDetails.mTempResizedBitmapHeight = bmp.getHeight();
				mItemDetails.mTempResizeBitmapwidth = bmp.getWidth();
	 

			} else {
				boolean cacheval = false;
				Log.i("imagenotshowing", "imagenotshowing: Custom url "+url);
				Log.i("imagenotshowing", "imagenotshowing: Server url "+ params[1]);
				bmp = mBitmapLoaderUtils.getBitmap(url, params[1], cacheval,
						mItemDetails.mTrendingImageHeight,mItemDetails.mTrendingImageWidth,
						Utils.DETAILS_SCREEN);
				Log.i("imagenotshowing", "imagenotshowing: "+bmp);
				if(bmp != null){
					Log.i("imagenotshowing", "imagenotshowing: height: "+bmp.getHeight());
				}
				 
			}
			return bmp;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (viewFlipperReference != null && imageViewReference != null
					&& bitmap != null) {

				final ImageView imageView = imageViewReference.get();
				final AisleContentBrowser vFlipper = viewFlipperReference.get();
				BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

				if (this == bitmapWorkerTask) {
					vFlipper.invalidate();
					imageView.setImageBitmap(bitmap);
					imageView.startAnimation(myFadeInAnimation);
					VueTrendingAislesDataModel.getInstance(
							VueApplication.getInstance()).dataObserver();
				}
			}
		}
	}

	// utility functions to keep track of all the async tasks that we
	// instantiate
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			Object task = ((ScaleImageView) imageView).getOpaqueWorkerObject();
			if (task instanceof BitmapWorkerTask) {
				BitmapWorkerTask workerTask = (BitmapWorkerTask) task;
				return workerTask;
			}
		}
		return null;
	}

	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			String bitmapUrl = bitmapWorkerTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapWorkerTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/*
	 * This function is strictly for use by internal APIs. Not that we have
	 * anything external but there is some trickery here! The getBitmap function
	 * cannot be invoked from the UI thread. Having to deal with complexity of
	 * when & how to call this API is too much for those who just want to have
	 * the bitmap. This is a utility function and is public because it is to be
	 * shared by other components in the internal implementation.
	 */
	public Bitmap getBitmap(String url, String serverUrl, boolean cacheBitmap,
			int bestHeight) {
		File f = mFileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f, bestHeight);
		if (b != null) {
			// Log.e("AisleContentAdapter","found file in file cache");
			mContentImagesCache.put(url, b);
			return b;
		}

		// from web
		try {
			Bitmap bitmap = null;
			System.setProperty("http.keepAlive", "false");
			URL imageUrl = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, bestHeight);
			// if(cacheBitmap)
			mContentImagesCache.put(url, bitmap);

			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				mContentImagesCache.evictAll();
			}
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f, int bestHeight) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = mScreenWidth/2;
			int height = o.outHeight;
			int scale = 1;
			bestHeight = mWindowContent.getBestHeightForWindow();
			if (height > bestHeight) {

				// Calculate ratios of height and width to requested height and
				// width
				final int heightRatio = Math.round((float) height
						/ (float) bestHeight);
				// final int widthRatio = Math.round((float) width / (float)
				// reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will
				// guarantee
				// a final image with both dimensions larger than or equal to
				// the
				// requested height and width.
				scale = heightRatio; // < widthRatio ? heightRatio : widthRatio;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createLayout(ScaleImageView image,
			AisleContentBrowser contentBrowser, int id) {
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params2.addRule(RelativeLayout.CENTER_IN_PARENT);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				VueApplication.getInstance().getPixel(32), VueApplication
						.getInstance().getPixel(32));
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		RelativeLayout imageParent = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams paramsRel = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		imageParent.setLayoutParams(paramsRel);
		image.setLayoutParams(params2);
		imageParent.addView(image);
		ImageView thumbImage = new ImageView(mContext);
		thumbImage.setLayoutParams(params);
		thumbImage.setImageResource(R.drawable.thumb_up);
		thumbImage.setVisibility(View.INVISIBLE);
		imageParent.addView(thumbImage);
		contentBrowser.addView(imageParent);
	}
}
