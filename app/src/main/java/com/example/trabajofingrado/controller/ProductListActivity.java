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

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ProductRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
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
    private ArrayList<Product> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private Product product;
    private FloatingActionButton btnAddProduct;
    private View auxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        recyclerView = findViewById(R.id.ProductRecyclerView);
        recyclerAdapter = new ProductRecyclerAdapter(productList);

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

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
        Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Storage storage = ds.getValue(Storage.class);
                    for(Map.Entry<String, String>  entry : storage.getProducts().entrySet()){
                        Product product = new Product(entry.getKey(), entry.getValue());
                        productList.clear();
                        productList.add(product);
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
        getMenuInflater().inflate(R.menu.product_modify_menu, menu);
        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        viewHolder = (RecyclerView.ViewHolder) auxView.getTag();
        position = viewHolder.getAdapterPosition();
        product = productList.get(position);
        switch (item.getItemId()){
            case R.id.modifyProduct:
                createModifyProductDialog(product).show();
                break;
            case R.id.addAmount:

                break;
            case R.id.substractAmount:

                break;
            case R.id.deleteProduct:

                break;
        }

        return true;
    }

    private AlertDialog createModifyProductDialog(Product product){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the product");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setText(product.getName());
        layout.addView(inputName);

        String productAmount = product.getAmount();
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
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            Storage storage = ds.getValue(Storage.class);
                            if (storage != null) {
                                if(inputName.getText().toString().equals(product.getName())){
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(inputName.getText().toString().trim())
                                            .setValue(inputAmount.getText().toString().trim() + " " +
                                                    inputUnits.getText().toString().trim());
                                }else{
                                    database.child(Objects.requireNonNull(ds.getKey()))
                                            .child("products")
                                            .child(product.getName())
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

        builder.setTitle("Introduce the product and the amount of it");

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
                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
                Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("storage"));
                ValueEventListener eventListener = new ValueEventListener() {
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
                                            "product already exists so the introduced amount " +
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
}