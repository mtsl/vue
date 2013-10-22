package com.lateralthoughts.vue;

public class ImageRating {
	
    public static final int NEW_TIME_STAMP = 2;
    public static final int OLD_TIME_STAMP = 1;
    public static final int SAME_TIME_STAMP = 0;
	Long id;
	Boolean liked;
	Long aisleId;
	Long imageId;
	Long lastModifiedTimestamp;
	
	public ImageRating() {
		
	}
	
	public ImageRating(Long id, Boolean liked,
	    Long aisleId) {
		super();
		this.id = id;
		this.liked = liked;
		this.aisleId = aisleId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getLiked() {
		return liked;
	}
	public void setLiked(Boolean liked) {
		this.liked = liked;
	}
	public Long getAisleId() {
		return aisleId;
	}
	public void setAisleId(Long aisleId) {
		this.aisleId = aisleId;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public Long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}

	public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
		this.lastModifiedTimestamp = lastModifiedTimestamp;
	}
	
	public boolean compareTo(ImageRating other) {
	  boolean imgIdMatched = false;
	  if(this.imageId.longValue() == other.imageId.longValue()) {
	    imgIdMatched = true;
	  }
	  if(imgIdMatched) {
	    return true;
	  }
	  
	  return false;
	}
	
	public int compareTime(long timeStamp) {
	  if(this.lastModifiedTimestamp > timeStamp) {
	    return NEW_TIME_STAMP;
	  } else if(this.lastModifiedTimestamp < timeStamp) {
	    return OLD_TIME_STAMP;
	  } else {
	    return SAME_TIME_STAMP;
	  }
	}
}
