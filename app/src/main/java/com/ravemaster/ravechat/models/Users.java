package com.ravemaster.ravechat.models;

public class Users {
    String name, email, image, token;

    public Users(String name, String email, String image, String token) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.token = token;
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
}
