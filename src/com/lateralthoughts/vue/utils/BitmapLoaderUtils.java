package com.lateralthoughts.vue.utils;

//android imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.VueApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView.HitTestResult;

public class BitmapLoaderUtils {

	// private Context mContext;
	private static BitmapLoaderUtils sBitmapLoaderUtils;
	private FileCache mFileCache;
	// private VueMemoryCache<Bitmap> mAisleImagesCache;
	private BitmapLruCache mAisleImagesCache;

	// private int mScreenWidth;

	// private final boolean DEBUG = false;

	private BitmapLoaderUtils() {
		// mContext = context;
		mFileCache = VueApplication.getInstance().getFileCache();
		// mAisleImagesCache =
		// VueApplication.getInstance().getAisleImagesMemCache();
		mAisleImagesCache = BitmapLruCache.getInstance(VueApplication
				.getInstance());

		// DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		// mScreenWidth = metrics.widthPixels;

		// mExecutorService = Executors.newFixedThreadPool(5);
	}

	public static BitmapLoaderUtils getInstance() {
		if (null == sBitmapLoaderUtils) {
			sBitmapLoaderUtils = new BitmapLoaderUtils();
		}
		return sBitmapLoaderUtils;
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
			int bestHeight, int bestWidth, String source) {
		Log.i("added url", "added url  getBitmap " + url);
		File f = mFileCache.getFile(url);
		Log.i("added url", "added url  getBitmap " + f);
		// from SD cache
		Bitmap b = decodeFile(f, bestHeight, bestWidth, source);
		if (b != null) {

			if (cacheBitmap)
				mAisleImagesCache.putBitmap(url, b);
			return b;
		}

		// from web
		try {
			if (serverUrl == null || serverUrl.length() < 1) {

				return null;
			}
			Bitmap bitmap = null;
			URL imageUrl = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			int hashCode = url.hashCode();
			String filename = String.valueOf(hashCode);
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, bestHeight, bestWidth, source);
			if (cacheBitmap)
				mAisleImagesCache.putBitmap(url, bitmap);
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				// mAisleImagesCache.clear();
			}
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	public Bitmap decodeFile(File f, int bestHeight, int bestWidth,
			String source) {
		Log.i("added url", "added url in  decodeFile: bestheight is "
				+ bestHeight);

		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			Log.i("imageHeight", "imageHeight: " + o.outHeight);
			Log.i("imageHeight", "imageHeight: " + o.outWidth);
			Log.i("imageHeight", "imageUri: " + f.getAbsolutePath());

			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = mScreenWidth/2;
			int height = o.outHeight;
			int width = o.outWidth;
			Log.i("window", "clickedwindow ID bitmap Height4 bestHeight: "
					+ bestHeight);
			Log.i("window", "clickedwindow ID original height: " + height);

			int reqWidth = bestWidth;
			int scale = 1;

			if (height > bestHeight) {

				// Calculate ratios of height and width to requested height and
				// width
				final int heightRatio = Math.round((float) height
						/ (float) bestHeight);
				final int widthRatio = Math.round((float) width
						/ (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will
				// guarantee
				// a final image with both dimensions larger than or equal to
				// the
				// requested height and width.
				scale = heightRatio; // < widthRatio ? heightRatio : widthRatio;
				scale = 1;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			// o2.inSampleSize = o.inSampleSize;
			// if(DEBUG) Log.d("Jaws","using inSampleSizeScale = " + scale +
			// " original width = " + o.outWidth + "screen width = " +
			// mScreenWidth);
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			Log.i("window",
					"clickedwindow ID  new bitmap height1 : "
							+ bitmap.getHeight());
			Log.i("window",
					"clickedwindow ID new bitmap widht1: " + bitmap.getWidth());
			stream2.close();
			if(source.equalsIgnoreCase(Utils.TRENDING_SCREEN)){
				return getBitmap(bitmap,bestWidth,bestHeight);
			}
			
			
			if (source.equalsIgnoreCase(Utils.DETAILS_SCREEN)) {
				// scaling factor considers only integers may be some times
				// scaling factor becomes 1 even there is slight difference in
				// sizes
				// so to avoid croping in that cases in Detailsview screen
				// compare sizes after scaling.
				if (bitmap != null) {
					width = bitmap.getWidth();
					height = bitmap.getHeight();
					if (height > bestHeight) {
						float tempWidth = (width * bestHeight) / height;
						width = (int) tempWidth;
						bitmap = getModifiedBitmap(bitmap, width, bestHeight);
					}
				}
			}

			if (bitmap != null) {
				width = bitmap.getWidth();
				height = bitmap.getHeight();

				if (width > reqWidth) {
					float tempHeight = (height * reqWidth) / width;
					height = (int) tempHeight;
					bitmap = getModifiedBitmap(bitmap, reqWidth, height);
				}
			}
			Log.i("window",
					"clickedwindow ID  new bitmap height2 : "
							+ bitmap.getHeight());
			Log.i("window",
					"clickedwindow ID new bitmap widht2: " + bitmap.getWidth());
			return bitmap;
		} catch (FileNotFoundException e) {
			Log.i("added url", "added urldecodeFile  filenotfound exception ");
		} catch (IOException e) {
			Log.i("added url", "added urldecodeFile  io exception ");
			e.printStackTrace();
		} catch (Throwable ex) {
			Log.i("added url", "added urldecodeFile  throwable exception ");
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				// mAisleImagesCache.clear();
			}
			return null;
		}
		return null;
	}

	public Bitmap getBitmap(Bitmap bitmap, int bestWidth, int bestHeight) {
		int width, height;
		if (bitmap != null) {
			width = bitmap.getWidth();
			height = bitmap.getHeight();
			if (height > bestHeight) {
				float tempWidth = (width * bestHeight) / height;
				width = (int) tempWidth;
				bitmap = getModifiedBitmap(bitmap, width, bestHeight);
			}

			if (bitmap != null) {
				width = bitmap.getWidth();
				height = bitmap.getHeight();

				if (width > bestWidth) {
					float tempHeight = (height * bestWidth) / width;
					height = (int) tempHeight;
					bitmap = getModifiedBitmap(bitmap, bestWidth, height);
				}
			}
		}
		return bitmap;
	}

	private Bitmap getModifiedBitmap(Bitmap originalImage, int width, int height) {
		// here width & height are the desired width & height values)

		// first lets create a new bitmap and a canvas to draw into it.
		Bitmap newBitmap = Bitmap.createBitmap((int) width, (int) height,
				Config.ARGB_8888);
		float originalWidth = originalImage.getWidth(), originalHeight = originalImage
				.getHeight();
		Canvas canvas = new Canvas(newBitmap);
		float scale = width / originalWidth;
		float xTranslation = 0.0f, yTranslation = (height - originalHeight
				* scale) / 2.0f;
		Matrix transformation = new Matrix();
		// now that we have the transformations, set that for our drawing ops
		transformation.postTranslate(xTranslation, yTranslation);
		transformation.preScale(scale, scale);
		// create a paint and draw into new canvas
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalImage, transformation, paint);
		Log.i("imagenotcoming",
				"bitmap issue scalleddown: originalbitmap width "
						+ newBitmap.getWidth());
		Log.i("imagenotcoming",
				"bitmap issue:scalleddown originalbitmap height:  "
						+ newBitmap.getHeight());
		return newBitmap;
	}

	public Bitmap getCachedBitmap(String url) {
		return mAisleImagesCache.get(url);
	}

	public void clearCache() {
		// mAisleImagesCache.clear();
		// mFileCache.clear();
	}

	public void removeBitmapFromCache(String id) {
		// mAisleImagesCache.removeBitmap(id);
	}
}
