package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

/**
 * Fragment for creating Aisle
 * 
 */
public class CreateAisleFragment extends Fragment {

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
	LinearLayout mainHeadingRow = null, dataEntryBottomBottomLayout = null;
	RelativeLayout dataEntryBottomTopLayout = null,
			dataEntryInviteFriendsLayout = null,
			dataEntryInviteFriendsPopupLayout = null,
			dataEntryInviteFriendsFacebookLayout = null,
			titleBarBottomLayout = null, actionBarTopLayout = null,
			dataEntryInviteFriendsCancelLayout = null;
	public static boolean createAilseKeyboardHiddenShownFlag = false;
	ProgressDialog dataentryInviteFriendsProgressdialog = null;
	ListView dataEntryInviteFriendsList = null;
	ImageView createAisleTitleBg = null, createAiselCloseBtn = null,
			createAiselToServer = null, shareCreatedAisle = null,
			addImageToAisle = null, createAisleTitleTopBg = null;
	ShareDialog mShare = null;
	boolean addImageToAisleFlag = false;
	float screenHeight = 0, screenWidth = 0;
	private static final int AISLE_IMAGE_MARGIN = 96;

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
		createAilseKeyboardHiddenShownFlag = true;
		View v = inflater.inflate(R.layout.create_aisleview_fragment,
				container, false);
		inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		lookingForText = (EditTextBackEvent) v
				.findViewById(R.id.lookingfortext);
		createAisleTitleTopBg = (ImageView) v
				.findViewById(R.id.createaisel_title_top_bg);
		dataEntryInviteFriendsCancelLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_cancellayout);
		actionBarTopLayout = (RelativeLayout) v
				.findViewById(R.id.actionbar_top_layout);
		createAiselToServer = (ImageView) v
				.findViewById(R.id.createaisel_to_server);
		addImageToAisle = (ImageView) v.findViewById(R.id.add_image_to_aisle);
		createAiselCloseBtn = (ImageView) v
				.findViewById(R.id.createaisel_close_btn);
		createAisleTitleBg = (ImageView) v
				.findViewById(R.id.createaisel_title_bg);
		titleBarBottomLayout = (RelativeLayout) v
				.findViewById(R.id.titlebarbottomlayout);
		dataEntryBottomBottomLayout = (LinearLayout) v
				.findViewById(R.id.dataentry_bottom_bottom_layout);
		shareCreatedAisle = (ImageView) v
				.findViewById(R.id.share_created_aisle);
		lookingForBigText = (TextView) v.findViewById(R.id.lookingforbigtext);
		lookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		dataEntryInviteFriendsList = (ListView) v
				.findViewById(R.id.data_entry_Invitefriends_list);
		dataEntryBottomTopLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_bottom_top_layout);
		dataEntryInviteFriendsFacebookLayout = (RelativeLayout) v
				.findViewById(R.id.dataentry_invitefriends_facebooklayout);
		dataEntryInviteFriendsPopupLayout = (RelativeLayout) v
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
			public void onKeyBackPressed() {
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
			public void onKeyBackPressed() {
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
			public void onKeyBackPressed() {
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
						// getFriendsList(getActivity().getResources().getString(R.string.sidemenu_sub_option_Facebook));
					}
				});

		createAisleTitleBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CreateAisleActivity activity = (CreateAisleActivity) getActivity();
				activity.showBezelMenu();
			}
		});
		createAisleTitleTopBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CreateAisleActivity activity = (CreateAisleActivity) getActivity();
				activity.showBezelMenu();
			}
		});
		createAiselCloseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CreateAisleActivity activity = (CreateAisleActivity) getActivity();
				activity.finishActivity();
			}
		});
		createAiselToServer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAisleToServer();
			}
		});
		shareCreatedAisle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mShare = new ShareDialog(getActivity(), getActivity());
				ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
				clsShare shareObj = new clsShare(null, imagePath);
				imageUrlList.add(shareObj);
				mShare.share(imageUrlList, "", "");
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
		addImageToAisle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addImageToAisleFlag = true;
				Intent intent = new Intent(getActivity(),
						CreateAisleSelectionActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
				intent.putExtras(b);
				getActivity().startActivityForResult(intent,
						VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
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
		try {
			imagePath = picturePath;
			createaisleBg.setImageBitmap(Utils.getResizedImage(new File(
					imagePath), screenHeight, screenWidth));
			if (addImageToAisleFlag) {
				addImageToAisleToServer();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addImageToAisleToServer() {
		String imageUrl = imagePath; // This path is image location stored in
		// locally when user selects from Camera
		// OR Gallery.
	}

	public void addAisleToServer() {
		// hiding keyboard
		inputMethodManager.hideSoftInputFromWindow(
				saySomethingAboutAisle.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(
				occasionText.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(
				lookingForText.getWindowToken(), 0);
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
		actionBarTopLayout.setVisibility(View.VISIBLE);
		titleBarBottomLayout.setVisibility(View.GONE);
		mainHeadingRow.setVisibility(View.GONE);
		touchToChangeImage.setVisibility(View.GONE);
		dataEntryBottomBottomLayout.setVisibility(View.GONE);
		lookingForPopup.setVisibility(View.GONE);
		ocassionPopup.setVisibility(View.GONE);
		categoeryPopup.setVisibility(View.GONE);
		categoryListviewLayout.setVisibility(View.GONE);
		dataEntryBottomTopLayout.setVisibility(View.VISIBLE);
	}
}
