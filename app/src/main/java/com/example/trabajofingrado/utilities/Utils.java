package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.example.trabajofingrado.controller.RecipeDetailActivity;
import com.example.trabajofingrado.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Utils {
    public static final DatabaseReference
            CALENDAR_REFERENCE = FirebaseDatabase.getInstance().getReference("calendar"),
            PRODUCT_REFERENCE = FirebaseDatabase.getInstance().getReference("products"),
            RECIPE_REFERENCE = FirebaseDatabase.getInstance().getReference("recipes"),
            SHOPPING_LIST_REFERENCE = FirebaseDatabase.getInstance().getReference("shoppingLists"),
            STORAGE_REFERENCE = FirebaseDatabase.getInstance().getReference("storages");


    public static boolean checkValidString(String string){
        return string.trim().length() != 0;
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

    public static long dateToEpoch(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month, day);

        return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    public static LocalDateTime epochToDateTime(long epochTimestamp) {
        Instant instant = Instant.ofEpochSecond(epochTimestamp);

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static String getCurrentTime(){
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        return sdf.format(new Date(timestamp));
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

    public static void setFadeAnimation(View view){
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(800);
        view.startAnimation(anim);
    }
}
