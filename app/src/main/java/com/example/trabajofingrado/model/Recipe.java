package com.example.trabajofingrado.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Recipe {
    // Fields
    private String author, id, image, name;
    private HashMap<String, StorageProduct> ingredients;
    private ArrayList<String> steps;

    /**
     * Default constructor
     */
    public Recipe() {
    }

    /**
     * Parameterized constructor
     *
     * @param name
     * @param image
     * @param author
     * @param ingredients
     * @param steps
     * @param id
     */
    public Recipe(String name, String image, String author, HashMap<String, StorageProduct> ingredients, ArrayList<String> steps, String id) {
        this.name = name;
        this.image = image;
        this.author = author;
        this.ingredients = ingredients;
        this.steps = steps;
        this.id = id;
    }

    // Getter
    public String getAuthor() {
        return author;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public HashMap<String, StorageProduct> getIngredients() {
        return ingredients;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    // Setter
    public void setAuthor(String author) {
        this.author = author;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIngredients(HashMap<String, StorageProduct> ingredients) {
        this.ingredients = ingredients;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", author='" + author + '\'' +
                ", id='" + id + '\'' +
                ", ingredients=" + ingredients +
                ", steps=" + steps +
                '}';
    }
}
