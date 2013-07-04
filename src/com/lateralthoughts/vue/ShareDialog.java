package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.lateralthoughts.vue.utils.InstalledPackageRetriever;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.facebook.Request.Callback;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * @author raju common class for share functionality capable of handling
 *         email,gmail,twitter,face book and used action send intent to call the
 *         other applications
 * 
 */
public class ShareDialog {

	LayoutInflater inflater;
	AlertDialog.Builder screenDialog;

	ArrayList<String> listmessages = new ArrayList<String>();
	ArrayList<ResolveInfo> currentDisp = new ArrayList<ResolveInfo>();
	ArrayList<Drawable> drawbles = new ArrayList<Drawable>();

	List<ResolveInfo> activities;
	Intent sendIntent;
	Context context;
	Activity activity;
	String shareOption;
	Resources resouces;

	InstalledPackageRetriever shareintentObj;
	public final static int TWITER_TITLE_BUDGET = 76;
	Bitmap articleBitmap;
	

	public Dialog dialog;
	
	String aisleTitle, name;
	ArrayList<clsShare> imagePathArray;
	
	
	
	/**
	 * 
	 * @param context
	 *            Context
	 */
	public ShareDialog(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resouces = context.getResources();
		
		
	}

	

	public void share(ArrayList<clsShare> imagePathArray, String aisleTitle, String name) {
		
		this.imagePathArray = imagePathArray;
		this.aisleTitle = aisleTitle;
		this.name = name;
		
		prepareShareIntentData();

		openScreenDialog();
	}

	/** to show pop-up */
	private void openScreenDialog() {
	 
		  dialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.sharedialogue);
		ListView listview = (ListView) dialog.findViewById(R.id.networklist);
		TextView okbuton = (TextView) dialog.findViewById(R.id.shownetworkok);
		listview.setAdapter(new CustomAdapter());
		
		
		listview.setOnItemClickListener(new OnItemClickListener(){
	        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
	        	Log.e("sharedialog", "clicked");
	    		
	    		shareIntent(position);
	    		
	         }
	    });
	    
	
		dialog.show();
		okbuton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				InputMethodManager i1pm = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				i1pm.hideSoftInputFromWindow(null, 0);
			}
		});
		
		screenDialog = new AlertDialog.Builder(context);
		screenDialog.setTitle("SHARE VIA");


	}

	/**
	 * Adapter to set on popup dialogue
	 * 
	 * */
	public class CustomAdapter extends BaseAdapter {
		/**
		 * returns the count
		 * 
		 * @return int
		 */
		public int getCount() {
			return listmessages.size();
		}

		/**
		 * @param arg0
		 *            int
		 * @return Object
		 */
		public Object getItem(int arg0) {
			return arg0;
		}

		/**
		 * @param arg0
		 *            int
		 * @return long
		 */
		public long getItemId(int arg0) {
			return arg0;
		}

		/**
		 * @param position
		 *            int
		 * 
		 * @param convertView
		 *            View
		 * @param parent
		 *            ViewGroup
		 * @return View
		 */
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holderView = null;
			if (convertView == null) {
				holderView = new Holder();
				convertView = inflater.inflate(R.layout.sharedialog_row, null);
				holderView.network = (TextView) convertView
						.findViewById(R.id.gmail);
				holderView.launcheicon=(ImageView) convertView.findViewById(R.id.launchericon);
				holderView.popupbg = (RelativeLayout) convertView.findViewById(R.id.popupbg);
				
				convertView.setTag(holderView);
			} else {
				holderView = (Holder) convertView.getTag();
			}

			holderView.launcheicon.setImageDrawable(drawbles.get(position));
		
 
			holderView.network.setText(listmessages.get(position));
			
	
			
		
			return convertView;
		}

	}

	private void shareIntent(final int position)
 {
		try {

			if (listmessages.get(position).equalsIgnoreCase(
					VueConstants.FACEBOOK_APP_NAME)) {
				
				
				dialog.dismiss();
				
				  String shareText = "Your friend "
							+ name
							+ " wants your opinion - get Vue to see the full details and help "
							+ name + " out.";
				
				 Intent i = new Intent(context, VueLoginActivity.class);
				  Bundle b = new Bundle();
				  b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				  b.putString(VueConstants.FROM_INVITEFRIENDS, null);
				  b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, true);
				  b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				  b.putString(VueConstants.FBPOST_TEXT, shareText);
				  b.putParcelableArrayList(VueConstants.FBPOST_IMAGEURLS, imagePathArray);
				  i.putExtras(b);
				  context.startActivity(i);
				
				
				 /* final SharedPreferences sharedPreferencesObj = activity.getSharedPreferences(
					        VueConstants.SHAREDPREFERENCE_NAME, 0);
				  
				  boolean facebookloginflag = sharedPreferencesObj.getBoolean(
					        VueConstants.FACEBOOK_LOGIN, false);
				
				
				  
				  final ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
				  
				  final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                          R.drawable.vue_launcher_icon);
				  
				  bitmapList.add(bitmap);
				  
				  final Bitmap bitmap1 = BitmapFactory.decodeResource(context.getResources(),
                          R.drawable.vue_title_icon);
				  
				  bitmapList.add(bitmap1);
				  
				  if(facebookloginflag)
				  {
					  shareToFacebook(bitmapList, shareText);
				  }
				  else
				  {
					  
					  showAlertMessageShareError("Please Login to Facebook from Mainscreen to Share content to Facebook" , true);
					  
					  	// start Facebook Login
						Session.openActiveSession(activity, true,
								new Session.StatusCallback() {

									// callback when session changes state
									@Override
									public void call(Session session,
											SessionState state, Exception exception) {
										  Log.e("fb", "63"+session);
										if (session.isOpened()) {
						                	Log.e("fb", "64");

											   Session.setActiveSession(session);
											
											   SharedPreferences.Editor editor = sharedPreferencesObj.edit();
											      editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
											    		  session.getAccessToken());
											      editor
											          .putBoolean(VueConstants.VUE_LOGIN, true);
											      editor
											      .putBoolean(VueConstants.FACEBOOK_LOGIN, true);
											      editor.commit();
											
											shareToFacebook(bitmapList, shareText);

											// make request to the /me API
											Request.executeMeRequestAsync(session,
													new Request.GraphUserCallback() {

														// callback after Graph API
														// response with user object
														@Override
														public void onCompleted(
																GraphUser user,
																Response response) {
										                	Log.e("fb", "65");

															if (user != null) {
															}
														}
													});
										}
									}
								});
				  
					
					  VueApplication.getInstance().fbsharingflag = true;
					  VueLandingPageActivity.fbLogin();
				  */
				//  }
				
			}

			else if (listmessages.get(position).equalsIgnoreCase(
					VueConstants.GOOGLEPLUS_APP_NAME)) {
				shareImageAndText(position);
			} else if (listmessages.get(position).equalsIgnoreCase(
					VueConstants.GMAIL_APP_NAME)) {
				shareImageAndText(position);
			} else if (listmessages.get(position).equalsIgnoreCase(
					VueConstants.TWITTER_APP_NAME)) {
				shareText(position);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			showAlertMessageShareError(listmessages.get(position), false);
		}

	}
	
	private void showAlertMessageShareError(String appName, boolean fberror) {

	    final Dialog gplusdialog = new Dialog(context,
	        R.style.Theme_Dialog_Translucent);
	    gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
	    TextView messagetext = (TextView) gplusdialog.findViewById(R.id.messagetext);
	    
	   if(!fberror) messagetext.setText("Unable to Share content to "+ appName);
	   else messagetext.setText(appName);
	    TextView noButton = (TextView) gplusdialog.findViewById(R.id.nobutton);
	    
	    noButton.setVisibility(View.GONE);
	    
	    TextView okButton = (TextView) gplusdialog.findViewById(R.id.okbutton);
	    okButton.setText("OK");
	    okButton.setOnClickListener(new OnClickListener() {

	      public void onClick(View v) {
	        gplusdialog.dismiss();
	     
	      }
	    });
	   

	    gplusdialog.show();

	  }
	
	private void prepareShareIntentData() {
		

		if (shareintentObj == null) {
			InstalledPackageRetriever shareintentObjtemp = new InstalledPackageRetriever(
					context);
			shareintentObjtemp.createIntent();
			shareintentObjtemp.getInstalledPackages();
			shareintentObjtemp.makeShorList();
			shareintentObj = shareintentObjtemp;
		}

		listmessages.addAll(shareintentObj.getListMessages());
		currentDisp.addAll(shareintentObj.getDisplayPackages());
		drawbles.addAll(shareintentObj.getDrawables());
		sendIntent = shareintentObj.getShareIntent();

	}

	
	

	private class Holder {
		TextView network;
		ImageView launcheicon;
		RelativeLayout popupbg;
	}
	
	private void downloadImage(String url, File f)
	{
	      try {
			URL imageUrl = new URL(url);
			  HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
			  conn.setConnectTimeout(30000);
			  conn.setReadTimeout(30000);
			  conn.setInstanceFollowRedirects(true);
			  InputStream is=conn.getInputStream();
			  OutputStream os = new FileOutputStream(f);
			  Utils.CopyStream(is, os);
			  os.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void shareImageAndText(final int position)
	{
		
		final ProgressDialog progress = ProgressDialog.show(activity, "", "Sharing...");
		
		
		
		   Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                

    				ArrayList<Uri> imageUris = new ArrayList<Uri>();

    				
    				if (imagePathArray != null && imagePathArray.size() > 0) {
    					for (int i = 0; i < imagePathArray.size(); i++) {
    						
    						File f = new File(imagePathArray.get(i).getFilepath());
    						
    						if(!f.exists()) downloadImage(imagePathArray.get(i).getImageUrl(), f);
    						
    						Uri screenshotUri = Uri.fromFile(f);
    						imageUris.add(screenshotUri);
    					}
    				}
                	
                	String shareText = "Your friend "
    						+ name
    						+ " wants your opinion - get Vue to see the full details and help "
    						+ name + " out.";
    				sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
    				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("mailto:"));
    				
    				sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

    				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
    						imageUris);

    				ResolveInfo info = (ResolveInfo) currentDisp.get(position);
    				
    				
    				
    				sendIntent.setClassName(info.activityInfo.packageName,
    						info.activityInfo.name);

                	
                	activity.runOnUiThread(new Runnable() {

                      @Override
                      public void run() {
                    	  progress.dismiss();
                    		screenDialog.setCancelable(true);
            				context.startActivity(sendIntent);

            				dialog.dismiss();
                      }
                    });
                  } 
                });t.start();
		
	}
	
	private void shareText(int position)
	{
		dialog.dismiss();
		
		String shareText = "Your friend "
				+ name
				+ " wants your opinion - get Vue to see the full details and help "
				+ name + " out.";
		sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

		ResolveInfo info = (ResolveInfo) currentDisp.get(position);
		
		Log.e("share", info.activityInfo.packageName + "..."+ info.activityInfo.name);
		
		sendIntent.setClassName(info.activityInfo.packageName,
				info.activityInfo.name);

		screenDialog.setCancelable(true);
		context.startActivity(sendIntent);

	}
	
	

}