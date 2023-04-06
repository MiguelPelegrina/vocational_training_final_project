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
                Intent intent = new Intent(StorageListActivity.this, ProductListActivity.class);
                intent.putExtra("name", storage.getName());
                startActivity(intent);
            }
        });

        // TODO Get only the associated storages of the logged-in-user. Test new storages and users
        // as data might be bugged: the second user works fine but the first doesnt
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Storage storage = dataSnapshot1.getValue(Storage.class);
                    if(storage != null){
                        if(Boolean.TRUE.equals(storage.getUsers().get(getIntent().getStringExtra("username")))){
                            Log.d("Snapshot", dataSnapshot1.getValue().toString());
                            storageList.add(storage);
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