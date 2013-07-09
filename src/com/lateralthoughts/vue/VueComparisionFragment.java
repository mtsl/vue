package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.lateralthoughts.vue.AisleDetailsViewListLoader.BitmapWorkerTask;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.HorizontalListView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VueComparisionFragment extends Fragment {
     HorizontalListView mTopScroller,mBottomScroller;
     int mStatusbarHeight;
     int mScreenTotalHeight;
     int mCoparisionScreenHeight;
     Context mContext;
 	AisleWindowContent mWindowContent;
 	 ArrayList<AisleImageDetails> mimageDetailsArr = null;
 	AisleImageDetails mitemDetails = null;
     private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
     private BitmapLoaderUtils mBitmapLoaderUtils;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
     
        View v = inflater.inflate(R.layout.aisle_comparision_view_fragment, container, false);
    
        mTopScroller = (HorizontalListView) v.findViewById(R.id.topscroller);
        mBottomScroller = (HorizontalListView) v.findViewById(R.id.bottomscroller);
      //  VueComparisionAdapter vcAdapter = new VueComparisionAdapter(getActivity(), null, null);
       // vcAdapter.setUpImages(mTopScroller,mBottomScroller);
        mStatusbarHeight = VueApplication.getInstance().getmStatusBarHeight();
        mScreenTotalHeight = VueApplication.getInstance().getScreenHeight();
        mCoparisionScreenHeight = mScreenTotalHeight - mStatusbarHeight - VueApplication.getInstance().getPixel(30);
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel.getInstance(mContext);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(mContext);

      		for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
      			 mWindowContent = (AisleWindowContent) mVueTrendingAislesDataModel.getAisleAt(i);
      			if (mWindowContent.getAisleId().equalsIgnoreCase(
      					VueApplication.getInstance().getClickedWindowID())) {
      				 mWindowContent = (AisleWindowContent) mVueTrendingAislesDataModel.getAisleAt(i);
      				break;
      			}
      		}
      		mimageDetailsArr = mWindowContent.getImageList();
      		 if(null != mimageDetailsArr && mimageDetailsArr.size() != 0){ 
      			 
      		 }
        
        mTopScroller.setAdapter(new ComparisionAdapter(mContext));
        mBottomScroller.setAdapter(new ComparisionAdapter(mContext));
        
        
       
       

        
        return v;
    }


class ComparisionAdapter extends BaseAdapter {
    LayoutInflater minflater;
	public ComparisionAdapter(Context context) {
		   minflater = (LayoutInflater)
				   context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mimageDetailsArr.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder viewHolder;
		 mitemDetails = mimageDetailsArr.get(position);
		 Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(mitemDetails.mCustomImageUrl);
		  if(convertView == null) {
			  viewHolder = new ViewHolder();
					  convertView = minflater.inflate(R.layout.vuecompareimg, null);
					  viewHolder.img = (ImageView) convertView.findViewById(R.id.vue_compareimg);
					  RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mCoparisionScreenHeight/2, mCoparisionScreenHeight/2);
					  params.addRule(RelativeLayout.CENTER_IN_PARENT);
					  params.setMargins(VueApplication.getInstance().getPixel(10), 0, 0, 0);
					  viewHolder.img.setLayoutParams(params);
					  viewHolder.img.setBackgroundColor(Color.parseColor("#FFFFFF"));
					  convertView.setTag(viewHolder);
		  }
		  viewHolder =   (ViewHolder) convertView.getTag();
		  if(bitmap != null)
		  viewHolder.img.setImageBitmap(bitmap); 
		  else {
			  viewHolder.img.setImageResource(R.drawable.ic_launcher);
			  BitmapWorkerTask task = new BitmapWorkerTask(null, viewHolder.img, mCoparisionScreenHeight/2);
			  task.execute(mitemDetails.mCustomImageUrl);
			 
		  }
		return convertView;
	}
	
	private class ViewHolder {
		ImageView img;
	}
}

class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    //private final WeakReference<AisleContentBrowser>viewFlipperReference;
    private String url = null;
    private int mBestHeight;

    public BitmapWorkerTask(AisleContentBrowser vFlipper, ImageView imageView, int bestHeight) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        mBestHeight = bestHeight;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        url = params[0];
        Bitmap bmp = null;            
        //we want to get the bitmap and also add it into the memory cache
        bmp = mBitmapLoaderUtils.getBitmap(url, true, mBestHeight); 
        return bmp;            
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
      
                imageView.setImageBitmap(bitmap);
            
        }
    }
}


}