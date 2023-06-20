package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.utilities.Utils.checkValidStrings;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;

import com.example.trabajofingrado.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use cases related to the authentication when a user does not have an
 * opened session:
 *  - Sign up/sign in with email and password
 *  - Sign in with a Google account
 */
public class AuthenticationActivity extends AppCompatActivity {
    // Fields
    // Of class
    private static final int GOOGLE_SIGN_IN = 1;

    // Of instance
    private Button btnSignIn, btnSignUp;
    private SignInButton btnGoogle;
    private TextInputEditText txtEmail, txtPassword;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setTitle(R.string.app_name);

        bindViews();

        setListener();
    }

    /**
     * This is activity result is called when a the GoogleSignInIntent is finished
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the request code
        if(requestCode == GOOGLE_SIGN_IN){
            // Get the signed in google account
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Get the account
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Check if the account is instanced
                if (account != null){
                    // Get the id of the token
                    String tokenId = account.getIdToken();

                    // Authenticate the token
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(tokenId,null);

                    // Sign in
                    FirebaseAuth.getInstance().signInWithCredential(authCredential)
                            .addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            // Move to the next activity
                            toCalendarActivity("google");
                        }else{
                            googleSignInError();
                        }
                    });
                }
            } catch (ApiException e) {
                googleSignInError();
            }
        }else{
            googleSignInError();
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
    }

    /**
     * Sets the listener of the views
     */
    private void setListener() {
        // Set an on click listener to sign up the user
        btnSignUp.setOnClickListener(view -> {
            // Check if the edit texts are empty
            if(checkValidStrings(getEditTextsAsList())){
                // Sign up with email and password
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(txtEmail.getText().toString(),
                        txtPassword.getText().toString()).addOnCompleteListener(task -> {

                            // Check if the user was signed up successfully
                    if(task.isSuccessful()){
                        // Inform the user
                        Toasty.success(AuthenticationActivity.this,"You signed up!").show();

                        // Move to the next activity
                        toCalendarActivity("email");
                    }else{
                        // Inform the user
                        Toasty.error(AuthenticationActivity.this,
                                "You could not sign up, you might have signed up before").show();
                    }
                });
            }else{
                // Inform the user
                enterValidData(AuthenticationActivity.this);
            }
        });

        // Set an on click listener to sign in the user
        btnSignIn.setOnClickListener(view -> {
            // Check if the edit texts are empty
            if(checkValidStrings(getEditTextsAsList())){
                // Sign in with email and password
                FirebaseAuth.getInstance().signInWithEmailAndPassword(txtEmail.getText().toString(),
                        txtPassword.getText().toString()).addOnCompleteListener(task -> {
                    // Check if the user was signed in successfully
                    if(task.isSuccessful()){
                        // Inform the user
                        Toasty.success(AuthenticationActivity.this,"You signed in!").show();

                        // Move to the next activity
                        toCalendarActivity("email");
                    }else{
                        // Inform the user
                        Toasty.error(AuthenticationActivity.this,
                                "You could not sign in, you might have to sign up first").show();
                    }
                });
            }else{
                // Inform the user
                enterValidData(AuthenticationActivity.this);
            }
        });

        btnGoogle.setOnClickListener(view -> {
            // Generate the google sign in options
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            // Generate the google sign in client
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(AuthenticationActivity.this, gso);

            // Move to the google sign in activity
            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN);
        });
    }

    /**
     * Informs the user that an error happened
     */
    private void googleSignInError(){
        Toasty.error(AuthenticationActivity.this,"You could not sign in with Google.").show();
    }

    /**
     * Method that starts the main activity while saving the user data for the next login
     */
    private void toCalendarActivity(String signInMethod){
        // Generate the shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Generate the editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (signInMethod){
            case "email":
                // Get the introduced data
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();

                // Check if any data is available
                if(!email.isEmpty() && !password.isEmpty()){
                    // Save the data for the next login
                    editor.putString("signInMethod", signInMethod);
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.apply();
                }
                break;
            case "google":
                // Save the data for the next login
                editor.putString("signInMethod", signInMethod);
                editor.apply();
                break;
        }

        // Move to the next activity
        startActivity(new Intent(AuthenticationActivity.this, CalendarActivity.class));
    }

    /**
     * Gets the content of all used edit texts of this activity as list
     * @return A list that contains strings of the edit texts
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