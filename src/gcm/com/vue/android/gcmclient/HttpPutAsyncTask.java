package gcm.com.vue.android.gcmclient;

import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;

public class HttpPutAsyncTask extends AsyncTask<String, Boolean, String> {
    
    private String doPUT(String urlString, String data) {
        try {
            URL url = new URL(urlString);
            
            HttpPut httpPut = new HttpPut(url.toString());
            StringEntity entity = new StringEntity(data);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httpPut.setEntity(entity);
            
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity1 = response.getEntity();
            
            if (entity1 != null
                    && (response.getStatusLine().getStatusCode() == 201 || response
                            .getStatusLine().getStatusCode() == 200)) {
                String sl = response.getStatusLine().getReasonPhrase();
                return sl;
            } else {
                String sl = response.getStatusLine().getReasonPhrase();
                return sl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    
    @Override
    protected String doInBackground(String... params) {
        String data = doPUT(params[0], params[1]);
        return data;
    }
    
    protected void onPostExecute(String result) {
    }
}
