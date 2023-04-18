package com.example.trabajofingrado.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.trabajofingrado.R;

public class ChoiceActivity extends AppCompatActivity {
    private Button btnRecipes;
    private Button btnStorages;
    private Button btnMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        btnRecipes = findViewById(R.id.btnRecipes);
        btnStorages = findViewById(R.id.btnStorages);
        btnMainActivity = findViewById(R.id.btnMainActivity);

        String username = getIntent().getStringExtra("username");

        btnRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChoiceActivity.this,RecipeListActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        btnStorages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChoiceActivity.this, StorageListActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("activity", "choiceActivity");
                startActivity(intent);
            }
        });

        btnMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChoiceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}