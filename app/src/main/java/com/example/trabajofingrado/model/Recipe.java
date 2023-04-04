package com.example.trabajofingrado.model;

import android.net.Uri;

import java.util.ArrayList;

public class Recipe {
    private String name;
    private Uri image;
    private ArrayList<String> ingredients;
    private ArrayList<String> cookingSteps;

    public Recipe(String name, Uri image, ArrayList<String> ingredients, ArrayList<String> cookingSteps) {
        this.name = name;
        this.image = image;
        this.ingredients = ingredients;
        this.cookingSteps = cookingSteps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(ArrayList<String> cookingSteps) {
        this.cookingSteps = cookingSteps;
    }
}
