package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class ProductListActivity extends AppCompatActivity {
    private ArrayList<Product> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProductRecyclerAdapter recyclerAdapter;
    private FloatingActionButton btnAddProduct;

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
                createInputDialog().show();
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
        query.addListenerForSingleValueEvent(eventListener);
    }

    private AlertDialog createInputDialog(){
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