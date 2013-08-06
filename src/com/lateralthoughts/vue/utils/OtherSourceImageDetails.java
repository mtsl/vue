package com.lateralthoughts.vue.utils;

import android.net.Uri;

public class OtherSourceImageDetails {
	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOriginUrl() {
		return originUrl;
	}

	public void setOriginUrl(String originUrl) {
		this.originUrl = originUrl;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public OtherSourceImageDetails(String thumbUrl, String title,
			String originUrl, int width, int height, Uri imageUri) {
		this.thumbUrl = thumbUrl;
		this.title = title;
		this.originUrl = originUrl;
		this.width = width;
		this.height = height;
		this.imageUri = imageUri;
	}

	public OtherSourceImageDetails() {

	}

	private String thumbUrl;
	private String title;
	private String originUrl;
	private int width;
	private int height;
	private Uri imageUri;

	public Uri getImageUri() {
		return imageUri;
	}

	public void setImageUri(Uri imageUri) {
		this.imageUri = imageUri;
	}

}