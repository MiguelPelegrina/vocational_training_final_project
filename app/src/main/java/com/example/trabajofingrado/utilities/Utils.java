package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.example.trabajofingrado.controller.RecipeDetailActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
import com.example.trabajofingrado.controller.StorageProductListActivity;
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
    // Fields
    // All database references
    public static final DatabaseReference
            CALENDAR_REFERENCE = FirebaseDatabase.getInstance().getReference("calendar"),
            PRODUCT_REFERENCE = FirebaseDatabase.getInstance().getReference("products"),
            RECIPE_REFERENCE = FirebaseDatabase.getInstance().getReference("recipes"),
            SHOPPING_LIST_REFERENCE = FirebaseDatabase.getInstance().getReference("shoppingLists"),
            STORAGE_REFERENCE = FirebaseDatabase.getInstance().getReference("storages");


    /**
     * Checks if a string is empty. A string filled with white spaces is considered empty.
     * @param string
     */
    public static boolean checkValidString(String string){
        return string.trim().length() != 0;
    }

    /**
     * Checks if all strings inside an array are not empty
     * @param strings
     */
    public static boolean checkValidStrings(ArrayList<String> strings){
        // Set the flag
        boolean valid = true;

        // Check if any string is empty
        for(int i = 0; i < strings.size() && valid; i++){
            // If any string is empty, the array is not considered valid
            if(strings.get(i).trim().length() == 0){
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Informs the user that an error happened
     * @param context Context from which this method is called
     */
    public static void connectionError(Context context){
        Toasty.error(context, "An error trying to access the database happened.").show();
    }

    /**
     * Casts a day, a month and a year to an epoch to save in the database
     * @param day
     * @param month
     * @param year
     * @return The epoch of a date
     */
    public static long dateToEpoch(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month, day);

        return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    /**
     * Casts an epoch to an instant to get the epoch stored in the database as a date
     * @param epochTimestamp
     * @return The date time of the epoch
     */
    public static LocalDateTime epochToDateTime(long epochTimestamp) {
        Instant instant = Instant.ofEpochSecond(epochTimestamp);

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Get the current time with the following formart: HH:mm dd-MM-yyyy
     * @return The date of the current moment
     */
    public static String getCurrentTime(){
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        return sdf.format(new Date(timestamp));
    }

    /**
     * Informs the user that the entered date was not valid
     * @param activity Activity from which the method is called
     */
    public static void enterValidData(Activity activity) {
        Toasty.error(activity, "You need to enter valid data").show();
    }

    /**
     * Move to the recipe details activity
     * @param activity Activity from which this method was called
     * @param recipe
     */
    public static void moveToRecipeDetails(Activity activity, Recipe recipe) {
        // Configure the intent
        Intent intent = new Intent(activity, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        intent.putExtra("recipeName", recipe.getName());

        // Check if the user is the owner of the recipe. Enable the option to modify if true
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), recipe.getAuthor())) {
            intent.putExtra("action", "modify");
        } else {
            intent.putExtra("action", "view");
        }

        // Move to the detail activity
        activity.startActivity(intent);
    }

    /**
     * Set the fading animation of recycler holder
     * @param view View whose animation will be changed
     */
    public static void setFadeAnimation(View view){
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(800);
        view.startAnimation(anim);
    }

    /**
     * Set the storage to the clipboard of the device to enable the user to join a new storage
     */
    public static void copyStorageIdToClipboard(Activity activity, String storageId) {
        // Get the clipboard
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);

        // Generate the clip
        ClipData clip = ClipData.newPlainText("storage access code", storageId);

        // Set the clip to the clipboard
        clipboard.setPrimaryClip(clip);

        // Check the android version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // Inform the user if the version is too low
            Toasty.info(activity, "Code copied").show();
        }
    }
}
