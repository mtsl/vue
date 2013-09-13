package com.lateralthoughts.vue.domain;

public class Aisle {
       // Long id;
        Long ownerUserId;
        String category;
        String lookingFor;
        String name;
        String occassion;
        Long refImageId;

        /** ImageList should never be uploaded with the aisle */
        //@JsonIgnore
        //ImageList imageList;

        public Aisle() {}

        public Aisle(Long id, String category, String lookingFor, String name,
                     String occassion, Long refImageId, Long ownerUserId) {
            super();
          //  this.id = id;
            this.category = category;
            this.lookingFor = lookingFor;
            this.name = name;
            this.occassion = occassion;
            this.refImageId = refImageId;
            this.ownerUserId = ownerUserId;
        }

       /* public Long getId() {
            return id;
        }*/

       /* public void setId(Long id) {
            this.id = id;
        }*/

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLookingFor() {
            return lookingFor;
        }

        public void setLookingFor(String lookingFor) {
            this.lookingFor = lookingFor;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOccassion() {
            return occassion;
        }

        public void setOccassion(String occassion) {
            this.occassion = occassion;
        }

        public Long getRefImageId() {
            return refImageId;
        }

        public void setRefImageId(Long refImageId) {
            this.refImageId = refImageId;
        }

        public Long getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerUserId(Long ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        //@JsonIgnore
        //public ImageList getImageList() {
        //    return imageList;
        //}

        //@JsonIgnore
        //public void setImageList(ImageList imageList) {
        //    this.imageList = imageList;
        //}
 }
