package com.lateralthoughts.vue.connectivity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.AisleWindowContentFactory;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Image;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class NetworkHandler {

	Context mContext;
	private static final String SEARCH_REQUEST_URL = "http://2-java.vueapi-canary.appspot.com/api/getaisleswithmatchingkeyword/";
	// http://2-java.vueapi-canary-development1.appspot.com/api/
	public DataBaseManager mDbManager;
	protected VueContentGateway mVueContentGateway;
	protected TrendingAislesContentParser mTrendingAislesParser;
	private static final int NOTIFICATION_THRESHOLD = 4;
	private static final int TRENDING_AISLES_BATCH_SIZE = 10;
	public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
	private static String MY_AISLES = "aislesget/user/";
	protected int mLimit;
	public int mOffset;
	ArrayList<AisleWindowContent> aislesList = null;
	private SharedPreferences mSharedPreferencesObj;

	/* public ArrayList<String> bookmarkedAisles = new ArrayList<String>(); */
	// public ArrayList<AisleWindowContent> bookmarkedAisleContent = new
	// ArrayList<AisleWindowContent>();

	public NetworkHandler(Context context) {
		mContext = context;
		mVueContentGateway = VueContentGateway.getInstance();
		mSharedPreferencesObj = VueApplication.getInstance()
				.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
		try {
			mTrendingAislesParser = new TrendingAislesContentParser(
					new Handler(), VueConstants.AISLE_TRENDING_LIST_DATA);
		} catch (Exception e) {
		}
		mDbManager = DataBaseManager.getInstance(mContext);
		mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
		mOffset = 0;
	}

	// whle user scrolls down get next 10 aisles
	public void requestMoreAisle(boolean loadMore, String screenname) {

		if (VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance())
				.isMoreDataAvailable()) {

			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).loadOnRequest = false;

			if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
				mOffset += mLimit;
			else {
				mOffset += mLimit;
				mLimit = TRENDING_AISLES_BATCH_SIZE;
			}
			Log.i("listmovingissue", "listmovingissue***: " + mOffset);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore, screenname);
		} else {
			Log.i("offeset and limit", "offeset1: else part");
		}

	}

	// get the aisle based on the category
	public void reqestByCategory(String category, NotifyProgress progress,
			boolean fromServer, boolean loadMore, String screenname) {

		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.setNotificationProgress(progress, fromServer);
		String downLoadFromServer = "fromDb";
		if (fromServer) {
			downLoadFromServer = "fromServer";
			Log.e("DataBaseManager",
					"SURU updated aisle Order: DATABASE LODING FROM SERVER 1");
			mOffset = 0;
			mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).clearContent();
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).showProgress();
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.setMoreDataAVailable(true);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore, screenname);

		} else {
			DataBaseManager.getInstance(VueApplication.getInstance())
					.resetDbParams();
			ArrayList<AisleWindowContent> aisleContentArray = mDbManager
					.getAislesFromDB(null, false);
			if (aisleContentArray.size() == 0) {
				VueTrendingAislesDataModel.getInstance(VueApplication
						.getInstance()).isFromDb = false;
				return;
			}

			Message msg = new Message();
			msg.obj = aisleContentArray;
			VueTrendingAislesDataModel.getInstance(mContext).mHandler
					.sendMessage(msg);
		}

	}

	public static void requestTrending() {

	}

	public void requestUpdateAisle(Aisle aisle) {
		AisleManager.getAisleManager().updateAisle(aisle);
	}

	// request the server to create an empty aisle.
	public void requestCreateAisle(Aisle aisle,
			final AisleUpdateCallback callback) {
		AisleManager.getAisleManager().createEmptyAisle(aisle, callback);
	}

	public void requestForDeleteImage(Image image, String aisleId) {
		AisleManager.getAisleManager().deleteImage(image, aisleId);
	}

	public void requestForAddImage(boolean fromDetailsScreenFlag,
			String imageId, VueImage image,
			ImageAddedCallback imageAddedCallback) {
		AisleManager.getAisleManager().addImageToAisle(fromDetailsScreenFlag,
				imageId, image, imageAddedCallback);
	}

	public void requestForUploadImage(File imageFile,
			ImageUploadCallback callback) {
		AisleManager.getAisleManager().uploadImage(imageFile, callback);
	}

	// get aisles related to search keyword
	public void requestSearch(final String searchString) {
		JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
				+ searchString, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				if (null != response) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("Search result",
							response.toString());
					responseBundle.putBoolean("loadMore", false);
					mTrendingAislesParser.send(1, responseBundle);
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("Search Resopnse", "SURU Search Error Resopnse : "
						+ error.getMessage());
			}
		});
		// RETRY POLICY
		vueRequest.setRetryPolicy(new DefaultRetryPolicy(
				DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Utils.MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		VueApplication.getInstance().getRequestQueue().add(vueRequest);

	}

	public void requestUserAisles(String userId) {

		JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
				+ MY_AISLES + userId, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				if (null != response) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("Search result",
							response.toString());
					responseBundle.putBoolean("loadMore", false);
					mTrendingAislesParser.send(1, responseBundle);
				}
				Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("Search Resopnse", "SURU Search Error Resopnse : "
						+ error.getMessage());
			}
		});

		VueApplication.getInstance().getRequestQueue().add(vueRequest);

	}

	public void loadInitialData(boolean loadMore, Handler mHandler,
			String screenName) {

		Log.i("formdbtrending", "formdbtrending***: loadInitialData");
		getBookmarkAisleByUser();
		getRatedImageList();

		mOffset = 0;
		if (!VueConnectivityManager.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			ArrayList<AisleWindowContent> aisleContentArray = mDbManager
					.getAislesFromDB(null, false);
			if (aisleContentArray.size() == 0) {
				return;
			}
			Message msg = new Message();
			msg.obj = aisleContentArray;
			mHandler.sendMessage(msg);

		} else {
			mLimit = 30;
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore, screenName);
		}

	}

	public void loadTrendingAisle(boolean loadMore, boolean fromServer,
			NotifyProgress progress, String screenName) {
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.setNotificationProgress(progress, fromServer);
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.showProgress();
		mVueContentGateway.getTrendingAisles(mLimit, mOffset,
				mTrendingAislesParser, loadMore, screenName);
	}

	public void requestAislesByUser(boolean fromServer,
			final NotifyProgress progress, final String screenName) {

		mOffset = 0;
		if (!fromServer) {
			// TODO get data from local db.
			Log.i("myaisledbcheck",
					"myaisledbcheck aisle are my aisles are fetching from db $$$$: ");
			String userId = getUserId();
			if (userId != null) {
				ArrayList<AisleWindowContent> windowList = DataBaseManager
						.getInstance(VueApplication.getInstance())
						.getAislesByUserId(userId);
				if (windowList != null && windowList.size() > 0) {
					clearList(progress);
					for (AisleWindowContent aisleItem : windowList) {
						Log.i("userAisle",
								"userailse: userId: "
										+ aisleItem.getAisleContext().mUserId);
						if (!aisleItem.getAisleContext().mUserId.equals(userId)) {
							windowList.remove(aisleItem);
						}
					}
					for (int i = 0; i < windowList.size(); i++) {
						VueTrendingAislesDataModel.getInstance(
								VueApplication.getInstance()).addItemToList(
								windowList.get(i).getAisleContext().mAisleId,
								windowList.get(i));
					}
					Intent i = new Intent(VueConstants.LANDING_SCREEN_RECEIVER);
					i.putExtra(VueConstants.LANDING_SCREEN_RECEIVER_KEY,
							screenName);
					VueApplication.getInstance().sendBroadcast(i);
					VueTrendingAislesDataModel.getInstance(
							VueApplication.getInstance()).dataObserver();

				} else {

					StackViews.getInstance().pull();
					Toast.makeText(VueLandingPageActivity.landingPageActivity,
							"There are no Aisles for this User.",
							Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(VueApplication.getInstance(),
						"Unable to get user id", Toast.LENGTH_SHORT).show();
				StackViews.getInstance().pull();
			}
		} else {
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.setNotificationProgress(progress, fromServer);
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).showProgress();
			// TODO: CHANGE THIS REQUEST TO VOLLEY
			if (VueConnectivityManager.isNetworkConnected(VueApplication
					.getInstance())) {
				VueTrendingAislesDataModel.getInstance(VueApplication
						.getInstance()).loadOnRequest = false;
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							aislesList = null;
							String userId = getUserId();
							aislesList = getAislesByUser(userId);

							if (aislesList != null && aislesList.size() > 0) {
								Log.i("userAisle",
										"userailse: userId 1111: " + userId
												+ "  list Size: "
												+ aislesList.size());
								for (AisleWindowContent aisleItem : aislesList) {
									Log.i("userAisle",
											"userailse: userId: "
													+ aisleItem
															.getAisleContext().mUserId);
									if (!aisleItem.getAisleContext().mUserId
											.equals(userId)) {
										aislesList.remove(aisleItem);
									}
								}
							}
							Log.i("aislesList myaisles",
									"aislesList myaisles: " + aislesList.size());
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (VueLandingPageActivity.landingPageActivity != null) {
							VueLandingPageActivity.landingPageActivity
									.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											VueTrendingAislesDataModel
													.getInstance(VueApplication
															.getInstance()).loadOnRequest = false;
											if (aislesList != null
													&& aislesList.size() > 0) {

												Intent intent = new Intent(
														VueConstants.LANDING_SCREEN_RECEIVER);
												intent.putExtra(
														VueConstants.LANDING_SCREEN_RECEIVER_KEY,
														screenName);
												VueApplication.getInstance()
														.sendBroadcast(intent);
												clearList(progress);
												for (int i = 0; i < aislesList
														.size(); i++) {
													VueTrendingAislesDataModel
															.getInstance(
																	VueApplication
																			.getInstance())
															.addItemToList(
																	aislesList
																			.get(i)
																			.getAisleContext().mAisleId,
																	aislesList
																			.get(i));
												}
												VueTrendingAislesDataModel
														.getInstance(
																VueApplication
																		.getInstance())
														.dataObserver();
												// adding my aisle to db.
												DataBaseManager
														.getInstance(
																VueApplication
																		.getInstance())
														.addTrentingAislesFromServerToDB(
																VueApplication
																		.getInstance(),
																aislesList,
																mOffset,
																DataBaseManager.MY_AISLES);
											} else {
												StackViews.getInstance().pull();
												Toast.makeText(
														VueLandingPageActivity.landingPageActivity,
														"There are no Aisles for this User.",
														Toast.LENGTH_LONG)
														.show();
											}
											VueTrendingAislesDataModel
													.getInstance(
															VueApplication
																	.getInstance())
													.dismissProgress();

										}

									});
						}
					}
				}).start();
			} else {

				Toast.makeText(
						VueApplication.getInstance(),
						VueApplication.getInstance().getResources()
								.getString(R.string.no_network),
						Toast.LENGTH_LONG).show();
				StackViews.getInstance().pull();
			}
		}
	}

	public int getmOffset() {
		return mOffset;
	}

	public void setmOffset(int mOffset) {
		this.mOffset = mOffset;
	}

	public ArrayList<AisleWindowContent> getAislesByUser(String userId)
			throws Exception {
		// TODO: change to volley

		if (userId == null) {
			return null;
		}
		String requestUrl = UrlConstants.GET_AISLELIST_BYUSER_RESTURL + "/"
				+ userId;
		URL url = new URL(requestUrl);
		HttpGet httpGet = new HttpGet(url.toString());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getEntity() != null
				&& response.getStatusLine().getStatusCode() == 200) {
			String responseMessage = EntityUtils.toString(response.getEntity());
			Log.i("aisleWindowImageUrl",
					"aisleWindowImageUrl response Message: " + responseMessage);
			return new Parser().getUserAilseLIst(responseMessage);
		}
		return null;

	}

	public void getBookmarkAisleByUser() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
 
					String userId = getUserId();
					Log.i("bookmarked aisle",
							"bookmarked persist issue  userid: " + userId);
					if (userId == null) {
						return;
					}
					URL url = new URL(UrlConstants.GET_BOOKMARK_Aisles + "/"
							+ userId + "/" + "0");
					HttpGet httpGet = new HttpGet(url.toString());
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(httpGet);
					Log.e("bookmarked aisle",
							"bookmarked aisle response.getStatusLine().getStatusCode(); "
									+ response.getStatusLine().getStatusCode());
					if (response.getEntity() != null
							&& response.getStatusLine().getStatusCode() == 200) {
						String responseMessage = EntityUtils.toString(response
								.getEntity());
						if (responseMessage != null) {
							ArrayList<AisleBookmark> bookmarkedAisles = new Parser()
									.parseBookmarkedAisles(responseMessage);
							for (AisleBookmark aB : bookmarkedAisles) {
								Log.i("bookmarked aisle",
										"bookmarked persist issue  aisleId: "
												+ aB.getAisleId());
								DataBaseManager.getInstance(mContext)
										.updateBookmarkAisles(aB.getId(),
												Long.toString(aB.getAisleId()),
												aB.getBookmarked());
							}
						}
						// Log.e("bookmarked aisle",
						// "bookmarked aisle bookmarkedAisles size(); " +
						// bookmarkedAisles.size());
					}
				} catch (Exception e) {
					Log.i("bookmarked aisle", "bookmarked aisle 3 error: ");
					e.printStackTrace();
				}

			}
		}).start();

	}

	public boolean isAisleBookmarked(String aisleId) {
		Log.i("bookmarked aisle",
				"bookmarked my bookmarks id enter in method: " + aisleId);
		Cursor cursor = mContext.getContentResolver().query(
				VueConstants.BOOKMARKER_AISLES_URI, null,
				VueConstants.AISLE_ID + "=?", new String[] { aisleId }, null);
		if (cursor.moveToFirst()) {
			do {
				if (aisleId.equals(cursor.getString(cursor
						.getColumnIndex(VueConstants.AISLE_ID)))) {
					if (cursor
							.getInt(cursor
									.getColumnIndex(VueConstants.IS_LIKED_OR_BOOKMARKED)) == 1) {
						cursor.close();
						return true;
					} else {
						cursor.close();
						return false;
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return false;
	}

	public VueUser getUserObj() {
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(
					VueApplication.getInstance(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userId = null;
		if (storedVueUser != null) {
			userId = Long.valueOf(storedVueUser.getId()).toString();
			storedVueUser.getUserImageURL();
		}
		return storedVueUser;

	}

	public String getUserId() {
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(
					VueApplication.getInstance(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userId = null;
		if (storedVueUser != null) {
			userId = Long.valueOf(storedVueUser.getId()).toString();
			storedVueUser.getUserImageURL();
		}
		return userId;
	}

	public ImageComment createImageComment(ImageCommentRequest comment)
			throws Exception {
		ImageComment createdImageComment = null;
		ObjectMapper mapper = new ObjectMapper();
		Log.e("NetworkHandler", "Comments Issue: createImageComment()");
		if (VueConnectivityManager.isNetworkConnected(mContext)) {
			Log.e("NetworkHandler", "Comments Issue: Network is there");
			URL url = new URL(UrlConstants.CREATE_IMAGECOMMENT_RESTURL + "/"
					+ Long.valueOf(getUserObj().getId()).toString());
			HttpPut httpPut = new HttpPut(url.toString());
			StringEntity entity = new StringEntity(
					mapper.writeValueAsString(comment));
			System.out.println("ImageComment create request: "
					+ mapper.writeValueAsString(comment));
			entity.setContentType("application/json;charset=UTF-8");
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			httpPut.setEntity(entity);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpPut);
			if (response.getEntity() != null
					&& response.getStatusLine().getStatusCode() == 200) {
				Log.e("NetworkHandler", "Comments Issue: got success responce");
				String responseMessage = EntityUtils.toString(response
						.getEntity());
				System.out.println("Comment Response: " + responseMessage);
				Log.i("createimageCommenterUrl",
						"createimageCommenterUrl res: " + responseMessage);
				if (responseMessage.length() > 0) {
					Log.e("NetworkHandler",
							"Comments Issue: responseMessage size is > 0 responseMessage: "
									+ responseMessage);
					createdImageComment = (new ObjectMapper()).readValue(
							responseMessage, ImageComment.class);
					Editor editor = mSharedPreferencesObj.edit();
					editor.putBoolean(VueConstants.IS_COMMENT_DIRTY, false);
					editor.commit();
					DataBaseManager.getInstance(mContext).addComments(
							createdImageComment, false);
				}
			} else {
				Log.e("NetworkHandler", "Comments Issue: responce fail: "
						+ response.getStatusLine().getStatusCode());
			}
		} else {
			Editor editor = mSharedPreferencesObj.edit();
			editor.putBoolean(VueConstants.IS_COMMENT_DIRTY, true);
			editor.commit();
			createdImageComment = new ImageComment();
			createdImageComment.setComment(comment.getComment());
			createdImageComment.setLastModifiedTimestamp(comment
					.getLastModifiedTimestamp());
			createdImageComment.setOwnerImageId(comment.getOwnerImageId());
			createdImageComment.setOwnerUserId(comment.getOwnerUserId());
			DataBaseManager.getInstance(mContext).addComments(
					createdImageComment, true);
		}
		return createdImageComment;
	}

	public void getCommentsFromDb(String aisleId) {
		Map<Long, ArrayList<String>> commentsMap = new HashMap<Long, ArrayList<String>>();
		String imageId = null;
		Object temp;
		ArrayList<String> tempComments;
		tempComments = commentsMap.remove(Long.parseLong(imageId));
		if (tempComments == null) {
			tempComments = new ArrayList<String>();
			tempComments.add("add value from cursor");
		} else {
			tempComments.add("add value from cursor");
		}
		commentsMap.put(Long.parseLong(imageId), tempComments);

	}

	public void getRatedImageList() {
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		JsonArrayRequest vueRequest = new JsonArrayRequest(
				UrlConstants.GET_RATINGS_RESTURL + "/" + userId + "/" + 0L,
				new Response.Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray response) {
						if (null != response) {
							ArrayList<ImageRating> retrievedImageRating = null;
							if (response.length() > 0) {
								try {
									retrievedImageRating = Parser
											.parseRatedImages(response
													.toString());
									DataBaseManager.getInstance(mContext)
											.insertRatedImages(
													retrievedImageRating);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						Log.e("get reating image Resopnse",
								"SURU get reating image Resopnse : " + response);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("get rating image",
								"SURU get rating image Error Resopnse : "
										+ error.getMessage());
					}
				});
		VueApplication.getInstance().getRequestQueue().add(vueRequest);
	}

	private void clearList(NotifyProgress progress) {
		if (progress != null) {
			progress.clearBrowsers();
		}
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.clearAisles();
		AisleWindowContentFactory.getInstance(VueApplication.getInstance())
				.clearObjectsInUse();
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.dataObserver();
	}

	public void makeOffseZero() {
		mOffset = 0;
	}
	/*
	 * public void setOffset(int offset){ mOffset = offset;
	 * Log.i("listmovingissue", "listmovingissue  setting to: "+mOffset); }
	 */

}
