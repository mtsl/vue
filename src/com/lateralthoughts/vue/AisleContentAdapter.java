package com.lateralthoughts.vue;

//android imports
import java.lang.ref.WeakReference;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Bitmap;

//internal imports
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ScaledImageViewFactory;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

public class AisleContentAdapter extends BaseAdapter {
	private String mContentUrls[];
	private String mUniqueAisleId;
	private ScaledImageViewFactory mScaledImageViewFactory;
	private BitmapLoaderUtils mBitmapLoaderUtils;
	
	public AisleContentAdapter(Context context, String uniqueAisleId, String urls[]){
		mScaledImageViewFactory = ScaledImageViewFactory.getInstance(context);
		mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(context);
	}
	public int getCount(){
		if(null != mContentUrls){
			return mContentUrls.length;
		}
		return 0;
	}
	
	public long getItemId(int id){
		return 0;
	}
	
	public String getItem(int position){
		if(null != mContentUrls && position < mContentUrls.length){
			return mContentUrls[position];
		}
		return null;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		
		if(null == convertView){
			holder = new ViewHolder();
			convertView = mScaledImageViewFactory.getEmptyImageView();
			holder.mContentView = (ScaleImageView)convertView;
			convertView.setTag(holder);
		}
		
		holder = (ViewHolder)convertView.getTag();
		String imageUrl = getItem(position);
		loadImageIntoAisleBrowser(holder.mContentView, imageUrl);
		return null;
	}
	
	//AisleContentAdapter specific methods & classes
	static class ViewHolder{
		ScaleImageView mContentView;
	}
	
	public String getAisleIdentifier(){
		return mUniqueAisleId;
	}
	
	public void resetAdapter(String uniqueId, String urls[]){
		mUniqueAisleId = uniqueId;
		mContentUrls = urls;
	}
	
	private void loadImageIntoAisleBrowser(ScaleImageView imageView, String imageUrl){
	    if (cancelPotentialDownload(imageUrl, imageView)) {        	
	        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        ((ScaleImageView)imageView).setOpaqueWorkerObject(task);
	        task.execute(imageUrl);
	    }
	}
	
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String url = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;            
            bmp = mBitmapLoaderUtils.getBitmap(url, true);
            return bmp;            
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                
                if (this == bitmapWorkerTask) {
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

}
