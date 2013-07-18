package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import com.lateralthoughts.vue.VueApplication;

public class Utils {
	private static final String CURRENT_FONT_SIZE = "currentFontSize";
	public static final String NETWORK_SETTINGS = "networkSettings";
	public static final int LARGE_TEXT_SIZE = 22;
	public static final int MEDIUM_TEXT_SIZE = 18;
	public static final int SMALL_TEXT_SIZE = 14;

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static String addImageInfo(String url, int width, int height) {
		if (url.contains("width") && url.contains("height")) {
			return url;
		}
		StringBuilder modifiedUrl = new StringBuilder(url);
		modifiedUrl.append("?width=" + width);
		modifiedUrl.append("&height=" + height);
		return modifiedUrl.toString();
	}

	/**
	 * To get the CURRENT_FONT_SIZE value stored in SharedPreferences. it will
	 * return default value which is MEDIUM_TEXT_SIZE (18sp) if not value is
	 * stored in SharedPreferences.
	 * 
	 * @param Context
	 *            context.
	 * @return int Current font size from SharedPreferences.
	 * */
	public static int getCurrentFontSize(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getInt(CURRENT_FONT_SIZE, MEDIUM_TEXT_SIZE);
	}

	/**
	 * To change the CURRENT_FONT_SIZE value stored in SharedPreferences.
	 * 
	 * @param Context
	 *            context
	 * @param int newFontSize
	 * */
	public static void changeCurrentFontSize(Context context, int newFontSize) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putInt(CURRENT_FONT_SIZE, newFontSize);
		editor.commit();
	}

	/**
	 * To save the network settings in SharedPreferances.
	 * 
	 * @param Context
	 *            context.
	 * @param boolean isWifiOnly, if isWifiOnly is true then network excess
	 *        should be made only if wifi is on.
	 * */
	public static void saveNetworkSettings(Context context, boolean isWifiOnly) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean(NETWORK_SETTINGS, isWifiOnly);
		editor.commit();
	}

	/**
	 * To get the network settings value from SharedPreferances.
	 * 
	 * @param Context
	 *            context.
	 * @return boolean, if true then network excess should be made only if wifi
	 *         is on.
	 * */
	public static boolean isWifiOnly(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean(NETWORK_SETTINGS, false);
	}

	/**
	 * 
	 * @param bmp
	 * @param file
	 */
	public static void saveBitmap(Bitmap bmp, File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	// Getting Image file path from URI.
	public static String getPath(Uri uri, Activity activity) {
		Log.e("getPath", ""+uri);
		Cursor cursor = activity.getContentResolver().query(uri, null, null,
				null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
			Log.e("getPath", ""+uri.getPath());						// path
			return uri.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			Log.e("getPath", ""+cursor.getString(idx)+"..?"+idx);
			return cursor.getString(idx);
		}
	}

	public static String getResizedImage(File f, float screenHeight,
			float screenWidth, Context mContext) {
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
			Log.e("Utils bitmap width and height ratio", widthRatio
					+ "........" + heightRatio + "????????" + scale);
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = (int) scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap resizedBitmap = BitmapFactory
					.decodeStream(stream2, null, o2);
			stream2.close();
			File resizedFileName = new File(
					vueAppResizedImageFileName(mContext));
			String resizedFilePath = resizedFileName.getPath();
			Log.e("Utils", resizedFilePath);
			FileOutputStream out = new FileOutputStream(resizedFileName);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			resizedBitmap.recycle();
			resizedBitmap = null;
			return resizedFilePath;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String vueAppCameraImageFileName(Context context) {
		FileCache fileCacheObj = new FileCache(context);
		if (fileCacheObj.vueAppCameraPicsDir != null) {
			File fv[] = fileCacheObj.vueAppCameraPicsDir.listFiles();
			if (fv != null) {
				return fileCacheObj.getVueAppCameraPictureFile(
						fv.length + 1 + "").getPath();
			}
		}

		return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
	}

	public static String vueAppResizedImageFileName(Context context) {
		FileCache fileCacheObj = new FileCache(context);
		if (fileCacheObj.vueAppResizedImagesDir != null) {
			File fv[] = fileCacheObj.vueAppResizedImagesDir.listFiles();
			if (fv != null) {
				return fileCacheObj.getVueAppResizedPictureFile(
						fv.length + 1 + "").getPath();
			}
		}

		return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
	}

	public static ImageDimention getScalledImage(Bitmap bitmap,
			int availableWidth, int availableHeight) {

		ImageDimention imgDimention = new ImageDimention();
		float requiredWidth, requiredHeight;
		float bitmapOriginalWidth = bitmap.getWidth();
		float bitmapOriginalHeight = bitmap.getHeight();
		float scaleFactor;
		requiredHeight = availableHeight;
		if (availableWidth > VueApplication.getInstance()
				.getVueDetailsCardWidth()) {
			requiredWidth = VueApplication.getInstance()
					.getVueDetailsCardWidth();
		} else {
			requiredWidth = availableWidth;
			// requiredWidth =
			// VueApplication.getInstance().getVueDetailsCardWidth();
		}
		if ((availableWidth < VueApplication.getInstance()
				.getVueDetailsCardWidth() && availableHeight < VueApplication
				.getInstance().getVueDetailsCardHeight())) {
			imgDimention.mImgWidth = availableWidth;
			imgDimention.mImgHeight = availableHeight;
			return imgDimention;
		}

		float temp = requiredWidth / bitmapOriginalWidth;
		if (temp <= 1) {
			// reduce the image size to the smallest one of given dimensions
			scaleFactor = Math.min(requiredWidth / bitmapOriginalWidth,
					requiredHeight / bitmapOriginalHeight);
			requiredHeight = Math.round(bitmapOriginalHeight * scaleFactor);
			requiredWidth = Math.round(bitmapOriginalWidth * scaleFactor);
		} else {
			// increase the image size to required width increase the height
			// proportioned to width
			scaleFactor = requiredWidth / bitmapOriginalWidth;
			requiredHeight = Math.round(bitmapOriginalHeight * scaleFactor);
			requiredWidth = Math.round(bitmapOriginalWidth * scaleFactor);
		}
		if (requiredHeight > VueApplication.getInstance()
				.getVueDetailsCardHeight()) {
			// decrease the image to card height and decrease the imageWidht
			// proportioned to height
			scaleFactor = VueApplication.getInstance()
					.getVueDetailsCardHeight() / requiredHeight;
			requiredHeight = Math.round(requiredHeight * scaleFactor);
			requiredWidth = Math.round(requiredWidth * scaleFactor);
		}

		imgDimention.mImgWidth = (int) requiredWidth;
		imgDimention.mImgHeight = (int) requiredHeight;
		// bitmap = createBitmap(bitmap, (int)requiredWidth,
		// (int)requiredHeight);
		return imgDimention;

	}

	private static Bitmap createBitmap(Bitmap bitmap, int width, int height) {
		if (width > 0 && height > 0) {
			try {
				Bitmap bmap = Bitmap.createScaledBitmap(bitmap, width, height,
						true);
				return bmap;
			} catch (Exception e) {
				e.printStackTrace();
				Log.i("bitmap1",
						"bitmap1 before exception " + bitmap.getWidth());
				e.printStackTrace();
			} catch (Throwable e) {
				Log.i("bitmap1",
						"bitmap1 before exception Throwable "
								+ bitmap.getWidth());
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public static float dipToPixels(Context context, float dipValue) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
				metrics);
	}
}
