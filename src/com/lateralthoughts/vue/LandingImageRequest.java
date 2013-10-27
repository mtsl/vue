package com.lateralthoughts.vue;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

public class LandingImageRequest extends ImageRequest {
    private Config mLandingDecodeConfig = Bitmap.Config.ARGB_8888;
    public LandingImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
                        Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, decodeConfig, errorListener);
    }
}
