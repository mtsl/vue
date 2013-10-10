package com.lateralthoughts.vue;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.facebook.model.GraphUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

//FB imports
//java util imports

public class VueUserManager {
	public interface UserUpdateCallback {
		public void onUserUpdated(VueUser user);
	}

	// private String VUE_API_BASE_URI =
	// "http://2-java.vueapi-canary-development1.appspot.com/";
	// private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";

	private String USER_CREATE_ENDPOINT = "api/usercreate/trial";
	private String GPLUS_USER_CREATE_ENDPOINT = "api/usercreate/googleplus";
	private String INSTAGRAM_USER_CREATE_ENDPOINT = "api/usercreate/instagram";
	private String FB_USER_CREATE_ENDPOINT = "api/usercreate/facebook/";
	private String USER_UPDATE_FB = "api/userupdate/facebook/";

	public enum PreferredIdentityLayer {
		INSTAGRAM, GPLUS, FB, DEVICE_ID, GPLUS_FB, GPLUS_INSTAGRAM, FB_INSTAGRAM, ALL_IDS_AVAILABLE // always
																									// keep
																									// this
		// enum the last. If
		// you don't know
		// what you are
		// doing don't muck
		// around in here
	}

	private static VueUserManager sUserManager = null;
	private VueUser mCurrentUser;

	private VueUserManager() {
		mCurrentUser = null;
	}

	public static VueUserManager getUserManager() {
		if (null == sUserManager)
			sUserManager = new VueUserManager();
		return sUserManager;
	}

	private void setCurrentUser(VueUser user) {
		mCurrentUser = user;
	}

	// get the currently logged in VueUser object. Even if the user chose not to
	// identify themselves we would have
	// created an empty VueUser object. In this case, the getIdentity() API will
	// return DEVICE_ID to indicate that
	// this user is identified only by this device id.
	public VueUser getCurrentUser() {
		return mCurrentUser;
	}

	// create an unidentified VueUser object. This is an asynchronous API and
	// needs to make a round trip
	// network call.
	// Usually this call cannot be invoked when mCurrentUser is set to a valid
	// value. This is because we can only
	// have only current user at a time. When this call returns the
	// UserUpdateCallback's onUserUpdated API will
	// be invoked and the VueUser object is created and set at that point.
	public void createUnidentifiedUser(final String userInitals,
			final String deviceId, final UserUpdateCallback callback) {
		if (null != mCurrentUser || null == callback)
			throw new RuntimeException(
					"Cannot call createFBIdentifiedUser when User is "
							+ "already available. Try the update APIs");

		Response.Listener listener = new Response.Listener<String>() {

			@Override
			public void onResponse(String jsonArray) {
				Log.e("VueUserDebug", "vueuser: response listener ");
				if (null != jsonArray) {
					Log.e("Profiling", "Create User: Profiling : onResponse()"
							+ jsonArray);

					try {
						Log.i("userid",
								"userid123456 null check storedVueUser response1: ");
						JSONObject user = new JSONObject(jsonArray);
						// JSONObject user = userInfo.getJSONObject("user");
						String id = user.getString("id");
						String email = user.getString("email");
						String firstName = user.getString("firstName");
						String lastName = user.getString("lastName");
						String deviceId = user.getString("deviceId");
						Log.i("userid",
								"userid123456 null check storedVueUser response2: ");
						VueUser vueUser = new VueUser(null, null, null,
								Utils.getDeviceId(), null);
						vueUser.setVueUserId(id);
						vueUser.setFirstName(userInitals);
						VueApplication.getInstance().setmUserInitials(
								userInitals);
						vueUser.setUserIdentityMethod(PreferredIdentityLayer.DEVICE_ID);
						VueUserManager.this.setCurrentUser(vueUser);
						Log.i("imageurl", "imageurl is ok got user id: "
								+ vueUser);
						callback.onUserUpdated(vueUser);
					} catch (JSONException ex) {
						Log.i("userid",
								"userid123456 null check storedVueUser response exception: ");
						ex.printStackTrace();
					}

				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("VueUserDebug", "vueuser: error listener ");
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};

		try {
			Log.e("VueUserDebug", "vueuser: method called ");
			/*
			 * ObjectMapper mapper = new ObjectMapper(); VueUser
			 * vueuserRequestObject = new VueUser(deviceId,
			 * "FACEBOOK_ID_UNKNOWN", "GOOGLE_PLUS_ID_UNKNOWN", "", userInitals,
			 * ""); String userAsString = mapper
			 * .writeValueAsString(vueuserRequestObject);
			 */
			// Log.e("VueUserDebug", "vueuser: request " + userAsString);
			String s = "{\"email\":\"\",\"firstName\":"
					+ userInitals + ",\"lastName\":\"\",\"deviceId\":"
					+ deviceId + "}";
			UserCreateRequest request = new UserCreateRequest(s, listener,
					errorListener);
			VueApplication.getInstance().getRequestQueue().add(request);
		} catch (Exception e) {

		}

	}

	public void createFBIdentifiedUser(final GraphUser graphUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.

		/*
		 * if (null != mCurrentUser) throw new RuntimeException(
		 * "Cannot call createFBIdentifiedUser when User is " +
		 * "already available. Try the update APIs");
		 */

		final VueUser user = parseGraphUserIntoVueUser(graphUser);
		Response.Listener listener = new Response.Listener<String>() {
			GraphUser graphUser;

			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					VueUserManager.this.setCurrentUser(user);
					user.setUserIdentityMethod(PreferredIdentityLayer.FB);
					callback.onUserUpdated(user);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ FB_USER_CREATE_ENDPOINT + user.getFacebookId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);

	}

	public void createGooglePlusIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.

		/*
		 * if (null != mCurrentUser) throw new RuntimeException(
		 * "Cannot call createFBIdentifiedUser when User is " +
		 * "already available. Try the update APIs");
		 */
		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					VueUserManager.this.setCurrentUser(vueUser);
					vueUser.setUserIdentityMethod(PreferredIdentityLayer.GPLUS);
					callback.onUserUpdated(vueUser);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ GPLUS_USER_CREATE_ENDPOINT + vueUser.getGooglePlusId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);

	}

	public void createInstagramIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.

		/*
		 * if (null != mCurrentUser) throw new RuntimeException(
		 * "Cannot call createFBIdentifiedUser when User is " +
		 * "already available. Try the update APIs");
		 */
		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					VueUserManager.this.setCurrentUser(vueUser);
					vueUser.setUserIdentityMethod(PreferredIdentityLayer.INSTAGRAM);
					callback.onUserUpdated(vueUser);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ INSTAGRAM_USER_CREATE_ENDPOINT + vueUser.getInstagramId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);

	}

	public void updateFBIdentifiedUser(final GraphUser graphUser,
			final VueUser vueUser, final UserUpdateCallback callback) {

		final VueUser user = parseGraphUserIntoVueUser(graphUser);
		Response.Listener listener = new Response.Listener<String>() {
			GraphUser graphUser;

			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					user.setVueUserId(vueUser.getVueId());
					user.setUserIdentityMethod(vueUser.getUserIdentity());
					user.setDeviceId(vueUser.getDeviceId());
					user.setGooglePlusId(vueUser.getGooglePlusId());
					VueUserManager.this.setCurrentUser(user);
					callback.onUserUpdated(user);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ FB_USER_CREATE_ENDPOINT + user.getFacebookId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);

	}

	public void updateGooglePlusIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.

		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					VueUserManager.this.setCurrentUser(vueUser);
					callback.onUserUpdated(vueUser);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ GPLUS_USER_CREATE_ENDPOINT + vueUser.getGooglePlusId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);

	}

	public void updateInstagramIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.

		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonArray) {
				if (null != jsonArray) {
					VueUserManager.this.setCurrentUser(vueUser);
					callback.onUserUpdated(vueUser);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		String requestUrl = UrlConstants.SERVER_BASE_URL
				+ INSTAGRAM_USER_CREATE_ENDPOINT + vueUser.getInstagramId();
		UserCreateRequest request = new UserCreateRequest(null, listener,
				errorListener);
		VueApplication.getInstance().getRequestQueue().add(request);
	}

	public void updateUnidentifiedUser(PreferredIdentityLayer newIdentity,
			String idValue, UserUpdateCallback callback) {

	}

	private class UserCreateRequest extends Request<String> {
		// ... other methods go here
		Response.Listener<String> mListener;
		Response.ErrorListener mErrorListener;
		private String muserAsString;
		private StringEntity mEntity;

		public UserCreateRequest(String userAsString,
				Response.Listener<String> listener,
				Response.ErrorListener errorListener) {
			super(Method.PUT, UrlConstants.SERVER_BASE_URL
					+ USER_CREATE_ENDPOINT, errorListener);
			mListener = listener;
			mErrorListener = errorListener;
			muserAsString = userAsString;
			try {
				mEntity = new StringEntity(muserAsString);
			} catch (UnsupportedEncodingException ex) {
			}
		}

		@Override
		public String getBodyContentType() {
			return mEntity.getContentType().getValue();
		}

		@Override
		public byte[] getBody() throws AuthFailureError {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				mEntity.writeTo(bos);
			} catch (IOException e) {
				VolleyLog.e("IOException writing to ByteArrayOutputStream");
			}
			return bos.toByteArray();
		}

		@Override
		public Map<String, String> getHeaders() {
			HashMap<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", "application/json");
			return headersMap;
		}

		@Override
		protected Response<String> parseNetworkResponse(NetworkResponse response) {
			String parsed;
			try {
				parsed = new String(response.data,
						HttpHeaderParser.parseCharset(response.headers));
			} catch (UnsupportedEncodingException e) {
				parsed = new String(response.data);
			}
			return Response.success(parsed,
					HttpHeaderParser.parseCacheHeaders(response));
		}

		@Override
		protected void deliverResponse(String s) {
			mListener.onResponse(s);
			Log.e("VueUser", "response = " + s);
		}

		@Override
		public void deliverError(VolleyError error) {
			mErrorListener.onErrorResponse(error);
		}

	}

	public void fetchUserDataFromLocalId(String id,
			final UserUpdateCallback callback) {
		// https://1-java.vueapi-canary-development1.appspot.com/api/userget/id/5707702298738688

		String requestUrlBase = UrlConstants.SERVER_BASE_URL
				+ "api/userget/id/" + id;
		String requestUrl = requestUrlBase;
		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonString) {
				if (null != jsonString) {
					Log.e("Vue App", "jsonString = " + jsonString);
					VueUser user = parseVueUserInfo(jsonString);
					callback.onUserUpdated(user);
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("VueNetworkError",
						"Vue encountered network operations error. Error = "
								+ error.networkResponse);
			}
		};
		StringRequest vueRequest = new StringRequest(requestUrl, listener,
				errorListener) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				HashMap<String, String> headersMap = new HashMap<String, String>();
				headersMap.put("Accept-Encoding", "gzip");
				headersMap.put("Content-Type", "application/json");
				return headersMap;
			}
		};

		VueApplication.getInstance().getRequestQueue().add(vueRequest);
	}

	private VueUser parseGraphUserIntoVueUser(GraphUser graphUser) {
		if (null == graphUser) {
			throw new NullPointerException(
					"GraphUser was set to null in parseGraphUserIntoVueUser");
		}
		VueUser vueUser = null;
		try {
			String firstName = graphUser.getFirstName();
			String lastName = graphUser.getLastName();
			String birthday = graphUser.getBirthday();
			String facebookUserId = graphUser.getUsername();
			JSONObject innerObject = graphUser.getInnerJSONObject();
			String email;
			try {
				email = innerObject
						.getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY);
			} catch (Exception e) {
				email = graphUser.getUsername();
				e.printStackTrace();
			}
			String username = graphUser.getName();
			String facebookId = graphUser.getId();
			vueUser = new VueUser(facebookUserId, null, null, null, email);
			vueUser.setFirstName(firstName);
			vueUser.setLastName(lastName);
			vueUser.setBirthday(birthday);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return vueUser;
	}

	private VueUser parseVueUserInfo(String jsonString) {
		VueUser user = null;
		// {"user":{"id":5707702298738688,"email":null,"firstName":null,"lastName":null,
		// "joinTime":1375333802621,"deviceId":null,"facebookId":"FACEBOOK_ID_UNKNOWN","
		// googlePlusId":"GOOGLE_PLUS_ID_UNKNOWN"}}

		if (null == jsonString)
			throw new NullPointerException(
					"parseVueUserInfo invoked with null object");

		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			String userString = jsonObject.optString("user");
			JSONObject userObject = jsonObject.getJSONObject("user");
			String id = userObject.optString("id");
			String email = userObject.optString("email");
			String firstName = userObject.optString("firstName");
			String lastName = userObject.optString("lastName");
			String deviceId = userObject.optString("deviceId");
			user = new VueUser(null, null, null, null, email);
			user.setUserIdentityMethod(PreferredIdentityLayer.DEVICE_ID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user;
	}
}
