package com.example.trabajofingrado.model;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class RecipesDay {
    // Fields
    private Long date;
    private List<String> recipes;

    public RecipesDay() {}

    public RecipesDay(Long date, List<String> recipes) {
        this.date = date;
        this.recipes = recipes;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public List<String> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<String> recipes) {
        this.recipes = recipes;
    }
}
