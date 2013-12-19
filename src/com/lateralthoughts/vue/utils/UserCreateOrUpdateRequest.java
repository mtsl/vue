package com.lateralthoughts.vue.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

public class UserCreateOrUpdateRequest extends Request<String> {
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;
    private String muserAsString;
    private StringEntity mEntity;
    
    public UserCreateOrUpdateRequest(String userAsString, String url,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener) {
        super(Method.PUT, url, errorListener);
        mListener = listener;
        mErrorListener = errorListener;
        muserAsString = userAsString;
        try {
            mEntity = new StringEntity(muserAsString);
        } catch (UnsupportedEncodingException ex) {
        }
    }
    
    @Override
    public String getBodyContentType() {
        return mEntity.getContentType().getValue();
    }
    
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
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