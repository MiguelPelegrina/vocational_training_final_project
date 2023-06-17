package com.example.trabajofingrado.controller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the authentication of the user
 */
public class AuthenticationActivity extends AppCompatActivity {
    // Fields
    // Of class
    private static final int GOOGLE_SIGN_IN = 1;
    // Of instance
    private Button btnSignIn, btnSignUp;
    private TextInputEditText txtEmail, txtPassword;
    private TextInputLayout txtPasswordLayout;
    private SignInButton btnGoogle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        // Set the title
        setTitle("Cook together everywhere!");

        // Bind the views
        bindViews();

        // Set the listener
        setListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null){
                    String tokenId = account.getIdToken();
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(tokenId,null);
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            toCalendarActivity("google");
                        }else{
                            GoogleSignInError();
                        }
                    });
                }
            } catch (ApiException e) {
                GoogleSignInError();
            }
        }else{
            GoogleSignInError();
        }
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogleSignIn);
        toolbar = findViewById(R.id.toolbar_authentication);
        setSupportActionBar(toolbar);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPasswordLayout = findViewById(R.id.txtPasswordLayout);
    }

    private void setListener() {
        // Set an on click listener to sign up the user
        btnSignUp.setOnClickListener(view -> {
            // Check if the edit texts are empty
            if(Utils.checkValidStrings(getEditTextsAsList())){
                // Sign up the user
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        txtEmail.getText().toString(),
                        txtPassword.getText().toString()
                ).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        // Communicate to the user that they signed up
                        Toasty.success(AuthenticationActivity.this,
                                "You signed up!",
                                Toasty.LENGTH_SHORT,true).show();
                        // Move to the next activity
                        toCalendarActivity("email");
                    }else{
                        // Communicate to the user that they are already signed up
                        Toasty.error(AuthenticationActivity.this,
                                "You could not sign up, you might have signed up before",
                                Toasty.LENGTH_LONG,true).show();
                    }
                });
            }else{
                // Communicate to the user that an error happened
                Toasty.error(AuthenticationActivity.this,
                        "Enter valid data: empty fields or those filled with space " +
                                "are not allowed",
                        Toasty.LENGTH_LONG,true).show();
            }
        });

        // Set an on click listener to sign in the user
        btnSignIn.setOnClickListener(view -> {
            // Check if the edit texts are empty
            if(Utils.checkValidStrings(getEditTextsAsList())){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        txtEmail.getText().toString(),
                        txtPassword.getText().toString()
                ).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        // Communicate to the user that they signed in
                        Toasty.success(AuthenticationActivity.this,
                                "You signed in!",
                                Toasty.LENGTH_SHORT,true).show();
                        toCalendarActivity("email");
                    }else{
                        // Communicate to the user that they need to sign up before signing in
                        Toasty.error(AuthenticationActivity.this,
                                "You could not sign in, you might have to sign up first",
                                Toasty.LENGTH_LONG,true).show();
                    }
                });
            }else{
                // Communicate to the user that an error happened
                Toasty.error(AuthenticationActivity.this,
                        "Enter valid data: empty fields or those filled with space " +
                                "are not allowed",
                        Toasty.LENGTH_LONG,true).show();
            }
        });

        btnGoogle.setOnClickListener(view -> {
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(AuthenticationActivity.this, gso);

            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN);
        });
    }

    private void GoogleSignInError(){
        Toasty.error(AuthenticationActivity.this,
                "You could not sign in with Google.",
                Toasty.LENGTH_LONG,true).show();
    }

    /**
     * Method that starts the main activity while saving the user data for the next login
     */
    private void toCalendarActivity(String signInMethod){
        switch (signInMethod){
            case "email":
                // Get the introduced data
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();

                // Check if any data is available
                if(!email.isEmpty() && !password.isEmpty()){
                    // Save the data for the next login
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("signInMethod", signInMethod.toString());
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.apply();
                }
                break;
            case "google":
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("signInMethod", signInMethod.toString());
                editor.apply();
                break;
        }


        // Start the main activity
        Intent intent = new Intent(AuthenticationActivity.this, CalendarActivity.class);
        startActivity(intent);
    }

    /**
     * Method to get the content of all used EditTexts of this activity in one list
     * @return A list of the EditTexts strings
     */
    private ArrayList<String> getEditTextsAsList(){
        // Declare and instance a list
        ArrayList<String> editTextStrings = new ArrayList<>();

        // Set the values
        editTextStrings.add(txtEmail.getText().toString());
        editTextStrings.add(txtPassword.getText().toString());

        // Return the list
        return editTextStrings;
    }
}