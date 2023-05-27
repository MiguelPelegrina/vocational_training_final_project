package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;

import com.bumptech.glide.util.Util;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RecipeListActivity
        extends BaseActivity {
    // Fields
    // Of the class
    private static final int STORAGE_CHOICE_RESULT_CODE = 1, RECIPE_MODIFY_RESULT_CODE = 2;
    // Of the instance
    private int position;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private FloatingActionButton btnAddRecipe;
    private MenuItem item;
    private Recipe recipe;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_recipe_list);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();

        if (getCallingActivity() == null) {
            if (getIntent() != null) {
                if (getIntent().getStringExtra("storageId") != null) {
                    createPortionsAmountDialog(getIntent().getStringExtra("storageId")).show();
                }
            }
        }
    }

    /**
     * Handles the results obtained by the startActivityForResult() method.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the called activity
        switch (requestCode) {
            case STORAGE_CHOICE_RESULT_CODE:
                // Check the result
                if (resultCode == RESULT_OK) {
                    createPortionsAmountDialog(data.getStringExtra("storageId")).show();
                }
                break;
            case RECIPE_MODIFY_RESULT_CODE:
                // Check the result
                if (resultCode == RESULT_OK) {
                    recyclerAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * Instances the options menu to enable to filter the recipe list
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.recipe_search_filter_menu, menu);

        // Configure the searchView
        this.setSearchView(menu);

        this.item = menu.findItem(R.id.menu_item_filter_by_storage);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles the selected item of the options menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        switch (item.getItemId()) {
            case R.id.menu_item_filter_by_storage:
                // TODO CHECK IF ITEM CHECKING WORKS
                if (!item.isChecked()) {
                    // Configure the intent
                    Intent intent = new Intent(RecipeListActivity.this, StorageListActivity.class);
                    intent.putExtra("activity", "recipe");
                    // Move to the next activity
                    startActivityForResult(intent, STORAGE_CHOICE_RESULT_CODE);
                } else {
                    this.item.setChecked(false);
                    // Refill the list
                    fillRecipeList();
                }
                break;
            case R.id.menu_item_filter_by_owner:
                Query query = RECIPE_REFERENCE.orderByChild("author").equalTo(FirebaseAuth.getInstance().getUid());
                this.fillRecipeWithQueryList(query);
                // TODO CHECK IF ITEM CHECKING WORKS
                if (item.isChecked()) {
                    item.setChecked(false);
                    // Refill the list
                    fillRecipeList();
                } else {
                    item.setChecked(true);
                }
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()) {
            case R.id.rvRecipesListActivity:
                getMenuInflater().inflate(R.menu.recipe_detail_menu, menu);
                //menu.findItem(R.id.menu_item_storages_with_available_products).setVisible(false);
                if (recipe.getAuthor().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    menu.findItem(R.id.menu_item_modify_recipe).setEnabled(true);
                    menu.findItem(R.id.menu_item_delete_recipe).setEnabled(true);
                }
                break;
        }

        menu.setHeaderTitle("As author");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Move directly to modify
            case R.id.menu_item_modify_recipe:
                Intent intent = new Intent(RecipeListActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "modify");
                intent.putExtra("recipeId", recipe.getId());
                startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
                break;
            // Delete
            case R.id.menu_item_delete_recipe:
                createDeleteRecipeInputDialog().show();
                break;
        }

        return true;
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
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeListActivity.this);
            }
        });
    }

    // Auxiliary methods

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddRecipe = findViewById(R.id.btnAddRecipeActivity);
        drawerLayout = findViewById(R.id.drawer_layout_recipes);
        toolbar = findViewById(R.id.toolbar_recipes);
        recyclerView = findViewById(R.id.rvRecipesListActivity);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        // Set the data
        this.fillRecipeList();
    }

    /**
     * Configure the listener
     */
    private void setListener() {
        // Set the on click listener of the recycler adapter. This way we can get more details
        // about the selected recipe.
        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getCallingActivity() == null && getIntent() != null && getIntent().getLongExtra("recipesDayDate", 0) != 0) {
                    setRecipe(view);

                    long recipesDayDate = getIntent().getLongExtra("recipesDayDate", 0);

                    HashMap<String, Object> updates = new HashMap<>();
                    int recipesSize = getIntent().getIntExtra("recipesSize", 0);
                    if (recipesSize > 0) {
                        updates.put(recipesDayDate + "/recipes/" + recipesSize, recipe.getId());
                    } else {
                        List<String> recipes = new ArrayList<>();
                        recipes.add(recipe.getId());

                        RecipesDay recipesDay = new RecipesDay(recipesDayDate, recipes);

                        updates.put(recipesDayDate + "", recipesDay);
                    }
                    CALENDAR_REFERENCE.updateChildren(updates);
                    startActivity(new Intent(RecipeListActivity.this, CalendarActivity.class));
                } else {
                    setRecipe(view);

                    moveToDetails();
                }
            }
        });

        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setRecipe(view);
                registerForContextMenu(recyclerView);
                return false;
            }
        });

        // Set the on click listener of the add boton. This way we can add another recipe to the
        // databsse
        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to the add or modify recipe activity
                Intent intent = new Intent(RecipeListActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "add");
                startActivity(intent);
            }
        });
    }

    /**
     * Fills the recipe list with all the recipes from the database
     */
    private void fillRecipeList() {
        // TODO MIGHT BE BETTER TO ENABLE A REFRESH BUTTON AND BE SINGLE EVENT SO THAT IT DOENST
        //  SCREW UP FILTERS
        // Set the database to get all the recipes
        RECIPE_REFERENCE.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                recipeList.clear();
                // Get every recipe
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot1.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeListActivity.this);
            }
        });
    }

    /**
     * Fills the recipe list depending on the introduced qquery
     *
     * @param query
     */
    private void fillRecipeWithQueryList(Query query) {
        // Set the database to get all the recipes
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                recipeList.clear();
                // Get every recipe depending on the query
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Recipe recipe = ds.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeListActivity.this);
            }
        });
    }

    private void moveToDetails() {
        // Configure the intent
        Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
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
        startActivity(intent);
    }

    /**
     * Configures the alert dialog that ask the user about the amount of portions they want to cook
     *
     * @return
     */
    private AlertDialog createPortionsAmountDialog(String storageId) {
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

                    getRecipesAvailableByStorage(storageId, amountPortions);
                } else {
                    Toasty.error(RecipeListActivity.this,
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

    /**
     * Instances the searchView to enable to filter by name or product
     *
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_recipes);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();
        searchView.setQueryHint("Search by name or ingredients");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.getFilter().filter(s);
                return false;
            }
        });
    }

    /**
     *
     */
    private void getRecipesAvailableByStorage(String storageId, int amountPortions) {
        // Get the database instance of the storages
        DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Set the query to get the selected storage
        Query query = storageRef.orderByChild("id").equalTo(storageId);
        // Set the listener to get the data
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Set the list with all the recipes
                List<Recipe> fullRecipeList = new ArrayList<>(recipeList);

                // Clear the list
                recipeList.clear();

                // Loop through the snapshot children
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the products stored in the selected storage
                    HashMap<String, StorageProduct> storedProducts = ds.getValue(Storage.class).getProducts();

                    // Generate a hashset with the stored products
                    HashSet<String> availableProducts = new HashSet<>(storedProducts.keySet());

                    // Loop through all recipes
                    for (Recipe recipe : fullRecipeList) {
                        // Get all products of the
                        boolean recipePossible = true;

                        // Loop through all recipe ingredients
                        for (Map.Entry<String, StorageProduct> entry : recipe.getIngredients().entrySet()) {
                            String ingredientName = entry.getKey();
                            StorageProduct ingredient = entry.getValue();

                            // Check if the ingredient is available
                            if (!availableProducts.contains(ingredientName)) {
                                recipePossible = false;
                                // Cut the execution
                                break;
                            }

                            // Check if there's enough of the ingredient
                            StorageProduct product = storedProducts.get(ingredientName);
                            if (product.getAmount() < ingredient.getAmount() * amountPortions) {
                                recipePossible = false;
                                // TODO FILL A HASHMAP WITH THE NAME AND THE AMOUNT OF THE PRODUCT TO CREATE A SHOPPING LIST LATER ON
                                // Cut the execution
                                break;
                            }
                        }
                        if (recipePossible) {
                            recipeList.add(recipe);
                        }
                    }
                    recyclerAdapter.notifyDataSetChanged();
                    item.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeListActivity.this);
            }
        });
    }

    private void setRecipe(View view) {
        viewHolder = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolder.getAdapterPosition();
        recipe = recipeList.get(position);
    }
}