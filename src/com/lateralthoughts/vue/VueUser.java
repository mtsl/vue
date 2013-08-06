package com.lateralthoughts.vue;

import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import org.json.JSONArray;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.UnsupportedEncodingException;

/**
 * UserCredentials in Vue involves creating a Vue User account on the backend to which we can associate identity
 * layers such as FB id, G+ id, etc.
 * A Vue User may choose to not provide their identity (FB, G+) as they begin exploring the app but may decide to
 * login with a specific id at a later point in time.
 *
 *
 */

public class VueUser implements Serializable {

    public VueUser(String facebookId, String googlePlusId, String emailId){
        mEmailId = emailId;
        mFacebookId = facebookId;
        mGooglePlusId = googlePlusId;
    }

    public void setUsersName(String firstName, String lastName){
        mFirstName = firstName;
        mLastName = lastName;
    }

    public void setBirthday(String birthday){
        mBirthday = birthday;
    }

    public String getFBUserId(){
        return mFacebookId;
    }

    public String getGooglePlusUserId(){
        return mGooglePlusId;
    }

    public void setUserIdentityMethod(VueUserManager.PreferredIdentityLayer identity){
        mUserIdentifier = identity;
    }

    public VueUserManager.PreferredIdentityLayer getUserIdentity(){
        return mUserIdentifier;
    }

    private String mBirthday;
    private String mFacebookId;
    private String mGooglePlusId;
    private String mFirstName;
    private String mLastName;
    private Locale mUserLocale;
    private String mEmailId;
    private VueUserManager.PreferredIdentityLayer mUserIdentifier;
}
