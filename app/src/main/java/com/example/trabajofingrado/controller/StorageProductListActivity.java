package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.options_menu_item_get_available_recipes;
import static com.example.trabajofingrado.R.id.options_menu_item_share_storage;
import static com.example.trabajofingrado.R.id.options_menu_item_change_storage_name;
import static com.example.trabajofingrado.R.id.options_menu_item_leave_storage;
import static com.example.trabajofingrado.R.id.modifyProduct;
import static com.example.trabajofingrado.R.id.addAmount;
import static com.example.trabajofingrado.R.id.substractAmount;
import static com.example.trabajofingrado.R.id.menu_item_delete_recipe_product;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.example.trabajofingrado.utilities.StorageListInputDialogs;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class StorageProductListActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int ADD_AMOUNT = 1;
    private static final int SUBTRACT_AMOUNT = 2;
    private static final int PRODUCT_ADD_REQUEST_CODE = 1;

    // Of instance
    private int position;
    private final ArrayList<StorageProduct> storageProductList = new ArrayList<>();
    private FloatingActionButton btnAddProduct;
    private RecyclerView recyclerView;
    private StorageProduct storageProduct;
    private StorageProductRecyclerAdapter adapter;
    private String searchCriteria, storageId, storageName;
    private TextView txtNoProductsAvailable;
    private View auxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_storage_list);

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

                    createAddProductDialog(productName, productUnits).show();
                }
            }
        }
    }

    /**
     * Instances the options menu to enable to filter the product list
     *
     * @param menu
     * @return
     */
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
            case options_menu_item_get_available_recipes:
                if (storageProductList.isEmpty()) {
                    Toasty.error(StorageProductListActivity.this,
                            "Add products before you attempt to cook anything").show();
                } else {
                    Intent intent = new Intent(StorageProductListActivity.this, RecipeListActivity.class);
                    intent.putExtra("storageId", storageId);
                    startActivity(intent);
                }
                break;
            case options_menu_item_share_storage:
                copyStorageCodeToClipboard();
                break;
            case options_menu_item_change_storage_name:
                StorageListInputDialogs.updateStorageNameDialog(
                        StorageProductListActivity.this, storageId).show();
                break;
            case options_menu_item_leave_storage:
                StorageListInputDialogs.leaveStorageDialog(
                        StorageProductListActivity.this, storageId, storageName, null, null).show();
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

        switch (item.getItemId()) {
            case modifyProduct:
                createModifyProductDialog().show();
                break;
            case addAmount:
                createCalculateAmountDialog(storageProduct, ADD_AMOUNT).show();
                break;
            case substractAmount:
                createCalculateAmountDialog(storageProduct, SUBTRACT_AMOUNT).show();
                break;
            case menu_item_delete_recipe_product:
                createDeleteProductDialog(storageProduct).show();
                break;
        }

        return true;
    }

    private void setStorageProduct() {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) auxView.getTag();
        position = viewHolder.getAdapterPosition();
        storageProduct = storageProductList.get(position);
    }

    // Auxiliary methods

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        btnAddProduct = findViewById(R.id.btnAddProduct);
        txtNoProductsAvailable = findViewById(R.id.txtNoProductsAvailable);
        drawerLayout = findViewById(R.id.drawer_layout_storages);
        toolbar = findViewById(R.id.toolbar_product_list);
        recyclerView = findViewById(R.id.rvProductsStorage);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new StorageProductRecyclerAdapter(storageProductList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
            Intent intent = new Intent(StorageProductListActivity.this, ShowProductListActivity.class);
            intent.putExtra("action", "add");
            startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
        });

        adapter.setOnLongClickListener(view -> {
            registerForContextMenu(recyclerView);
            auxView = view;
            return false;
        });
    }

    /**
     * Instances the searchView to enable to filter by name
     *
     * @param menu
     */
    private void setSearchView(Menu menu) {

        MenuItem recipeSearchItem = menu.findItem(R.id.search_bar_products);
        SearchView searchView = (SearchView) recipeSearchItem.getActionView();
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
                searchCriteria = s;
                adapter.getFilter().filter(searchCriteria);
                return false;
            }
        });
    }

    /**
     * Loads the products from the database
     */
    private void fillProductList() {
        // Set the query
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);

                    if (storage.getProducts() != null) {
                        for (Map.Entry<String, StorageProduct> entry : storage.getProducts().entrySet()) {
                            adapter.add(entry.getValue());
                        }
                        txtNoProductsAvailable.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    }

                }
                if (storageProductList.isEmpty()) {
                    txtNoProductsAvailable.setVisibility(View.VISIBLE);
                } else {
                    txtNoProductsAvailable.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(StorageProductListActivity.this);
            }
        });
    }

    private void copyStorageCodeToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("storage access code", storageId);
        clipboard.setPrimaryClip(clip);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toasty.info(StorageProductListActivity.this, "Code copied").show();
        }
    }

    private AlertDialog createDeleteProductDialog(StorageProduct storageProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete the product " + storageProduct.getName())
                .setTitle("Delete product");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
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

    private void deleteProduct() {
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);
                    if (storage != null) {
                        STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                .child("products")
                                .child(storageProduct.getName())
                                .removeValue().addOnCompleteListener(task -> {
                                    adapter.getFilter().filter(searchCriteria);
                                    storageProductList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(StorageProductListActivity.this);
            }
        });
    }

    private AlertDialog createCalculateAmountDialog(StorageProduct storageProduct, int amountCalculation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView productName = new TextView(this);
        productName.setText(storageProduct.getName());
        layout.addView(productName);

        final EditText inputAmount = new EditText(this);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final TextView productUnits = new TextView(this);
        productUnits.setText(storageProduct.getUnitType());
        layout.addView(productUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                StorageProduct product = storage.getProducts().get(productName.getText().toString());

                                int sumOfProducts = 0;
                                switch (amountCalculation) {
                                    case ADD_AMOUNT:
                                        sumOfProducts = product.getAmount() + Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                    case SUBTRACT_AMOUNT:
                                        sumOfProducts = product.getAmount() - Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                }
                                if (sumOfProducts > 0) {
                                    STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(productName.getText().toString().trim())
                                            .child("amount")
                                            .setValue(sumOfProducts);
                                } else {
                                    if (sumOfProducts == 0) {
                                        STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                                .child("products")
                                                .child(productName.getText().toString().trim())
                                                .removeValue();
                                    } else {
                                        Toasty.error(StorageProductListActivity.this,
                                                "You cannot have negative amounts of " + product.getName()).show();
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utils.connectionError(StorageProductListActivity.this);
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private AlertDialog createModifyProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the product");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView inputName = new TextView(this);
        inputName.setText(storageProduct.getName());
        layout.addView(inputName);

        final EditText inputAmount = new EditText(this);
        inputAmount.setText(storageProduct.getAmount() + "");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final TextView inputUnits = new TextView(this);
        inputUnits.setText(storageProduct.getUnitType());
        layout.addView(inputUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {

            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Storage storage = ds.getValue(Storage.class);
                        if (storage != null) {
                            storageProduct.setAmount(Integer.parseInt(inputAmount.getText().toString()));

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
                    Utils.connectionError(StorageProductListActivity.this);
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private AlertDialog createAddProductDialog(String name, String units) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the amount of " + name + " in " + units);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);

        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // TODO CREATE ANOTHER METHOD
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Storage storage = ds.getValue(Storage.class);
                        if (storage != null) {
                            if (storage.getProducts() != null) {
                                if (storage.getProducts().containsKey(name)) {
                                    // Update a product if it already exists
                                    StorageProduct product = storage.getProducts().get(name);
                                    int sumOfProducts = product.getAmount() + Integer.parseInt(inputAmount.getText().toString());
                                    STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .child("amount")
                                            .setValue(sumOfProducts);
                                    Toasty.info(StorageProductListActivity.this, "The " +
                                                    "introduced amount was added to the existent product.")
                                            .show();
                                } else {
                                    // Set a product if it didn't exist before
                                    STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .setValue(new StorageProduct(
                                                    Integer.parseInt(inputAmount.getText().toString()),
                                                    name,
                                                    units));
                                }
                            } else {
                                // Set a product when the list is empty
                                STORAGE_REFERENCE.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(name)
                                        .setValue(new StorageProduct(
                                                Integer.parseInt(inputAmount.getText().toString()),
                                                name,
                                                units
                                        ));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utils.connectionError(StorageProductListActivity.this);
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }
}