package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageProductRecyclerAdapter;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
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

public class ProductListActivity extends AppCompatActivity {
    private static final int ADD_AMOUNT = 1;
    private static final int SUBSTRACT_AMOUNT = 2;

    private ArrayList<StorageProduct> storageProductList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private StorageProduct storageProduct;
    private FloatingActionButton btnAddProduct;
    private View auxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_storage_list);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        recyclerView = findViewById(R.id.rvProductsStorage);
        recyclerAdapter = new StorageProductRecyclerAdapter(storageProductList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddProductDialog().show();
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

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
        Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
        ValueEventListener eventListener = new ValueEventListener() {
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
                Log.d(TAG, error.getMessage());
            }
        };
        query.addValueEventListener(eventListener);
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
                createModifyProductDialog(storageProduct).show();
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

    private AlertDialog createDeleteProductDialog(StorageProduct storageProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete the storageProduct " + storageProduct.getDescription())
                .setTitle("Delete storageProduct");

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
                        Log.d(TAG, error.getMessage());
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
                builder.setMessage("Introduce the amount you want to add to the existent storageProduct")
                        .setTitle("Add to the storageProduct");
                break;
            case SUBSTRACT_AMOUNT:
                builder.setMessage("Introduce the amount you want to substract from the existent storageProduct")
                        .setTitle("Substract from the storageProduct");
                break;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // TODO CHANGE SIMILAR TO ADD_RECIPE_ACTIVITY
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
                        Log.d(TAG, error.getMessage());
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

    private AlertDialog createModifyProductDialog(StorageProduct storageProduct){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the storageProduct");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setText(storageProduct.getDescription());
        layout.addView(inputName);

        String productAmount = storageProduct.getAmount();
        final EditText inputAmount = new EditText(this);
        inputAmount.setText(productAmount.substring(0, productAmount.indexOf(" ")));
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final EditText inputUnits = new EditText(this);
        inputUnits.setText(productAmount.substring(productAmount.indexOf(" ")).trim());
        layout.addView(inputUnits);

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
                                if(inputName.getText().toString().equals(storageProduct.getDescription())){
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(inputName.getText().toString().trim())
                                            .setValue(inputAmount.getText().toString().trim() + " " +
                                                    inputUnits.getText().toString().trim());
                                }else{
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(storageProduct.getDescription())
                                            .removeValue();
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(inputName.getText().toString().trim())
                                            .setValue(inputAmount.getText().toString().trim() + " "
                                                    +  inputUnits.getText().toString().trim());
                                }
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
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

    private AlertDialog createAddProductDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the storageProduct and the amount of it");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        layout.addView(inputName);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final EditText inputUnits = new EditText(this);
        inputUnits.setHint("Units");
        layout.addView(inputUnits);

        builder.setView(layout);

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
                                if(storage.getProducts().containsKey(inputName.getText().toString())){
                                    String value = storage.getProducts().get(inputName.getText().toString());
                                    String dsValue = value.substring(0, value.indexOf(" "));
                                    int sumOfProducts = Integer.parseInt(dsValue) + Integer.parseInt(inputAmount.getText().toString());
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(inputName.getText().toString())
                                            .setValue( sumOfProducts + " "
                                                    +  inputUnits.getText().toString());
                                    Toasty.info(ProductListActivity.this, "The " +
                                            "storageProduct already exists so the introduced amount " +
                                            "was added to the existent instead.").show();
                                }else{
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(inputName.getText().toString())
                                            .setValue(inputAmount.getText().toString() + " " +
                                                    inputUnits.getText().toString());
                                }
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
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