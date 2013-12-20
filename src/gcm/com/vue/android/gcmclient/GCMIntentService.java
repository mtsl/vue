package gcm.com.vue.android.gcmclient;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.lateralthoughts.vue.utils.UrlConstants;

public class GCMIntentService extends GCMBaseIntentService {
    
    public GCMIntentService() {
        super(UrlConstants.CURRENT_SERVER_PROJECT_ID);
    }
    
    @Override
    protected void onError(Context ctx, String sError) {
    }
    
    @Override
    protected void onMessage(Context ctx, Intent intent) {
        String message = intent.getStringExtra("data");
        sendGCMIntent(ctx, message);
    }
    
    private void sendGCMIntent(Context ctx, String message) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("GCM_RECEIVED_ACTION");
        broadcastIntent.putExtra("gcm", message);
        ctx.sendBroadcast(broadcastIntent);
    }
    
    @Override
    protected void onRegistered(Context ctx, String regId) {
        // send regId to your server
    }
    
    @Override
    protected void onUnregistered(Context ctx, String regId) {
        // send notification to your server to remove that regId
    }
    
}
