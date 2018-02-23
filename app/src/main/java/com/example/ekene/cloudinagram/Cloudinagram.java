package com.example.ekene.cloudinagram;

import android.content.Intent;

/**
 * Created by EKENE on 2/1/2018.
 */

public class Cloudinagram {

    String message;
    String imageUrl;

    public Cloudinagram(String message, String imageUrl){
        this.message = message;
        this.imageUrl = imageUrl;

    }
    public Cloudinagram(){

    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void setImageUrl(String imageUrl) {
        this.message = imageUrl;
    }

}
