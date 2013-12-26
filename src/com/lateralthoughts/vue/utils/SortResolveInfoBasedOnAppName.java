package com.lateralthoughts.vue.utils;

import java.util.Comparator;

import android.content.pm.ResolveInfo;

import com.lateralthoughts.vue.VueApplication;

@SuppressWarnings("rawtypes")
public class SortResolveInfoBasedOnAppName implements Comparator {
    public int compare(Object o1, Object o2) {
        ResolveInfo dd1 = (ResolveInfo) o1;
        ResolveInfo dd2 = (ResolveInfo) o2;
        return dd1.activityInfo.applicationInfo
                .loadLabel(VueApplication.getInstance().getPackageManager())
                .toString()
                .compareToIgnoreCase(
                        dd2.activityInfo.applicationInfo.loadLabel(
                                VueApplication.getInstance()
                                        .getPackageManager()).toString());
    }
}
