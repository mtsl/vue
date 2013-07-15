package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.SortBasedOnName;

/**
 * Fragment for creating Aisle
 * 
 */
public class CreateAilseFragment extends Fragment {

	ListView categoryListview = null;
	LinearLayout lookingForPopup = null, lookingForListviewLayout = null,
			ocassionListviewLayout = null, ocassionPopup = null,
			categoeryPopup = null, categoryListviewLayout = null;
	TextView touchToChangeImage = null, lookingForBigText = null,
			occassionBigText = null, categoryText = null;
	com.lateralthoughts.vue.utils.EditTextBackEvent lookingForText = null,
			occasionText = null, saySomethingAboutAisle = null;
	private static final String categoryitemsArray[] = { "Apparel", "Beauty",
			"Electronics", "Entertainment", "Events", "Food", "Home" };
	private Drawable listDivider = null;
	ImageView createaisleBg = null, categoeryIcon = null;
	Uri selectedCameraImage = null;
	InputMethodManager inputMethodManager;
	int categoryCurrentSelectedPosition = 0;
	boolean dontGoToNextlookingFor = false, dontGoToNextForOccasion = false;
	String previousLookingfor = null, previousOcasion = null,
			previousSaySomething = null;
	String imagePath = null;
	LinearLayout mainheadingrow = null, dataentry_bottom_bottom_layout = null;
	RelativeLayout dataentry_bottom_top_layout = null,
			dataentry_invite_friends_layout = null,
			dataentry_invite_friends_popup_layout = null,
			dataentry_invitefriends_facebooklayout = null;
	public static boolean createAilseKeyboardHiddenShownFlag = false;
	ProgressDialog dataentryInviteFriendsProgressdialog = null;
	SharedPreferences sharedPreferencesObj = null;
	ListView dataEntryInviteFriendsList = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		createAilseKeyboardHiddenShownFlag = true;
		View v = inflater.inflate(R.layout.create_aisleview_fragment,
				container, false);
		inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		lookingForText = (EditTextBackEvent) v
				.findViewById(R.id.lookingfortext);
		dataentry_bottom_bottom_layout = (LinearLayout) v
				.findViewById(R.id.dataentry_bottom_bottom_layout);
		lookingForBigText = (TextView) v.findViewById(R.id.lookingforbigtext);
		lookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		dataEntryInviteFriendsList = (ListView) v
				.findViewById(R.id.data_entry_Invitefriends_list);
		dataentry_bottom_top_layout = (RelativeLayout) v
				.findViewById(R.id.dataentry_bottom_top_layout);
		dataentry_invitefriends_facebooklayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_facebooklayout);
		dataentry_invite_friends_popup_layout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invite_friends_popup_layout);
		occassionBigText = (TextView) v.findViewById(R.id.occassionbigtext);
		ocassionListviewLayout = (LinearLayout) v
				.findViewById(R.id.ocassionlistviewlayout);
		ocassionPopup = (LinearLayout) v.findViewById(R.id.ocassionpopup);
		occasionText = (EditTextBackEvent) v.findViewById(R.id.occasiontext);
		lookingForListviewLayout = (LinearLayout) v
				.findViewById(R.id.lookingforlistviewlayout);
		lookingForPopup = (LinearLayout) v.findViewById(R.id.lookingforpopup);
		touchToChangeImage = (TextView) v.findViewById(R.id.touchtochangeimage);
		saySomethingAboutAisle = (EditTextBackEvent) v
				.findViewById(R.id.saysomethingaboutaisle);
		categoeryIcon = (ImageView) v.findViewById(R.id.categoeryicon);
		categoeryPopup = (LinearLayout) v.findViewById(R.id.categoerypopup);
		categoryText = (TextView) v.findViewById(R.id.categorytext);
		categoryText.setText(categoryitemsArray[0]);
		categoryListview = (ListView) v.findViewById(R.id.categorylistview);
		categoryListviewLayout = (LinearLayout) v
				.findViewById(R.id.categorylistviewlayout);
		mainheadingrow = (LinearLayout) v.findViewById(R.id.mainheadingrow);
		listDivider = getResources().getDrawable(R.drawable.list_divider_line);
		lookingForListviewLayout.setVisibility(View.GONE);
		createaisleBg = (ImageView) v.findViewById(R.id.createaisel_bg);
		categoryListview.setDivider(listDivider);
		dataentry_invite_friends_layout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invite_friends_layout);
		previousLookingfor = lookingForText.getText().toString();
		previousOcasion = occasionText.getText().toString();
		previousSaySomething = saySomethingAboutAisle.getText().toString();
		saySomethingAboutAisle
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1,
							KeyEvent arg2) {
						createAilseKeyboardHiddenShownFlag = false;
						previousSaySomething = saySomethingAboutAisle.getText()
								.toString();
						inputMethodManager.hideSoftInputFromWindow(
								saySomethingAboutAisle.getWindowToken(), 0);
						inputMethodManager.hideSoftInputFromWindow(
								occasionText.getWindowToken(), 0);
						inputMethodManager.hideSoftInputFromWindow(
								lookingForText.getWindowToken(), 0);
						return true;
					}
				});
		saySomethingAboutAisle.setonInterceptListen(new OnInterceptListener() {
			@Override
			public void onInterceptTouch() {
				createAilseKeyboardHiddenShownFlag = false;
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				saySomethingAboutAisle.setText(previousSaySomething);
			}

			@Override
			public void setFlag(boolean flag) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		saySomethingAboutAisle.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				createAilseKeyboardHiddenShownFlag = true;
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		lookingForText
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						createAilseKeyboardHiddenShownFlag = false;
						lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						lookingForBigText.setText(lookingForText.getText()
								.toString());
						previousLookingfor = lookingForText.getText()
								.toString();
						lookingForPopup.setVisibility(View.GONE);
						inputMethodManager.hideSoftInputFromWindow(
								lookingForText.getWindowToken(), 0);
						if (!dontGoToNextlookingFor) {
							createAilseKeyboardHiddenShownFlag = true;
							occassionBigText.setBackgroundColor(getResources()
									.getColor(R.color.yellowbgcolor));
							ocassionPopup.setVisibility(View.VISIBLE);
							occasionText.requestFocus();
							inputMethodManager.showSoftInput(occasionText, 0);
						}
						return true;
					}
				});
		lookingForText.setonInterceptListen(new OnInterceptListener() {
			public void onInterceptTouch() {
				createAilseKeyboardHiddenShownFlag = false;
				lookingForPopup.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				lookingForText.setText(previousLookingfor);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
			}

			@Override
			public void setFlag(boolean flag) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
				return false;
			}
		});

		occasionText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				createAilseKeyboardHiddenShownFlag = false;
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				occassionBigText.setText(" "
						+ occasionText.getText().toString());
				previousOcasion = occasionText.getText().toString();
				ocassionPopup.setVisibility(View.GONE);
				if (!dontGoToNextForOccasion) {
					createAilseKeyboardHiddenShownFlag = true;
					categoryListview.setVisibility(View.VISIBLE);
					categoryListview.setAdapter(new CategoryAdapter(
							getActivity()));
					categoryListviewLayout.setVisibility(View.VISIBLE);
					categoeryPopup.setVisibility(View.VISIBLE);
				}
				return true;
			};
		});
		occasionText.setonInterceptListen(new OnInterceptListener() {
			public void onInterceptTouch() {
				createAilseKeyboardHiddenShownFlag = false;
				lookingForPopup.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				occasionText.setText(previousOcasion);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
			}

			@Override
			public void setFlag(boolean flag) {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		lookingForBigText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				createAilseKeyboardHiddenShownFlag = true;
				dontGoToNextlookingFor = true;
				lookingForPopup.setVisibility(View.VISIBLE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(getResources().getColor(
						R.color.yellowbgcolor));
				ocassionPopup.setVisibility(View.GONE);
				lookingForText.requestFocus();
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.showSoftInput(lookingForText, 0);
				categoryListview.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
			}
		});
		occassionBigText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				createAilseKeyboardHiddenShownFlag = true;
				dontGoToNextForOccasion = true;
				ocassionPopup.setVisibility(View.VISIBLE);
				lookingForPopup.setVisibility(View.GONE);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				occassionBigText.setBackgroundColor(getResources().getColor(
						R.color.yellowbgcolor));
				occasionText.requestFocus();
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.showSoftInput(occasionText, 0);
				categoryListview.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
			}
		});
		categoeryIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lookingForPopup.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				categoryListview.setVisibility(View.VISIBLE);
				categoryListview.setAdapter(new CategoryAdapter(getActivity()));
				categoryListviewLayout.setVisibility(View.VISIBLE);
				categoeryPopup.setVisibility(View.VISIBLE);
			}
		});
		touchToChangeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),
						CreateAisleSelectionActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
				intent.putExtras(b);
				getActivity().startActivityForResult(intent,
						VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
			}
		});
		dataentry_invite_friends_layout
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						dataentry_invite_friends_popup_layout
								.setVisibility(View.VISIBLE);
					}
				});
		dataentry_invitefriends_facebooklayout
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
		return v;
	}

	// Category....
	class CategoryAdapter extends BaseAdapter {
		Activity context;

		public CategoryAdapter(Activity context) {
			super();
			this.context = context;
		}

		class ViewHolder {
			TextView dataentryitemname;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.dataentry_row, null, true);
				holder = new ViewHolder();
				holder.dataentryitemname = (TextView) rowView
						.findViewById(R.id.dataentryitemname);
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
			if (position == categoryCurrentSelectedPosition) {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.black));
				holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
			} else {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.dataentrytextcolor));
				holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
			}
			holder.dataentryitemname.setText(categoryitemsArray[position]);
			rowView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					categoryCurrentSelectedPosition = position;
					categoryText.setText(categoryitemsArray[position]);
					categoryListview.setVisibility(View.GONE);
					categoeryPopup.setVisibility(View.GONE);
					categoryListviewLayout.setVisibility(View.GONE);
				}
			});
			return rowView;
		}

		@Override
		public int getCount() {
			return categoryitemsArray.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
	}

	public void setGalleryORCameraImage(String picturePath) {
		imagePath = picturePath;
		createaisleBg.setImageURI(Uri.fromFile(new File(picturePath)));
	}

	public void addAisleToServer() {
		// Input parameters for Adding Aisle to server request...
		String category = categoryText.getText().toString();
		String lookingFor = lookingForBigText.getText().toString();
		String occasion = occassionBigText.getText().toString();
		String imageUrl = imagePath; // This path is image location stored in
										// locally when user selects from Camera
										// OR Gallery.
		String title = ""; // For Camera and Gallery we don't have title.
		String store = ""; // For Camera and Gallery we don't have store.
		renderUIAfterAddingAisleToServer();
	}

	public void renderUIAfterAddingAisleToServer() {
		mainheadingrow.setVisibility(View.GONE);
		touchToChangeImage.setVisibility(View.GONE);
		dataentry_bottom_bottom_layout.setVisibility(View.GONE);
		lookingForPopup.setVisibility(View.GONE);
		ocassionPopup.setVisibility(View.GONE);
		categoeryPopup.setVisibility(View.GONE);
		categoryListviewLayout.setVisibility(View.GONE);
		dataentry_bottom_top_layout.setVisibility(View.VISIBLE);
	}

	public void getFriendsList(String s) {

		dataentryInviteFriendsProgressdialog = ProgressDialog.show(
				getActivity(), "", "Plase wait...");
		Log.e(getTag(), "SURU : Value of s : " + s);

		sharedPreferencesObj = getActivity().getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);

		boolean facebookloginflag = sharedPreferencesObj.getBoolean(
				VueConstants.FACEBOOK_LOGIN, false);
		boolean googleplusloginflag = sharedPreferencesObj.getBoolean(
				VueConstants.GOOGLEPLUS_LOGIN, false);
		if (s.equals("Facebook")) {

			if (facebookloginflag) {
				fbFriendsList();
			} else {
				if (dataentryInviteFriendsProgressdialog.isShowing()) {
					dataentryInviteFriendsProgressdialog.dismiss();
				}

				Intent i = new Intent(getActivity(), VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.FACEBOOK);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				b.putBoolean(
						VueConstants.DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG,
						true);
				i.putExtras(b);
				startActivity(i);

			}

		} else if (s.equals("Google Plus")) {
			if (googleplusloginflag) {
				Log.e(getTag(), "GOOGLEPLUS : Value of s : 1");
				getGPlusFriendsList();
			} else {
				if (dataentryInviteFriendsProgressdialog.isShowing()) {
					dataentryInviteFriendsProgressdialog.dismiss();
				}
				Log.e(getTag(), "GOOGLEPLUS : Value of s : 2");
				Log.e(getTag(), "GOOGLEPLUS : Value of s : 3");
				Intent i = new Intent(getActivity(), VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.GOOGLEPLUS);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				startActivity(i);
			}

		} else {
			if (dataentryInviteFriendsProgressdialog.isShowing()) {
				dataentryInviteFriendsProgressdialog.dismiss();
			}
		}
	}

	// Pull and display fb friends from facebook.com
	private void fbFriendsList() {

		SharedPreferences sharedPreferencesObj = getActivity()
				.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);

		String accessToken = sharedPreferencesObj.getString(
				VueConstants.FACEBOOK_ACCESSTOKEN, null);

		if (accessToken != null) {
			String mainURL = VueConstants.FACEBOOK_GETFRIENDS_URL + accessToken
					+ VueConstants.FACEBOOK_FRIENDS_DETAILS;

			Response.Listener listener = new Response.Listener<String>() {

				@Override
				public void onResponse(String response) {
					// TODO Auto-generated method stub
					List<FbGPlusDetails> fbGPlusFriends;
					try {
						fbGPlusFriends = JsonParsing(response);
						if (fbGPlusFriends != null) {
							dataEntryInviteFriendsList.setVisibility(View.VISIBLE);
							dataEntryInviteFriendsList
									.setAdapter(new InviteFriendsAdapter(
											getActivity(),
											R.layout.invite_friends,
											fbGPlusFriends));
						}
						if (dataentryInviteFriendsProgressdialog.isShowing()) {
							dataentryInviteFriendsProgressdialog.dismiss();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if (dataentryInviteFriendsProgressdialog.isShowing()) {
							dataentryInviteFriendsProgressdialog.dismiss();
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
					if (dataentryInviteFriendsProgressdialog.isShowing()) {
						dataentryInviteFriendsProgressdialog.dismiss();
					}
				}
			};

			StringRequest myReq = new StringRequest(Method.GET, mainURL,
					listener, errorListener);

			VueApplication.getInstance().getRequestQueue().add(myReq);

		}

		else {
			if (dataentryInviteFriendsProgressdialog.isShowing()) {
				dataentryInviteFriendsProgressdialog.dismiss();
			}
		}

	}

	// Pull and display G+ friends from plus.google.com.
	private void getGPlusFriendsList() {
		if (VueLandingPageActivity.googlePlusFriendsDetailsList != null) {
			dataEntryInviteFriendsList.setVisibility(View.VISIBLE);
			dataEntryInviteFriendsList.setAdapter(new InviteFriendsAdapter(
					getActivity(), R.layout.invite_friends,
					VueLandingPageActivity.googlePlusFriendsDetailsList));
			if (dataentryInviteFriendsProgressdialog.isShowing()) {
				dataentryInviteFriendsProgressdialog.dismiss();
			}
		} else {
			if (dataentryInviteFriendsProgressdialog.isShowing()) {
				dataentryInviteFriendsProgressdialog.dismiss();
			}
			Intent i = new Intent(getActivity(), VueLoginActivity.class);
			Bundle b = new Bundle();
			b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
			b.putBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN, true);
			b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
			b.putString(VueConstants.FROM_INVITEFRIENDS,
					VueConstants.GOOGLEPLUS);
			b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
			i.putExtras(b);
			startActivity(i);
		}

	}

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	List<FbGPlusDetails> JsonParsing(String jsonString) throws JSONException {
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

		Collections.sort(facebookFriendsDetailsList, new SortBasedOnName());

		return facebookFriendsDetailsList;
	}
}
