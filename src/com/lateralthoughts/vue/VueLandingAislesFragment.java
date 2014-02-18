package com.lateralthoughts.vue;

//generic android & java goodies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.ui.ArcMenu;
import com.lateralthoughts.vue.utils.Utils;
import com.origamilabs.library.views.StaggeredGridView;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends Fragment {
	private Context mContext;
	private VueContentGateway mVueContentGateway;
	private LandingPageViewAdapter mStaggeredAdapter;

	private StaggeredGridView mStaggeredView;

	private AisleClickListener mAisleClickListener;
	public boolean mIsFlingCalled;

	public boolean mIsIdleState;
	private ProgressBar mTrendingLoad;

	// TODO: define a public interface that can be implemented by the parent
	// activity so that we can notify it with an ArrayList of AisleWindowContent
	// once we have received the result and parsed it. The idea is that the
	// activity
	// can then initiate a worker in the background to go fetch more content and
	// get
	// ready to launch other activities/fragments within the application

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;

		// without much ado lets get started with retrieving the trending aisles
		// list
		mVueContentGateway = VueContentGateway.getInstance();
		if (null == mVueContentGateway) {
			// assert here: this is a no go!
		}

		mAisleClickListener = new AisleClickListener();
		mStaggeredAdapter = new LandingPageViewAdapter(mContext,
				mAisleClickListener);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// TODO: any particular state that we want to restore?

	}

	public void notifyAdapters() {
		if (null != mStaggeredAdapter) {
			mStaggeredAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// synchronized list view approach
		View v = inflater.inflate(R.layout.aisles_view_fragment, container,
				false);
		mStaggeredView = (StaggeredGridView) v.findViewById(R.id.aisles_grid);
		mTrendingLoad = (ProgressBar) v.findViewById(R.id.trending_load);
		int margin = getResources().getDimensionPixelSize(R.dimen.margin);
		mStaggeredView.setItemMargin(margin); // set the GridView margin

		mStaggeredView.setPadding(margin, 0, margin, 0); // have the margin on
															// the sides as well
		mStaggeredView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return false; // To change body of implemented methods use File
								// | Settings | File Templates.
			}
		});

		mStaggeredView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {
				switch (i) {
				case SCROLL_STATE_FLING:
				case SCROLL_STATE_TOUCH_SCROLL:
					mStaggeredAdapter.setIsScrolling(true);
					VueApplication.getInstance().getRequestQueue()
							.cancelAll(VueApplication.LOAD_IMAGES_REQUEST_TAG);
					break;

				case SCROLL_STATE_IDLE:
					mStaggeredAdapter.setIsScrolling(false);
					VueApplication.getInstance().getRequestQueue()
							.cancelAll(VueApplication.MORE_AISLES_REQUEST_TAG);
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount > 0
						&& mTrendingLoad.getVisibility() == View.VISIBLE) {
					mTrendingLoad.setVisibility(View.GONE);
				}
				if (VueTrendingAislesDataModel.getInstance(mContext).loadOnRequest
						&& VueLandingPageActivity.mLandingScreenName != null
						&& VueLandingPageActivity.mLandingScreenName
								.equalsIgnoreCase(getResources().getString(
										R.string.trending))
						&& !VueTrendingAislesDataModel.getInstance(mContext).mIsFromDb) {
					int lastVisiblePosition = firstVisibleItem
							+ visibleItemCount;
					if ((totalItemCount - lastVisiblePosition) < 5) {
						VueTrendingAislesDataModel
								.getInstance(mContext)
								.getNetworkHandler()
								.requestMoreAisle(
										true,
										getResources().getString(
												R.string.trending));
						try {
							SharedPreferences sharedPreferencesObj = getActivity()
									.getSharedPreferences(
											VueConstants.SHAREDPREFERENCE_NAME,
											0);
							boolean isHelpShown = sharedPreferencesObj
									.getBoolean(VueConstants.HELP_SCREEN_ACCES,
											false);
							if (isHelpShown) {
								int count = sharedPreferencesObj
										.getInt(VueConstants.USER_FINDFRIENDS_OPEN_COUNT,
												0);
								final int SHOW_LIMIT = 3;
								if (count < SHOW_LIMIT) {
									long showedTime = sharedPreferencesObj
											.getLong(
													VueConstants.USER_FINDFRIENDS_OPEN_TIME,
													0);
									int hours = (int) Utils
											.dateDifference(showedTime);
									final int DAY_LATER = 24;
									if (hours > DAY_LATER) {
										showInviteFriendsDialog();
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		});
		mStaggeredView.setAdapter(mStaggeredAdapter);
		return v;
	}

	private class AisleClickListener implements AisleContentClickListener {
		@Override
		public void onAisleClicked(String id, int count, int aisleImgCurrentPos) {
			if (VueLandingPageActivity.mOtherSourceImagePath == null) {
				VueApplication.getInstance().saveTrendingRefreshTime(0);
				Map<String, String> articleParams = new HashMap<String, String>();
				VueUser storedVueUser = null;
				try {
					storedVueUser = Utils.readUserObjectFromFile(getActivity(),
							VueConstants.VUE_APP_USEROBJECT__FILENAME);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				if (storedVueUser != null) {
					articleParams.put("User_Id",
							Long.valueOf(storedVueUser.getId()).toString());
				} else {
					articleParams.put("User_Id", "anonymous");
				}

				DataBaseManager.getInstance(mContext)
						.updateOrAddRecentlyViewedAisles(id);

				FlurryAgent.logEvent("User_Select_Aisle", articleParams);
				Intent intent = new Intent();
				intent.setClass(VueApplication.getInstance(),
						AisleDetailsViewActivity.class);
				VueApplication.getInstance().setClickedWindowID(id);
				VueApplication.getInstance().setClickedWindowCount(count);
				VueApplication.getInstance().setmAisleImgCurrentPos(
						aisleImgCurrentPos);
				startActivity(intent);
			} else {
				VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
				vueLandingPageActivity.hideDefaultActionbar();
				VueLandingPageActivity.mOtherSourceAddImageAisleId = id;
				notifyAdapters();
			}

		}

		@Override
		public boolean isFlingCalled() {
			return mIsFlingCalled;
		}

		@Override
		public boolean isIdelState() {

			return mIsIdleState;
		}

		@Override
		public boolean onDoubleTap(String id) {
			AisleWindowContent windowItem = VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).getAisleAt(id);
			int finalWidth = 0, finaHeight = 0;
			if (windowItem.getImageList().get(0).mAvailableHeight >= windowItem
					.getBestHeightForWindow()) {
				finalWidth = (windowItem.getImageList().get(0).mAvailableWidth * windowItem
						.getBestHeightForWindow())
						/ windowItem.getImageList().get(0).mAvailableHeight;
				finaHeight = windowItem.getBestHeightForWindow();
			}

			if (finalWidth > VueApplication.getInstance().getScreenWidth() / 2) {

				finaHeight = (finaHeight
						* VueApplication.getInstance().getScreenWidth() / 2)
						/ finalWidth;
				finalWidth = VueApplication.getInstance().getScreenWidth() / 2;
			}

			String writeSdCard = null;
			writeSdCard = "*************************aisle info:"
					+ " started***********************\n";
			writeSdCard = writeSdCard + "\nAisleId: " + windowItem.getAisleId()
					+ "\n" + "Smallest Image Height: "
					+ windowItem.getImageList().get(0).mTrendingImageHeight
					+ "\n" + "Card Width: "
					+ VueApplication.getInstance().getVueDetailsCardWidth() / 2
					+ "\n";
			for (int i = 0; i < windowItem.getImageList().size(); i++) {
				writeSdCard = writeSdCard + "\n ImageUrl: "
						+ windowItem.getImageList().get(i).mImageUrl;
				writeSdCard = writeSdCard + "\n" + "image Width: "
						+ windowItem.getImageList().get(i).mAvailableWidth
						+ " Height: "
						+ windowItem.getImageList().get(i).mAvailableHeight;
			}
			writeSdCard = writeSdCard + "\n\n After Resized Aisle height: "
					+ windowItem.getImageList().get(0).mTrendingImageHeight
					+ " After Resized Aisle width: "
					+ VueApplication.getInstance().getVueDetailsCardWidth() / 2;

			for (int i = 0; i < windowItem.getImageList().size(); i++) {
				writeSdCard = writeSdCard + "\n CustomImageUrl: "
						+ windowItem.getImageList().get(i).mCustomImageUrl;
			}

			writeSdCard = writeSdCard
					+ "\n###################### info end ################################";

			writeSdCard = writeSdCard + "\n "
					+ windowItem.getAisleContext().mFirstName + " ???? "
					+ windowItem.getAisleContext().mLastName;

			writeToSdcard(writeSdCard);
			return false;
		}

		@Override
		public void refreshList() {
			mStaggeredAdapter.notifyDataSetChanged();
		}

		@Override
		public void hideProgressBar(int count) {
			if (mTrendingLoad.getVisibility() == View.VISIBLE) {
				mTrendingLoad.setVisibility(View.GONE);
				notifyAdapters();
			}
		}

		@Override
		public void showProgressBar() {
			if (mTrendingLoad.getVisibility() == View.GONE) {
				mTrendingLoad.setVisibility(View.VISIBLE);
			}

		}
	}

	public int getListPosition() {
		return mStaggeredView.getFirstPosition();

	}

	private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(getActivity());
			item.setImageResource(itemDrawables[i]);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			lp.setMargins(4, 4, 4, 4);
			final int position = i;
			menu.addItem(i, lp, item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "position:" + position,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private void writeToSdcard(String message) {

		String path = Environment.getExternalStorageDirectory().toString();
		File dir = new File(path + "/vueImageDetails/");
		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		File file = new File(dir, "/"
				+ Calendar.getInstance().get(Calendar.DATE)
				+ "-"
				+ Utils.getWeekDay(Calendar.getInstance().get(
						Calendar.DAY_OF_WEEK)) + ".txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));
			out.write("\n" + message + "\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showInviteFriendsDialog() {
		SharedPreferences sharedPreferencesObj = mContext.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		int count = sharedPreferencesObj.getInt(
				VueConstants.USER_FINDFRIENDS_OPEN_COUNT, 0);
		count = count + 1;
		Editor edit = sharedPreferencesObj.edit();
		edit.putInt(VueConstants.USER_FINDFRIENDS_OPEN_COUNT, count);
		edit.putLong(VueConstants.USER_FINDFRIENDS_OPEN_TIME,
				System.currentTimeMillis());
		edit.commit();
		StringBuilder sb = new StringBuilder(
				"Use vue for your shopping decisions\nGet your friends to join the fun\nEarn $$ rewards. ");
		final Dialog dialog = new Dialog(mContext,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.help_popup);
		TextView title = (TextView) dialog.findViewById(R.id.dialogtitle);
		title.setText("Make vue your own");
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);
		View networkdialogline = dialog.findViewById(R.id.networkdialogline);
		TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
		okbutton.setText(getString(R.string.ok));
		nobutton.setVisibility(View.GONE);

		messagetext.setText(sb);
		messagetext.setTextSize(Utils.MEDIUM_TEXT_SIZE);
		okbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
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
			}
		});
	}

}
