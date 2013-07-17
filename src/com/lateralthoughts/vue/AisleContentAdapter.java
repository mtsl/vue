package com.lateralthoughts.vue;

//android imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

//java imports
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ScaledImageViewFactory;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.ImageDimention;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.VueMemoryCache;
import com.lateralthoughts.vue.utils.FileCache;

/**
 * The AisleContentAdapter object will be associated with aisles on a per-aisle
 * basis. Within each aisle's content, this object will manage resources such
 * as determining the number of images per aisle that need to be kept in memory
 * when to fetch these images etc.
 * Here is the general outline:
 * The AisleLoader class is associated with the TrendingAislesPage and will help
 * load up the first images in each of the ViewFlipper.
 * During startup, we don't want to consume more resources (threads, CPU cycles) to
 * download content that is not visible right away. For example, even though an aisle
 * has multiple images these images are visible one at a time.
 * The AisleContentAdapter will instead perform this function. At startup, this object
 * will download either max of N items or the most number of items in the aisle - whichever
 * is lowest. Say an aisle contains 10 images, we will download just N where N is set to 1 or 2
 * or other number based on trial & error.
 * When the user swipes through images and the ViewFlipper instance will seek the next item from
 * this adapter. At this point, we will adjust the currentPivotIndex and ensure that we have 1 image
 * on either side readily available in the content cache. This should help with performance and also
 * optimize how much we need to do during startup time.
 */
public class AisleContentAdapter implements IAisleContentAdapter {

    private VueMemoryCache<Bitmap> mContentImagesCache;
    private ArrayList<AisleImageDetails> mAisleImageDetails;
    private AisleWindowContent mWindowContent;
    
    private ExecutorService mExecutorService;
    private Context mContext;
    private FileCache mFileCache;
    private ScaledImageViewFactory mImageViewFactory;
    private ColorDrawable mColorDrawable;
    private int mCurrentPivotIndex;
    private BitmapLoaderUtils mBitmapLoaderUtils;
    private String mSourceName;
    private ImageDimention mImageDimention;
    
    public AisleContentAdapter(Context context){
        mContext = context;
        mContentImagesCache = VueApplication.getInstance().getAisleContentCache();
        mFileCache = VueApplication.getInstance().getFileCache();
        mCurrentPivotIndex = -1;
        mImageViewFactory  = ScaledImageViewFactory.getInstance(mContext);     
        mExecutorService = Executors.newFixedThreadPool(5);
        mColorDrawable = new ColorDrawable(Color.WHITE);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(mContext);
    }
    
    //========================= Methods from the inherited IAisleContentAdapter ========================//
    @Override
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent) {
        // TODO Auto-generated method stub
        mWindowContent = windowContent;
        mAisleImageDetails = mWindowContent.getImageList();
        
        
        //lets file cache the first two items in the list
        queueImagePrefetch(mAisleImageDetails, mWindowContent.getBestHeightForWindow(), 1,2);
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
    public void setSourceName(String name) {
    	mSourceName = name;
    }
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
                cacheBitmapToLocal(mBitmapsToFetch.mImagesList.get(i).mCustomImageUrl, mBitmapsToFetch.mBestHeight);
            }
        }
    }
    
    
    public void cacheBitmapToLocal(String url, int bestHeight) 
    {
        File f = mFileCache.getFile(url);        
        //from SD cache
        if(isBitmapCachedLocally(f, bestHeight)){
            return;
        }
        
        //from web
        try {
            URL imageUrl = new URL(url);
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
           if(ex instanceof OutOfMemoryError)
               mContentImagesCache.clear();
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
    public boolean setAisleContent(AisleContentBrowser contentBrowser,ScaleImageView reuseView, int currentIndex, int wantedIndex, 
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
            Bitmap bitmap = getCachedBitmap(itemDetails.mCustomImageUrl);
      
            
            if(bitmap != null){
            	 if(mSourceName != null && mSourceName.equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
                		mImageDimention = Utils.getScalledImage(bitmap, itemDetails.mAvailableWidth, itemDetails.mAvailableHeight);
                  	 }
                //Log.e("AisleContentAdapter","bitmap present. imageView = " + imageView);
            	//setParams(contentBrowser,imageView,bitmap);
            	 if(mSourceName != null && mSourceName.equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
            		 if(bitmap.getHeight() < mImageDimention.mImgHeight) {
            			 bitmap =  mBitmapLoaderUtils.getBitmap(itemDetails.mImageUrl, true, mImageDimention.mImgHeight);
            		 }
                
            	 }
                imageView.setImageBitmap(bitmap);
                contentBrowser.addView(imageView);
            }
            else{
                loadBitmap(itemDetails,itemDetails.mAvailableHeight,contentBrowser, imageView);
                contentBrowser.addView(imageView);
            }
        }
        return true;
    }
    
    public Bitmap getCachedBitmap(String url){
        return mContentImagesCache.get(url);      
    }
    
    public void loadBitmap( AisleImageDetails itemDetails, int bestHeight, AisleContentBrowser flipper, ImageView imageView) {
    	String loc = itemDetails.mImageUrl;
        if (cancelPotentialDownload(loc, imageView)) {          
            BitmapWorkerTask task = new BitmapWorkerTask(itemDetails,flipper, imageView, bestHeight);
            ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
            task.execute(loc);
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<AisleContentBrowser>viewFlipperReference;
        private String url = null;
        private int mBestHeightForImage;
        AisleContentBrowser aisleContentBrowser ;
        private int mAVailableWidth,mAvailabeHeight;

        public BitmapWorkerTask( AisleImageDetails itemDetails,AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            viewFlipperReference = new WeakReference<AisleContentBrowser>(vFlipper); 
            mBestHeightForImage = bestHeight;
            aisleContentBrowser = vFlipper;
            mAVailableWidth = itemDetails.mAvailableWidth;
            mAvailabeHeight = itemDetails.mAvailableHeight;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            //we want to get the bitmap and also add it into the memory cache
            //bmp = getBitmap(url, true, mBestHeightForImage); 
            bmp = mBitmapLoaderUtils.getBitmap(url, true, mBestHeightForImage);
			if (bmp != null) {
				if (mSourceName != null
						&& mSourceName
								.equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
					mImageDimention = Utils.getScalledImage(bmp,
							mAVailableWidth, mAvailabeHeight);

					if (bmp.getHeight() < mImageDimention.mImgHeight) {
						bmp = mBitmapLoaderUtils.getBitmap(url, true,
								mImageDimention.mImgHeight);
				     	 Log.i("bitmapsize", "bitmapsize after12 width: "+bmp.getWidth());
						  Log.i("bitmapsize", "bitmapsize after12 height: "+bmp.getHeight());
					} 

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
					// bitmap = setParams(aisleContentBrowser, imageView,
					// bitmap);
					/* if(mSourceName != null && mSourceName.equalsIgnoreCase(AisleDetailsViewAdapter.TAG)) {
			                bitmap = Utils.getScalledImage(bitmap,mAVailableWidth, mAvailabeHeight);
			            	 }*/
					imageView.setImageBitmap(bitmap);
				}
			}
		}
    }
    
    //utility functions to keep track of all the async tasks that we instantiate
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Object task = ((ScaleImageView)imageView).getOpaqueWorkerObject();
            if (task instanceof BitmapWorkerTask) {
                BitmapWorkerTask workerTask = (BitmapWorkerTask)task;
                return workerTask;
            }
        }
        return null;
    }
    
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
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
     * This function is strictly for use by internal APIs. Not that we have anything external but
     * there is some trickery here! The getBitmap function cannot be invoked from the UI thread.
     * Having to deal with complexity of when & how to call this API is too much for those who
     * just want to have the bitmap. This is a utility function and is public because it is to 
     * be shared by other components in the internal implementation.   
     */
    public Bitmap getBitmap(String url, boolean cacheBitmap, int bestHeight) 
    {
        File f = mFileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f, bestHeight);
        if(b != null){
            //Log.e("AisleContentAdapter","found file in file cache");
            mContentImagesCache.put(url, b);
            return b;
        }
        
        //from web
        try {
            Bitmap bitmap=null;
            System.setProperty("http.keepAlive", "false");
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, bestHeight);
            //if(cacheBitmap)
                mContentImagesCache.put(url, bitmap);
            
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               mContentImagesCache.clear();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
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
    
    
    /*private Bitmap setParams(AisleContentBrowser vFlipper,ImageView imageView,Bitmap bitmap) {
    	Log.i("width & height", "reqWidth1: Bitmap is greater than card size0" );
    	int imgCardHeight =   (VueApplication.getInstance().getScreenHeight() *60) /100;
    	FrameLayout.LayoutParams showpieceParams = new FrameLayout.LayoutParams(
				VueApplication.getInstance().getScreenWidth(),imgCardHeight);
    	showpieceParams.setMargins(0, 50, 0, 50);
    	if(vFlipper != null)
    	vFlipper.setLayoutParams(showpieceParams);
    	  if(bitmap.getHeight() > imgCardHeight || bitmap.getWidth() >VueApplication.getInstance().getScreenWidth() ) {
    	 Log.i("width & height", "reqWidth1: Bitmap is less than card size" );
    		 bitmap =  Utils.getScaledBitMap(bitmap, ((VueApplication.getInstance().getScreenWidth()*90)/100), (imgCardHeight*90)/100);
    	  } else {
    		  Log.i("width & height", "reqWidth1: Bitmap is less than card size" );
    	  }
    	if(vFlipper != null) {
    	FrameLayout.LayoutParams params = 
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
    	} else {
    		LinearLayout.LayoutParams params = 
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            //params.setMargins(100, 0, 100, 100);
             imageView.setScaleType(ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(params);
            Log.i("width & height", "reqWidth1: Bitmap is greater than card size1" );
            if(bitmap.getHeight() >  VueApplication.getInstance().getScreenHeight()/3 || bitmap.getWidth() >VueApplication.getInstance().getScreenWidth() ) {
      		  Log.i("width & height", "reqWidth1: Bitmap is greater than card size2" );
      		 bitmap =  Utils.getScaledBitMap(bitmap,VueApplication.getInstance().getPixel(220),  VueApplication.getInstance().getPixel(220));
      	  }
    	}
        return bitmap;
    }*/
}
