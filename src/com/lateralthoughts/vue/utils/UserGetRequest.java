package com.lateralthoughts.vue.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

public class UserGetRequest extends Request<String> {
    // ... other methods go here
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;
    
    public UserGetRequest(String url, Response.Listener<String> listener,
            Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
        mErrorListener = errorListener;
        
    }
    
    @Override
    public Map<String, String> getHeaders() {
        HashMap<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("Content-Type", "application/json");
        return headersMap;
    }
    
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }
    
    @Override
    protected void deliverResponse(String s) {
        mListener.onResponse(s);
    }
    
    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
    
}
