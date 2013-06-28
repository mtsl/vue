package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.lateralthoughts.vue.utils.InstalledPackageRetriever;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
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
	List<File> imagePathArray;
	
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

	

	public void share(List<File> imagePathArray, String aisleTitle, String name) {
		
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

	private void shareIntent(int position)
 {
		try {
			Log.e("sharedialog", "method called");

			/*if (listmessages.get(position).equalsIgnoreCase("facebook")) {

				ArrayList<Uri> imageUris = new ArrayList<Uri>();

				if (imagePathArray != null && imagePathArray.size() > 0) {
					for (int i = 0; i < imagePathArray.size(); i++) {
						Log.e("Share", imagePathArray.get(i).getPath());
						Uri screenshotUri = Uri.fromFile(imagePathArray.get(i));
						imageUris.add(screenshotUri);
					}
				}


				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";
				sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				//sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						imageUris);

				ResolveInfo info = (ResolveInfo) currentDisp.get(position);
				sendIntent.setClassName(info.activityInfo.packageName,
						info.activityInfo.name);

				screenDialog.setCancelable(true);
				context.startActivity(sendIntent);

				dialog.dismiss();
			}
			*/

			 if (listmessages.get(position).equalsIgnoreCase("Google+")) {
				Log.e("Share", "Google+");
				/*String path = null;
				path = Images.Media.insertImage(context.getContentResolver(),
						BitmapFactory.decodeResource(context.getResources(),
								R.drawable.vue_launcher_icon), aisleTitle, null);*/

				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";

				VueLandingPageActivity.mSignInFragment.share(
						VueLandingPageActivity.plusClient, activity, shareText,
						imagePathArray);

				dialog.dismiss();
			}

/*	else if (listmessages.get(position).equalsIgnoreCase("twitter")) {
				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";
				sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

				ResolveInfo info = (ResolveInfo) currentDisp.get(position);
				sendIntent.setClassName(info.activityInfo.packageName,
						info.activityInfo.name);

				screenDialog.setCancelable(true);
				context.startActivity(sendIntent);

				dialog.dismiss();
			}*/ else if(listmessages.get(position).equals("Gmail")){

				
					ArrayList<Uri> imageUris = new ArrayList<Uri>();

					if (imagePathArray != null && imagePathArray.size() > 0) {
						for (int i = 0; i < imagePathArray.size(); i++) {
							Log.e("Share", imagePathArray.get(i).getPath());
							Uri screenshotUri = Uri.fromFile(imagePathArray.get(i));
							imageUris.add(screenshotUri);
						}
					}

/*	ArrayList<Uri> imageUris = new ArrayList<Uri>();

					String path = null;
					path = Images.Media.insertImage(context.getContentResolver(),
							BitmapFactory.decodeResource(context.getResources(),
									R.drawable.vue_launcher_icon), aisleTitle, null);
					Uri screenshotUri = Uri.parse(path);
					imageUris.add(screenshotUri);

					String path1 = null;
					path1 = Images.Media.insertImage(context.getContentResolver(),
							BitmapFactory.decodeResource(context.getResources(),
									R.drawable.background), aisleTitle, null);
					Uri screenshotUri1 = Uri.parse(path1);
					imageUris.add(screenshotUri1);

					String path11 = null;
					path11 = Images.Media.insertImage(context.getContentResolver(),
							BitmapFactory.decodeResource(context.getResources(),
									R.drawable.vue_launcher_icon), aisleTitle, null);
					Uri screenshotUri11 = Uri.parse(path11);
					imageUris.add(screenshotUri11);*/
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

					screenDialog.setCancelable(true);
					context.startActivity(sendIntent);

					dialog.dismiss();
				

			}
			 
			else
			{
				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";
				sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

				ResolveInfo info = (ResolveInfo) currentDisp.get(position);
				sendIntent.setClassName(info.activityInfo.packageName,
						info.activityInfo.name);

				screenDialog.setCancelable(true);
				context.startActivity(sendIntent);

				dialog.dismiss();
			}
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			showAlertMessageShareError(listmessages.get(position));
		}

	}
	
	private void showAlertMessageShareError(String appName) {

	    final Dialog gplusdialog = new Dialog(context,
	        R.style.Theme_Dialog_Translucent);
	    gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
	    TextView messagetext = (TextView) gplusdialog.findViewById(R.id.messagetext);
	    
	    messagetext.setText("Unable to Share content to "+ appName);
	    
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
		/*
		 * sendIntent = new Intent(android.content.Intent.ACTION_SEND);
		 * sendIntent.setType("text/plain");
		 * sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
		 * this.articleHeadline);
		 * sendIntent.putExtra(android.content.Intent.EXTRA_HTML_TEXT, "<b>" +
		 * articleText + "</b>");
		 */

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
	
	
}