package com.lateralthoughts.vue.domain;

public class AisleBookmark {
	
	Long id;
	Boolean bookmarked;
	Long aisleId;
	Long lastModifiedTimestamp;
	
	public AisleBookmark() {
		
	}
	
	public AisleBookmark(Long id, Boolean bookmarked,
			Long aisleId) {
		super();
		this.id = id;
		this.bookmarked = bookmarked;
		this.aisleId = aisleId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getBookmarked() {
		return bookmarked;
	}
	public void setBookmarked(Boolean bookmarked) {
		this.bookmarked = bookmarked;
	}
	public Long getAisleId() {
		return aisleId;
	}
	public void setAisleId(Long aisleId) {
		this.aisleId = aisleId;
	}

	public Long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}

	public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
		this.lastModifiedTimestamp = lastModifiedTimestamp;
	}
}
