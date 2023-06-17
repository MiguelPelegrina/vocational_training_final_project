package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.nav_calendar;
import static com.example.trabajofingrado.R.id.nav_recipe_list;
import static com.example.trabajofingrado.R.id.nav_shopping_lists_list;
import static com.example.trabajofingrado.R.id.nav_sign_out;
import static com.example.trabajofingrado.R.id.nav_storage_list;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.trabajofingrado.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;

/**
 * Parent activity of all activities that implement the navigation view
 */
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    protected ActionBarDrawerToggle toggle;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        bindViews();
    }

    /**
     * Handles the selected items of the navigation bar
     * @param item The selected item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Intent intent;
        // Check the selected item
        switch (item.getItemId()) {
            case nav_recipe_list:
                // Move to the recipes
                startActivity(new Intent(BaseActivity.this, RecipeListActivity.class));
                break;
            case nav_storage_list:
                // Move to the storages
                intent = new Intent(BaseActivity.this, StorageListActivity.class);
                intent.putExtra("activity", "view");
                startActivity(intent);
                break;
            case nav_shopping_lists_list:
                // Move to the shopping lists
                startActivity(new Intent(BaseActivity.this, ShoppingListsListActivity.class));
                break;
            case nav_calendar:
                // Move to the calendar
                startActivity(new Intent(BaseActivity.this, CalendarActivity.class));
                break;
            case nav_sign_out:
                // Sign out the user
                signOut(BaseActivity.this);
                break;
        }

        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Handles the "Back" call, closing the drawer if it is open, or getting back to the previous
     * activity
     */
    @Override
    public void onBackPressed() {
        // Check if the drawer is open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Get back to the previous activity
            super.onBackPressed();
        }
    }

    /**
     * Configures the drawer layout
     */
    protected void setDrawerLayout(@IdRes int navigationMenuItemSelected) {
        // Set the toolbar
        setSupportActionBar(toolbar);

        // Instance the toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        // Synchronize the toggle
        toggle.syncState();

        // Select the actual activity
        switch (navigationMenuItemSelected) {
            case nav_recipe_list:
                navigationView.setCheckedItem(nav_recipe_list);
                break;
            case nav_storage_list:
                navigationView.setCheckedItem(nav_storage_list);
                break;
            case nav_shopping_lists_list:
                navigationView.setCheckedItem(nav_shopping_lists_list);
                break;
            case nav_calendar:
                navigationView.setCheckedItem(nav_calendar);
                break;
        }

        setListener();
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        navigationView = findViewById(R.id.nav_view);
    }

    /**
     * Configure the listener
     */
    private void setListener() {
        //
        navigationView.setNavigationItemSelectedListener(this);

        //
        drawerLayout.addDrawerListener(toggle);
    }

    private static void signOut(Activity activity) {
        // TODO MIGHT NOT BE NECESSARY
        // Remove the user data from the shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Gets the google sign in options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Gets the google sign in client
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);

        // Signs out the user
        googleSignInClient.signOut()
                .addOnCompleteListener(activity, task -> activity.startActivity(new Intent(activity, AuthenticationActivity.class)));
    }
}
