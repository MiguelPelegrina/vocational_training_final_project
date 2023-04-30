package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;

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

public class RecipeListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private static final int STORAGE_CHOICE_RESULT_CODE = 1;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private FloatingActionButton btnAddRecipe;
    private int amountPortions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        setDrawerLayout();

        setRecyclerViewsAndAdapter();

        setListener();

        this.fillRecipeList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORAGE_CHOICE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = database.orderByChild("name").equalTo(data.getStringExtra("storage"));
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_recipe_list:
                startActivity(new Intent(RecipeListActivity.this, RecipeListActivity.class));
                break;
            case R.id.nav_storage_list:
                startActivity(new Intent(RecipeListActivity.this, StorageListActivity.class));
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_search_filter_menu, menu);

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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_filter_by_storage:
                if(!item.isChecked()){
                    createInputDialog(item).show();
                }else{
                    item.setChecked(false);
                    fillRecipeList();
                }
                break;
            case R.id.menu_item_filter_by_owner:
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
                Query query = database.orderByChild("author").equalTo(FirebaseAuth.getInstance().getUid());
                this.fillRecipeWithQueryList(query);
                if (item.isChecked()) {
                    item.setChecked(false);
                    fillRecipeList();
                } else {
                    item.setChecked(true);
                }
                break;
        }

        return true;
    }

    // Auxiliary methods
    private void setRecyclerViewsAndAdapter() {
        this.recyclerView = findViewById(R.id.rvRecipes);
        this.btnAddRecipe = findViewById(R.id.btnAddRecipeActivity);
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);
    }

    private void setDrawerLayout() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_recipe_list);
    }

    private void setListener() {
        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Recipe recipe = recipeList.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("name", recipe.getName());
                startActivity(intent);
            }
        });

        this.btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecipeListActivity.this, AddModifyRecipeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fillRecipeList(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot1.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private void fillRecipeWithQueryList(Query query){
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot1.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private AlertDialog createInputDialog(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the number of portions that you want to cook");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(null);

        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                amountPortions = 0;
                amountPortions = Integer.parseInt(input.getText().toString());
                item.setChecked(true);
                Intent intent = new Intent(RecipeListActivity.this, StorageListActivity.class);
                intent.putExtra("activity", "recipeActivity");
                intent.putExtra("portions", amountPortions);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RecipeListActivity.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RecipeListActivity.this, gso);

        googleSignInClient.signOut()
                .addOnCompleteListener(RecipeListActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(RecipeListActivity.this, AuthenticationActivity.class));
                    }
                });
    }
}