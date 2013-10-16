package com.lateralthoughts.vue;

import java.io.Serializable;

/**
 * UserCredentials in Vue involves creating a Vue User account on the backend to
 * which we can associate identity layers such as FB id, G+ id, etc. A Vue User
 * may choose to not provide their identity (FB, G+) as they begin exploring the
 * app but may decide to login with a specific id at a later point in time.
 * 
 * 
 */

@SuppressWarnings("serial")
public class VueUser implements Serializable {

	public static final String DEFAULT_FACEBOOK_ID = "FACEBOOK_ID_UNKNOWN";
	public static final String DEFAULT_GOOGLEPLUS_ID = "GOOGLE_PLUS_ID_UNKNOWN";

	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private Long joinTime;
	private String deviceId;

	/** Indexed as objectify can only query indexed fields */
	private String facebookId;
	private String googlePlusId;

	public VueUser() {
		facebookId = DEFAULT_FACEBOOK_ID;
		googlePlusId = DEFAULT_GOOGLEPLUS_ID;
	}

	public VueUser(Long id, String email, String firstName, String lastName,
			Long joinTime, String deviceId, String facebookId,
			String googlePlusId) {
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.joinTime = joinTime;
		this.deviceId = deviceId;
		this.facebookId = facebookId;
		this.googlePlusId = googlePlusId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(Long joinTime) {
		this.joinTime = joinTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public String getGooglePlusId() {
		return googlePlusId;
	}

	public void setGooglePlusId(String googlePlusId) {
		this.googlePlusId = googlePlusId;
	}
}
