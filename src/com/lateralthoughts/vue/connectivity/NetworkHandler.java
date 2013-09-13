package com.lateralthoughts.vue.connectivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.lateralthoughts.vue.VueApplication;

public class NetworkHandler {

  private static final String SEARCH_REQUEST_URL = "http://1-java.vueapi-canary-devel-search.appspot.com/api/getaisleswithmatchingkeyword/";
  
  public void requestMoreAisle() {

  }

  public void reqestByCategory(String catName) {

  }

  public void requestTrending() {

  }

  public void requestCreateAisle() {

  }

  public static void requestSearch(final String searchString, final ResultReceiver receiver) {

    ///testSearchResopnce(searchString);
    JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL + searchString, new Response.Listener<JSONArray>() {

      @Override
      public void onResponse(JSONArray response) {
        if (null != response) {
          Bundle responseBundle = new Bundle();
          responseBundle.putString("Search result", response.toString());
          responseBundle.putBoolean("loadMore", false);
          receiver.send(1, responseBundle);
        }
        Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
      }}, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e("Search Resopnse", "SURU Search Error Resopnse : " + error.getMessage());
        }});
    
        VueApplication.getInstance().getRequestQueue().add(vueRequest);

  }

  public void requestUserAisles() {

  }
  
  private static void testSearchResopnce(final String searchString) {
    Thread t = new Thread(new Runnable() {
      
      @Override
      public void run() {
        HttpClient httpClient = new DefaultHttpClient();  
        String url = SEARCH_REQUEST_URL + searchString;
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                entity.writeTo(out);
                out.close();
                String responseStr = out.toString();
                Log.e("VueNetworkError", "Suru Search URL : " + responseStr);
                // do something with response 
            } else {
                // handle bad response
            }
        } catch (ClientProtocolException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        } 
      }
    });
    t.start();
  }
}







/*

Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
  @Override
  public void onResponse(JSONArray jsonArray) {
    if (null != jsonArray) {
      Bundle responseBundle = new Bundle();
      responseBundle.putString("Search result", jsonArray.toString());
      responseBundle.putBoolean("loadMore", false);
      receiver.send(1, responseBundle);
    }
  }
};
Response.ErrorListener errorListener = new Response.ErrorListener() {
  @Override
  public void onErrorResponse(VolleyError error) {
    Bundle responseBundle = new Bundle();
    responseBundle.putString("result", "error");
    // receiver.send(1,responseBundle);
    Log.e("VueNetworkError", "Suru error check");
    Log.e("VueNetworkError",
        "Vue encountered network operations error. Error = "
            + error.networkResponse);
  }
};


JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
    + searchString, listener, errorListener) {
  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    HashMap<String, String> headersMap = new HashMap<String, String>();
    headersMap.put("Accept-Encoding", "gzip");
    headersMap.put("Content-Type", "application/json");
    return headersMap;
  }
};
Log.e("VueNetworkError", "Suru Search URL : " + SEARCH_REQUEST_URL + searchString);
*/