package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class Order {

    private ArrayList<Food> foodList;
    private String orderDateTime;
    private String completedDateTime;
    private String patientName;
    private String patientEmail;
    private String restaurantName;
    private String status;

    public String getCompletedDateTime() { return completedDateTime; }

    public void setCompletedDateTime(String completedDateTime) { this.completedDateTime = completedDateTime; }

    public String getPatientEmail() { return patientEmail; }

    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public ArrayList<Food> getFoodList() { return foodList; }

    public void setFoodList(ArrayList<Food> foodList) {
        this.foodList = foodList;
    }

    public String getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(String orderDateTime) {
        this.orderDateTime = orderDateTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Order(ArrayList<Food> foodList, String orderDateTime, String completedDateTime, String patientName,String patientEmail, String restaurantName, String status) {
        this.foodList = foodList;
        this.orderDateTime = orderDateTime;
        this.completedDateTime = completedDateTime;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.restaurantName = restaurantName;
        this.status = status;
    }
}
