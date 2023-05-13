package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.controller.AuthenticationActivity;
import com.example.trabajofingrado.controller.RecipeListActivity;
import com.example.trabajofingrado.controller.ShoppingListsListActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
import com.example.trabajofingrado.model.Storage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class Utils {
    public static final String PRODUCT_PATH = "products";
    public static final String RECIPE_PATH = "recipes";
    public static final String STORAGE_PATH = "storages";
    public static final String SHOPPING_LIST_PATH = "shoppingLists";

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

    public static String getCurrentTime(){
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        String date = sdf.format(new Date(timestamp));
        return date;
    }
}
