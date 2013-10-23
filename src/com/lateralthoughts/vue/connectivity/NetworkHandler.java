package com.lateralthoughts.vue.connectivity;

import android.content.Context;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.lateralthoughts.vue.AisleManager;
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
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleComment;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.ui.ViewInfo;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	protected int mOffset;
	ArrayList<AisleWindowContent> aislesList = null;

	/* public ArrayList<String> bookmarkedAisles = new ArrayList<String>(); */
	// public ArrayList<AisleWindowContent> bookmarkedAisleContent = new
	// ArrayList<AisleWindowContent>();

	public NetworkHandler(Context context) {
		mContext = context;
		mVueContentGateway = VueContentGateway.getInstance();
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
		Log.i("formdbtrending", "formdbtrending***: requestMoreAisle");
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
			Log.i("formdbtrending", "formdbtrending: reqestByCategory");
			// Log.i("duplicateImages", "duplicateImages from db1");
			DataBaseManager.getInstance(VueApplication.getInstance())
					.resetDbParams();
			ArrayList<AisleWindowContent> aisleContentArray = mDbManager
					.getAislesFromDB(null);
			for (int i = 0; i < aisleContentArray.size(); i++) {
				for (int j = 0; j < aisleContentArray.get(i).getImageList()
						.size(); j++) {
					// Log.i("duplicateImages imageurl",
					// "duplicateImages imageurl: "+
					// aisleContentArray.get(i).getImageList().get(j).mImageUrl);
				}
				// Log.i("duplicateImages",
				// "duplicateImages imageurl########: "+i);
			}
			Log.i("formdbtrending", "formdbtrending: reqestByCategory aisleContentArray.size() "+aisleContentArray.size());
			if (aisleContentArray.size() == 0) {
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

	// request the server to create an empty aisle.
	public void requestCreateAisle(Aisle aisle,
			final AisleUpdateCallback callback) {
		AisleManager.getAisleManager().createEmptyAisle(aisle, callback);
	}

	public void requestForAddImage(boolean fromDetailsScreenFlag,
			String imageId, VueImage image, ImageAddedCallback callback) {
		AisleManager.getAisleManager().addImageToAisle(fromDetailsScreenFlag,
				imageId, image, callback);
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
					.getAislesFromDB(null);
			if (aisleContentArray.size() == 0) {
				return;
			}
			Message msg = new Message();
			msg.obj = aisleContentArray;
			mHandler.sendMessage(msg);

		} else {
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
			NotifyProgress progress, final String screenName) {

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
				Log.i("meoptions", "meoptions: MyAisle list size: "
						+ windowList.size());
				if (windowList != null && windowList.size() > 0) {
					clearList();
					for (int i = 0; i < windowList.size(); i++) {
						VueTrendingAislesDataModel.getInstance(
								VueApplication.getInstance()).addItemToList(
								windowList.get(i).getAisleContext().mAisleId,
								windowList.get(i));
					}
					VueLandingPageActivity.changeScreenName(screenName);
					VueTrendingAislesDataModel.getInstance(
							VueApplication.getInstance()).dataObserver();
				} else {
					StackViews.getInstance().pull();
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
											Log.i("myailsedebug",
													"myailsedebug: recieved my runonuithread:  ");
											if (aislesList != null
													&& aislesList.size() > 0) {
												clearList();
												Log.i("myailsedebug",
														"myailsedebug: recieved my runonuithread: if ");
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
																aislesList);

												// if this is the first set of
												// data
												// we
												// are receiving
												// go
												// ahead
												// notify the data set changed
												VueLandingPageActivity
														.changeScreenName(screenName);
												Log.i("myaisledbcheck",
														"myaisledbcheck aisle are fetching from server inserting to db success: ");
											} else {
												// if this is the first set of
												// data
												// we
												// are receiving
												// go
												// ahead
												// notify the data set changed
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
					if (userId == null) {
						Log.i("bookmarked aisle",
								"bookmarked aisle ID IS NULL RETURNING");
						return;
					}
					Log.i("bookmarked aisle", "bookmarked aisle 2 User Id; "
							+ userId);
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
							ArrayList<String> bookmarkedAisles = new Parser()
									.parseBookmarkedAisles(responseMessage);
							for (String s : bookmarkedAisles) {
								DataBaseManager.getInstance(mContext)
										.updateBookmarkAisles(s, true);
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

	/*
	 * public void addBookmarked(String aisleId){ if(aisleId != null)
	 * bookmarkedAisles.add(aisleId); }
	 */
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
					cursor.close();
					return true;
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return false;
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
		}
		return userId;

	}

	public ImageComment createImageComment(ImageComment comment)
			throws Exception {
		ImageComment createdImageComment = null;
		ObjectMapper mapper = new ObjectMapper();

		URL url = new URL(UrlConstants.CREATE_IMAGECOMMENT_RESTURL + "/"
				+ getUserId());
		HttpPut httpPut = new HttpPut(url.toString());
		StringEntity entity = new StringEntity(
				mapper.writeValueAsString(comment));
		entity.setContentType("application/json;charset=UTF-8");
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json;charset=UTF-8"));
		httpPut.setEntity(entity);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPut);
		if (response.getEntity() != null
				&& response.getStatusLine().getStatusCode() == 200) {
			String responseMessage = EntityUtils.toString(response.getEntity());
			if (responseMessage.length() > 0) {
				createdImageComment = (new ObjectMapper()).readValue(
						responseMessage, ImageComment.class);
			}
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
									retrievedImageRating = (new ObjectMapper()).readValue(
											response.toString(),
											new TypeReference<List<ImageRating>>() {
											});
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
						Log.e("get reating image",
								"SURU get reating image Error Resopnse : "
										+ error.getMessage());
					}
				});
		VueApplication.getInstance().getRequestQueue().add(vueRequest);
	}

	private void clearList() {
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

}
