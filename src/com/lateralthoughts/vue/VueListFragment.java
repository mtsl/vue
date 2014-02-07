package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.SortBasedOnName;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

@SuppressLint("ValidFragment")
public class VueListFragment extends Fragment implements TextWatcher {
    
    public static final String TAG = "VueListFragment";
    private ExpandableListView expandListView;
    private LinearLayout mCustomlayout, mAboutLayout, mInviteFriendsLayout;
    private RelativeLayout mBezelMainLayout, mDoneLayout, mAboutDoneLayout,
            vue_list_fragment_invite_friendsLayout_mainxml;
    private ImageView mUserProfilePic;
    private EditText mUserNameEdit, mUuserDobEdit, mUserGenderEdit,
            mUserEmailEdit, mUserLocationEdit, mSideMenuSearchBar;
    private Animation mAnimDown;
    private Animation mAnimUp;
    private ListView mInviteFrirendsListView;
    public FriendsListener listener;
    private SharedPreferences mSharedPreferencesObj;
    private ProgressDialog mProgress;
    private LayoutInflater inflater;
    private boolean mIsProfileEdited = false;
    boolean mIsNewUser = false;
    private String mProfilePicUrl = "";
    private RelativeLayout vue_list_fragment_actionbar;
    private BezelMenuRefreshReciever mBezelMenuRefreshReciever = null;
    View mView = null;
    private MixpanelAPI mixpanel;
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBezelMenuRefreshReciever != null) {
                VueApplication.getInstance().unregisterReceiver(
                        mBezelMenuRefreshReciever);
            }
        } catch (Exception e) {
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null;
    }
    
    public VueListFragment() {
        mBezelMenuRefreshReciever = new BezelMenuRefreshReciever();
        IntentFilter ifiltercategory = new IntentFilter(
                "RefreshBezelMenuReciver");
        VueApplication.getInstance().registerReceiver(
                mBezelMenuRefreshReciever, ifiltercategory);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setRetainInstance(true);
        mixpanel = mixpanel = MixpanelAPI.getInstance(getActivity(),
                VueApplication.getInstance().MIXPANEL_TOKEN);
        if (getActivity() instanceof VueLandingPageActivity) {
            VueApplication.getInstance().landingPage = (VueLandingPageActivity) getActivity();
        }
        this.inflater = inflater;
        mSharedPreferencesObj = getActivity().getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        listener = new FriendsListener() {
            @Override
            public boolean onBackPressed() {
                boolean returnWhat = false;
                if (mInviteFriendsLayout != null
                        && mInviteFriendsLayout.getVisibility() == View.VISIBLE) {
                    mInviteFriendsLayout.setVisibility(View.GONE);
                    expandListView.setVisibility(View.VISIBLE);
                    returnWhat = true;
                }
                if (mAboutLayout != null
                        && mAboutLayout.getVisibility() == View.VISIBLE) {
                    mAboutLayout.setVisibility(View.GONE);
                    mAboutLayout.startAnimation(mAnimDown);
                    expandListView.setVisibility(View.VISIBLE);
                    returnWhat = true;
                }
                if (mCustomlayout != null
                        && mCustomlayout.getVisibility() == View.VISIBLE) {
                    mCustomlayout.setVisibility(View.GONE);
                    mCustomlayout.startAnimation(mAnimDown);
                    expandListView.setVisibility(View.VISIBLE);
                    returnWhat = true;
                }
                return returnWhat;
            }
        };
        mView = inflater.inflate(R.layout.vue_list_fragment, null);
        return mView;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        vue_list_fragment_actionbar = (RelativeLayout) getActivity()
                .findViewById(R.id.vue_list_fragment_sidemenu_actionbar);
        vue_list_fragment_actionbar.setVisibility(View.GONE);
        
        final VueListFragmentAdapter adapter = new VueListFragmentAdapter(
                getActivity(), getBezelMenuOptionItems());
        expandListView.setAdapter(adapter);
        mAnimDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.anim_down);
        mAnimUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_up);
        mSideMenuSearchBar.setOnKeyListener(new OnKeyListener() {
            
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    VueTrendingAislesDataModel.getInstance(getActivity())
                            .clearAisles();
                    String s = mSideMenuSearchBar.getText().toString().trim();
                    if (s.isEmpty()) {
                        return false;
                    }
                    VueTrendingAislesDataModel.getInstance(getActivity())
                            .getNetworkHandler().requestSearch(s);
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
                if (groupPosition == 1) {
                    refreshBezelMenu();
                }
                if (VueLandingPageActivity.mOtherSourceImagePath == null) {
                    TextView textView = (TextView) v
                            .findViewById(R.id.vue_list_fragment_itemTextview);
                    String s = textView.getText().toString();
                    FlurryAgent.logEvent(s);
                    if (s.equals(getString(R.string.sidemenu_option_Trending_Aisles))) {
                        VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = true;
                        if (getActivity() instanceof VueLandingPageActivity) {
                            VueLandingPageActivity vueLandingPageActivity1 = (VueLandingPageActivity) getActivity();
                            vueLandingPageActivity1.showCategory(s, false);
                        }
                        if (getActivity() instanceof AisleDetailsViewActivity) {
                            startActivity(new Intent(
                                    (AisleDetailsViewActivity) getActivity(),
                                    VueLandingPageActivity.class));
                        } else if (getActivity() instanceof DataEntryActivity) {
                            startActivity(new Intent(
                                    (DataEntryActivity) getActivity(),
                                    VueLandingPageActivity.class));
                        }
                        
                    } else if (s
                            .equals(getString(R.string.sidemenu_option_About))) {
                        inflateAboutLayout();
                    } else if (s
                            .equals(getString(R.string.sidemenu_option_FeedBack))) {
                        startActivity(new Intent(getActivity(),
                                FeedbackForm.class));
                    } else if (s
                            .equals(getString(R.string.sidemeun_option_Settings))) {
                        inflateSettingsLayout();
                    } else if (s
                            .equals(getString(R.string.sidemenu_option_Invite_Friends))) {
                        View layoutInviewFriends = inflater.inflate(
                                R.layout.invite, null);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        vue_list_fragment_invite_friendsLayout_mainxml
                                .addView(layoutInviewFriends);
                        mInviteFriendsLayout = (LinearLayout) layoutInviewFriends
                                .findViewById(R.id.vue_list_fragment_invite_friendsLayout);
                        mInviteFriendsLayout.setLayoutParams(params);
                        mInviteFrirendsListView = (ListView) layoutInviewFriends
                                .findViewById(R.id.vue_list_fragment_Invitefriends_list);
                        mInviteFrirendsListView
                                .setOnItemClickListener(new OnItemClickListener() {
                                    
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> parent, View view,
                                            int position, long id) {
                                        // TODO:
                                    }
                                });
                        
                    } else if (s
                            .equals(getString(R.string.sidemenu_option_Login))) {
                        FlurryAgent.logEvent("Login_Without_Prompt");
                        mSharedPreferencesObj = getActivity()
                                .getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        boolean fbloginfalg = mSharedPreferencesObj.getBoolean(
                                VueConstants.FACEBOOK_LOGIN, false);
                        boolean googleplusloginfalg = mSharedPreferencesObj
                                .getBoolean(VueConstants.GOOGLEPLUS_LOGIN,
                                        false);
                        if (!googleplusloginfalg || !fbloginfalg) {
                            
                            Intent i = new Intent(getActivity(),
                                    VueLoginActivity.class);
                            Bundle b = new Bundle();
                            b.putBoolean(
                                    VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
                                    false);
                            b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG,
                                    false);
                            b.putString(VueConstants.FROM_INVITEFRIENDS, null);
                            b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN,
                                    true);
                            i.putExtras(b);
                            startActivity(i);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getResources().getString(
                                            R.string.already_logged_in_msg),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else if (s
                            .equals(getString(R.string.sidemenu_option_Help))) {
                        Intent i = new Intent(getActivity(), Help.class);
                        i.putExtra(VueConstants.HELP_KEY,
                                getString(R.string.frombezel));
                        startActivity(i);
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
                
                TextView textView = (TextView) v
                        .findViewById(R.id.child_itemTextview);
                String s = textView.getText().toString();
                if (s.equals(getString(R.string.sidemenu_sub_option_My_Aisles))) {
                    VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = false;
                    if (getActivity() instanceof VueLandingPageActivity) {
                        ((VueLandingPageActivity) getActivity()).showCategory(
                                s, false);
                    } else {
                        creatingNewViewFromaOtherActivity(s);
                    }
                    
                    return true;
                } else if (s
                        .equals(getString(R.string.sidemenu_sub_option_Bookmarks))) {
                    if (getActivity() instanceof VueLandingPageActivity) {
                        VueLandingPageActivity activity = (VueLandingPageActivity) getActivity();
                        activity.startActivity(new Intent(getActivity(),
                                VueLandingPageActivity.class));
                        VueApplication.getInstance().landingPage.showCategory(
                                s, false);
                    } else {
                        creatingNewViewFromaOtherActivity(s);
                    }
                    return true;
                } else if (s
                        .equals(getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles))) {
                    if (getActivity() instanceof VueLandingPageActivity) {
                        ((VueLandingPageActivity) getActivity()).showCategory(
                                s, false);
                    } else {
                        creatingNewViewFromaOtherActivity(s);
                    }
                    return true;
                } else if (s.trim().equals(
                        getString(R.string.sidemenu_sub_option_Interactions))) {
                    return true;
                } else if (s
                        .contains(getString(R.string.sidemenu_sub_option_My_Pointss))) {
                    int pointsEarned = Utils.getUserPoints();
                    showRewardsDialog("Silver", pointsEarned);
                    return true;
                }
                if (VueLandingPageActivity.mOtherSourceImagePath == null) {
                    if (s.equals(getString(R.string.sidemenu_option_Profile))) {
                        FlurryAgent.logEvent("Settings_" + s);
                        getUserInfo();
                    } else if (s
                            .equals(getString(R.string.sidemenu_sub_option_Facebook))
                            || s.equals(getString(R.string.sidemenu_sub_option_Googleplus))) {
                        FlurryAgent.logEvent("InviteFriends_" + s);
                        Utils.saveUserPoints(
                                VueConstants.USER_INVITE_FRIEND_POINTS, 5,
                                getActivity());
                        getFriendsList(s);
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
    
    public void setEditTextVisible(boolean show) {
        if (vue_list_fragment_actionbar == null) {
            return;
        }
        if (show) {
            vue_list_fragment_actionbar.setVisibility(View.VISIBLE);
        } else {
            vue_list_fragment_actionbar.setVisibility(View.GONE);
        }
    }
    
    /**
     * It will prepare and return a list of options to show in side menu in
     * bezel.
     * 
     * @return List<ListOptionItem> each ListOptionItem contains item name to
     *         display on screen, image to display and list of sub options.
     * */
    public List<ListOptionItem> getBezelMenuOptionItems() {
        List<ListOptionItem> groups = new ArrayList<VueListFragment.ListOptionItem>();
        ListOptionItem item = new ListOptionItem(
                getString(R.string.sidemenu_option_Trending_Aisles),
                R.drawable.trending, null);
        groups.add(item);
        String userName = getUserId();
        if (userName == null || userName.isEmpty()) {
            userName = getString(R.string.sidemenu_option_Me);
        }
        item = new ListOptionItem(userName, R.drawable.new_profile,
                getMeChildren());
        File f = new FileCache(getActivity())
                .getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME);
        if (f.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(f.getPath());
            
            if (bmp != null) {
                item.userPic = bmp;
            }
        }
        
        groups.add(item);
        // TODO: use this code when Backend server code is ready for categories
        /*
         * item = new ListOptionItem(
         * getString(R.string.sidemenu_option_Categories),
         * R.drawable.new_categories, getCategoriesChildren());
         * groups.add(item);
         */
        item = new ListOptionItem(getString(R.string.sidemeun_option_Settings),
                R.drawable.settings01, getSettingsChildren());
        groups.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_option_Invite_Friends),
                R.drawable.invite, getInviteFriendsChildren());
        groups.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_option_About),
                R.drawable.about, null);
        groups.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_option_FeedBack),
                R.drawable.feedback, null);
        groups.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_option_Login),
                R.drawable.login, null);
        groups.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_option_Help),
                R.drawable.help, null);
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
                R.drawable.new_apparel, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Beauty),
                R.drawable.new_beauty, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Electronics),
                R.drawable.new_electronics, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Entertainment),
                R.drawable.new_entertainment, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Events),
                R.drawable.new_events, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Food),
                R.drawable.new_food, null);
        categoriesChildren.add(item);
        item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Home),
                R.drawable.new_home, null);
        categoriesChildren.add(item);
        
        return categoriesChildren;
    }
    
    private List<ListOptionItem> getInviteFriendsChildren() {
        List<ListOptionItem> inviteFriendsChildren = new ArrayList<VueListFragment.ListOptionItem>();
        ListOptionItem item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Facebook), 0, null);
        inviteFriendsChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Googleplus), 0, null);
        inviteFriendsChildren.add(item);
        return inviteFriendsChildren;
    }
    
    private List<ListOptionItem> getSettingsChildren() {
        List<ListOptionItem> settingsChildren = new ArrayList<VueListFragment.ListOptionItem>();
        ListOptionItem item = new ListOptionItem(
                getString(R.string.sidemenu_option_Profile),
                R.drawable.new_profile, null);
        settingsChildren.add(item);
        return settingsChildren;
    }
    
    private List<ListOptionItem> getMeChildren() {
        List<ListOptionItem> meChildren = new ArrayList<VueListFragment.ListOptionItem>();
        ListOptionItem item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_My_Aisles),
                R.drawable.my_aisles, null);
        meChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Bookmarks),
                R.drawable.bookmark, null);
        meChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles),
                R.drawable.new_recently_viewed, null);
        meChildren.add(item);
        item = new ListOptionItem(
                getString(R.string.sidemenu_sub_option_My_Pointss) + " "
                        + Utils.getUserPoints(),
                R.drawable.new_recently_viewed, null);
        meChildren.add(item);
        return meChildren;
    }
    
    /***/
    private class ListOptionItem {
        public String tag;
        public int iconRes;
        List<ListOptionItem> children;
        public Bitmap userPic;
        
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
    protected class VueListFragmentAdapter extends BaseExpandableListAdapter {
        
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
            holder.icon.setImageResource(groups.get(groupPosition).children
                    .get(childPosition).iconRes);
            // (This line of code will be used to set the icons for children at
            // below line when get all the icons).
            // holder.icon.setImageResource(R.drawable.vue_launcher_icon);
            holder.itemName.setText(groups.get(groupPosition).children
                    .get(childPosition).tag);
            return convertView;
        }
        
        @Override
        public int getChildrenCount(int groupPosition) {
            if (groups.get(groupPosition).tag.equals(getActivity().getString(
                    R.string.pending_aisle_option))) {
                return 0;
            }
            if (groups.get(groupPosition).tag.equals("Categories")
                    || (groups.get(groupPosition).tag
                            .equals(getString(R.string.sidemenu_option_Invite_Friends)))
                    || groups.get(groupPosition).tag.equals("Settings")
                    || groupPosition == 1) {
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
            if (groupPosition == 1) {
                if (groups.get(groupPosition).userPic != null) {
                    holder.icon
                            .setImageBitmap(groups.get(groupPosition).userPic);
                } else {
                    holder.icon
                            .setImageResource(groups.get(groupPosition).iconRes);
                }
            } else {
                holder.icon.setImageResource(groups.get(groupPosition).iconRes);
            }
            holder.itemName.setText(groups.get(groupPosition).tag);
            return convertView;
        }
        
        @Override
        public boolean hasStableIds() {
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
        
        mProgress = ProgressDialog.show(getActivity(), "", "Please wait...");
        mSharedPreferencesObj = getActivity().getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean facebookloginflag = mSharedPreferencesObj.getBoolean(
                VueConstants.FACEBOOK_LOGIN, false);
        boolean googleplusloginflag = mSharedPreferencesObj.getBoolean(
                VueConstants.GOOGLEPLUS_LOGIN, false);
        if (s.equals(getResources().getString(
                R.string.sidemenu_sub_option_Facebook))) {
            
            if (facebookloginflag) {
                fbFriendsList();
            } else {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
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
                getGPlusFriendsList();
            } else {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
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
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }
    }
    
    // Pull and display fb friends from facebook.com
    @SuppressWarnings({ "rawtypes", "unchecked" })
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
                            mInviteFrirendsListView
                                    .setAdapter(new InviteFriendsAdapter(
                                            getActivity(), fbGPlusFriends,
                                            false));
                            
                            expandListView.setVisibility(View.GONE);
                            mInviteFriendsLayout.setVisibility(View.VISIBLE);
                            mInviteFriendsLayout.startAnimation(mAnimUp);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getResources().getString(
                                            R.string.fb_no_friends),
                                    Toast.LENGTH_LONG).show();
                        }
                        if (mProgress.isShowing()) {
                            mProgress.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mProgress.isShowing()) {
                            mProgress.dismiss();
                        }
                    }
                }
            };
            
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                }
            };
            
            StringRequest myReq = new StringRequest(Method.GET, mainURL,
                    listener, errorListener);
            VueApplication.getInstance().getRequestQueue().add(myReq);
            
        } else {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }
    }
    
    // Pull and display G+ friends from plus.google.com.
    private void getGPlusFriendsList() {
        if (VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
            mInviteFrirendsListView
                    .setAdapter(new InviteFriendsAdapter(
                            getActivity(),
                            VueLandingPageActivity.mGooglePlusFriendsDetailsList,
                            false));
            expandListView.setVisibility(View.GONE);
            mInviteFriendsLayout.setVisibility(View.VISIBLE);
            mInviteFriendsLayout.startAnimation(mAnimUp);
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        } else {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
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
        mCustomlayout.startAnimation(mAnimUp);
        mCustomlayout.setVisibility(View.VISIBLE);
        
        VueUserProfile vueUserProfile = null;
        try {
            vueUserProfile = Utils.readUserProfileObjectFromFile(getActivity(),
                    VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (vueUserProfile != null) {
            mIsNewUser = true;
            mProfilePicUrl = vueUserProfile.getUserProfilePicture();
            mUserNameEdit.setText(vueUserProfile.getUserName());
            mUuserDobEdit.setText(vueUserProfile.getUserDOB());
            mUserGenderEdit.setText(vueUserProfile.getUserGender());
            mUserEmailEdit.setText(vueUserProfile.getUserEmail());
            mUserLocationEdit.setText(vueUserProfile.getUserLocation());
            if (!mUserEmailEdit.getText().toString().isEmpty()) {
                mUserEmailEdit.setEnabled(false);
            }
        }
        
        if (mProfilePicUrl != null) {
            File f = new FileCache(getActivity())
                    .getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME);
            if (f.exists()) {
                mUserProfilePic.setImageURI(Uri.fromFile(f));
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
        if (mCustomlayout == null) {
            layoutSettings = inflater.inflate(R.layout.settings_layout, null);
            LinearLayout.LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mCustomlayout = (LinearLayout) layoutSettings
                    .findViewById(R.id.customlayout);
            mCustomlayout.setLayoutParams(params);
            mUserProfilePic = (ImageView) layoutSettings
                    .findViewById(R.id.user_profilePic);
            mUserNameEdit = (EditText) layoutSettings
                    .findViewById(R.id.user_name_EditText);
            mUuserDobEdit = (EditText) layoutSettings
                    .findViewById(R.id.user_DOB_EditText);
            mUserGenderEdit = (EditText) layoutSettings
                    .findViewById(R.id.user_Gender_EditText);
            mUserEmailEdit = (EditText) layoutSettings
                    .findViewById(R.id.user_Email_EditText);
            mUserLocationEdit = (EditText) layoutSettings
                    .findViewById(R.id.user_location_EditText);
            
            mUuserDobEdit.setOnTouchListener(new OnTouchListener() {
                
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        dataPicker();
                    }
                    return false;
                }
            });
            mUserNameEdit.addTextChangedListener(this);
            mUuserDobEdit.addTextChangedListener(this);
            mUserGenderEdit.addTextChangedListener(this);
            mUserEmailEdit.addTextChangedListener(this);
            mUserLocationEdit.addTextChangedListener(this);
            mDoneLayout = (RelativeLayout) layoutSettings
                    .findViewById(R.id.donelayout);
            mDoneLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserNameEdit.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(),
                                "User name cannot be blank", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    mCustomlayout.setVisibility(View.GONE);
                    mCustomlayout.startAnimation(mAnimDown);
                    expandListView.setVisibility(View.VISIBLE);
                    if (mIsProfileEdited || mIsNewUser) {
                        mUuserDobEdit.getText().toString();
                        mUserGenderEdit.getText().toString();
                        mUserEmailEdit.getText().toString();
                        mUserLocationEdit.getText().toString();
                        mUserEmailEdit.getText().toString();
                        try {
                            VueUserProfile vueUserProfile = new VueUserProfile(
                                    mProfilePicUrl, mUserEmailEdit.getText()
                                            .toString(), mUserNameEdit
                                            .getText().toString(),
                                    mUuserDobEdit.getText().toString(),
                                    mUserGenderEdit.getText().toString(),
                                    mUserLocationEdit.getText().toString(),
                                    true);
                            Utils.writeUserProfileObjectToFile(
                                    getActivity(),
                                    VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME,
                                    vueUserProfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mIsProfileEdited = false;
                        mIsNewUser = false;
                    }
                }
            });
            mBezelMainLayout.addView(layoutSettings);
        }
        mCustomlayout.setVisibility(View.GONE);
        
    }
    
    private void inflateAboutLayout() {
        View aboutLayoutView = null;
        if (mAboutLayout == null) {
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
            mAboutLayout = (LinearLayout) aboutLayoutView
                    .findViewById(R.id.aboutlayout);
            mAboutLayout.setLayoutParams(params);
            mAboutDoneLayout = (RelativeLayout) aboutLayoutView
                    .findViewById(R.id.aboutdonelayout);
            mAboutDoneLayout.setLayoutParams(params2);
            mAboutDoneLayout.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    expandListView.setVisibility(View.VISIBLE);
                    mAboutLayout.setVisibility(View.GONE);
                    mAboutLayout.startAnimation(mAnimDown);
                }
            });
            mBezelMainLayout.addView(aboutLayoutView);
        }
        expandListView.setVisibility(View.GONE);
        mAboutLayout.setVisibility(View.VISIBLE);
        mAboutLayout.startAnimation(mAnimUp);
    }
    
    @Override
    public void afterTextChanged(Editable s) {
        if (!mIsProfileEdited)
            mIsProfileEdited = true;
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            String y = Integer.toString(year);
            String m = Integer.toString(monthOfYear);
            String d = Integer.toString(dayOfMonth);
            mUuserDobEdit.setText(y + "/" + m + "/" + d);
        }
    };
    
    private void dataPicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog DPD = new DatePickerDialog(getActivity(),
                mDateSetListener, mYear, mMonth, mDay);
        DPD.show();
    }
    
    public interface ProfileImageChangeListenor {
        public void onImageChange();
    }
    
    public String getUserId() {
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String userName = null;
        if (storedVueUser != null) {
            userName = storedVueUser.getFirstName() + " "
                    + storedVueUser.getLastName();
        }
        return userName;
        
    }
    
    public void refreshBezelMenu() {
        VueListFragmentAdapter adapter = null;
        adapter = new VueListFragment.VueListFragmentAdapter(
                VueListFragment.this.getActivity(),
                VueListFragment.this.getBezelMenuOptionItems());
        VueListFragment.this.expandListView.setAdapter(adapter);
    }
    
    public class BezelMenuRefreshReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent != null) {
                try {
                    refreshBezelMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void creatingNewViewFromaOtherActivity(String s) {
        VueApplication.getInstance().mNewViewSelection = true;
        VueApplication.getInstance().mNewlySelectedView = s;
        getActivity().finish();
    }
    
    public void closeKeybaord() {
        final InputMethodManager mInputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(
                mSideMenuSearchBar.getWindowToken(), 0);
        
    }
    
    private void showRewardsDialog(String userType, int pointsEarned) {
        int pointsRequired = 0;
        String nextUserType = "silver";
        if (pointsEarned < 100) {
            userType = "bronze";
            nextUserType = "silver";
            pointsRequired = 100 - pointsEarned;
        } else {
            userType = "silver";
        }
        if (userType.endsWith("bronze")) {
            
            StringBuilder sb = new StringBuilder("You are a ");
            sb.append(userType);
            sb.append(" user with ");
            sb.append("" + pointsEarned);
            sb.append(" points! Only ");
            sb.append(pointsRequired);
            sb.append(" points to go to become a ");
            sb.append(nextUserType);
            sb.append(" user");
            final Dialog dialog = new Dialog(getActivity(),
                    R.style.Theme_Dialog_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.networkdialogue);
            TextView messagetext = (TextView) dialog
                    .findViewById(R.id.messagetext);
            TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);
            View networkdialogline = dialog
                    .findViewById(R.id.networkdialogline);
            networkdialogline.setVisibility(View.GONE);
            TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
            nobutton.setVisibility(View.GONE);
            okbutton.setText(getResources().getString(R.string.ok));
            messagetext.setText(sb);
            okbutton.setOnClickListener(new OnClickListener() {
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
                }
            });
        } else if (userType.equals("silver")) {
            StringBuilder sb = new StringBuilder(
                    "Congratulations! You are now a Silver Vuer! As a thank you, we will gladly send you $5 to shop online.");
            final SharedPreferences sharedPreferencesObj = VueApplication
                    .getInstance().getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
            final Dialog dialog = new Dialog(getActivity(),
                    R.style.Theme_Dialog_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.networkdialogue);
            TextView messagetext = (TextView) dialog
                    .findViewById(R.id.messagetext);
            TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);
            View networkdialogline = dialog
                    .findViewById(R.id.networkdialogline);
            TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
            nobutton.setText(getResources()
                    .getString(R.string.continue_earning));
            okbutton.setText(getResources().getString(R.string.redeem_it_now));
            boolean isRedeemCoupon = sharedPreferencesObj.getBoolean(
                    VueConstants.USER_REEDM_POINTS, false);
            if (isRedeemCoupon) {
                okbutton.setVisibility(View.GONE);
            }
            messagetext.setText(sb);
            nobutton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            okbutton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    VueUser storedVueUser = null;
                    try {
                        storedVueUser = Utils.readUserObjectFromFile(
                                getActivity(),
                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                        if (storedVueUser != null) {
                            JSONObject nameTag = new JSONObject();
                            nameTag.put("Redeem", "RedeemItNow");
                            nameTag.put("Id", storedVueUser.getId());
                            nameTag.put("Email", storedVueUser.getEmail());
                            
                            Editor editor = sharedPreferencesObj.edit();
                            editor.putBoolean(VueConstants.USER_REEDM_POINTS,
                                    true);
                            editor.commit();
                            // TODO: mix panel log.
                            mixpanel.track("Coupon", nameTag);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    
                    Toast.makeText(
                            getActivity(),
                            "Thank you for being such an awesome Vuer! Expect to see the rewards from us shortly in your email inbox.",
                            Toast.LENGTH_LONG).show();
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
                }
            });
        }
    }
    
}
