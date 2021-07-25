package com.example.workoutvisdraft;

public class APIData {
   String data;

   public APIData(String encodedVid) {
      this.data = encodedVid;
   }

   public String getEncodedVid() {
      return data;
   }

   public void setEncodedVid(String encodedVid) {
      this.data = encodedVid;
   }
}
