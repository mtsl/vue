package gcm.com.vue.android.gcmclient;

import android.content.Context;

import com.google.android.gcm.GCMRegistrar;

public class RegisterGCMClient {
    
    // This registerClient() method checks the current device, checks the
    // manifest for the appropriate rights, and then retrieves a registration id
    // from the GCM cloud. If there is no registration id, GCMRegistrar will
    // register this device for the specified project, which will return a
    // registration id.
    public static String registerClient(Context context, String projectId) {
        
        try {
            // Check that the device supports GCM (should be in a try / catch)
            GCMRegistrar.checkDevice(context);
            
            // Check the manifest to be sure this app has all the required
            // permissions.
            GCMRegistrar.checkManifest(context);
            
            // Get the existing registration id, if it exists.
            String regId = GCMRegistrar.getRegistrationId(context);
            
            if (regId.equals("")) {
                GCMRegistrar.register(context, projectId);
            }
            
            // Get the registration id
            regId = GCMRegistrar.getRegistrationId(context);
            return regId;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
