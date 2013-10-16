package com.lateralthoughts.vue;

public class DataentryImage {
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	private String imagePath;
	private String sourceUrl;

	public DataentryImage(String imagePath, String sourceUrl) {
		this.imagePath = imagePath;
		this.sourceUrl = sourceUrl;
	}
}
