package com.example.trabajofingrado.utilities;

import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.controller.StorageProductListActivity;
import com.example.trabajofingrado.model.ShoppingList;
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
    public static AlertDialog leaveStorageDialog(Activity activity, String id, String name,
                                                 StorageRecyclerAdapter adapter, String searchCriteria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Are you sure you want to leave " + name);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> removeStorageUser(activity, id, adapter, searchCriteria));

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private static void removeStorageUser(Activity activity, String id, StorageRecyclerAdapter adapter,
                                          String searchCriteria) {
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);
                    if (storage != null) {
                        for (Map.Entry<String, Boolean> user : storage.getUsers().entrySet()) {
                            if (user.getKey().trim().equals(FirebaseAuth.getInstance().getUid())) {
                                Map<String, Object> childUpdates = new HashMap<>();

                                if (storage.getUsers().entrySet().size() > 1) {
                                    childUpdates.put(storage.getId()
                                            + "/users/"
                                            + FirebaseAuth.getInstance().getUid(), null);
                                    STORAGE_REFERENCE.updateChildren(childUpdates)
                                            .addOnCompleteListener(task -> Toasty.success(activity,
                                                    "You left the storage!").show());
                                } else {
                                    STORAGE_REFERENCE.child(storage.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (adapter != null && searchCriteria != null) {
                                                adapter.getFilter().filter(searchCriteria);
                                            }
                                            Toasty.success(activity,"You left the storage!").show();
                                            deleteShoppingLists(activity, id);
                                            if (activity.getClass().equals(StorageProductListActivity.class)) {
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

    public static void deleteShoppingLists(Activity activity, String storageId) {
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("storageId").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
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

                    STORAGE_REFERENCE.child(storageId)
                            .child("name")
                            .setValue(name).addOnCompleteListener(task -> {
                                if (activity.getClass().equals(StorageProductListActivity.class)) {
                                    activity.setTitle(name);
                                }

                                Query query = SHOPPING_LIST_REFERENCE.orderByChild("storageId").equalTo(storageId);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                                            childUpdates.put(shoppingList.getId()
                                                    + "/storageName", name);
                                        }
                                        SHOPPING_LIST_REFERENCE.updateChildren(childUpdates);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Utils.connectionError(activity);
                                    }
                                });
                            });
                } else {
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
