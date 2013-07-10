package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.Utils;

public class VueListFragment extends Fragment {
 // public static final String TAG = "VueListFragment";
  private ExpandableListView expandListView;
  private LinearLayout customlayout, aboutlayout, invitefriendsLayout;
  private RelativeLayout donelayout, aboutdonelayout;
  private ImageView userProfilePic;
  private TextView userName, userDateOfBirth, userGender, userCurrentLocation;
  private CheckBox smallch, mediumch, largech ;
  private Animation animDown;
  private Animation animUp;
  private ListView inviteFrirendsListView;
  public FriendsListener listener;
  private SharedPreferences sharedPreferencesObj;
  private ImageLoader mImageLoader;
  private ProgressDialog progress;
 
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    sharedPreferencesObj = getActivity().getSharedPreferences(
        VueConstants.SHAREDPREFERENCE_NAME, 0);
   // pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        if (aboutlayout != null && aboutlayout.getVisibility() == View.VISIBLE) {
          aboutlayout.setVisibility(View.GONE);
          aboutlayout.startAnimation(animDown);
          returnWhat = true;
        }
        if (customlayout != null
            && customlayout.getVisibility() == View.VISIBLE) {
          customlayout.setVisibility(View.GONE);
          customlayout.startAnimation(animDown);
          returnWhat = true;
        }
        return returnWhat;
      }
    };
    return inflater.inflate(R.layout.vue_list_fragment, null);
  }

  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    customlayout = (LinearLayout) getActivity().findViewById(R.id.customlayout);
    customlayout.setVisibility(View.GONE);
    expandListView = (ExpandableListView) getActivity().findViewById(
        R.id.vue_list_fragment_list);
    donelayout = (RelativeLayout) getActivity().findViewById(R.id.donelayout);
    aboutlayout = (LinearLayout) getActivity().findViewById(R.id.aboutlayout);
    aboutdonelayout = (RelativeLayout) getActivity().findViewById(
        R.id.aboutdonelayout);
    LayoutParams params = new LayoutParams((int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, getScreenWidth() / 2, getActivity()
            .getResources().getDisplayMetrics()),
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
            getActivity().getResources().getDisplayMetrics()));
    params.gravity = Gravity.CENTER_HORIZONTAL;
    aboutdonelayout.setLayoutParams(params);
    inviteFrirendsListView = (ListView) getActivity().findViewById(
        R.id.vue_list_fragment_Invitefriends_list);
    invitefriendsLayout = (LinearLayout) getActivity().findViewById(
        R.id.vue_list_fragment_invite_friendsLayout);
    VueListFragmentAdapter adapter = new VueListFragmentAdapter(getActivity(),
        getBezelMenuOptionItems());
    expandListView.setAdapter(adapter);
    animDown = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_down);
    animUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_up);
    userProfilePic = (ImageView) getActivity().findViewById(R.id.user_profilePic);
    userName = (TextView) getActivity().findViewById(R.id.user_name);
    userDateOfBirth = (TextView) getActivity().findViewById(R.id.user_date_Of_birth);
    userGender = (TextView) getActivity().findViewById(R.id.user_gender);
    userCurrentLocation = (TextView) getActivity().findViewById(R.id.user_current_location);
    expandListView.setOnGroupClickListener(new OnGroupClickListener() {

      @Override
      public boolean onGroupClick(ExpandableListView parent, View v,
          int groupPosition, long id) {
        TextView textView = (TextView) v
            .findViewById(R.id.vue_list_fragment_itemTextview);
        String s = textView.getText().toString();
        if (s.equals(getString(R.string.sidemeun_option_Settings))) {
          customlayout.setVisibility(View.VISIBLE);
          customlayout.startAnimation(animUp);
        } else if (s.equals(getString(R.string.sidemenu_option_About))) {
          aboutlayout.setVisibility(View.VISIBLE);
          aboutlayout.startAnimation(animUp);
        } else if (s.equals(getString(R.string.sidemenu_option_FeedBack))) {
          startActivity(new Intent(getActivity(), FeedbackForm.class));
        }
        else if(s.equals(getString(R.string.sidemenu_option_Login)))
        {
        	 sharedPreferencesObj = getActivity().getSharedPreferences(
     	            VueConstants.SHAREDPREFERENCE_NAME, 0);
        	 boolean fbloginfalg = sharedPreferencesObj.getBoolean(
       	          VueConstants.FACEBOOK_LOGIN, false);
       	boolean googleplusloginfalg = sharedPreferencesObj.getBoolean(
       	          VueConstants.GOOGLEPLUS_LOGIN, false);
       	
       	if(!googleplusloginfalg || !fbloginfalg)
       	{
        	
					Intent i = new Intent(getActivity(), VueLoginActivity.class);
					Bundle b = new Bundle();
					 b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
					b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
					b.putString(VueConstants.FROM_INVITEFRIENDS,
							null);
					b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, true);
					i.putExtras(b);
					startActivity(i);
       	}
       	else
       	{
       	 Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.AlreadyLoggedinmesg),
     	        Toast.LENGTH_LONG).show();
       	}
       	}
        return false;
      }
    });

    expandListView.setOnChildClickListener(new OnChildClickListener() {

      @Override
      public boolean onChildClick(ExpandableListView parent, View v,
          int groupPosition, int childPosition, long id) {
    	  TextView textView = (TextView) v.findViewById(R.id.child_itemTextview);
    	  String s = textView.getText().toString();
    	  getFriendsList(s);
        return false;
      }
    });
    aboutdonelayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        aboutlayout.setVisibility(View.GONE);
        aboutlayout.startAnimation(animDown);
      }
    });
    donelayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
       // Utils.saveNetworkSettings(getActivity(), wifich.isChecked());
        customlayout.setVisibility(View.GONE);
        customlayout.startAnimation(animDown);
      }
    });

    inviteFrirendsListView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {

        String itemName = ((TextView) view
            .findViewById(R.id.invite_friends_name)).getText().toString();
        // TODO:
      }
    });
  }

  public void setListener() {

  }

  /**
   * 
   * @param small boolean
   * @param medium boolean
   * @param large boolean to check the check boxes when select one of the check
   *        box
   */
  private void setChecks(boolean small, boolean medium, boolean large) {
    smallch.setChecked(small);
    mediumch.setChecked(medium);
    largech.setChecked(large);
  }

  /**
   * It will prepare and return a list of options to show in side menu in bezel.
   * 
   * @return List<ListOptionItem> each ListOptionItem contains item name to
   *         display on screen, image to diaplay and list of sub options.
   * */
  private List<ListOptionItem> getBezelMenuOptionItems() {
    List<ListOptionItem> groups = new ArrayList<VueListFragment.ListOptionItem>();
    ListOptionItem item = new ListOptionItem(
        getString(R.string.sidemenu_option_My_Aisles), R.drawable.profile, null);
    groups.add(item);
    item = new ListOptionItem(getString(R.string.sidemenu_option_Categories),
        R.drawable.category, getCategoriesChildren());
    groups.add(item);
    item = new ListOptionItem(getString(R.string.sidemeun_option_Settings),
        R.drawable.settings01, null);
    groups.add(item);
    /*item = new ListOptionItem(getString(R.string.sidemenu_option_Profile),
        R.drawable.profile, null);
    groups.add(item);*/
    item = new ListOptionItem(
        getString(R.string.sidemenu_option_Invite_Friends), R.drawable.invite,
        getInviteFriendsChildren());
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
   *         display on screen, image to diaplay, there will be not sub options.
   * */
  private List<ListOptionItem> getCategoriesChildren() {
    List<ListOptionItem> categoriesChildren = new ArrayList<VueListFragment.ListOptionItem>();
    ListOptionItem item = new ListOptionItem(
        getString(R.string.sidemenu_sub_option_Apparel), R.drawable.comment,
        null);
    categoriesChildren.add(item);
    item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Beauty),
        R.drawable.comment, null);
    categoriesChildren.add(item);
    item = new ListOptionItem(
        getString(R.string.sidemenu_sub_option_Electronics),
        R.drawable.comment, null);
    categoriesChildren.add(item);
    item = new ListOptionItem(
        getString(R.string.sidemenu_sub_option_Entertainment),
        R.drawable.comment, null);
    categoriesChildren.add(item);
    item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Events),
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
        getString(R.string.sidemenu_sub_option_Facebook), R.drawable.comment,
        null);
    inviteFriendsChildren.add(item);
    /*
     * item = new ListOptionItem(
     * getString(R.string.sidemenu_sub_option_Twitter), R.drawable.comment,
     * null); inviteFriendsChildren.add(item);
     */
    item = new ListOptionItem(getString(R.string.sidemenu_sub_option_Gmail),
        R.drawable.comment, null);
    inviteFriendsChildren.add(item);
    return inviteFriendsChildren;
  }

  /***/
  private class ListOptionItem {
    public String tag;
    public int iconRes;
    List<ListOptionItem> children;

    public ListOptionItem(String tag, int iconRes, List<ListOptionItem> children) {
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

    private List<ListOptionItem> groups;

    public VueListFragmentAdapter(Context context, List<ListOptionItem> groups) {
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
      // groups.get(groupPosition).children.get(childPosition).iconRes
      // (This line of code will be used to set the icons for children at
      // below line when get all the icons).
      holder.icon.setImageResource(R.drawable.vue_launcher_icon);
      holder.itemName.setText(groups.get(groupPosition).children
          .get(childPosition).tag);
      return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      if (groups.get(groupPosition).tag.equals("Categories")
          || (groups.get(groupPosition).tag
              .equals(getString(R.string.sidemenu_option_Invite_Friends)))) {
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

  private class InviteFriendsAdapter extends ArrayAdapter<FbGPlusDetails> {

    List<FbGPlusDetails> items;
    Context context;

    public InviteFriendsAdapter(Context context, int textViewResourceId,
        List<FbGPlusDetails> objects) {
      super(context, textViewResourceId, objects);
      this.context = context;
      items = objects;
      mImageLoader = new ImageLoader(VueApplication.getInstance().getRequestQueue(), new BitmapLruCache(1024));

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      InviteFriendHolder holder;
      if (convertView == null) {
        convertView = LayoutInflater.from(getActivity()).inflate(
            R.layout.invite_friends, null);
        holder = new InviteFriendHolder();
        holder.friendprifilepic = (NetworkImageView) convertView
            .findViewById(R.id.invite_friends_imageView);
        holder.name = (TextView) convertView
            .findViewById(R.id.invite_friends_name);
        
        holder.invite_friends_addFriends = (Button) convertView.findViewById(R.id.invite_friends_addFriends);
        
        convertView.setTag(holder);
      } else {
        holder = (InviteFriendHolder) convertView.getTag();
      }
      holder.name.setText(items.get(position).getName());
    /*  holder.imageView.setImageBitmap(imageLoader.loadImage(context,
          items.get(position).getProfile_image_url(), holder.imageView));*/
      
      holder.friendprifilepic.setImageUrl(items.get(position).getProfile_image_url(), mImageLoader);

      
      
      final int index = position;
      
      holder.invite_friends_addFriends .setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					
					// Google+ friends
					if(items.get(index).getGoogleplusFriend() != null)
					{
						Intent i = new Intent(getActivity(), VueLoginActivity.class);
						Bundle b = new Bundle();
						b.putInt(VueConstants.GOOGLEPLUS_FRIEND_INDEX, index);
						b.putBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE, true);
						i.putExtras(b);
						startActivity(i);

						
					}
					// Facebook friends
					else
					{
						if(items.get(index).getId() != null)
						{
						Intent i = new Intent(getActivity(), VueLoginActivity.class);
						Bundle b = new Bundle();
						b.putString(VueConstants.FB_FRIEND_ID, items.get(index).getId());
						b.putString(VueConstants.FB_FRIEND_NAME, items.get(index).getName());
						i.putExtras(b);
						startActivity(i);
						}	
					}
				}
			});
      
      return convertView;
    }

    @Override
    public int getCount() {
      try {
		return items.size();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return 0;
    }
  }
  private static class InviteFriendHolder {
	  NetworkImageView friendprifilepic;
    TextView name;
    Button invite_friends_addFriends;
  }

  public interface FriendsListener {

    public boolean onBackPressed();
  }
  
  public void getFriendsList(String s) {

	    progress = ProgressDialog.show(getActivity(), "", "Plase wait...");
	    Log.e(getTag(), "SURU : Value of s : " + s);
	    
	    sharedPreferencesObj = getActivity().getSharedPreferences(
	            VueConstants.SHAREDPREFERENCE_NAME, 0);
	    
	    boolean facebookloginflag = sharedPreferencesObj.getBoolean(VueConstants.FACEBOOK_LOGIN,
	        false);
	    boolean googleplusloginflag = sharedPreferencesObj.getBoolean(VueConstants.GOOGLEPLUS_LOGIN,
		        false);
	    if (s.equals("Facebook")) {

	        final VueShare share = new VueShare();
	        if (facebookloginflag) {
	          fbFriendsList(share);
	        }  else {
		        if (progress.isShowing()) {
			          progress.dismiss();
			        }
			        	
			          Intent i = new Intent(getActivity(), VueLoginActivity.class);
			       	  Bundle b = new Bundle();
			       	  b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
			       	 b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
			       	  b.putString(VueConstants.FROM_INVITEFRIENDS, VueConstants.FACEBOOK);
			       	 b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
			       	  i.putExtras(b);
			       	  startActivity(i);
			         
			       
			      }
	       
	    } else if (s.equals("Google Plus")) {
	        if (googleplusloginflag) {
	        	 Log.e(getTag(), "GOOGLEPLUS : Value of s : 1" );
	          getGPlusFriendsList();
	        } else {
		        if (progress.isShowing()) {
			          progress.dismiss();
			        }
		        Log.e(getTag(), "GOOGLEPLUS : Value of s : 2" );
			        	 Log.e(getTag(), "GOOGLEPLUS : Value of s : 3" );
			          Intent i = new Intent(getActivity(), VueLoginActivity.class);
			       	  Bundle b = new Bundle();
			       	  b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
			       	 b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
			       	  b.putString(VueConstants.FROM_INVITEFRIENDS, VueConstants.GOOGLEPLUS);
			       	  b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
			       	  i.putExtras(b);
			       	  startActivity(i);
			        }
			      
	    }
	    else {
			if (progress.isShowing()) {
				progress.dismiss();
			}
		}
	  }

	  // Pull and display fb friends from facebook.com
  private void fbFriendsList(final VueShare share) {



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
            fbGPlusFriends = share.JsonParsing(response);
            if (fbGPlusFriends != null) {
              inviteFrirendsListView.setAdapter(new InviteFriendsAdapter(
                  getActivity(), R.layout.invite_friends, fbGPlusFriends));
              expandListView.setVisibility(View.GONE);
              invitefriendsLayout.setVisibility(View.VISIBLE);
              invitefriendsLayout.startAnimation(animUp);
            }
            if (progress.isShowing()) {
              progress.dismiss();
            }
          } catch (JSONException e) {
            // TODO Auto-generated catch block
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
          Log.e("VueNetworkError",
              "Vue encountered network operations error. Error = "
                  + error.networkResponse);
          if (progress.isShowing()) {
            progress.dismiss();
          }
        }
      };

      StringRequest myReq = new StringRequest(Method.GET, mainURL, listener,
          errorListener);

      /*
       * JsonArrayRequest vueRequest = new JsonArrayRequest(mainURL, listener,
       * errorListener );
       */

      VueApplication.getInstance().getRequestQueue().add(myReq);

    }

    else {
      if (progress.isShowing()) {
        progress.dismiss();
      }
    }

  }


	  // Pull and display G+ friends from plus.google.com.
	  private void getGPlusFriendsList() {
	    if(VueLandingPageActivity.googlePlusFriendsDetailsList != null) {
	    inviteFrirendsListView.setAdapter(new InviteFriendsAdapter(getActivity(),
	        R.layout.invite_friends, VueLandingPageActivity.googlePlusFriendsDetailsList));
	    expandListView.setVisibility(View.GONE);
	    invitefriendsLayout.setVisibility(View.VISIBLE);
	    invitefriendsLayout.startAnimation(animUp);
	    
	    if (progress.isShowing()) {
		      progress.dismiss();
		    }
	    
	    }
	    else
	    {
	    	
	    	  if (progress.isShowing()) {
	    	      progress.dismiss();
	    	    }
	    	
	    	 Intent i = new Intent(getActivity(), VueLoginActivity.class);
	       	  Bundle b = new Bundle();
	       	  b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
	       	  b.putBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN, true);
	       	 b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
	       	  b.putString(VueConstants.FROM_INVITEFRIENDS, VueConstants.GOOGLEPLUS);
	       	  b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
	       	  i.putExtras(b);
	       	  startActivity(i);
	    }
	  

	  }
	  
    private void getUserInfo() {
      String name  = userName.getText().toString();
      String dob = userDateOfBirth.getText().toString();
      String gender = userGender.getText().toString();
      String location = userCurrentLocation.toString();
    }
}