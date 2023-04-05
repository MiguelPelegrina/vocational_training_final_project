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
import com.example.trabajofingrado.io.FirebaseUser;
import com.example.trabajofingrado.model.User;
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
    //Declaracion de variables
    private Button btnLogin;
    private Button btnRegister;
    private EditText txtUserEmail;
    private EditText txtUserName;
    private EditText txtUserPassword;
    // Variables encargadas de guardar los datos de login del usuario que ha realizado un login
    // de forma exitosa
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializacion de variables
        // Asociamos los elemento del layout con el código
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        txtUserEmail = findViewById(R.id.txtEmail);
        txtUserName = findViewById(R.id.txtName);
        txtUserPassword = findViewById(R.id.txtPassword);
        saveLoginCheckBox = findViewById(R.id.cbSaveLoginData);

        // Obtenemos las preferencias encargadas de guardar los datos del login
        loginPreferences = getSharedPreferences("loginPreferences", MODE_PRIVATE);
        // Inicializamos el editor
        loginPreferencesEditor = loginPreferences.edit();
        // Si anteriormente se han guardado los datos
        saveLogin = loginPreferences.getBoolean("saveLogin",false);
        if(saveLogin){
            // Rellenamos los campos de texto y checkeamos el checkBox
            txtUserEmail.setText(loginPreferences.getString("email", ""));
            txtUserName.setText(loginPreferences.getString("name", ""));
            txtUserPassword.setText(loginPreferences.getString("password",""));
            saveLoginCheckBox.setChecked(true);
        }

        // La biblioteca Toasty (diferente a la vista en clase) permite modificar los atributos por
        // defecto. En este caso aumentamos su tamaño
        Toasty.Config.getInstance().setTextSize(20).apply();
        // Informamos al usuario
        Toasty.info(this,"Para poder hacer login debe registrarse primero",Toasty.LENGTH_LONG, true).show();

        //FirebaseUser firebaseUser = new FirebaseUser();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comprobarCampos()){
                    User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
                    Query query = database.orderByChild("email").equalTo(user.getEmail());
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Log.d("User", snapshot.toString());
                                Intent intent = new Intent(LoginActivity.this, ChoiceActivity.class);
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
                                // Si el usuario ya está registrado
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

        // Oyente que gestiona el evento OnClick sobre el botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos que los campos no estén vacios, si no lo están informamos al usuario
                if(comprobarCampos()){
                    User user = new User(txtUserEmail.getText().toString(), txtUserName.getText().toString(), txtUserPassword.getText().toString());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
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
                                // Si el usuario ya está registrado
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

    /**
     * Método que comprueba que los campos de texto no estén vacios
     * @return Devuelve true si la longitud del texto de los campos de texto es mayor que 0 y sino
     * devuelve false
     */
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