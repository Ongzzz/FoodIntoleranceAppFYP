package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class Cart {

    private ArrayList<Food> food = new ArrayList<>();
    private String patientName;
    private String restaurantName;

    public Cart(ArrayList<Food> food, String patientName, String restaurantName) {
        this.food = food;
        this.patientName = patientName;
        this.restaurantName = restaurantName;
    }

    public ArrayList<Food> getFood() {
        return food;
    }

    public void setFood(ArrayList<Food> food) {
        this.food = food;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
