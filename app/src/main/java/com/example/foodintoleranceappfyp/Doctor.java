package com.example.foodintoleranceappfyp;

import android.net.Uri;

import java.util.ArrayList;

public class Doctor {

    private String name;
    private String email;
    private String hospital;
    private ArrayList<String> documentNameList;

    public Doctor(String name, String email, String hospital, ArrayList<String> documentNameList) {
        this.name = name;
        this.email = email;
        this.hospital = hospital;
        this.documentNameList = documentNameList;
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

    public ArrayList<String> getDocumentNameList() {
        return documentNameList;
    }

    public void setDocumentNameList(ArrayList<String> documentNameList) {
        this.documentNameList = documentNameList;
    }

}
