/**
 * A simple rest client implemented as a service. Interact with the service
 * by sending in intents that contain the header and parameters for the
 * REST end point. Also, pass in a result receiver to be notified once the
 * API returns. 
 */
package com.lateralthoughts.vue.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.utils.ParcelableNameValuePair;

public class VueContentRestService extends IntentService {

	public static final String PARAMS_FIELD = "params";
	public static final String HEADERS_FIELD = "headers";
	public static final String RECEIVER_FIELD = "receiver";
	public static final String URL_FIELD = "url";
	public static final String BATCH_DATA_FLAG = "BATCH_DATA";
	public static final String LIMIT_DATA_FIELD = "LIMIT_DATA";
	public static final String BATCH_SIZE_FIELD = "BATCH_SIZE";
	public static final String STARTING_OFFSET_FIELD = "STARTING_OFFSET";
	private HttpClient mHttpClient;
    public VueContentRestService(){
        super("VueContentRestService");
        Log.e("VueContentRestService", "network connection VueContentRestService()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        VueApplication app = VueApplication.getInstance();
        mHttpClient = app.getHttpClient();
        //mHttpClient = new DefaultHttpClient();
        Log.e("VueContentRestService", "network connection onStartCommand()");
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent){
    	if(null == intent)
    		return;
    	 if(!VueConnectivityManager.isNetworkConnected(VueApplication.getInstance())) {
           Log.e("VueContentRestService", "network connection No");
           return;
         }
        mParams = intent.getParcelableArrayListExtra(PARAMS_FIELD);
        mHeaders = intent.getParcelableArrayListExtra(HEADERS_FIELD);
        mUrl = intent.getStringExtra(URL_FIELD);
        mReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_FIELD);
        try{
            go();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void go(){
        String consolidatedParams = "";
        if(!mParams.isEmpty()){
            consolidatedParams += "?";
            for(ParcelableNameValuePair p : mParams)
            {
                String paramString = null;
                try{
                    paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                }   catch(UnsupportedEncodingException encode_ex){
                    encode_ex.printStackTrace();
                }
                if(consolidatedParams.length() > 1)
                {
                    consolidatedParams  +=  "&" + paramString;
                }
                else
                {
                    consolidatedParams += paramString;
                }
            }
        }

        mRequest = new HttpGet(mUrl + consolidatedParams);

        //add headers
        for(ParcelableNameValuePair h : mHeaders){
            mRequest.addHeader(h.getName(), h.getValue());
        }
        executeRequest();
    }

    private void executeRequest(){
        
        HttpResponse httpResponse;

        try {
            httpResponse = mHttpClient.execute(mRequest);

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                String response = convertStreamToString(instream);
                Bundle responseBundle = new Bundle();
                responseBundle.putString("result", response);
                mReceiver.send(1, responseBundle);
                instream.close();
            }else{
                Log.e("VueTrendingAislesDataModel","looks like we don't have anything more?");
            }

        } catch (ClientProtocolException e)  {
            mHttpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            mHttpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        }

    }
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    ArrayList <ParcelableNameValuePair> mParams;
    ArrayList <ParcelableNameValuePair> mHeaders;
    private String mUrl;
    private ResultReceiver mReceiver;
    
    //private int mOffset;
    //private int mLimit;
    //private int mBatchSize;

    //http related objects that we need for the service
    HttpRequestBase mRequest;

}

