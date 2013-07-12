package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.lateralthoughts.vue.CreateAisleSelectionActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

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

	private static Bitmap getResizedBitmap(Bitmap bm, int reqWidth,
			int reqHeight) {
		int originalWidth = bm.getWidth();
		int originalHeight = bm.getHeight();
		float scaleWidth;
		float scaleHeight = 0;
		float aspect = (float) originalWidth / originalHeight;
		if (originalWidth > reqWidth) {
			scaleWidth = reqWidth;
			scaleHeight = scaleWidth / aspect;
		} else if (originalHeight > reqHeight) {
			scaleHeight = reqHeight;
			scaleWidth = scaleHeight / aspect;
		} else {
			// expand the image to the screen size
			scaleWidth = reqWidth;
			scaleHeight = scaleWidth / aspect;
			while (scaleHeight > reqHeight) {
				// if the imagewidht is very less then image height may increase
				// to large number to make sure
				// the height is always below the reqired hieght.
				reqWidth = reqWidth - 10;
				scaleWidth = reqWidth;
				scaleHeight = scaleWidth / aspect;
			}
		}
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth / originalWidth, scaleHeight
				/ originalHeight);
		// recreate the new Bitmap
		System.gc();
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, originalWidth,
				originalHeight, matrix, true);
		// bm.recycle();
		return resizedBitmap;
	}

	public static Bitmap getScaledBitMap(Bitmap bitmap, int reqWidth,
			int reqHeight) {
		/*
		 * Log.i("bitmaptest", "bitmaptest12: coming reqWidth: "+reqWidth);
		 * Log.i("bitmaptest", "bitmaptest12: coming reqHeight: "+reqHeight);
		 * bitmap = getResizedBitmap(bitmap, reqWidth, reqHeight); return
		 * bitmap;
		 */
		float bitmapwidth, bitmapheight;
		float newWidth, newHeight;
		bitmapwidth = bitmap.getWidth();
		bitmapheight = bitmap.getHeight();
		if (bitmapwidth > reqWidth) {
			newWidth = (bitmapwidth * reqWidth) / bitmapwidth;
			newHeight = (bitmapheight * reqWidth) / bitmapwidth;
		} else {
			newWidth = reqWidth;
			newHeight = reqHeight;
		}
		if (newHeight > reqHeight) {
			newHeight = (bitmapheight * reqHeight) / bitmapheight;
			newWidth = (bitmapwidth * reqHeight) / bitmapheight;
		}
		int x = Math.round(newWidth);
		int y = Math.round(newHeight);
		return createBitmap(bitmap, x, y);
		// return createBitmap(bitmap,xnew,ynew);
		// return bitmap;
	}

	private static Bitmap createBitmap(Bitmap bitmap, int width, int height) {
		if (width > 0 && height > 0) {
			try {
				Bitmap bmap = Bitmap.createScaledBitmap(bitmap, width, height,
						true);
				return bmap;
			} catch (Exception e) {
				e.printStackTrace();

			} catch (Throwable e) {

			}
		}
		return bitmap;
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
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);

		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static void saveImage(File f, float screenHeight, float screenWidth) {
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

			if (height > screenHeight) {
				// Calculate ratios of height and width to requested height and
				// width
				heightRatio = Math.round((float) height / (float) screenHeight);
			scale = heightRatio;
			}

			/*if (width > screenWidth) {
				// Calculate ratios of height and width to requested height and
				// width
				widthRatio = Math.round((float) width / (float) screenWidth);
			}*/

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
		//	scale = heightRatio < widthRatio ? heightRatio : widthRatio;

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap resizedbitmap = BitmapFactory
					.decodeStream(stream2, null, o2);
			stream2.close();
			FileOutputStream out = new FileOutputStream(f);
			resizedbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			resizedbitmap.recycle();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String vueAppCameraImageFileName(Context context) {
		FileCache fileCacheObj = new FileCache(context);
		if (fileCacheObj.vueAppCameraPicsDir != null) {
			File fv[] = fileCacheObj.vueAppCameraPicsDir.listFiles();
			if (fv != null) {
				return fileCacheObj.getVueAppCameraPictureFile(fv.length + 1 + "").getPath();
			}
		}

		return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
	}

}
