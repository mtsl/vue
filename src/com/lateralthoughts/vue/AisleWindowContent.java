package com.lateralthoughts.vue;

import android.widget.LinearLayout;

//Task for the queue
public class AisleWindowContent
{
    public String url;
    public LinearLayout windowContentRoot;
    public AisleWindowContent(String u, LinearLayout root){
        url = u; 
        windowContentRoot = root;
    }
}
