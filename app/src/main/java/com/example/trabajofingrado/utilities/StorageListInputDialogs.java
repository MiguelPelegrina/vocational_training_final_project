package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.controller.ProductListActivity;
import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class StorageListInputDialogs {
    public static DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

    public static AlertDialog leaveStorageDialog(Activity activity, String id, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Are you sure you want to leave " + name);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeStorageUser(activity, id);

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

    private static void removeStorageUser(Activity activity, String id) {
        Query query = storageReference.orderByChild("id").equalTo(id);
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
                                            Toasty.success(activity,
                                                    "You left the storage!").show();
                                        }
                                    });
                                }else {
                                    storageReference.child(storage.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            deleteShoppingLists(activity, id);
                                            if (activity.getClass().equals(ProductListActivity.class)) {
                                                activity.finish();
                                            }
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
                Utils.connectionError(activity);
            }
        });
    }

    private static void deleteShoppingLists(Activity activity, String storageId) {
        DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);
        Query query = shoppingListsReference.orderByChild("storageId").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(activity);
            }
        });
    }

    public static AlertDialog updateStorageNameDialog(Activity activity, String storageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Set a new name.");

        final EditText inputName = new EditText(activity);
        inputName.setHint("Name");

        builder.setView(inputName);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.checkValidString(inputName.getText().toString())) {
                    String name = inputName.getText().toString();
                    storageReference.child(storageId)
                            .child("name")
                            .setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(activity.getClass().equals(ProductListActivity.class)){
                                        activity.setTitle(name);
                                    }
                                }
                            });
                }else{
                    Utils.enterValidData(activity);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        return builder.create();
    }
}
