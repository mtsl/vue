package com.lateralthoughts.vue;

//generic android & java goodies
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.widget.ListView;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueAisleDetailsViewFragment extends Fragment {
    private Context mContext;
    private VueContentGateway mVueContentGateway;
    private AisleDetailsViewAdapter mAisleDetailsAdapter;   
    private ListView mAisleDetailsList;

    //TODO: define a public interface that can be implemented by the parent
    //activity so that we can notify it with an ArrayList of AisleWindowContent
    //once we have received the result and parsed it. The idea is that the activity
    //can then initiate a worker in the background to go fetch more content and get
    //ready to launch other activities/fragments within the application
    
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = activity;
        
        //without much ado lets get started with retrieving the trending aisles list
        mVueContentGateway = VueContentGateway.getInstance();
        if(null == mVueContentGateway){
            //assert here: this is a no go!
        }       

        mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext, null);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //TODO: any particular state that we want to restore?
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.aisles_detailed_view_fragment, container, false);
        
        mAisleDetailsList = (ListView)v.findViewById(R.id.aisle_details_list);  
        mAisleDetailsList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
        mAisleDetailsAdapter.notifyDataSetChanged();
        
        /*mAisleDetailsList.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.e("VinoTouchFragment","onClick in fragment. Id = " + v.getId());
                
            }
        });*/
        Log.d("VueAisleDetailsViewFragment","Get ready to display details view");
        return v;
    }
}
