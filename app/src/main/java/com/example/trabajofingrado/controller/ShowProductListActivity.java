package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.utilities.Utils.PRODUCT_REFERENCE;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShowProductRecyclerAdapter;
import com.example.trabajofingrado.model.ShowProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Controller that handles the use cases of selecting products for recipes, storages or shopping lists
 */
public class ShowProductListActivity extends BaseActivity {
    // Fields
    private final ArrayList<ShowProduct> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;
    private ShowProductRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        setTitle("Products");

        bindViews();

        setDrawerLayout(0);

        setRecyclerView();

        setListener();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_product_search_filter_menu, menu);

        setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Instances the search view to enable to filter by name
     * @param menu
     */
    private void setSearchView(Menu menu) {
        // Set the search view
        MenuItem productSearchItem = menu.findItem(R.id.search_bar_products);
        SearchView searchView = (SearchView) productSearchItem.getActionView();

        // Configure the search view
        searchView.setQueryHint("Search by name");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        recyclerView = findViewById(R.id.rvProducts);
        drawerLayout = findViewById(R.id.drawer_layout_add_product);
        toolbar = findViewById(R.id.toolbar_add_product);
    }

    /**
     * Loads all the data from product table of the database
     */
    private void fillProductList(){
        // Search the products in the database
        Query query = PRODUCT_REFERENCE.orderByChild("name");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the list
                productList.clear();

                // Loop through the products
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the product
                    ShowProduct product = ds.getValue(ShowProduct.class);

                    // Add the product to the list
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShowProductListActivity.this);
            }
        });
    }

    /**
     * Sets the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new ShowProductRecyclerAdapter(productList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Set a divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data
        fillProductList();
    }

    /**
     * Sets the listener of the views
     */
    private void setListener() {
        adapter.setOnClickListener(view -> {
            // Get the view holder
            viewHolder = (RecyclerView.ViewHolder) view.getTag();

            // Get the product
            ShowProduct product = productList.get(viewHolder.getAdapterPosition());

            // Configure the intent to return the data of the selected product
            Intent returnIntent = new Intent();
            returnIntent.putExtra("name", product.getName());
            returnIntent.putExtra("unitType", product.getUnitType());

            // Check what action to perform
            switch (getIntent().getStringExtra("action")){
                // Add a product
                case "add":
                    returnIntent.putExtra("action", "add");
                    break;
                    // Modify the name of product
                case "modify":
                    returnIntent.putExtra("action", "modify");
                    break;
            }

            setResult(Activity.RESULT_OK, returnIntent);

            finish();
        });
    }
}