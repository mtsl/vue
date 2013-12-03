package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.SortBasedOnName;

public class DataEntryInviteFriendsActivity extends Activity {

	private ListView mDataEntryListFragmentInviteFriendsListview = null;
	private boolean mFromGoogleplusFlag;
	private SharedPreferences mSharedPreferences = null;
	private ProgressDialog mProgressDialog = null;
	private ArrayList<Integer> mSelectedPositionsInviteFriendsPositions = new ArrayList<Integer>();
	private ImageView mDataEntryInviteFriendsCloseBtn = null;
	private Button mDataEntryInviteFriendsInviteBtn = null;
	private Bundle mBundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dataentry_invite_friends_screen);
		mDataEntryListFragmentInviteFriendsListview = (ListView) findViewById(R.id.dataentry_list_fragment_invitefriends_list);
		mDataEntryInviteFriendsCloseBtn = (ImageView) findViewById(R.id.data_entry_invite_friends_close_btn);
		mDataEntryInviteFriendsInviteBtn = (Button) findViewById(R.id.data_entry_invite_friends_invite_btn);
		mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mFromGoogleplusFlag = mBundle
					.getBoolean(VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_GOOGLEPLUS_FLAG_KEY);
			// Display Google+ friends
			if (mFromGoogleplusFlag) {
				getFriendsList(
						getResources().getString(
								R.string.sidemenu_sub_option_Googleplus), false);
			}
			// Display Facebook Friends...
			else {
				getFriendsList(
						getResources().getString(
								R.string.sidemenu_sub_option_Facebook), false);
			}
		}
		mDataEntryInviteFriendsCloseBtn
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						finish();
					}
				});
		mDataEntryInviteFriendsInviteBtn
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						if (mSelectedPositionsInviteFriendsPositions != null)
							mSelectedPositionsInviteFriendsPositions.clear();

						int len = mDataEntryListFragmentInviteFriendsListview
								.getCount();
						SparseBooleanArray checked = mDataEntryListFragmentInviteFriendsListview
								.getCheckedItemPositions();
						for (int i = 0; i < len; i++)
							if (checked.get(i)) {
								mSelectedPositionsInviteFriendsPositions.add(i);
							}

						if (mSelectedPositionsInviteFriendsPositions != null
								&& mSelectedPositionsInviteFriendsPositions
										.size() > 0) {
							Intent i = new Intent(
									DataEntryInviteFriendsActivity.this,
									VueLoginActivity.class);
							Bundle b = new Bundle();
							b.putIntegerArrayList(
									VueConstants.GOOGLEPLUS_FRIEND_INDEX,
									mSelectedPositionsInviteFriendsPositions);
							b.putBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE,
									true);
							if (mBundle != null
									&& mBundle
											.getString(VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_FILE_PATH_ARRAY_KEY) != null) {
								b.putString(
										VueConstants.GOOGLEPLUS_FRIEND_IMAGE_PATH_LIST_KEY,
										mBundle.getString(VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_FILE_PATH_ARRAY_KEY));
							}
							i.putExtras(b);
							startActivity(i);
						} else {
							Toast.makeText(
									DataEntryInviteFriendsActivity.this,
									getResources()
											.getString(
													R.string.data_entry_invite_friends_mandatory_mesg),
									Toast.LENGTH_LONG).show();
						}

					}
				});
		if (mFromGoogleplusFlag) {
			mDataEntryInviteFriendsInviteBtn.setVisibility(View.VISIBLE);
			mDataEntryListFragmentInviteFriendsListview
					.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		} else {
			mDataEntryInviteFriendsInviteBtn.setVisibility(View.GONE);
		}

	}

	private void setDataEntryInviteFriendsAdapter(
			List<FbGPlusDetails> friendsList) {
		if (mDataEntryListFragmentInviteFriendsListview != null) {
			mDataEntryListFragmentInviteFriendsListview
					.setAdapter(new InviteFriendsAdapter(this, friendsList,
							mFromGoogleplusFlag));
		}
	}

	public void getFriendsList(String s, boolean fromOnActivityResultMethodFlag) {
		mProgressDialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.pleasewait_mesg));
		mSharedPreferences = getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean facebookloginflag = mSharedPreferences.getBoolean(
				VueConstants.FACEBOOK_LOGIN, false);
		boolean googleplusloginflag = mSharedPreferences.getBoolean(
				VueConstants.GOOGLEPLUS_LOGIN, false);
		if (s.equals(getResources().getString(
				R.string.sidemenu_sub_option_Facebook))) {

			if (facebookloginflag) {
				fbFriendsList();
			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Intent i = new Intent(this, VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.FACEBOOK);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				startActivityForResult(i,
						VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
			}

		} else if (s.equals(getResources().getString(
				R.string.sidemenu_sub_option_Googleplus))) {
			if (googleplusloginflag) {
				getGPlusFriendsList();
			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Intent i = new Intent(this, VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.GOOGLEPLUS);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				startActivityForResult(i,
						VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
			}

		} else {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	// Pull and display G+ friends from plus.google.com.
	private void getGPlusFriendsList() {
		if (VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (VueLandingPageActivity.mGooglePlusFriendsDetailsList.size() > 0) {
				setDataEntryInviteFriendsAdapter(VueLandingPageActivity.mGooglePlusFriendsDetailsList);
			} else {
				Toast.makeText(DataEntryInviteFriendsActivity.this,
						getResources().getString(R.string.fb_no_friends),
						Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			Intent i = new Intent(this, VueLoginActivity.class);
			Bundle b = new Bundle();
			b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
			b.putBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN, true);
			b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
			b.putString(VueConstants.FROM_INVITEFRIENDS,
					VueConstants.GOOGLEPLUS);
			b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
			i.putExtras(b);
			startActivityForResult(i,
					VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
		}
	}

	// Pull and display fb friends from facebook.com
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fbFriendsList() {
		SharedPreferences sharedPreferencesObj = getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		String accessToken = sharedPreferencesObj.getString(
				VueConstants.FACEBOOK_ACCESSTOKEN, null);
		if (accessToken != null) {
			String mainURL = VueConstants.FACEBOOK_GETFRIENDS_URL + accessToken
					+ VueConstants.FACEBOOK_FRIENDS_DETAILS;
			Response.Listener listener = new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					List<FbGPlusDetails> fbGPlusFriends;
					try {
						fbGPlusFriends = JsonParsing(response);
						if (mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						if (fbGPlusFriends != null) {
							setDataEntryInviteFriendsAdapter(fbGPlusFriends);
						} else {
							Toast.makeText(
									DataEntryInviteFriendsActivity.this,
									getResources().getString(
											R.string.fb_no_friends),
									Toast.LENGTH_LONG).show();
							finish();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if (mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
					}
				}
			};

			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e("VueNetworkError",
							"Vue encountered network operations error. Error = "
									+ error.networkResponse);
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
				}
			};

			StringRequest myReq = new StringRequest(Method.GET, mainURL,
					listener, errorListener);

			VueApplication.getInstance().getRequestQueue().add(myReq);

		} else {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	private List<FbGPlusDetails> JsonParsing(String jsonString)
			throws JSONException {
		List<FbGPlusDetails> facebookFriendsDetailsList = null;

		JSONObject mainJsonObj = new JSONObject(jsonString);
		JSONArray dataArray = mainJsonObj.getJSONArray("data");
		if (dataArray != null && dataArray.length() > 0) {
			facebookFriendsDetailsList = new ArrayList<FbGPlusDetails>();

			for (int i = 0; i < dataArray.length(); i++) {

				JSONObject jsonObj = dataArray.getJSONObject(i);
				FbGPlusDetails objFacebookFriendsDetails = new FbGPlusDetails(
						jsonObj.getString("id"), jsonObj.getString("name"),
						jsonObj.getJSONObject("picture").getJSONObject("data")
								.getString("url"), null);

				facebookFriendsDetailsList.add(objFacebookFriendsDetails);
			}
		}

		if (facebookFriendsDetailsList != null)
			Collections.sort(facebookFriendsDetailsList, new SortBasedOnName());

		return facebookFriendsDetailsList;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("InviteFriends Activity", "request code ::" + requestCode
				+ " ::: result code :::" + resultCode);
		if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
				&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
			if (data != null) {
				if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
					getFriendsList(
							data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY),
							true);
				}
			}
		} else {
			finish();
		}
	}
}
