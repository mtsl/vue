package com.facebook;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.facebook.Request.GraphUserCallback;
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
  
  public static void createAlbumRequest(Session session) {
    
    Request.executeMeRequestAsync(session, new GraphUserCallback() {
      
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
    });
  
  }
}
