package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

public class Help extends Activity {
    ViewPager mHelpPager;
    private LayoutInflater mInflater;
    private int mHelpScreens;
    private String helpScreens[] = { "imageOne", "imageTwo", "imageThree" };
    FileCache mFileCache;
    int mScreenWidth, mScreenHeight, mCurrentPosition;
    String mFromWhere;
    boolean endReached = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helpscree);
        mHelpScreens = helpScreens.length;
        SharedPreferences sharedPreferencesObj = this.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        mFromWhere = getIntent().getStringExtra(VueConstants.HELP_KEY);
        mFileCache = new FileCache(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        mHelpPager = (ViewPager) findViewById(R.id.pager);
        HelpPagerAdapter helpAdapter = new HelpPagerAdapter();
        mHelpPager.setAdapter(helpAdapter);
        Editor editor = sharedPreferencesObj.edit();
        editor.putBoolean(VueConstants.HELP_SCREEN_ACCES, true);
        editor.commit();
        mHelpPager.setOnPageChangeListener(new OnPageChangeListener() {
            
            @Override
            public void onPageSelected(int arg0) {
                endReached = false;
                mCurrentPosition = arg0;
                
            }
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                
            }
            
            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
                    if (endReached) {
                        if (mCurrentPosition == mHelpScreens - 1
                                && !mFromWhere
                                        .equalsIgnoreCase(getString(R.string.frombezel))) {
                            finish();
                            Intent i = new Intent(Help.this,
                                    VueLoginActivity.class);
                            Bundle b = new Bundle();
                            b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG,
                                    false);
                            b.putString(VueConstants.FROM_INVITEFRIENDS, null);
                            b.putBoolean(
                                    VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
                                    false);
                            b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN,
                                    false);
                            i.putExtras(b);
                            startActivity(i);
                        }
                    } else {
                        endReached = true;
                    }
                }
                
            }
        });
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mFromWhere.equalsIgnoreCase(getString(R.string.frombezel))) {
                VueUser storedVueUser = null;
                try {
                    storedVueUser = Utils.readUserObjectFromFile(this,
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                if (storedVueUser != null) {
                    VueApplication.getInstance().setmUserInitials(
                            storedVueUser.getFirstName());
                    VueApplication.getInstance().setmUserId(
                            storedVueUser.getId());
                    VueApplication.getInstance().setmUserName(
                            storedVueUser.getFirstName() + " "
                                    + storedVueUser.getLastName());
                } else {
                    Intent i = new Intent(Help.this, VueLoginActivity.class);
                    Bundle b = new Bundle();
                    b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
                    b.putString(VueConstants.FROM_INVITEFRIENDS, null);
                    b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
                    b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
                    i.putExtras(b);
                    startActivity(i);
                }
                
            }
            
        }
        super.onBackPressed();
        return false;
    }
    
    private class HelpPagerAdapter extends PagerAdapter implements
            GestureDetector.OnGestureListener {
        
        /**
         * 
         * @param mContext
         *            Context
         */
        
        @Override
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((RelativeLayout) object);
        }
        
        @Override
        public void finishUpdate(View arg0) {
            
        }
        
        @Override
        public int getCount() {
            
            return mHelpScreens;
        }
        
        @Override
        public Object instantiateItem(View view, int position) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) view.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            View myView = mInflater.inflate(R.layout.help_inflater, null);
            ImageView helpImage = (ImageView) myView
                    .findViewById(R.id.pager_image);
            ProgressBar pb = (ProgressBar) myView
                    .findViewById(R.id.progressBar1);
            pb.setVisibility(View.GONE);
            helpImage.setVisibility(View.VISIBLE);
            setImageResource(position, helpImage, pb);
            ((ViewPager) view).addView(myView);
            return myView;
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            
        }
        
        @Override
        public Parcelable saveState() {
            return null;
        }
        
        @Override
        public void startUpdate(View arg0) {
            
        }
        
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            
            return false;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return false;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return false;
        }
        
        @Override
        public void onShowPress(MotionEvent e) {
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }
    
    private void setImageResource(int position, ImageView helpImage,
            ProgressBar progressBar) {
        
        BitmapWorkerTask task = new BitmapWorkerTask(helpImage, progressBar,
                position);
        task.execute();
    }
    
    private File drawableToBitmap(int position) {
        
        int drawable = 0;
        if (helpScreens[position].equalsIgnoreCase("imageOne")) {
            drawable = R.drawable.helpone;
        } else if (helpScreens[position].equalsIgnoreCase("imageTwo")) {
            drawable = R.drawable.helptwo;
        } else if (helpScreens[position].equalsIgnoreCase("imageThree")) {
            drawable = R.drawable.helpthree;
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                drawable);
        File f = mFileCache.getHelpFile(helpScreens[position]);
        Utils.saveBitmap(largeIcon, f);
        largeIcon.recycle();
        largeIcon = null;
        return f;
        
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarReference;
        int mCurrentPosition;
        
        public BitmapWorkerTask(ImageView imageView, ProgressBar progressBar,
                int currentPosition) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            progressBarReference = new WeakReference<ProgressBar>(progressBar);
            mCurrentPosition = currentPosition;
        }
        
        @Override
        protected void onPreExecute() {
            ProgressBar pb = progressBarReference.get();
            if (pb != null)
                pb.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            File f = mFileCache.getHelpFile(helpScreens[mCurrentPosition]);
            Bitmap bmp = decodeFile(f, mScreenHeight, mScreenWidth);
            if (bmp == null) {
                f = drawableToBitmap(mCurrentPosition);
                bmp = decodeFile(f, mScreenHeight, mScreenWidth);
            }
            return bmp;
        }
        
        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            
            final ImageView imageView = imageViewReference.get();
            
            ProgressBar pb = progressBarReference.get();
            if (pb != null) {
                pb.setVisibility(View.GONE);
            }
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            
        }
    }
    
    private Bitmap decodeFile(File f, int bestHeight, int bestWidth) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
            // Find the correct scale value. It should be the power of 2.
            int height = o.outHeight;
            int width = o.outWidth;
            int reqWidth = bestWidth;
            int scale = 1;
            
            if (height > bestHeight || width > bestWidth) {
                
                // Calculate ratios of height and width to requested height and
                // width
                final int heightRatio = (int) ((float) height / (float) bestHeight);
                final int widthRatio = (int) ((float) width / (float) reqWidth);
                
                // Choose the smallest ratio as inSampleSize value, this will
                // guarantee
                // a final image with both dimensions larger than or equal to
                // the
                // requested height and width.
                scale = heightRatio < widthRatio ? heightRatio : widthRatio;
                
            }
            
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            // o2.inSampleSize = o.inSampleSize;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            
            e.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }
}
