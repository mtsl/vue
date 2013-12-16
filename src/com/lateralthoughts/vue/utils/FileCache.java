package com.lateralthoughts.vue.utils;

import java.io.File;
import android.content.Context;
import com.lateralthoughts.vue.VueConstants;

public class FileCache {

	private File cacheDir;
	private long twoDaysOldTime = 2 * 24 * 60 * 60 * 1000;
	private File mVueAppCameraPicsDir;

	public File getmVueAppCameraPicsDir() {
		return mVueAppCameraPicsDir;
	}

	public void setmVueAppCameraPicsDir(File mVueAppCameraPicsDir) {
		this.mVueAppCameraPicsDir = mVueAppCameraPicsDir;
	}

	public File getmVueAppResizedImagesDir() {
		return mVueAppResizedImagesDir;
	}

	public void setmVueAppResizedImagesDir(File mVueAppResizedImagesDir) {
		this.mVueAppResizedImagesDir = mVueAppResizedImagesDir;
	}

	private File mVueAppResizedImagesDir;

	private File mVueUserProfileImageDir;

	public File getmVueUserProfileImageDir() {
		return mVueUserProfileImageDir;
	}

	public void setmVueUserProfileImageDir(File mVueUserProfileImageDir) {
		this.mVueUserProfileImageDir = mVueUserProfileImageDir;
	}

	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(context.getExternalCacheDir(), "LazyList");
			mVueAppCameraPicsDir = new File(context.getExternalFilesDir(null),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			mVueAppResizedImagesDir = new File(
					context.getExternalFilesDir(null),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
			mVueUserProfileImageDir = new File(
					context.getExternalFilesDir(null),
					VueConstants.VUE_APP_USER_PROFILE_PICTURES_FOLDER);
		} else {
			cacheDir = context.getCacheDir();
			mVueAppCameraPicsDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
			mVueAppResizedImagesDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
			mVueUserProfileImageDir = new File(context.getFilesDir(),
					VueConstants.VUE_APP_USER_PROFILE_PICTURES_FOLDER);
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		if (!mVueAppCameraPicsDir.exists())
			mVueAppCameraPicsDir.mkdirs();
		if (!mVueAppResizedImagesDir.exists())
			mVueAppResizedImagesDir.mkdirs();
		if (!mVueUserProfileImageDir.exists())
			mVueUserProfileImageDir.mkdirs();
	}

	public File getFile(String url) {
		int hashCode = url.hashCode();
		String filename = String.valueOf(hashCode);
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

	public File getVueAppUserProfilePictureFile(String imageName) {
		File f = new File(mVueUserProfileImageDir, imageName + ".jpg");
		return f;
	}

	@Deprecated
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
		if (files == null) {
			return;
		}
		for (File f : files) {
			long lastModifidedDate = f.lastModified();
			if (System.currentTimeMillis() - lastModifidedDate >= twoDaysOldTime) {
				if (f.delete()) {
				}
			}
		}
	}
}