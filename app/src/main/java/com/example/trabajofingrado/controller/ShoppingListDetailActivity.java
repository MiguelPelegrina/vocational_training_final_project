package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.StorageProductRecyclerAdapter;
import com.example.trabajofingrado.interfaces.RecyclerViewActionListener;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ShoppingListDetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    // Fields
    // Of class
    private static final int PRODUCT_ADD_REQUEST_CODE = 1;

    // Of instance
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Button btnAddProduct;
    private ArrayList<StorageProduct> products = new ArrayList<>();
    private ArrayList<StorageProduct> boughtProducts = new ArrayList<>();
    private RecyclerView rvProducts;
    private RecyclerView rvBoughtProducts;
    private ShoppingListProductRecyclerAdapter raProducts;
    private ShoppingListProductRecyclerAdapter raBoughtProducts;
    private RecyclerView.ViewHolder vhProduct;
    private RecyclerView.ViewHolder vhBoughtProduct;
    private DatabaseReference shoppingListReference;
    private RecyclerViewActionListener rvProductsActionListener;
    private RecyclerViewActionListener rvBoughtProductsActionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        // TODO SENT INTENT WITH NAME
        setTitle(getIntent().getStringExtra("shoppingListName"));

        // Get the database instance of the shopping lists
        this.shoppingListReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout();

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();

        this.fillShoppingLists();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Utils.setupNavigationSelection(item, ShoppingListDetailActivity.this);

        // Close the drawer
        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        this.btnAddProduct = findViewById(R.id.btnAddShoppingListProduct);
        this.drawerLayout = findViewById(R.id.drawer_layout_shopping_lists);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_shopping_list);
        this.rvProducts = findViewById(R.id.rvShoppingListDetailActivityProducts);
        this.rvBoughtProducts = findViewById(R.id.rvShoppingListDetailActivityBoughtProducts);
    }

    /**
     * Configures the drawer layout
     */
    private void setDrawerLayout(){
        // Set the toolbar
        this.setSupportActionBar(this.toolbar);

        // Instance the toggle
        this.toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        // Synchronize the toggle
        this.toggle.syncState();
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        rvProductsActionListener = new RecyclerViewActionListener() {
            @Override
            public void onViewClicked(int clickedViewId, int clickedItemPosition) {
                switch (clickedViewId){
                    case R.id.cbProduct:
                        // TODO DIRECT DATABASE CHANGE IS THE MOST REASONABLE I THINK
                        break;
                }
            }
        };

        rvBoughtProductsActionListener = new RecyclerViewActionListener() {
            @Override
            public void onViewClicked(int clickedViewId, int clickedItemPosition) {
                switch (clickedViewId){
                    case R.id.cbProduct:
                        // TODO DIRECT DATABASE CHANGE IS THE MOST REASONABLE I THINK
                        break;
                }
            }
        };

        // Instance the adapters
        this.raProducts = new ShoppingListProductRecyclerAdapter(products, rvProductsActionListener);
        this.raBoughtProducts = new ShoppingListProductRecyclerAdapter(boughtProducts, rvBoughtProductsActionListener);

        // Instance the layout manager
        LinearLayoutManager layoutManagerProducts = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerBoughtProducts = new LinearLayoutManager(this);

        // Configure the recycler view
        this.rvProducts.setAdapter(raProducts);
        this.rvBoughtProducts.setAdapter(raBoughtProducts);
        this.rvProducts.setLayoutManager(layoutManagerProducts);
        this.rvBoughtProducts.setLayoutManager(layoutManagerBoughtProducts);
    }

    private void setListener(){
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingListDetailActivity.this, AddRecipeProductActivity.class);
                intent.putExtra("action", "add");
                startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
            }
        });

        //
        this.navigationView.setNavigationItemSelectedListener(this);

        //
        this.drawerLayout.addDrawerListener(this.toggle);
    }

    private void fillShoppingLists(){
        // Set the database to get all products
        Query query = shoppingListReference.orderByChild("id").equalTo(getIntent().getStringExtra("shoppingListId"));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                boughtProducts.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList != null){
                        if(shoppingList.getNeedToBuyProducts() != null){
                            for (Map.Entry<String, String> entry: shoppingList.getNeedToBuyProducts().entrySet()){
                                StorageProduct product = new StorageProduct(entry.getKey(), entry.getValue());
                                products.add(product);
                            }
                        }

                        if(shoppingList.getBoughtProducts() != null){
                            for (Map.Entry<String, String> entry : shoppingList.getBoughtProducts().entrySet()){
                                StorageProduct product = new StorageProduct(entry.getKey(), entry.getValue());
                                boughtProducts.add(product);
                            }
                        }
                    }
                    raProducts.notifyDataSetChanged();
                    raBoughtProducts.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }
}