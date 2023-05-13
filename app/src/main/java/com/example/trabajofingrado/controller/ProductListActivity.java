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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

import es.dmoral.toasty.Toasty;

public class ProductListActivity extends BaseActivity{
    // Fields
    // Of class
    private static final int ADD_AMOUNT = 1;
    private static final int SUBSTRACT_AMOUNT = 2;
    private static final int PRODUCT_ADD_REQUEST_CODE = 1;

    // Of instance
    private ArrayList<StorageProduct> storageProductList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private StorageProduct storageProduct;
    private FloatingActionButton btnAddProduct;
    private View auxView;
    private TextView txtNoProductsAvailable;
    private DatabaseReference storageReference;
    private String storageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_storage_list);

        setTitle(getIntent().getStringExtra("storageName"));
        this.storageId = getIntent().getStringExtra("storageId");

        // Get the database instance of the storages
        this.storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout(R.id.nav_storage_list);

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();

        this.fillProductList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_ADD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String productName = data.getStringExtra("name");
                String productUnits = data.getStringExtra("unitType");

                createAddProductDialog(productName, productUnits).show();
            }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.options_menu_item_share_storage:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("storage access code", storageId);
                clipboard.setPrimaryClip(clip);

                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
                    Toasty.info(ProductListActivity.this, "The code was copied:" +
                            clipboard.getPrimaryClip().getItemAt(0).toString()).show();
                }
                break;
            case R.id.options_menu_item_leave_storage:
                createLeaveStorageDialog().show();
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
        this.txtNoProductsAvailable = findViewById(R.id.txtNoProductsAvailable);
        this.drawerLayout = findViewById(R.id.drawer_layout_storages);
        this.toolbar = findViewById(R.id.toolbar_product_list);
        this.recyclerView = findViewById(R.id.rvProductsStorage);
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
    }

    private void setListener() {
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
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
        // Set the database to get all products
        Query query = storageReference.orderByChild("id").equalTo(storageId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storageProductList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);
                    if(storage.getProducts() != null){
                        for(Map.Entry<String, StorageProduct> entry : storage.getProducts().entrySet()){
                            storageProductList.add(entry.getValue());
                        }
                        txtNoProductsAvailable.setVisibility(View.INVISIBLE);
                        recyclerAdapter.notifyDataSetChanged();
                    }

                }
                if(storageProductList.isEmpty()){
                    txtNoProductsAvailable.setVisibility(View.VISIBLE);
                }else{
                    txtNoProductsAvailable.setVisibility(View.INVISIBLE);
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
        Query query = storageReference.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);
                    if (storage != null) {
                        storageReference.child(Objects.requireNonNull(ds.getKey()))
                                .child("products")
                                .child(storageProduct.getName())
                                .removeValue();
                    }
                    storageProductList.remove(position);
                    recyclerAdapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ProductListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
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
        productName.setText(storageProduct.getName());
        layout.addView(productName);

        int productAmount = storageProduct.getAmount();
        final EditText inputAmount = new EditText(this);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final TextView productUnits = new TextView(this);
        productUnits.setText(productAmount);
        layout.addView(productUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query query = storageReference.orderByChild("id").equalTo(storageId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                StorageProduct product = storage.getProducts().get(productName.getText().toString());

                                int sumOfProducts = 0;
                                switch (amountCalculation){
                                    case ADD_AMOUNT:
                                        sumOfProducts = product.getAmount() + Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                    case SUBSTRACT_AMOUNT:
                                        sumOfProducts = product.getAmount() - Integer.parseInt(inputAmount.getText().toString());
                                        break;
                                }
                                storageReference.child(Objects.requireNonNull(ds.getKey()))
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

    private AlertDialog createModifyProductDialog(){
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

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Query query = storageReference.orderByChild("id").equalTo(storageId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                storageReference.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getName())
                                        .removeValue();
                                storageReference.child(Objects.requireNonNull(ds.getKey()))
                                        .child("products")
                                        .child(storageProduct.getName())
                                        .setValue(inputAmount.getText().toString().trim() + " "
                                                +  inputUnits.getText().toString().trim());
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
                Query query = storageReference.orderByChild("id").equalTo(storageId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                if(storage.getProducts() != null){
                                    if(storage.getProducts().containsKey(name)){
                                        // Update a product if it already exists
                                        StorageProduct product = storage.getProducts().get(name);
                                        int sumOfProducts = product.getAmount() + Integer.parseInt(inputAmount.getText().toString());
                                        storageReference.child(Objects.requireNonNull(ds.getKey()))
                                                .child("products")
                                                .child(name)
                                                .child("amount")
                                                .setValue(sumOfProducts);
                                        Toasty.info(ProductListActivity.this, "The " +
                                                "product already exists so the introduced amount " +
                                                "was added to the existent instead.").show();
                                    }else{
                                        // Set a product if it didnt exist before
                                        storageReference.child(Objects.requireNonNull(ds.getKey()))
                                                .child("products")
                                                .child(name)
                                                .setValue(new StorageProduct(
                                                        Integer.parseInt(inputAmount.getText().toString()),
                                                        name,
                                                        units));
                                    }
                                }else{
                                    // Set a product when the list is empty
                                    storageReference.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(name)
                                            .setValue(new StorageProduct(
                                                    Integer.parseInt(inputAmount.getText().toString()),
                                                    name,
                                                    units
                                            ));
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

    private AlertDialog createLeaveStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductListActivity.this);

        builder.setTitle("Are you sure you want to leave " + getTitle());

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
        Query query = storageReference.orderByChild("id").equalTo(storageId);
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
                                            Toasty.success(ProductListActivity.this,
                                                    "You left the storage!").show();
                                        }
                                    });
                                }else {
                                    storageReference.child(storage.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();
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
                Toasty.error(ProductListActivity.this, "An error trying to access " +
                        "the database happened. Check your internet connection").show();
            }
        });
    }
}