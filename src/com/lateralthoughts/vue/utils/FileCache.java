package com.lateralthoughts.vue.utils;

import java.io.File;

import com.lateralthoughts.vue.VueConstants;

import android.content.Context;
import android.util.Log;

public class FileCache {

	private File cacheDir;
	File vueAppCameraPicsDir;
	File vueAppResizedImagesDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"LazyList");
			vueAppCameraPicsDir = new File(context.getExternalFilesDir(null),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			vueAppResizedImagesDir = new File(
					context.getExternalFilesDir(null),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
		} else {
			cacheDir = context.getCacheDir();
			vueAppCameraPicsDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			vueAppResizedImagesDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		if (!vueAppCameraPicsDir.exists())
			vueAppCameraPicsDir.mkdirs();
		if (!vueAppResizedImagesDir.exists())
			vueAppResizedImagesDir.mkdirs();
	}

	public File getFile(String url) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
	  int hashCode = url.hashCode();
	  Log.e("Profiling", "Profiling New hashCode : " + url.hashCode());
		String filename = String.valueOf(hashCode);
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename + ".jpg");
		return f;

	}

	public File getVueAppCameraPictureFile(String cameraImageName) {
		File f = new File(vueAppCameraPicsDir, cameraImageName + ".jpg");
		return f;
	}

	public File getVueAppResizedPictureFile(String resizedImageName) {
		File f = new File(vueAppResizedImagesDir, resizedImageName + ".jpg");
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
		File[] files = vueAppCameraPicsDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

	public void clearVueAppResizedPictures() {
		File[] files = vueAppResizedImagesDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

}