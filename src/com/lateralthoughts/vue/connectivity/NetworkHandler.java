package com.lateralthoughts.vue.connectivity;

import android.content.Context;
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
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.ui.ViewInfo;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkHandler {
  Context mContext;
  private static final String SEARCH_REQUEST_URL = "http://2-java.vueapi-canary.appspot.com/api/getaisleswithmatchingkeyword/";
  // http://2-java.vueapi-canary-development1.appspot.com/api/
  DataBaseManager mDbManager;
  protected VueContentGateway mVueContentGateway;
  protected TrendingAislesContentParser mTrendingAislesParser;
  private static final int NOTIFICATION_THRESHOLD = 4;
  private static final int TRENDING_AISLES_BATCH_SIZE = 10;
  public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
  private static String MY_AISLES = "aislesget/user/";
  protected int mLimit;
  protected int mOffset;
  ArrayList<AisleWindowContent> aislesList = null;
  public ArrayList<String> bookmarkedAisles = new ArrayList<String>();
  //public ArrayList<AisleWindowContent> bookmarkedAisleContent = new ArrayList<AisleWindowContent>();

  public NetworkHandler(Context context) {
    mContext = context;
    mVueContentGateway = VueContentGateway.getInstance();
    mTrendingAislesParser = new TrendingAislesContentParser(new Handler(),
        VueConstants.AISLE_TRENDING_LIST_DATA);
    mDbManager = DataBaseManager.getInstance(mContext);
    mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
    mOffset = 0;
  }

  // whle user scrolls down get next 10 aisles
  public void requestMoreAisle(boolean loadMore, String screenname) {

    Log.i("offeset and limit", "offeset1: load moredata");
    if (VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
        .isMoreDataAvailable()) {

      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = false;
      
        if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
        mOffset += mLimit; else { mOffset += mLimit; mLimit =
        TRENDING_AISLES_BATCH_SIZE; }
       
      Log.i("offeset and limit", "offeset1: " + mOffset + " and limit: "
          + mLimit);
/*      mOffset = VueTrendingAislesDataModel.getInstance(
          VueApplication.getInstance()).listSize();*/
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
      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
          .clearContent();
      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
          .showProgress();
      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
          .setMoreDataAVailable(true);
      mVueContentGateway.getTrendingAisles(mLimit, mOffset,
          mTrendingAislesParser, loadMore, screenname);

    } else {
    	//Log.i("duplicateImages", "duplicateImages from db1");
      DataBaseManager.getInstance(VueApplication.getInstance()).resetDbParams();
      ArrayList<AisleWindowContent> aisleContentArray = mDbManager
          .getAislesFromDB(null);
      for(int i=0;i<aisleContentArray.size();i++){
    	 // Log.i("duplicateImages", "duplicateImages imageurl******: "+i);
    	  for(int j=0;j< aisleContentArray.get(i).getImageList().size();j++){
    		// Log.i("duplicateImages imageurl", "duplicateImages imageurl: "+ aisleContentArray.get(i).getImageList().get(j).mImageUrl);
    	  }
    	 // Log.i("duplicateImages", "duplicateImages imageurl########: "+i);
      }
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
  public void requestCreateAisle(Aisle aisle, final AisleUpdateCallback callback) {
    AisleManager.getAisleManager().createEmptyAisle(aisle, callback);
  }

  public void requestForAddImage(boolean fromDetailsScreenFlag, VueImage image,
      ImageAddedCallback callback) {
    AisleManager.getAisleManager().addImageToAisle(fromDetailsScreenFlag,
        image, callback);
  }

  // get aisles related to search keyword
  public void requestSearch(final String searchString) {
    JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
        + searchString, new Response.Listener<JSONArray>() {

      @Override
      public void onResponse(JSONArray response) {
        if (null != response) {
          Bundle responseBundle = new Bundle();
          responseBundle.putString("Search result", response.toString());
          responseBundle.putBoolean("loadMore", false);
          mTrendingAislesParser.send(1, responseBundle);
        }
        Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
      }
    }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("Search Resopnse",
            "SURU Search Error Resopnse : " + error.getMessage());
      }
    });
    //RETRY POLICY
    vueRequest.setRetryPolicy(new DefaultRetryPolicy(
    		DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 
            Utils.MAX_RETRIES, 
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
          responseBundle.putString("Search result", response.toString());
          responseBundle.putBoolean("loadMore", false);
          mTrendingAislesParser.send(1, responseBundle);
        }
        Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
      }
    }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("Search Resopnse",
            "SURU Search Error Resopnse : " + error.getMessage());
      }
    });

    VueApplication.getInstance().getRequestQueue().add(vueRequest);

  }

  public void loadInitialData(boolean loadMore, Handler mHandler, String screenName) {
    Log.i("bookmarked aisle", "bookmarked aisle O");
    getBookmarkAisleByUser();
    getRatedImageList();
    
    mOffset = 0;
    if (!VueConnectivityManager.isNetworkConnected(mContext)) {
      Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
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

  public void requestAislesByUser(boolean fromServer, NotifyProgress progress, final String screenName) {
   
    mOffset = 0;
    if (!fromServer) {
      // TODO get data from local db.
      Log.i("myaisledbcheck",
          "myaisledbcheck aisle are my aisles are fetching from db $$$$: ");
      String userId = getUserId();
      if (userId != null) {

        ArrayList<AisleWindowContent> windowList = DataBaseManager.getInstance(
            VueApplication.getInstance()).getAislesByUserId(userId);

        for (int i = 0; i < windowList.size(); i++) {
          VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
              .addItemToList(windowList.get(i).getAisleContext().mAisleId,
                  windowList.get(i));
        }
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
            .dataObserver();
      } else {
        Toast.makeText(VueApplication.getInstance(), "Unable to get user id",
            Toast.LENGTH_SHORT).show();
      }
    } else {
      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
          .setNotificationProgress(progress, fromServer);
      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
          .showProgress();
      Log.i("myaisledbcheck",
          "myaisledbcheck aisle are my aisles are fetching from ser12 $$$$: ");
      // TODO: CHANGE THIS REQUEST TO VOLLEY
      if (VueConnectivityManager.isNetworkConnected(VueApplication
          .getInstance())) {
  /*      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
            .clearAisles();
        AisleWindowContentFactory.getInstance(VueApplication.getInstance())
            .clearObjectsInUse();*/
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = false;
        new Thread(new Runnable() {

          @Override
          public void run() {
            try {
              aislesList = null;
              aislesList = getAislesByUser();
            } catch (Exception e) {
              e.printStackTrace();
            }
            if (VueLandingPageActivity.landingPageActivity != null
             /*   && (VueLandingPageActivity.mVueLandingActionbarScreenName
                    .getText().toString().equals(VueApplication.getInstance()
                    .getString(R.string.sidemenu_sub_option_My_Aisles)))*/) {
              VueLandingPageActivity.landingPageActivity
                  .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      VueTrendingAislesDataModel.getInstance(VueApplication
                          .getInstance()).loadOnRequest = false;
                      Log.i("myailsedebug",
                          "myailsedebug: recieved my runonuithread:  ");
                      if (aislesList != null && aislesList.size() > 0) {
                    	  clearList();
                        Log.i("myailsedebug",
                            "myailsedebug: recieved my runonuithread: if ");
                        for (int i = 0; i < aislesList.size(); i++) {
                          VueTrendingAislesDataModel.getInstance(
                              VueApplication.getInstance()).addItemToList(
                              aislesList.get(i).getAisleContext().mAisleId,
                              aislesList.get(i));
                        }
                        if (aislesList != null) {
                          Log.i(
                              "myaisledbcheck",
                              "myaisledbcheck aisle are fetching from server inserting to db  windowList size is: "
                                  + aislesList.size());
                        } else {
                          Log.i(
                              "myaisledbcheck",
                              "myaisledbcheck aisle are fetching from server inserting to db  aislesList is null: ");
                        }
                        // adding my aisle to db.
                        DataBaseManager.getInstance(
                            VueApplication.getInstance())
                            .addTrentingAislesFromServerToDB(
                                VueApplication.getInstance(), aislesList);


                    
                        // if this is the first set of data
                        // we
                        // are receiving
                        // go
                        // ahead
                        // notify the data set changed
                        VueTrendingAislesDataModel.getInstance(
                            VueApplication.getInstance()).dataObserver();
                        VueLandingPageActivity.changeScreenName(screenName);
                        Log.i("myaisledbcheck",
                            "myaisledbcheck aisle are fetching from server inserting to db success: ");
                      } else {
                        // if this is the first set of data
                        // we
                        // are receiving
                        // go
                        // ahead
                        // notify the data set changed
                    	  StackViews.getInstance().pull();
                        VueTrendingAislesDataModel.getInstance(
                            VueApplication.getInstance()).dataObserver();
                        Toast.makeText(
                            VueLandingPageActivity.landingPageActivity,
                            "There are no Aisles for this User.",
                            Toast.LENGTH_LONG).show();
                      }
                      VueTrendingAislesDataModel.getInstance(
                          VueApplication.getInstance()).dismissProgress();
                    }

                  });
            }
          }
        }).start();
      } else {
        Toast.makeText(
            VueApplication.getInstance(),
            VueApplication.getInstance().getResources()
                .getString(R.string.no_network), Toast.LENGTH_LONG).show();
      }
    }
  }

  public int getmOffset() {
    return mOffset;
  }

  public void setmOffset(int mOffset) {
    this.mOffset = mOffset;
  }

  public ArrayList<AisleWindowContent> getAislesByUser() throws Exception {
    // TODO: change to volley

    String userId = getUserId();
    if(userId == null) {
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
      Log.i("aisleWindowImageUrl", "aisleWindowImageUrl response Message: "
          + responseMessage);
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
            Log.i("bookmarked aisle", "bookmarked aisle ID IS NULL RETURNING");
            return;
          }
          Log.i("bookmarked aisle", "bookmarked aisle 2 User Id; " + userId);
          URL url = new URL(UrlConstants.GET_BOOKMARK_Aisles + "/" + userId
              + "/" + "0");
          HttpGet httpGet = new HttpGet(url.toString());
          DefaultHttpClient httpClient = new DefaultHttpClient();
          HttpResponse response = httpClient.execute(httpGet);
          Log.e("bookmarked aisle", "bookmarked aisle response.getStatusLine().getStatusCode(); " + response.getStatusLine().getStatusCode());
          if (response.getEntity() != null
              && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            Log.i("bookmarked aisle", "bookmarked aisle 3 response: "
                + responseMessage);
            if(responseMessage != null)
            bookmarkedAisles =  new Parser().parseBookmarkedAisles(responseMessage);
            Log.e("bookmarked aisle", "bookmarked aisle bookmarkedAisles size(); " + bookmarkedAisles.size());
          }
        } catch (Exception e) {
          Log.i("bookmarked aisle", "bookmarked aisle 3 error: ");
          e.printStackTrace();
        }

      }
    }).start();

  }
  public void addBookmarked(String aisleId){
    if(aisleId != null)
    bookmarkedAisles.add(aisleId);
  }
  public boolean isAisleBookmarked(String aisleId) {
    Log.i("bookmarked aisle", "bookmarked my bookmarks id enter in method: "+aisleId);
    if(bookmarkedAisles.size() < 1){
      Log.i("bookmarked aisle", "bookmarked my bookmarks size is zero: " );
      return false;
    }
    for(String id: bookmarkedAisles){
      Log.i("bookmarked aisle", "bookmarked my bookmarks id: "+id);
      if(aisleId.equalsIgnoreCase(id)){
        Log.i("bookmarked aisle", "bookmarked my bookmarks id matched: "+id);
      }
    }
     boolean isAisleBookmared = false;
    for(String id: bookmarkedAisles){
      if(aisleId.equalsIgnoreCase(id)){
        isAisleBookmared = true;
        break;
      }
    }
    return isAisleBookmared;
  }
  
  private String getUserId() {
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

  public void getRatedImageList() {
    String userId = getUserId();
    if(userId == null) {
      return;
    }
    JsonArrayRequest vueRequest = new JsonArrayRequest(UrlConstants.GET_RATINGS_RESTURL
        + "/" + userId + "/" + 0L, new Response.Listener<JSONArray>() {

      @Override
      public void onResponse(JSONArray response) {
        if (null != response) {
          ArrayList<ImageRating> retrievedImageRating = null;
          if (response.length() > 0){
                try {
                  retrievedImageRating = (new ObjectMapper()).readValue(
                      response.toString(),
                      new TypeReference<List<ImageRating>>() {});
                  DataBaseManager.getInstance(mContext).insertRatedImages(retrievedImageRating);
                } catch (Exception e) {
                e.printStackTrace();
              }
          }
        }
        Log.e("get reating image Resopnse", "SURU get reating image Resopnse : " + response);
      }
    }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("get reating image",
            "SURU get reating image Error Resopnse : " + error.getMessage());
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
 public void makeOffseZero(){
	 mOffset = 0;
 }
}
