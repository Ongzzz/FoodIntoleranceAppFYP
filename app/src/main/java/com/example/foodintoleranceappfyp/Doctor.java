package com.example.foodintoleranceappfyp;

public class Doctor {

    private String name;
    private String email;
    private String hospital;

    public Doctor(String name, String email,String hospital) {
        this.name = name;
        this.email = email;
        this.hospital = hospital;
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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}
