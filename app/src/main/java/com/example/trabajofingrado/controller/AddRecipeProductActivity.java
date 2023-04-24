package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
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
import com.example.trabajofingrado.adapter.RecipeProductRecyclerAdapter;
import com.example.trabajofingrado.model.RecipeProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddRecipeProductActivity extends AppCompatActivity {
    // Fields
    private ArrayList<RecipeProduct> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe_product);

        this.recyclerView = findViewById(R.id.rvRecipeProducts);
        this.recyclerAdapter = new RecipeProductRecyclerAdapter(productList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                RecipeProduct product = productList.get(viewHolder.getAdapterPosition());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("description", product.getDescription());
                returnIntent.putExtra("unitType", product.getUnit_type());

                switch (getIntent().getStringExtra("action")){
                    case "add":
                        returnIntent.putExtra("action", "add");
                        break;
                    case "modify":
                        returnIntent.putExtra("action", "modify");
                        break;
                }

                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        this.fillProductList();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_product_search_filter_menu, menu);

        MenuItem productSearchItem = menu.findItem(R.id.search_bar_products);
        SearchView searchView = (SearchView) productSearchItem.getActionView();

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

    private void fillProductList(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.PRODUCT_PATH);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    RecipeProduct product = dataSnapshot1.getValue(RecipeProduct.class);
                    productList.add(product);
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