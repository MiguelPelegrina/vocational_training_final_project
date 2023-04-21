package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.User;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class AuthenticationActivity extends AppCompatActivity {
    // Fields
    private Button btnRegister;
    private Button btnLogin;
    private EditText txtUserEmail;
    private EditText txtUserPassword;
    private SharedPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Access the preferences to skip the authentication if the user already signed in before
        loginPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = loginPreferences.getString("email", "");
        String password = loginPreferences.getString("password", "");
        if(!password.isEmpty() && !email.isEmpty()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email,
                    password
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        toMainActivity();
                    }
                }
            });
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        this.setTitle("Authentication");

        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        txtUserEmail = findViewById(R.id.txtEmail);
        txtUserPassword = findViewById(R.id.txtPassword);

        Toasty.Config.getInstance().setTextSize(20).apply();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the edit texts are empty
                if(Utils.checkValidStrings(getEditTextsAsList())){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            txtUserEmail.getText().toString(),
                            txtUserPassword.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toasty.success(AuthenticationActivity.this,
                                        "You signed in!",
                                        Toasty.LENGTH_SHORT,true).show();
                                toMainActivity();
                            }else{
                                Toasty.error(AuthenticationActivity.this,
                                        "You could not sign in, you might be registered already",
                                        Toasty.LENGTH_LONG,true).show();
                            }
                        }
                    });

                    /*User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.USERPATH);
                    Query query = database.orderByChild("email").equalTo(user.getEmail());
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Intent intent = new Intent(AuthenticationActivity.this, ChoiceActivity.class);
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    intent.putExtra("username",ds.getKey());
                                }
                                Toasty.success(AuthenticationActivity.this,
                                        "Se ha logeado exitosamente",
                                        Toasty.LENGTH_SHORT,true).show();
                                if(saveLoginCheckBox.isChecked()){
                                    loginPreferencesEditor.putBoolean("saveLogin", true);
                                    loginPreferencesEditor.putString("email", txtUserEmail.getText().toString());
                                    loginPreferencesEditor.putString("name", txtUserName.getText().toString());
                                    loginPreferencesEditor.putString("password", txtUserPassword.getText().toString());
                                    loginPreferencesEditor.commit();
                                }else{
                                    loginPreferencesEditor.clear();
                                    loginPreferencesEditor.commit();
                                }
                                startActivity(intent);
                            }else{
                                Toasty.error(AuthenticationActivity.this,
                                        "No se ha podido logear, probablemente no esté " +
                                                "registrado", Toasty.LENGTH_LONG,true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                            Toasty.error(AuthenticationActivity.this,
                                    "No se ha podido logear, probablemente no tenga " +
                                            "conexión", Toasty.LENGTH_LONG,true).show();
                        }
                    };
                    query.addListenerForSingleValueEvent(eventListener);*/
                }else{
                    Toasty.error(AuthenticationActivity.this,
                            "Enter valid data: empty fields or those filled with space " +
                                    "are not allowed",
                            Toasty.LENGTH_LONG,true).show();
                }
            }
        });

         btnLogin.setOnClickListener(new View.OnClickListener() {
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
                                Toasty.success(AuthenticationActivity.this,
                                        "You logged in!",
                                        Toasty.LENGTH_SHORT,true).show();
                                toMainActivity();
                            }else{
                                Toasty.error(AuthenticationActivity.this,
                                        "You could not log in, you might have to " +
                                                "register first",
                                        Toasty.LENGTH_LONG,true).show();
                            }
                        }
                    });

                    /*User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.USERPATH);
                    Query query = database.orderByChild("email").equalTo(user.getEmail());
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                database.push().setValue(user);
                                Toasty.success(AuthenticationActivity.this,
                                        "Se ha registrado exitosamente, ya puede hacer Login",
                                        Toasty.LENGTH_SHORT,true).show();
                            }else{
                                Toasty.error(AuthenticationActivity.this,
                                        "No se ha podido registrar, probablemente ya esté " +
                                                "registrado", Toasty.LENGTH_LONG,true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                            Toasty.error(AuthenticationActivity.this,
                                    "No se ha podido registrar, probablemente no tenga " +
                                            "conexión", Toasty.LENGTH_LONG,true).show();
                        }
                    };
                    query.addListenerForSingleValueEvent(eventListener);*/
                }else{
                    Toasty.error(AuthenticationActivity.this,
                            "Enter valid data: empty fields or those filled with space " +
                                    "are not allowed",
                            Toasty.LENGTH_LONG,true).show();
                }
            }
        });
    }

    private void toMainActivity(){
        String email = txtUserEmail.getText().toString();
        String password = txtUserPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();
        }
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