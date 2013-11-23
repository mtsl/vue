package com.lateralthoughts.vue;

import java.io.Serializable;

public class DataentryImage implements Serializable {
	public String getResizedImagePath() {
		return resizedImagePath;
	}

	public void setResizedImagePath(String imagePath) {
		this.resizedImagePath = imagePath;
	}

	private String resizedImagePath;
	private String detailsUrl; // findAt...

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getOriginalImagePath() {
		return originalImagePath;
	}

	public void setOriginalImagePath(String originalImagePath) {
		this.originalImagePath = originalImagePath;
	}

	private String originalImagePath;
	private int imageWidth;
	private int imageHeight;
	private String imageStore;
	private boolean isCheckedFlag;

	public boolean isCheckedFlag() {
		return isCheckedFlag;
	}

	public void setCheckedFlag(boolean isCheckedFlag) {
		this.isCheckedFlag = isCheckedFlag;
	}

	public String getAisleId() {
		return aisleId;
	}

	public void setAisleId(String aisleId) {
		this.aisleId = aisleId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	private String aisleId;
	private String imageId;

	public boolean isAddedToServerFlag() {
		return isAddedToServerFlag;
	}

	public void setAddedToServerFlag(boolean isAddedToServerFlag) {
		this.isAddedToServerFlag = isAddedToServerFlag;
	}

	private boolean isAddedToServerFlag;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	private String imageUrl;

	public String getImageStore() {
		return imageStore;
	}

	public void setImageStore(String imageStore) {
		this.imageStore = imageStore;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

	public DataentryImage(String aisleId, String imageId,
			String resizedImagePath, String originalImagePath, String imageUrl,
			String detailsUrl, int imageWidth, int imageHeight,
			String imageStore, boolean isAddedToServerFlag) {
		this.aisleId = aisleId;
		this.imageId = imageId;
		this.originalImagePath = originalImagePath;
		this.resizedImagePath = resizedImagePath;
		this.imageUrl = imageUrl;
		this.detailsUrl = detailsUrl;
		this.imageHeight = imageHeight;
		this.imageWidth = imageWidth;
		this.imageStore = imageStore;
		this.isAddedToServerFlag = isAddedToServerFlag;
	}
}
