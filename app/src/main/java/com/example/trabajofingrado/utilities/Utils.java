package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.controller.AuthenticationActivity;
import com.example.trabajofingrado.controller.RecipeListActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class Utils {
    public static final String PRODUCT_PATH = "products";
    public static final String RECIPE_PATH = "recipes";
    public static final String STORAGE_PATH = "storages";

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

    public static void handleNavigationSelection(MenuItem item, Activity activity){
        // Check the selected item
        switch (item.getItemId()){
            case R.id.nav_recipe_list:
                // Move to the recipes
                activity.startActivity(new Intent(activity, RecipeListActivity.class));
                break;
            case R.id.nav_storage_list:
                // Move to the storages
                Intent intent = new Intent(activity, StorageListActivity.class);
                intent.putExtra("activity", "view");
                activity.startActivity(intent);
                break;
            case R.id.nav_sign_out:
                // Sign out the user
                signOut(activity);
                break;
        }
    }

    private static void signOut(Activity activity) {
        // TODO MIGHT NOT BE NECESSARY
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        //
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);

        //
        googleSignInClient.signOut()
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        activity.startActivity(new Intent(activity, AuthenticationActivity.class));
                    }
                });
    }
}
