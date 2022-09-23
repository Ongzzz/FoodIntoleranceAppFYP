package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class Food {

    private String name;
    private String description;
    private double price;
    private int quantity;
    private ArrayList<String> intolerance = new ArrayList<String>();
    private String imagePath;
    private String restaurantName;
    private String status;

    public Food(String name, String description, double price, int quantity, ArrayList<String> intolerance, String imagePath, String restaurantName, String status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.intolerance = intolerance;
        this.imagePath = imagePath;
        this.restaurantName = restaurantName;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ArrayList<String> getIntolerance() {
        return intolerance;
    }

    public void setIntolerance(ArrayList<String> intolerance) {
        this.intolerance = intolerance;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getStatus() {
        return status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
