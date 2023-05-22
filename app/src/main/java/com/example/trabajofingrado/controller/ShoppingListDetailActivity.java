package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.cbProduct;
import static com.example.trabajofingrado.R.id.menu_item_modify_shopping_list_name;
import static com.example.trabajofingrado.R.id.menu_item_delete_shopping_list;
import static com.example.trabajofingrado.R.id.txtShoppingListProductName;
import static com.example.trabajofingrado.R.id.txtShoppingListProductAmount;
import static com.example.trabajofingrado.R.id.txtDeleteShoppingListProduct;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ShoppingListProductRecyclerAdapter;
import com.example.trabajofingrado.interfaces.RecyclerViewActionListener;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.ShoppingListInputDialogs;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ShoppingListDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int PRODUCT_ADD_REQUEST_CODE = 1,
            STORAGE_CHOICE_RESULT_CODE = 2;
    // Of instance
    private ArrayList<StorageProduct> productList = new ArrayList<>(),
            boughtProductList = new ArrayList<>();
    private Button btnAddBoughtProductsToStorage, btnAddProduct;
    private DatabaseReference shoppingListReference;
    private RecyclerView rvBoughtProducts, rvProducts;
    private RecyclerViewActionListener rvBoughtProductsActionListener, rvProductsActionListener;
    private ShoppingListProductRecyclerAdapter raBoughtProducts, raProducts;
    private StorageProduct product;
    private String shoppingListId;
    private TextView txtLastEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        // Get the database instance of the shopping lists
        shoppingListReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        // TODO REFACTOR WITH SHOPPING LISTS LIST ACTIVITY
        if (getIntent().getStringExtra("shoppingListId") == null) {
            String storageName = getIntent().getStringExtra("storageName");
            String storageId = getIntent().getStringExtra("storageId");

            createAddShoppingListDialog(storageId, storageName).show();
        } else {
            // Set the information from the intent
            setTitle(getIntent().getStringExtra("shoppingListName"));
            shoppingListId = getIntent().getStringExtra("shoppingListId");
        }

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_shopping_lists_list);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();

        if (shoppingListId != null) {
            fillShoppingList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PRODUCT_ADD_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String productName = data.getStringExtra("name");
                    String productUnits = data.getStringExtra("unitType");

                    createAddProductDialog(productName, productUnits).show();
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
                ShoppingListInputDialogs.updateShoppingListNameDialog(ShoppingListDetailActivity.this, shoppingListId).show();
                break;
            case menu_item_delete_shopping_list:
                ShoppingListInputDialogs.deleteShoppingListDialog(ShoppingListDetailActivity.this, shoppingListId).show();
                break;
        }

        return super.onOptionsItemSelected(item);
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
        rvProductsActionListener = new RecyclerViewActionListener() {
            @Override
            public void onViewClicked(int clickedViewId, int clickedItemPosition) {
                switch (clickedViewId) {
                    case cbProduct:
                        product = productList.get(clickedItemPosition);
                        updateShoppingListWithBoughtProduct();
                        break;
                    case txtShoppingListProductName:

                        break;
                    case txtShoppingListProductAmount:

                        break;
                    case txtDeleteShoppingListProduct:
                        product = productList.get(clickedItemPosition);
                        deleteShoppingListProduct();
                        break;
                }
            }
        };

        rvBoughtProductsActionListener = new RecyclerViewActionListener() {
            @Override
            public void onViewClicked(int clickedViewId, int clickedItemPosition) {
                switch (clickedViewId) {
                    case cbProduct:
                        product = boughtProductList.get(clickedItemPosition);
                        updateShoppingListWithProduct();
                        break;
                    case txtShoppingListProductName:

                        break;
                    case txtShoppingListProductAmount:

                        break;
                    case txtDeleteShoppingListProduct:
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

    private void setListener() {
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingListDetailActivity.this, AddProductActivity.class);
                intent.putExtra("action", "add");
                startActivityForResult(intent, PRODUCT_ADD_REQUEST_CODE);
            }
        });

        btnAddBoughtProductsToStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = storageReference.orderByChild("id").equalTo(getIntent().getStringExtra("storageId"));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Storage storage = ds.getValue(Storage.class);
                            for (StorageProduct storageProduct : boughtProductList) {
                                String name = storageProduct.getName();

                                OnCompleteListener<Void> storageUpdated = new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        shoppingListReference.child(Objects.requireNonNull(shoppingListId))
                                                .child("boughtProducts")
                                                .removeValue();

                                        shoppingListReference.child(Objects.requireNonNull(shoppingListId))
                                                .child("lastEdited")
                                                .setValue(Utils.getCurrentTime());
                                    }
                                };

                                if (storage.getProducts() != null && storage.getProducts().containsKey(name)) {
                                    // Update a product if it already exists
                                    int sumOfProducts = storage.getProducts().get(name).getAmount() + storageProduct.getAmount();

                                    storageReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .child("amount")
                                            .setValue(sumOfProducts).addOnCompleteListener(storageUpdated);
                                } else {
                                    // Set a product if it didnt exist before
                                    storageReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(storageProduct.getName())
                                            .setValue(storageProduct).addOnCompleteListener(storageUpdated);
                                }
                            }
                        }

                        Toasty.success(ShoppingListDetailActivity.this, "Storage refilled").show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utils.connectionError(ShoppingListDetailActivity.this);
                    }
                });
            }
        });
    }

    private void fillShoppingList() {
        // Set the database to get all shopping lists
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                boughtProductList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList != null) {
                        if (shoppingList.getProducts() != null) {
                            for (Map.Entry<String, StorageProduct> entry : shoppingList.getProducts().entrySet()) {
                                StorageProduct product = new StorageProduct(
                                        entry.getValue().getAmount(),
                                        entry.getValue().getName(),
                                        entry.getValue().getUnitType());
                                productList.add(product);
                            }
                        }

                        if (shoppingList.getBoughtProducts() != null) {
                            for (Map.Entry<String, StorageProduct> entry : shoppingList.getBoughtProducts().entrySet()) {
                                StorageProduct product = new StorageProduct(
                                        entry.getValue().getAmount(),
                                        entry.getValue().getName(),
                                        entry.getValue().getUnitType());
                                boughtProductList.add(product);
                            }
                        }
                        txtLastEdited.setText("Edited: " + shoppingList.getLastEdited());
                    }

                    raProducts.notifyDataSetChanged();
                    raBoughtProducts.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    private void updateShoppingListWithBoughtProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getProducts() != null) {
                        DatabaseReference shoppingListRef = shoppingListReference.child(ds.getKey());

                        Map<String, Object> updates = new HashMap<>();

                        updates.put("products/" + product.getName(), null);
                        if (shoppingList.getBoughtProducts().containsKey(product.getName())) {
                            int newProductAmount = product.getAmount() + shoppingList.getBoughtProducts().get(product.getName()).getAmount();
                            product.setAmount(newProductAmount);
                        }
                        updates.put("boughtProducts/" + product.getName(), product);
                        updates.put("lastEdited", Utils.getCurrentTime());

                        shoppingListRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error != null) {
                                    Utils.connectionError(ShoppingListDetailActivity.this);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    private void updateShoppingListWithProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getBoughtProducts() != null) {
                        DatabaseReference shoppingListRef = shoppingListReference.child(ds.getKey());

                        Map<String, Object> updates = new HashMap<>();

                        updates.put("products/" + product.getName(), product);
                        if (shoppingList.getProducts().containsKey(product.getName())) {
                            int newProductAmount = product.getAmount() + shoppingList.getProducts().get(product.getName()).getAmount();
                            product.setAmount(newProductAmount);
                        }
                        updates.put("boughtProducts/" + product.getName(), null);
                        updates.put("lastEdited", Utils.getCurrentTime());

                        shoppingListRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error != null) {
                                    Utils.connectionError(ShoppingListDetailActivity.this);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    private void deleteShoppingListProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getProducts() != null) {
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("products")
                                .child(product.getName())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
            }
        });
    }

    private void deleteShoppingListBoughtProduct() {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getBoughtProducts() != null) {
                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("boughtProducts")
                                .child(product.getName())
                                .removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
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
                if (Utils.checkValidString(inputAmount.getText().toString())) {
                    Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                                if (shoppingList != null) {
                                    product = new StorageProduct(
                                            Integer.parseInt(inputAmount.getText().toString()),
                                            name, units);
                                    if (shoppingList.getProducts() != null) {
                                        if (shoppingList.getProducts().containsKey(name)) {
                                            // Update a product if it already exists
                                            StorageProduct storageProduct = shoppingList.getProducts().get(name);

                                            product.setAmount(storageProduct.getAmount() + Integer.parseInt(inputAmount.getText().toString()));

                                            shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                                    .child("products")
                                                    .child(name)
                                                    .setValue(product);
                                            Toasty.info(ShoppingListDetailActivity.this, "The " +
                                                    "product already exists so the introduced amount " +
                                                    "was added to the existent instead.").show();
                                        } else {
                                            // Set a product if it didn't exist before
                                            shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                                    .child("products")
                                                    .child(name)
                                                    .setValue(product);
                                        }
                                    } else {
                                        // Set a product when the list is empty
                                        shoppingListReference.child(Objects.requireNonNull(ds.getKey()))
                                                .child("products")
                                                .child(name)
                                                .setValue(product);
                                    }
                                }
                                raProducts.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Utils.connectionError(ShoppingListDetailActivity.this);
                        }
                    });
                } else {
                    Utils.enterValidData(ShoppingListDetailActivity.this);
                }
            }
        });

        return builder.create();
    }

    private AlertDialog createAddShoppingListDialog(String storageId, String storageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListDetailActivity.this);

        builder.setTitle("Add a shopping list to " + storageName + ".");

        final EditText input = new EditText(this);
        input.setHint("Name of shopping list");

        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.checkValidString(input.getText().toString())) {
                    createNewShoppingList(storageId, storageName, input.getText().toString());
                } else {
                    Utils.enterValidData(ShoppingListDetailActivity.this);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startActivity(new Intent(ShoppingListDetailActivity.this, ShoppingListsListActivity.class));
            }
        });

        return builder.create();
    }

    private void createNewShoppingList(String storageId, String storageName, String shoppingListName) {
        DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
        Query query = storageReference.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);

                    DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

                    HashMap<String, Boolean> users = new HashMap<>();
                    for (Map.Entry<String, Boolean> user : storage.getUsers().entrySet()) {
                        users.put(user.getKey(), true);
                    }

                    ShoppingList shoppingList = new ShoppingList(users,
                            shoppingListName, Utils.getCurrentTime(), UUID.randomUUID().toString(), storage.getId(), storage.getName());

                    // TODO UPDATE STORAGE WITH SHOPPING LISTS AS WELL

                    shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            setTitle(storageName);
                            shoppingListId = shoppingList.getId();
                            txtLastEdited.setText("Edited: " + shoppingList.getLastEdited());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(ShoppingListDetailActivity.this);
            }
        });
    }
}
