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

public class ShoppingListPutController {
    public static void createNewShoppingList(Activity activity, Storage storage, String name) {

        ShoppingList shoppingList = new ShoppingList(
                name, Utils.getCurrentTime(), UUID.randomUUID().toString(),
                storage.getId(), storage.getName());

        SHOPPING_LIST_REFERENCE.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(task -> {
            Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storage.getId());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Storage shoppingListStorage = ds.getValue(Storage.class);
                        if(shoppingListStorage.getShoppingLists() == null){
                            HashMap<String, Boolean> shoppingLists = new HashMap<>();
                            shoppingLists.put(shoppingList.getId(), true);
                            STORAGE_REFERENCE.child(storage.getId())
                                            .child("shoppingLists")
                                            .setValue(shoppingLists);
                        } else{
                            Map<String, Object> updates = new HashMap<>();

                            updates.put(storage.getId() + "/shoppingLists/" + shoppingList.getId(), true);
                            STORAGE_REFERENCE.updateChildren(updates, (error, ref) -> {
                                if (error != null) {
                                    Utils.connectionError(activity);
                                } else {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utils.connectionError(activity);
                }
            });

            startShoppingListActivity(activity, shoppingList);
        });
    }

    public static void createNewShoppingListWithProducts(Activity activity, HashMap<String, StorageProduct> products, Storage storage, String name) {
        String shoppingListId =  UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList(products,
                name, Utils.getCurrentTime(), shoppingListId, storage.getId(), storage.getName());

        SHOPPING_LIST_REFERENCE.child(shoppingList.getId()).
                setValue(shoppingList)
                .addOnCompleteListener(task -> startShoppingListActivity(activity, shoppingList));
    }

    private static void startShoppingListActivity(Activity activity, ShoppingList shoppingList){
        Intent intent = new Intent(activity, ShoppingListDetailActivity.class);
        intent.putExtra("shoppingListId", shoppingList.getId());
        intent.putExtra("shoppingListName", shoppingList.getName());
        activity.startActivity(intent);
    }
}
