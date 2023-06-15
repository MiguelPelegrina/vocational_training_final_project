package com.example.trabajofingrado.io;

import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

/**
 * Controller of shopping list, responsible of saving shopping lists into the database
 */
public class ShoppingListPutController {
    /**
     * Saves a shopping list into database without any products
     *
     * @param activity         Activity that uses this method
     * @param storage
     * @param shoppingListName
     */
    public static void createNewShoppingList(Activity activity, Storage storage, String shoppingListName, boolean moveToShoppingList) {
        // Generate a shopping list
        ShoppingList shoppingList = new ShoppingList(
                shoppingListName, Utils.getCurrentTime(), UUID.randomUUID().toString(),
                storage.getId(), storage.getName());

        // Saves the shopping list into the database
        SHOPPING_LIST_REFERENCE.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(task -> {
            addShoppingListToStorage(activity, storage, shoppingList);


            // Move to the shopping list activity
            if (moveToShoppingList) {
                startShoppingListActivity(activity, shoppingList);
            }
        });
    }

    /**
     * Saves the shopping list into the storage
     */
    public static void addShoppingListToStorage(Activity activity, Storage storage, ShoppingList shoppingList) {
        // Searches the referenced storage of the shopping list
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storage.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Gets the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Check if the storage has any shopping lists
                    if (storage.getShoppingLists() == null) {
                        // Generate a new shopping list map for the storage
                        HashMap<String, Boolean> shoppingLists = new HashMap<>();

                        // Put the new shopping list into the storage shopping list map
                        shoppingLists.put(shoppingList.getId(), true);

                        // Save the storage with the new shopping list
                        STORAGE_REFERENCE.child(storage.getId())
                                .child("shoppingLists")
                                .setValue(shoppingLists);
                    } else {
                        // Generate an updates map
                        Map<String, Object> updates = new HashMap<>();

                        // Put the new shopping list into the updates map
                        updates.put(storage.getId() + "/shoppingLists/" + shoppingList.getId(), true);

                        // Update the storage with the new shopping list
                        STORAGE_REFERENCE.updateChildren(updates, (error, ref) -> {
                            // Check if any error happened
                            if (error != null) {
                                // Inform the user
                                Utils.connectionError(activity);
                            } else {
                                // Inform the user
                                Toasty.success(activity, "Shopping list " +
                                        shoppingList.getName() + " was created!").show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Communicate with the user
                Utils.connectionError(activity);
            }
        });
    }

    /**
     * Saves a new shopping list into the database. Sets the products that need to be bought.
     *
     * @param activity         Activity that uses this method
     * @param products         Map of products that need to be bought
     * @param storage
     * @param shoppingListName
     */
    public static void createNewShoppingListWithProducts(Activity activity, HashMap<String,
            StorageProduct> products, Storage storage, String shoppingListName) {
        String shoppingListId = UUID.randomUUID().toString();

        // Generate a shopping list
        ShoppingList shoppingList = new ShoppingList(products,
                shoppingListName, Utils.getCurrentTime(), shoppingListId, storage.getId(), storage.getName());

        // Saves a new shopping list into the database
        SHOPPING_LIST_REFERENCE.child(shoppingList.getId()).
                setValue(shoppingList)
                .addOnCompleteListener(task ->
                        addShoppingListToStorage(activity, storage, shoppingList)
                );
    }

    /**
     * Starts the shopping list activity and sets the intent with the shopping list data
     *
     * @param activity     Activity that uses this method
     * @param shoppingList
     */
    private static void startShoppingListActivity(Activity activity, ShoppingList shoppingList) {
        // Configure the intent
        Intent intent = new Intent(activity, ShoppingListDetailActivity.class);
        intent.putExtra("shoppingListId", shoppingList.getId());
        intent.putExtra("shoppingListName", shoppingList.getName());
        // Starts the activity
        activity.startActivity(intent);
    }
}
