package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.cbProduct;
import static com.example.trabajofingrado.R.id.menu_item_modify_shopping_list_name;
import static com.example.trabajofingrado.R.id.menu_item_delete_shopping_list;
import static com.example.trabajofingrado.R.id.txtDeleteShoppingListProduct;
import static com.example.trabajofingrado.io.ShoppingListPutController.createNewShoppingList;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.deleteShoppingListDialog;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.updateShoppingListNameDialog;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.checkValidString;
import static com.example.trabajofingrado.utilities.Utils.connectionError;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListProductRecyclerAdapter;
import com.example.trabajofingrado.interfaces.RecyclerViewActionListener;
import com.example.trabajofingrado.io.ShoppingListPutController;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles all the use related to managing a shopping list
 */
public class ShoppingListDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int PRODUCT_ADD_REQUEST_CODE = 1, STORAGE_CHOICE_RESULT_CODE = 2;

    // Of instance
    private final ArrayList<StorageProduct> productList = new ArrayList<>(),
            boughtProductList = new ArrayList<>();
    private Button btnAddBoughtProductsToStorage, btnAddProduct;
    private RecyclerView rvBoughtProducts, rvProducts;
    private RecyclerViewActionListener rvBoughtProductsActionListener, rvProductsActionListener;
    private ShoppingListProductRecyclerAdapter raBoughtProducts, raProducts;
    private StorageProduct product;
    private String shoppingListId;
    private TextView txtLastEdited;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        // Check the intent:
        // If the no shopping list id is set, the user will create a new one
        if (getIntent().getStringExtra("shoppingListId") == null) {
            // Get the data from the intent
            String storageName = getIntent().getStringExtra("storageName");
            String storageId = getIntent().getStringExtra("storageId");

            addShoppingListDialog(storageId, storageName).show();
        } else {
            // Set the information from the intent
            setTitle(getIntent().getStringExtra("shoppingListName"));
            shoppingListId = getIntent().getStringExtra("shoppingListId");
        }

        bindViews();

        setDrawerLayout(R.id.nav_shopping_lists_list);

        setRecyclerView();

        setListener();

        if (shoppingListId != null) {
            fillShoppingList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_ADD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String productName = data.getStringExtra("name");
                String productUnits = data.getStringExtra("unitType");

                addProductDialog(productName, productUnits).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify_delete_shopping_list_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case menu_item_modify_shopping_list_name:
                updateShoppingListNameDialog(ShoppingListDetailActivity.this, shoppingListId).show();
                break;
            case menu_item_delete_shopping_list:
                deleteShoppingListDialog(ShoppingListDetailActivity.this, shoppingListId, null, null).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SHOPPING_LIST_REFERENCE.removeEventListener(valueEventListener);
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddProduct = findViewById(R.id.btnAddShoppingListProduct);
        btnAddBoughtProductsToStorage = findViewById(R.id.btnAddBoughtProductsToStorage);
        txtLastEdited = findViewById(R.id.txtLastEdited);
        drawerLayout = findViewById(R.id.drawer_layout_shopping_lists);
        toolbar = findViewById(R.id.toolbar_shopping_list);
        rvProducts = findViewById(R.id.rvShoppingListDetailActivityProducts);
        rvBoughtProducts = findViewById(R.id.rvShoppingListDetailActivityBoughtProducts);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        rvProductsActionListener = (clickedViewId, clickedItemPosition) -> {
            // Get the product
            product = productList.get(clickedItemPosition);

            // Check the view holder item
            switch (clickedViewId) {
                case cbProduct:
                    updateShoppingListWithBoughtProduct();
                    break;
                case txtDeleteShoppingListProduct:
                    deleteShoppingListProduct();
                    break;
            }
        };

        rvBoughtProductsActionListener = (clickedViewId, clickedItemPosition) -> {
            // Get the bought product
            product = boughtProductList.get(clickedItemPosition);

            // Check the view holder item
            switch (clickedViewId) {
                case cbProduct:
                    updateShoppingListWithProduct();
                    break;
                case txtDeleteShoppingListProduct:
                    deleteShoppingListBoughtProduct();
                    break;
            }
        };

        // Instance the adapters
        raProducts = new ShoppingListProductRecyclerAdapter(productList, rvProductsActionListener, false);
        raBoughtProducts = new ShoppingListProductRecyclerAdapter(boughtProductList, rvBoughtProductsActionListener, true);

        // Instance the layout manager
        LinearLayoutManager layoutManagerProducts = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerBoughtProducts = new LinearLayoutManager(this);

        // Configure the recycler view
        rvProducts.setAdapter(raProducts);
        rvBoughtProducts.setAdapter(raBoughtProducts);
        rvProducts.setLayoutManager(layoutManagerProducts);
        rvBoughtProducts.setLayoutManager(layoutManagerBoughtProducts);
    }

    /**
     * Set the listener of all the views
     */
    private void setListener() {
        btnAddProduct.setOnClickListener(view -> moveToProductList());

        btnAddBoughtProductsToStorage.setOnClickListener(view -> {
            // Search the storage from the database
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(getIntent().getStringExtra("storageId"));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Get the storage
                        Storage storage = ds.getValue(Storage.class);

                        // Loop through the bought products of the shopping list
                        for (StorageProduct storageProduct : boughtProductList) {
                            // Get the name of the product
                            String name = storageProduct.getName();

                            // Interface whose method will be execute when the storage gets updated
                            OnCompleteListener<Void> storageUpdated = new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    // Remove the bought products from the shopping list
                                    SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(shoppingListId))
                                            .child("boughtProducts")
                                            .removeValue();

                                    // Update the time stamp the last time the shopping list was modified
                                    SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(shoppingListId))
                                            .child("lastEdited")
                                            .setValue(Utils.getCurrentTime());
                                }
                            };

                            // Check if the has the product already
                            if (storage.getProducts() != null && storage.getProducts().containsKey(name)) {
                                // Update the amount of the product if it already exists
                                int sumOfProducts = storage.getProducts().get(name).getAmount() + storageProduct.getAmount();

                                // Update the storage
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(name)
                                        .child("amount")
                                        .setValue(sumOfProducts)
                                        // Update the shopping list
                                        .addOnCompleteListener(storageUpdated);
                            } else {
                                // // Update the storage with product if it didn't exist before
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getName())
                                        .setValue(storageProduct)
                                        // Update the shopping list
                                        .addOnCompleteListener(storageUpdated);
                            }

                            // Inform the user
                            Toasty.success(ShoppingListDetailActivity.this, "Storage refilled").show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    connectionError(ShoppingListDetailActivity.this);
                }
            });
        });
    }

    /**
     * Moves the user to the product list to choose a product they want to add to the shopping list
     */
    private void moveToProductList() {
        Intent intent = new Intent(ShoppingListDetailActivity.this, ShowProductListActivity.class);
        intent.putExtra("action", "add");
        startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
    }

    /**
     * Load the shopping list from the database
     */
    private void fillShoppingList() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the lists
                productList.clear();
                boughtProductList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList != null) {

                        // Check if the shopping list has any products
                        if (shoppingList.getProducts() != null) {
                            // Loop through the products
                            for (Map.Entry<String, StorageProduct> entry : shoppingList.getProducts().entrySet()) {
                                // Generate a new product
                                StorageProduct product = new StorageProduct(
                                        entry.getValue().getAmount(),
                                        entry.getValue().getName(),
                                        entry.getValue().getUnitType());

                                // Add the product to the product list
                                productList.add(product);
                            }
                        }

                        if (shoppingList.getBoughtProducts() != null) {
                            // Loop through the bought products
                            for (Map.Entry<String, StorageProduct> entry : shoppingList.getBoughtProducts().entrySet()) {
                                // Generate a new product
                                StorageProduct product = new StorageProduct(
                                        entry.getValue().getAmount(),
                                        entry.getValue().getName(),
                                        entry.getValue().getUnitType());

                                // Add the product to the bought product list
                                boughtProductList.add(product);
                            }
                        }

                        // Show the last time the shopping list that the shopping list was modified
                        txtLastEdited.setText("Edited: " + shoppingList.getLastEdited());
                    }

                    raProducts.notifyDataSetChanged();
                    raBoughtProducts.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(ShoppingListDetailActivity.this);
            }
        };

        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addValueEventListener(valueEventListener);
    }

    /**
     * Updates the shopping list with the bought product
     */
    private void updateShoppingListWithBoughtProduct() {
        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Check if the shopping list has any products
                    if (shoppingList.getProducts() != null) {
                        // Get the reference of the shopping list
                        DatabaseReference shoppingListRef = SHOPPING_LIST_REFERENCE.child(ds.getKey());

                        // Generate the updates map
                        Map<String, Object> updates = new HashMap<>();

                        // Set the update to remove the product from the product list
                        updates.put("products/" + product.getName(), null);

                        // Check if the bought product list already contains the product
                        if (shoppingList.getBoughtProducts() != null && shoppingList.getBoughtProducts().containsKey(product.getName())) {
                            // Calculate the new amount
                            int newProductAmount = product.getAmount() + shoppingList.getBoughtProducts().get(product.getName()).getAmount();

                            // Set the amount
                            product.setAmount(newProductAmount);
                        }

                        // Set the update to add a product as bought
                        updates.put("boughtProducts/" + product.getName(), product);

                        // Set the update to modify the last edited time
                        updates.put("lastEdited", Utils.getCurrentTime());

                        // Update the shopping list
                        shoppingListRef.updateChildren(updates, (error, ref) -> {
                            if (error != null) {
                                // Inform the user
                                connectionError(ShoppingListDetailActivity.this);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    /**
     * Updates the shopping list with the product
     */
    private void updateShoppingListWithProduct() {
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Check if the shopping list has any bought products
                    if (shoppingList.getBoughtProducts() != null) {
                        // Get the reference of the shopping list
                        DatabaseReference shoppingListRef = SHOPPING_LIST_REFERENCE.child(ds.getKey());

                        // Generate the updates map
                        Map<String, Object> updates = new HashMap<>();

                        // Set the update to add the product to the product list
                        updates.put("products/" + product.getName(), product);

                        // Check if the product list already contains the product
                        if (shoppingList.getProducts() != null && shoppingList.getProducts().containsKey(product.getName())) {
                            // Calculate the new amount
                            int newProductAmount = product.getAmount() + shoppingList.getProducts().get(product.getName()).getAmount();

                            // Set the amount
                            product.setAmount(newProductAmount);
                        }

                        // Set the update to remove a bought product
                        updates.put("boughtProducts/" + product.getName(), null);

                        // Set the update to modify the last edited time
                        updates.put("lastEdited", Utils.getCurrentTime());

                        // Update the shopping list
                        shoppingListRef.updateChildren(updates, (error, ref) -> {
                            if (error != null) {
                                // Inform the user
                                connectionError(ShoppingListDetailActivity.this);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    /**
     * Delete a product from the shopping list
     */
    private void deleteShoppingListProduct() {
        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Check if it has any products
                    if (shoppingList.getProducts() != null) {
                        // Remove the product
                        SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                .child("products")
                                .child(product.getName())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Inform the user
                connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    /**
     * Delete a bought product from the shopping list
     */
    private void deleteShoppingListBoughtProduct() {
        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Check if it has any products
                    if (shoppingList.getBoughtProducts() != null) {
                        // Remove the bought product
                        SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                .child("boughtProducts")
                                .child(product.getName())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Inform the user
                connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    /**
     * Creates an alert dialog so that the user can add a product
     * @param productName
     * @param unitsType
     */
    private AlertDialog addProductDialog(String productName, String unitsType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Introduce the amount of " + productName + " in " + unitsType);

        // Configure the edit text so the user can introduce the amount
        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            addProduct(productName, unitsType, inputAmount.getText().toString());
        });

        return builder.create();
    }

    /**
     * Adds a product to the shopping list
     * @param productName
     * @param unitsType
     * @param productAmount
     */
    private void addProduct(String productName, String unitsType, String productAmount) {
        // Check if the introduce amount is valid
        if (checkValidString(productAmount)) {
            // Search the shopping list
            Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Get the shopping list
                        ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                        if (shoppingList != null) {
                            // Set the product
                            product = new StorageProduct(Integer.parseInt(productAmount),
                                    productName, unitsType);

                            // Check if the shopping list has any products
                            if (shoppingList.getProducts() != null) {
                                // Check if the shopping list contains the product already
                                if (shoppingList.getProducts().containsKey(productName)) {
                                    // Get the product
                                    StorageProduct product1 = shoppingList.getProducts().get(productName);

                                    // Modify the amount of the product
                                    product.setAmount(product1.getAmount() + Integer.parseInt(productAmount));

                                    // Update a product if it already exists
                                    SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(productName)
                                            .setValue(product);

                                    // Inform the user
                                    Toasty.info(ShoppingListDetailActivity.this, "The " +
                                            "product already exists so the introduced amount " +
                                            "was added to the existent instead.").show();
                                } else {
                                    // Set a product if it didn't exist before
                                    SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(productName)
                                            .setValue(product);
                                }
                            } else {
                                // Set a product when the list is empty
                                SHOPPING_LIST_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(productName)
                                        .setValue(product);
                            }
                        }
                        raProducts.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Inform the user
                    connectionError(ShoppingListDetailActivity.this);
                }
            });
        } else {
            // Inform the user
            enterValidData(ShoppingListDetailActivity.this);
        }
    }

    /**
     * Create an alert dialog to create a new shopping list
     * @param storageId
     * @param storageName
     */
    private AlertDialog addShoppingListDialog(String storageId, String storageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListDetailActivity.this);

        // Configure the builder
        builder.setTitle("Add a shopping list to " + storageName + ".");

        // Configure the edit text so the user can introduce the name of the shopping list
        final EditText inputShoppingListName = new EditText(this);
        inputShoppingListName.setHint("Name of shopping list");
        builder.setView(inputShoppingListName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // TODO PRIVATE NEW METHOD
            addShoppingList(inputShoppingListName.getText().toString(), storageId);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            // Move to the shopping lists list
            startActivity(new Intent(ShoppingListDetailActivity.this, ShoppingListsListActivity.class));
        });

        return builder.create();
    }

    /**
     * Save a shopping list into the database
     * @param shoppingListName
     * @param storageId
     */
    private void addShoppingList(String shoppingListName, String storageId) {
        // Check if the string is valid
        if (checkValidString(shoppingListName)) {
            // Search the storage
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Get the storage
                        Storage storage = ds.getValue(Storage.class);

                        createNewShoppingList(ShoppingListDetailActivity.this, storage,
                                shoppingListName, false);

                        setTitle(shoppingListName);

                        // Update the last edited time
                        txtLastEdited.setText("Edited: " + Utils.getCurrentTime());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Inform the user
                    connectionError(ShoppingListDetailActivity.this);
                }
            });

        } else {
            // Inform the user
            enterValidData(ShoppingListDetailActivity.this);
        }
    }
}
