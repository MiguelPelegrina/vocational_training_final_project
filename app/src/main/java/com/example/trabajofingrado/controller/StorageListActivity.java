package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class StorageListActivity extends AppCompatActivity {
    private ArrayList<Storage> storageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list);

        recyclerView = findViewById(R.id.StorageRecyclerView);
        recyclerAdapter = new StorageRecyclerAdapter(storageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Storage storage = storageList.get(viewHolder.getAdapterPosition());
                Intent intent = null;
                switch (getIntent().getStringExtra("activity")) {
                    case "choiceActivity":
                        intent = new Intent(StorageListActivity.this, ProductListActivity.class);
                        intent.putExtra("name", storage.getName());
                        startActivity(intent);
                        break;
                    case "recipeActivity":
                        intent = new Intent(StorageListActivity.this, RecipeListActivity.class);
                        intent.putExtra("storage", storage.getName());
                        setResult(StorageListActivity.RESULT_OK, intent);
                        finish();
                        break;
                }


            }
        });

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
        Query query = database.orderByChild(getIntent().getStringExtra("username"));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Storage storage = dataSnapshot.getValue(Storage.class);
                    if(storage != null){
                        for(Map.Entry<String, Boolean> user: storage.getUsers().entrySet()){
                            if(user.getKey().trim().equals(getIntent().getStringExtra("username").trim())){
                                storageList.add(storage);
                            }
                        }
                    }
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