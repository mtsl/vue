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

import java.util.HashMap;
import java.util.LinkedList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Logging;

/**
 * Helper that handles loading and caching images from remote URLs.
 * 
 * The simple way to use this class is to call
 * {@link ImageLoader#get(String, ImageListener)} and to pass in the default
 * image listener provided by {@link ImageLoader (android.widget.ImageView, int,
 * int)}. Note that all function calls to this class must be made from the main
 * thead, and all responses will be delivered to the main thread as well.
 */
public class NetworkImageLoader extends ImageLoader {
    /** RequestQueue for dispatching ImageRequests onto. */
    private final RequestQueue mRequestQueue;
    
    /**
     * Amount of time to wait after first response arrives before delivering all
     * responses.
     */
    private int mBatchResponseDelayMs = 100;
    
    /**
     * The cache implementation to be used as an L1 cache before calling into
     * volley.
     */
    private final ImageCache mCache;
    protected static final long ANIMATION_DURATION_MS = 100;
    private static Resources mResources;
    
    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight
     * requests so that we can coalesce multiple requests to the same URL into a
     * single network request.
     */
    private final HashMap<String, BatchedImageRequest> mInFlightRequests = new HashMap<String, BatchedImageRequest>();
    
    /** HashMap of the currently pending responses (waiting to be delivered). */
    private final HashMap<String, BatchedImageRequest> mBatchedResponses = new HashMap<String, BatchedImageRequest>();
    
    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    
    /** Runnable for in-flight response delivery. */
    private Runnable mRunnable;
    
    private FileCache mFileCache;
    
    /**
     * Constructs a new ImageLoader.
     * 
     * @param queue
     *            The RequestQueue to use for making image requests.
     * @param imageCache
     *            The cache to use as an L1 cache.
     */
    public NetworkImageLoader(RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
        mRequestQueue = queue;
        mCache = imageCache;
        mFileCache = VueApplication.getInstance().getFileCache();
    }
    
    /**
     * Checks if the item is available in the cache.
     * 
     * @param requestUrl
     *            The url of the remote image
     * @param maxWidth
     *            The maximum width of the returned image.
     * @param maxHeight
     *            The maximum height of the returned image.
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
     * The ImageContainer will contain either the specified default bitmap or
     * the loaded bitmap. If the default was returned, the {@link ImageLoader}
     * will be invoked when the request is fulfilled.
     * 
     * @param requestUrl
     *            The URL of the image to be loaded.
     */
    @Override
    public ImageContainer get(String requestUrl, final ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }
    
    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the
     * data relating to the request (as well as the default image if the
     * requested image is not available).
     * 
     * @param requestUrl
     *            The url of the remote image
     * @param imageListener
     *            The listener to call when the remote image is loaded
     * @param maxWidth
     *            The maximum width of the returned image.
     * @param maxHeight
     *            The maximum height of the returned image.
     * @return A container object that contains all of the properties of the
     *         request, as well as the currently available image (default if
     *         remote is not loaded).
     */
    public ImageContainer get(final String requestUrl,
            ImageListener imageListener, int maxWidth, int maxHeight) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();
        
        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
        
        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new NetworkImageContainer(cachedBitmap,
                    requestUrl, null, null);
            imageListener.onResponse(container, true, true);
            return container;
        }
        
        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer = new NetworkImageContainer(null,
                requestUrl, cacheKey, imageListener);
        
        // Update the caller to let them know that they should use the default
        // bitmap.
        imageListener.onResponse(imageContainer, true, false);
        
        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }
        // The request is not already in flight. Send the new request to the
        // network and
        // track it.
        Request<?> newRequest = new ImageRequest(requestUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                }, maxWidth, maxHeight, Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logging.e("TrendingImageLoad",
                                " Image loading failed for url = " + requestUrl
                                        + " error = " + error.getMessage());
                        onGetImageError(cacheKey, error);
                    }
                });
        
        mRequestQueue.add(newRequest);
        newRequest.setTag(new String("FetchImageRequest"));
        mInFlightRequests.put(cacheKey, new BatchedImageRequest(newRequest,
                imageContainer));
        return imageContainer;
    }
    
    /**
     * Sets the amount of time to wait after the first response arrives before
     * delivering all responses. Batching can be disabled entirely by passing in
     * 0.
     * 
     * @param newBatchedResponseDelayMs
     *            The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }
    
    /**
     * Handler for when an image was successfully loaded.
     * 
     * @param cacheKey
     *            The cache key that is associated with the image request.
     * @param bitmap
     *            The bitmap that was returned from the network.
     */
    private void onGetImageSuccess(String cacheKey, Bitmap bitmap) {
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
     * 
     * @param cacheKey
     *            The cache key that is associated with the image request.
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
         * The most relevant bitmap for the container. If the image was in
         * cache, the Holder to use for the final bitmap (the one that pairs to
         * the requested URL).
         */
        private Bitmap mBitmap;
        
        private final ImageListener mListener;
        
        /** The cache key that was associated with the request */
        private final String mCacheKey;
        
        /** The request URL that was specified */
        private final String mRequestUrl;
        
        /**
         * Constructs a BitmapContainer object.
         * 
         * @param bitmap
         *            The final bitmap (if it exists).
         * @param requestUrl
         *            The requested URL for this container.
         * @param cacheKey
         *            The cache key that identifies the requested URL for this
         *            container.
         */
        public NetworkImageContainer(Bitmap bitmap, String requestUrl,
                String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }
        
        /**
         * Releases interest in the in-flight request (and cancels it if no one
         * else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }
            
            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request
                        .removeContainerAndCancelIfNecessary(this);
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
         * Returns the bitmap associated with the request URL if it has been
         * loaded, null otherwise.
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
     * Wrapper class used to map a Request to the set of active ImageContainer
     * objects that are interested in its results.
     */
    private class BatchedImageRequest {
        /** The request being tracked */
        private final Request<?> mRequest;
        
        /** The result of the request being tracked by this item */
        private Bitmap mResponseBitmap;
        
        /** Error if one occurred for this response */
        private VolleyError mError;
        
        /**
         * List of all of the active ImageContainers that are interested in the
         * request
         */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();
        
        /**
         * Constructs a new BatchedImageRequest object
         * 
         * @param request
         *            The request being tracked
         * @param container
         *            The ImageContainer of the person who initiated the
         *            request.
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
         * Adds another ImageContainer to the list of those interested in the
         * results of the request.
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }
        
        /**
         * Detatches the bitmap container from the request and cancels the
         * request if no one is left listening.
         * 
         * @param container
         *            The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(
                ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }
    
    /**
     * Starts the runnable for batched delivery of responses if it is not
     * already started.
     * 
     * @param cacheKey
     *            The cacheKey of the response being delivered.
     * @param request
     *            The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedImageRequest request) {
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a
        // new one.
        // Note that this will be used to deliver responses to all callers in
        // mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedImageRequest bir : mBatchedResponses.values()) {
                        for (ImageContainer container : bir.mContainers) {
                            // If one of the callers in the batched request
                            // canceled the request
                            // after the response was received but before it was
                            // delivered,
                            // skip them.
                            if (((NetworkImageContainer) container).mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                ((NetworkImageContainer) container).mBitmap = bir.mResponseBitmap;
                                ((NetworkImageContainer) container).mListener
                                        .onResponse(container, false, false);
                            } else {
                                ((NetworkImageContainer) container).mListener
                                        .onErrorResponse(bir.getError());
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
            throw new IllegalStateException(
                    "ImageLoader must be invoked from the main thread.");
        }
    }
    
    @Override
    public void putBitmap(String cacheKey, Bitmap bitmap) {
        mCache.putBitmap(cacheKey, bitmap);
    }
}
