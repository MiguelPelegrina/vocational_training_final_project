package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class RecipeDetailActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    // Of class
    private static final int RECIPE_MODIFY_RESULT_CODE = 1;
    // Of instance
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private TextView txtName;
    private TextView txtIngredients;
    private TextView txtSteps;
    private ImageView imgRecipeDetail;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout();

        this.setData();
    }

    /**
     * Handles the selected items of the navigation bar
     * @param item The selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Utils.setupNavigationSelection(item, RecipeDetailActivity.this);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECIPE_MODIFY_RESULT_CODE) {
            setData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (getIntent().getStringExtra("action").equals("modify")){
            menu.findItem(R.id.menu_item_modify_recipe).setEnabled(true);
            menu.findItem(R.id.menu_item_delete_recipe).setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify_delete_recipe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_modify_recipe:
                Intent intent = new Intent(RecipeDetailActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "modify");
                intent.putExtra("recipeId", getIntent().getStringExtra("recipeId"));
                startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
                break;
            case R.id.menu_item_delete_recipe:
                createDeleteRecipeInputDialog().show();
                break;
        }

        return true;
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.drawerLayout = findViewById(R.id.drawer_layout_recipe_detail);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_recipe_detail);
        this.txtName = findViewById(R.id.txtRecipeDetailName);
        this.txtIngredients = findViewById(R.id.txtIngredients);
        this.txtSteps = findViewById(R.id.txtSteps);
        this.imgRecipeDetail = findViewById(R.id.imgRecipeDetailImage);
    }

    /**
     * Configures the drawer layout
     */
    private void setDrawerLayout() {
        // Set the toolbar
        this.setSupportActionBar(this.toolbar);

        // Instance the toggle
        this.toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        // Synchronize the toggle
        this.toggle.syncState();

        // Mark the actual activity
        this.navigationView.setCheckedItem(R.id.nav_recipe_list);
    }

    private AlertDialog createDeleteRecipeInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Are you sure you want to delete " + recipe.getName() + "?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRecipe();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private void deleteRecipe() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        Query query = database.orderByChild("id").equalTo(recipe.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                    startActivity(new Intent(RecipeDetailActivity.this, RecipeListActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(RecipeDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void setData(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        Query query = database.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtIngredients.setText(getString(R.string.ingredients));
                txtSteps.setText(getString(R.string.steps));
                for(DataSnapshot ds: snapshot.getChildren()){
                    recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        txtName.setText(recipe.getName());

                        Glide.with(RecipeDetailActivity.this)
                                .load(recipe.getImage())
                                .error(R.drawable.image_not_found)
                                .into(imgRecipeDetail);

                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            txtIngredients.append("\n - " + ingredient.getValue().getName() +
                                    ": " + ingredient.getValue().getAmount() + " " +
                                    ingredient.getValue().getUnitType());
                        }

                        for (String step : recipe.getSteps()) {
                            txtSteps.append("\n - " + step);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(RecipeDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }
}














