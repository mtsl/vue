/**
 * 
 * This view will auto determine the width or height by determining if the 
 * height or width is set and scale the other dimension depending on the images dimension
 * 
 * This view also contains an ImageChangeListener which calls changed(boolean isEmpty) once a 
 * change has been made to the ImageView
 * 
 * @author Maurycy Wojtowicz
 *
 */
package com.lateralthoughts.vue.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

public class ScaleImageView extends /*Network*/ImageView {
	private ImageChangeListener imageChangeListener;
	private boolean scaleToWidth = false; // this flag determines if should
											// measure height manually dependent
											// of width
	private Rect mRect;
	private Paint mPaint;
	private int mBestHeight;
	private int mBestWidth;

	public ScaleImageView(Context context) {
		super(context);
		init();
	}

	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		this.setScaleType(ScaleType.CENTER_CROP);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		Log.i("calling here", "calling here");
		//new BitmapReszie().execute(bm);
		 super.setImageBitmap(bm);
		// this.setImageBitmap(bm);

		if (imageChangeListener != null)
			imageChangeListener.changed((bm == null));
	}

	@Override
	public void setImageDrawable(Drawable d) {
		super.setImageDrawable(d);

		if (imageChangeListener != null)
			imageChangeListener.changed((d == null));
	}

	@Override
	public void setImageResource(int id) {
		super.setImageResource(id);
	}

	public interface ImageChangeListener {
		// a callback for when a change has been made to this imageView
		void changed(boolean isEmpty);
	}

	public ImageChangeListener getImageChangeListener() {
		return imageChangeListener;
	}

	public void setImageChangeListener(ImageChangeListener imageChangeListener) {
		this.imageChangeListener = imageChangeListener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode == MeasureSpec.EXACTLY
				|| widthMode == MeasureSpec.AT_MOST) {
			scaleToWidth = true;
		} else if (heightMode == MeasureSpec.EXACTLY
				|| heightMode == MeasureSpec.AT_MOST) {
			scaleToWidth = false;
		} else
			throw new IllegalStateException(
					"width or height needs to be set to match_parent or a specific dimension");

		if (getDrawable() == null || getDrawable().getIntrinsicWidth() == 0) {
			// nothing to measure
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		} else {
			if (scaleToWidth) {
				int iw = this.getDrawable().getIntrinsicWidth();
				int ih = this.getDrawable().getIntrinsicHeight();
				int heightC = width * ih / iw;
				if (height > 0)
					if (heightC > height) {
						// dont let hegiht be greater then set max
						heightC = height;
						width = heightC * iw / ih;
					}

				this.setScaleType(ScaleType.CENTER);
				setMeasuredDimension(width, heightC);

			} else {
				// need to scale to height instead
				int marg = 0;
				/*
				 * if(getParent()!=null){ if(getParent().getParent()!=null){
				 * marg+= ((RelativeLayout)
				 * getParent().getParent()).getPaddingTop(); marg+=
				 * ((RelativeLayout)
				 * getParent().getParent()).getPaddingBottom(); } }
				 */

				int iw = this.getDrawable().getIntrinsicWidth();
				int ih = this.getDrawable().getIntrinsicHeight();

				width = height * iw / ih;
				height -= marg;
				setMeasuredDimension(width, height);
			}

		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setOpaqueWorkerObject(Object object) {
		mObject = object;
	}

	public Object getOpaqueWorkerObject() {
		return mObject;
	}

	public void setContainerObject(Object object) {
		mContainer = object;
	}

	public Object getContainerObject() {
		return mContainer;
	}


/*	public void setImageUrl(String url, ImageLoader imageLoader, int bestWidth,
			int bestHeight) {
		mBestHeight = bestHeight;
		mBestWidth = bestWidth;
		super.setImageUrl(url, imageLoader);
	}*/

	private Object mObject;
	private Object mContainer;

	public class BitmapReszie extends AsyncTask<Bitmap, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			// TODO Auto-generated method stub
			Bitmap bm = BitmapLoaderUtils.getInstance().getBitmap(params[0],
					mBestWidth, mBestHeight);
			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				setImage(result);
			}
		}

	}

	private void setImage(Bitmap bm) {
		super.setImageBitmap(bm);
	}
}
