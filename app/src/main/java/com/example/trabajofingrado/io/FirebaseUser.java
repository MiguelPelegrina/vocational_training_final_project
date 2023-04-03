package com.example.trabajofingrado.io;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.trabajofingrado.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUser {
    private DatabaseReference database;

    public FirebaseUser() {
        this.database = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void createNewUser(User user) {
        database.child(user.getName()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }else{
                    String taskResult = String.valueOf(task.getResult().getValue());
                    Log.d("firebase", taskResult);
                        database.push().setValue(user);
                }
            }
        });
    }


}
