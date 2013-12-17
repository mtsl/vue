package com.instagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.instagram.InstagramDialog.OAuthDialogListener;

public class InstagramApp {
    
    private InstagramSession mSession;
    private InstagramDialog mDialog;
    private OAuthAuthenticationListener mListener;
    private ProgressDialog mProgress;
    private String mAuthUrl;
    private String mAccessToken;
    private String mClientId;
    private String mClientSecret;
    
    private static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;
    
    /**
     * Callback url, as set in 'Manage OAuth Costumers' page
     * (https://developer.github.com/)
     */
    
    public static String mCallbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    
    public InstagramApp(Context context, String clientId, String clientSecret,
            String callbackUrl) {
        
        mClientId = clientId;
        mClientSecret = clientSecret;
        mSession = new InstagramSession(context);
        mAccessToken = mSession.getAccessToken();
        mCallbackUrl = callbackUrl;
        mAuthUrl = AUTH_URL
                + "?client_id="
                + clientId
                + "&redirect_uri="
                + mCallbackUrl
                + "&response_type=code&display=touch&scope=likes+comments+relationships";
        
        OAuthDialogListener listener = new OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                getAccessToken(code);
            }
            
            @Override
            public void onError(String error) {
                mListener.onFail("Authorization failed");
            }
        };
        
        mDialog = new InstagramDialog(context, mAuthUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);
    }
    
    private void getAccessToken(final String code) {
        mProgress.setMessage("Getting access token ...");
        mProgress.show();
        
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(
                            urlConnection.getOutputStream());
                    writer.write("client_id=" + mClientId + "&client_secret="
                            + mClientSecret + "&grant_type=authorization_code"
                            + "&redirect_uri=" + mCallbackUrl + "&code=" + code);
                    writer.flush();
                    String response = streamToString(urlConnection
                            .getInputStream());
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response)
                            .nextValue();
                    
                    mAccessToken = jsonObj.getString("access_token");
                    String id = jsonObj.getJSONObject("user").getString("id");
                    String user = jsonObj.getJSONObject("user").getString(
                            "username");
                    String name = jsonObj.getJSONObject("user").getString(
                            "full_name");
                    String bio = jsonObj.getJSONObject("user").getString("bio");
                    String website = jsonObj.getJSONObject("user").getString(
                            "website");
                    String profilepictureurl = jsonObj.getJSONObject("user")
                            .getString("profile_picture");
                    
                    mSession.storeAccessToken(mAccessToken, id, user, name,
                            bio, website, profilepictureurl);
                    
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                
                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                mProgress.dismiss();
                if (msg.arg1 == 1) {
                    mListener.onFail("Failed to get access token");
                } else if (msg.arg1 == 2) {
                    mListener.onFail("Failed to get user information");
                }
            } else {
                mProgress.dismiss();
                mListener.onSuccess();
            }
        }
    };
    
    public boolean hasAccessToken() {
        return (mAccessToken == null) ? false : true;
    }
    
    public void setListener(OAuthAuthenticationListener listener) {
        mListener = listener;
    }
    
    public String getUserName() {
        return mSession.getUsername();
    }
    
    public String getId() {
        return mSession.getId();
    }
    
    public String getName() {
        return mSession.getName();
    }
    
    public String getBio() {
        return mSession.getApiBio();
    }
    
    public String getWebsite() {
        return mSession.getApiWebsite();
    }
    
    public String getProfilePicture() {
        return mSession.getApiProfilePicture();
    }
    
    public void authorize() {
        mDialog.show();
    }
    
    private String streamToString(InputStream is) throws IOException {
        String str = "";
        
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                
                reader.close();
            } finally {
                is.close();
            }
            
            str = sb.toString();
        }
        
        return str;
    }
    
    public void resetAccessToken() {
        if (mAccessToken != null) {
            mSession.resetAccessToken();
            mAccessToken = null;
        }
    }
    
    public interface OAuthAuthenticationListener {
        public abstract void onSuccess();
        
        public abstract void onFail(String error);
    }
}