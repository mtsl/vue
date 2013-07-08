package com.lateralthoughts.vue.utils;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class SortBasedOnName implements Comparator
{
public int compare(Object o1, Object o2) 
{

    FbGPlusDetails dd1 = (FbGPlusDetails)o1;// where FbGPlusDetails is your object class
    FbGPlusDetails dd2 = (FbGPlusDetails)o2;
    return dd1.getName().compareToIgnoreCase(dd2.getName());//where uname is field name
}

}