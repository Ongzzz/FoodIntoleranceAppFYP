package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class RestaurantOwner {

    private String name;
    private String email;
    private ArrayList<String> restaurantName = new ArrayList<>();

    public RestaurantOwner(String name, String email, ArrayList<String> restaurantName) {
        this.name = name;
        this.email = email;
        this.restaurantName = restaurantName;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(ArrayList<String> restaurantName) {
        this.restaurantName = restaurantName;
    }

}
