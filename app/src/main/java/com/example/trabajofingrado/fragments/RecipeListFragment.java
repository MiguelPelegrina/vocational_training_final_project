package com.example.trabajofingrado.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.controller.AddRecipeActivity;
import com.example.trabajofingrado.controller.RecipeDetailActivity;
import com.example.trabajofingrado.controller.RecipeListActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.Storage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeListFragment extends Fragment {
    // Fields
    private View view;
    private static final int STORAGE_CHOICE_RESULT_CODE = 1;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    //private FloatingActionButton btnAddRecipe;
    private int amountPortions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        this.recyclerView = view.findViewById(R.id.recyclerViewRecipesFragment);
        //this.btnAddRecipe = view.findViewById(R.id.btnAddRecipe);
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Recipe recipe = recipeList.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(view.getContext(), RecipeDetailActivity.class);
                intent.putExtra("name", recipe.getName());
                startActivity(intent);
            }
        });

        // TODO ADD FLOATINGACTIONBUTTON IN FRAGMENT
        /*this.btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddRecipeActivity.class);
                startActivity(intent);
            }
        });*/

        this.fillRecipeList();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_filter_menu, menu);

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

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_filter_available:
                if(!item.isChecked()){
                    createInputDialog(item).show();
                }else{
                    item.setChecked(false);
                    fillRecipeList();
                }
                break;
            case R.id.menu_item_filter_own:
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private AlertDialog createInputDialog(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle("Introduce the number of portions that you want to cook");

        final EditText input = new EditText(view.getContext());

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(null);

        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                amountPortions = 0;
                amountPortions = Integer.parseInt(input.getText().toString());
                item.setChecked(true);
                Intent intent = new Intent(view.getContext(), StorageListActivity.class);
                intent.putExtra("activity", "recipe");
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
}