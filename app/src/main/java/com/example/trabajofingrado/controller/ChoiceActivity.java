package com.example.trabajofingrado.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.trabajofingrado.R;

public class ChoiceActivity extends AppCompatActivity {
    private Button btnRecipes;
    private Button btnStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        btnRecipes = findViewById(R.id.btnRecipes);
        btnStorage = findViewById(R.id.btnStorage);

        btnRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChoiceActivity.this,RecipeListActivity.class);
                startActivity(intent);
            }
        });

        btnStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChoiceActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });
    }
}