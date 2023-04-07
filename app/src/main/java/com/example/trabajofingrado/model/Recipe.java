package com.example.trabajofingrado.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

public class Recipe {
    private String name;
    private String image;
    private String author;
    private HashMap<String, String> ingredients;
    private ArrayList<String> steps;

    public Recipe() {
    }

    public Recipe(String name, String image, String author, HashMap<String, String> ingredients, ArrayList<String> steps) {
        this.name = name;
        this.image = image;
        this.author = author;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public HashMap<String, String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(HashMap<String, String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }
}
