package com.example.trabajofingrado.utilities;

import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import android.app.Activity;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.adapter.ShoppingListRecyclerAdapter;
import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
     * @param activity Activity where the input dialog will be shown
     * @param shoppingListId
     * @param adapter Recycler adapter of storages whose filter will be updated. Set to null if
     *                no filter is applied
     * @param @param searchCriteria Actual value of the search criteria used to filter a recycler adapter.
     *                              Set to null if no filter is applied
     * @return Returns the input dialog
     */
    public static AlertDialog deleteShoppingListDialog(Activity activity, String shoppingListId,
                                                       ShoppingListRecyclerAdapter adapter,
                                                       String searchCriteria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Are you sure that you want to delete this shopping list?");

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ds.getRef().removeValue();

                        Query queryStorage = STORAGE_REFERENCE.orderByChild(shoppingListId);
                        queryStorage.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    Storage storage = ds.getValue(Storage.class);
                                    STORAGE_REFERENCE.child(storage.getId())
                                            .child("shoppingLists")
                                            .child(shoppingListId).removeValue()
                                            .addOnCompleteListener(task ->{
                                                        if (adapter != null && searchCriteria != null) {
                                                            adapter.getFilter().filter(searchCriteria);
                                                        }
                                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Utils.connectionError(activity);
                            }
                        });

                        if (activity.getClass().equals(ShoppingListDetailActivity.class)) {
                            activity.finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utils.connectionError(activity);
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     *
     * @param activity
     * @param shoppingListId
     * @return
     */
    public static AlertDialog updateShoppingListNameDialog(Activity activity, String shoppingListId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Set a new name.");

        final EditText inputName = new EditText(activity);
        inputName.setHint("Name");

        builder.setView(inputName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            if (Utils.checkValidString(inputName.getText().toString())) {
                String name = inputName.getText().toString();
                SHOPPING_LIST_REFERENCE.child(shoppingListId)
                        .child("name")
                        .setValue(name).addOnCompleteListener(task -> {
                            if(activity.getClass().equals(ShoppingListDetailActivity.class)){
                                activity.setTitle(name);
                            }
                        });
            }else{
                Utils.enterValidData(activity);
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        return builder.create();
    }
}
