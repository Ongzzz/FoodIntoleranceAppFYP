package com.example.foodintoleranceappfyp;

import java.util.ArrayList;
import java.util.List;

public class Patient {

    private String name;
    private String email;
    private String state;
    private String gender;
    private ArrayList<String> intolerance = new ArrayList<String>();

    public Patient(String name, String email, String state, String gender, ArrayList<String> intolerance) {
        this.name = name;
        this.email = email;
        this.state = state;
        this.gender = gender;
        this.intolerance = intolerance;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<String> getIntolerance() {
        return intolerance;
    }

    public void setIntolerant(ArrayList<String> intolerance) {
        this.intolerance = intolerance;
    }
}
