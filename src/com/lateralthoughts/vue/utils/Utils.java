package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.DataentryImage;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.VueUserProfile;

public class Utils {
	private static final String CURRENT_FONT_SIZE = "currentFontSize";
	public static final String NETWORK_SETTINGS = "networkSettings";
	public static final int LARGE_TEXT_SIZE = 22;
	public static final int MEDIUM_TEXT_SIZE = 18;
	public static final int SMALL_TEXT_SIZE = 14;
	public static final String DETAILS_SCREEN = "details_screen";
	public static final String TRENDING_SCREEN = "trending_screen";
	public static final String FLURRY_APP_KEY = "6938R8DC7R5HZWF976TJ";
	public static int MAX_RETRIES = 3;
	public static String mChangeAilseId;
	public static boolean isAisleChanged = false;
	public static boolean mAinmate = true;

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
			os.close();
			is.close();
		} catch (Exception ex) {
			Log.i("added url", "added url  InputStream got error ");
			ex.printStackTrace();
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
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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
		Log.e("getPath", "" + uri);
		Cursor cursor = activity.getContentResolver().query(uri, null, null,
				null, null);
		Log.e("cs", "5");
		if (cursor == null) { // Source is Dropbox or other similar local file
			Log.e("getPath if", "" + uri.getPath()); // path
			return uri.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			Log.e("cs", "6");
			Log.e("getPath else", "" + cursor.getString(idx) + "..?" + idx);
			return cursor.getString(idx);
		}
	}

	// String[] ... [0] is resizedImagePath, [1] originalImageWidth, [2]
	// orignalImageHeight
	public static String[] getResizedImage(File f, float screenHeight,
			float screenWidth, Context mContext) {
		try {
			String[] returnArray = new String[3];
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			Log.e("cs", "10");
			int height = o.outHeight;
			int width = o.outWidth;
			returnArray[1] = width + "";
			returnArray[2] = height + "";
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
			File resizedFileName = new File(
					vueAppResizedImageFileName(mContext));
			saveBitmap(resizedBitmap, resizedFileName);
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
			returnArray[0] = resizedFileName.getPath();
			return returnArray;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String vueAppCameraImageFileName(Context context) {
		FileCache fileCacheObj = new FileCache(context);
		if (fileCacheObj.getmVueAppCameraPicsDir() != null) {
			File fv[] = fileCacheObj.getmVueAppCameraPicsDir().listFiles();
			if (fv != null) {
				return fileCacheObj.getVueAppCameraPictureFile(
						fv.length + 1 + "").getPath();
			}
		}

		return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
	}

	public static String vueAppResizedImageFileName(Context context) {
		FileCache fileCacheObj = new FileCache(context);
		if (fileCacheObj.getmVueAppResizedImagesDir() != null) {
			File fv[] = fileCacheObj.getmVueAppResizedImagesDir().listFiles();
			if (fv != null) {
				return fileCacheObj.getVueAppResizedPictureFile(
						fv.length + 1 + "").getPath();
			}
		}

		return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
	}

	public static ImageDimension getScalledImage(Bitmap bitmap,
			int availableWidth, int availableHeight) {
		Log.i("imageSize", "imageSize originalImageHeight: " + availableHeight);
		Log.i("imageSize", "imageSize originalImageWidth: " + availableWidth);

		ImageDimension imgDimension = new ImageDimension();
		float requiredWidth, requiredHeight;
		float bitmapOriginalWidth = bitmap.getWidth();
		float bitmapOriginalHeight = bitmap.getHeight();
		float scaleFactor;
		requiredHeight = availableHeight;

		Log.i("imageSize", "imageSize cardWidth: "
				+ VueApplication.getInstance().getScreenWidth());
		Log.i("imageSize", "imageSize cardHeight: "
				+ VueApplication.getInstance().getScreenHeight());

		if ((availableWidth < VueApplication.getInstance().getScreenWidth() && availableHeight < VueApplication
				.getInstance().getScreenHeight())) {
			imgDimension.mImgWidth = availableWidth;
			imgDimension.mImgHeight = availableHeight;
			return imgDimension;
		}

		if (availableWidth > VueApplication.getInstance().getScreenWidth()) {
			requiredWidth = VueApplication.getInstance().getScreenWidth();
		} else {
			requiredWidth = availableWidth;
			// requiredWidth =
			// VueApplication.getInstance().getVueDetailsCardWidth();
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
		if (requiredHeight > VueApplication.getInstance().getScreenHeight()) {
			// decrease the image to card height and decrease the imageWidht
			// proportioned to height
			scaleFactor = VueApplication.getInstance().getScreenHeight()
					/ requiredHeight;
			requiredHeight = Math.round(requiredHeight * scaleFactor);
			requiredWidth = Math.round(requiredWidth * scaleFactor);
		}

		imgDimension.mImgWidth = (int) requiredWidth;
		imgDimension.mImgHeight = (int) requiredHeight;
		// bitmap = createBitmap(bitmap, (int)requiredWidth,
		// (int)requiredHeight);
		return imgDimension;

	}

	public static float dipToPixels(Context context, float dipValue) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
				metrics);
	}

	/***
	 * Getting Current Date...
	 * 
	 * @return
	 */
	public static String date() {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
				VueConstants.DATE_FORMAT);
		return dateFormatGmt.format(new Date());
	}

	/**
	 * 7 * 24 * 60 * 60 * 1000
	 * 
	 * Getting two weeks before time
	 */
	public static String twoWeeksBeforeTime() {
		long twoWeeksDifferenceTime = (System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
				VueConstants.DATE_FORMAT);
		return dateFormatGmt.format(new Date(twoWeeksDifferenceTime));
	}

	public static Bitmap getBitmap() {
		Bitmap icon = BitmapFactory.decodeResource(
				VueApplication.getInstance().mVueApplicationContext
						.getResources(), R.drawable.ic_launcher);
		return icon;
	}

	public static boolean appInstalledOrNot(String uri, Context context) {
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	public static String getUrlFromString(String stringWithUrl) {
		if (stringWithUrl != null) {
			String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(stringWithUrl);
			while (m.find()) {
				String urlStr = m.group();
				if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
					urlStr = urlStr.substring(1, urlStr.length() - 1);
				}
				return urlStr;
			}
		}
		return null;
	}

	public static void writeUserObjectToFile(Context context, String fileName,
			VueUser vueUser) throws Exception {
		FileOutputStream fos = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(vueUser);
		os.close();
	}

	public static VueUser readUserObjectFromFile(Context context,
			String fileName) throws Exception {
		FileInputStream fis = context.openFileInput(fileName);
		ObjectInputStream is = new ObjectInputStream(fis);
		VueUser vueUser = (VueUser) is.readObject();
		is.close();
		return vueUser;
	}

	public static void writeUserProfileObjectToFile(Context context,
			String fileName, VueUserProfile vueUserProfile) throws Exception {
		FileOutputStream fos = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(vueUserProfile);
		os.close();
	}

	public static VueUserProfile readUserProfileObjectFromFile(Context context,
			String fileName) throws Exception {
		FileInputStream fis = context.openFileInput(fileName);
		ObjectInputStream is = new ObjectInputStream(fis);
		VueUserProfile vueUserProfile = (VueUserProfile) is.readObject();
		is.close();
		return vueUserProfile;
	}

	public static void writeAisleImagePathListToFile(Context context,
			String fileName, ArrayList<DataentryImage> imagePathList)
			throws Exception {
		FileOutputStream fos = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(imagePathList);
		os.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList<DataentryImage> readAisleImagePathListFromFile(
			Context context, String fileName) throws Exception {
		FileInputStream fis = context.openFileInput(fileName);
		ObjectInputStream is = new ObjectInputStream(fis);
		ArrayList<DataentryImage> imagePathList = (ArrayList) is.readObject();
		is.close();
		return imagePathList;
	}

	public static String getDeviceId() {
		String deviceId = Secure.getString(VueApplication.getInstance()
				.getContentResolver(), Secure.ANDROID_ID);
		Log.e("Utils", "get device id method called" + deviceId);
		return deviceId;
	}

	public static boolean getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
			Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		return sharedPreferences
				.getBoolean(
						VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_CREATE_AISLESCREEN_FLAG,
						false);
	}

	public static void putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
			Context context, boolean flag) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(
				VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_CREATE_AISLESCREEN_FLAG,
				flag);
		editor.commit();
	}

	public static String getDataentryScreenAisleId(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		return sharedPreferences.getString(
				VueConstants.DATAENTRY_SCREEN_AISLE_ID, null);
	}

	public static void putDataentryScreenAisleId(Context context, String aisleId) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(VueConstants.DATAENTRY_SCREEN_AISLE_ID, aisleId);
		editor.commit();
	}

	public static boolean getDataentryAddImageAisleFlag(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		return sharedPreferences.getBoolean(
				VueConstants.DATAENTRY_ADDIMAGE_AISLE_FLAG, false);
	}

	public static void putDataentryAddImageAisleFlag(Context context,
			boolean flag) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(VueConstants.DATAENTRY_ADDIMAGE_AISLE_FLAG, flag);
		editor.commit();
	}

	public static boolean getDataentryEditAisleFlag(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		return sharedPreferences.getBoolean(
				VueConstants.DATAENTRY_EDIT_AISLE_FLAG, false);
	}

	public static void putDataentryEditAisleFlag(Context context, boolean flag) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(VueConstants.DATAENTRY_EDIT_AISLE_FLAG, flag);
		editor.commit();
	}

	public static boolean isLoadDataentryScreenFlag(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		return sharedPreferences.getBoolean(
				VueConstants.LOAD_DATAENTRY_SCREEN_FLAG, false);
	}

	public static void setLoadDataentryScreenFlag(Context context,
			boolean loadDataentryScreenFlag) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(VueConstants.LOAD_DATAENTRY_SCREEN_FLAG,
				loadDataentryScreenFlag);
		editor.commit();
	}

	public static int modifyHeightForDetailsView(
			ArrayList<AisleImageDetails> imageList) {
		int mWindowLargestHeight = 0;
		float[] imageHeightList = new float[imageList.size()];
		float availableScreenHeight = VueApplication.getInstance()
				.getVueDetailsCardHeight();
		float adjustedImageHeight, adjustedImageWidth;
		float imageHeight, imageWidth;
		float cardWidth = VueApplication.getInstance().getVueDetailsCardWidth();

		for (int i = 0; i < imageList.size(); i++) {
			imageHeight = imageList.get(i).mAvailableHeight;
			imageWidth = imageList.get(i).mAvailableWidth;
			if (imageHeight > availableScreenHeight) {
				adjustedImageHeight = availableScreenHeight;
				adjustedImageWidth = (adjustedImageHeight / imageHeight)
						* imageWidth;
				imageHeight = adjustedImageHeight;
				imageWidth = adjustedImageWidth;
			}
			if (imageWidth > cardWidth) {
				adjustedImageWidth = cardWidth;
				adjustedImageHeight = (adjustedImageWidth / imageWidth)
						* imageHeight;
				imageHeight = adjustedImageHeight;
				imageWidth = adjustedImageWidth;
			}
			imageList.get(i).mDetailsImageHeight = Math.round(imageHeight);
			imageList.get(i).mDetailsImageWidth = Math.round(imageWidth);
			imageHeightList[i] = imageHeight;

		}
		mWindowLargestHeight = (int) imageHeightList[0];

		for (int i = 0; i < imageHeightList.length; i++) {
			if (mWindowLargestHeight < imageHeightList[i]) {
				mWindowLargestHeight = (int) imageHeightList[i];

			}
		}

		return mWindowLargestHeight;
	}

	public static Bitmap getBestDementions(Bitmap bitmap, int originalWidth,
			int originalHeight, int availableScreenWidth,
			int availableScreenHeight) {
		float adjustedImageHeight, adjustedImageWidth;
		float imageHeight = originalHeight, imageWidth = originalWidth;
		if (imageHeight > availableScreenHeight) {
			adjustedImageHeight = availableScreenHeight;
			adjustedImageWidth = (adjustedImageHeight / imageHeight)
					* imageWidth;
			imageHeight = adjustedImageHeight;
			imageWidth = adjustedImageWidth;
		}
		if (imageWidth > availableScreenWidth) {
			adjustedImageWidth = availableScreenWidth;
			adjustedImageHeight = (adjustedImageWidth / imageWidth)
					* imageHeight;
			imageHeight = adjustedImageHeight;
			imageWidth = adjustedImageWidth;
		}

		return BitmapLoaderUtils.getInstance().getBitmap(bitmap,
				(int) imageWidth, (int) imageHeight);
	}

	public static String getStoreNameFromUrl(String url) {
		if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
				&& VueApplication.getInstance().mShoppingApplicationDetailsList
						.size() > 0) {
			for (int i = 0; i < VueApplication.getInstance().mShoppingApplicationDetailsList
					.size(); i++) {
				if (url.toLowerCase()
						.contains(
								VueApplication.getInstance().mShoppingApplicationDetailsList
										.get(i).getAppName().toLowerCase())) {
					return VueApplication.getInstance().mShoppingApplicationDetailsList
							.get(i).getAppName();
				}
			}
		}
		return "UnKnown";
	}

	public static void showAlertMessageForBackendNotIntegrated(
			final Activity activity, final boolean finishActivity) {
		final Dialog dialog = new Dialog(activity,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.vue_popup);
		TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText("Sorry, Server side integration is pending.");
		okButton.setText("OK");
		noButton.setVisibility(View.GONE);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (finishActivity) {
					activity.finish();
				}
			}
		});
		dialog.show();
	}

	public static ArrayList<ShoppingApplicationDetails> getInstalledApplicationsList(
			Context context) {
		ArrayList<ShoppingApplicationDetails> installedApplicationdetailsList = new ArrayList<ShoppingApplicationDetails>();
		Intent intent = new Intent("android.intent.action.MAIN", null);
		intent.addCategory("android.intent.category.LAUNCHER");
		List<ResolveInfo> activities = context.getPackageManager()
				.queryIntentActivities(intent, 0);
		PackageManager pm = context.getPackageManager();
		final Object a[] = activities.toArray();
		for (int i = 0; i < activities.size(); i++) {
			boolean isSystemApp = false;
			try {
				isSystemApp = isSystemPackage(pm
						.getPackageInfo(
								((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName,
								PackageManager.GET_ACTIVITIES));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			String packageName = ((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName;
			if (!isSystemApp
					&& !(packageName.equals("com.lateralthoughts.vue"))
					&& !(packageName.equals(VueConstants.ETSY_PACKAGE_NAME))
					&& !(packageName.equals(VueConstants.FANCY_PACKAGE_NAME))
					&& !(Arrays
							.asList(VueApplication.SHOPPINGAPP_PACKAGES_ARRAY)
							.contains(packageName))) {
				ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
						((ResolveInfo) a[i]).activityInfo.applicationInfo
								.loadLabel(context.getPackageManager())
								.toString(),
						((ResolveInfo) a[i]).activityInfo.name, packageName,
						((ResolveInfo) a[i]).activityInfo.applicationInfo
								.loadIcon(context.getPackageManager()));
				installedApplicationdetailsList.add(shoppingApplicationDetails);
			}
		}
		return installedApplicationdetailsList;
	}

	public static boolean isSystemPackage(PackageInfo pkgInfo) {
		return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;
	}

}
