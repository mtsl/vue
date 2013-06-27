package com.lateralthoughts.vue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.GooglePlusFriendsDetails;

/**
 * This is common class for all Vue sharing functionality.
 * @author krishna
 *
 */
public class VueShare {
	
	List<FbGPlusDetails> googlePlusFriendsDetailsList = null;
	
	/**
	 * By Krishna.V
	 * This method is used to get the Google+ friends information.
	 * @param plusClientObj
	 */
	public List<FbGPlusDetails> getGooglePlusFriends(PlusClient plusClientObj)
	{

		googlePlusFriendsDetailsList = null;

		plusClientObj.loadPeople(new OnPeopleLoadedListener() {

			@Override
			public void onPeopleLoaded(ConnectionResult status,
					PersonBuffer personBuffer, String nextPageToken) {

				if (ConnectionResult.SUCCESS == status.getErrorCode()) {
					if (personBuffer != null && personBuffer.getCount() > 0) {
						googlePlusFriendsDetailsList = new ArrayList<FbGPlusDetails>();
						for (Person p : personBuffer) {
							FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(
									p.getDisplayName(), p.getImage().getUrl());

							googlePlusFriendsDetailsList
									.add(googlePlusFriendsDetailsObj);

						}
					}
				}
			}
		}, Person.Collection.VISIBLE);
		return googlePlusFriendsDetailsList;
	}
	
	
	
	
	/**
	 * By Krishna.V 
	 * This is method will get the list of Facebook friends information.
	 * @param context
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public List<FbGPlusDetails> getFacebookFriends(Context context) throws IOException, JSONException {
		
		List<FbGPlusDetails> facebookFriendsDetailsList = null;
		
		SharedPreferences sharedPreferencesObj = context.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
		
		String accessToken = sharedPreferencesObj.getString(VueConstants.FACEBOOK_ACCESSTOKEN, null);
		
		if(accessToken != null)
		{
		String mainURL = VueConstants.FACEBOOK_GETFRIENDS_URL+accessToken+VueConstants.FACEBOOK_FRIENDS_DETAILS;
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(mainURL);
         HttpResponse response = httpclient.execute(httpget);
         BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
         StringBuffer sb = new StringBuffer("");
         String line = "";
         while ((line = in.readLine()) != null) {                    
             sb.append(line);
         }
         in.close();
         String result = sb.toString();
        // Log.v("My Response :: ", result);

			if (result != null) {
				facebookFriendsDetailsList = JsonParsing(result);
			}
         
		}
		return facebookFriendsDetailsList;
		
	}
	
	/**
	 * By Krishna.V
	 * This method is used to Json Parsing.
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	private List<FbGPlusDetails> JsonParsing(String jsonString) throws JSONException
	{
		List<FbGPlusDetails> facebookFriendsDetailsList = null;

		JSONObject mainJsonObj = new JSONObject(jsonString);
		JSONArray dataArray = mainJsonObj.getJSONArray("data");
		if (dataArray != null && dataArray.length() > 0) {
			facebookFriendsDetailsList = new ArrayList<FbGPlusDetails>();

			for (int i = 0; i < dataArray.length(); i++) {

				JSONObject jsonObj = dataArray.getJSONObject(i);

				FbGPlusDetails objFacebookFriendsDetails = new FbGPlusDetails(
						jsonObj.getString("name"), jsonObj
								.getJSONObject("picture").getJSONObject("data")
								.getString("url"));

				facebookFriendsDetailsList.add(objFacebookFriendsDetails);
			}
		}
		return facebookFriendsDetailsList;
	}
	

}
