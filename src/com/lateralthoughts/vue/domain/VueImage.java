package com.lateralthoughts.vue.domain;

public class VueImage {
	
    Long id;
    Long ownerUserId;
    Long ownerAisleId;
	String detailsUrl;
	Integer height;
	Integer width;
	String imageUrl;
	Integer rating;
	String store;
	String title;
	
	public VueImage() {}

	public VueImage(Long id, String detailsUrl, Integer height, Integer width,
			String imageUrl, Integer rating, String store, String title,
			Long ownerUserId, Long ownerAisleId) {
		super();
		this.id = id;
		this.detailsUrl = detailsUrl;
		this.height = height;
		this.width = width;
		this.imageUrl = imageUrl;
		this.rating = rating;
		this.store = store;
		this.title = title;
		this.ownerAisleId = ownerAisleId;
		this.ownerUserId = ownerUserId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(Long ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public Long getOwnerAisleId() {
		return ownerAisleId;
	}

	public void setOwnerAisleId(Long ownerAisleId) {
		this.ownerAisleId = ownerAisleId;
	}
}
