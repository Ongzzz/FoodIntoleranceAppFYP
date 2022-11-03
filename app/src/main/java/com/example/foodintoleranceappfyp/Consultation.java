package com.example.foodintoleranceappfyp;

import java.util.ArrayList;

public class Consultation {

    private String dateTime;
    private String patientName;
    private String patientEmail;
    private String doctorName;
    private String doctorEmail;

    public Consultation(String dateTime, String patientName, String patientEmail, String doctorName, String doctorEmail) {
        this.dateTime = dateTime;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
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
