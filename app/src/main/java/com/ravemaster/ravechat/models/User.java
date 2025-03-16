package com.ravemaster.ravechat.models;

import java.io.Serializable;

public class User implements Serializable {
    String name, email, image, token,id;

    public User(String name, String email, String image, String token, String id) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.token = token;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public String getToken() {
        return token;
    }

    public String getId(){
        return id;
    }
}
