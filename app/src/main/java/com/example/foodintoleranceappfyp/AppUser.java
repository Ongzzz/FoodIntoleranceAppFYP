package com.example.foodintoleranceappfyp;

import java.io.Serializable;

public class AppUser {

    private String name;
    private String userType;

    public AppUser(String name, String userType) {
        this.name = name;
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
