package com.lateralthoughts.vue;

import java.io.File;

import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OnInterceptListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * Fragment for creating Aisle
 * @author krishna
 *
 */
public class CreateAilseFragment extends Fragment{

	ListView lookingforlistview = null, ocassionlistview = null, categorylistview = null;
	
	LinearLayout lookingforpopup = null, lookingforlistviewlayout = null, ocassionlistviewlayout = null, ocassionpopup = null, categoerypopup = null, categorylistviewlayout = null;
	
	TextView  touchtochangeimage = null, lookingforbigtext = null, occassionbigtext = null,categorytext = null;
	
	com.lateralthoughts.vue.utils.EditTextBackEvent lookingfortext = null,  occasiontext = null, saysomethingaboutaisle = null;
	
	private static final String lookingforitemsArray[] = {"LivingRoomSet", "Dress", "Shoes", "Belt", "Tie"};
	
	private static final String occasionitemsArray[] = {"Redecoration", "Vacation", "Wedding", "Birthday", "Party"};
	
	private static final String categoryitemsArray[] = { "Apparel", "Beauty", "Electronics", "Entertainment", "Events", "Food", "Home"};
	
	private Drawable listdivider = null;
	
	ImageView createaisel_bg = null, categoeryicon = null;
	
	Uri selectedCameraImage = null;
	
	InputMethodManager inputMethodManager;
	
	int categorycurrentselectedposition = 0;
	
	boolean dontgotonextforlookup = false, dontgotonextforoccasion = false;
	
	String previouslookingfor = null, previousocasion = null, previoussaysomething = null;
	
	public static boolean create_ailse_keyboard_hidden_shown_flag = false;
	
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
		
		create_ailse_keyboard_hidden_shown_flag = true;
		
		View v = inflater.inflate(R.layout.create_aisleview_fragment, container, false);
		
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		lookingforlistview = (ListView) v.findViewById(R.id.lookingforlistview);
		
		lookingfortext = (EditTextBackEvent) v.findViewById(R.id.lookingfortext);
		
		lookingforbigtext = (TextView) v.findViewById(R.id.lookingforbigtext);
		
		lookingforbigtext.setBackgroundColor(getResources().getColor(R.color.yellowbgcolor));
		
		occassionbigtext = (TextView) v.findViewById(R.id.occassionbigtext);
		
		ocassionlistview = (ListView) v.findViewById(R.id.ocassionlistview);
		
		ocassionlistviewlayout = (LinearLayout) v.findViewById(R.id.ocassionlistviewlayout);
		
		ocassionpopup = (LinearLayout) v.findViewById(R.id.ocassionpopup);
		
		occasiontext = (EditTextBackEvent) v.findViewById(R.id.occasiontext);
		
		lookingforlistviewlayout = (LinearLayout) v.findViewById(R.id.lookingforlistviewlayout);
		
		lookingforpopup = (LinearLayout) v.findViewById(R.id.lookingforpopup);
		
		touchtochangeimage = (TextView) v.findViewById(R.id.touchtochangeimage);
		
		saysomethingaboutaisle = (EditTextBackEvent) v.findViewById(R.id.saysomethingaboutaisle);
		
		categoeryicon = (ImageView) v.findViewById(R.id.categoeryicon);
	
		lookingforlistview.setAdapter(new LookingForAdapter(getActivity()));
		
		categoerypopup = (LinearLayout) v.findViewById(R.id.categoerypopup);
		
		categorytext = (TextView) v.findViewById(R.id.categorytext);
		
		categorytext.setText(categoryitemsArray[0]);
		
		categorylistview = (ListView) v.findViewById(R.id.categorylistview);
		
		categorylistviewlayout = (LinearLayout) v.findViewById(R.id.categorylistviewlayout);
		
		listdivider = getResources().getDrawable(R.drawable.list_divider_line);
		
		lookingforlistview.setDivider(listdivider);
		
		lookingforlistviewlayout.setVisibility(View.GONE);
		
		lookingforlistview.setVisibility(View.GONE);
		
		createaisel_bg = (ImageView) v.findViewById(R.id.createaisel_bg);
		
		
		categorylistview.setAdapter(new CategoryAdapter(getActivity()));
		
		categorylistview.setDivider(listdivider);
		
		previouslookingfor = lookingfortext.getText().toString();
		previousocasion = occasiontext.getText().toString();
		previoussaysomething = saysomethingaboutaisle.getText().toString();
		
		saysomethingaboutaisle.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				create_ailse_keyboard_hidden_shown_flag = false;
				previoussaysomething = saysomethingaboutaisle.getText().toString();
				inputMethodManager.hideSoftInputFromWindow(saysomethingaboutaisle.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);
				return true;
			}
		});
		
		saysomethingaboutaisle.setonInterceptListen(new OnInterceptListener() {
			
			@Override
			public void onInterceptTouch() {
				// TODO Auto-generated method stub
				create_ailse_keyboard_hidden_shown_flag = false;
				inputMethodManager.hideSoftInputFromWindow(saysomethingaboutaisle.getWindowToken(), 0);
			saysomethingaboutaisle.setText(previoussaysomething);	
			}
		});
		
		saysomethingaboutaisle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				 create_ailse_keyboard_hidden_shown_flag = true;
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lookingfortext.setOnEditorActionListener(
		        new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				
				
				create_ailse_keyboard_hidden_shown_flag = false;
				
						lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);
					

						lookingforbigtext.setText(lookingfortext.getText()
								.toString());
						
						
						previouslookingfor = lookingfortext.getText().toString();
						
						lookingforpopup.setVisibility(View.GONE);
						inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);

						if(!dontgotonextforlookup)
						{
							create_ailse_keyboard_hidden_shown_flag = true;
						occassionbigtext.setBackgroundColor(getResources()
								.getColor(R.color.yellowbgcolor));
						
						ocassionpopup.setVisibility(View.VISIBLE);
						occasiontext.requestFocus();
						
						inputMethodManager.showSoftInput(occasiontext, 0);
						}
			            return true;
			       
			}
		});
		
		
		lookingfortext.setonInterceptListen(new OnInterceptListener() {
	          public void onInterceptTouch() {
	        	  create_ailse_keyboard_hidden_shown_flag = false;
	        	  lookingforpopup.setVisibility(View.GONE);
	 			 ocassionpopup.setVisibility(View.GONE);
	 			
	 			 lookingfortext.setText(previouslookingfor);
	 			 
	 			occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
	 			lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);
	 			inputMethodManager.hideSoftInputFromWindow(saysomethingaboutaisle.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);
	          }
	        });
		
		occasiontext.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
				// TODO Auto-generated method stub
				
				create_ailse_keyboard_hidden_shown_flag = false;
						inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
			
						occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
				         occassionbigtext.setText(" "+occasiontext.getText().toString());
				         previousocasion = occasiontext.getText().toString();
			            ocassionpopup.setVisibility(View.GONE);
			            
			            if(!dontgotonextforoccasion)
			            {
			            	create_ailse_keyboard_hidden_shown_flag = true;
			            categorylistview.setVisibility(View.VISIBLE);
						categorylistviewlayout.setVisibility(View.VISIBLE);
						categoerypopup.setVisibility(View.VISIBLE);
			            }
						
			            
			            return true;
			        
			};
		});
		
		
		occasiontext.setonInterceptListen(new OnInterceptListener() {
	          public void onInterceptTouch() {
	        	  create_ailse_keyboard_hidden_shown_flag = false;
	        	  
	        	  lookingforpopup.setVisibility(View.GONE);
	 			 ocassionpopup.setVisibility(View.GONE);
	 			occasiontext.setText(previousocasion);
	 			occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
	 			lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);

	 			inputMethodManager.hideSoftInputFromWindow(saysomethingaboutaisle.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
	 			inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);
	          }
	        });
		
		
		lookingforbigtext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				 create_ailse_keyboard_hidden_shown_flag = true;
			
				dontgotonextforlookup = true;
				
				 lookingforpopup.setVisibility(View.VISIBLE);
				
				occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
				lookingforbigtext.setBackgroundColor(getResources()
						.getColor(R.color.yellowbgcolor));

				  ocassionpopup.setVisibility(View.GONE);
				lookingfortext.requestFocus();
				inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
				inputMethodManager.showSoftInput(lookingfortext, 0);
				
				 categorylistview.setVisibility(View.GONE);
					categorylistviewlayout.setVisibility(View.GONE);
					categoerypopup.setVisibility(View.GONE);
				
			}
		});
		
		
	occassionbigtext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			
				 create_ailse_keyboard_hidden_shown_flag = true;
				
				dontgotonextforoccasion = true;
				
				 ocassionpopup.setVisibility(View.VISIBLE);
				
				  lookingforpopup.setVisibility(View.GONE);
				
				lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);
				occassionbigtext.setBackgroundColor(getResources()
						.getColor(R.color.yellowbgcolor));

			
				occasiontext.requestFocus();
				inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);
				inputMethodManager.showSoftInput(occasiontext, 0);
				
				 categorylistview.setVisibility(View.GONE);
					categorylistviewlayout.setVisibility(View.GONE);
					categoerypopup.setVisibility(View.GONE);
					
				
				
			}
		});
		
	
	categoeryicon.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			 lookingforpopup.setVisibility(View.GONE);
			 ocassionpopup.setVisibility(View.GONE);
			
			occassionbigtext.setBackgroundColor(Color.TRANSPARENT);
			lookingforbigtext.setBackgroundColor(Color.TRANSPARENT);

			inputMethodManager.hideSoftInputFromWindow(occasiontext.getWindowToken(), 0);
			inputMethodManager.hideSoftInputFromWindow(lookingfortext.getWindowToken(), 0);
			
			categorylistview.setVisibility(View.VISIBLE);
			categorylistviewlayout.setVisibility(View.VISIBLE);
			categoerypopup.setVisibility(View.VISIBLE);
		}
	});
	
	touchtochangeimage.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			 Intent intent = new Intent(getActivity(), CreateAisleSelectionActivity.class);
	         Bundle b = new Bundle();
	         b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
	         intent.putExtras(b);
			 getActivity().startActivityForResult(intent, VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
		}
	});
		
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
			
				if (position == categorycurrentselectedposition) {
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
						
						categorycurrentselectedposition = position;
						
						categorytext.setText(categoryitemsArray[position]);
						
						categorylistview.setVisibility(View.GONE);
						categoerypopup.setVisibility(View.GONE);
						categorylistviewlayout.setVisibility(View.GONE);
						
						
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

	
		public void setGalleryImage(String picturePath)
		{
			
			Log.e("CreateAilseActivty", "set gallery image called"+picturePath);
			
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
