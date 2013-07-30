package com.lateralthoughts.vue.utils;

import java.io.File;
import com.lateralthoughts.vue.VueConstants;
import android.content.Context;
import android.util.Log;

public class FileCache {

	private File cacheDir;
	File mVueAppCameraPicsDir;
	File mVueAppResizedImagesDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"LazyList");
			mVueAppCameraPicsDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			mVueAppResizedImagesDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
		} else {
			cacheDir = context.getCacheDir();
			mVueAppCameraPicsDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			mVueAppResizedImagesDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		if (!mVueAppCameraPicsDir.exists())
			mVueAppCameraPicsDir.mkdirs();
		if (!mVueAppResizedImagesDir.exists())
			mVueAppResizedImagesDir.mkdirs();
	}

	public File getFile(String url) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		Log.i("url dummy", "url dummy  : getFile url "+url);
		int hashCode = url.hashCode();
		Log.i("url dummy", "url dummy  : getFile url hashCode "+hashCode);
		String filename = String.valueOf(hashCode);
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename + ".jpg");
		return f;

	}

	public File getVueAppCameraPictureFile(String cameraImageName) {
		File f = new File(mVueAppCameraPicsDir, cameraImageName + ".jpg");
		return f;
	}

	public File getVueAppResizedPictureFile(String resizedImageName) {
		File f = new File(mVueAppResizedImagesDir, resizedImageName + ".jpg");
		return f;
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

	public void clearVueAppCameraPictures() {
		File[] files = mVueAppCameraPicsDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

	public void clearVueAppResizedPictures() {
		File[] files = mVueAppResizedImagesDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

}