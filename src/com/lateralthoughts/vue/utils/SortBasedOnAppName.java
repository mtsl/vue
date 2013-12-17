package com.lateralthoughts.vue.utils;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class SortBasedOnAppName implements Comparator {
    public int compare(Object o1, Object o2) {
        ShoppingApplicationDetails dd1 = (ShoppingApplicationDetails) o1;
        ShoppingApplicationDetails dd2 = (ShoppingApplicationDetails) o2;
        return dd1.getAppName().compareToIgnoreCase(dd2.getAppName());
    }
    
}