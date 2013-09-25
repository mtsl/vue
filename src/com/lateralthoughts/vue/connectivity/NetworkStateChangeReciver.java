package com.lateralthoughts.vue.connectivity;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NetworkStateChangeReciver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent inetent) {
    Log.e("NetworkStateChangeReciver", "SURU NEWWORK STATE CHANGED : ");
    if (VueConnectivityManager.isNetworkConnected(context)) {
      Log.e("NetworkStateChangeReciver",
          "SURU NEWWORK STATE CHANGED : network CONNECTED");
      // TODO: need to sync the data which has not synced at the time of user
      // input in to the app because of not network connection.
      if (true) {
        VueUser storedVueUser = null;
        try {
          storedVueUser = Utils.readUserObjectFromFile(context,
              VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch(Exception e) {
          e.printStackTrace();
        }
        ArrayList<AisleWindowContent> aisles = DataBaseManager.getInstance(
            context).getDirtyAisles("1");
        for(AisleWindowContent content : aisles) {
          JsonArrayRequest vueRequest = new JsonArrayRequest(UrlConstants.CREATE_BOOKMARK_RESTURL
              + storedVueUser.getVueId(), new Response.Listener<JSONArray>() {

          @Override
          public void onResponse(JSONArray response) {
              if (null != response) {
                AisleBookmark createdAisleBookmark = null;
                ObjectMapper mapper = new ObjectMapper();
                try {
                  createdAisleBookmark = (new ObjectMapper()).readValue(
                      response.toString(), AisleBookmark.class);
                } catch (Exception e) {
                  e.printStackTrace();
                }
               /* Bundle responseBundle = new Bundle();
                  responseBundle.putString("Search result",
                          response.toString());
                  responseBundle.putBoolean("loadMore", false);
                  mTrendingAislesParser.send(1, responseBundle);*/
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
      }
    }
  }

}
