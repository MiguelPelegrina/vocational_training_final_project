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

/**
 * Class responsible for handling input dialogs related to storages
 */
public class StorageListInputDialogs {
    /**
     * Asks the user to confirm the decision to leave a storage
     * @param activity Activity where the input dialog will be shown
     * @param storageId
     * @param storageName
     * @param adapter Recycler adapter of storages whose filter will be updated. Set to null if
     *                no filter is applied
     * @param searchCriteria Actual value of the search criteria used to filter a recycler adapter.
     *                       Set to null if no filter is applied
     * @return Returns the input dialog
     */
    public static AlertDialog leaveStorageDialog(Activity activity, String storageId, String storageName,
                                                 StorageRecyclerAdapter adapter, String searchCriteria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Are you sure you want to leave " + storageName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> removeStorageUser(activity, storageId, adapter, searchCriteria));

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Removes a user from the selected storage
     * @param activity Activity where the input dialog will be shown
     * @param id Id of the storage
     * @param adapter Recycler adapter of the whose filter will be updated. Set to null if you no
     *      *                filter is applied
     * @param searchCriteria Actual value of the search criteria used to filter a recycler adapter
     */
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

                                // Checks if any other is part of the storage
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

    /**
     * Deletes all associated shopping lists of the storage
     * @param activity
     * @param storageId
     */
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

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
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
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        return builder.create();
    }
}
