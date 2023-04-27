package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private static final int RECIPE_MODIFY_RESULT_CODE = 1;
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

        setData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECIPE_MODIFY_RESULT_CODE) {
            setData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (getIntent().getStringExtra("action").equals("modify")){
            menu.findItem(R.id.menu_item_modify_recipe).setEnabled(true);
        }else{
            menu.findItem(R.id.menu_item_modify_recipe).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify_recipe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_modify_recipe:
                Intent intent = new Intent(RecipeDetailActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "modify");
                intent.putExtra("recipeUUID", getIntent().getStringExtra("recipeUUID"));
                startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    private void setData(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        Query query = database.orderByChild("uuid").equalTo(getIntent().getStringExtra("recipeUUID"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtIngredients.setText(getString(R.string.ingredients));
                txtSteps.setText(getString(R.string.steps));
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














