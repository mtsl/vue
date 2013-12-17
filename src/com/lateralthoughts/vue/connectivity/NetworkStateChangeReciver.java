package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.BookmarkPutRequest;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class NetworkStateChangeReciver extends BroadcastReceiver {

  private SharedPreferences mSharedPreferencesObj;
  AisleBookmark createdAisleBookmark = null;
  AisleBookmark aisleBookmark = null;
  boolean isDirty = true;

  @Override
  public void onReceive(Context context, Intent inetent) {
    if (VueConnectivityManager.isNetworkConnected(context)) {
      // TODO: if Database is dirty use shared preference here.
      mSharedPreferencesObj = context.getSharedPreferences(
          VueConstants.SHAREDPREFERENCE_NAME, 0);
      if (mSharedPreferencesObj.getBoolean(VueConstants.IS_AISLE_DIRTY, false)) {
        VueUser storedVueUser = null;
        try {
          storedVueUser = Utils.readUserObjectFromFile(context,
              VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
          e.printStackTrace();
        }
        final ArrayList<AisleWindowContent> aisles = DataBaseManager
            .getInstance(context).getDirtyAisles("1");
        for (AisleWindowContent content : aisles) {
          aisleBookmark = new AisleBookmark(null, true, Long.parseLong(content
              .getAisleId()));
          try {
            storedVueUser = Utils.readUserObjectFromFile(
                VueApplication.getInstance(),
                VueConstants.VUE_APP_USEROBJECT__FILENAME);

            ObjectMapper mapper = new ObjectMapper();
            String bookmarkAisleAsString = mapper
                .writeValueAsString(aisleBookmark);
            Response.Listener listener = new Response.Listener<String>() {

              @Override
              public void onResponse(String jsonArray) {
                if (jsonArray != null) {
                  try {
                    createdAisleBookmark = (new ObjectMapper()).readValue(
                        jsonArray, AisleBookmark.class);
                    AisleManager.getAisleManager().updateBookmartToDb(aisles,
                        aisleBookmark, false);
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              }
            };

            Response.ErrorListener errorListener = new ErrorListener() {

              @Override
              public void onErrorResponse(VolleyError error) {
                isDirty = true;
              }
            };
            BookmarkPutRequest request = new BookmarkPutRequest(
                bookmarkAisleAsString, listener, errorListener,
                UrlConstants.CREATE_BOOKMARK_RESTURL + "/"
                    + storedVueUser.getId());
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      if ((mSharedPreferencesObj.getBoolean(VueConstants.IS_IMAGE_DIRTY, false))) {
        ArrayList<ImageRating> imagsRating = DataBaseManager.getInstance(
            context).getDirtyImages("1");
        for (ImageRating imgRating : imagsRating) {
          try {
            AisleManager.getAisleManager().updateRating(imgRating, 0);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      
      if(mSharedPreferencesObj.getBoolean(VueConstants.IS_COMMENT_DIRTY, false)) {
        ArrayList<ImageComment> comments = DataBaseManager.getInstance(
            context).getDirtyComments("1");
        NetworkHandler networkHandler = new NetworkHandler(context);
        for(ImageComment comment : comments) {
          try {
        	  ImageCommentRequest imageRequest = new ImageCommentRequest();
        	  imageRequest.setComment(comment.getComment());
        	  imageRequest.setLastModifiedTimestamp(comment.getLastModifiedTimestamp());
        	  imageRequest.setOwnerImageId(comment.getOwnerImageId());
        	  imageRequest.setOwnerUserId(comment.getOwnerUserId());
            networkHandler.createImageComment(imageRequest);
          } catch (Exception e) { 
            e.printStackTrace();
          }
        }
      }
    }
  }

}
