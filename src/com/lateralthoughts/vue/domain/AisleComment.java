package com.lateralthoughts.vue.domain;

public class AisleComment {
	  Long id;
      Long ownerAisleId;
      Long ownerUserId;
      String comment;
      String commenterFirstName;
      String commenterLastName;
      Long lastModifiedTimestamp;
      Long createdTimestamp;
 
      
      public AisleComment() {
      }

      public Long getId() {
              return id;
      }

      public void setId(Long id) {
              this.id = id;
      }

      public Long getOwnerUserId() {
              return ownerUserId;
      }

      public void setOwnerUserId(Long ownerUserId) {
              this.ownerUserId = ownerUserId;
      }

      public String getComment() {
              return comment;
      }

      public void setComment(String comment) {
              this.comment = comment;
      }

      public Long getLastModifiedTimestamp() {
              return lastModifiedTimestamp;
      }

      public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
              this.lastModifiedTimestamp = lastModifiedTimestamp;
      }

      public Long getCreatedTimestamp() {
              return createdTimestamp;
      }

      public void setCreatedTimestamp(Long createdTimestamp) {
              this.createdTimestamp = createdTimestamp;
      }

      public Long getOwnerAisleId() {
              return ownerAisleId;
      }

      public void setOwnerAisleId(Long ownerAisleId) {
              this.ownerAisleId = ownerAisleId;
      }

      public String getCommenterFirstName() {
              return commenterFirstName;
      }

      public void setCommenterFirstName(String commenterFirstName) {
              this.commenterFirstName = commenterFirstName;
      }

      public String getCommenterLastName() {
              return commenterLastName;
      }

      public void setCommenterLastName(String commenterLastName) {
              this.commenterLastName = commenterLastName;
      }
}
