package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.NetworkHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Logging;
import com.lateralthoughts.vue.utils.SortBasedOnName;
import com.lateralthoughts.vue.utils.Utils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class VueListFragment extends SherlockFragment implements TextWatcher/* Fragment */{

	public static final String TAG = "VueListFragment";
	private ExpandableListView expandListView;
	private LinearLayout customlayout, aboutlayout, invitefriendsLayout;
	private RelativeLayout mBezelMainLayout, donelayout, aboutdonelayout,
			vue_list_fragment_invite_friendsLayout_mainxml;
	private ImageView userProfilePic;
	private EditText userNameEdit, userDOBEdit, userGenderEdit, userEmailEdit,
			userLocationEdit, mSideMenuSearchBar;
	private Animation animDown;
	private Animation animUp;
	private ListView inviteFrirendsListView;
	public FriendsListener listener;
	private SharedPreferences sharedPreferencesObj;
	private ProgressDialog progress;
	private LayoutInflater inflater;
	private boolean isProfileEdited = false;
	boolean isNewUser = false;
	

	private String profilePicUrl = "";

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	  if (getActivity() instanceof VueLandingPageActivity) {
	    VueApplication.getInstance().landingPage = (VueLandingPageActivity) getActivity();
	  }
		this.inflater = inflater;
		sharedPreferencesObj = getActivity().getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		listener = new FriendsListener() {
			@Override
			public boolean onBackPressed() {
				boolean returnWhat = false;
				if (invitefriendsLayout != null
						&& invitefriendsLayout.getVisibility() == View.VISIBLE) {
					invitefriendsLayout.setVisibility(View.GONE);
					expandListView.setVisibility(View.VISIBLE);
					returnWhat = true;
				}
				if (aboutlayout != null
						&& aboutlayout.getVisibility() == View.VISIBLE) {
					aboutlayout.setVisibility(View.GONE);
					aboutlayout.startAnimation(animDown);
					expandListView.setVisibility(View.VISIBLE);
					returnWhat = true;
				}
				if (customlayout != null
						&& customlayout.getVisibility() == View.VISIBLE) {
					customlayout.setVisibility(View.GONE);
					customlayout.startAnimation(animDown);
					expandListView.setVisibility(View.VISIBLE);
					returnWhat = true;
				}
				return returnWhat;
			}
		};
		return inflater.inflate(R.layout.vue_list_fragment, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSideMenuSearchBar = (EditText) getActivity().findViewById(
				R.id.side_Menu_searchBar);
		mBezelMainLayout = (RelativeLayout) getActivity().findViewById(
				R.id.bezel_menu_main_layout);
		vue_list_fragment_invite_friendsLayout_mainxml = (RelativeLayout) getActivity()
				.findViewById(
						R.id.vue_list_fragment_invite_friendsLayout_mainxml);
		expandListView = (ExpandableListView) getActivity().findViewById(
				R.id.vue_list_fragment_list);

		final VueListFragmentAdapter adapter = new VueListFragmentAdapter(
				getActivity(), getBezelMenuOptionItems());
		expandListView.setAdapter(adapter);
		animDown = AnimationUtils
				.loadAnimation(getActivity(), R.anim.anim_down);
		animUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_up);
		/**/
		mSideMenuSearchBar.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					VueTrendingAislesDataModel.getInstance(getActivity())
							.clearAisles();
					if (getActivity() instanceof SlidingFragmentActivity) {
						SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
						activity.getSlidingMenu().toggle();
					}
					String s = mSideMenuSearchBar.getText().toString().trim();
					if (s.isEmpty()) {
						return false;
					}
					VueTrendingAislesDataModel.getInstance(getActivity())
							.getNetworkHandler().requestSearch(s);
					// TODO:close the keyboard here
					final InputMethodManager mInputMethodManager = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					mInputMethodManager.hideSoftInputFromWindow(
							mSideMenuSearchBar.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

    expandListView.setOnGroupClickListener(new OnGroupClickListener() {


      @Override
      public boolean onGroupClick(ExpandableListView parent, View v,
          int groupPosition, long id) {
        if (VueLandingPageActivity.mOtherSourceImagePath == null) {
          TextView textView = (TextView) v
              .findViewById(R.id.vue_list_fragment_itemTextview);
          String s = textView.getText().toString();
          FlurryAgent.logEvent(s);
          if (s.equals(getString(R.string.sidemenu_option_Trending_Aisles))) {
            VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = true;
            VueLandingPageActivity vueLandingPageActivity1 = (VueLandingPageActivity) getActivity();
            vueLandingPageActivity1.showCategory(s);
            if (getActivity() instanceof SlidingFragmentActivity) {
              SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
              activity.getSlidingMenu().toggle();
              if (getActivity() instanceof VueLandingPageActivity) {
                VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
                vueLandingPageActivity.mVueLandingActionbarScreenName
                    .setText(getString(R.string.trending));
              }
              if (getActivity() instanceof AisleDetailsViewActivity) {
                startActivity(new Intent(
                    (AisleDetailsViewActivity) getActivity(),
                    VueLandingPageActivity.class));
              } else if (getActivity() instanceof DataEntryActivity) {
                startActivity(new Intent((DataEntryActivity) getActivity(),
                    VueLandingPageActivity.class));
              }
            }

          } else if (s.equals(getString(R.string.sidemenu_option_About))) {
            inflateAboutLayout();
          } else if (s.equals(getString(R.string.sidemenu_option_FeedBack))) {
            startActivity(new Intent(getActivity(), FeedbackForm.class));
          } else if (s.equals(getString(R.string.sidemeun_option_Settings))) {
            inflateSettingsLayout();
          } else if (s
              .equals(getString(R.string.sidemenu_option_Invite_Friends))) {
            View layoutInviewFriends = inflater.inflate(R.layout.invite, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            vue_list_fragment_invite_friendsLayout_mainxml
                .addView(layoutInviewFriends);
            invitefriendsLayout = (LinearLayout) layoutInviewFriends
                .findViewById(R.id.vue_list_fragment_invite_friendsLayout);
            invitefriendsLayout.setLayoutParams(params);
            inviteFrirendsListView = (ListView) layoutInviewFriends
                .findViewById(R.id.vue_list_fragment_Invitefriends_list);
            inviteFrirendsListView
                .setOnItemClickListener(new OnItemClickListener() {

                  @Override
                  public void onItemClick(AdapterView<?> parent, View view,
                      int position, long id) {

                    // TODO:
                  }
                });
          } else if (s.equals(getString(R.string.sidemenu_option_Login))) {
            FlurryAgent.logEvent("Login_Without_Prompt");
            sharedPreferencesObj = getActivity().getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
            boolean fbloginfalg = sharedPreferencesObj.getBoolean(
                VueConstants.FACEBOOK_LOGIN, false);
            boolean googleplusloginfalg = sharedPreferencesObj.getBoolean(
                VueConstants.GOOGLEPLUS_LOGIN, false);
            if (!googleplusloginfalg || !fbloginfalg) {

              Intent i = new Intent(getActivity(), VueLoginActivity.class);
              Bundle b = new Bundle();
              b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
              b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
              b.putString(VueConstants.FROM_INVITEFRIENDS, null);
              b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, true);
              i.putExtras(b);
              startActivity(i);
            } else {
              Toast.makeText(
                  getActivity(),
                  getActivity().getResources().getString(R.string.already_logged_in_msg), Toast.LENGTH_LONG)
                  .show();
            }
          }

          return false;
        } else {
          try {
            VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
            vueLandingPageActivity.showDiscardOtherAppImageDialog();
          } catch (Exception e) {
            e.printStackTrace();
          }
          return false;
        }
      }
    });


    expandListView.setOnChildClickListener(new OnChildClickListener() {

      @Override
      public boolean onChildClick(ExpandableListView parent, View v,
          int groupPosition, int childPosition, long id) {

        // Added sidemenu_sub_option_My_Aisles functionality
        TextView textView = (TextView) v.findViewById(R.id.child_itemTextview);
        String s = textView.getText().toString();
        Logging.i(TAG, "Child Click: Name of item: " + s);
        if (s.equals(getString(R.string.sidemenu_sub_option_My_Aisles))) {
        	Logging.i("clicked on", "clicked on: "+s);
          VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = false;

          if (getActivity() instanceof SlidingFragmentActivity) {
            SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
            activity.getSlidingMenu().toggle();
           // if (getActivity() instanceof VueLandingPageActivity) {
            	//VueLandingPageActivity vueLandingPageActivity1 = (VueLandingPageActivity) getActivity();
            activity.startActivity(new Intent(getActivity(), VueLandingPageActivity.class));
            VueApplication.getInstance().landingPage.showCategory(s);
           // }
            if (getActivity() instanceof AisleDetailsViewActivity) {
              startActivity(new Intent(
                  (AisleDetailsViewActivity) getActivity(),
                  VueLandingPageActivity.class));
            } else if (getActivity() instanceof DataEntryActivity) {
              startActivity(new Intent((DataEntryActivity) getActivity(),
                  VueLandingPageActivity.class));
            }
          }
              return true;
        } else if (s.equals(getString(R.string.sidemenu_sub_option_Bookmarks))) {
        	  if (getActivity() instanceof SlidingFragmentActivity) {
        	   // if (getActivity() instanceof VueLandingPageActivity) {
        	    SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
        	    activity.startActivity(new Intent(getActivity(), VueLandingPageActivity.class));
        	    /*((VueLandingPageActivity) getActivity())*/VueApplication.getInstance().landingPage.showCategory(s);
        	   // } else {
        	      //TODO: Need to implement in all the screens.
        	   // }
                  activity.getSlidingMenu().toggle();
        	  }
        	return true;
        } else if (s.equals(getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles))) {
          Logging.i("clicked on", "clicked on: "+s);
          if (getActivity() instanceof SlidingFragmentActivity) {
          //  if (getActivity() instanceof VueLandingPageActivity) {
            SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
            activity.startActivity(new Intent(getActivity(), VueLandingPageActivity.class));
              /*((VueLandingPageActivity) getActivity())*/VueApplication.getInstance().landingPage.showCategory(s);              
            //} else {
              //TODO: Need to implement in all the screens.
           // }
             // SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
              activity.getSlidingMenu().toggle();
    	  }
          return true;
        } else if(s.trim().equals(getString(R.string.sidemenu_sub_option_Interactions))) {
        	//TODO: need to implement interactions option
        	Logging.i("clicked on", "clicked on: "+s);
        	return true;
        }
        Logging.i("clicked on", "clicked on:2222 "+s);

        if (VueLandingPageActivity.mOtherSourceImagePath == null) {
          if (s.equals(getString(R.string.sidemenu_option_Profile))) {
            FlurryAgent.logEvent("Settings_" + s);
            getUserInfo();
          } else if (s.equals(getString(R.string.sidemenu_sub_option_Facebook))
              || s.equals(getString(R.string.sidemenu_sub_option_Googleplus))) {
            FlurryAgent.logEvent("InviteFriends_" + s);
            getFriendsList(s);
          } else {
            TextView categoryText = (TextView) v
                .findViewById(R.id.child_itemTextview);
            String cat = categoryText.getText().toString();
            if (getActivity() instanceof SlidingFragmentActivity) {
              SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
              activity.getSlidingMenu().toggle();
              if (getActivity() instanceof VueLandingPageActivity) {
                /*
                 * VueTrendingAislesDataModel model = VueTrendingAislesDataModel
                 * .getInstance(getActivity()); model.clearAisles();
                 */
                AisleWindowContentFactory.getInstance(getActivity())
                    .clearObjectsInUse();
                VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
                vueLandingPageActivity.showCategory(cat);
              }

              /*
               * ScaledImageViewFactory mImageViewFactory =
               * ScaledImageViewFactory .getInstance(getActivity());
               * mImageViewFactory.clearAllImageViews();
               */

              if (getActivity() instanceof AisleDetailsViewActivity) {
                startActivity(new Intent(
                    (AisleDetailsViewActivity) getActivity(),
                    VueLandingPageActivity.class));
              } else if (getActivity() instanceof DataEntryActivity) {
                startActivity(new Intent((DataEntryActivity) getActivity(),
                    VueLandingPageActivity.class));
              }
            }
          }
          return false;
        } else {
          try {
            VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
            vueLandingPageActivity.showDiscardOtherAppImageDialog();
          } catch (Exception e) {
            e.printStackTrace();
          }
          return false;
        }
      }
    });
	}

	/**
	 * It will prepare and return a list of options to show in side menu in
	 * bezel.
	 * 
	 * @return List<ListOptionItem> each ListOptionItem contains item name to
	 *         display on screen, image to display and list of sub options.
	 * */
	private List<ListOptionItem> getBezelMenuOptionItems() {
		List<ListOptionItem> groups = new ArrayList<VueListFragment.ListOptionItem>();
		ListOptionItem item = new ListOptionItem(
				getString(R.string.sidemenu_option_Trending_Aisles),
				R.drawable.profile, null);
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_option_Me),
				R.drawable.profile, getMeChildren());
		groups.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_option_Categories),
				R.drawable.category, getCategoriesChildren());
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemeun_option_Settings),
				R.drawable.settings01, getSettingsChildren());
		groups.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_option_Invite_Friends),
				R.drawable.invite, getInviteFriendsChildren());
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_option_Help),
				R.drawable.help, null);
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_option_About),
				R.drawable.vue_launcher_icon, null);
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_option_FeedBack),
				R.drawable.vue_launcher_icon, null);
		groups.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_option_Login),
				R.drawable.vue_launcher_icon, null);
		groups.add(item);
		return groups;
	}

	/**
	 * It will prepare and return a list of options to show in the Categories
	 * section of side menu in bezel.
	 * 
	 * @return List<ListOptionItem> each ListOptionItem contains item name to
	 *         display on screen, image to diaplay, there will be not sub
	 *         options.
	 * */
	private List<ListOptionItem> getCategoriesChildren() {
		List<ListOptionItem> categoriesChildren = new ArrayList<VueListFragment.ListOptionItem>();
		ListOptionItem item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Apparel),
				R.drawable.comment, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Beauty),
				R.drawable.comment, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Electronics),
				R.drawable.comment, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Entertainment),
				R.drawable.vue_launcher_icon, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Events),
				R.drawable.comment, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Food),
				R.drawable.comment, null);
		categoriesChildren.add(item);
		item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Home),
				R.drawable.comment, null);
		categoriesChildren.add(item);

		return categoriesChildren;
	}

	private List<ListOptionItem> getInviteFriendsChildren() {
		List<ListOptionItem> inviteFriendsChildren = new ArrayList<VueListFragment.ListOptionItem>();
		ListOptionItem item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Facebook),
				R.drawable.comment, null);
		inviteFriendsChildren.add(item);
		/*
		 * item = new ListOptionItem(
		 * getString(R.string.sidemenu_sub_option_Twitter), R.drawable.comment,
		 * null); inviteFriendsChildren.add(item);
		 */
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Googleplus),
				R.drawable.comment, null);
		inviteFriendsChildren.add(item);
		return inviteFriendsChildren;
	}

	private List<ListOptionItem> getSettingsChildren() {
		List<ListOptionItem> settingsChildren = new ArrayList<VueListFragment.ListOptionItem>();
		ListOptionItem item = new ListOptionItem(
				getString(R.string.sidemenu_option_Profile),
				R.drawable.profile, null);
		settingsChildren.add(item);
		return settingsChildren;
	}

	private List<ListOptionItem> getMeChildren() {
		List<ListOptionItem> meChildren = new ArrayList<VueListFragment.ListOptionItem>();
		ListOptionItem item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_My_Aisles),
				R.drawable.profile, null);
		meChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Interactions),
				R.drawable.profile, null);
		meChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Bookmarks),
				R.drawable.profile, null);
		meChildren.add(item);
		item = new ListOptionItem(
				getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles),
				R.drawable.profile, null);
		meChildren.add(item);
		return meChildren;
	}

	/***/
	private class ListOptionItem {
		public String tag;
		public int iconRes;
		List<ListOptionItem> children;

		public ListOptionItem(String tag, int iconRes,
				List<ListOptionItem> children) {
			this.tag = tag;
			this.iconRes = iconRes;
			this.children = children;
		}
	}

	/***/
	private static class Holder {
		public ImageView icon;
		public TextView itemName;

	}

	// vue_list_fragment_list
	private class VueListFragmentAdapter extends BaseExpandableListAdapter {

		public List<ListOptionItem> groups;

		public VueListFragmentAdapter(Context context,
				List<ListOptionItem> groups) {
			this.groups = groups;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			if (groups.get(groupPosition).tag.equals("Categories")) {
				return groups.get(groupPosition).children.get(childPosition);
			}
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.child, null);
				holder = new Holder();
				holder.icon = (ImageView) convertView
						.findViewById(R.id.child_iconImageview);
				holder.itemName = (TextView) convertView
						.findViewById(R.id.child_itemTextview);

				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.icon.setImageResource(groups.get(groupPosition).children.get(childPosition).iconRes);
			// (This line of code will be used to set the icons for children at
			// below line when get all the icons).
			//holder.icon.setImageResource(R.drawable.vue_launcher_icon);
			holder.itemName.setText(groups.get(groupPosition).children
					.get(childPosition).tag);
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (groups.get(groupPosition).tag.equals("Categories")
					|| (groups.get(groupPosition).tag
							.equals(getString(R.string.sidemenu_option_Invite_Friends)))
					|| groups.get(groupPosition).tag.equals("Settings")
					|| groups.get(groupPosition).tag
							.equals(getString(R.string.sidemenu_option_Me))) {
				return groups.get(groupPosition).children.size();
			}
			return 0;
		}

		@Override
		public Object getGroup(int groupPosition) {

			return groups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {

			return groups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.vue_list_fragment_row, null);
				holder = new Holder();
				holder.icon = (ImageView) convertView
						.findViewById(R.id.vue_list_fragment_iconImageview);
				holder.itemName = (TextView) convertView
						.findViewById(R.id.vue_list_fragment_itemTextview);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			/*
			 * if(groups.get(groupPosition).tag.equals("Categories")) {
			 * expandListView.expandGroup(groupPosition); }
			 */
			holder.icon.setImageResource(groups.get(groupPosition).iconRes);
			holder.itemName.setText(groups.get(groupPosition).tag);
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}

	}

	private int getScreenWidth() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		return displaymetrics.widthPixels;
	}

	public interface FriendsListener {

		public boolean onBackPressed();
	}

	public void getFriendsList(String s) {

		progress = ProgressDialog.show(getActivity(), "", "Please wait...");
		sharedPreferencesObj = getActivity().getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean facebookloginflag = sharedPreferencesObj.getBoolean(
				VueConstants.FACEBOOK_LOGIN, false);
		boolean googleplusloginflag = sharedPreferencesObj.getBoolean(
				VueConstants.GOOGLEPLUS_LOGIN, false);
		if (s.equals(getResources().getString(
				R.string.sidemenu_sub_option_Facebook))) {

			if (facebookloginflag) {
				fbFriendsList();
			} else {
				if (progress.isShowing()) {
					progress.dismiss();
				}
				Intent i = new Intent(getActivity(), VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.FACEBOOK);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				getActivity().startActivityForResult(i,
						VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
			}

		} else if (s.equals(getResources().getString(
				R.string.sidemenu_sub_option_Googleplus))) {
			if (googleplusloginflag) {
				Logging.d(getTag(), "GOOGLEPLUS : Value of s : 1");
				getGPlusFriendsList();
			} else {
				if (progress.isShowing()) {
					progress.dismiss();
				}
				Logging.d(getTag(), "GOOGLEPLUS : Value of s : 2");
				Logging.d(getTag(), "GOOGLEPLUS : Value of s : 3");
				Intent i = new Intent(getActivity(), VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS,
						VueConstants.GOOGLEPLUS);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				getActivity().startActivityForResult(i,
						VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
			}

		} else {
			if (progress.isShowing()) {
				progress.dismiss();
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
					List<FbGPlusDetails> fbGPlusFriends;
					try {
						fbGPlusFriends = JsonParsing(response);
						if (fbGPlusFriends != null) {
							inviteFrirendsListView
									.setAdapter(new InviteFriendsAdapter(
											getActivity(), fbGPlusFriends,
											false));

							expandListView.setVisibility(View.GONE);
							invitefriendsLayout.setVisibility(View.VISIBLE);
							invitefriendsLayout.startAnimation(animUp);
						} else {
							Toast.makeText(
									getActivity(),
									getResources().getString(
											R.string.fb_no_friends),
									Toast.LENGTH_LONG).show();
						}
						if (progress.isShowing()) {
							progress.dismiss();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if (progress.isShowing()) {
							progress.dismiss();
						}
					}
				}
			};

			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Logging.d("VueNetworkError",
							"Vue encountered network operations error. Error = "
									+ error.networkResponse);
					if (progress.isShowing()) {
						progress.dismiss();
					}
				}
			};

			StringRequest myReq = new StringRequest(Method.GET, mainURL,
					listener, errorListener);

			/*
			 * JsonArrayRequest vueRequest = new JsonArrayRequest(mainURL,
			 * listener, errorListener );
			 */

			VueApplication.getInstance().getRequestQueue().add(myReq);

		} else {
			if (progress.isShowing()) {
				progress.dismiss();
			}
		}
	}

	// Pull and display G+ friends from plus.google.com.
	private void getGPlusFriendsList() {
		if (VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
			inviteFrirendsListView
					.setAdapter(new InviteFriendsAdapter(
							getActivity(),
							VueLandingPageActivity.mGooglePlusFriendsDetailsList,
							false));
			expandListView.setVisibility(View.GONE);
			invitefriendsLayout.setVisibility(View.VISIBLE);
			invitefriendsLayout.startAnimation(animUp);
			if (progress.isShowing()) {
				progress.dismiss();
			}
		} else {
			if (progress.isShowing()) {
				progress.dismiss();
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
			getActivity().startActivityForResult(i,
					VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE);
		}
	}

	private void getUserInfo() {
		expandListView.setVisibility(View.GONE);
		customlayout.startAnimation(animUp);
		customlayout.setVisibility(View.VISIBLE);

		VueUserProfile vueUserProfile = null;
		try {
			vueUserProfile = Utils.readUserProfileObjectFromFile(getActivity(),
					VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (vueUserProfile != null) {
			isNewUser = true;
			profilePicUrl = vueUserProfile.getUserProfilePicture();
			userNameEdit.setText(vueUserProfile.getUserName());
			userDOBEdit.setText(vueUserProfile.getUserDOB());
			userGenderEdit.setText(vueUserProfile.getUserGender());
			userEmailEdit.setText(vueUserProfile.getUserEmail());
			userLocationEdit.setText(vueUserProfile.getUserLocation());
			if (!userEmailEdit.getText().toString().isEmpty()) {
				userEmailEdit.setEnabled(false);
			}
		}

		if (profilePicUrl != null) {
			File f = new FileCache(getActivity())
					.getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME);
			if (f.exists()) {
				userProfilePic.setImageURI(Uri.fromFile(f));
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

		if (facebookFriendsDetailsList != null) {
			Collections.sort(facebookFriendsDetailsList, new SortBasedOnName());
		}

		return facebookFriendsDetailsList;
	}

	private void inflateSettingsLayout() {
		View layoutSettings = null;
		if (customlayout == null) {
			layoutSettings = inflater.inflate(R.layout.settings_layout, null);
			LinearLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			customlayout = (LinearLayout) layoutSettings
					.findViewById(R.id.customlayout);
			customlayout.setLayoutParams(params);
			userProfilePic = (ImageView) layoutSettings
					.findViewById(R.id.user_profilePic);
			// userName = (TextView)
			// layoutSettings.findViewById(R.id.user_name);
			// userDateOfBirth = (TextView)
			// layoutSettings.findViewById(R.id.user_date_Of_birth);
			// userGender = (TextView)
			// layoutSettings.findViewById(R.id.user_gender);
			// userEmail = (TextView)
			// layoutSettings.findViewById(R.id.user_Email);
			// userCurrentLocation = (TextView)
			// layoutSettings.findViewById(R.id.user_current_location);

			userNameEdit = (EditText) layoutSettings
					.findViewById(R.id.user_name_EditText);
			userDOBEdit = (EditText) layoutSettings
					.findViewById(R.id.user_DOB_EditText);
			userGenderEdit = (EditText) layoutSettings
					.findViewById(R.id.user_Gender_EditText);
			userEmailEdit = (EditText) layoutSettings
					.findViewById(R.id.user_Email_EditText);
			userLocationEdit = (EditText) layoutSettings
					.findViewById(R.id.user_location_EditText);

			userDOBEdit.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View view, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						dataPicker();
					}
					return false;
				}
			});

			/*
			 * userDOBEdit.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { dataPicker(); } });
			 */

			userNameEdit.addTextChangedListener(this);
			userDOBEdit.addTextChangedListener(this);
			userGenderEdit.addTextChangedListener(this);
			userEmailEdit.addTextChangedListener(this);
			userLocationEdit.addTextChangedListener(this);
			donelayout = (RelativeLayout) layoutSettings
					.findViewById(R.id.donelayout);
			donelayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Utils.saveNetworkSettings(getActivity(),
					// wifich.isChecked());
					Logging.i("Profiling", "Profiling User Profile onClick");
					if (userNameEdit.getText().toString().isEmpty()) {
						Toast.makeText(getActivity(),
								"User name cannot be blank", Toast.LENGTH_LONG)
								.show();
						return;
					}
					customlayout.setVisibility(View.GONE);
					customlayout.startAnimation(animDown);
					expandListView.setVisibility(View.VISIBLE);
					if (isProfileEdited || isNewUser) {
						Logging.i("Profiling",
								"Profiling User Profile onClick isProfileEdited : "
										+ isProfileEdited);
						userDOBEdit.getText().toString();
						userGenderEdit.getText().toString();
						userEmailEdit.getText().toString();
						userLocationEdit.getText().toString();
						userEmailEdit.getText().toString();
						try {
							VueUserProfile vueUserProfile = new VueUserProfile(
									profilePicUrl, userEmailEdit.getText()
											.toString(), userNameEdit.getText()
											.toString(), userDOBEdit.getText()
											.toString(), userGenderEdit
											.getText().toString(),
									userLocationEdit.getText().toString(), true);
							Utils.writeUserProfileObjectToFile(
									getActivity(),
									VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME,
									vueUserProfile);
						} catch (Exception e) {
							e.printStackTrace();
						}
						isProfileEdited = false;
						isNewUser = false;
					}
				}
			});
			mBezelMainLayout.addView(layoutSettings);
		}
		customlayout.setVisibility(View.GONE);

	}

	private void inflateAboutLayout() {
		View aboutLayoutView = null;
		if (aboutlayout == null) {
			aboutLayoutView = inflater.inflate(R.layout.about, null);
			LinearLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			LayoutParams params2 = new LayoutParams(
					(int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, getScreenWidth() / 2,
							getActivity().getResources().getDisplayMetrics()),
					(int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 48, getActivity()
									.getResources().getDisplayMetrics()));
			params2.gravity = Gravity.CENTER_HORIZONTAL;
			aboutlayout = (LinearLayout) aboutLayoutView
					.findViewById(R.id.aboutlayout);
			aboutlayout.setLayoutParams(params);
			aboutdonelayout = (RelativeLayout) aboutLayoutView
					.findViewById(R.id.aboutdonelayout);
			aboutdonelayout.setLayoutParams(params);
			aboutdonelayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					expandListView.setVisibility(View.VISIBLE);
					aboutlayout.setVisibility(View.GONE);
					aboutlayout.startAnimation(animDown);
				}
			});
			mBezelMainLayout.addView(aboutLayoutView);
		}
		expandListView.setVisibility(View.GONE);
		aboutlayout.setVisibility(View.VISIBLE);
		aboutlayout.startAnimation(animUp);
	}

	@Override
	public void afterTextChanged(Editable s) {
		Logging.i("Profiling",
				"Profiling User Profile onClick afterTextChanged 1 : "
						+ isProfileEdited);
		if (!isProfileEdited)
			isProfileEdited = true;
		Logging.i("Profiling",
				"Profiling User Profile onClick afterTextChanged 2 : "
						+ isProfileEdited);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			String y = Integer.toString(year);
			String m = Integer.toString(monthOfYear);
			String d = Integer.toString(dayOfMonth);
			userDOBEdit.setText(y + "/" + m + "/" + d);
		}
	};

	// private boolean isDataPickerOpen = false;
	private void dataPicker() {
		// if(!isDataPickerOpen) {
		// isProfileEdited = true;
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog DPD = new DatePickerDialog(getActivity(),
				mDateSetListener, mYear, mMonth, mDay);
		DPD.show();
		// isDataPickerOpen = true;
		// }
	}

}
