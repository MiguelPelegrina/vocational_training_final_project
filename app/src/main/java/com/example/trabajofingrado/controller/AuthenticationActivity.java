package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class AuthenticationActivity extends AppCompatActivity {
    // Fields
    private Button btnSignUp;
    private Button btnSignIn;
    private EditText txtUserEmail;
    private EditText txtUserPassword;
    private SharedPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Access the preferences to skip the authentication if the user already signed in before
        loginPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the information
        String email = loginPreferences.getString("email", "");
        String password = loginPreferences.getString("password", "");

        // Check if there is any information available
        if(!password.isEmpty() && !email.isEmpty()){
            // Sign in
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email,
                    password
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        // Move to the next activity
                        toMainActivity();
                    }
                }
            });
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Set the title
        this.setTitle("Authentication");

        // Instance the fields
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        txtUserEmail = findViewById(R.id.txtEmail);
        txtUserPassword = findViewById(R.id.txtPassword);

        // Configure Toasty to a bigger font size
        Toasty.Config.getInstance().setTextSize(20).apply();

        // Set an on click listener to sign up the user
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the edit texts are empty
                if(Utils.checkValidStrings(getEditTextsAsList())){
                    // Sign up the user
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            txtUserEmail.getText().toString(),
                            txtUserPassword.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // Communicate to the user that they signed up
                                Toasty.success(AuthenticationActivity.this,
                                        "You signed up!",
                                        Toasty.LENGTH_SHORT,true).show();
                                // Move to the next activity
                                toMainActivity();
                            }else{
                                // Communicate to the user that they are already signed up
                                Toasty.error(AuthenticationActivity.this,
                                        "You could not sign in, you might be registered " +
                                                "already",
                                        Toasty.LENGTH_LONG,true).show();
                            }
                        }
                    });
                }else{
                    // Communicate to the user that an error happened
                    Toasty.error(AuthenticationActivity.this,
                            "Enter valid data: empty fields or those filled with space " +
                                    "are not allowed",
                            Toasty.LENGTH_LONG,true).show();
                }
            }
        });

        // Set an on click listener to sign in the user
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the edit texts are empty
                if(Utils.checkValidStrings(getEditTextsAsList())){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            txtUserEmail.getText().toString(),
                            txtUserPassword.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // Communicate to the user that they signed in
                                Toasty.success(AuthenticationActivity.this,
                                        "You logged in!",
                                        Toasty.LENGTH_SHORT,true).show();
                                toMainActivity();
                            }else{
                                // Communicate to the user that they need to sign up before signing in
                                Toasty.error(AuthenticationActivity.this,
                                        "You could not log in, you might have to " +
                                                "register first",
                                        Toasty.LENGTH_LONG,true).show();
                            }
                        }
                    });
                }else{
                    // Communicate to the user that an error happened
                    Toasty.error(AuthenticationActivity.this,
                            "Enter valid data: empty fields or those filled with space " +
                                    "are not allowed",
                            Toasty.LENGTH_LONG,true).show();
                }
            }
        });
    }

    /**
     * Method that starts the main activity while saving the user data for the next login
     */
    private void toMainActivity(){
        // Get the introduced data
        String email = txtUserEmail.getText().toString();
        String password = txtUserPassword.getText().toString();

        // Check if any data is available
        if(!email.isEmpty() && !password.isEmpty()){
            // Save the data for the next login
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();
        }

        // Start the main activity
        Intent intent = new Intent(AuthenticationActivity.this, ChoiceActivity.class);
        startActivity(intent);
    }

    /**
     * Method to get the content of all used EditTexts of this activity in one list
     * @return A list of the EditTexts strings
     */
    private ArrayList<String> getEditTextsAsList(){
        // Declare and instanc a list
        ArrayList<String> editTextStrings = new ArrayList<>();

        // Set the values
        editTextStrings.add(txtUserEmail.getText().toString());
        editTextStrings.add(txtUserPassword.getText().toString());

        // Return the list
        return editTextStrings;
    }
}