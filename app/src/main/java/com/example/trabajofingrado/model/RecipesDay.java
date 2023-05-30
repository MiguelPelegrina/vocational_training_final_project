package com.example.trabajofingrado.model;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class RecipesDay {
    // Fields
    private Long date;
    private ArrayList<String> recipes;

    public RecipesDay() {}

    public RecipesDay(Long date, ArrayList<String> recipes) {
        this.date = date;
        this.recipes = recipes;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public ArrayList<String> getRecipes() {
        return recipes;
    }

    public void setRecipes(ArrayList<String> recipes) {
        this.recipes = recipes;
    }
}
