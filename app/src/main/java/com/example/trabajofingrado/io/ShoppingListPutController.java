package com.example.trabajofingrado.io;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingListPutController {
    public static void createNewShoppingList(Activity activity, Storage storage, String name) {
        DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        HashMap<String, Boolean> users = new HashMap<>();
        for(Map.Entry<String, Boolean> user : storage.getUsers().entrySet()){
            users.put(user.getKey(), true);
        }

        String shoppingListId =  UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList(users,
                name, Utils.getCurrentTime(), shoppingListId, storage.getId(), storage.getName()) ;

        // TODO UPDATE STORAGE WITH SHOPPING LISTS AS WELL

        shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startShoppingListActivity(activity, shoppingList);
            }
        });
    }

    public static void createNewShoppingListWithProducts(Activity activity, HashMap<String, StorageProduct> products, Storage storage, String name) {
        DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        HashMap<String, Boolean> users = new HashMap<>();
        for(Map.Entry<String, Boolean> user : storage.getUsers().entrySet()){
            users.put(user.getKey(), true);
        }

        String shoppingListId =  UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList(products, users,
                name, Utils.getCurrentTime(), shoppingListId, storage.getId(), storage.getName()) ;

        // TODO UPDATE STORAGE WITH SHOPPING LISTS AS WELL

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
