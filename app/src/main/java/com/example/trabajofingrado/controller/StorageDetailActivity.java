package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.options_menu_item_create_shopping_list;
import static com.example.trabajofingrado.R.id.options_menu_item_get_available_recipes;
import static com.example.trabajofingrado.R.id.options_menu_item_share_storage;
import static com.example.trabajofingrado.R.id.options_menu_item_change_storage_name;
import static com.example.trabajofingrado.R.id.options_menu_item_leave_storage;
import static com.example.trabajofingrado.R.id.modifyProduct;
import static com.example.trabajofingrado.R.id.addAmount;
import static com.example.trabajofingrado.R.id.substractAmount;
import static com.example.trabajofingrado.R.id.deleteRecipeProduct;
import static com.example.trabajofingrado.utilities.StorageListInputDialogs.addShoppingListDialog;
import static com.example.trabajofingrado.utilities.StorageListInputDialogs.leaveStorageDialog;
import static com.example.trabajofingrado.utilities.StorageListInputDialogs.updateStorageNameDialog;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.connectionError;
import static com.example.trabajofingrado.utilities.Utils.copyStorageIdToClipboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use cases related to one storage:
 *  - Add a product
 *  - Delete a product
 *  - Update a product
 *  - Update the storage name
 *  - Leave the storage
 *  - Create a new shopping list
 *  - Check which recipes are available with the products of this storage
 */
public class StorageDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int ADD_AMOUNT = 1, SUBTRACT_AMOUNT = 2, PRODUCT_ADD_REQUEST_CODE = 1;

    // Of instance
    private int position;
    private final ArrayList<StorageProduct> storageProductList = new ArrayList<>();
    private FloatingActionButton btnAddProduct;
    private RecyclerView recyclerView;
    private StorageProduct storageProduct;
    private StorageProductRecyclerAdapter adapter;
    private String searchCriteria, storageId, storageName;
    private TextView tvProductAmount, tvProductStorageName, txtNoProductsAvailable;
    private ValueEventListener valueEventListener;
    private View auxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_detail);

        // Get the data from the intent
        storageName = getIntent().getStringExtra("storageName");
        storageId = getIntent().getStringExtra("storageId");

        setTitle("Storage: " + storageName);

        bindViews();

        setDrawerLayout(R.id.nav_storage_list);

        setRecyclerView();

        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        STORAGE_REFERENCE.removeEventListener(valueEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the request code
        if (requestCode == PRODUCT_ADD_REQUEST_CODE) {
            // Check the result
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the data from the intent
                    String productName = data.getStringExtra("name");
                    String productUnits = data.getStringExtra("unitType");

                    addProductDialog(productName, productUnits).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_search_filter_menu, menu);

        setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check the item id
        switch (item.getItemId()) {
            case options_menu_item_create_shopping_list:
                // Generate a users map
                HashMap<String, Boolean> users = new HashMap<>();
                users.put(FirebaseAuth.getInstance().getUid(), true);

                // Generate a storage
                Storage storage = new Storage(storageName, storageId, users);
                addShoppingListDialog(StorageDetailActivity.this, storage).show();
                break;
            case options_menu_item_get_available_recipes:
                // Check if there are any products is empty
                if (storageProductList.isEmpty()) {
                    // Inform the user
                    Toasty.error(StorageDetailActivity.this,
                            "Add products before you attempt to cook anything").show();
                } else {
                    // Moves the user to the recipe list to let them choose a recipe
                    Intent intent = new Intent(StorageDetailActivity.this, RecipeListActivity.class);
                    intent.putExtra("storageId", storageId);
                    startActivity(intent);
                }
                break;
            case options_menu_item_share_storage:
                copyStorageIdToClipboard(StorageDetailActivity.this, storageId);
                break;
            case options_menu_item_change_storage_name:
                updateStorageNameDialog(StorageDetailActivity.this, storageId).show();
                break;
            case options_menu_item_leave_storage:
                leaveStorageDialog(StorageDetailActivity.this, storageId, storageName, null, null).show();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.modify_storage_product_menu, menu);

        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        setStorageProduct();

        // Check the menu item
        switch (item.getItemId()) {
            case modifyProduct:
                modifyProductDialog().show();
                break;
            case addAmount:
                calculateAmountDialog(storageProduct, ADD_AMOUNT).show();
                break;
            case substractAmount:
                calculateAmountDialog(storageProduct, SUBTRACT_AMOUNT).show();
                break;
            case deleteRecipeProduct:
                deleteProductDialog(storageProduct).show();
                break;
        }

        return true;
    }

    // Auxiliary methods

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        btnAddProduct = findViewById(R.id.btnAddProduct);
        drawerLayout = findViewById(R.id.drawer_layout_storages);
        recyclerView = findViewById(R.id.rvProductsStorage);
        toolbar = findViewById(R.id.toolbar_product_list);
        tvProductAmount = findViewById(R.id.tvProductAmount);
        tvProductStorageName = findViewById(R.id.tvProductStorageName);
        txtNoProductsAvailable = findViewById(R.id.txtNoProductsAvailable);
    }

    /**
     * Set the storage product
     */
    private void setStorageProduct() {
        // Get the view holder
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) auxView.getTag();

        // Set the position
        position = viewHolder.getAdapterPosition();

        // Set the storage product
        storageProduct = storageProductList.get(position);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new StorageProductRecyclerAdapter(storageProductList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Add a divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data of the adapter
        fillProductList();
    }

    /**
     * Sets the listener of all the views
     */
    private void setListener() {
        btnAddProduct.setOnClickListener(view -> {
            moveToProductList();
        });

        adapter.setOnLongClickListener(view -> {
            registerForContextMenu(recyclerView);
            auxView = view;
            return false;
        });
    }

    /**
     * Moves the user to the product list to add another product
     */
    private void moveToProductList() {
        Intent intent = new Intent(StorageDetailActivity.this, ShowProductListActivity.class);
        intent.putExtra("action", "add");
        startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
    }

    /**
     * Instances the searchView to enable to filter by name
     *
     * @param menu
     */
    private void setSearchView(Menu menu) {
        // Get the search view
        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_products);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();

        // Configure the search view
        searchView.setQueryHint("Search by name");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Get the criteria
                searchCriteria = s;

                // Get the filter
                adapter.getFilter().filter(searchCriteria);

                return false;
            }
        });
    }

    /**
     * Loads the products of the storage from the database
     */
    private void fillProductList() {
        // Search the products of the storage
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);

        // Instance the value event listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the lists
                adapter.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Check if the storage has any products
                    if (storage.getProducts() != null) {
                        // Loop through the storage products
                        for (Map.Entry<String, StorageProduct> entry : storage.getProducts().entrySet()) {
                            // Add the product to the list
                            adapter.add(entry.getValue());
                        }

                        adapter.notifyDataSetChanged();
                    }

                }

                // Inform the user that the product list is empty if it is empty
                tvProductAmount.setVisibility(storageProductList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
                tvProductStorageName.setVisibility(storageProductList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
                txtNoProductsAvailable.setVisibility(storageProductList.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(StorageDetailActivity.this);
            }
        };

        query.addValueEventListener(valueEventListener);
    }

    /**
     * Creates an alert dialog to ask the user to confirm their choice of deleting a product
     *
     * @param storageProduct
     */
    private AlertDialog deleteProductDialog(StorageProduct storageProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setMessage("Are you sure you want to delete the product " + storageProduct.getName())
                .setTitle("Delete product");

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> deleteProduct());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Deletes a product of the storage from the database
     */
    private void deleteProduct() {
        // Search the storage in the database
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    if (storage != null) {
                        // Remove the product from the storage
                        STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                .child("products")
                                .child(storageProduct.getName())
                                .removeValue().addOnCompleteListener(task ->
                                        // Update the filter to not show the deleted product
                                        adapter.getFilter().filter(searchCriteria));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(StorageDetailActivity.this);
            }
        });
    }

    /**
     * Creates an alert dialog to add or subtract the amount of a product
     *
     * @param storageProduct
     * @param amountCalculation
     */
    private AlertDialog calculateAmountDialog(StorageProduct storageProduct, int amountCalculation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Check if the user wants to add or subtract
        switch (amountCalculation) {
            case ADD_AMOUNT:
                builder.setMessage("Introduce the amount you want to add to the existent product")
                        .setTitle("Add to the product");
                break;
            case SUBTRACT_AMOUNT:
                builder.setMessage("Introduce the amount you want to subtract from the existent product")
                        .setTitle("Subtract from the product");
                break;
        }

        // Configure the builder
        // Set the layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Configure a text view to show the product name
        final TextView productName = new TextView(this);
        productName.setText(storageProduct.getName());
        layout.addView(productName);

        // Configure an edit text to get the user input
        final EditText inputAmount = new EditText(this);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        // Configure a text view to show the unit types
        final TextView productUnits = new TextView(this);
        productUnits.setText(storageProduct.getUnitType());
        layout.addView(productUnits);

        // Set the layout
        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Search the storage
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Get the storage
                        Storage storage = ds.getValue(Storage.class);

                        if (storage != null) {
                            // Get the product
                            StorageProduct product = storage.getProducts().get(productName.getText().toString());

                            int sumOfProducts = 0;

                            // Check what the user wants to do
                            switch (amountCalculation) {
                                case ADD_AMOUNT:
                                    sumOfProducts = product.getAmount() + Integer.parseInt(inputAmount.getText().toString());
                                    break;
                                case SUBTRACT_AMOUNT:
                                    sumOfProducts = product.getAmount() - Integer.parseInt(inputAmount.getText().toString());
                                    break;
                            }

                            // Check the new amount of the product
                            if (sumOfProducts > 0) {
                                // Set the new value
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(productName.getText().toString().trim())
                                        .child("amount")
                                        .setValue(sumOfProducts);
                            } else {
                                // Delete the product if the amount is 0
                                if (sumOfProducts == 0) {
                                    STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(productName.getText().toString().trim())
                                            .removeValue();
                                } else {
                                    // Inform the user
                                    Toasty.error(StorageDetailActivity.this,
                                            "You cannot have negative amounts of " + product.getName()).show();
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    connectionError(StorageDetailActivity.this);
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Creates an alert dialog to modify the amount of a product directly instead of adding or subtracting
     *
     * @return
     */
    private AlertDialog modifyProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Modify the product");

        // Set the layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set a text view to show the product name
        final TextView inputName = new TextView(this);
        inputName.setText(storageProduct.getName());
        layout.addView(inputName);

        // Set an edit text to change the amount
        final EditText inputAmount = new EditText(this);
        inputAmount.setText(storageProduct.getAmount() + "");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        // Set a text to show the product unit type
        final TextView inputUnits = new TextView(this);
        inputUnits.setText(storageProduct.getUnitType());
        layout.addView(inputUnits);

        // Set the layout
        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Search the storage
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Get the storage
                        Storage storage = ds.getValue(Storage.class);

                        if (storage != null) {
                            // Set the new amount
                            storageProduct.setAmount(Integer.parseInt(inputAmount.getText().toString()));

                            // Save the product into the database
                            STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                    .child("products")
                                    .child(storageProduct.getName())
                                    .setValue(storageProduct);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    connectionError(StorageDetailActivity.this);
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Creates an alert dialog to add a product
     *
     * @param productName
     * @param units
     */
    private AlertDialog addProductDialog(String productName, String units) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure
        builder.setTitle("Introduce the amount of " + productName + " in " + units);

        // Set an edit text to get the amount of the product
        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);

        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", (dialog, which) ->
                addProduct(productName, inputAmount.getText().toString(), units)
        );
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Saves or updates the product in the database.
     * @param productName
     * @param productAmount
     * @param productUnits
     */
    private void addProduct(String productName, String productAmount, String productUnits) {
        // Search the storage
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    if (storage != null) {
                        // Check if the storage has any products
                        if (storage.getProducts() != null) {
                            // Check if the product is already in the storage
                            if (storage.getProducts().containsKey(productName)) {
                                // Get the product
                                StorageProduct product = storage.getProducts().get(productName);

                                // Get the sum of product and the new amount
                                int sumOfProducts = product.getAmount() + Integer.parseInt(productAmount);

                                // Update the product in the database
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(productName)
                                        .child("amount")
                                        .setValue(sumOfProducts);

                                // Inform the user
                                Toasty.info(StorageDetailActivity.this, "The " +
                                                "introduced amount was added to the existent product.")
                                        .show();
                            } else {
                                // Set a product if it didn't exist before
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(productName)
                                        .setValue(new StorageProduct(Integer.parseInt(productAmount),
                                                productName, productUnits));
                            }
                        } else {
                            // Set a product when the list is empty
                            STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                    .child("products")
                                    .child(productName)
                                    .setValue(new StorageProduct(Integer.parseInt(productAmount),
                                            productName, productUnits
                                    ));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(StorageDetailActivity.this);
            }
        });
    }
}