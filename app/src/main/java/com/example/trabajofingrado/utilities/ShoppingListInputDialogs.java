package com.example.trabajofingrado.utilities;

import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.connectionError;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.adapter.ShoppingListRecyclerAdapter;
import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.Storage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Class responsible for handling input dialogs related to shopping lists
 */
public class ShoppingListInputDialogs {
    /**
     * Asks the user to confirm the decision to delete a shopping list
     *
     * @param activity       Activity where the input dialog will be shown
     * @param shoppingListId
     * @param adapter        Recycler adapter of storages whose filter will be updated. Set to null if
     *                       no filter is applied
     * @param @param         searchCriteria Actual value of the search criteria used to filter a recycler adapter.
     *                       Set to null if no filter is applied
     * @return Returns the alert dialog
     */
    public static AlertDialog deleteShoppingListDialog(Activity activity, String shoppingListId,
                                                       ShoppingListRecyclerAdapter adapter,
                                                       String searchCriteria) {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Configure the builder
        builder.setTitle("Are you sure that you want to delete this shopping list?");

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            deleteShoppingList(activity, shoppingListId, adapter, searchCriteria);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Deletes the shopping from the database
     * @param activity
     * @param shoppingListId
     * @param adapter
     * @param searchCriteria
     */
    private static void deleteShoppingList(Activity activity, String shoppingListId,
                                           ShoppingListRecyclerAdapter adapter,
                                           String searchCriteria) {
        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Delete the shopping list
                    ds.getRef().removeValue();

                    // Search the storage of the shopping list
                    Query queryStorage = STORAGE_REFERENCE.orderByChild(shoppingListId);
                    queryStorage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Get the storage
                                Storage storage = ds.getValue(Storage.class);

                                // Delete the shopping list from the storage
                                STORAGE_REFERENCE.child(storage.getId())
                                        .child("shoppingLists")
                                        .child(shoppingListId)
                                        .removeValue()
                                        .addOnCompleteListener(task -> {
                                            // Check if there are any adapter and search criteria
                                            if (adapter != null && searchCriteria != null) {
                                                // Set the filter to not show the deleted shopping list anymore
                                                adapter.getFilter().filter(searchCriteria);
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            connectionError(activity);
                        }
                    });

                    if (activity.getClass().equals(ShoppingListDetailActivity.class)) {
                        activity.finish();
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
     * @param activity
     * @param shoppingListId
     * @return Returns the alert dialog
     */
    public static AlertDialog updateShoppingListNameDialog(Activity activity, String shoppingListId) {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Configure the builder
        builder.setTitle("Set a new name.");

        // Set an edit text
        final EditText inputName = new EditText(activity);
        inputName.setHint("Name");
        builder.setView(inputName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the new name is valid
            if (Utils.checkValidString(inputName.getText().toString())) {
                // Get the name
                String shoppingListName = inputName.getText().toString();

                // Update the name of the shopping list
                SHOPPING_LIST_REFERENCE.child(shoppingListId).child("name").setValue(shoppingListName)
                        .addOnCompleteListener(task -> {
                            // Check if the method is called from the shopping list detail activity
                            if (activity.getClass().equals(ShoppingListDetailActivity.class)) {
                                // Set the title of the activity to the new name
                                activity.setTitle(shoppingListName);
                            }
                        });
            } else {
                enterValidData(activity);
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        return builder.create();
    }
}
