package com.lateralthoughts.vue;

//generic android goodies
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

public class AisleDetailsViewActivity extends FragmentActivity {
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.aisle_details_activity_landing);
        getActionBar().hide();
      
    }
    
    @Override
    public void onResume(){
        super.onResume();
    }
    
    @Override
    public void onPause(){
        super.onPause();
        
    }
    
      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
      }
     
      /*@Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.title_options, menu);
          // Configure the search info and add any event listeners
          return super.onCreateOptionsMenu(menu);
      }*/
}
