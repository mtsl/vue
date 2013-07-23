package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.lateralthoughts.vue.connectivity.AisleData;
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

/**
 * Fragment for creating Aisle
 * 
 */
public class DataEntryFragment extends Fragment {

	private ListView categoryListview = null, lookingForListview = null,
			ocassionListview = null;
	private LinearLayout lookingForPopup = null,
			lookingForListviewLayout = null, ocassionPopup = null,
			categoeryPopup = null, categoryListviewLayout = null,
			ocassionListviewLayout = null, dataEntryRootLayout = null;
	private TextView touchToChangeImage = null, lookingForBigText = null,
			occassionBigText = null, categoryText = null;
	private com.lateralthoughts.vue.utils.EditTextBackEvent lookingForText = null,
			occasionText = null,
			saySomethingAboutAisle = null,
			findAtText = null;
	private static final String categoryitemsArray[] = { "Apparel", "Beauty",
			"Electronics", "Entertainment", "Events", "Food", "Home" };
	private Drawable listDivider = null;
	private ImageView createaisleBg = null, categoeryIcon = null;
	private InputMethodManager inputMethodManager;
	private boolean dontGoToNextlookingFor = false,
			dontGoToNextForOccasion = false;
	private String previousLookingfor = null, previousOcasion = null,
			previousFindAtText = null, previousSaySomething = null;
	private String imagePath = null, resizedImagePath = null;
	private LinearLayout mainHeadingRow = null;
	private RelativeLayout dataEntryBottomTopLayout = null,
			dataEntryInviteFriendsLayout = null,
			dataEntryInviteFriendsPopupLayout = null,
			dataEntryInviteFriendsFacebookLayout = null,
			dataEntryBottomBottomLayout = null,
			dataEntryInviteFriendsCancelLayout = null,
			dataEntryInviteFriendsGoogleplusLayout = null;
	private ImageView findAtIcon = null;
	public ShareDialog mShare = null;
	private boolean addImageToAisleFlag = false, editAisleImageFlag = false;
	private float screenHeight = 0, screenWidth = 0;
	private LinearLayout findAtPopup = null;
	private ViewPager dataEntryAislesViewpager = null;
	private static final int AISLE_IMAGE_MARGIN = 96;
	private static final String LOOKING_FOR = "Looking";
	private static final String OCCASION = " Occasion";
	private static final String CATEGORY = "Category";
	private ArrayList<String> aisleImagePathList = new ArrayList<String>();
	private int currentPagePosition = 0;
	public static boolean msaySomethingAboutAisleClicked = false;
	private ArrayList<String> mLookingForAisleKeywordsList = null,
			mOccassionAisleKeywordsList = null,
			mCategoryAilseKeywordsList = null;

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
		DisplayMetrics dm = getResources().getDisplayMetrics();
		screenHeight = dm.heightPixels;
		screenHeight = screenHeight
				- Utils.dipToPixels(getActivity(), AISLE_IMAGE_MARGIN);
		screenWidth = dm.widthPixels;
		View v = inflater.inflate(R.layout.data_entry_fragment, container,
				false);
		inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		lookingForText = (EditTextBackEvent) v
				.findViewById(R.id.lookingfortext);
		dataEntryAislesViewpager = (ViewPager) v
				.findViewById(R.id.dataentry_aisles_viewpager);
		ocassionListviewLayout = (LinearLayout) v
				.findViewById(R.id.ocassionlistviewlayout);
		dataEntryRootLayout = (LinearLayout) v
				.findViewById(R.id.dataentry_root_layout);
		ocassionListview = (ListView) v.findViewById(R.id.ocassionlistview);
		findAtIcon = (ImageView) v.findViewById(R.id.find_at_icon);
		lookingForListview = (ListView) v.findViewById(R.id.lookingforlistview);
		findAtText = (EditTextBackEvent) v.findViewById(R.id.find_at_text);
		findAtPopup = (LinearLayout) v.findViewById(R.id.find_at_popup);
		dataEntryInviteFriendsCancelLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_cancellayout);
		dataEntryBottomBottomLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_bottom_bottom_layout);
		dataEntryInviteFriendsGoogleplusLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_googlepluslayout);
		lookingForBigText = (TextView) v.findViewById(R.id.lookingforbigtext);
		lookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		dataEntryBottomTopLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_bottom_top_layout);
		dataEntryInviteFriendsFacebookLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_facebooklayout);
		dataEntryInviteFriendsPopupLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invite_friends_popup_layout);
		occassionBigText = (TextView) v.findViewById(R.id.occassionbigtext);
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
		mainHeadingRow = (LinearLayout) v.findViewById(R.id.mainheadingrow);
		listDivider = getResources().getDrawable(R.drawable.list_divider_line);
		lookingForListviewLayout.setVisibility(View.GONE);
		createaisleBg = (ImageView) v.findViewById(R.id.createaisel_bg);
		categoryListview.setDivider(listDivider);
		dataEntryInviteFriendsLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invite_friends_layout);
		previousLookingfor = lookingForText.getText().toString();
		previousOcasion = occasionText.getText().toString();
		previousSaySomething = saySomethingAboutAisle.getText().toString();
		mLookingForAisleKeywordsList = getAisleKeywords(VueConstants.LOOKING_FOR_TABLE);
		if (mLookingForAisleKeywordsList != null) {
			lookingForText.setText(mLookingForAisleKeywordsList.get(0));
			lookingForBigText.setText(mLookingForAisleKeywordsList.get(0));
			lookingForPopup.setVisibility(View.GONE);
			lookingForListviewLayout.setVisibility(View.GONE);
		} else {
			lookingForListviewLayout.setVisibility(View.GONE);
			lookingForText.requestFocus();
			inputMethodManager.showSoftInput(lookingForText, 0);
		}
		mOccassionAisleKeywordsList = getAisleKeywords(VueConstants.OCCASION_TABLE);
		if (mOccassionAisleKeywordsList != null) {
			occasionText.setText(mOccassionAisleKeywordsList.get(0));
			occassionBigText.setText(mOccassionAisleKeywordsList.get(0));
		}
		mCategoryAilseKeywordsList = getAisleKeywords(VueConstants.CATEGORY_TABLE);
		if (mCategoryAilseKeywordsList != null) {
			categoryText.setText(mCategoryAilseKeywordsList.get(0));
		}
		saySomethingAboutAisle
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1,
							KeyEvent arg2) {
						saySomethingAboutAisle.setCursorVisible(false);
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
			public void onKeyBackPressed() {
				msaySomethingAboutAisleClicked = false;
				saySomethingAboutAisle.setCursorVisible(false);
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
				return true;
			}
		});
		lookingForText
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						if (lookingForText.getText().toString().trim().length() > 0) {
							lookingForBigText.setText(lookingForText.getText()
									.toString());
						}
						previousLookingfor = lookingForText.getText()
								.toString();
						lookingForPopup.setVisibility(View.GONE);
						lookingForListviewLayout.setVisibility(View.GONE);
						inputMethodManager.hideSoftInputFromWindow(
								lookingForText.getWindowToken(), 0);
						if (!dontGoToNextlookingFor) {
							occassionBigText.setBackgroundColor(getResources()
									.getColor(R.color.yellowbgcolor));
							ocassionPopup.setVisibility(View.VISIBLE);
							if (mOccassionAisleKeywordsList != null
									&& mOccassionAisleKeywordsList.size() > 0) {
								ocassionListviewLayout
										.setVisibility(View.VISIBLE);
								ocassionListview
										.setAdapter(new OccassionAdapter(
												getActivity(),
												mOccassionAisleKeywordsList));
							}
							occasionText.requestFocus();
							inputMethodManager.showSoftInput(occasionText, 0);
						}
						return true;
					}
				});
		lookingForText.setonInterceptListen(new OnInterceptListener() {
			public void onKeyBackPressed() {
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
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
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				if (occasionText.getText().toString().trim().length() > 0) {
					occassionBigText.setText(" "
							+ occasionText.getText().toString());
				}
				previousOcasion = occasionText.getText().toString();
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				if (!dontGoToNextForOccasion) {
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
			public void onKeyBackPressed() {
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
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
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				findAtPopup.setVisibility(View.GONE);
				categoryListview.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				if (addImageToAisleFlag || editAisleImageFlag) {
					showAlertForEditPermission(LOOKING_FOR);
				} else {
					lookingForTextClickFunctionality();
				}
			}
		});
		occassionBigText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				findAtPopup.setVisibility(View.GONE);
				categoryListview.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				if (addImageToAisleFlag || editAisleImageFlag) {
					showAlertForEditPermission(OCCASION);
				} else {
					occassionTextClickFunctionality();
				}
			}
		});
		categoeryIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				findAtPopup.setVisibility(View.GONE);
				if (addImageToAisleFlag || editAisleImageFlag) {
					showAlertForEditPermission(CATEGORY);
				} else {
					categoryIconClickFunctionality();
				}
			}
		});
		touchToChangeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				findAtPopup.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				Intent intent = new Intent(getActivity(),
						CreateAisleSelectionActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
				intent.putExtras(b);
				getActivity().startActivityForResult(intent,
						VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
			}
		});
		dataEntryInviteFriendsLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dataEntryInviteFriendsPopupLayout.setVisibility(View.VISIBLE);
			}
		});
		dataEntryInviteFriendsFacebookLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
						if (appInstalledOrNot(VueConstants.FACEBOOK_PACKAGE_NAME)) {
							mShare = new ShareDialog(getActivity(),
									getActivity());
							if (aisleImagePathList != null) {
								ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
								for (int i = 0; i < aisleImagePathList.size(); i++) {
									clsShare shareObj = new clsShare(null,
											aisleImagePathList.get(i));
									imageUrlList.add(shareObj);
								}
								String shareText = "Your friend "
										+ ""
										+ " wants your opinion - get Vue to see the full details and help "
										+ "" + " out.";
								Intent i = new Intent(getActivity(),
										VueLoginActivity.class);
								Bundle b = new Bundle();
								b.putBoolean(
										VueConstants.CANCEL_BTN_DISABLE_FLAG,
										false);
								b.putString(VueConstants.FROM_INVITEFRIENDS,
										null);
								b.putBoolean(
										VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
										true);
								b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN,
										false);
								b.putString(VueConstants.FBPOST_TEXT, shareText);
								b.putParcelableArrayList(
										VueConstants.FBPOST_IMAGEURLS,
										imageUrlList);
								i.putExtras(b);
								getActivity().startActivity(i);
							}
						} else {
							Toast.makeText(getActivity(),
									"Facebook Application was not installed.",
									Toast.LENGTH_LONG).show();
						}
					}
				});
		dataEntryInviteFriendsGoogleplusLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
						if (appInstalledOrNot(VueConstants.GOOGLEPLUS_PACKAGE_NAME)) {
							ArrayList<Uri> imageUris = new ArrayList<Uri>();
							for (int i = 0; i < aisleImagePathList.size(); i++) {
								imageUris.add(Uri.fromFile(new File(
										aisleImagePathList.get(i))));
							}
							String shareText = "Your friend "
									+ ""
									+ " wants your opinion - get Vue to see the full details and help "
									+ "" + " out.";
							Intent sendIntent = new Intent(
									android.content.Intent.ACTION_SEND);
							sendIntent.setType("text/plain");
							sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
							sendIntent.putExtra(Intent.EXTRA_STREAM,
									Uri.parse("mailto:"));
							sendIntent.putExtra(
									android.content.Intent.EXTRA_TEXT,
									shareText);
							sendIntent.putParcelableArrayListExtra(
									Intent.EXTRA_STREAM, imageUris);
							String activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
							sendIntent.setClassName(
									VueConstants.GOOGLEPLUS_PACKAGE_NAME,
									activityname);
							getActivity().startActivityForResult(sendIntent,
									VueConstants.SHARE_INTENT_REQUEST_CODE);
						} else {
							Toast.makeText(getActivity(),
									"Google+ Application was not installed.",
									Toast.LENGTH_LONG).show();
						}
					}
				});
		dataEntryInviteFriendsCancelLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
					}
				});
		findAtIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findAtPopup.setVisibility(View.VISIBLE);
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				findAtText.requestFocus();
				inputMethodManager.showSoftInput(findAtText, 0);
			}
		});
		findAtText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				previousFindAtText = findAtText.getText().toString();
				findAtPopup.setVisibility(View.GONE);
				return true;
			};
		});
		findAtText.setonInterceptListen(new OnInterceptListener() {
			public void onKeyBackPressed() {
				findAtText.setText(previousFindAtText);
				findAtPopup.setVisibility(View.GONE);
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
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
		dataEntryRootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// hiding keyboard
				inputMethodManager.hideSoftInputFromWindow(
						saySomethingAboutAisle.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				findAtPopup.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			}
		});
		saySomethingAboutAisle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				msaySomethingAboutAisleClicked = true;
				inputMethodManager.hideSoftInputFromWindow(
						occasionText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						lookingForText.getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromWindow(
						findAtText.getWindowToken(), 0);
				lookingForPopup.setVisibility(View.GONE);
				lookingForListviewLayout.setVisibility(View.GONE);
				ocassionPopup.setVisibility(View.GONE);
				ocassionListviewLayout.setVisibility(View.GONE);
				categoeryPopup.setVisibility(View.GONE);
				findAtPopup.setVisibility(View.GONE);
				categoryListviewLayout.setVisibility(View.GONE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				saySomethingAboutAisle.setCursorVisible(true);
				saySomethingAboutAisle.requestFocus();
				inputMethodManager.showSoftInput(saySomethingAboutAisle, 0);
			}
		});
		lookingForText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				ArrayList<String> tempLookingForKeywordsList = null;
				if (mLookingForAisleKeywordsList != null
						&& mLookingForAisleKeywordsList.size() > 0) {
					tempLookingForKeywordsList = new ArrayList<String>();
					for (int i = 0; i < mLookingForAisleKeywordsList.size(); i++) {
						String temp = mLookingForAisleKeywordsList.get(i);
						if (temp.length() >= s.length()) {
							String tempString = temp.substring(0, s.length());
							if (tempString.equalsIgnoreCase(s.toString())) {
								tempLookingForKeywordsList
										.add(mLookingForAisleKeywordsList
												.get(i));
							}
						}
					}
					lookingForListview.setAdapter(new LookingForAdapter(
							getActivity(), tempLookingForKeywordsList));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
		occasionText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				ArrayList<String> tempOccassionKeywordsList = null;
				if (mOccassionAisleKeywordsList != null
						&& mOccassionAisleKeywordsList.size() > 0) {
					tempOccassionKeywordsList = new ArrayList<String>();
					for (int i = 0; i < mOccassionAisleKeywordsList.size(); i++) {
						String temp = mOccassionAisleKeywordsList.get(i);
						if (temp.length() > s.length()) {
							String tempString = temp.substring(0, s.length());
							if (tempString.equalsIgnoreCase(s.toString())) {
								tempOccassionKeywordsList
										.add(mOccassionAisleKeywordsList.get(i));
							}
						}
					}
					ocassionListview.setAdapter(new OccassionAdapter(
							getActivity(), tempOccassionKeywordsList));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
		return v;
	}

	public void createAisleClickFunctionality() {
		inputMethodManager.hideSoftInputFromWindow(
				saySomethingAboutAisle.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(
				occasionText.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(
				lookingForText.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(findAtText.getWindowToken(),
				0);
		lookingForPopup.setVisibility(View.GONE);
		lookingForListviewLayout.setVisibility(View.GONE);
		ocassionPopup.setVisibility(View.GONE);
		ocassionListviewLayout.setVisibility(View.GONE);
		categoeryPopup.setVisibility(View.GONE);
		findAtPopup.setVisibility(View.GONE);
		categoryListviewLayout.setVisibility(View.GONE);
		occassionBigText.setBackgroundColor(Color.TRANSPARENT);
		lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		if (!(lookingForBigText.getText().toString().trim().equals(LOOKING_FOR))
				&& !(occassionBigText.getText().toString().trim()
						.equals(OCCASION))) {
			if (addImageToAisleFlag) {
				addImageToAisleToServer();
			} else {
				addAisleToServer();
			}
		} else {
			showAlertForMandotoryFields();
		}

	}

	public void editButtonClickFunctionality() {

		editAisleImageFlag = true;
		currentPagePosition = dataEntryAislesViewpager.getCurrentItem();
		resizedImagePath = aisleImagePathList.get(currentPagePosition);
		imagePath = aisleImagePathList.get(currentPagePosition);
		createaisleBg.setImageURI(Uri.fromFile(new File(aisleImagePathList
				.get(currentPagePosition))));
		dataEntryAislesViewpager.setVisibility(View.GONE);
		createaisleBg.setVisibility(View.VISIBLE);
		DataEntryActivity obj = (DataEntryActivity) getActivity();
		obj.getSupportActionBar().setTitle(
				getResources().getString(R.string.edit_aisle_screen_title));
		dataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
		dataEntryBottomTopLayout.setVisibility(View.GONE);
		DataEntryActivity activity = (DataEntryActivity) getActivity();
		activity.isNewActionBar = false;
		activity.invalidateOptionsMenu();
		mainHeadingRow.setVisibility(View.VISIBLE);
		touchToChangeImage.setVisibility(View.VISIBLE);
		occassionBigText.setBackgroundColor(Color.TRANSPARENT);
		lookingForBigText.setBackgroundColor(Color.TRANSPARENT);

	}

	public void shareClickFunctionality() {

		mShare = new ShareDialog(getActivity(), getActivity());
		if (aisleImagePathList != null) {
			ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
			for (int i = 0; i < aisleImagePathList.size(); i++) {
				clsShare shareObj = new clsShare(null,
						aisleImagePathList.get(i));
				imageUrlList.add(shareObj);
			}
			mShare.share(imageUrlList, "", "");
		}

	}

	public void addImageToAisleButtonClickFunctionality() {

		addImageToAisleFlag = true;
		Intent intent = new Intent(getActivity(),
				CreateAisleSelectionActivity.class);
		Bundle b = new Bundle();
		b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
		intent.putExtras(b);
		getActivity().startActivityForResult(intent,
				VueConstants.CREATE_AILSE_ACTIVITY_RESULT);

	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getActivity().getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	private void showAlertForEditPermission(final String sourceName) {
		final Dialog dialog = new Dialog(getActivity(),
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
		TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(getResources().getString(
				R.string.dataentry_edit_permission));
		okButton.setText("Yes");
		noButton.setText("No");
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				if (sourceName.equals(LOOKING_FOR)) {
					lookingForTextClickFunctionality();
				} else if (sourceName.equals(OCCASION)) {
					occassionTextClickFunctionality();
				} else if (sourceName.equals(CATEGORY)) {
					categoryIconClickFunctionality();
				}
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showAlertForMandotoryFields() {
		final Dialog dialog = new Dialog(getActivity(),
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
		TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(getResources().getString(
				R.string.dataentry_mandtory_field_mesg));
		okButton.setVisibility(View.GONE);
		noButton.setText("OK");
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void lookingForTextClickFunctionality() {
		dontGoToNextlookingFor = true;
		lookingForPopup.setVisibility(View.VISIBLE);
		if (mLookingForAisleKeywordsList != null
				&& mLookingForAisleKeywordsList.size() > 0) {
			lookingForListviewLayout.setVisibility(View.VISIBLE);
			lookingForListview.setAdapter(new LookingForAdapter(getActivity(),
					mLookingForAisleKeywordsList));
		}
		lookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		lookingForText.requestFocus();
		inputMethodManager.showSoftInput(lookingForText, 0);
	}

	private void occassionTextClickFunctionality() {
		dontGoToNextForOccasion = true;
		ocassionPopup.setVisibility(View.VISIBLE);
		if (mOccassionAisleKeywordsList != null
				&& mOccassionAisleKeywordsList.size() > 0) {
			ocassionListviewLayout.setVisibility(View.VISIBLE);
			ocassionListview.setAdapter(new OccassionAdapter(getActivity(),
					mOccassionAisleKeywordsList));
		}
		occassionBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		occasionText.requestFocus();
		inputMethodManager.showSoftInput(occasionText, 0);
	}

	private void categoryIconClickFunctionality() {
		categoryListview.setVisibility(View.VISIBLE);
		categoryListview.setAdapter(new CategoryAdapter(getActivity()));
		categoryListviewLayout.setVisibility(View.VISIBLE);
		categoeryPopup.setVisibility(View.VISIBLE);
	}

	// LookingFor....
	private class LookingForAdapter extends BaseAdapter {
		Activity context;
		ArrayList<String> lookingForKeywordsList = null;

		public LookingForAdapter(Activity context,
				ArrayList<String> lookingForKeywordsList) {
			super();
			this.context = context;
			this.lookingForKeywordsList = lookingForKeywordsList;
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
			try {
				if (lookingForKeywordsList.get(position).equals(
						lookingForText.getText().toString())) {
					holder.dataentryitemname.setTextColor(getResources()
							.getColor(R.color.black));
					holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
				} else {
					holder.dataentryitemname.setTextColor(getResources()
							.getColor(R.color.dataentrytextcolor));
					holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
				}
				holder.dataentryitemname.setText(lookingForKeywordsList
						.get(position));
				rowView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						lookingForBigText.setText(lookingForKeywordsList
								.get(position));
						lookingForText.setText(lookingForKeywordsList
								.get(position));
					}
				});
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return rowView;
		}

		@Override
		public int getCount() {
			try {
				return lookingForKeywordsList.size();
			} catch (Exception e) {
				return 0;
			}
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

	// Occassion....
	private class OccassionAdapter extends BaseAdapter {
		Activity context;
		ArrayList<String> occassionKeywordsList = null;

		public OccassionAdapter(Activity context,
				ArrayList<String> occassionKeywordsList) {
			super();
			this.context = context;
			this.occassionKeywordsList = occassionKeywordsList;
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
			try {
				if (occassionKeywordsList.get(position).equals(
						occasionText.getText().toString())) {
					holder.dataentryitemname.setTextColor(getResources()
							.getColor(R.color.black));
					holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
				} else {
					holder.dataentryitemname.setTextColor(getResources()
							.getColor(R.color.dataentrytextcolor));
					holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
				}
				holder.dataentryitemname.setText(occassionKeywordsList
						.get(position));
				rowView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						occasionText.setText(occassionKeywordsList
								.get(position));
						occassionBigText.setText(occassionKeywordsList
								.get(position));
					}
				});
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return rowView;
		}

		@Override
		public int getCount() {
			try {
				return occassionKeywordsList.size();
			} catch (Exception e) {
				return 0;
			}
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

	// Category....
	private class CategoryAdapter extends BaseAdapter {
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
			if (categoryitemsArray[position].equals(categoryText.getText()
					.toString())) {
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
		try {
			Log.e("frag1", "gallery called,,,," + picturePath);
			imagePath = picturePath;
			createaisleBg.setVisibility(View.VISIBLE);
			resizedImagePath = Utils.getResizedImage(new File(imagePath),
					screenHeight, screenWidth, getActivity());
			Log.e("Frag", resizedImagePath);
			createaisleBg.setImageURI(Uri.fromFile(new File(resizedImagePath)));
			if (addImageToAisleFlag) {
				dataEntryAislesViewpager.setVisibility(View.GONE);
				DataEntryActivity obj = (DataEntryActivity) getActivity();
				obj.getSupportActionBar().setTitle(
						getResources().getString(
								R.string.add_imae_to_aisle_screen_title));
				dataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
				dataEntryBottomTopLayout.setVisibility(View.GONE);
				DataEntryActivity activity = (DataEntryActivity) getActivity();
				activity.isNewActionBar = false;
				activity.invalidateOptionsMenu();
				mainHeadingRow.setVisibility(View.VISIBLE);
				touchToChangeImage.setVisibility(View.VISIBLE);
				occassionBigText.setBackgroundColor(Color.TRANSPARENT);
				lookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addImageToAisleToServer() {
		// Input parameters for Adding Aisle to server request...
		String category = categoryText.getText().toString();
		String lookingFor = lookingForBigText.getText().toString();
		String occasion = occassionBigText.getText().toString();
		String imageUrl = imagePath; // This path is image location stored in
										// locally when user selects from Camera
										// OR Gallery.
		String title = ""; // For Camera and Gallery we don't have title.
		String store = ""; // For Camera and Gallery we don't have store.
		storeMetaAisleDataIntoLocalStorage();
	}

	private void addAisleToServer() {
		// Input parameters for Adding Aisle to server request...
		String category = categoryText.getText().toString();
		String lookingFor = lookingForBigText.getText().toString();
		String occasion = occassionBigText.getText().toString();
		String imageUrl = imagePath; // This path is image location stored in
										// locally when user selects from Camera
										// OR Gallery.
		String title = ""; // For Camera and Gallery we don't have title.
		String store = ""; // For Camera and Gallery we don't have store.
		storeMetaAisleDataIntoLocalStorage();
	}

	private void storeMetaAisleDataIntoLocalStorage() {
		showDataProgressOnNotification();
		renderUIAfterAddingAisleToServer();
	}

	private void renderUIAfterAddingAisleToServer() {
		DataEntryActivity obj = (DataEntryActivity) getActivity();
		obj.getSupportActionBar().setTitle(
				getResources().getString(R.string.app_name));
		if (editAisleImageFlag) {
			aisleImagePathList.remove(currentPagePosition);
		}
		editAisleImageFlag = false;
		DataEntryActivity activity = (DataEntryActivity) getActivity();
		activity.isNewActionBar = true;
		activity.invalidateOptionsMenu();
		mainHeadingRow.setVisibility(View.GONE);
		touchToChangeImage.setVisibility(View.GONE);
		dataEntryBottomBottomLayout.setVisibility(View.GONE);
		dataEntryBottomTopLayout.setVisibility(View.VISIBLE);
		aisleImagePathList.add(0, resizedImagePath);
		dataEntryAislesViewpager.setVisibility(View.VISIBLE);
		createaisleBg.setVisibility(View.GONE);
		dataEntryAislesViewpager.setAdapter(new DataEntryAilsePagerAdapter(
				getActivity(), aisleImagePathList));
	}

/*	private void addAisleMetaDataToDB(String tableName, AisleData mAisleData) {
		Uri uri = null;
		if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
			uri = VueConstants.LOOKING_FOR_CONTENT_URI;
		} else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
			uri = VueConstants.OCCASION_CONTENT_URI;
		} else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
			uri = VueConstants.CATEGORY_CONTENT_URI;
		} else {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(VueConstants.KEYWORD, mAisleData.keyword);
		values.put(VueConstants.LAST_USED_TIME, Utils.date());
		values.put(VueConstants.NUMBER_OF_TIMES_USED, mAisleData.count);
		if (mAisleData.isNew) {
			getActivity().getContentResolver().insert(uri, values);
		} else {
			getActivity().getContentResolver().update(uri, values,
					VueConstants.KEYWORD + "=?",
					new String[] { mAisleData.keyword });
		}
	}*/

	private ArrayList<String> getAisleKeywords(String tableName) {
		ArrayList<String> aisleKeywordsList = null;
		Uri uri = null;
		if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
			uri = VueConstants.LOOKING_FOR_CONTENT_URI;
		} else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
			uri = VueConstants.OCCASION_CONTENT_URI;
		} else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
			uri = VueConstants.CATEGORY_CONTENT_URI;
		} else {
			return null;
		}
		String twoWeeksBeforeTime = Utils.twoWeeksBeforeTime();
		Cursor c = getActivity().getContentResolver().query(uri, null,
				VueConstants.LAST_USED_TIME + " >?",
				new String[] { twoWeeksBeforeTime },
				VueConstants.NUMBER_OF_TIMES_USED + " DESC");
		if (c.moveToFirst()) {
			aisleKeywordsList = new ArrayList<String>();
			do {
				aisleKeywordsList.add(c.getString(c
						.getColumnIndex(VueConstants.KEYWORD)));
			} while (c.moveToNext());
		}
		c.close();
		Cursor c1 = getActivity().getContentResolver().query(uri, null,
				VueConstants.LAST_USED_TIME + " <=?",
				new String[] { twoWeeksBeforeTime },
				VueConstants.NUMBER_OF_TIMES_USED + " DESC");
		if (c1.moveToFirst()) {
			if (aisleKeywordsList == null)
				aisleKeywordsList = new ArrayList<String>();
			do {
				aisleKeywordsList.add(c1.getString(c1
						.getColumnIndex(VueConstants.KEYWORD)));
			} while (c1.moveToNext());
		}
		c1.close();
		return aisleKeywordsList;
	}

	private AisleData getAisleDataForKeyword(String keyWord, String tableName) {
		AisleData aisleDataObj = null;
		Uri uri = null;
		if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
			uri = VueConstants.LOOKING_FOR_CONTENT_URI;
		} else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
			uri = VueConstants.OCCASION_CONTENT_URI;
		} else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
			uri = VueConstants.CATEGORY_CONTENT_URI;
		} else {
			return null;
		}
		Cursor c = getActivity().getContentResolver().query(uri, null,
				VueConstants.KEYWORD + "=?", new String[] { keyWord }, null);
		if (c.moveToFirst()) {
			aisleDataObj = new AisleData();
			aisleDataObj.keyword = c.getString(c
					.getColumnIndex(VueConstants.KEYWORD));
			aisleDataObj.count = c.getInt(c
					.getColumnIndex(VueConstants.NUMBER_OF_TIMES_USED));
			aisleDataObj.time = c.getString(c
					.getColumnIndex(VueConstants.LAST_USED_TIME));
		}
		c.close();
		return aisleDataObj;
	}

	private void showDataProgressOnNotification() {
		final NotificationManager mNotifyManager = (NotificationManager) getActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Builder mBuilder = new NotificationCompat.Builder(getActivity());
		mBuilder.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(
						getResources().getString(R.string.uploading_mesg))
				.setSmallIcon(R.drawable.vue_launcher_icon);
		// Start a lengthy operation in a background thread
		new Thread(new Runnable() {
			@Override
			public void run() {

				AisleData lookingForAisleDataObj = getAisleDataForKeyword(
						lookingForBigText.getText().toString().trim(),
						VueConstants.LOOKING_FOR_TABLE);
				if (lookingForAisleDataObj != null) {
					lookingForAisleDataObj.count += 1;
					lookingForAisleDataObj.isNew = false;
				} else {
					lookingForAisleDataObj = new AisleData();
					lookingForAisleDataObj.keyword = lookingForBigText
							.getText().toString().trim();
					lookingForAisleDataObj.count = 1;
					lookingForAisleDataObj.isNew = true;
				}
				String currentTime = Utils.date();
				lookingForAisleDataObj.time = currentTime;
				addAisleMetaDataToDB(VueConstants.LOOKING_FOR_TABLE,
						lookingForAisleDataObj);
				AisleData occassionAisleDataObj = getAisleDataForKeyword(
						occassionBigText.getText().toString().trim(),
						VueConstants.OCCASION_TABLE);
				if (occassionAisleDataObj != null) {
					occassionAisleDataObj.count += 1;
					occassionAisleDataObj.isNew = false;
				} else {
					occassionAisleDataObj = new AisleData();
					occassionAisleDataObj.keyword = occassionBigText.getText()
							.toString().trim();
					occassionAisleDataObj.count = 1;
					occassionAisleDataObj.isNew = true;
				}
				occassionAisleDataObj.time = currentTime;
				addAisleMetaDataToDB(VueConstants.OCCASION_TABLE,
						occassionAisleDataObj);
				AisleData categoryAisleDataObj = getAisleDataForKeyword(
						categoryText.getText().toString().trim(),
						VueConstants.CATEGORY_TABLE);
				if (categoryAisleDataObj != null) {
					categoryAisleDataObj.count += 1;
					categoryAisleDataObj.isNew = false;
				} else {
					categoryAisleDataObj = new AisleData();
					categoryAisleDataObj.keyword = categoryText.getText()
							.toString().trim();
					categoryAisleDataObj.count = 1;
					categoryAisleDataObj.isNew = true;
				}
				categoryAisleDataObj.time = currentTime;
				addAisleMetaDataToDB(VueConstants.CATEGORY_TABLE,
						categoryAisleDataObj);

				/*
				 * int incr; // Do the "lengthy" operation 20 times for (incr =
				 * 0; incr <= 100; incr += 20) { // Sets the progress indicator
				 * to a max value, the // current completion percentage, and
				 * "determinate" // state mBuilder.setProgress(100, incr,
				 * false); mBuilder.setContentText("Uploading... (" + incr +
				 * "%)"); // Displays the progress bar for the first time.
				 * mNotifyManager.notify(0, mBuilder.build()); // Sleeps the
				 * thread, simulating an operation // that takes time try { //
				 * Sleep for 5 seconds Thread.sleep(5 * 1000); } catch
				 * (InterruptedException e) { } }
				 */

				// When the loop is finished, updates the notification
				mBuilder.setContentText("Uploading completed")
				// Removes the progress bar
						.setProgress(0, 0, false);
				mNotifyManager.notify(0, mBuilder.build());
			}
		}
		// Starts the thread by calling the run() method in its Runnable
		).start();
	}
}
