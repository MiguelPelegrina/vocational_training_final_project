package com.example.trabajofingrado.controller;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.trabajofingrado.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class LauncherActivity extends AppCompatActivity {
    private SharedPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Access the preferences to skip the authentication if the user already signed in before
        loginPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the information
        String email = loginPreferences.getString("email", "");
        String password = loginPreferences.getString("password", "");

        Log.d("Login data", email + " : " + password);

        // Check if there is any information available
        if(!password.isEmpty() && !email.isEmpty()){
            // Sign in
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email,
                    password
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Move to the next activity depending on the
                    if(task.isSuccessful()){
                        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }else{
            Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);


    }
}