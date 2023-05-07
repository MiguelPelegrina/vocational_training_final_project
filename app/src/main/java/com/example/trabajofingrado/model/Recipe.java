package com.example.trabajofingrado.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Recipe {
    private String name;
    private String image;
    private String author;
    private String id;
    private HashMap<String, StorageProduct> ingredients;
    private ArrayList<String> steps;

    public Recipe() {
    }

    public Recipe(String name, String image, String author, HashMap<String, StorageProduct> ingredients, ArrayList<String> steps, String id) {
        this.name = name;
        this.image = image;
        this.author = author;
        this.ingredients = ingredients;
        this.steps = steps;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public HashMap<String, StorageProduct> getIngredients() {
        return ingredients;
    }

    public void setIngredients(HashMap<String, StorageProduct> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if(obj instanceof Recipe){
            Recipe recipe = (Recipe) obj;
            if(this.id.equals(recipe.id)){
                isEqual = true;
            }
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
