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

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListRecyclerAdapter;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.utilities.Utils;
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

import es.dmoral.toasty.Toasty;

public class ShoppingListsListActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    // Fields
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ArrayList<ShoppingList> shoppingListsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ShoppingListRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private FloatingActionButton btnAddShoppingList;

    private DatabaseReference shoppingListReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_lists_list);

        setTitle("Shopping lists");

        // Get the database instance of the shopping lists
        shoppingListReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout();

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

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
        Utils.setupNavigationSelection(item, ShoppingListsListActivity.this);

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

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.btnAddShoppingList = findViewById(R.id.btnAddShoppingList);
        this.drawerLayout = findViewById(R.id.drawer_layout_shopping_lists);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_shopping_lists);
        this.recyclerView = findViewById(R.id.rvShoppingListsListActivity);
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
        this.recyclerAdapter = new ShoppingListRecyclerAdapter(shoppingListsList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        // Set the data
        this.fillShoppingListsList();
    }

    private void setListener() {
        btnAddShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingListsListActivity.this, ShoppingListDetailActivity.class);
                intent.putExtra("action", "add");
                startActivity(intent);
            }
        });

        recyclerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                ShoppingList shoppingList = shoppingListsList.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(ShoppingListsListActivity.this, ShoppingListDetailActivity.class);
                intent.putExtra("shoppingListId",shoppingList.getId());
                intent.putExtra("shoppingListName", shoppingList.getName());
                intent.putExtra("storageId", shoppingList.getStorageId());
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
    private void fillShoppingListsList(){
        Query query = shoppingListReference.orderByChild(FirebaseAuth.getInstance().getUid());
        // Set the database to get all shopping lists
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                shoppingListsList.clear();
                // Get every recipe
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList.getUsers().containsKey(FirebaseAuth.getInstance().getUid())){
                        shoppingListsList.add(shoppingList);
                    }
                }
                recyclerAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListsListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }
}