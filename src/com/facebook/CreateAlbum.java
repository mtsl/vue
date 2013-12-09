package com.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Request.Callback;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.lateralthoughts.vue.VueApplication;

public class CreateAlbum {

  private static String url = "https://graph.facebook.com/";
  private String userId = "USER_ID/";
  private String albumName = "albums { name :'my USER album' }";
  private final static List<String> READ_PERMISSIONS = Arrays.asList("manage_pages", "email",
      "user_birthday");
  
  public static void createUserAlbum() {
    Session session = Session.getActiveSession();
    boolean fbloggedin = (session != null && session.isOpened());
    if(!fbloggedin) {
      Log.e("CreateAlbum", "ALBUM create: First fbloggedin: " + fbloggedin);
      com.facebook.widget.LoginButton loginBt = new LoginButton(VueApplication.getInstance());
      loginBt.setReadPermissions(READ_PERMISSIONS);
      loginBt
              .setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
                  public void onUserInfoFetched(GraphUser user) {
                    Session session = Session.getActiveSession();
                    boolean fbloggedin = (session != null && session.isOpened());
                    if(fbloggedin) {
                      Log.e("CreateAlbum", "ALBUM create: Second fbloggedin: YES " + fbloggedin);
                      createAlbumRequest(session);  
                    } else {
                      Log.e("CreateAlbum", "ALBUM create: Second fbloggedin: NO " + fbloggedin);
                    }
                    
                  }
              });
      loginBt.performClick();
    }
    Log.e("CreateAlbum", "ALBUM create: fbloggedin: " + fbloggedin);
   
  }
  
 // private static final List<String> PERMISSIONS = Arrays.asList("publish_stream", "manage_pages");
 // private static final int REAUTH_ACTIVITY_CODE = 100;
  
  public void createNewAlbumRequest(Activity activity, String albumName) {
    Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 1");
    Session session = Session.getActiveSession();
    if (session == null || !session.isOpened()) {
      Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 2 Session Null");
      return;
    }
    
    //List<String> permissions = session.getPermissions();
    Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 3");
      Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 4");
      Bundle params = new Bundle();
      params.putString("name", albumName);
      Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 5");
      Callback callback = new Callback() {
        
        @Override
        public void onCompleted(Response response) {
          Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 9 " + response.toString());
        }
      };
      Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 6");
      Request request = new Request(session, "me/albums",
          params, HttpMethod.POST,
          callback);
      Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 7");
     request.executeAsync();
     Log.e("CreateAlbum", "SURU CREATE ALBUM RESPONSE: 8");
    //}
  }

  private  boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
    for (String string : subset) {
       if (!superset.contains(string)) {
           return false;
       }
    }
    return true;
  }


  private static final String USER_AGENT = "Mozilla/5.0";
  public static void createAlbumRequest(final Session session) {
    new Thread(new Runnable() {
      
      @Override
      public void run() {
        Log.e("CreateAlbum", "ALBUM create: SURU 1: ");
        String baseUrl = "https://graph.facebook.com/me/albums?";
         String method = "method=POST&";
         String formate = "format=json&suppress_http_code=1&";
         String accessToken = "access_token=" + session.getAccessToken();
         String newAlbumUrl = baseUrl + method + "name=VeryVueTest&" + accessToken;

        URL url = null;
        try {
          Log.e("CreateAlbum", "ALBUM create: SURU 2: ");
          url = new URL(baseUrl);
          Log.e("CreateAlbum", "ALBUM create: SURU 6 URL: " + newAlbumUrl);
          HttpClient client = new DefaultHttpClient();
          HttpPost post = new HttpPost(baseUrl);
          List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
          urlParameters.add(new BasicNameValuePair("method", "POST"));
          urlParameters.add(new BasicNameValuePair("format", "json"));
          urlParameters.add(new BasicNameValuePair("suppress_http_code", "1"));
          urlParameters.add(new BasicNameValuePair("name", "VeryVueTest"));
          urlParameters.add(new BasicNameValuePair("access_token", session.getAccessToken()));
          post.setEntity(new UrlEncodedFormEntity(urlParameters));
          Log.e("CreateAlbum", "ALBUM create: SURU 3: ");
          HttpResponse response = client.execute(post);
          Log.e("CreateAlbum", "ALBUM create: SURU 4: ");
          BufferedReader rd = new BufferedReader(
              new InputStreamReader(response.getEntity().getContent()));
          Log.e("CreateAlbum", "ALBUM create: SURU 5: ");
          StringBuffer result = new StringBuffer();
          String line = "";
          while ((line = rd.readLine()) != null) {
           result.append(line);
          }
          Log.e("CreateAlbum", "ALBUM create: SURU 6 RESPONCE: " + result.toString());
          // System.out.println(result.toString());
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    }).start();
    
    
/*    Request.executeMeRequestAsync(session, new GraphUserCallback() {
      
      @Override
      public void onCompleted(final GraphUser user, Response response) {
        new Thread(new Runnable() {
          
          @Override
          public void run() {
            String userId = user.getId();
            String albumName = "albums { name : 'SuruVueTestAlbum'}";
            String albumUrl = url + userId + "/albums";
            URL url;
            try {
              url = new URL(albumUrl);
              HttpPut httpPut = new HttpPut(url.toString());
              DefaultHttpClient httpClient = new DefaultHttpClient();
              StringEntity entity = new StringEntity(
                  "{ name :'Surendra_Test_Album' }");
              System.out.println("ALBUM create request:");
              entity.setContentType("application/json;charset=UTF-8");
              entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                  "application/json;charset=UTF-8"));
              httpPut.setEntity(entity);
              HttpResponse response1 = httpClient.execute(httpPut);
              if (response1.getEntity() != null
                  && response1.getStatusLine().getStatusCode() == 200) {
                String responseMessage = EntityUtils.toString(response1
                    .getEntity());
                System.out.println("ALBUM create RESPONSE: " + responseMessage);
              } else {
                System.out.println("ALBUM create ERROR RESPONSE: "
                    + response1.getStatusLine().getStatusCode());
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }).start();
      }
    });*/
  
  }
}
