package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.menu_item_delete_shopping_list;
import static com.example.trabajofingrado.R.id.menu_item_modify_shopping_list_name;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListRecyclerAdapter;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.ShoppingListInputDialogs;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class ShoppingListsListActivity extends BaseActivity {
    // Fields
    private int position;
    private final ArrayList<ShoppingList> shoppingListsList = new ArrayList<>();
    private FloatingActionButton btnAddShoppingList;
    private RecyclerView recyclerView;
    private ShoppingList shoppingList;
    private ShoppingListRecyclerAdapter adapter;
    private String searchCriteria;
    private TextView txtNoShoppingListsAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_lists_list);

        setTitle("Your shopping lists");

        bindViews();

        setDrawerLayout(R.id.nav_shopping_lists_list);

        setRecyclerView();

        setListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.shopping_lists_filter_menu, menu);

        setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.modify_delete_shopping_list_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case menu_item_modify_shopping_list_name:
                ShoppingListInputDialogs.updateShoppingListNameDialog(ShoppingListsListActivity.this, shoppingList.getId()).show();
                break;
            case menu_item_delete_shopping_list:
                ShoppingListInputDialogs.deleteShoppingListDialog(
                        ShoppingListsListActivity.this, shoppingList.getId(), adapter, searchCriteria).show();
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
     * Instances the searchView to enable to filter by shopping list name or storage
     *
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_shopping_lists);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();
        searchView.setQueryHint("Search by name or storage");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchCriteria = s;
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new ShoppingListRecyclerAdapter(shoppingListsList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data
        fillShoppingListsList();
    }

    /**
     * Set the listener of all the views
     */
    private void setListener() {
        btnAddShoppingList.setOnClickListener(view -> {
            Intent intent = new Intent(ShoppingListsListActivity.this, StorageListActivity.class);
            intent.putExtra("activity", "addShoppingList");
            startActivity(intent);
        });

        adapter.setOnClickListener(view -> {
            setShoppingList(view);
            Intent intent = new Intent(ShoppingListsListActivity.this, ShoppingListDetailActivity.class);
            intent.putExtra("shoppingListId", shoppingList.getId());
            intent.putExtra("shoppingListName", shoppingList.getName());
            intent.putExtra("storageId", shoppingList.getStorageId());
            startActivity(intent);
        });

        adapter.setOnLongClickListener(view -> {
            setShoppingList(view);
            registerForContextMenu(recyclerView);
            return false;
        });
    }

    /**
     * Loads the shopping lists associated to the user
     */
    private void fillShoppingListsList() {
        Query storageQuery = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        storageQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                adapter.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage st = ds.getValue(Storage.class);

                    if (st.getShoppingLists() != null) {
                        for (Map.Entry<String, Boolean> entry : st.getShoppingLists().entrySet()) {
                            Query shoppingListQuery = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(entry.getKey());
                            shoppingListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        ShoppingList shoppingList1 = dataSnapshot.getValue(ShoppingList.class);

                                        adapter.add(shoppingList1);
                                        adapter.notifyItemInserted(adapter.getItemCount());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Utils.connectionError(ShoppingListsListActivity.this);
                                }
                            });
                        }
                    }
                }
                /*if (shoppingListsList.isEmpty()) {
                    txtNoShoppingListsAvailable.setVisibility(View.VISIBLE);
                } else {
                    txtNoShoppingListsAvailable.setVisibility(View.INVISIBLE);
                }*/
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListsListActivity.this);
            }
        });

        /*Query query = SHOPPING_LIST_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        // Set the database to get all shopping lists of the storages related to the user
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                adapter.clear();
                // Get every shopping list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    // TODO CHECK USERS WITH STORAGE, NOT WITH SHOPPINGLIST USERS
                    if (shoppingList.getUsers() != null && shoppingList.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        adapter.add(shoppingList);
                    }
                    txtNoShoppingListsAvailable.setVisibility(View.INVISIBLE);
                }
                adapter.notifyDataSetChanged();
                if (shoppingListsList.isEmpty()) {
                    txtNoShoppingListsAvailable.setVisibility(View.VISIBLE);
                } else {
                    txtNoShoppingListsAvailable.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListsListActivity.this);
            }
        });*/
    }

    private void setShoppingList(View view) {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolder.getAdapterPosition();
        shoppingList = shoppingListsList.get(position);
    }
}