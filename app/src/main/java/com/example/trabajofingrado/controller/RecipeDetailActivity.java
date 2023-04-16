package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {
    private TextView txtName;
    private TextView txtIngredients;
    private TextView txtSteps;
    private ImageView imgRecipeDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        txtName = findViewById(R.id.txtRecipeDetailName);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtSteps = findViewById(R.id.txtSteps);
        imgRecipeDetail = findViewById(R.id.imgRecipeDetailImage);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
        Query query = database.orderByChild("name").equalTo(getIntent().getStringExtra("name"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        txtName.setText(recipe.getName());

                        Glide.with(RecipeDetailActivity.this)
                                .load(recipe.getImage())
                                .error(R.drawable.image_not_found)
                                .into(imgRecipeDetail);

                        for (Map.Entry<String, String> ingredient : recipe.getIngredients().entrySet()) {
                            txtIngredients.append("\n - " + ingredient.getKey() + ": " + ingredient.getValue());
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
        });
    }
}














