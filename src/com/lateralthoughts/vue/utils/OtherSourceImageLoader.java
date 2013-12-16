package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;

public class OtherSourceImageLoader {

	FileCache mFileCache;
	PhotosQueue photosQueue;
	PhotosLoader photoLoaderThread;
	public static OtherSourceImageLoader mOtherSourceImageLoader;

	public OtherSourceImageLoader() {
		mFileCache = new FileCache(VueApplication.getInstance());
		photosQueue = new PhotosQueue();
		photoLoaderThread = new PhotosLoader();
		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
	}

	public static OtherSourceImageLoader getInstatnce() {
		if (mOtherSourceImageLoader == null) {
			mOtherSourceImageLoader = new OtherSourceImageLoader();
		}
		return mOtherSourceImageLoader;
	}

	final int stub_id = R.drawable.aisle_bg_progressbar_drawable;

	public void DisplayImage(String url, ImageView imageView,
			RelativeLayout otherSourceAisleImage) {
		String filename = String.valueOf(url.hashCode());
		File f = mFileCache.getVueAppResizedPictureFile(filename);
		if (f.exists()) {
			otherSourceAisleImage.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			try {
				imageView.setImageBitmap(BitmapFactory.decodeFile(f.getPath()));
			} catch (Throwable ex) {
				imageView.setImageResource(R.drawable.no_image);
				ex.printStackTrace();
			}
		} else {
			queuePhoto(url, imageView, otherSourceAisleImage);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView,
			RelativeLayout otherSourceAisleImage) {
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView, otherSourceAisleImage);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	private File getBitmap(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = mFileCache.getVueAppResizedPictureFile(filename);
		if (f.exists())
			return f;
		try {
			InputStream is = new URL(url).openStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			f = decodeFile(f);
			return f;
		} catch (MalformedURLException e) {
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private File decodeFile(File f) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			int height = o.outHeight;
			int width = o.outWidth;
			int scale = 1;
			int heightRatio = 0;
			int widthRatio = 0;
			Log.e("Utils bitmap width",
					width + "..." + VueApplication.getInstance().mScreenWidth);
			Log.e("Utils bitmap height",
					height + "..." + VueApplication.getInstance().mScreenHeight);
			if (height > VueApplication.getInstance().mScreenHeight) {
				// Calculate ratios of height and width to requested height and
				// width
				heightRatio = Math.round((float) height
						/ (float) VueApplication.getInstance().mScreenHeight);
			}
			if (width > VueApplication.getInstance().mScreenWidth) {
				// Calculate ratios of height and width to requested height and
				// width
				widthRatio = Math.round((float) width
						/ (float) VueApplication.getInstance().mScreenWidth);
			}
			scale = heightRatio < widthRatio ? heightRatio : widthRatio;

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap resizedBitmap = BitmapFactory
					.decodeStream(stream2, null, o2);
			stream2.close();
			Utils.saveBitmap(resizedBitmap, f);
			BitmapFactory.Options o3 = new BitmapFactory.Options();
			o3.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o3);
			if (o3.outWidth > VueApplication.getInstance().mScreenWidth
					|| o3.outHeight > VueApplication.getInstance().mScreenHeight) {
				resizedBitmap = Utils.getBestDementions(resizedBitmap,
						o3.outWidth, o3.outHeight,
						VueApplication.getInstance().mScreenWidth,
						VueApplication.getInstance().mScreenHeight);
				Utils.saveBitmap(resizedBitmap, f);
			}
			resizedBitmap.recycle();
			return f;
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public RelativeLayout otherSourceAisleImage;

		public PhotoToLoad(String u, ImageView i,
				RelativeLayout otherSourceAisleImage) {
			url = u;
			imageView = i;
			this.otherSourceAisleImage = otherSourceAisleImage;
		}
	}

	public void stopThread() {
		photoLoaderThread.interrupt();
	}

	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		public void Clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				if (photosToLoad.get(j).imageView == image)
					photosToLoad.remove(j);
				else
					++j;
			}
		}
	}

	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						File f = getBitmap(photoToLoad.url);
						if (f != null && f.exists()) {
							if (((String) photoToLoad.imageView.getTag())
									.equals(photoToLoad.url)) {
								Bitmap bmp = null;
								try {
									bmp = BitmapFactory.decodeFile(f.getPath());
								} catch (Throwable ex) {
									ex.printStackTrace();
								}
								BitmapDisplayer bd = new BitmapDisplayer(bmp,
										photoToLoad.imageView,
										photoToLoad.otherSourceAisleImage);
								Activity a = (Activity) photoToLoad.imageView
										.getContext();
								a.runOnUiThread(bd);
							}
						} else {
							BitmapDisplayer bd = new BitmapDisplayer(null,
									photoToLoad.imageView,
									photoToLoad.otherSourceAisleImage);
							Activity a = (Activity) photoToLoad.imageView
									.getContext();
							a.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	class BitmapDisplayer implements Runnable {
		Bitmap bmp;
		ImageView imageView;
		RelativeLayout otherSourceAisleImage;

		public BitmapDisplayer(Bitmap bmp, ImageView i,
				RelativeLayout otherSourceAisleImage) {
			this.bmp = bmp;
			imageView = i;
			this.otherSourceAisleImage = otherSourceAisleImage;
		}

		public void run() {
			otherSourceAisleImage.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			if (bmp != null)
				imageView.setImageBitmap(bmp);
			else
				imageView.setImageResource(R.drawable.no_image);
		}
	}
}
