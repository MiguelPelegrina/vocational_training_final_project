package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeProductRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class AddProductActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // Fields
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ArrayList<Product> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

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
     * Handles the selected items of the navigation bar
     * @param item The selected item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Utils.setupNavigationSelection(item, AddProductActivity.this);

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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_search_filter_menu, menu);

        // Configure the searchView
        this.setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setSearchView(Menu menu) {
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
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.recyclerView = findViewById(R.id.rvProducts);
        this.drawerLayout = findViewById(R.id.drawer_layout_add_product);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_add_product);
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

    private void fillProductList(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.PRODUCT_PATH);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Product product = dataSnapshot1.getValue(Product.class);
                    productList.add(product);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(AddProductActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new RecipeProductRecyclerAdapter(productList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        // Set the data
        this.fillProductList();
    }

    private void setListener() {
        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Product product = productList.get(viewHolder.getAdapterPosition());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name", product.getName());
                returnIntent.putExtra("unitType", product.getUnitType());

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

        //
        this.navigationView.setNavigationItemSelectedListener(this);

        //
        this.drawerLayout.addDrawerListener(this.toggle);
    }
}