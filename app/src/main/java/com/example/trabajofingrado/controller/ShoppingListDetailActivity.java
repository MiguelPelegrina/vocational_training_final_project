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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListProductRecyclerAdapter;
import com.example.trabajofingrado.interfaces.RecyclerViewActionListener;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
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
import java.util.Objects;

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
    private Button btnAddBoughtProductsToStorage;
    private ArrayList<StorageProduct> productList = new ArrayList<>();
    private ArrayList<StorageProduct> boughtProductList = new ArrayList<>();
    private RecyclerView rvProducts;
    private RecyclerView rvBoughtProducts;
    private ShoppingListProductRecyclerAdapter raProducts;
    private ShoppingListProductRecyclerAdapter raBoughtProducts;
    private DatabaseReference shoppingListReference;
    private RecyclerViewActionListener rvProductsActionListener;
    private RecyclerViewActionListener rvBoughtProductsActionListener;
    private StorageProduct product;
    private String shoppingListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        // Set the information from the intent
        setTitle(getIntent().getStringExtra("shoppingListName"));
        this.shoppingListId = getIntent().getStringExtra("shoppingListId");

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

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.btnAddProduct = findViewById(R.id.btnAddShoppingListProduct);
        this.btnAddBoughtProductsToStorage = findViewById(R.id.btnAddBoughtProductsToStorage);
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
                        product = productList.get(clickedItemPosition);
                        updateShoppingListWithBoughtProduct();
                        break;
                    case R.id.txtShoppingListProductName:

                        break;
                    case R.id.txtShoppingListProductAmount:

                        break;
                    case R.id.txtDeleteShoppingListProduct:
                        product = productList.get(clickedItemPosition);
                        deleteShoppingListProduct();
                        break;
                }
            }
        };

        rvBoughtProductsActionListener = new RecyclerViewActionListener() {
            @Override
            public void onViewClicked(int clickedViewId, int clickedItemPosition) {
                switch (clickedViewId){
                    case R.id.cbProduct:
                        product = boughtProductList.get(clickedItemPosition);
                        updateShoppingListWithUnboughtProduct();
                        break;
                    case R.id.txtShoppingListProductName:

                        break;
                    case R.id.txtShoppingListProductAmount:

                        break;
                    case R.id.txtDeleteShoppingListProduct:
                        product = productList.get(clickedItemPosition);
                        deleteShoppingListBoughtProduct();
                        break;
                }
            }
        };

        // Instance the adapters
        this.raProducts = new ShoppingListProductRecyclerAdapter(productList, rvProductsActionListener, false);
        this.raBoughtProducts = new ShoppingListProductRecyclerAdapter(boughtProductList, rvBoughtProductsActionListener, true);

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

        btnAddBoughtProductsToStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Log.d("storage id", getIntent().getStringExtra("storageId"));
                Query query = storageReference.orderByChild("id").equalTo(getIntent().getStringExtra("storageId"));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            for(StorageProduct storageProduct : boughtProductList){
                                String name = storageProduct.getDescription();
                                if(storage.getProducts().containsKey(name)){
                                    // Update a product if it already exists
                                    String value = storage.getProducts().get(name);
                                    String dsValue = value.substring(0, value.indexOf(" "));
                                    String productValue = storageProduct.getAmount().substring(0, storageProduct.getAmount().indexOf(" "));
                                    String productUnits = storageProduct.getAmount().substring(storageProduct.getAmount().indexOf(" "), storageProduct.getAmount().length());
                                    int sumOfProducts = Integer.parseInt(dsValue) + Integer.parseInt(productValue);
                                    storageReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .setValue( sumOfProducts + " "
                                                    +  productUnits);
                                }else{
                                    // Set a product if it didnt exist before
                                    storageReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(storageProduct.getDescription())
                                            .setValue(storageProduct.getAmount());
                                }

                                // Delete the items from the bought products list of the shopping list
                                shoppingListReference.child(shoppingListId)
                                        .child("boughtProducts")
                                        .removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                });
            }
        });

        //
        this.navigationView.setNavigationItemSelectedListener(this);

        //
        this.drawerLayout.addDrawerListener(this.toggle);
    }

    private void fillShoppingLists(){
        // Set the database to get all shopping lists
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                boughtProductList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList != null){
                        if(shoppingList.getNeedToBuyProducts() != null){
                            for (Map.Entry<String, String> entry: shoppingList.getNeedToBuyProducts().entrySet()){
                                StorageProduct product = new StorageProduct(entry.getKey(), entry.getValue());
                                productList.add(product);
                            }
                        }

                        if(shoppingList.getBoughtProducts() != null){
                            for (Map.Entry<String, String> entry : shoppingList.getBoughtProducts().entrySet()){
                                StorageProduct product = new StorageProduct(entry.getKey(), entry.getValue());
                                boughtProductList.add(product);
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

    private void updateShoppingListWithBoughtProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList.getNeedToBuyProducts() != null){
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("needToBuyProducts")
                                .child(product.getDescription())
                                .removeValue();

                        shoppingListReference.child(ds.getKey())
                                .child("boughtProducts")
                                .child(product.getDescription())
                                .setValue(product.getAmount());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void updateShoppingListWithUnboughtProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList.getBoughtProducts() != null){
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("needToBuyProducts")
                                .child(product.getDescription())
                                .setValue(product.getAmount());

                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("boughtProducts")
                                .child(product.getDescription())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void deleteShoppingListProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList.getNeedToBuyProducts() != null){
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("needToBuyProducts")
                                .child(product.getDescription())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private void deleteShoppingListBoughtProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if(shoppingList.getBoughtProducts() != null){
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("boughtProducts")
                                .child(product.getDescription())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ShoppingListDetailActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }

    private AlertDialog createAddProductDialog(String name, String units) {
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
                Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                            if (shoppingList != null) {
                                if(shoppingList.getNeedToBuyProducts() != null){
                                    if(shoppingList.getNeedToBuyProducts().containsKey(name)){
                                        // Update a product if it already exists
                                        String value = shoppingList.getNeedToBuyProducts().get(name);
                                        String dsValue = value.substring(0, value.indexOf(" "));
                                        int sumOfProducts = Integer.parseInt(dsValue) + Integer.parseInt(inputAmount.getText().toString());
                                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                                .child("needToBuyProducts")
                                                .child(name)
                                                .setValue( sumOfProducts + " "
                                                        +  units);
                                        Toasty.info(ShoppingListDetailActivity.this, "The " +
                                                "product already exists so the introduced amount " +
                                                "was added to the existent instead.").show();
                                    }else{
                                        // Set a product if it didnt exist before
                                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                                .child("needToBuyProducts")
                                                .child(name)
                                                .setValue(inputAmount.getText().toString() + " " +
                                                        units);
                                    }
                                }else{
                                    // Set a product when the list is empty
                                    shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("needToBuyProducts")
                                            .child(name)
                                            .setValue( inputAmount.getText() + " "
                                                    +  units);
                                    // txtEmptyStorage.setVisibility(View.INVISIBLE);
                                }
                            }
                            raProducts.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(ShoppingListDetailActivity.this,
                                "An error trying to access " +
                                "the database happened. Check your internet connection").show();
                    }
                });
            }
        });

        return builder.create();
    }
}