package com.example.foodintoleranceappfyp;

import java.io.Serializable;
import java.util.ArrayList;

public class Restaurant implements Serializable {

    private String restaurantName;
    private String restaurantAddress;
    private String restaurantState;
    private String restaurantOwnerEmail;
    private String logoPath;
    private ArrayList<String> menu = new ArrayList<>();

    public Restaurant(String restaurantName, String restaurantAddress, String restaurantState, String restaurantOwnerEmail, ArrayList<String> menu, String logoPath) {
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.restaurantState = restaurantState;
        this.restaurantOwnerEmail = restaurantOwnerEmail;
        this.menu = menu;
        this.logoPath = logoPath;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getRestaurantState() {
        return restaurantState;
    }

    public void setRestaurantState(String restaurantState) {
        this.restaurantState = restaurantState;
    }

    public String getRestaurantOwnerEmail() {
        return restaurantOwnerEmail;
    }

    public void setRestaurantOwnerEmail(String restaurantOwnerEmail) {
        this.restaurantOwnerEmail = restaurantOwnerEmail;
    }

    public ArrayList<String> getMenu() {
        return menu;
    }

    public void setMenu(ArrayList<String> menu) {
        this.menu = menu;
    }

    public String getLogoPath() {
        return logoPath;
    }

}
