package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
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
import java.util.Set;

public class RecipeListActivity extends AppCompatActivity {
    // Fields
    private static final int STORAGE_CHOICE_RESULT_CODE = 1;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        this.recyclerView = findViewById(R.id.RecipeRecyclerView);
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Recipe recipe = recipeList.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("name", recipe.getName());
                startActivity(intent);
            }
        });

        this.fillRecipeList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_filter_menu, menu);

        MenuItem recipeSearchItem = menu.findItem(R.id.app_bar_search);
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
            case R.id.menu_item_filter_available:
                if(!item.isChecked()){
                    Intent intent = new Intent(RecipeListActivity.this, StorageListActivity.class);
                    intent.putExtra("username", getIntent().getStringExtra("username"));
                    intent.putExtra("activity", "recipeActivity");
                    item.setChecked(true);
                    startActivityForResult(intent, STORAGE_CHOICE_RESULT_CODE);
                }else{
                    item.setChecked(false);
                    fillRecipeList();
                }
                break;
            case R.id.menu_item_filter_own:
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
                Query query = database.orderByChild("author").equalTo(getIntent().getStringExtra("username"));
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

    private void fillRecipeList(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO A CONDITION IS PLACED BADLY OR MISSING, IT ONLY ADDS ONE SINGLE RECIPE, NEED TO RESET A BOOLEAN VALUE
        if (requestCode == STORAGE_CHOICE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
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
                                            if(Float.parseFloat(amountAvailable) < Float.parseFloat(amountNecessary)){
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
                    //Log.d("Storage", dataSnapshot1.getValue().toString());

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
            }
        }
    }
}