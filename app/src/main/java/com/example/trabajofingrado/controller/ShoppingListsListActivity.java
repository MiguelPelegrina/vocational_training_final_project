package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListRecyclerAdapter;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.utilities.ShoppingListInputDialogs;
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

import es.dmoral.toasty.Toasty;

public class ShoppingListsListActivity extends BaseActivity {
    // Fields
    private ArrayList<ShoppingList> shoppingListsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ShoppingListRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private FloatingActionButton btnAddShoppingList;

    private DatabaseReference shoppingListReference;
    private TextView txtNoShoppingListsAvailable;
    private ShoppingList shoppingList;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_lists_list);

        setTitle("Shopping lists");

        // Get the database instance of the shopping lists
        shoppingListReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_shopping_lists_list);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        setListener();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.modify_delete_shopping_list_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_modify_shopping_list_name:
                ShoppingListInputDialogs.updateShoppingListNameDialog(ShoppingListsListActivity.this, shoppingList.getId()).show();
                break;
            case R.id.menu_item_delete_shopping_list:
                ShoppingListInputDialogs.deleteShoppingListDialog(ShoppingListsListActivity.this, shoppingList.getId()).show();
                break;
        }

        return true;
    }

    // Auxiliary methods

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddShoppingList = findViewById(R.id.btnAddShoppingList);
        txtNoShoppingListsAvailable = findViewById(R.id.txtNoShoppingListsAvailable);
        drawerLayout = findViewById(R.id.drawer_layout_shopping_lists);
        toolbar = findViewById(R.id.toolbar_shopping_lists);
        recyclerView = findViewById(R.id.rvShoppingListsList);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        recyclerAdapter = new ShoppingListRecyclerAdapter(shoppingListsList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data
        fillShoppingListsList();
    }

    private void setListener() {
        btnAddShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingListsListActivity.this, StorageListActivity.class);
                intent.putExtra("activity", "add");
                startActivity(intent);
            }
        });

        this.recyclerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setShoppingList(view);
                Intent intent = new Intent(ShoppingListsListActivity.this, ShoppingListDetailActivity.class);
                intent.putExtra("shoppingListId", shoppingList.getId());
                intent.putExtra("shoppingListName", shoppingList.getName());
                intent.putExtra("storageId", shoppingList.getStorageId());
                startActivity(intent);
            }
        });

        this.recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setShoppingList(view);
                registerForContextMenu(recyclerView);
                return false;
            }
        });
    }

    /**
     * Fills the recipe list with all the recipes from the database
     */
    private void fillShoppingListsList() {
        Query query = shoppingListReference.orderByChild(FirebaseAuth.getInstance().getUid());
        // Set the database to get all shopping lists
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                shoppingListsList.clear();
                // Get every shopping list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getUsers() != null && shoppingList.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        shoppingListsList.add(shoppingList);
                    }
                    txtNoShoppingListsAvailable.setVisibility(View.INVISIBLE);
                }
                recyclerAdapter.notifyDataSetChanged();
                if (shoppingListsList.isEmpty()) {
                    txtNoShoppingListsAvailable.setVisibility(View.VISIBLE);
                } else {
                    txtNoShoppingListsAvailable.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListsListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void setShoppingList(View view) {
        viewHolder = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolder.getAdapterPosition();
        shoppingList = shoppingListsList.get(position);
    }
}