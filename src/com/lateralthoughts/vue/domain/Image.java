package com.lateralthoughts.vue.domain;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
        
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
        private int likeRatingCount;
        List<ImageComment> comments;
        
        public Image() {}

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
        
        /**
         * Image rating like count is a read-only field for the client.
         * So getter should be disabled. This will disable
         * serialization of the field.
         */
        @JsonIgnore
        public int getLikeRatingCount() {
                return likeRatingCount;
        }

        @JsonProperty
        public void setLikeRatingCount(int likeRatingCount) {
                this.likeRatingCount = likeRatingCount;
        }
        
        /**
         * Comments are a read-only field for the client.
         * So getter should be disabled. This will disable
         * serialization of the field.
         */
        @JsonIgnore
        public List<ImageComment> getComments() {
                return comments;
        }

        @JsonProperty
        public void setComments(List<ImageComment> comments) {
                this.comments = comments;
        }
}