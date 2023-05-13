package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class StorageListActivity
        extends BaseActivity{
    // Fields
    private ArrayList<Storage> storageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private Storage storage;
    private DatabaseReference storageReference;
    private TextView txtNoStoragesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list);

        setTitle(R.string.storages);

        this.storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout(R.id.nav_storage_list);

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();
    }

    /**
     * Instances the options menu to enable to filter the storage list
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.storage_search_filter_menu, menu);

        // Configure the searchView
        this.setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_create_new_storage:
                createAddStorageDialog().show();
                break;
            // TODO
            case R.id.menu_item_join_storage:
                createJoinStorageDialog().show();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onContextMenuClosed(menu);

        switch (v.getId()){
            case R.id.rvStorageList:
                getMenuInflater().inflate(R.menu.storage_context_menu, menu);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.context_menu_item_share_storage_code:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("storage access code", storage.getId());
                clipboard.setPrimaryClip(clip);
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
                    Toasty.info(StorageListActivity.this, "The code was copied:" +
                            clipboard.getPrimaryClip().getItemAt(0).toString()).show();
                }
                break;
            case R.id.context_menu_item_leave_storage:
                createLeaveStorageDialog().show();
                break;
            case R.id.context_menu_item_create_shopping_list:
                createAddShoppingListDialog().show();
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
        this.txtNoStoragesAvailable = findViewById(R.id.txtNoStoragesAvailable);
        this.drawerLayout = findViewById(R.id.drawer_layout_storages);
        this.toolbar = findViewById(R.id.toolbar_storages);
        this.recyclerView = findViewById(R.id.rvStorageList);
    }

    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new StorageRecyclerAdapter(storageList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        // Set the data
        this.fillStorageList();
    }

    private void setListener() {
        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Storage storage = storageList.get(viewHolder.getAdapterPosition());
                Intent intent = null;
                switch (getIntent().getStringExtra("activity")) {
                    case "view":
                        intent = new Intent(StorageListActivity.this, ProductListActivity.class);
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
                }
            }
        });

        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                storage = storageList.get(viewHolder.getAdapterPosition());
                registerForContextMenu(recyclerView);
                return false;
            }
        });
    }

    private void fillStorageList() {
        // TODO REFACTOR?
        Query query = storageReference.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);
                    if(storage != null){
                        for(Map.Entry<String, Boolean> user: storage.getUsers().entrySet()){
                            if(user.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                storageList.add(storage);
                            }
                        }
                        txtNoStoragesAvailable.setVisibility(View.INVISIBLE);
                    }
                }
                recyclerAdapter.notifyDataSetChanged();
                if(storageList.isEmpty()){
                    txtNoStoragesAvailable.setVisibility(View.VISIBLE);
                }else{
                    txtNoStoragesAvailable.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(StorageListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    /**
     * Instances the searchView to enable to filter by name or products
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_storages);
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
    }

    private AlertDialog createAddStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Name your storage");

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");

        builder.setView(inputName);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(Utils.checkValidString(inputName.getText().toString())){
                    saveStorage(inputName.getText().toString());
                }else{
                    Toasty.error(StorageListActivity.this, "The name cannot be empty").show();
                }
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

    private void saveStorage(String name){
        HashMap<String, Boolean> users = new HashMap<>();
        users.put(FirebaseAuth.getInstance().getUid(), true);

        Storage storage = new Storage(name, UUID.randomUUID().toString(), users);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(storage.getId(), storage);
        storageReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toasty.success(StorageListActivity.this,
                        "You created a new storage!").show();
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private AlertDialog createAddShoppingListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StorageListActivity.this);

        builder.setTitle("Add a shopping list to " + storage.getName() + ".");

        final EditText input = new EditText(this);
        input.setHint("Name");

        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(Utils.checkValidString(input.getText().toString())){
                    createNewShoppingList(input.getText().toString());
                }else{
                    Toasty.error(StorageListActivity.this, "You need to enter valid data.").show();
                }
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

    private void createNewShoppingList(String name) {
        DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        HashMap<String, Boolean> users = new HashMap<>();
        for(Map.Entry<String, Boolean> user : storage.getUsers().entrySet()){
            users.put(user.getKey(), true);
        }

        String shoppingListId =  UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList(users,
                name, Utils.getCurrentTime(), storage.getId(),
                shoppingListId) ;

        // TODO UPDATE STORAGE WITH SHOPPING LISTS AS WELL

        shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(StorageListActivity.this, ShoppingListDetailActivity.class);
                intent.putExtra("shoppingListId", shoppingList.getId());
                intent.putExtra("shoppingListName", shoppingList.getName());
                startActivity(intent);
            }
        });
    }

    private AlertDialog createLeaveStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StorageListActivity.this);

        builder.setTitle("Are you sure you want to leave " + storage.getName());

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeStorageUser();
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

    private void removeStorageUser() {
        Query query = storageReference.orderByChild("id").equalTo(storage.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);
                    if(storage != null){
                        for(Map.Entry<String, Boolean> user: storage.getUsers().entrySet()){
                            if(user.getKey().trim().equals(FirebaseAuth.getInstance().getUid())){
                                Map<String, Object> childUpdates = new HashMap<>();

                                if(storage.getUsers().entrySet().size() > 1){
                                    childUpdates.put(storage.getId()
                                            + "/users/"
                                            + FirebaseAuth.getInstance().getUid(), null);
                                    storageReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toasty.success(StorageListActivity.this,
                                                    "You left the storage!").show();
                                            recyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }else {
                                    storageReference.child(storage.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            recyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(StorageListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private AlertDialog createJoinStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StorageListActivity.this);

        builder.setTitle("Enter the code of the storage you want join");

        final EditText inputCode = new EditText(this);
        inputCode.setHint("Code");

        builder.setView(inputCode);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String id = inputCode.getText().toString();
                if (Utils.checkValidString(id)){
                    addUser(id);
                }else{
                    Toasty.error(StorageListActivity.this,
                            "You must enter a valid code").show();
                }
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

    private void addUser(String code) {
        Query query = storageReference.orderByChild("id").equalTo(code);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);

                    if (storage != null) {
                        HashMap<String, Boolean> users = storage.getUsers();
                        users.put(FirebaseAuth.getInstance().getUid(), true);
                        storage.setUsers(users);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(storage.getId(), storage);
                        storageReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toasty.success(StorageListActivity.this,
                                        "You joined a storage!").show();
                                //storageList.add(storage);
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }else{
                        Toasty.error(StorageListActivity.this,
                                "The code is no valid").show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(StorageListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }
}