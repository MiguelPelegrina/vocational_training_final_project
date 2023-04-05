package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailActivity extends AppCompatActivity {
    private TextView txtName;
    private TextView txtIngredients;
    private TextView txtSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        txtName = findViewById(R.id.txtRecipeDetailName);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtSteps = findViewById(R.id.txtSteps);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Recipes");
        Query query = database.child("1");
                //.orderByChild("name").equalTo(getIntent().getStringExtra("recipeName"));
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Log.d("Recipe", snapshot.toString());
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        txtName.setText(recipe.getName());

                        for (String ingredient : recipe.getIngredients().values()) {
                            txtIngredients.append("\n - " + ingredient);
                        }

                        for (String step : recipe.getSteps()) {
                            txtSteps.append("\n - " + step);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };
        query.addListenerForSingleValueEvent(eventListener);
    }
}














