/**
 * 
 * @author Vinodh Sundararajan
 * One of the more complex parts of the adapter is the mechanism for interacting
 * with content gateway and keeping track of aisle window content.
 * An AisleWindowContent is made up of a bunch of images all belonging to one
 * category but contributed by several different users.
 * We are going to be dealing with humongous amounts of data so need to careful
 * about this.
 * As soon as the adapter goes live we will initiate a request to get the top trending
 * aisles. But we have no idea how many are top trending; meaning, if there are hundreds of
 * them it will take forever for the data to come back We will therefore use the limit
 * and offset parameters to get data in smaller chunks.
 * The adapter keep an array of AisleWindowContent each of which contains array of content
 * When a new item comes in, it the category has already been created we add its content
 * to an existing AisleWindowContent. Otherwise, create a new one.
 * 
 * Relationship between AisleWindowContent and grid item: Each AisleWindowContent object will
 * take up one spot in the StaggeredGridView. This spot consists of a ViewFlipper so there will
 * many many images. This spot also contains a "meta" field with information relating to the person
 * who added the item, thumbnail image of the person, context and occasion for the category.
 *
 */

package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AilseLeftListLisner;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AilseRighttRightLisner;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.Utils;

public class TrendingAislesRightColumnAdapter extends
		TrendingAislesGenericAdapter {
	private Context mContext;

	private final String TAG = "TrendingAislesRightColumnAdapter";

	private AisleLoader mLoader;

	private static final boolean DEBUG = true;

	public int firstX;
	public int lastX;
	AisleContentClickListener listener;
	LinearLayout.LayoutParams mShowpieceParams, mShowpieceParamsDefault;
	BitmapLoaderUtils mBitmapLoaderUtils;
	public boolean mHasToShow = false;

	public TrendingAislesRightColumnAdapter(Context c,
			ArrayList<AisleWindowContent> content) {
		super(c, content);
		mContext = c;
		if (DEBUG)
			Log.e(TAG, "About to initiate request for trending aisles");
		mLoader = AisleLoader.getInstance(mContext);
	}

	public TrendingAislesRightColumnAdapter(Context c,
			AisleContentClickListener listener,
			ArrayList<AisleWindowContent> content) {
		super(c, listener, content);
		mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
		mContext = c;
		mLoader = AisleLoader.getInstance(mContext);
		this.listener = listener;

		if (DEBUG)
			Log.e(TAG, "About to initiate request for trending aisles");
		// mVueTrendingAislesDataModel.registerAisleDataObserver(this);
	}

	@Override
	public int getCount() {
		return mVueTrendingAislesDataModel.getAisleCount() / 2;
	}

	@Override
	public AisleWindowContent getItem(int position) {
		int positionFactor = 2;
		int actualPosition = 1;
		if (0 != position)
			actualPosition = (positionFactor * position) + actualPosition;

		return mVueTrendingAislesDataModel.getAisleAt(actualPosition);
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i("SCROLL_STATE_IDLE", "SCROLL_STATE_IDLE 3 getview");
		ViewHolder holder;
		Log.i("TrendingDataModel",
				"DataObserver for List Refresh:  Right getview ");
		if (null == convertView) {
			Log.i("TrendingDataModel",
					"DataObserver for List Refresh: Right getview if ");
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.staggered_row_item,
					null);
			holder = new ViewHolder();
			holder.aisleContentBrowser = (AisleContentBrowser) convertView
					.findViewById(R.id.aisle_content_flipper);
			holder.startImageLay = (LinearLayout) convertView
					.findViewById(R.id.starImagelay); 
			LinearLayout.LayoutParams showpieceParams = new LinearLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth() / 2, 200);
			// holder.aisleContentBrowser.setLayoutParams(showpieceParams);
			holder.aisleDescriptor = (LinearLayout) convertView
					.findViewById(R.id.aisle_descriptor);
			holder.profileThumbnail = (ImageView) holder.aisleDescriptor
					.findViewById(R.id.profile_icon_descriptor);
			holder.aisleOwnersName = (TextView) holder.aisleDescriptor
					.findViewById(R.id.descriptor_aisle_owner_name);
			holder.aisleContext = (TextView) holder.aisleDescriptor
					.findViewById(R.id.descriptor_aisle_context);
			holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			holder.aisleContentBrowser.setAilseRighttListLisner(new RightList());
			convertView.setTag(holder);

			if (DEBUG)
				Log.e("Jaws2", "getView invoked for a new view at position2 = "
						+ position);
		}
		// AisleWindowContent windowContent =
		// (AisleWindowContent)getItem(position);
		holder = (ViewHolder) convertView.getTag();
		holder.mWindowContent = (AisleWindowContent) getItem(position);
		holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
		int scrollIndex = 0; // getContentBrowserIndexForId(windowContent.getAisleId());
		mLoader.getAisleContentIntoView(holder, scrollIndex, position, false,
				listener,"RightAdapter");
		AisleContext context = holder.mWindowContent.getAisleContext();
		String mVueusername = null;
		if(mHasToShow){
			holder.startImageLay.setVisibility(View.VISIBLE);
		}else {
			holder.startImageLay.setVisibility(View.GONE);
		}
		if (context.mFirstName != null && context.mLastName != null) {
			mVueusername = context.mFirstName + " " + context.mLastName;
		} else if (context.mFirstName != null) {
			if (context.mFirstName.equals("Anonymous")) {
				mVueusername = VueApplication.getInstance().getmUserInitials();
			} else {
				mVueusername = context.mFirstName;
			}
		} else if (context.mLastName != null) {
			mVueusername = context.mLastName;
		}
		if (mVueusername != null
				&& mVueusername.trim().equalsIgnoreCase("Anonymous")) {
			if (VueApplication.getInstance().getmUserInitials() != null) {
				mVueusername = VueApplication.getInstance().getmUserInitials();
			}
		}
		if (mVueusername != null && mVueusername.trim().length() > 0) {
			holder.aisleOwnersName.setText(mVueusername);
		} else {
			holder.aisleOwnersName.setText("Anonymous");
		}

		// Title (lookingfor and occasion)
		String occasion = null;
		String lookingFor = null;
		if (context.mOccasion != null && context.mOccasion.length() > 1) {
			occasion = context.mOccasion;
		}
		if (context.mLookingForItem != null
				&& context.mLookingForItem.length() > 1) {
			lookingFor = context.mLookingForItem;
		}
		if (occasion != null && occasion.length() > 1) {
			occasion = occasion.toLowerCase();
			occasion = Character.toString(occasion.charAt(0)).toUpperCase()
					+ occasion.substring(1);
		}
		if (lookingFor != null && lookingFor.length() > 1) {
			lookingFor = lookingFor.toLowerCase();
			lookingFor = Character.toString(lookingFor.charAt(0)).toUpperCase()
					+ lookingFor.substring(1);
		}
		String title = "";
		if (occasion != null) {
			title = occasion + " : ";
		}
		if (lookingFor != null) {
			title = title + lookingFor;
		}
		holder.aisleContext.setText(title);
		return convertView;
	}

	@Override
	public void onAisleDataUpdated(int newCount) {
		Log.i("TrendingDataModel",
				"DataObserver for List Refresh: Right List AisleUpdate Called ");
		notifyDataSetChanged();
	}

	private int calculateActualPosition(int position) {
		int positionFactor = 2;
		int actualPosition = 1;
		if (0 != position)
			actualPosition = (positionFactor * position) + actualPosition;
		return actualPosition;
	}
	  private class RightList implements AilseRighttRightLisner {

		@Override
		public void onSwipe(boolean hasToShwo) {
			mHasToShow = hasToShwo;
			mHasToShow = false;
			Log.i("settingAdaptersrest", "settingAdaptersrestRight");
			notifyDataSetChanged();
			
		}
			  
		  }
}