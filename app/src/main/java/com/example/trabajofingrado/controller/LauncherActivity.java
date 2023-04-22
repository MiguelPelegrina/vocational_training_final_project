package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.trabajofingrado.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class LauncherActivity extends AppCompatActivity {
    // Fields
    private SharedPreferences loginPreferences;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                                }else{
                                    Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }else{
                        Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                        startActivity(intent);
                    }
                    break;
                case "google":
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    googleSignInClient = GoogleSignIn.getClient(LauncherActivity.this, gso);

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                    if(account != null){
                        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                        startActivity(intent);
                    }
                    break;

            }
        }else{
            Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }
}