package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageProductRecyclerAdapter;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ProductListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    // Fields
    // Of class
    private static final int ADD_AMOUNT = 1;
    private static final int SUBSTRACT_AMOUNT = 2;
    private static final int PRODUCT_ADD_REQUEST_CODE = 1;

    // Of instance
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ArrayList<StorageProduct> storageProductList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private StorageProduct storageProduct;
    private FloatingActionButton btnAddProduct;
    private View auxView;
    private String amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_storage_list);

        setTitle(getIntent().getStringExtra("storage"));

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout();

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_ADD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String productName = data.getStringExtra("description");
                String productUnits = data.getStringExtra("unitType");

                createAddProductDialog(productName, productUnits).show();
            }
        }
    }

    /**
     * Handles the selected items of the navigation bar
     * @param item The selected item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Check the selected item
        Utils.setupNavigationSelection(item, ProductListActivity.this);

        // Close the drawer
        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Handles the "Back" call, closing the drawer if pressed
     */
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    /**
     * Instances the options menu to enable to filter the product list
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.product_search_filter_menu, menu);

        // Configure the searchView
        this.setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.modify_storage_product_menu, menu);
        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        viewHolder = (RecyclerView.ViewHolder) auxView.getTag();
        position = viewHolder.getAdapterPosition();
        storageProduct = storageProductList.get(position);
        switch (item.getItemId()){
            case R.id.modifyProduct:
                createModifyProductDialog().show();
                break;
            case R.id.addAmount:
                createCalculateAmountDialog(storageProduct, ADD_AMOUNT).show();
                break;
            case R.id.substractAmount:
                createCalculateAmountDialog(storageProduct, SUBSTRACT_AMOUNT).show();
                break;
            case R.id.menu_item_delete_recipe_product:
                createDeleteProductDialog(storageProduct).show();
                break;
        }

        return true;
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        this.btnAddProduct = findViewById(R.id.btnAddProduct);
        this.drawerLayout = findViewById(R.id.drawer_layout_recipes);
        this.navigationView = findViewById(R.id.nav_view);
        this.toolbar = findViewById(R.id.toolbar_product_list);
        this.recyclerView = findViewById(R.id.rvProductsStorage);
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
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new StorageProductRecyclerAdapter(storageProductList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        this.fillProductList();
    }

    private void setListener() {
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, AddRecipeProductActivity.class);
                intent.putExtra("action","add");
                startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
            }
        });

        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                registerForContextMenu(recyclerView);
                auxView = view;
                return false;
            }
        });
    }
    /**
     * Instances the searchView to enable to filter by name
     * @param menu
     */
    private void setSearchView(Menu menu) {
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_products);
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


    private void fillProductList(){
        // Get the database instance of the products
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
        // Set the database to get all the products
        Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO MIGHT BE INEFFICIENT
                storageProductList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);
                    for(Map.Entry<String, String>  entry : storage.getProducts().entrySet()){
                        StorageProduct storageProduct = new StorageProduct(entry.getKey(), entry.getValue());
                        storageProductList.add(storageProduct);
                    }
                    recyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ProductListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private AlertDialog createDeleteProductDialog(StorageProduct storageProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete the product " + storageProduct.getDescription())
                .setTitle("Delete product");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                database.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getDescription())
                                        .removeValue();
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ProductListActivity.this, "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                };
                query.addListenerForSingleValueEvent(eventListener);
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

    private AlertDialog createCalculateAmountDialog(StorageProduct storageProduct, int amountCalculation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (amountCalculation){
            case ADD_AMOUNT:
                builder.setMessage("Introduce the amount you want to add to the existent product")
                        .setTitle("Add to the product");
                break;
            case SUBSTRACT_AMOUNT:
                builder.setMessage("Introduce the amount you want to substract from the existent product")
                        .setTitle("Substract from the product");
                break;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView productName = new TextView(this);
        productName.setText(storageProduct.getDescription());
        layout.addView(productName);

        String productAmount = storageProduct.getAmount();
        final EditText inputAmount = new EditText(this);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final TextView productUnits = new TextView(this);
        productUnits.setText(productAmount.substring(productAmount.indexOf(" ")).trim());
        layout.addView(productUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                String value = storage.getProducts().get(productName.getText().toString());
                                String dsValue = value.substring(0, value.indexOf(" "));
                                int sumOfProducts = 0;
                                switch (amountCalculation){
                                    case ADD_AMOUNT:
                                        sumOfProducts = Integer.parseInt(dsValue) + Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                    case SUBSTRACT_AMOUNT:
                                        sumOfProducts = Integer.parseInt(dsValue) - Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                }
                                database.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(productName.getText().toString().trim())
                                        .setValue(sumOfProducts + " " +
                                                productUnits.getText().toString().trim());
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ProductListActivity.this, "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                };
                query.addListenerForSingleValueEvent(eventListener);
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

    private AlertDialog createModifyProductDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the product");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView inputName = new TextView(this);
        inputName.setText(storageProduct.getDescription());
        layout.addView(inputName);

        String amount = storageProduct.getAmount();
        final EditText inputAmount = new EditText(this);
        inputAmount.setText(amount.substring(0, amount.indexOf(" ")));
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final TextView inputUnits = new TextView(this);
        inputUnits.setText(amount.substring(amount.indexOf(" ")));
        layout.addView(inputUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                database.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getDescription())
                                        .removeValue();
                                database.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getDescription())
                                        .setValue(inputAmount.getText().toString().trim() + " "
                                                +  inputUnits.getText().toString());
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ProductListActivity.this, "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                });
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

    private AlertDialog createAddProductDialog(String name, String units){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the amount of " + name + " in " + units);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);

        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                if(storage.getProducts().containsKey(name)){
                                    String value = storage.getProducts().get(name);
                                    String dsValue = value.substring(0, value.indexOf(" "));
                                    int sumOfProducts = Integer.parseInt(dsValue) + Integer.parseInt(inputAmount.getText().toString());
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .setValue( sumOfProducts + " "
                                                    +  units);
                                    Toasty.info(ProductListActivity.this, "The " +
                                            "product already exists so the introduced amount " +
                                            "was added to the existent instead.").show();
                                }else{
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .setValue(inputAmount.getText().toString() + " " +
                                                    units);
                                }
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ProductListActivity.this, "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                });
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
}