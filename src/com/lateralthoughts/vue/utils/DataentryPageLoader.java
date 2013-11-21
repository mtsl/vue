package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DataentryPageLoader {

	PhotosQueue photosQueue;
	PhotosLoader photoLoaderThread;
	public static DataentryPageLoader mOtherSourceImageLoader;

	public DataentryPageLoader() {
		photosQueue = new PhotosQueue();
		photoLoaderThread = new PhotosLoader();
		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
	}

	public static DataentryPageLoader getInstatnce() {
		if (mOtherSourceImageLoader == null) {
			mOtherSourceImageLoader = new DataentryPageLoader();
		}
		return mOtherSourceImageLoader;
	}

	public void DisplayImage(String originalImagePath, String imageUrl,
			String imagePath, ImageView imageView,
			ProgressBar dataEntryRowAisleImage, TextView touchToChangeImage,
			boolean hideTouchToChangeImageFlag, LinearLayout imageDeleteBtn,
			LinearLayout imageEditBtn, boolean isAddedToServer) {
		Log.e("DataentryPageLoader", "Display image called :: " + imagePath);
		queuePhoto(originalImagePath, imageUrl, imagePath, imageView,
				dataEntryRowAisleImage, touchToChangeImage,
				hideTouchToChangeImageFlag, imageDeleteBtn, imageEditBtn,
				isAddedToServer);
	}

	private void queuePhoto(String originalImagePath, String imageUrl,
			String imagePath, ImageView imageView,
			ProgressBar dataEntryRowAisleImage, TextView touchToChangeImage,
			boolean hideTouchToChangeImageFlag, LinearLayout imageDeleteBtn,
			LinearLayout imageEditBtn, boolean isAddedToServer) {
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(originalImagePath, imageUrl, imagePath,
				imageView, dataEntryRowAisleImage, touchToChangeImage,
				hideTouchToChangeImageFlag, imageDeleteBtn, imageEditBtn,
				isAddedToServer);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	private class PhotoToLoad {
		public String imagePath;
		public ImageView imageView;
		public ProgressBar dataEntryRowAisleImage;
		public TextView touchToChangeImage;
		public boolean hideTouchToChangeImageFlag;
		String originalImagePath;
		String imageUrl;
		LinearLayout imageDeleteBtn;
		LinearLayout imageEditBtn;
		boolean isAddedToServer;

		public PhotoToLoad(String originalImagePath, String imageUrl, String u,
				ImageView i, ProgressBar dataEntryRowAisleImage,
				TextView touchToChangeImage,
				boolean hideTouchToChangeImageFlag,
				LinearLayout imageDeleteBtn, LinearLayout imageEditBtn,
				boolean isAddedToServer) {
			imagePath = u;
			imageView = i;
			this.dataEntryRowAisleImage = dataEntryRowAisleImage;
			this.touchToChangeImage = touchToChangeImage;
			this.hideTouchToChangeImageFlag = hideTouchToChangeImageFlag;
			this.imageUrl = imageUrl;
			this.originalImagePath = originalImagePath;
			this.imageDeleteBtn = imageDeleteBtn;
			this.imageEditBtn = imageEditBtn;
			this.isAddedToServer = isAddedToServer;
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
						File f = new File(photoToLoad.imagePath);
						if (f != null && f.exists()) {
							Log.e("DataentryPageLoader",
									"Display image called 1 :: " + f.getPath());
							if (((String) photoToLoad.imageView.getTag())
									.equals(photoToLoad.imagePath)) {
								Log.e("DataentryPageLoader",
										"Display image called 11 :: "
												+ f.getPath());
								Bitmap bmp = BitmapFactory.decodeFile(f
										.getPath());
								BitmapDisplayer bd = new BitmapDisplayer(bmp,
										photoToLoad.imageView,
										photoToLoad.dataEntryRowAisleImage,
										photoToLoad.touchToChangeImage,
										photoToLoad.hideTouchToChangeImageFlag,
										photoToLoad.imageDeleteBtn,
										photoToLoad.imageEditBtn,
										photoToLoad.isAddedToServer);
								Activity a = (Activity) photoToLoad.imageView
										.getContext();
								a.runOnUiThread(bd);
							}
						} else {
							if (photoToLoad.originalImagePath != null) {
								DisplayMetrics dm = VueApplication
										.getInstance().getResources()
										.getDisplayMetrics();
								float screenHeight = dm.heightPixels;
								screenHeight = screenHeight
										- Utils.dipToPixels(
												VueApplication.getInstance(),
												DataEntryFragment.AISLE_IMAGE_MARGIN);
								float screenWidth = dm.widthPixels;
								getResizedImage(new File(
										photoToLoad.originalImagePath),
										new File(photoToLoad.imagePath),
										screenHeight, screenWidth,
										VueApplication.getInstance());
								if (((String) photoToLoad.imageView.getTag())
										.equals(photoToLoad.imagePath)) {
									Log.e("DataentryPageLoader",
											"Display image called 11 :: "
													+ f.getPath());
									Bitmap bmp = BitmapFactory.decodeFile(f
											.getPath());
									BitmapDisplayer bd = new BitmapDisplayer(
											bmp,
											photoToLoad.imageView,
											photoToLoad.dataEntryRowAisleImage,
											photoToLoad.touchToChangeImage,
											photoToLoad.hideTouchToChangeImageFlag,
											photoToLoad.imageDeleteBtn,
											photoToLoad.imageEditBtn,
											photoToLoad.isAddedToServer);
									Activity a = (Activity) photoToLoad.imageView
											.getContext();
									a.runOnUiThread(bd);
								}
							} else if (photoToLoad.imageUrl != null) {
								try {
									InputStream is = new URL(
											photoToLoad.imageUrl).openStream();
									OutputStream os = new FileOutputStream(
											new FileCache(VueApplication
													.getInstance())
													.getFile(photoToLoad.imageUrl));
									Utils.CopyStream(is, os);
									os.close();
									DisplayMetrics dm = VueApplication
											.getInstance().getResources()
											.getDisplayMetrics();
									float screenHeight = dm.heightPixels;
									screenHeight = screenHeight
											- Utils.dipToPixels(
													VueApplication
															.getInstance(),
													DataEntryFragment.AISLE_IMAGE_MARGIN);
									float screenWidth = dm.widthPixels;
									getResizedImage(
											new FileCache(VueApplication
													.getInstance())
													.getFile(photoToLoad.imageUrl),
											new File(photoToLoad.imagePath),
											screenHeight, screenWidth,
											VueApplication.getInstance());
									if (((String) photoToLoad.imageView
											.getTag())
											.equals(photoToLoad.imagePath)) {
										Log.e("DataentryPageLoader",
												"Display image called 11 :: "
														+ f.getPath());
										Bitmap bmp = BitmapFactory.decodeFile(f
												.getPath());
										BitmapDisplayer bd = new BitmapDisplayer(
												bmp,
												photoToLoad.imageView,
												photoToLoad.dataEntryRowAisleImage,
												photoToLoad.touchToChangeImage,
												photoToLoad.hideTouchToChangeImageFlag,
												photoToLoad.imageDeleteBtn,
												photoToLoad.imageEditBtn,
												photoToLoad.isAddedToServer);
										Activity a = (Activity) photoToLoad.imageView
												.getContext();
										a.runOnUiThread(bd);
									}
								} catch (MalformedURLException e) {
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class BitmapDisplayer implements Runnable {
		Bitmap bmp;
		ImageView imageView;
		ProgressBar dataEntryRowAisleImage;
		TextView touchToChangeImage;
		boolean hideTouchToChangeImageFlag;
		LinearLayout imageDeleteBtn;
		LinearLayout imageEditBtn;
		boolean isAddedToServer;

		public BitmapDisplayer(Bitmap bmp, ImageView i,
				ProgressBar dataEntryRowAisleImage,
				TextView touchToChangeImage,
				boolean hideTouchToChangeImageFlag,
				LinearLayout imageDeleteBtn, LinearLayout imageEditBtn,
				boolean isAddedToServer) {
			this.bmp = bmp;
			imageView = i;
			this.dataEntryRowAisleImage = dataEntryRowAisleImage;
			this.touchToChangeImage = touchToChangeImage;
			this.hideTouchToChangeImageFlag = hideTouchToChangeImageFlag;
			this.imageDeleteBtn = imageDeleteBtn;
			this.imageEditBtn = imageEditBtn;
			this.isAddedToServer = isAddedToServer;
		}

		public void run() {
			Log.e("DataentryPageLoader", "Display image called 111 :: ");
			dataEntryRowAisleImage.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			if (!hideTouchToChangeImageFlag) {
				if (isAddedToServer) {
					touchToChangeImage.setVisibility(View.GONE);
					imageDeleteBtn.setVisibility(View.VISIBLE);
					imageEditBtn.setVisibility(View.VISIBLE);
				} else {
					touchToChangeImage.setVisibility(View.GONE);
					imageDeleteBtn.setVisibility(View.GONE);
					imageEditBtn.setVisibility(View.GONE);
				}
			} else {
				touchToChangeImage.setVisibility(View.GONE);
				imageDeleteBtn.setVisibility(View.GONE);
				imageEditBtn.setVisibility(View.GONE);
			}
			if (bmp != null) {
				imageView.setImageBitmap(bmp);
			} else
				imageView.setImageResource(R.drawable.no_image);
		}
	}

	private void getResizedImage(File f, File resizedFileName,
			float screenHeight, float screenWidth, Context mContext) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			Log.e("cs", "10");
			int height = o.outHeight;
			int width = o.outWidth;
			int scale = 1;
			int heightRatio = 0;
			int widthRatio = 0;
			Log.e("Utils bitmap path", f.getPath());
			Log.e("Utils bitmap width", width + "..." + screenWidth);
			Log.e("Utils bitmap height", height + "..." + screenHeight);
			if (height > screenHeight) {
				// Calculate ratios of height and width to requested height and
				// width
				heightRatio = Math.round((float) height / (float) screenHeight);
			}
			if (width > screenWidth) {
				// Calculate ratios of height and width to requested height and
				// width
				widthRatio = Math.round((float) width / (float) screenWidth);
			}
			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			scale = heightRatio < widthRatio ? heightRatio : widthRatio;
			// decode with inSampleSize
			Log.e("cs", "12");
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = (int) scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap resizedBitmap = BitmapFactory
					.decodeStream(stream2, null, o2);
			stream2.close();
			Log.e("cs", "13");
			Utils.saveBitmap(resizedBitmap, resizedFileName);
			BitmapFactory.Options o3 = new BitmapFactory.Options();
			o3.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(resizedFileName),
					null, o3);
			if (o3.outWidth > VueApplication.getInstance().mScreenWidth
					|| o3.outHeight > VueApplication.getInstance().mScreenHeight) {
				resizedBitmap = Utils.getBestDementions(resizedBitmap,
						o3.outWidth, o3.outHeight,
						VueApplication.getInstance().mScreenWidth,
						VueApplication.getInstance().mScreenHeight);
				Utils.saveBitmap(resizedBitmap, resizedFileName);
			}
			resizedBitmap.recycle();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}