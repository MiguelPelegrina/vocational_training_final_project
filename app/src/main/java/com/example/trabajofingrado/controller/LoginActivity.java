package com.example.trabajofingrado.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.User;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class LoginActivity extends AppCompatActivity {
    // Fields
    private Button btnLogin;
    private Button btnRegister;
    private EditText txtUserEmail;
    private EditText txtUserName;
    private EditText txtUserPassword;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        txtUserEmail = findViewById(R.id.txtEmail);
        txtUserName = findViewById(R.id.txtName);
        txtUserPassword = findViewById(R.id.txtPassword);
        saveLoginCheckBox = findViewById(R.id.cbSaveLoginData);

        loginPreferences = getSharedPreferences("loginPreferences", MODE_PRIVATE);
        loginPreferencesEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin",false);
        if(saveLogin){
            // Rellenamos los campos de texto y checkeamos el checkBox
            txtUserEmail.setText(loginPreferences.getString("email", ""));
            txtUserName.setText(loginPreferences.getString("name", ""));
            txtUserPassword.setText(loginPreferences.getString("password",""));
            saveLoginCheckBox.setChecked(true);
        }

        Toasty.Config.getInstance().setTextSize(20).apply();
        Toasty.info(this,"Para poder hacer login debe registrarse primero",Toasty.LENGTH_LONG, true).show();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comprobarCampos()){
                    User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.USERPATH);
                    Query query = database.orderByChild("email").equalTo(user.getEmail());
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Intent intent = new Intent(LoginActivity.this, ChoiceActivity.class);
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    intent.putExtra("username",ds.getKey());
                                }
                                Toasty.success(LoginActivity.this,
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
                                Toasty.error(LoginActivity.this,
                                        "No se ha podido logear, probablemente no esté " +
                                                "registrado", Toasty.LENGTH_LONG,true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                            Toasty.error(LoginActivity.this,
                                    "No se ha podido logear, probablemente no tenga " +
                                            "conexión", Toasty.LENGTH_LONG,true).show();
                        }
                    };
                    query.addListenerForSingleValueEvent(eventListener);
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducir datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });

         btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comprobarCampos()){
                    User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.USERPATH);
                    Query query = database.orderByChild("email").equalTo(user.getEmail());
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                database.push().setValue(user);
                                Toasty.success(LoginActivity.this,
                                        "Se ha registrado exitosamente, ya puede hacer Login",
                                        Toasty.LENGTH_SHORT,true).show();
                            }else{
                                Toasty.error(LoginActivity.this,
                                        "No se ha podido registrar, probablemente ya esté " +
                                                "registrado", Toasty.LENGTH_LONG,true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                            Toasty.error(LoginActivity.this,
                                    "No se ha podido registrar, probablemente no tenga " +
                                            "conexión", Toasty.LENGTH_LONG,true).show();
                        }
                    };
                    query.addListenerForSingleValueEvent(eventListener);
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducir datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });
    }


    private boolean comprobarCampos(){
        boolean camposValidos = false;

        if(txtUserEmail.getText().toString().trim().length() > 0 &&
                txtUserEmail.getText().toString().trim().length() > 0 &&
                txtUserPassword.getText().toString().trim().length() > 0){
            camposValidos = true;
        }

        return camposValidos;
    }
}