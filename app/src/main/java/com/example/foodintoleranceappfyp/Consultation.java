package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class Consultation {

    private String dateTime;
    private String patientName;
    private String patientEmail;
    private String gender;
    private String state;
    private ArrayList<String> intolerance = new ArrayList<String>();
    private String doctorName;
    private String doctorEmail;

    public Consultation(String dateTime, String patientName, String patientEmail, String gender, String state, ArrayList<String> intolerance, String doctorName, String doctorEmail) {
        this.dateTime = dateTime;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.gender = gender;
        this.state = state;
        this.intolerance = intolerance;
        this.doctorName = doctorName;
        this.doctorEmail = doctorEmail;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ArrayList<String> getIntolerance() {
        return intolerance;
    }

    public void setIntolerance(ArrayList<String> intolerance) {
        this.intolerance = intolerance;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }
}
