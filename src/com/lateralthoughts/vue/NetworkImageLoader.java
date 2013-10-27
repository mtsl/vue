/**
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lateralthoughts.vue;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Helper that handles loading and caching images from remote URLs.
 *
 * The simple way to use this class is to call {@link ImageLoader#get(String, ImageListener)}
 * and to pass in the default image listener provided by
 * {@link ImageLoader#getImageListener(android.widget.ImageView, int, int)}. Note that all function calls to
 * this class must be made from the main thead, and all responses will be delivered to the main
 * thread as well.
 */
public class NetworkImageLoader extends ImageLoader {
    /** RequestQueue for dispatching ImageRequests onto. */
    private final RequestQueue mRequestQueue;

    /** Amount of time to wait after first response arrives before delivering all responses. */
    private int mBatchResponseDelayMs = 100;

    /** The cache implementation to be used as an L1 cache before calling into volley. */
    private final ImageCache mCache;
    protected static final long ANIMATION_DURATION_MS = 100;
    private static Resources mResources;

    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight requests so
     * that we can coalesce multiple requests to the same URL into a single network request.
     */
    private final HashMap<String, BatchedImageRequest> mInFlightRequests =
            new HashMap<String, BatchedImageRequest>();

    /** HashMap of the currently pending responses (waiting to be delivered). */
    private final HashMap<String, BatchedImageRequest> mBatchedResponses =
            new HashMap<String, BatchedImageRequest>();

    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Runnable for in-flight response delivery. */
    private Runnable mRunnable;

    private FileCache mFileCache;

    /**
     * Constructs a new ImageLoader.
     * @param queue The RequestQueue to use for making image requests.
     * @param imageCache The cache to use as an L1 cache.
     */
    public NetworkImageLoader(RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
        mRequestQueue = queue;
        mCache = imageCache;
        mFileCache = VueApplication.getInstance().getFileCache();
    }

    /**
     * Checks if the item is available in the cache.
     * @param requestUrl The url of the remote image
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return True if the item exists in cache, false otherwise.
     */
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        throwIfNotOnMainThread();

        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
        return mCache.getBitmap(cacheKey) != null;
    }

    /**
     * Returns an ImageContainer for the requested URL.
     *
     * The ImageContainer will contain either the specified default bitmap or the loaded bitmap.
     * If the default was returned, the {@link ImageLoader} will be invoked when the
     * request is fulfilled.
     *
     * @param requestUrl The URL of the image to be loaded.
     */
    @Override
    public ImageContainer get(String requestUrl, final ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return A container object that contains all of the properties of the request, as well as
     *     the currently available image (default if remote is not loaded).
     */
    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new NetworkImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true, true);
            return container;
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new NetworkImageContainer(null, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true, false);

        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the network and
        // track it.
        Request<?> newRequest =
                new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                }, maxWidth, maxHeight,
                        Config.ARGB_8888, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onGetImageError(cacheKey, error);
                    }
                });

        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey,
                new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param bitmap The bitmap that was returned from the network.
     */
    private void onGetImageSuccess(String cacheKey, Bitmap bitmap) {

        mCache.putBitmap(cacheKey, bitmap);

        // remove the request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseBitmap = bitmap;

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     * @param cacheKey The cache key that is associated with the image request.
     */
    private void onGetImageError(String cacheKey, VolleyError error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        // Set the error for this request
        request.setError(error);

        if (request != null) {
            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    private class NetworkImageContainer implements ImageContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        private Bitmap mBitmap;

        private final ImageListener mListener;

        /** The cache key that was associated with the request */
        private final String mCacheKey;

        /** The request URL that was specified */
        private final String mRequestUrl;

        /**
         * Constructs a BitmapContainer object.
         * @param bitmap The final bitmap (if it exists).
         * @param requestUrl The requested URL for this container.
         * @param cacheKey The cache key that identifies the requested URL for this container.
         */
        public NetworkImageContainer(Bitmap bitmap, String requestUrl,
                              String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
                request = mBatchedResponses.get(mCacheKey);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null otherwise.
         */
        public Bitmap getBitmap() {
            return mBitmap;
        }

        /**
         * Returns the requested URL for this container.
         */
        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer objects that are
     * interested in its results.
     */
    private class BatchedImageRequest {
        /** The request being tracked */
        private final Request<?> mRequest;

        /** The result of the request being tracked by this item */
        private Bitmap mResponseBitmap;

        /** Error if one occurred for this response */
        private VolleyError mError;

        /** List of all of the active ImageContainers that are interested in the request */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();

        /**
         * Constructs a new BatchedImageRequest object
         * @param request The request being tracked
         * @param container The ImageContainer of the person who initiated the request.
         */
        public BatchedImageRequest(Request<?> request, ImageContainer container) {
            mRequest = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(VolleyError error) {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public VolleyError getError() {
            return mError;
        }

        /**
         * Adds another ImageContainer to the list of those interested in the results of
         * the request.
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }

        /**
         * Detatches the bitmap container from the request and cancels the request if no one is
         * left listening.
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }

    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     * @param cacheKey The cacheKey of the response being delivered.
     * @param request The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedImageRequest request) {
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedImageRequest bir : mBatchedResponses.values()) {
                        for (ImageContainer container : bir.mContainers) {
                            // If one of the callers in the batched request canceled the request
                            // after the response was received but before it was delivered,
                            // skip them.
                            if (((NetworkImageContainer)container).mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                ((NetworkImageContainer)container).mBitmap = bir.mResponseBitmap;
                                ((NetworkImageContainer)container).mListener.onResponse(container, false, false);
                            } else {
                                ((NetworkImageContainer)container).mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    mBatchedResponses.clear();
                    mRunnable = null;
                }

            };
            // Post the runnable.
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }
    /**
     * Creates a cache key for use with the L1 cache.
     * @param url The URL of the request.
     * @param maxWidth The max-width of the output.
     * @param maxHeight The max-height of the output.
     */
    private static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString();
    }


    public Bitmap getBitmap(String url, String serverUrl, boolean cacheBitmap,
                            int bestHeight, int bestWidth, String source) {

        File f = mFileCache.getFile(url);

        // from SD cache
        Bitmap b = decodeFile(f, bestHeight, bestWidth, source);
        if (b != null) {
            return b;
        }

        // from web
        try {
            if (serverUrl == null || serverUrl.length() < 1) {

                return null;
            }
            Bitmap bitmap = null;
            URL imageUrl = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            int hashCode = url.hashCode();
            String filename = String.valueOf(hashCode);
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, bestHeight, bestWidth, source);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError) {
                // mAisleImagesCache.evictAll();
            }
            return null;
        }
    }

    // decodes image and scales it to reduce memory consumption
    public Bitmap decodeFile(File f, int bestHeight, int bestWidth,
                             String source) {
        Log.i("added url", "added url in  decodeFile: bestheight is "
                + bestHeight);

        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
            // Find the correct scale value. It should be the power of 2.
            // final int REQUIRED_SIZE = mScreenWidth/2;
            int height = o.outHeight;
            int width = o.outWidth;

            int reqWidth = bestWidth;
            int scale = 1;

            if (height > bestHeight) {

                // Calculate ratios of height and width to requested height and
                // width
                final int heightRatio =  (int) ((float) height
                        / (float) bestHeight);
                final int widthRatio =(int)((float) width
                        / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will
                // guarantee
                // a final image with both dimensions larger than or equal to
                // the
                // requested height and width.
                scale = heightRatio; // < widthRatio ? heightRatio : widthRatio;

            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            Log.i("imagenotshowing", "imagenotshowing: Not   found locally bitmap "+bitmap);
            Log.i("imagenotshowing", "imagenotshowing: Not   found locally bitmap "+bitmap.getHeight());
            stream2.close();
            if(source.equalsIgnoreCase(Utils.TRENDING_SCREEN)){
                return getBitmap(bitmap,bestWidth,bestHeight);
            }
            if (source.equalsIgnoreCase(Utils.DETAILS_SCREEN)) {
                // scaling factor considers only integers may be some times
                // scaling factor becomes 1 even there is slight difference in
                // sizes
                // so to avoid croping in that cases in Detailsview screen
                // compare sizes after scaling.
                if (bitmap != null) {
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    if (height > bestHeight) {
                        float tempWidth = (width * bestHeight) / height;
                        width = (int) tempWidth;
                        bitmap = getModifiedBitmap(bitmap, width, bestHeight);
                    }
                }
            }

            if (bitmap != null) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();

                if (width > reqWidth) {
                    float tempHeight = (height * reqWidth) / width;
                    height = (int) tempHeight;
                    bitmap = getModifiedBitmap(bitmap, reqWidth, height);
                }
            }
            Log.i("window",
                    "clickedwindow ID  new bitmap height2 : "
                            + bitmap.getHeight());
            Log.i("window",
                    "clickedwindow ID new bitmap widht2: " + bitmap.getWidth());
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.i("added url", "added urldecodeFile  filenotfound exception ");
        } catch (IOException e) {

            e.printStackTrace();
        } catch (Throwable ex) {

            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError) {
            }
            return null;
        }
        return null;
    }

    public Bitmap getBitmap(Bitmap bitmap, int bestWidth, int bestHeight) {
        return getModifiedBitmap(bitmap,bestWidth,bestHeight);
    }

    private Bitmap getModifiedBitmap(Bitmap originalImage, int width, int height) {
        // here width & height are the desired width & height values)

        // first lets create a new bitmap and a canvas to draw into it.
        Bitmap newBitmap = Bitmap.createBitmap((int) width, (int) height,
                Config.ARGB_8888);
        float originalWidth = originalImage.getWidth(), originalHeight = originalImage
                .getHeight();
        Canvas canvas = new Canvas(newBitmap);
        float scale = width / originalWidth;
        float xTranslation = 0.0f, yTranslation = (height - originalHeight
                * scale) / 2.0f;
        Matrix transformation = new Matrix();
        // now that we have the transformations, set that for our drawing ops
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        // create a paint and draw into new canvas
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalImage, transformation, paint);
        originalImage.recycle();
        Log.i("imagenotcoming",
                "bitmap issue scalleddown: originalbitmap width "
                        + newBitmap.getWidth());
        Log.i("imagenotcoming",
                "bitmap issue:scalleddown originalbitmap height:  "
                        + newBitmap.getHeight());
        return newBitmap;
    }
}
