package com.example.foodintoleranceappfyp;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {

    private String recipeName;
    private String recipeDescription;
    private String recipeImage;
    private String recipeURL;
    private String adminID;
    private ArrayList<String> intolerance;

    public Recipe(String recipeName, String recipeDescription, String recipeImage, String recipeURL, String adminID, ArrayList<String> intolerance) {
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.recipeImage = recipeImage;
        this.recipeURL = recipeURL;
        this.adminID = adminID;
        this.intolerance = intolerance;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public String getRecipeURL() {
        return recipeURL;
    }

    public void setRecipeURL(String recipeURL) {
        this.recipeURL = recipeURL;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public ArrayList<String> getIntolerance() {
        return intolerance;
    }

    public void setIntolerance(ArrayList<String> intolerance) {
        this.intolerance = intolerance;
    }
}
