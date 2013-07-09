package com.lateralthoughts.vue;

import java.io.File;

import com.lateralthoughts.vue.utils.FileCache;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment for creating Aisle
 * @author krishna
 *
 */
public class CreateAilseFragment extends Fragment{

	ListView lookingforlistview = null, ocassionlistview = null, categorylistview = null;
	
	LinearLayout lookingforpopup = null, lookingforlistviewlayout = null, ocassionlistviewlayout = null, ocassionpopup = null, categoerypopup = null, categorylistviewlayout = null;
	
	TextView lookingfortext = null, lookingforbigtext = null, occassionbigtext = null, occasiontext = null, categorytext = null;
	
	private static final String lookingforitemsArray[] = {"LivingRoomSet", "Dress", "Shoes", "Belt", "Tie"};
	
	private static final String occasionitemsArray[] = {"Redecoration", "Vacation", "Wedding", "Birthday", "Party"};
	
	private static final String categoryitemsArray[] = {"Home", "Office", "Resturant", "Theater", "Kichen"};
	
	private Drawable listdivider = null;
	
	ImageView createaisel_bg = null;
	
	Uri selectedCameraImage = null;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		
		View v = inflater.inflate(R.layout.create_aisleview_fragment, container, false);
		
		lookingforlistview = (ListView) v.findViewById(R.id.lookingforlistview);
		
		lookingfortext = (TextView) v.findViewById(R.id.lookingfortext);
		
		lookingforbigtext = (TextView) v.findViewById(R.id.lookingforbigtext);
		
		occassionbigtext = (TextView) v.findViewById(R.id.occassionbigtext);
		
		ocassionlistview = (ListView) v.findViewById(R.id.ocassionlistview);
		
		ocassionlistviewlayout = (LinearLayout) v.findViewById(R.id.ocassionlistviewlayout);
		
		ocassionpopup = (LinearLayout) v.findViewById(R.id.ocassionpopup);
		
		occasiontext = (TextView) v.findViewById(R.id.occasiontext);
		
		lookingforlistviewlayout = (LinearLayout) v.findViewById(R.id.lookingforlistviewlayout);
		
		lookingforpopup = (LinearLayout) v.findViewById(R.id.lookingforpopup);
		
		lookingfortext.setText(lookingforitemsArray[0]);
		
		
		occasiontext.setText(occasionitemsArray[0]);
		
	
		lookingforlistview.setAdapter(new LookingForAdapter(getActivity()));
		
		categoerypopup = (LinearLayout) v.findViewById(R.id.categoerypopup);
		
		categorytext = (TextView) v.findViewById(R.id.categorytext);
		
		categorytext.setText(categoryitemsArray[0]);
		
		categorylistview = (ListView) v.findViewById(R.id.categorylistview);
		
		categorylistviewlayout = (LinearLayout) v.findViewById(R.id.categorylistviewlayout);
		
		listdivider = getResources().getDrawable(R.drawable.list_divider_line);
		
		lookingforlistview.setDivider(listdivider);
		
		createaisel_bg = (ImageView) v.findViewById(R.id.createaisel_bg);
		
		return v;
	}
	
	class LookingForAdapter extends BaseAdapter
	{
		
		Activity context;

		public LookingForAdapter(Activity context) {
			super();
			this.context = context;
		}
		 class ViewHolder {
				
			 TextView dataentryitemname;
			
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			
			//here we inflating the layout "R.layout.cars_row"
			ViewHolder holder = null;
			View rowView = convertView;
			
			
			if (rowView == null) {
				
				LayoutInflater inflater = context.getLayoutInflater();
			
				rowView = inflater.inflate(R.layout.dataentry_row, null, true);

				holder = new ViewHolder();

				holder.dataentryitemname=(TextView) rowView.findViewById(R.id.dataentryitemname);
				
				
				rowView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) rowView.getTag();
			}
		
			if (position == 0) {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.black));
				holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
			} else {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.dataentrytextcolor));
				holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
			}
			
			holder.dataentryitemname.setText(lookingforitemsArray[position]);
			
			rowView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					
					lookingfortext.setText(lookingforitemsArray[position]);
					lookingforbigtext.setText(lookingforitemsArray[position]);					
					lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);
					occassionbigtext.setBackgroundColor(getResources().getColor(R.color.yellowbgcolor));
					
					lookingforlistview.setVisibility(View.GONE);
					lookingforpopup.setVisibility(View.GONE);
					lookingforlistviewlayout.setVisibility(View.GONE);
					
					ocassionlistview.setVisibility(View.VISIBLE);
					ocassionlistviewlayout.setVisibility(View.VISIBLE);
					ocassionpopup.setVisibility(View.VISIBLE);
					
					ocassionlistview.setAdapter(new OccasionAdapter(getActivity()));
					
					ocassionlistview.setDivider(listdivider);
					
				}
			});
			
			return rowView;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return lookingforitemsArray.length;
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
	
	// Occasion....
	class OccasionAdapter extends BaseAdapter
	{
		
		Activity context;

		public OccasionAdapter(Activity context) {
			super();
			this.context = context;
		}
		 class ViewHolder {
				
			 TextView dataentryitemname;
			
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			
			//here we inflating the layout "R.layout.cars_row"
			ViewHolder holder = null;
			View rowView = convertView;
			
			
			if (rowView == null) {
				
				LayoutInflater inflater = context.getLayoutInflater();
			
				rowView = inflater.inflate(R.layout.dataentry_row, null, true);

				holder = new ViewHolder();

				holder.dataentryitemname=(TextView) rowView.findViewById(R.id.dataentryitemname);
				
				
				rowView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) rowView.getTag();
			}
		
			if (position == 0) {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.black));
				holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
			} else {
				holder.dataentryitemname.setTextColor(getResources().getColor(
						R.color.dataentrytextcolor));
				holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
			}
			
			holder.dataentryitemname.setText(occasionitemsArray[position]);
			
			rowView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					
					occasiontext.setText(occasionitemsArray[position]);
					occassionbigtext.setText(" "+occasionitemsArray[position]);					
					occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
					
					ocassionlistview.setVisibility(View.GONE);
					ocassionpopup.setVisibility(View.GONE);
					ocassionlistviewlayout.setVisibility(View.GONE);
					
					categorylistview.setVisibility(View.VISIBLE);
					categorylistviewlayout.setVisibility(View.VISIBLE);
					categoerypopup.setVisibility(View.VISIBLE);
					
					categorylistview.setAdapter(new CategoryAdapter(getActivity()));
					
					categorylistview.setDivider(listdivider);
					
				}
			});
			
			return rowView;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return occasionitemsArray.length;
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
		class CategoryAdapter extends BaseAdapter
		{
			
			Activity context;

			public CategoryAdapter(Activity context) {
				super();
				this.context = context;
			}
			 class ViewHolder {
					
				 TextView dataentryitemname;
				
			}

			public View getView(final int position, View convertView, ViewGroup parent){
				
				//here we inflating the layout "R.layout.cars_row"
				ViewHolder holder = null;
				View rowView = convertView;
				
				
				if (rowView == null) {
					
					LayoutInflater inflater = context.getLayoutInflater();
				
					rowView = inflater.inflate(R.layout.dataentry_row, null, true);

					holder = new ViewHolder();

					holder.dataentryitemname=(TextView) rowView.findViewById(R.id.dataentryitemname);
					
					
					rowView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder) rowView.getTag();
				}
			
				if (position == 0) {
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
						// TODO Auto-generated method stub
						
						categorytext.setText(categoryitemsArray[position]);
						
						categorylistview.setVisibility(View.GONE);
						categoerypopup.setVisibility(View.GONE);
						categorylistviewlayout.setVisibility(View.GONE);
						
						showAlertMessageForChoosingImage();
						
					}
				});
				
				return rowView;

			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
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

	
		private void showAlertMessageForChoosingImage() {

		    final Dialog gplusdialog = new Dialog(getActivity(),
		        R.style.Theme_Dialog_Translucent);
		    gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		    gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
		    
		    TextView messagetext = (TextView) gplusdialog.findViewById(R.id.messagetext);
		    messagetext.setText("Select Image From");
		    
		    TextView noButton = (TextView) gplusdialog.findViewById(R.id.nobutton);
		    TextView okButton = (TextView) gplusdialog.findViewById(R.id.okbutton);
		    
		    okButton.setText("Camera");
		    noButton.setText("Gallery");
		    
		    
		    
		    // Camera....
		    okButton.setOnClickListener(new OnClickListener() {

		      public void onClick(View v) {
		        gplusdialog.dismiss();

		        Toast.makeText(getActivity(), "In Progress...", Toast.LENGTH_LONG).show();
		        
		       /* try{
		        	Intent intent = new Intent("android.provider.MediaStore.ACTION_IMAGE_CAPTURE");
		            File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
		            try {
						photo.createNewFile();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            intent.putExtra(MediaStore.EXTRA_OUTPUT,
		                    Uri.fromFile(photo));
		            selectedCameraImage = Uri.fromFile(photo);
		            getActivity().startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
		        	
		        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    		
		        	FileCache obj = new FileCache(getActivity());
		        	
		        
		     
		    		selectedCameraImage = Uri.fromFile(obj.getFile("test"));
		    		intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedCameraImage);
		    		getActivity().startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
				}catch(Exception e)
				{
					//showAlertMessage(getString(R.string.no_camera_label),getActivity());
				}*/
		     
		      }
		    });
		    
		    // Gallery....
		    noButton.setOnClickListener(new OnClickListener() {

		      public void onClick(View v) {
		        gplusdialog.dismiss();
		        
		        Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 
		        
		      /* Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);*/
				getActivity().startActivityForResult(Intent.createChooser(i,
				"Select Picture"), VueConstants.SELECT_PICTURE);
		      }
		    });

		    gplusdialog.show();
		    
		  

		  }

		public void setGalleryImage(String picturePath)
		{
			  createaisel_bg.setImageBitmap(BitmapFactory.decodeFile(picturePath));
		}
		
		public void setCameraImage()
		{
			   getActivity().getContentResolver().notifyChange(selectedCameraImage, null);
	            ContentResolver cr = getActivity().getContentResolver();
	            Bitmap bitmap;
	            try {
	                 bitmap = android.provider.MediaStore.Images.Media
	                 .getBitmap(cr, selectedCameraImage);

	                createaisel_bg.setImageBitmap(bitmap);
	                
	            } catch (Exception e) {
	                
	                Log.e("Camera", e.toString());
	            }
		}
		
}
