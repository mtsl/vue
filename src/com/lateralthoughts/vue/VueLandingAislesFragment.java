package com.lateralthoughts.vue;

//generic android & java goodies
import android.os.ResultReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

//java utils
import java.util.ArrayList;

//goodies from support libs
import android.support.v4.widget.StaggeredGridView;

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends Fragment {
	private Context mContext;
	private VueContentGateway mVueContentGateway;
	
	
	private Handler mResultHandler;
	private StaggeredGridView mTrendingAislesContentView;
	private TrendingAislesAdapter mContentAdapter;
	
	private int mOffset = 0;
	//TODO: define a public interface that can be implemented by the parent
	//activity so that we can notify it with an ArrayList of AisleWindowContent
	//once we have received the result and parsed it. The idea is that the activity
	//can then initiate a worker in the background to go fetch more content and get
	//ready to launch other activities/fragments within the application
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		mContext = activity;
		mResultHandler = new Handler();
		mTrendingAislesContentView = null;
		
		
		//without much ado lets get started with retrieving the trending aisles list
		mVueContentGateway = VueContentGateway.getInstance();
		if(null == mVueContentGateway){
			//assert here: this is a no go!
		}		

		int limit = 5;
		mOffset = 0;
		mContentAdapter = new TrendingAislesAdapter(mContext, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		//TODO: any particular state that we want to restore?
		
	}

	private String urls[] = { 
			"http://farm7.staticflickr.com/6101/6853156632_6374976d38_c.jpg",
			"http://farm8.staticflickr.com/7232/6913504132_a0fce67a0e_c.jpg",
			"http://farm5.staticflickr.com/4133/5096108108_df62764fcc_b.jpg",
			"http://farm5.staticflickr.com/4074/4789681330_2e30dfcacb_b.jpg",
			"http://farm9.staticflickr.com/8208/8219397252_a04e2184b2.jpg",
			"http://farm9.staticflickr.com/8483/8218023445_02037c8fda.jpg",
			"http://farm9.staticflickr.com/8335/8144074340_38a4c622ab.jpg",
			"http://farm9.staticflickr.com/8060/8173387478_a117990661.jpg",
			"http://farm9.staticflickr.com/8056/8144042175_28c3564cd3.jpg",
			"http://farm9.staticflickr.com/8183/8088373701_c9281fc202.jpg",
			"http://farm9.staticflickr.com/8185/8081514424_270630b7a5.jpg",
			"http://farm9.staticflickr.com/8462/8005636463_0cb4ea6be2.jpg",
			"http://farm9.staticflickr.com/8306/7987149886_6535bf7055.jpg",
			"http://farm9.staticflickr.com/8444/7947923460_18ffdce3a5.jpg",
			"http://farm9.staticflickr.com/8182/7941954368_3c88ba4a28.jpg",
			"http://farm9.staticflickr.com/8304/7832284992_244762c43d.jpg",
			"http://farm9.staticflickr.com/8163/7709112696_3c7149a90a.jpg",
			"http://farm8.staticflickr.com/7127/7675112872_e92b1dbe35.jpg",
			"http://farm8.staticflickr.com/7111/7429651528_a23ebb0b8c.jpg",
			"http://farm9.staticflickr.com/8288/7525381378_aa2917fa0e.jpg",
			"http://farm6.staticflickr.com/5336/7384863678_5ef87814fe.jpg",
			"http://farm8.staticflickr.com/7102/7179457127_36e1cbaab7.jpg",
			"http://farm8.staticflickr.com/7086/7238812536_1334d78c05.jpg",
			"http://farm8.staticflickr.com/7243/7193236466_33a37765a4.jpg",
			"http://farm8.staticflickr.com/7251/7059629417_e0e96a4c46.jpg",
			"http://farm8.staticflickr.com/7084/6885444694_6272874cfc.jpg"
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.aisles_view_fragment, container, false);
		mTrendingAislesContentView = (StaggeredGridView) v.findViewById(R.id.aisles_grid);
		
		int margin = getResources().getDimensionPixelSize(R.dimen.margin);
		
		mTrendingAislesContentView.setItemMargin(margin); // set the GridView margin
		
		mTrendingAislesContentView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 
		
		//StaggeredViewAdapter adapter = new StaggeredViewAdapter(mContext, R.id.imageView1, urls);
		 
		
		mTrendingAislesContentView.setAdapter(mContentAdapter);
		mContentAdapter.notifyDataSetChanged();
        return v;		
	}

}
