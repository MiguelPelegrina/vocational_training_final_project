package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.*;
import static com.example.trabajofingrado.R.id.context_menu_item_change_storage_name;
import static com.example.trabajofingrado.R.id.context_menu_item_create_shopping_list;
import static com.example.trabajofingrado.R.id.context_menu_item_leave_storage;
import static com.example.trabajofingrado.R.id.context_menu_item_share_storage_code;
import static com.example.trabajofingrado.R.id.menu_item_create_new_storage;
import static com.example.trabajofingrado.R.id.menu_item_join_storage;
import static com.example.trabajofingrado.R.id.rvStorageList;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.io.ShoppingListPutController;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.StorageListInputDialogs;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class StorageListActivity
        extends BaseActivity{
    // Fields
    private final ArrayList<Storage> storageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;
    private String searchCriteria;
    private Storage storage;
    private StorageRecyclerAdapter adapter;
    private TextView txtNoStoragesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_storage_list);

        setTitle("Your storages");

        bindViews();

        setDrawerLayout(id.nav_storage_list);

        setRecyclerView();

        setListener();
    }

    /**
     * Instances the options menu to enable to filter the storage list
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.storage_search_filter_menu, menu);

        setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check the item
        switch (item.getItemId()){
            case menu_item_create_new_storage:
                createAddStorageDialog().show();
                break;
            case menu_item_join_storage:
                createJoinStorageDialog().show();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onContextMenuClosed(menu);

        // Check the item id
        if (v.getId() == rvStorageList) {
            getMenuInflater().inflate(R.menu.storage_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Check the item id
        switch (item.getItemId()){
            case context_menu_item_create_shopping_list:
                createAddShoppingListDialog().show();
                break;
            case context_menu_item_share_storage_code:
                copyStorageIdToClipboard();
                break;
            case context_menu_item_change_storage_name:
                StorageListInputDialogs.updateStorageNameDialog(StorageListActivity.this, storage.getId()).show();
                break;
            case context_menu_item_leave_storage:
                StorageListInputDialogs.leaveStorageDialog(StorageListActivity.this,
                        storage.getId(), storage.getName(), adapter, searchCriteria).show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        txtNoStoragesAvailable = findViewById(id.txtNoStoragesAvailable);
        drawerLayout = findViewById(id.drawer_layout_storages);
        toolbar = findViewById(id.toolbar_storages);
        recyclerView = findViewById(rvStorageList);
    }

    /**
     * Sets the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new StorageRecyclerAdapter(storageList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Add a line to divide the items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data
        fillStorageList();
    }

    /**
     * Sets the listener of the views
     */
    private void setListener() {
        adapter.setOnClickListener(view -> {
            viewHolder = (RecyclerView.ViewHolder) view.getTag();
            Storage storage = storageList.get(viewHolder.getAdapterPosition());

            Intent intent;
            switch (getIntent().getStringExtra("activity")) {
                case "addShoppingList":
                    intent = new Intent(StorageListActivity.this, ShoppingListDetailActivity.class);
                    intent.putExtra("storageName", storage.getName());
                    intent.putExtra("storageId", storage.getId());
                    startActivity(intent);
                    break;
                case "recipe":
                    // Check if the storage has any products available
                    if(storage.getProducts() != null){
                        intent = new Intent(StorageListActivity.this, RecipeListActivity.class);
                        intent.putExtra("storageId", storage.getId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Toasty.error(StorageListActivity.this, "The storage is " +
                                "empty, you need to fill it first!").show();
                    }
                    break;
                case "view":
                    intent = new Intent(StorageListActivity.this, StorageProductListActivity.class);
                    intent.putExtra("storageName", storage.getName());
                    intent.putExtra("storageId", storage.getId());
                    startActivity(intent);
                    break;
            }
        });

        adapter.setOnLongClickListener(view -> {
            viewHolder = (RecyclerView.ViewHolder) view.getTag();
            storage = storageList.get(viewHolder.getAdapterPosition());
            registerForContextMenu(recyclerView);
            return false;
        });
    }

    /**
     * Loads the storages from the database based on the current user
     */
    private void fillStorageList() {
        // Set the query
        Query query = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);
                    if(storage != null){
                        for(Map.Entry<String, Boolean> user: storage.getUsers().entrySet()){
                            if(user.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                adapter.add(storage);
                            }
                        }
                        txtNoStoragesAvailable.setVisibility(View.INVISIBLE);
                    }
                }

                adapter.notifyDataSetChanged();
                if(storageList.isEmpty()){
                    txtNoStoragesAvailable.setVisibility(View.VISIBLE);
                }else{
                    txtNoStoragesAvailable.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(StorageListActivity.this);
            }
        });
    }

    /**
     * Instances the searchView to enable to filter by name or products
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(id.search_bar_storages);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();
        searchView.setQueryHint("Search by name or products");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchCriteria = s;
                adapter.getFilter().filter(searchCriteria);
                return false;
            }
        });
    }

    private void copyStorageIdToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("storage access code", storage.getId());
        clipboard.setPrimaryClip(clip);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
            Toasty.info(StorageListActivity.this, "Code copied").show();
        }
    }

    private AlertDialog createAddStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Name your storage");

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");

        builder.setView(inputName);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if(Utils.checkValidString(inputName.getText().toString())){
                saveStorage(inputName.getText().toString());
            }else{
                Toasty.error(StorageListActivity.this, "The name cannot be empty").show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private void saveStorage(String name){
        HashMap<String, Boolean> users = new HashMap<>();
        users.put(FirebaseAuth.getInstance().getUid(), true);

        Storage storage = new Storage(name, UUID.randomUUID().toString(), users);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(storage.getId(), storage);
        STORAGE_REFERENCE.updateChildren(childUpdates).addOnCompleteListener(task -> {
            Toasty.success(StorageListActivity.this,
                    "You created a new storage!").show();
            adapter.notifyDataSetChanged();
        });
    }

    private AlertDialog createAddShoppingListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StorageListActivity.this);

        builder.setTitle("Add a shopping list to " + storage.getName() + ".");

        final EditText input = new EditText(this);
        input.setHint("Name");

        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            if(Utils.checkValidString(input.getText().toString())){
                ShoppingListPutController.createNewShoppingList(
                        StorageListActivity.this, storage, input.getText().toString(), true);
            }else{
                Utils.enterValidData(StorageListActivity.this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private AlertDialog createJoinStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StorageListActivity.this);

        builder.setTitle("Enter the code of the storage you want join");

        final EditText inputCode = new EditText(this);
        inputCode.setHint("Code");

        builder.setView(inputCode);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            String id = inputCode.getText().toString();
            if (Utils.checkValidString(id)){
                addUser(id);
            }else{
                Utils.enterValidData(StorageListActivity.this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private void addUser(String code) {
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(code);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);

                    if (storage != null) {
                        if(!storage.getId().equals(code)){
                            HashMap<String, Boolean> users = storage.getUsers();
                            users.put(FirebaseAuth.getInstance().getUid(), true);
                            storage.setUsers(users);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put(storage.getId(), storage);
                            STORAGE_REFERENCE.updateChildren(updates).addOnCompleteListener(task -> {
                                Toasty.success(StorageListActivity.this,
                                        "You joined a storage!").show();
                                adapter.notifyDataSetChanged();
                            });
                        }else{
                            Toasty.error(StorageListActivity.this, "You are already of this storage.").show();
                        }
                    }
                }
                if(!snapshot.hasChildren()){
                        Toasty.error(StorageListActivity.this,
                                "The code is not valid").show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(StorageListActivity.this);
            }
        });
    }
}