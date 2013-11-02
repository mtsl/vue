package com.lateralthoughts.vue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.facebook.model.GraphUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.connectivity.NetworkHandler;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VueUserManager {
	public interface UserUpdateCallback {
		public void onUserUpdated(VueUser user);
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
          VueUser vueUser = new Parser().parseUserData(jsonArray);
          if (vueUser != null) {
            VueApplication.getInstance().setmUserInitials(userInitals);
            VueUserManager.this.setCurrentUser(vueUser);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser);
            callback.onUserUpdated(vueUser);
            try {
              VueTrendingAislesDataModel.getInstance(
                  VueApplication.getInstance()).dataObserver();
            } catch (Exception e) {
              e.printStackTrace();
            }
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
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
			ObjectMapper mapper = new ObjectMapper();
			VueUser newUser = new VueUser();
			newUser.setFirstName(userInitals);
			newUser.setLastName("");
			newUser.setDeviceId(deviceId);
			String userAsString = mapper.writeValueAsString(newUser);
			Log.e("VueUserDebug", "vueuser: request " + userAsString);
			UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
					userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
					errorListener);
			VueApplication.getInstance().getRequestQueue().add(request);
		} catch (Exception e) {

		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createFBIdentifiedUser(final GraphUser graphUser,
      final UserUpdateCallback callback) {
    // lets throw an exception if the current user is not NULL.
    if (null != mCurrentUser)
      throw new RuntimeException(
          "Cannot call createFBIdentifiedUser when User is "
              + "already available. Try the update APIs");
    final VueUser user = parseGraphUserIntoVueUser(graphUser, null);
    final Response.Listener listener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser = new Parser().parseUserData(jsonArray);
          if (vueUser != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser.getId());
            VueUserManager.this.setCurrentUser(vueUser);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser);
            callback.onUserUpdated(vueUser);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          }
        }
      }
    };
    final Response.ErrorListener errorListener = new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        if (null != error.networkResponse && null != error.networkResponse.data) {
          String errorData = error.networkResponse.data.toString();
          Log.e("VueUserDebug", "error date = " + errorData);
        }
      }
    };
    Response.Listener getListener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser = new Parser().parseUserData(jsonArray);
          if (vueUser != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser.getId());
            VueUserManager.this.setCurrentUser(vueUser);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser);
            callback.onUserUpdated(vueUser);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          } else {
            try {
              Log.e("VueUserDebug", "vueuser: method called ");
              ObjectMapper mapper = new ObjectMapper();
              String userAsString = mapper.writeValueAsString(user);
              Log.e("VueUserDebug", "vueuser: request " + userAsString);
              UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                  userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
                  errorListener);
              VueApplication.getInstance().getRequestQueue().add(request);
            } catch (Exception e) {

            }

          }
        } else {
          try {
            Log.e("VueUserDebug", "vueuser: method called ");
            ObjectMapper mapper = new ObjectMapper();
            String userAsString = mapper.writeValueAsString(user);
            Log.e("VueUserDebug", "vueuser: request " + userAsString);
            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
                errorListener);
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {

          }

        }
      }
    };
    Response.ErrorListener getErrorListener = new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        if (null != error.networkResponse && null != error.networkResponse.data) {
          String errorData = error.networkResponse.data.toString();
          Log.e("VueUserDebug", "error date = " + errorData);
        }
      }
    };
    UserGetRequest userGetRequest = new UserGetRequest(
        UrlConstants.GET_USER_FACEBOOK_ID_RESTURL + user.getFacebookId(),
        getListener, getErrorListener);
    VueApplication.getInstance().getRequestQueue().add(userGetRequest);
  }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void createGooglePlusIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// lets throw an exception if the current user is not NULL.
		if (null != mCurrentUser)
			throw new RuntimeException(
					"Cannot call createFBIdentifiedUser when User is "
							+ "already available. Try the update APIs");

    final Response.Listener listener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser2 = new Parser().parseUserData(jsonArray);
          if (vueUser2 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser2.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser2.getId());
            VueUserManager.this.setCurrentUser(vueUser2);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser2);
            callback.onUserUpdated(vueUser2);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          }
        }
      }
    };
		final Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};

    Response.Listener getListener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser1 = new Parser().parseUserData(jsonArray);
          if (vueUser1 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser1.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser1.getId());
            VueUserManager.this.setCurrentUser(vueUser1);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser1);
            callback.onUserUpdated(vueUser1);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          } else {
            try {
              Log.e("VueUserDebug", "vueuser: method called ");
              ObjectMapper mapper = new ObjectMapper();
              String userAsString = mapper.writeValueAsString(vueUser);
              Log.e("VueUserDebug", "vueuser: request " + userAsString);
              UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                  userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
                  errorListener);
              VueApplication.getInstance().getRequestQueue().add(request);
            } catch (Exception e) {

            }
          }
        } else {
          try {
            Log.e("VueUserDebug", "vueuser: method called ");
            ObjectMapper mapper = new ObjectMapper();
            String userAsString = mapper.writeValueAsString(vueUser);
            Log.e("VueUserDebug", "vueuser: request " + userAsString);
            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
                errorListener);
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {

          }
        }

      }
    };
		Response.ErrorListener getErrorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		UserGetRequest userGetRequest = new UserGetRequest(
				UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
						+ vueUser.getGooglePlusId(), getListener,
				getErrorListener);
		VueApplication.getInstance().getRequestQueue().add(userGetRequest);
	}

	public void createInstagramIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// TODO Need to add instagram id in the backend.
		/*
		 * // lets throw an exception if the current user is not NULL. if (null
		 * != mCurrentUser) throw new RuntimeException(
		 * "Cannot call createFBIdentifiedUser when User is " +
		 * "already available. Try the update APIs");
		 * 
		 * Response.Listener listener = new Response.Listener<String>() {
		 * 
		 * @Override public void onResponse(String jsonArray) { if (null !=
		 * jsonArray) { Log.e("Profiling",
		 * "Create User: Profiling : onResponse()" + jsonArray); VueUser vueUser
		 * = new Parser().parseUserData(jsonArray); if
		 * (VueApplication.getInstance().getmUserInitials() == null) {
		 * VueApplication.getInstance().setmUserInitials(
		 * vueUser.getFirstName()); }
		 * VueUserManager.this.setCurrentUser(vueUser); Log.i("imageurl",
		 * "imageurl is ok got user id: " + vueUser);
		 * callback.onUserUpdated(vueUser); } } }; Response.ErrorListener
		 * errorListener = new Response.ErrorListener() {
		 * 
		 * @Override public void onErrorResponse(VolleyError error) { if (null
		 * != error.networkResponse && null != error.networkResponse.data) {
		 * String errorData = error.networkResponse.data.toString();
		 * Log.e("VueUserDebug", "error date = " + errorData); } } };
		 * 
		 * try { Log.e("VueUserDebug", "vueuser: method called "); ObjectMapper
		 * mapper = new ObjectMapper(); String userAsString =
		 * mapper.writeValueAsString(vueUser); Log.e("VueUserDebug",
		 * "vueuser: request " + userAsString); UserCreateRequest request = new
		 * UserCreateRequest(userAsString, UrlConstants.CREATE_USER_RESTURL,
		 * listener, errorListener);
		 * VueApplication.getInstance().getRequestQueue().add(request); } catch
		 * (Exception e) {
		 * 
		 * }
		 */
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateFBIdentifiedUser(final GraphUser graphUser,
			final VueUser vueUser, final UserUpdateCallback callback) {
		final VueUser user = parseGraphUserIntoVueUser(graphUser, vueUser);
    final Response.Listener listener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser1 = new Parser().parseUserData(jsonArray);
          if (vueUser1 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser1.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser1.getId());
            VueUserManager.this.setCurrentUser(vueUser1);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser1);
            callback.onUserUpdated(vueUser1);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          }
        }
      }
    };
		final Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};

    Response.Listener getListener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser2 = new Parser().parseUserData(jsonArray);
          if (vueUser2 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser2.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser2.getId());
            VueUserManager.this.setCurrentUser(vueUser2);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser2);
            callback.onUserUpdated(vueUser2);
            showNotificationForSwitchingUser(String.valueOf(vueUser.getId()));
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          } else {
            try {
              Log.e("VueUserDebug", "vueuser: method called ");
              ObjectMapper mapper = new ObjectMapper();
              String userAsString = mapper.writeValueAsString(user);
              Log.e("VueUserDebug", "vueuser: request " + userAsString);
              UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                  userAsString, UrlConstants.UPDATE_USER_RESTURL, listener,
                  errorListener);
              VueApplication.getInstance().getRequestQueue().add(request);
            } catch (Exception e) {

            }

          }
        } else {
          try {
            ObjectMapper mapper = new ObjectMapper();
            String userAsString = mapper.writeValueAsString(user);
            Log.e("VueUserDebug", "vueuser: request " + userAsString);
            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                userAsString, UrlConstants.UPDATE_USER_RESTURL, listener,
                errorListener);
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {

          }

        }
      }
    };
		Response.ErrorListener getErrorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		UserGetRequest userGetRequest = new UserGetRequest(
				UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
						+ user.getFacebookId(), getListener, getErrorListener);
		VueApplication.getInstance().getRequestQueue().add(userGetRequest);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateGooglePlusIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
    final Response.Listener listener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser1 = new Parser().parseUserData(jsonArray);
          if (vueUser1 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser1.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser1.getId());
            VueUserManager.this.setCurrentUser(vueUser1);
            VueUserManager.this.setCurrentUser(vueUser1);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser1);
            callback.onUserUpdated(vueUser1);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          }
        }
      }
    };
		final Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};

    Response.Listener getListener = new Response.Listener<String>() {
      @Override
      public void onResponse(String jsonArray) {
        if (null != jsonArray) {
          Log.e("Profiling", "Create User: Profiling : onResponse()"
              + jsonArray);
          VueUser vueUser2 = new Parser().parseUserData(jsonArray);
          if (vueUser2 != null) {
            if (VueApplication.getInstance().getmUserInitials() == null) {
              VueApplication.getInstance().setmUserInitials(
                  vueUser2.getFirstName());
            }
            VueApplication.getInstance().setmUserId(vueUser2.getId());
            VueUserManager.this.setCurrentUser(vueUser2);
            VueUserManager.this.setCurrentUser(vueUser2);
            Log.i("imageurl", "imageurl is ok got user id: " + vueUser2);
            callback.onUserUpdated(vueUser2);
            showNotificationForSwitchingUser(String.valueOf(vueUser.getId()));
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.BOOKMARKER_AISLES_URI, null, null);
            VueApplication.getInstance().getContentResolver()
                .delete(VueConstants.RATED_IMAGES_URI, null, null);
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getBookmarkAisleByUser();
            VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance()).getNetworkHandler()
                .getRatedImageList();
          } else {
            try {
              Log.e("VueUserDebug", "vueuser: method called ");
              ObjectMapper mapper = new ObjectMapper();
              String userAsString = mapper.writeValueAsString(vueUser);
              Log.e("VueUserDebug", "vueuser: request " + userAsString);
              UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                  userAsString, UrlConstants.UPDATE_USER_RESTURL, listener,
                  errorListener);
              VueApplication.getInstance().getRequestQueue().add(request);
            } catch (Exception e) {

            }
          }
        } else {
          try {
            Log.e("VueUserDebug", "vueuser: method called ");
            ObjectMapper mapper = new ObjectMapper();
            String userAsString = mapper.writeValueAsString(vueUser);
            Log.e("VueUserDebug", "vueuser: request " + userAsString);
            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                userAsString, UrlConstants.UPDATE_USER_RESTURL, listener,
                errorListener);
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {

          }
        }
      }
    };
		Response.ErrorListener getErrorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		UserGetRequest userGetRequest = new UserGetRequest(
				UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
						+ vueUser.getGooglePlusId(), getListener,
				getErrorListener);
		VueApplication.getInstance().getRequestQueue().add(userGetRequest);
	}

	public void updateInstagramIdentifiedUser(final VueUser vueUser,
			final UserUpdateCallback callback) {
		// TODO Need to add instagram id in the backend.
		/*
		 * Response.Listener listener = new Response.Listener<String>() {
		 * 
		 * @Override public void onResponse(String jsonArray) { if (null !=
		 * jsonArray) { Log.e("Profiling",
		 * "Create User: Profiling : onResponse()" + jsonArray); VueUser vueUser
		 * = new Parser().parseUserData(jsonArray); if
		 * (VueApplication.getInstance().getmUserInitials() == null) {
		 * VueApplication.getInstance().setmUserInitials(
		 * vueUser.getFirstName()); }
		 * VueUserManager.this.setCurrentUser(vueUser); Log.i("imageurl",
		 * "imageurl is ok got user id: " + vueUser);
		 * callback.onUserUpdated(vueUser); } } }; Response.ErrorListener
		 * errorListener = new Response.ErrorListener() {
		 * 
		 * @Override public void onErrorResponse(VolleyError error) { if (null
		 * != error.networkResponse && null != error.networkResponse.data) {
		 * String errorData = error.networkResponse.data.toString();
		 * Log.e("VueUserDebug", "error date = " + errorData); } } }; try {
		 * Log.e("VueUserDebug", "vueuser: method called "); ObjectMapper mapper
		 * = new ObjectMapper(); String userAsString =
		 * mapper.writeValueAsString(vueUser); Log.e("VueUserDebug",
		 * "vueuser: request " + userAsString); UserCreateRequest request = new
		 * UserCreateRequest(userAsString, UrlConstants.UPDATE_USER_RESTURL,
		 * listener, errorListener);
		 * VueApplication.getInstance().getRequestQueue().add(request); } catch
		 * (Exception e) {
		 * 
		 * }
		 */
	}

	private class UserCreateOrUpdateRequest extends Request<String> {
		// ... other methods go here
		private Response.Listener<String> mListener;
		private Response.ErrorListener mErrorListener;
		private String muserAsString;
		private StringEntity mEntity;

		public UserCreateOrUpdateRequest(String userAsString, String url,
				Response.Listener<String> listener,
				Response.ErrorListener errorListener) {
			super(Method.PUT, url, errorListener);
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

	private class UserGetRequest extends Request<String> {
		// ... other methods go here
		private Response.Listener<String> mListener;
		private Response.ErrorListener mErrorListener;

		public UserGetRequest(String url, Response.Listener<String> listener,
				Response.ErrorListener errorListener) {
			super(Method.GET, url, errorListener);
			mListener = listener;
			mErrorListener = errorListener;

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fetchUserDataFromLocalId(String id,
			final UserUpdateCallback callback) {
		String requestUrl = UrlConstants.GET_USER_RESTURL + id;
		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String jsonString) {
				if (null != jsonString) {
					Log.e("Vue App", "jsonString = " + jsonString);
					VueUser user = new Parser().parseUserData(jsonString);
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

	private VueUser parseGraphUserIntoVueUser(GraphUser graphUser,
			VueUser storedVueUser) {
		if (null == graphUser) {
			throw new NullPointerException(
					"GraphUser was set to null in parseGraphUserIntoVueUser");
		}
		VueUser vueUser = null;
		try {
			String firstName = graphUser.getFirstName();
			String lastName = graphUser.getLastName();
			JSONObject innerObject = graphUser.getInnerJSONObject();
			String email;
			try {
				email = innerObject
						.getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY);
			} catch (Exception e) {
				email = graphUser.getUsername();
				e.printStackTrace();
			}
			String facebookId = null;
			try {
				if (email != null) {
					facebookId = email;
					facebookId = facebookId.replace(".", "");
				}
			} catch (Exception e) {
				facebookId = email;
			}
			vueUser = new VueUser(null, email, firstName, lastName, null,
					Utils.getDeviceId(), facebookId,
					VueUser.DEFAULT_GOOGLEPLUS_ID);
			if (storedVueUser != null) {
				vueUser.setDeviceId(storedVueUser.getDeviceId());
				vueUser.setGooglePlusId(storedVueUser.getGooglePlusId());
				vueUser.setId(storedVueUser.getId());
				vueUser.setJoinTime(storedVueUser.getJoinTime());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return vueUser;
	}

  private void showNotificationForSwitchingUser(final String userId) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          final ArrayList<AisleWindowContent> aislesList = new NetworkHandler(
              VueApplication.getInstance()).getAislesByUser(userId);
          try {
            VueLandingPageActivity.landingPageActivity
                .runOnUiThread(new Runnable() {

                  @SuppressWarnings("deprecation")
                  @Override
                  public void run() {
                    if (aislesList != null && aislesList.size() > 0) {
                      NotificationManager notificationManager = (NotificationManager) VueLandingPageActivity.landingPageActivity
                          .getSystemService(Context.NOTIFICATION_SERVICE);
                      Notification mNotification = new Notification(
                          R.drawable.vue_notification_icon,
                          "You are Switched the account.", System
                              .currentTimeMillis());
                      Intent MyIntent = new Intent(Intent.ACTION_VIEW);
                      PendingIntent StartIntent = PendingIntent.getActivity(
                          VueApplication.getInstance().getApplicationContext(),
                          0, MyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                      mNotification.flags = mNotification.flags
                          | Notification.FLAG_ONGOING_EVENT
                          | Notification.FLAG_AUTO_CANCEL;
                      mNotification.setLatestEventInfo(VueApplication
                          .getInstance().getApplicationContext(),
                          "User Account is Switched.",
                          "You are Switched the account.", StartIntent);
                      notificationManager.notify(
                          VueConstants.CHANGE_USER_NOTIFICATION_ID,
                          mNotification);
                    }
                  }
                });
          } catch (Exception e) {
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
