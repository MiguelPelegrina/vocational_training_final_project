package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.menu_item_delete_shopping_list;
import static com.example.trabajofingrado.R.id.menu_item_modify_shopping_list_name;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.deleteShoppingListDialog;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.updateShoppingListNameDialog;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Controller that handles the use cases of the shopping lists list:
 * - See all shopping lists
 * - Delete a shopping list
 * - Update the name of a shopping list
 * - Search for specific shopping list
 */
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
    private ValueEventListener valueEventListener;

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
        // Check the context menu item
        switch (item.getItemId()) {
            case menu_item_modify_shopping_list_name:
                updateShoppingListNameDialog(ShoppingListsListActivity.this, shoppingList.getId()).show();
                break;
            case menu_item_delete_shopping_list:
                deleteShoppingListDialog(ShoppingListsListActivity.this, shoppingList.getId(), adapter, searchCriteria).show();
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        STORAGE_REFERENCE.removeEventListener(valueEventListener);
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddShoppingList = findViewById(R.id.btnAddShoppingList);
        drawerLayout = findViewById(R.id.drawer_layout_shopping_lists);
        recyclerView = findViewById(R.id.rvShoppingListsList);
        toolbar = findViewById(R.id.toolbar_shopping_lists);
        txtNoShoppingListsAvailable = findViewById(R.id.txtNoShoppingListsAvailable);
    }

    /**
     * Instances the searchView to enable to filter by shopping list name or storage
     *
     * @param menu
     */
    private void setSearchView(Menu menu) {
        // Set the search view
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_shopping_lists);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();

        // Configure the search view
        searchView.setQueryHint("Search by name or storage");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Set the search criteria
                searchCriteria = s;

                // Filter the adapter
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

        // Set a divider
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
            // Move to the storage list activity
            Intent intent = new Intent(ShoppingListsListActivity.this, StorageListActivity.class);
            intent.putExtra("activity", "addShoppingList");
            startActivity(intent);
        });

        adapter.setOnClickListener(view -> {
            setShoppingList(view);

            // Move to the shopping list detail activity
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
        // Search the storages of the user
        Query storageQuery = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());

        // Instance the value event listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual storage lists
                recyclerView.getRecycledViewPool().clear();
                adapter.clear();

                // Loop through the storages
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage st = ds.getValue(Storage.class);

                    // Check if the storage has any shopping lists
                    if (st.getShoppingLists() != null) {
                        // Loop through the stopping lists
                        for (Map.Entry<String, Boolean> entry : st.getShoppingLists().entrySet()) {
                            // Search the shopping lists in the database
                            Query shoppingListQuery = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(entry.getKey());
                            shoppingListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Loop through the shopping lists
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        // Get the shopping list
                                        ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                                        // Add the shopping list to the adapter
                                        adapter.add(shoppingList);
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Utils.connectionError(ShoppingListsListActivity.this);
                                }
                            });
                        }
                    }
                    //txtNoShoppingListsAvailable.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListsListActivity.this);
            }
        };

        storageQuery.addValueEventListener(valueEventListener);
    }

    /**
     * Set te selected shopping list
     *
     * @param view
     */
    private void setShoppingList(View view) {
        // Get the view holder
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();

        // Get the position
        position = viewHolder.getAdapterPosition();

        // Get the shopping list
        shoppingList = shoppingListsList.get(position);
    }
}