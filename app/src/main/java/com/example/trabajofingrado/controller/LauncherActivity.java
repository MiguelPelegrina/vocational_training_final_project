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

/**
 * Controller that handles the use cases related to the authentication when a user has an opened
 * session:
 *  - Sign in with email and password
 *  - Sign in with a Google account
 */
public class LauncherActivity extends AppCompatActivity {
    // Fields
    private SharedPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Access the preferences to skip the authentication if the user already signed in before
        // Fields
        loginPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the provider if available
        String provider = loginPreferences.getString("signInMethod", null);

        // Check if there is any provider
        if (provider != null) {
            // Check the type of provider
            switch (provider) {
                case "email":
                    signInWithEmail();

                    break;
                case "google":
                    signInWithGoogle();

                    break;
            }
        } else {
            // Move the user to the authentication activity
            startActivity(new Intent(LauncherActivity.this, AuthenticationActivity.class));
            finish();
        }
    }

    /**
     * Signs in the user with their email
     */
    private void signInWithEmail() {
        // Get their data
        String email = loginPreferences.getString("email", null);
        String password = loginPreferences.getString("password", null);

        // Check if there is any information available
        if (password != null && email != null) {
            // Sign in the user
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        Intent intent;

                        // Check if the sign was successful
                        if (task.isSuccessful()) {
                            // Set the intent
                            intent = new Intent(LauncherActivity.this, CalendarActivity.class);
                        } else {
                            intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
                        }

                        // Move to the next activity
                        startActivity(intent);
                    });
        } else {
            // Move the user to the authentication activity
            startActivity(new Intent(LauncherActivity.this, AuthenticationActivity.class));
            finish();
        }
    }

    /**
     * Signs in the user with their Google account
     */
    private void signInWithGoogle() {
        // Generate the last signed in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        Intent intent;
        // Check if the account is instanced
        if (account != null) {
            // Set the intent
            intent = new Intent(LauncherActivity.this, CalendarActivity.class);
        } else {
            intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
        }
        // Move the user to the next activity
        startActivity(intent);
        finish();
    }
}