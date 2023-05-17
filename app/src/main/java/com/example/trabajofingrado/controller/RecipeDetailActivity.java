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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class RecipeDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int RECIPE_MODIFY_RESULT_CODE = 1;
    // Of instance
    private TextView txtName;
    private TextView txtIngredients;
    private TextView txtSteps;
    private ImageView imgRecipeDetail;
    private Recipe recipe;
    private boolean recipeAvailable;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout(R.id.nav_recipe_list);

        this.setData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECIPE_MODIFY_RESULT_CODE) {
            this.setData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getIntent().getStringExtra("action").equals("modify")) {
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
        switch (item.getItemId()) {
            case R.id.menu_item_storages_with_available_products:
                createPortionsAmountDialog().show();
                break;
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
        this.toolbar = findViewById(R.id.toolbar_recipe_detail);
        this.txtName = findViewById(R.id.txtRecipeDetailName);
        this.txtIngredients = findViewById(R.id.txtIngredients);
        this.txtSteps = findViewById(R.id.txtSteps);
        this.imgRecipeDetail = findViewById(R.id.imgRecipeDetailImage);
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
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    startActivity(new Intent(RecipeDetailActivity.this, RecipeListActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private void setData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        Query query = database.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtIngredients.setText(getString(R.string.ingredients));
                txtSteps.setText(getString(R.string.steps));
                for (DataSnapshot ds : snapshot.getChildren()) {
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
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private AlertDialog createPortionsAmountDialog() {
        // Instance the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title
        builder.setTitle("Introduce the number of portions that you want to cook");

        // Configure the edit text
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(null);
        builder.setView(input);

        // Instance the confirm button
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check if the user introduced something
                if (Utils.checkValidString(input.getText().toString())) {
                    int amountPortions = Integer.parseInt(input.getText().toString());

                    alertDialog = createStorageAvailableDialog(amountPortions);
                    alertDialog.show();
                } else {
                    Toasty.error(RecipeDetailActivity.this,
                            "You need to enter a valid amount").show();
                }
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


    private void getRecipesAvailableByStorage(ArrayAdapter<String> arrayAdapter, int amountPortions) {
        // Get the database instance of the storages
        DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Set the query to get the selected storage
        Query query = storageRef.orderByChild(FirebaseAuth.getInstance().getUid());

        recipeAvailable = false;

        // Set the listener to get the data
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Loop through the snapshot children
                for (DataSnapshot ds : snapshot.getChildren()) {

                    Storage storage = ds.getValue(Storage.class);

                    // Get the products stored in the selected storage
                    HashMap<String, StorageProduct> storedProducts = storage.getProducts();
                    // Check if the storage has any products
                    if (storedProducts != null) {
                        // Generate a hashset with the stored products
                        HashSet<String> availableProducts = new HashSet<>(storedProducts.keySet());

                        // Loop through all recipe ingredients
                        for (Map.Entry<String, StorageProduct> entry : recipe.getIngredients().entrySet()) {
                            String ingredientName = entry.getKey();
                            StorageProduct ingredient = entry.getValue();

                            // Check if the ingredient is available
                            if (!availableProducts.contains(ingredientName)) {
                                // Cut the execution
                                break;
                            }

                            // Check if there's enough of the ingredient
                            StorageProduct product = storedProducts.get(ingredientName);
                            if (product.getAmount() < ingredient.getAmount() * amountPortions) {
                                // TODO FILL A HASHMAP WITH THE NAME AND THE AMOUNT OF THE PRODUCT TO CREATE A SHOPPING LIST LATER ON
                                // Cut the execution
                                break;
                            }

                            arrayAdapter.add(storage.getName());
                            recipeAvailable = true;
                        }
                    }
                }
                if (!recipeAvailable) {
                    Toasty.info(RecipeDetailActivity.this, "No storage has enough products").show();
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private AlertDialog createStorageAvailableDialog(int amountPortions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        builder.setTitle("Storages with enough ingredients");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter(RecipeDetailActivity.this, android.R.layout.simple_list_item_1);
        getRecipesAvailableByStorage(arrayAdapter, amountPortions);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }
}














