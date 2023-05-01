package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RecipeListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    // Of the class
    private static final int STORAGE_CHOICE_RESULT_CODE = 1;
    private static final int RECIPE_MODIFY_RESULT_CODE = 2;
    // Of the instance
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private FloatingActionButton btnAddRecipe;
    private int amountPortions;
    private MenuItem item;
    private int position;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout();

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();
    }

    /**
     * Handles the results obtained by the startActivityForResult() method.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the called activity
        switch (requestCode){
            case STORAGE_CHOICE_RESULT_CODE:
                // Check the result
                if (resultCode == RESULT_OK) {
                    getRecipesAvailableByStorage(data);
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
     * Handles the selected items of the navigation bar
     * @param item The selected item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Utils.setupNavigationSelection(item, RecipeListActivity.this);

        // Close the drawer
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handles the "Back" call, closing the drawer if pressed
     */
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    /**
     * Instances the options menu to enable to filter the recipe list
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.recipe_search_filter_menu, menu);

        // Configure the searchView
        setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles the selected item of the options menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        switch (item.getItemId()){
            case R.id.menu_item_filter_by_storage:
                // TODO CHECK IF ITEM CHECKING WORKS
                if(!item.isChecked()){
                    this.item = item;
                    createInputDialog().show();
                }else{
                    item.setChecked(false);
                    // Refill the list
                    fillRecipeList();
                }
                break;
            case R.id.menu_item_filter_by_owner:
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
                Query query = database.orderByChild("author").equalTo(FirebaseAuth.getInstance().getUid());
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

        switch (v.getId()){
            case R.id.rvRecipesListActivity:
                getMenuInflater().inflate(R.menu.modify_delete_recipe_menu, menu);
                if(recipe.getAuthor().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    menu.findItem(R.id.context_menu_item_modify_recipe).setEnabled(true);
                    menu.findItem(R.id.context_menu_item_delete_recipe).setEnabled(true);
                }
                break;
        }

        menu.setHeaderTitle("As author");

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            // Delete
            case R.id.context_menu_item_modify_recipe:
                Intent intent = new Intent(RecipeListActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "modify");
                intent.putExtra("recipeUUID", recipe.getUuid());
                startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
                break;
            // Move directly to modify
            case R.id.context_menu_item_delete_recipe:
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
                //recipeList.remove(position);
                recyclerAdapter.notifyItemRemoved(position);
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
        Query query = database.orderByChild("uuid").equalTo(recipe.getUuid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                    recyclerAdapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(RecipeListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.btnAddRecipe = findViewById(R.id.btnAddRecipeActivity);
        this.drawerLayout = findViewById(R.id.drawer_layout_recipes);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_recipes);
        this.recyclerView = findViewById(R.id.rvRecipesListActivity);
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

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

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
        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the recipe
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                recipe = recipeList.get(viewHolder.getAdapterPosition());

                // Configure the intent
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipeUUID", recipe.getUuid());
                // Check if the user is the owner of the recipe to enable the option to modify
                // their own recipe
                if(Objects.equals(FirebaseAuth.getInstance().getUid(), recipe.getAuthor())){
                    intent.putExtra("action", "modify");
                }else{
                    intent.putExtra("action", "view");
                }

                // Move to the detail activity
                startActivity(intent);
            }
        });

        this.recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setRecipe(view);
                registerForContextMenu(recyclerView);
                return false;
            }
        });

        // Set the on click listener of the add boton. This way we can add another recipe to the
        // databsse
        this.btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to the add or modify recipe activity
                Intent intent = new Intent(RecipeListActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "add");
                startActivity(intent);
            }
        });

        //
        this.navigationView.setNavigationItemSelectedListener(this);

        //
        this.drawerLayout.addDrawerListener(this.toggle);
    }

    /**
     * Fills the recipe list with all the recipes from the database
     */
    private void fillRecipeList(){
        // Get the database instance of the recipes
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        // Set the database to get all the recipes
        database.addValueEventListener(new ValueEventListener() {
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
                Toasty.error(RecipeListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    /**
     * Fills the recipe list depending on the introduced qquery
     * @param query
     */
    private void fillRecipeWithQueryList(Query query){
        // Set the database to get all the recipes
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                recipeList.clear();
                // Get every recipe depending on the query
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot1.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(RecipeListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    /**
     * Configures the alert dialog that ask the user about the amount of portions they want to cook
     * @return
     */
    private AlertDialog createInputDialog(){
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
                // TODO CHECK POSSIBLE ERROR
                amountPortions = 0;
                amountPortions = Integer.parseInt(input.getText().toString());

                // Configure the intent
                Intent intent = new Intent(RecipeListActivity.this, StorageListActivity.class);
                intent.putExtra("activity", "recipe");
                intent.putExtra("portions", amountPortions);
                // Move to the next activity
                startActivityForResult(intent, STORAGE_CHOICE_RESULT_CODE);
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

    private void signOut() {
        // TODO MIGHT NOT BE NECESSARY
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RecipeListActivity.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        //
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RecipeListActivity.this, gso);

        //
        googleSignInClient.signOut()
                .addOnCompleteListener(RecipeListActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(RecipeListActivity.this, AuthenticationActivity.class));
                    }
                });
    }

    /**
     * Instances the searchView to enable to filter by name or product
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_recipes);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();

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
    private void getRecipesAvailableByStorage(Intent data) {
        // Get the database instance of the storages
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
        // Set the query to get the selected storage
        Query query = database.orderByChild("name").equalTo(data.getStringExtra("storage"));
        // Set the listener to get the data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO DO QUERY IN FIREBASE
                List<Recipe> fullRecipeList = new ArrayList<>(recipeList);
                recipeList.clear();
                // Loop through the snapshot children
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    // Get the products stored in the selected storage
                    HashMap<String,String> storedProducts = dataSnapshot1.getValue(Storage.class).getProducts();
                    // Loop through all recipes
                    for(Recipe recipe : fullRecipeList){
                        HashMap<String,String> auxStoredProducts = new HashMap<>(storedProducts);
                        boolean recipePossible = true;
                        // Check if all products for this concrete recipe are available
                        if(storedProducts.keySet().containsAll(recipe.getIngredients().keySet())){
                            auxStoredProducts.keySet().retainAll(recipe.getIngredients().keySet());
                            // Loop through all products
                            for(Map.Entry<String, String> product: auxStoredProducts.entrySet()){
                                boolean necessaryAmountAvailable = true;
                                // Loop through every ingredient
                                for(int i = 0; i < recipe.getIngredients().values().size() && necessaryAmountAvailable; i++){
                                    String amountAvailable = product.getValue().substring(0,product.getValue().indexOf(" ")).toLowerCase();
                                    String amountNecessary = recipe.getIngredients().get(product.getKey()).substring(0, recipe.getIngredients().get(product.getKey()).indexOf(" ")).toLowerCase();
                                    if(Float.parseFloat(amountAvailable) < Float.parseFloat(amountNecessary) * amountPortions){
                                        necessaryAmountAvailable = false;
                                        // TODO FILL A HASHMAP WITH THE NAME AND THE AMOUNT OF THE PRODUCT TO CREATE A
                                        // SHOPPING LIST LATER ON
                                    }
                                    if(!necessaryAmountAvailable){
                                        recipePossible = false;
                                    }
                                }
                            }
                        }else{
                            recipePossible = false;
                        }
                        if(recipePossible){
                            recipeList.add(recipe);
                        }
                    }
                }
                recyclerAdapter.notifyDataSetChanged();
                item.setChecked(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(RecipeListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void setRecipe(View view){
        viewHolder = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolder.getAdapterPosition();
        recipe = recipeList.get(position);
    }
}