package com.example.trabajofingrado.io;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.trabajofingrado.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUser {
    private DatabaseReference database;

    public FirebaseUser() {
        this.database = FirebaseDatabase.getInstance().getReference("Users");
    }

    public int createNewUser(User user) {
        final int[] result = {-1};

        Query query = database.orderByChild("email").equalTo(user.getEmail());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    database.push().setValue(user);
                    result[0] = 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        query.addListenerForSingleValueEvent(eventListener);

        return result[0];
    }


}
