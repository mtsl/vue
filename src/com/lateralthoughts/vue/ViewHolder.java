package com.lateralthoughts.vue;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lateralthoughts.vue.ui.AisleContentBrowser;

public class ViewHolder {
    AisleContentBrowser aisleContentBrowser;
    TextView aisleOwnersName;
    TextView aisleContext;
    ImageView profileThumbnail;
    String uniqueContentId;
    LinearLayout aisleDescriptor;
    AisleWindowContent mWindowContent;
}