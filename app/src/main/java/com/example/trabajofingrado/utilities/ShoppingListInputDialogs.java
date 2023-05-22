package com.example.trabajofingrado.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.trabajofingrado.controller.ShoppingListDetailActivity;
import com.example.trabajofingrado.model.ShoppingList;
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
import java.util.Objects;

public class ShoppingListInputDialogs {
    public static DatabaseReference shoppingListReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

    public static AlertDialog deleteShoppingListDialog(Activity activity, String shoppingListId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Are you sure that you want to delete this shopping list?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();



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

    public static AlertDialog updateShoppingListNameDialog(Activity activity, String shoppingListId) {
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
                    shoppingListReference.child(shoppingListId)
                            .child("name")
                            .setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(activity.getClass().equals(ShoppingListDetailActivity.class)){
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
