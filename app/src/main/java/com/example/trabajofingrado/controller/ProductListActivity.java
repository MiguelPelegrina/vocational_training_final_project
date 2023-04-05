package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.model.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {
    private ArrayList<Product> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list);

        recyclerView = findViewById(R.id.ProductRecyclerView);
        recyclerAdapter = new ProductRecyclerAdapter(productList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Products");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Product product = dataSnapshot1.getValue(Product.class);
                    productList.add(product);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }


}