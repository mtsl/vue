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
    
    private final static List<String> READ_PERMISSIONS = Arrays.asList(
            "manage_pages", "email", "user_birthday");
    
    public static void createUserAlbum() {
        Session session = Session.getActiveSession();
        boolean fbloggedin = (session != null && session.isOpened());
        if (!fbloggedin) {
            com.facebook.widget.LoginButton loginBt = new LoginButton(
                    VueApplication.getInstance());
            loginBt.setReadPermissions(READ_PERMISSIONS);
            loginBt.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
                public void onUserInfoFetched(GraphUser user) {
                    Session session = Session.getActiveSession();
                    boolean fbloggedin = (session != null && session.isOpened());
                    if (fbloggedin) {
                        Log.e("CreateAlbum",
                                "ALBUM create: Second fbloggedin: YES "
                                        + fbloggedin);
                        createAlbumRequest(session);
                    } else {
                        Log.e("CreateAlbum",
                                "ALBUM create: Second fbloggedin: NO "
                                        + fbloggedin);
                    }
                    
                }
            });
            loginBt.performClick();
        }
    }
    
    public void createNewAlbumRequest(Activity activity, String albumName) {
        Session session = Session.getActiveSession();
        if (session == null || !session.isOpened()) {
            return;
        }
        Bundle params = new Bundle();
        params.putString("name", albumName);
        Callback callback = new Callback() {
            @Override
            public void onCompleted(Response response) {
            }
        };
        Request request = new Request(session, "me/albums", params,
                HttpMethod.POST, callback);
        request.executeAsync();
    }
    
    private boolean isSubsetOf(Collection<String> subset,
            Collection<String> superset) {
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
                String baseUrl = "https://graph.facebook.com/me/albums?";
                String method = "method=POST&";
                String formate = "format=json&suppress_http_code=1&";
                String accessToken = "access_token=" + session.getAccessToken();
                String newAlbumUrl = baseUrl + method + "name=VeryVueTest&"
                        + accessToken;
                
                URL url = null;
                try {
                    url = new URL(baseUrl);
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(baseUrl);
                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("method", "POST"));
                    urlParameters.add(new BasicNameValuePair("format", "json"));
                    urlParameters.add(new BasicNameValuePair(
                            "suppress_http_code", "1"));
                    urlParameters.add(new BasicNameValuePair("name",
                            "VeryVueTest"));
                    urlParameters.add(new BasicNameValuePair("access_token",
                            session.getAccessToken()));
                    post.setEntity(new UrlEncodedFormEntity(urlParameters));
                    HttpResponse response = client.execute(post);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity()
                                    .getContent()));
                    StringBuffer result = new StringBuffer();
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }
}
