package com.example.trabajofingrado.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    protected DrawerLayout drawerLayout;
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }

    protected void onCreateDrawer() {
        // Bind the views
        this.bindViews();
    }

    /**
     * Handles the selected items of the navigation bar
     *
     * @param item The selected item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Intent intent;
        // Check the selected item
        switch (item.getItemId()){
            case R.id.nav_recipe_list:
                // Move to the recipes
                startActivity(new Intent(BaseActivity.this, RecipeListActivity.class));
                break;
            case R.id.nav_storage_list:
                // Move to the storages
                intent = new Intent(BaseActivity.this, StorageListActivity.class);
                intent.putExtra("activity", "view");
                startActivity(intent);
                break;
            case R.id.nav_shopping_lists_list:
                // Move to the shopping lists
                intent = new Intent(BaseActivity.this, ShoppingListsListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sign_out:
                // Sign out the user
                signOut(BaseActivity.this);
                break;
        }

        // Close the drawer
        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Handles the "Back" call, closing the drawer if it is open, or getting back to the previous
     * activity
     */
    @Override
    public void onBackPressed() {
        // Check if the drawer is open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            // Get back to the previous activity
            super.onBackPressed();
        }
    }

    /**
     * Configures the drawer layout
     */
    protected void setDrawerLayout(@IdRes int navigationMenuItemSelected) {
        // Set the toolbar
        this.setSupportActionBar(this.toolbar);

        // Instance the toggle
        this.toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        // Synchronize the toggle
        this.toggle.syncState();

        // Mark the actual activity
        switch (navigationMenuItemSelected){
            case R.id.nav_recipe_list:
                this.navigationView.setCheckedItem(R.id.nav_recipe_list);
                break;
            case R.id.nav_storage_list:
                this.navigationView.setCheckedItem(R.id.nav_storage_list);
                break;
            case R.id.nav_shopping_lists_list:
                this.navigationView.setCheckedItem(R.id.nav_shopping_lists_list);
                break;
        }

        this.setListener();
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.navigationView = findViewById(R.id.nav_view);
    }

    /**
     * Configure the listener
     */
    private void setListener() {
        //
        this.navigationView.setNavigationItemSelectedListener(this);

        //
        this.drawerLayout.addDrawerListener(this.toggle);
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
