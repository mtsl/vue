package gcm.com.vue.android.gcmclient;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gcm.GCMRegistrar;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.utils.UrlConstants;

public class RegisterGCMClient {
    
    // This registerClient() method checks the current device, checks the
    // manifest for the appropriate rights, and then retrieves a registration id
    // from the GCM cloud. If there is no registration id, GCMRegistrar will
    // register this device for the specified project, which will return a
    // registration id.
    public static void registerClient(Context context, String projectId) {
        
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
            if (regId != null && regId.trim().length() > 0
                    && projectId.equals(UrlConstants.CURRENT_SERVER_PROJECT_ID)) {
                SharedPreferences sharedPreferencesObj = VueApplication
                        .getInstance().getSharedPreferences(
                                VueConstants.SHAREDPREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferencesObj.edit();
                editor.putString(VueConstants.GCM_REGISTRATION_ID, regId);
                editor.commit();
            } else if (regId != null
                    && regId.trim().length() > 0
                    && projectId
                            .equals(VueApplication.MIXPANEL_SEREVER_PROJECT_ID)) {
                SharedPreferences sharedPreferencesObj = VueApplication
                        .getInstance().getSharedPreferences(
                                VueConstants.SHAREDPREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferencesObj.edit();
                editor.putString(VueConstants.MIXPANEL_REGISTRATION_ID, regId);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
