package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.BookmarkPutRequest;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkStateChangeReciver extends BroadcastReceiver {

  AisleBookmark createdAisleBookmark = null;
  AisleBookmark aisleBookmark = null;
  boolean isDirty = true;
  
  @Override
  public void onReceive(Context context, Intent inetent) {
    Log.e("NetworkStateChangeReciver", "SURU NEWWORK STATE CHANGED : ");
    if (VueConnectivityManager.isNetworkConnected(context)) {
      Log.e("NetworkStateChangeReciver",
          "SURU NEWWORK STATE CHANGED : network CONNECTED");
      //TODO: if Database is dirty use shared preference here.
      if (true) {
        VueUser storedVueUser = null;
        try {
          storedVueUser = Utils.readUserObjectFromFile(context,
              VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
          e.printStackTrace();
        }
        final ArrayList<AisleWindowContent> aisles = DataBaseManager.getInstance(
            context).getDirtyAisles("1");
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
                Log.e("Search Resopnse", "SURU bookmark success Resopnse : ");
                if (jsonArray != null) {
                  try {
                    createdAisleBookmark = (new ObjectMapper()).readValue(
                        jsonArray, AisleBookmark.class);
                    AisleManager.getAisleManager().updateBookmartToDb(aisles, aisleBookmark, false);
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
                Log.e("Search Resopnse", "SURU bookmark Error Resopnse : "
                    + error.getMessage());
              }
            };
            BookmarkPutRequest request = new BookmarkPutRequest(
                bookmarkAisleAsString, listener, errorListener,
                UrlConstants.CREATE_BOOKMARK_RESTURL + "/"
                    + storedVueUser.getVueId());
            VueApplication.getInstance().getRequestQueue().add(request);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

}
