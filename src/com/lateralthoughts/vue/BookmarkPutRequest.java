package com.lateralthoughts.vue;

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
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpHeaderParser;

public class BookmarkPutRequest extends Request<String> {
  // ... other methods go here
  private Map<String, String> mParams;
  Response.Listener<String> mListener;
  private String mAisleAsString;
  private StringEntity mEntity;

  public BookmarkPutRequest(String aisleAsString,
      Response.Listener<String> listener,
      Response.ErrorListener errorListener, String url) {
    super(Method.PUT, url, errorListener);
    mListener = listener;
    mAisleAsString = aisleAsString;
    try {
      mEntity = new StringEntity(mAisleAsString);
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
}