package com.lateralthoughts.vue.utils;

import java.io.File;
import java.util.Date;

import com.lateralthoughts.vue.VueConstants;
import android.content.Context;
import android.util.Log;

public class FileCache {

	private File cacheDir;
	private long twoDaysOldTime = 2 * 24 * 60 * 60 * 1000;
	File mVueAppCameraPicsDir;
	File mVueAppResizedImagesDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					/*android.os.Environment.getExternalStorageDirectory()*/
			        context.getExternalCacheDir(),
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
		 
		int hashCode = url.hashCode();
		 Log.i("added url", "added url filechace hascode: "+hashCode);
		 Log.i("added url", "added url filechace: "+url);
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
   
	public void clearTwoDaysOldPictures() {
	  File[] files = cacheDir.listFiles();
	  if(files == null) {
	    return;
	  }
	  Log.e("Profiling", "Profiling files Array size: " + files.length);
	  int count = 0;
	  for(File f : files) {
	   long lastModifidedDate = f.lastModified();
	   Log.e("Profiling", "Profiling deleting two lastModifidedDate : " + new Date(lastModifidedDate));
	   if(System.currentTimeMillis() - lastModifidedDate >= twoDaysOldTime) {
	     Log.e("Profiling", "Profiling deleting two days old images");
	     if(f.delete()) {
	       Log.e("Profiling", "Profiling Total images Deleted: " + ++count);
	     }
	   }
	  }
	}
}