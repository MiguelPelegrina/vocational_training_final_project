package com.example.trabajofingrado.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.controller.ProductListActivity;
import com.example.trabajofingrado.controller.RecipeListActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
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

public class StorageListFragment extends Fragment {
    // Fields
    private View view;
    private ArrayList<Storage> storageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StorageRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_storage_list, container, false);

        recyclerView = view.findViewById(R.id.StorageRecyclerView);
        recyclerAdapter = new StorageRecyclerAdapter(storageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Storage storage = storageList.get(viewHolder.getAdapterPosition());
                Intent intent = null;
                switch (getActivity().getIntent().getStringExtra("activity")) {
                    case "choiceActivity":
                        intent = new Intent(view.getContext(), ProductListActivity.class);
                        intent.putExtra("storage", storage.getName());
                        startActivity(intent);
                        break;
                    case "recipeActivity":
                        intent = new Intent(view.getContext(), RecipeListActivity.class);
                        intent.putExtra("storage", storage.getName());
                        getActivity().setResult(StorageListActivity.RESULT_OK, intent);
                        getActivity().finish();
                        break;
                }
            }
        });

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.STORAGEPATH);
        Query query = database.orderByChild(getActivity().getIntent().getStringExtra("username"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Storage storage = dataSnapshot.getValue(Storage.class);
                    if(storage != null){
                        for(Map.Entry<String, Boolean> user: storage.getUsers().entrySet()){
                            if(user.getKey().trim().equals(getActivity().getIntent().getStringExtra("username").trim())){
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

        return view;
    }
}