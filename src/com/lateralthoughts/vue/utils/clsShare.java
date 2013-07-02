package com.lateralthoughts.vue.utils;

import java.io.File;

public class clsShare {
private String imageUrl;
public clsShare(String imageUrl, File file) {
	this.imageUrl = imageUrl;
	this.file = file;
}
public String getImageUrl() {
	return imageUrl;
}
public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
}
public File getFile() {
	return file;
}
public void setFile(File file) {
	this.file = file;
}
private File file;
}
