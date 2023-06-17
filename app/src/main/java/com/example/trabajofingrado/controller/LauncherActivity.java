package com.example.trabajofingrado.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.trabajofingrado.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class LauncherActivity extends AppCompatActivity {
    // Fields
    private SharedPreferences loginPreferences;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Access the preferences to skip the authentication if the user already signed in before
        loginPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the data if available
        String provider = loginPreferences.getString("signInMethod", null);

        if(provider != null){
            switch (provider){
                case "email":
                    email = loginPreferences.getString("email", null);
                    password = loginPreferences.getString("password", null);

                    // Check if there is any information available
                    if(password != null && email != null){
                        // Sign in
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener(task -> {
                            // Move to the next activity depending on the
                            Intent intent;
                            if(task.isSuccessful()){
                                intent = new Intent(LauncherActivity.this, CalendarActivity.class);
                            }else{
                                intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                            }
                            startActivity(intent);
                        });
                    }else{
                        Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case "google":
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                    Intent intent;
                    if(account != null){
                        intent = new Intent(LauncherActivity.this, CalendarActivity.class);
                    }else{
                        intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                    }
                    startActivity(intent);
                    finish();
                    break;
            }
        }else{
            Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }

        // Configure Toasty for a bigger font size
        Toasty.Config.getInstance().setTextSize(20).apply();
    }
}