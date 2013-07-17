package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.googleplus.MomentUtil;
import com.googleplus.PlusClientFragment;
import com.googleplus.PlusClientFragment.OnSignedInListener;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.SortBasedOnName;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class VueLoginActivity extends FragmentActivity implements
		OnSignedInListener, OnPeopleLoadedListener, OnPersonLoadedListener {

	private boolean hideCancelButton = false;
	private boolean fromBezelMenuLogin = false;
	private String fromInviteFriends = null;
	private boolean fromDetailsFbShare = false;
	private boolean dontCallUserInfoChangesMethod = false;
	private boolean fromGoogleplusInvitefriends = false;
	private boolean facebookFlag = false;
	private String fbFriendId = null;
	private String fbFriendName = null;
	private boolean googleplusFriendInvite = false;
	private boolean googleplusAutomaticLogin = false;
	SharedPreferences sharedPreferencesObj;

	/** The button should say "View item" in English. */
	private static final String LABEL_VIEW_ITEM = "VIEW_ITEM";

	// Google+ integration
	public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
	public PlusClientFragment mSignInFragment;
	private boolean googleplusLoggedinDialogFlag = false;
	private static final int REQUEST_CODE_INTERACTIVE_POST = 3;
	Activity context;
	LinearLayout socialIntegrationMainLayout;
	Bundle bundle = null;
	private static final String TAG = "VueLoginActivity";
	private final List<String> PERMISSIONS = new ArrayList<String>();
	ProgressDialog facebookProgressialog, googlePlusProgressDialog;
	private final String PENDING_ACTION_BUNDLE_KEY = VueApplication
			.getInstance().getString(R.string.pendingActionBundleKey);
	private PendingAction pendingAction = PendingAction.NONE;

	enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.socialnetworkingloginscreen);

		PERMISSIONS.add("publish_actions");
		PERMISSIONS.add("email");
		PERMISSIONS.add("user_birthday");
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		RelativeLayout googleplusign_in_buttonlayout = (RelativeLayout) findViewById(R.id.googleplusign_in_buttonlayout);
		RelativeLayout fblog_in_buttonlayout = (RelativeLayout) findViewById(R.id.fblog_in_buttonlayout);
		LoginButton login_button = (LoginButton) findViewById(R.id.login_button);
		socialIntegrationMainLayout = (LinearLayout) findViewById(R.id.socialintegrationmainlayotu);
		RelativeLayout cancellayout = (RelativeLayout) findViewById(R.id.cancellayout);
		context = this;
		sharedPreferencesObj = this.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		facebookProgressialog = ProgressDialog.show(context, "Facebook",
				"Sharing....", true);
		googlePlusProgressDialog = ProgressDialog.show(context, "Google+",
				"Loading....", true);
		googlePlusProgressDialog.dismiss();
		facebookProgressialog.dismiss();

		bundle = getIntent().getExtras();
		if (bundle != null) {
			hideCancelButton = bundle
					.getBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG);
			fromBezelMenuLogin = bundle
					.getBoolean(VueConstants.FROM_BEZELMENU_LOGIN);
			fromInviteFriends = bundle
					.getString(VueConstants.FROM_INVITEFRIENDS);
			fromDetailsFbShare = bundle
					.getBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE);
			fbFriendId = bundle.getString(VueConstants.FB_FRIEND_ID);
			fbFriendName = bundle.getString(VueConstants.FB_FRIEND_NAME);
			googleplusFriendInvite = bundle
					.getBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE);
			googleplusAutomaticLogin = bundle
					.getBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN);
			}

		// Facebook Invite friend
		if (fbFriendId != null) {
			socialIntegrationMainLayout.setVisibility(View.GONE);
			ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
			trendingbg.setVisibility(View.GONE);
			publishFeedDialog(fbFriendId, fbFriendName);
		}
		// Google+ invite friend
		else if (googleplusFriendInvite) {
			socialIntegrationMainLayout.setVisibility(View.GONE);
			ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
			trendingbg.setVisibility(View.GONE);
			googlePlusProgressDialog.show();
			dontCallUserInfoChangesMethod = true;
			mSignInFragment = PlusClientFragment.getPlusClientFragment(
					VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
		} else if (googleplusAutomaticLogin) {
			socialIntegrationMainLayout.setVisibility(View.GONE);
			ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
			trendingbg.setVisibility(View.GONE);
			googlePlusProgressDialog.show();
			dontCallUserInfoChangesMethod = true;
			mSignInFragment = PlusClientFragment.getPlusClientFragment(
					VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
			mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
		} else {
			if (fromDetailsFbShare) {
				socialIntegrationMainLayout.setVisibility(View.GONE);
				ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
				trendingbg.setVisibility(View.GONE);
				sharedPreferencesObj = this.getSharedPreferences(
						VueConstants.SHAREDPREFERENCE_NAME, 0);
				boolean facebookloginflag = sharedPreferencesObj.getBoolean(
						VueConstants.FACEBOOK_LOGIN, false);
				if (facebookloginflag) {
					ArrayList<clsShare> filePathList = bundle
							.getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
					shareToFacebook(filePathList,
							bundle.getString(VueConstants.FBPOST_TEXT));
				} else {
					socialIntegrationMainLayout.setVisibility(View.VISIBLE);
					googleplusign_in_buttonlayout.setVisibility(View.GONE);
					fblog_in_buttonlayout.setVisibility(View.INVISIBLE);
					cancellayout.setVisibility(View.GONE);
					login_button.performClick();
				}
			} else {
				mSignInFragment = PlusClientFragment.getPlusClientFragment(
						VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
				if (hideCancelButton) {
					cancellayout.setVisibility(View.GONE);
				}
				boolean fbloginfalg = sharedPreferencesObj.getBoolean(
						VueConstants.FACEBOOK_LOGIN, false);
				boolean googleplusloginfalg = sharedPreferencesObj.getBoolean(
						VueConstants.GOOGLEPLUS_LOGIN, false);
				if (fromInviteFriends != null) {
					if (fromInviteFriends.equals(VueConstants.FACEBOOK)) {
						googleplusign_in_buttonlayout.setVisibility(View.GONE);
						facebookFlag = true;
					}
					if (fromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
						dontCallUserInfoChangesMethod = true;
						fromGoogleplusInvitefriends = true;
						fblog_in_buttonlayout.setVisibility(View.GONE);
					}
				} else if (fromBezelMenuLogin) {
					if (fbloginfalg) {
						dontCallUserInfoChangesMethod = true;
						fblog_in_buttonlayout.setVisibility(View.GONE);
					} else if (googleplusloginfalg) {
						facebookFlag = true;
						googleplusign_in_buttonlayout.setVisibility(View.GONE);
					}
				}
				googleplusign_in_buttonlayout
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								socialIntegrationMainLayout
										.setVisibility(View.GONE);
								googleplusLoggedinDialogFlag = true;
								if (fromGoogleplusInvitefriends)
									googlePlusProgressDialog.show();
								mSignInFragment
										.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
							}
						});
				login_button.setPublishPermissions(PERMISSIONS);
				login_button
						.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
							public void onUserInfoFetched(GraphUser user) {
								if (!dontCallUserInfoChangesMethod)
									updateUI();
							}
						});
				cancellayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						finish();
					}
				});
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(VueLoginActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction(null, null);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
		outState.putString(VueConstants.FROM_INVITEFRIENDS, fromInviteFriends);
		outState.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
				fromDetailsFbShare);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// From Googleplus app to send the invitation to friend
		if (requestCode == REQUEST_CODE_INTERACTIVE_POST) {
			if (googlePlusProgressDialog != null)
				if (googlePlusProgressDialog.isShowing())
					googlePlusProgressDialog.dismiss();
			finish();
		}
		try {
			uiHelper.onActivityResult(requestCode, resultCode, data);
			if (!dontCallUserInfoChangesMethod)
				updateUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			mSignInFragment.handleOnActivityResult(requestCode, resultCode,
					data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSignedIn(PlusClient plusClient) {
		SharedPreferences.Editor editor = sharedPreferencesObj.edit();
		editor.putBoolean(VueConstants.VUE_LOGIN, true);
		editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, true);
		editor.putString(VueConstants.GOOGLEPLUS_USER_EMAIL,
				plusClient.getAccountName());
		editor.commit();
		plusClient.loadPerson(this, "me");
		if (!googleplusFriendInvite && !facebookFlag
				&& !googleplusAutomaticLogin) {
			Toast.makeText(this,
					plusClient.getAccountName() + " is connected.",
					Toast.LENGTH_LONG).show();
		}
		// To show Google+ App install dialog after login with Google+
		if (googleplusLoggedinDialogFlag) {

			boolean installed = appInstalledOrNot(VueConstants.GOOGLEPLUS_PACKAGE_NAME);
			if (!installed) {
				if (googlePlusProgressDialog != null
						&& googlePlusProgressDialog.isShowing())
					googlePlusProgressDialog.dismiss();
				showAlertMessageForGoolgePlusAppInstalation();
			}
		}

		if (googleplusFriendInvite) {
			share(plusClient,
					this,
					"Invitation from Vue application.",
					VueLandingPageActivity.googlePlusFriendsDetailsList
							.get(bundle
									.getInt(VueConstants.GOOGLEPLUS_FRIEND_INDEX))
							.getGoogleplusFriend());
		} else {
			plusClient.loadPeople(this, Person.Collection.VISIBLE);
		}
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPeopleLoaded(ConnectionResult status,
			PersonBuffer personBuffer, String nextPageToken) {
		if (ConnectionResult.SUCCESS == status.getErrorCode()) {
			VueLandingPageActivity.googlePlusFriendsDetailsList = new ArrayList<FbGPlusDetails>();
			if (personBuffer != null && personBuffer.getCount() > 0) {
				for (Person p : personBuffer) {
					FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(
							null, p.getDisplayName(), p.getImage().getUrl(), p);
					VueLandingPageActivity.googlePlusFriendsDetailsList
							.add(googlePlusFriendsDetailsObj);
				}
				Collections.sort(
						VueLandingPageActivity.googlePlusFriendsDetailsList,
						new SortBasedOnName());
				if (fromInviteFriends != null
						&& fromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
					BaseActivity.mFrag.getFriendsList(getResources().getString(
							R.string.sidemenu_sub_option_Gmail));
				}
			}
		}
		if (googlePlusProgressDialog != null
				&& googlePlusProgressDialog.isShowing())
			googlePlusProgressDialog.dismiss();
		if (!facebookFlag)
			finish();
	}

	private void showAlertMessageForGoolgePlusAppInstalation() {
		final Dialog gplusdialog = new Dialog(this,
				R.style.Theme_Dialog_Translucent);
		gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
		TextView noButton = (TextView) gplusdialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) gplusdialog.findViewById(R.id.okbutton);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				gplusdialog.dismiss();
				Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
						.parse("market://details?id="
								+ VueConstants.GOOGLEPLUS_PACKAGE_NAME));
				startActivity(goToMarket);
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				gplusdialog.dismiss();
			}
		});
		gplusdialog.show();
		gplusdialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
	}

	public void saveFBLoginDetails(final Session session) {
		sharedPreferencesObj = this.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		final SharedPreferences.Editor editor = sharedPreferencesObj.edit();
		Request.executeMeRequestAsync(session, new GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user,
					com.facebook.Response response) {
				// TODO Auto-generated method stub
				if (user != null) {
					String location = "";
					try {
						JSONObject contentArray = null;
						contentArray = new JSONObject(user.getLocation() + "");
						for (int i = 0; i < contentArray.length(); i++) {
							location = contentArray.getJSONArray("state")
									.getJSONObject(1).getString("name");
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					editor.putString(
							VueConstants.FACEBOOK_USER_PROFILE_PICTURE,
							VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
									+ user.getId()
									+ VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL);
					editor.putString(VueConstants.FACEBOOK_USER_EMAIL,
							user.getProperty("email") + "");
					editor.putString(VueConstants.FACEBOOK_USER_NAME,
							user.getName());
					editor.putString(VueConstants.FACEBOOK_USER_DOB,
							user.getBirthday());
					editor.putString(VueConstants.FACEBOOK_USER_GENDER,
							user.getProperty("gender") + "");
					editor.putString(VueConstants.FACEBOOK_USER_LOCATION,
							location);
					editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
							session.getAccessToken());
					editor.putBoolean(VueConstants.VUE_LOGIN, true);
					editor.putBoolean(VueConstants.FACEBOOK_LOGIN, true);
					editor.commit();
					if (fromInviteFriends != null
							&& fromInviteFriends.equals(VueConstants.FACEBOOK)) {
						fromInviteFriends = null;
						try {
								BaseActivity.mFrag
										.getFriendsList(context
												.getResources()
												.getString(
														R.string.sidemenu_sub_option_Facebook));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (!fromDetailsFbShare)
						finish();
				} else {
					editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
							session.getAccessToken());
					editor.putBoolean(VueConstants.VUE_LOGIN, true);
					editor.putBoolean(VueConstants.FACEBOOK_LOGIN, true);
					editor.commit();
					if (fromInviteFriends != null
							&& fromInviteFriends.equals(VueConstants.FACEBOOK)) {
						fromInviteFriends = null;
						try {
							BaseActivity.mFrag
									.getFriendsList(context
											.getResources()
											.getString(
													R.string.sidemenu_sub_option_Facebook));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!fromDetailsFbShare)
						finish();
				}
			}
		});
		try {
			socialIntegrationMainLayout.setVisibility(View.GONE);
			ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
			trendingbg.setVisibility(View.GONE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean fbloggedin = (session != null && session.isOpened());
		if (fbloggedin) {
			saveFBLoginDetails(session);
			if (fromDetailsFbShare) {
				try {
					ArrayList<clsShare> filePathList = bundle
							.getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
					shareToFacebook(filePathList,
							bundle.getString(VueConstants.FBPOST_TEXT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void shareToFacebook(ArrayList<clsShare> fileList, String articledesc) {
		performPublish(PendingAction.POST_PHOTO, fileList, articledesc);
	}

	void performPublish(PendingAction action, ArrayList<clsShare> fileList,
			String articledesc) {
		Session session = Session.getActiveSession();
		if (session != null) {
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction(fileList, articledesc);
			} else {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSIONS));
			}
		}
	}

	boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	void handlePendingAction(ArrayList<clsShare> fileList, String articledesc) {
		postPhoto(fileList, articledesc);
	}

	void postPhoto(final ArrayList<clsShare> fileList, final String articledesc) {
		if (hasPublishPermission()) {
			facebookProgressialog.show();
			// Post photo....
			if (fileList != null) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < fileList.size(); i++) {
							final File f = new File(fileList.get(i)
									.getFilepath());
							if (!f.exists()) {
								@SuppressWarnings("rawtypes")
								Response.Listener listener = new Response.Listener<InputStream>() {
									@Override
									public void onResponse(InputStream is) {
										OutputStream os = null;
										try {
											os = new FileOutputStream(f);
										} catch (FileNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										Utils.CopyStream(is, os);
									}
								};
								Response.ErrorListener errorListener = new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError arg0) {
										Log.e(TAG, arg0.getMessage());
									}
								};
								ImageRequest imagerequestObj = new ImageRequest(
										fileList.get(i).getImageUrl(),
										listener, 0, 0, null, errorListener);
								VueApplication.getInstance().getRequestQueue()
										.add(imagerequestObj);
							}
							final int index = i;
							VueLoginActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Bundle parameters = new Bundle(1);
									parameters.putString("message", articledesc
											+ "");
									ParcelFileDescriptor descriptor = null;
									try {
										descriptor = ParcelFileDescriptor
												.open(new File(fileList.get(
														index).getFilepath()),
														ParcelFileDescriptor.MODE_READ_ONLY);
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									parameters.putParcelable("picture",
											descriptor);
									Callback callback = new Request.Callback() {
										public void onCompleted(
												com.facebook.Response response) {
											if (index == fileList.size() - 1) {
												facebookProgressialog.dismiss();
												showPublishResult(
														VueLoginActivity.this
																.getString(R.string.photo_post),
														response.getGraphObject(),
														response.getError());
											}
										}
									};
									Request request = new Request(Session
											.getActiveSession(), "me/photos",
											parameters, HttpMethod.POST,
											callback);
									request.executeAsync();
								}
							});
						}
					}
				});
				t.start();
			} else {
				facebookProgressialog.dismiss();
			}
		}
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String alertMessage = null;
		if (error == null) {
			alertMessage = VueLoginActivity.this
					.getString(R.string.successfully_posted_post);
		} else {
			if (error.getErrorCode() == 506) {
				alertMessage = VueLoginActivity.this
						.getString(R.string.duplicatepostmesg);
			} else if (error.getErrorCode() == 368) {
				alertMessage = VueLoginActivity.this
						.getString(R.string.facebookwarningmesg);
			} else {
				alertMessage = error.getErrorMessage();
			}
		}
		final Dialog dialog = new Dialog(VueLoginActivity.this,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.networkdialogue);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);
		okbutton.setVisibility(View.GONE);
		View networkdialogline = dialog.findViewById(R.id.networkdialogline);
		networkdialogline.setVisibility(View.GONE);
		TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
		nobutton.setText("ok");
		messagetext.setText(alertMessage);
		nobutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
			}
		});
		dialog.show();
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private void publishFeedDialog(String friend_uid, String friendname) {
		Bundle params = new Bundle();
		params.putString("to", friend_uid);
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
				VueLoginActivity.this, Session.getActiveSession(), params))
				.setOnCompleteListener(new OnCompleteListener() {
					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(VueLoginActivity.this,
										"Posted story, id: " + postId,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(VueLoginActivity.this,
										"Publish cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							Toast.makeText(VueLoginActivity.this,
									"Publish cancelled", Toast.LENGTH_SHORT)
									.show();
						} else {
							// Generic, ex: network error
							Toast.makeText(VueLoginActivity.this,
									"Error posting story", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}).build();
		feedDialog.show();
		feedDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public void onSignedFail() {
		boolean fbloginflag = sharedPreferencesObj.getBoolean(
				VueConstants.FACEBOOK_LOGIN, false);
		SharedPreferences.Editor editor = sharedPreferencesObj.edit();
		if (!fbloginflag)
			editor.putBoolean(VueConstants.VUE_LOGIN, false);
		editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, false);
		editor.commit();
		if (googlePlusProgressDialog != null
				&& googlePlusProgressDialog.isShowing())
			googlePlusProgressDialog.dismiss();
		finish();
	}

	private Intent getInteractivePostIntent(PlusClient plusClient,
			Activity activity, String post, Person googlefriend) {
		String action = "/?view=true";
		Uri callToActionUrl = Uri
				.parse(getString(R.string.plus_example_deep_link_url) + action);
		String callToActionDeepLinkId = getString(R.string.plus_example_deep_link_id)
				+ action;
		// Create an interactive post builder.
		PlusShare.Builder builder = new PlusShare.Builder(activity, plusClient);
		// Set call-to-action metadata.
		builder.addCallToAction(LABEL_VIEW_ITEM, callToActionUrl,
				callToActionDeepLinkId);
		// Set the target url (for desktop use).
		builder.setContentUrl(Uri
				.parse(getString(R.string.plus_example_deep_link_url)));
		// Set the target deep-link ID (for mobile use).
		builder.setContentDeepLinkId(
				getString(R.string.plus_example_deep_link_id), null, null, null);
		List<Person> googlefriendList = new ArrayList<Person>();
		googlefriendList.add(googlefriend);
		builder.setRecipients(googlefriendList);
		// Set the pre-filled message.
		builder.setText(post);
		builder.setType("text/plain");
		return builder.getIntent();
	}

	public void share(PlusClient plusClient, Activity activity, String post,
			Person googleplusFriend) {
		startActivityForResult(
				getInteractivePostIntent(plusClient, activity, post,
						googleplusFriend), REQUEST_CODE_INTERACTIVE_POST);
	}

	@Override
	public void onPersonLoaded(ConnectionResult connectionresult, Person person) {
		// TODO Auto-generated method stub
		if (connectionresult.getErrorCode() == ConnectionResult.SUCCESS) {
			SharedPreferences.Editor editor = sharedPreferencesObj.edit();
			editor.putString(VueConstants.GOOGLEPLUS_USER_NAME,
					person.getDisplayName());
			editor.putString(VueConstants.GOOGLEPLUS_USER_PROFILE_PICTURE,
					person.getImage().getUrl());
			editor.commit();
		}
	}

}
