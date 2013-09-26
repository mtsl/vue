package com.lateralthoughts.vue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;

public class AisleManager {

  private ObjectMapper mObjectMapper;

  public interface AisleUpdateCallback {
    public void onAisleUpdated(String id);
  }

  public interface ImageAddedCallback {
    public void onImageAdded(AisleImageDetails imageDetails);
  }

  // private static String VUE_API_BASE_URI =
  // "http://2-java.vueapi-canary-development1.appspot.com/";

  // private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
  // private static String CREATE_AISLE_ENDPOINT = "api/aislecreate";
  private String CREATE_IMAGE_ENDPOINT = "imagecreate";
  private static AisleManager sAisleManager = null;

  private VueUser mCurrentUser;

  private AisleManager() {
    mObjectMapper = new ObjectMapper();
  }

  public static AisleManager getAisleManager() {
    if (null == sAisleManager) sAisleManager = new AisleManager();
    return sAisleManager;
  }

  // create an unidentified VueUser object. This is an asynchronous API and
  // needs to make a round trip
  // network call.
  // Usually this call cannot be invoked when mCurrentUser is set to a valid
  // value. This is because we can only
  // have only current user at a time. When this call returns the
  // UserUpdateCallback's onUserUpdated API will
  // be invoked and the VueUser object is created and set at that point.
  public void createEmptyAisle(final Aisle aisle,
      final AisleUpdateCallback callback) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          final String response = testCreateAisle(aisle);
          VueLandingPageActivity.landingPageActivity
              .runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  // ////////////////////////////////////////////////

                  if (null != response) {

                    Log.i("myailsedebug", "myailsedebug: recieved response:  "
                        + response);
                    try {
                      // JSONObject userInfo = new JSONObject(jsonArray);

                      AisleWindowContent aileItem = new Parser()
                          .getAisleCotent(response);
                      VueTrendingAislesDataModel.getInstance(
                          VueApplication.getInstance()).addItemToListAt(
                          aileItem.getAisleContext().mAisleId, aileItem, 0);
                      VueTrendingAislesDataModel.getInstance(
                          VueApplication.getInstance()).dataObserver();
                      ArrayList<AisleWindowContent> list = new ArrayList<AisleWindowContent>();
                      list.add(aileItem);
                      DataBaseManager.getInstance(VueApplication.getInstance())
                          .addTrentingAislesFromServerToDB(
                              VueApplication.getInstance(), list);
                      // JSONObject user = userInfo.getJSONObject("user");
                      // TODO: GET THE AISLE OBJECT FROM THE PARSER CLASE SEND
                      // THE AISLE AND AISLE ID BACK.
                      callback.onAisleUpdated(aileItem.getAisleContext().mAisleId);
                      FlurryAgent.logEvent("Create_Aisle_Success");
                      // VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler().requestAislesByUser();
                    } catch (Exception ex) {
                      Log.e("Profiling",
                          "Profiling : onResponse() **************** error");
                      ex.printStackTrace();
                    }
                  } else {
                    Toast.makeText(VueApplication.getInstance(),
                        "New Aisle Creation in server is failed.",
                        Toast.LENGTH_LONG).show();

                  }



                  // ///////////////////////////////////////////////////////////

                }
              });
        } catch (Exception e) {
          Log.i("myailsedebug",
              "myailsedebug: recieved response****: got excetption  ");
          e.printStackTrace();
        }

      }
    }).start();


    /*
     * if (null == aisle) throw new RuntimeException(
     * "Can't create Aisle without a non null aisle object"); String
     * aisleAsString = null; try { aisleAsString =
     * mObjectMapper.writeValueAsString(aisle); } catch (JsonProcessingException
     * ex2) {
     * 
     * } Response.Listener listener = new Response.Listener<String>() {
     * 
     * @Override public void onResponse(String jsonArray) { if (null !=
     * jsonArray) {
     * 
     * Log.i("myailsedebug", "myailsedebug: recieved response:  " + jsonArray);
     * try { // JSONObject userInfo = new JSONObject(jsonArray);
     * 
     * AisleWindowContent aileItem = new Parser() .getAisleCotent(jsonArray);
     * VueTrendingAislesDataModel.getInstance(
     * VueApplication.getInstance()).addItemToListAt(
     * aileItem.getAisleContext().mAisleId, aileItem, 0);
     * VueTrendingAislesDataModel.getInstance(
     * VueApplication.getInstance()).dataObserver();
     * ArrayList<AisleWindowContent> list = new ArrayList<AisleWindowContent>();
     * list.add(aileItem);
     * DataBaseManager.getInstance(VueApplication.getInstance
     * ()).addTrentingAislesFromServerToDB(VueApplication.getInstance(),list);
     * // JSONObject user = userInfo.getJSONObject("user"); // TODO: GET THE
     * AISLE OBJECT FROM THE PARSER CLASE SEND // THE AISLE AND AISLE ID BACK.
     * callback.onAisleUpdated(aileItem.getAisleContext().mAisleId);
     * FlurryAgent.logEvent("Create_Aisle_Success"); //
     * VueTrendingAislesDataModel
     * .getInstance(VueApplication.getInstance()).getNetworkHandler
     * ().requestAislesByUser(); } catch (Exception ex) { Log.e("Profiling",
     * "Profiling : onResponse() **************** error"); ex.printStackTrace();
     * } } else { Toast.makeText(VueApplication.getInstance(),
     * "New Aisle Creation in server is failed.", Toast.LENGTH_LONG).show();
     * 
     * } } }; Response.ErrorListener errorListener = new
     * Response.ErrorListener() {
     * 
     * @Override public void onErrorResponse(VolleyError error) {
     * Log.i("imageurl", "imageurl  aisle creation error response ");
     * Toast.makeText(VueApplication.getInstance(),
     * "New Aisle Creation in server is failed.", Toast.LENGTH_LONG).show(); if
     * (null != error.networkResponse && null != error.networkResponse.data) {
     * String errorData = error.networkResponse.data.toString();
     * 
     * } } }; Log.i("imageurl",
     * "imageurl  aisle creation request aisleAsString: " + aisleAsString); //
     * String requestUrl = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT; String
     * requestUrl = UrlConstants.CREATE_AISLE_RESTURL;
     * 
     * Log.i("imageurl", "imageurl  aisle creation request url: " + requestUrl);
     * 
     * AislePutRequest request = new AislePutRequest(aisleAsString, listener,
     * errorListener, requestUrl);
     * VueApplication.getInstance().getRequestQueue().add(request);
     */}

  private AisleContext parseAisleContent(JSONObject user) {
    AisleContext aisle = null;

    return aisle;

  }

  // issues a request to add an image to the aisle.
  public void addImageToAisle(final boolean fromDetailsScreenFlag,
      VueImage image, final ImageAddedCallback callback) {
    Log.i("addimagefuncitonality", "addimagefuncitonality entered in method");
    if (null == image) {
      throw new RuntimeException(
          "Can't create Aisle without a non null aisle object");
    }
    String imageAsString = null;
    try {
      imageAsString = mObjectMapper.writeValueAsString(image);
    } catch (JsonProcessingException ex2) {

    }
    Response.Listener listener = new Response.Listener<String>() {

      @Override
      public void onResponse(String jsonArray) {

        if (null != jsonArray) {

          if (!fromDetailsScreenFlag) {
            Log.i("addimagefuncitonality",
                "addimagefuncitonality jsonArray response: " + jsonArray);
            try {
              AisleImageDetails aisleImageDetails = new Parser()
                  .parseAisleImageData(new JSONObject(jsonArray));
              if (aisleImageDetails != null) {
                AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).getAisleAt(
                        aisleImageDetails.mOwnerAisleId);
                aisleWindowContent.prepareCustomUrl(aisleImageDetails);
                Log.i("Ailse Manager", "customimageurl add image to aisle: "
                    + aisleImageDetails.mCustomImageUrl);
                aisleWindowContent.getImageList().add(aisleImageDetails);
                VueTrendingAislesDataModel.getInstance(
                    VueApplication.getInstance()).dataObserver();
                String s[] = {aisleImageDetails.mOwnerAisleId};
                ArrayList<AisleWindowContent> list = DataBaseManager
                    .getInstance(VueApplication.getInstance()).getAislesFromDB(
                        s);
                if (list != null) {
                  list.get(0).getImageList().add(aisleImageDetails);
                  DataBaseManager.getInstance(VueApplication.getInstance())
                      .addTrentingAislesFromServerToDB(
                          VueApplication.getInstance(), list);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
          // callback.onImageAdded(new
          // Parser().getImageDetails(jsonArray));
        }
      }
    };
    Response.ErrorListener errorListener = new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {

        if (null != error.networkResponse && null != error.networkResponse.data) {
          String errorData = error.networkResponse.data.toString();
          Log.i("addimagefuncitonality",
              "addimagefuncitonality jsonArray response ERROR: ");
        }
      }
    };
    Log.i("addimagefuncitonality",
        "addimagefuncitonality entered in method requst String: "
            + imageAsString);
    AislePutRequest request = new AislePutRequest(imageAsString, listener,
        errorListener, UrlConstants.CREATE_IMAGE_RESTURL);
    VueApplication.getInstance().getRequestQueue().add(request);
  }

  private class AislePutRequest extends Request<String> {
    // ... other methods go here
    private Map<String, String> mParams;
    Response.Listener<String> mListener;
    private String mAisleAsString;
    private StringEntity mEntity;

    public AislePutRequest(String aisleAsString,
        Response.Listener<String> listener,
        Response.ErrorListener errorListener, String url) {
      super(Method.PUT, url, errorListener);
      mListener = listener;
      mAisleAsString = aisleAsString;
      try {
        mEntity = new StringEntity(mAisleAsString);
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
    }
  }

  public String testCreateAisle(Aisle aisle) throws Exception {
    Aisle createdAisle = null;
    ObjectMapper mapper = new ObjectMapper();
    String responseMessage = null;
    URL url = new URL(UrlConstants.CREATE_AISLE_RESTURL);
    HttpPut httpPut = new HttpPut(url.toString());
    StringEntity entity = new StringEntity(mapper.writeValueAsString(aisle));
    System.out.println("Aisle create request: "
        + mapper.writeValueAsString(aisle));
    entity.setContentType("application/json;charset=UTF-8");
    entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
        "application/json;charset=UTF-8"));
    httpPut.setEntity(entity);

    DefaultHttpClient httpClient = new DefaultHttpClient();
    HttpResponse response = httpClient.execute(httpPut);
    if (response.getEntity() != null
        && response.getStatusLine().getStatusCode() == 200) {
      responseMessage = EntityUtils.toString(response.getEntity());
      System.out.println("AISLE CREATED Response: " + responseMessage);
      Log.i("myailsedebug", "myailsedebug: recieved response*******:  "
          + responseMessage);
    } else {
      Log.i("myailsedebug",
          "myailsedebug: recieved response******* response code :  "
              + response.getStatusLine().getStatusCode());
    }
    return responseMessage;

  }
  
  public void aisleBookmarkUpdate(AisleBookmark aisleBookmark, String userId) throws ClientProtocolException, IOException {
    URL url = new URL(UrlConstants.CREATE_BOOKMARK_RESTURL + 
        "/" +  userId);
    
    ObjectMapper mapper = 
        new ObjectMapper();
    HttpPut httpPut = new HttpPut(url.toString());
    StringEntity entity = new StringEntity(mapper.writeValueAsString(aisleBookmark));
    System.out.println("AisleBookmark create request: "+mapper.writeValueAsString(aisleBookmark));
    entity.setContentType("application/json;charset=UTF-8");
    entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
    httpPut.setEntity(entity);

    DefaultHttpClient httpClient = new DefaultHttpClient();
    HttpResponse response = httpClient.execute(httpPut);                     
    if(response.getEntity()!=null && 
            response.getStatusLine().getStatusCode() == 200) {
        String responseMessage = EntityUtils.toString(response.getEntity());
        System.out.println("Response: "+responseMessage);
        if (responseMessage.length() > 0)
        {
          Log.e("Bookmark", "aisle bookmarked responce: " + responseMessage);
          AisleBookmark createdAisleBookmark = (new ObjectMapper()).readValue(responseMessage, AisleBookmark.class);
        }
    }
  }
}
