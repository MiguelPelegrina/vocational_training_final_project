package com.example.trabajofingrado.io;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ShoppingListPutController {
    private static final DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

    public static void createNewShoppingList(Activity activity, Storage storage, String name) {
        HashMap<String, Boolean> users = new HashMap<>();
        for(Map.Entry<String, Boolean> user : storage.getUsers().entrySet()){
            users.put(user.getKey(), true);
        }

        ShoppingList shoppingList = new ShoppingList(
                users, name,
                Utils.getCurrentTime(), UUID.randomUUID().toString(),
                storage.getId(), storage.getName());

        shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                Query query = storageReference.orderByChild("id").equalTo(storage.getId());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Storage shoppingListStorage = ds.getValue(Storage.class);
                            if(shoppingListStorage.getShoppingLists() == null){
                                HashMap<String, Boolean> shoppingLists = new HashMap<>();
                                shoppingLists.put(shoppingList.getId(), true);
                                storageReference.child(storage.getId())
                                                .child("shoppingLists")
                                                .setValue(shoppingLists);
                            } else{
                                Map<String, Object> updates = new HashMap<>();

                                updates.put(storage.getId() + "/shoppingLists/" + shoppingList.getId(), true);
                                storageReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {
                                            Utils.connectionError(activity);
                                        } else {

                                        }
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
            }
        });
    }

    public static void createNewShoppingListWithProducts(Activity activity, HashMap<String, StorageProduct> products, Storage storage, String name) {
        HashMap<String, Boolean> users = new HashMap<>();
        for(Map.Entry<String, Boolean> user : storage.getUsers().entrySet()){
            users.put(user.getKey(), true);
        }

        String shoppingListId =  UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList(products, users,
                name, Utils.getCurrentTime(), shoppingListId, storage.getId(), storage.getName());

        shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startShoppingListActivity(activity, shoppingList);
            }
        });
    }

    private static void startShoppingListActivity(Activity activity, ShoppingList shoppingList){
        Intent intent = new Intent(activity, ShoppingListDetailActivity.class);
        intent.putExtra("shoppingListId", shoppingList.getId());
        intent.putExtra("shoppingListName", shoppingList.getName());
        activity.startActivity(intent);
    }
}
