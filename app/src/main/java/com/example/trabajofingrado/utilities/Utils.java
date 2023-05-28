package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.trabajofingrado.controller.RecipeDetailActivity;
import com.example.trabajofingrado.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Utils {
    public static final String PRODUCT_PATH = "products",
            SHOPPING_LIST_PATH = "shoppingLists",
            STORAGE_PATH = "storages";

    public static final DatabaseReference CALENDAR_REFERENCE = FirebaseDatabase.getInstance().getReference("calendar"),
    RECIPE_REFERENCE = FirebaseDatabase.getInstance().getReference("recipes");


    public static boolean checkValidString(String string){
        boolean valid = true;

        if(string.trim().length() == 0){
            valid = false;
        }

        return valid;
    }

    public static boolean checkValidStrings(ArrayList<String> strings){
        boolean valid = true;

        for(int i = 0; i < strings.size() && valid; i++){
            if(strings.get(i).trim().length() == 0){
                valid = false;
            }
        }

        return valid;
    }
    public static void connectionError(Context context){
        Toasty.error(context, "An error trying to access " +
                "the database happened. Check your internet connection").show();
    }

    public static String getCurrentTime(){
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        String date = sdf.format(new Date(timestamp));
        return date;
    }

    public static void enterValidData(Activity activity) {
        Toasty.error(activity, "You need to enter valid data").show();
    }

    public static void moveToRecipeDetails(Activity activity, Recipe recipe) {
        // Configure the intent
        Intent intent = new Intent(activity, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        intent.putExtra("recipeName", recipe.getName());
        // Check if the user is the owner of the recipe to enable the option to modify
        // their own recipe
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), recipe.getAuthor())) {
            intent.putExtra("action", "modify");
        } else {
            intent.putExtra("action", "view");
        }

        // Move to the detail activity
        activity.startActivity(intent);
    }
}
