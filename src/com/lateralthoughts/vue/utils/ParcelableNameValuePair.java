package com.lateralthoughts.vue.utils;

import org.apache.http.NameValuePair;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNameValuePair implements NameValuePair, Parcelable {
    
    String mName, mValue;
    
    public ParcelableNameValuePair(String name, String value) {
        this.mName = name;
        this.mValue = value;
    }
    
    @Override
    public String getName() {
        return mName;
    }
    
    @Override
    public String getValue() {
        return mValue;
    }
    
    public int describeContents() {
        return 0;
    }
    
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeString(mValue);
    }
    
    public static final Parcelable.Creator<ParcelableNameValuePair> CREATOR = new Parcelable.Creator<ParcelableNameValuePair>() {
        public ParcelableNameValuePair createFromParcel(Parcel in) {
            return new ParcelableNameValuePair(in);
        }
        
        public ParcelableNameValuePair[] newArray(int size) {
            return new ParcelableNameValuePair[size];
        }
    };
    
    private ParcelableNameValuePair(Parcel in) {
        mName = in.readString();
        mValue = in.readString();
    }
    
}
