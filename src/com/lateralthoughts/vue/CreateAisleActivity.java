package com.lateralthoughts.vue;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CreateAisleActivity extends /*BaseActivity*/ FragmentActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_aisle_main);
	}
	
	
	 @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.title_options, menu);
	    ImageView icon = (ImageView) findViewById(android.R.id.home);
	    icon.setOnClickListener(new OnClickListener() {

	      @Override
	      public void onClick(View arg0) {
	     //   getSlidingMenu().toggle();
	      }
	    });
	  
	    // Configure the search info and add any event listeners
	    return super.onCreateOptionsMenu(menu);
	  }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		// From Gallery...
		if(requestCode == VueConstants.SELECT_PICTURE)
		{
			/*Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
 
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();*/
			
			Uri selectedImageUri = data.getData();

			//OI FILE Manager
			String filemanagerstring = selectedImageUri.getPath();

			//MEDIA GALLERY
			String selectedImagePath = getPath(selectedImageUri);
             
            try {
            	CreateAilseFragment fragment = (CreateAilseFragment) getSupportFragmentManager().findFragmentById(R.id.create_aisles_view_fragment);
    			  
    			fragment.setGalleryImage(selectedImagePath);
    				
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
			
		}
		
		// From Camera...
		else if(requestCode == VueConstants.CAMERA_REQUEST)
		{
			CreateAilseFragment fragment = (CreateAilseFragment) getSupportFragmentManager().findFragmentById(R.id.create_aisles_view_fragment);
			
			 
	         fragment.setCameraImage();
		}
	}

	// Getting Image file path from URI.
	public String getPath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);

		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

}
