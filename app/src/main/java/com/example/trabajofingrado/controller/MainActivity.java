package com.example.trabajofingrado.controller;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main_activity, new MainFragment())
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }
}

