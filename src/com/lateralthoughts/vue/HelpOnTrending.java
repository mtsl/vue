package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

public class HelpOnTrending extends Activity {
    int mScreenWidth, mScreenHeight;
    FileCache mFileCache;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helpontrending);
        getActionBar().hide();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        mFileCache = new FileCache(this);
        ImageView imageHelp = (ImageView) findViewById(R.id.help_four);
        
        imageHelp.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                finish();
            }
            
            public void onSwipeRight() {
                finish();
            }
        });
        
        BitmapWorkerTask task = new BitmapWorkerTask(imageHelp);
        task.execute();
    }
    
    /**
     * Detects left and right swipes across a view.
     */
    public class OnSwipeTouchListener implements OnTouchListener {
        
        private final GestureDetector gestureDetector;
        
        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context,
                    new GestureListener());
        }
        
        public void onSwipeLeft() {
        }
        
        public void onSwipeRight() {
        }
        
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
        
        private final class GestureListener extends SimpleOnGestureListener {
            
            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                    float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY)
                        && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        
        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            File f = mFileCache.getHelpFile("imageFourNew");
            Bitmap bmp = decodeFile(f, mScreenHeight, mScreenWidth);
            if (bmp == null) {
                try {
                    int drawable = R.drawable.helpfour;
                    Bitmap largeIcon = BitmapFactory.decodeResource(
                            getResources(), drawable);
                    File f2 = mFileCache.getHelpFile("imageFourNew");
                    Utils.saveBitmap(largeIcon, f2);
                    largeIcon.recycle();
                    largeIcon = null;
                    bmp = decodeFile(f, mScreenHeight, mScreenWidth);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bmp;
        }
        
        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final ImageView imageView = imageViewReference.get();
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
