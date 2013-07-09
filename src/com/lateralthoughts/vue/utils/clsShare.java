package com.lateralthoughts.vue.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class clsShare implements Parcelable{
	
	
	 @SuppressWarnings("rawtypes")
		public static final Parcelable.Creator CREATOR =
			new Parcelable.Creator() {
		        @Override
				public clsShare createFromParcel(Parcel in) {
		            return new clsShare(in);
		        }
		
		        @Override
				public clsShare[] newArray(int size) {
		            return new clsShare[size];
		        }
		    };
	
		    public clsShare(Parcel in)
		    {
		    	readFromParcel(in);
		    }
		    
		    

	private void readFromParcel(Parcel in) {

		imageUrl = in.readString();
		filepath = in.readString();

	}
	
private String imageUrl;
public clsShare(String imageUrl, String filepath) {
	this.imageUrl = imageUrl;
	this.filepath = filepath;
}
public String getImageUrl() {
	return imageUrl;
}
public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
}

private String filepath;
public String getFilepath() {
	return filepath;
}



public void setFilepath(String filepath) {
	this.filepath = filepath;
}



@Override
public int describeContents() {
	// TODO Auto-generated method stub
	return 0;
}
@Override
public void writeToParcel(Parcel dest, int flags) {
	// TODO Auto-generated method stub
	
	dest.writeString(imageUrl);
    dest.writeString(filepath);
}
}
