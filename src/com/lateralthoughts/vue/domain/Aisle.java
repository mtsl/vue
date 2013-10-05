package com.lateralthoughts.vue.domain;

public class Aisle {
	// Long id;
	Long mOwnerUserId;
	String mCategory;
	String mLookingFor;
	String mName;
	String mOccassion;
	Long mRefImageId;
	VueImage mAisleImage;
	/* String mDescription; */
	String mAisleOwnerFirstName;
	String mAisleOwnerLastName;

	public VueImage getAisleImage() {
		return mAisleImage;
	}

	public void setAisleImage(VueImage mAisleImage) {
		this.mAisleImage = mAisleImage;
	}

	/** ImageList should never be uploaded with the aisle */
	// @JsonIgnore
	// ImageList imageList;

	public Aisle() {
	}

	public Aisle(Long id, String category, String lookingFor, String name,
			String occassion, Long refImageId, Long ownerUserId,
			String aisleOwnerFirstName, String aisleOwnerLastName,
			String descreption, VueImage aisleImage) {
		super();
		// this.id = id;
		this.mCategory = category;
		this.mLookingFor = lookingFor;
		this.mName = name;
		this.mOccassion = occassion;
		this.mRefImageId = refImageId;
		this.mOwnerUserId = ownerUserId;
		this.mAisleImage = aisleImage;
		/*
		 * this.mDescription = descreption; this.mFisrtName = firstName;
		 */
		this.mAisleOwnerLastName = aisleOwnerLastName;
		this.mAisleOwnerFirstName = aisleOwnerFirstName;
	}

	/*
	 * public Long getId() { return id; }
	 */

	/*
	 * public void setId(Long id) { this.id = id; }
	 */

	/*
	 * public void setmDescription(String mDescription) { this.mDescription =
	 * mDescription; }
	 */

	public String getAisleOwnerLastName() {
		return mAisleOwnerLastName;
	}

	public void setAisleOwnerLastName(String mAisleOwnerLastName) {
		this.mAisleOwnerLastName = mAisleOwnerLastName;
	}

	public String getAisleOwnerFirstName() {
		return mAisleOwnerFirstName;
	}

	public void setAisleOwnerFirstName(String mAisleOwnerFirstName) {
		this.mAisleOwnerFirstName = mAisleOwnerFirstName;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String category) {
		this.mCategory = category;
	}

	public String getLookingFor() {
		return mLookingFor;
	}

	public void setLookingFor(String lookingFor) {
		this.mLookingFor = lookingFor;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getOccassion() {
		return mOccassion;
	}

	public void setOccassion(String occassion) {
		this.mOccassion = occassion;
	}

	public Long getRefImageId() {
		return mRefImageId;
	}

	public void setRefImageId(Long refImageId) {
		this.mRefImageId = refImageId;
	}

	public Long getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(Long ownerUserId) {
		this.mOwnerUserId = ownerUserId;
	}

	// @JsonIgnore
	// public ImageList getImageList() {
	// return imageList;
	// }

	// @JsonIgnore
	// public void setImageList(ImageList imageList) {
	// this.imageList = imageList;
	// }
}
