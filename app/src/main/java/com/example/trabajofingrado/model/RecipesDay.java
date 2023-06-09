package com.example.trabajofingrado.model;

import java.util.ArrayList;

/**
 * Class responsible for storing all the recipes that are planned to be prepared on a specific date
 */
public class RecipesDay {
    // Fields
    private Long date;
    private ArrayList<String> recipes;

    /**
     * Default constructor
     */
    public RecipesDay() {
    }

    /**
     * Parameterized constructor
     *
     * @param date
     * @param recipes
     */
    public RecipesDay(Long date, ArrayList<String> recipes) {
        this.date = date;
        this.recipes = recipes;
    }

    // Getter
    public Long getDate() {
        return date;
    }

    public ArrayList<String> getRecipes() {
        return recipes;
    }

    // Setter
    public void setDate(Long date) {
        this.date = date;
    }

    public void setRecipes(ArrayList<String> recipes) {
        this.recipes = recipes;
    }
}
