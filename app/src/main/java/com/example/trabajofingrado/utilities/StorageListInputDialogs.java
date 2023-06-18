package com.example.trabajofingrado.utilities;

import static com.example.trabajofingrado.io.ShoppingListPutController.createNewShoppingList;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.checkValidString;
import static com.example.trabajofingrado.utilities.Utils.connectionError;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;

import android.app.Activity;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.adapter.StorageRecyclerAdapter;
import com.example.trabajofingrado.controller.StorageDetailActivity;
import com.example.trabajofingrado.controller.StorageListActivity;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
     * Creates an alert dialog to create a new shopping list
     * @param activity Activity where the input dialog will be shown
     * @param storage
     */
    public static AlertDialog addShoppingListDialog(Activity activity, Storage storage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Configure the builder
        builder.setTitle("Add a shopping list to " + storage.getName() + ".");

        // Configure an edit text to set the name
        final EditText input = new EditText(activity);
        input.setHint("Name");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the data is valid
            if (checkValidString(input.getText().toString())) {
                createNewShoppingList(activity, storage, input.getText().toString(), true);
            } else {
                enterValidData(activity);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Asks the user to confirm the decision to leave a storage
     *
     * @param activity       Activity where the input dialog will be shown
     * @param storageId
     * @param storageName
     * @param adapter        Recycler adapter of storages whose filter will be updated. Set to null if
     *                       no filter is applied
     * @param searchCriteria Actual value of the search criteria used to filter a recycler adapter.
     *                       Set to null if no filter is applied
     * @return Returns the alert dialog
     */
    public static AlertDialog leaveStorageDialog(Activity activity, String storageId, String storageName,
                                                 StorageRecyclerAdapter adapter, String searchCriteria) {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Configure the alert dialog
        builder.setTitle("Are you sure you want to leave " + storageName);
        builder.setPositiveButton("Confirm", (dialogInterface, i) -> removeStorageUser(activity, storageId, adapter, searchCriteria));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Removes a user from the selected storage
     *
     * @param activity       Activity where the input dialog will be shown
     * @param storageId
     * @param adapter        Recycler adapter of the whose filter will be updated. Set to null if you no
     *                       *                filter is applied
     * @param searchCriteria Actual value of the search criteria used to filter a recycler adapter
     */
    private static void removeStorageUser(Activity activity, String storageId, StorageRecyclerAdapter adapter,
                                          String searchCriteria) {
        // Search the storage in the database
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);
                    if (storage != null) {
                        // Loop through the users of the storage
                        for (Map.Entry<String, Boolean> user : storage.getUsers().entrySet()) {

                            // Check if the user is part of the map
                            if (user.getKey().trim().equals(FirebaseAuth.getInstance().getUid())) {
                                // Generate an updates map
                                Map<String, Object> updates = new HashMap<>();

                                // Checks if any other user is part of the storage
                                if (storage.getUsers().entrySet().size() > 1) {
                                    // Put the removed user into the updates map
                                    updates.put(storage.getId() + "/users/"
                                            + FirebaseAuth.getInstance().getUid(), null);

                                    // Remove the user from the storage
                                    STORAGE_REFERENCE.updateChildren(updates)
                                            .addOnCompleteListener(task -> Toasty.success(activity,
                                                    "You left the storage!").show());
                                } else {
                                    // Remove the storage from the database
                                    STORAGE_REFERENCE.child(storage.getId()).removeValue()
                                            .addOnCompleteListener(task -> {
                                                // Set the filter of the search view of the activity
                                                // so that the deleted storage is not shown anymore
                                                if (adapter != null && searchCriteria != null) {
                                                    adapter.getFilter().filter(searchCriteria);
                                                }

                                                // Inform the user that they left the storage
                                                Toasty.success(activity, "You left the storage!").show();

                                                // Delete all shopping lists associated with the storage
                                                deleteShoppingLists(activity, storageId);

                                                // Finish the activity
                                                if (activity.getClass().equals(StorageDetailActivity.class)) {
                                                    activity.finish();
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
                connectionError(activity);
            }
        });
    }

    /**
     * Deletes all associated shopping lists of the storage
     *
     * @param activity
     * @param storageId
     */
    public static void deleteShoppingLists(Activity activity, String storageId) {
        // Search the shopping lists that are part of the storage
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("storageId").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Loop through all shopping lists
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Remove the shopping list
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(activity);
            }
        });
    }

    /**
     * @param activity
     * @param storageId
     * @return
     */
    public static AlertDialog updateStorageNameDialog(Activity activity, String storageId) {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Configure the alert dialog
        builder.setTitle("Set a new name.");

        // Set an edit text for the name
        final EditText inputName = new EditText(activity);
        inputName.setHint("Name");
        builder.setView(inputName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the edit text has any valid data
            if (checkValidString(inputName.getText().toString())) {
                // Get the name
                String storageName = inputName.getText().toString();

                // Update the storage name in the database
                STORAGE_REFERENCE.child(storageId).child("name").setValue(storageName)
                        .addOnCompleteListener(task -> {
                            // Check if the method was called from the storage product list activity
                            if (activity.getClass().equals(StorageDetailActivity.class)) {
                                // Set the name of the storage as the title of the activity
                                activity.setTitle(storageName);
                            }

                            // Search the shopping lists
                            Query query = SHOPPING_LIST_REFERENCE.orderByChild("storageId").equalTo(storageId);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Generate an updates map
                                    Map<String, Object> updates = new HashMap<>();

                                    // Loop through the shopping lists
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        // Get the shopping list
                                        ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                                        // Put the new storage name into the updates map
                                        updates.put(shoppingList.getId() + "/storageName", storageName);
                                    }

                                    // Update all shopping lists
                                    SHOPPING_LIST_REFERENCE.updateChildren(updates);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    connectionError(activity);
                                }
                            });
                        });
            } else {
                enterValidData(activity);
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        return builder.create();
    }
}
