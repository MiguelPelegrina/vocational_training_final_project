package com.example.trabajofingrado.controller;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ProductRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AddRecipeActivity extends AppCompatActivity {
    private ArrayList<Product> ingredientList = new ArrayList<>();
    private ArrayList<String> stepList = new ArrayList<>();
    private RecyclerView recyclerViewIngredients;
    private RecyclerView recyclerViewSteps;
    private ProductRecyclerAdapter recyclerAdapterIngredients;
    private ProductRecyclerAdapter recyclerAdapterSteps;
    private RecyclerView.ViewHolder viewHolderIngredient;
    private RecyclerView.ViewHolder viewHolderStep;
    private Button btnAddIngredient;
    private Button btnAddStep;
    private int position;
    private Product ingredient;

    private View auxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        this.btnAddIngredient = findViewById(R.id.btnRecipeDetailAddIngredient);
        this.btnAddStep = findViewById(R.id.btnRecipeDetailAddStep);
        this.recyclerViewIngredients = findViewById(R.id.rvRecipeDetailIngredients);
        this.recyclerAdapterIngredients = new ProductRecyclerAdapter(ingredientList);
        //this.recyclerViewSteps = new ;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        this.recyclerViewIngredients.setAdapter(recyclerAdapterIngredients);
        //this.raSteps
        this.recyclerViewIngredients.setLayoutManager(layoutManager);
        //this.raSteps

        this.btnAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddIngredientDialog().show();
            }
        });

        this.btnAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddStepDialog().show();
            }
        });

        this.recyclerAdapterIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(recyclerViewIngredients);
                auxView = view;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_recipe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_save_recipe:
                /*DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
                Query query = database.;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            database.setValue( );
                            Toasty.info(AddRecipeActivity.this, "The " +
                                    "recipe was saved.").show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });*/
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.modify_ingredient_menu, menu);
        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        viewHolderIngredient = (RecyclerView.ViewHolder) auxView.getTag();
        position = viewHolderIngredient.getAdapterPosition();
        ingredient = ingredientList.get(position);
        switch (item.getItemId()){
            case R.id.modifyIngredient:
                createModifyProductDialog(ingredient).show();
                break;
            case R.id.deleteIngrdient:
                ingredientList.remove(ingredient);
                break;
        }
        recyclerAdapterIngredients.notifyDataSetChanged();

        return true;
    }

    private AlertDialog createAddStepDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce a step");

        final EditText inputStep = new EditText(this);
        inputStep.setHint("Step");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stepList.add(inputStep.getText().toString());
                recyclerAdapterSteps.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private AlertDialog createAddIngredientDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the ingredient you will use");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        layout.addView(inputName);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final EditText inputUnits = new EditText(this);
        inputUnits.setHint("Units");
        layout.addView(inputUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Product product = new Product(inputName.getText().toString(), inputAmount.getText() + " " + inputUnits.getText());
                ingredientList.add(product);
                recyclerAdapterIngredients.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private AlertDialog createModifyProductDialog(Product product){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the ingredient");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setText(product.getName());
        layout.addView(inputName);

        String productAmount = product.getAmount();
        final EditText inputAmount = new EditText(this);
        inputAmount.setText(productAmount.substring(0, productAmount.indexOf(" ")));
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final EditText inputUnits = new EditText(this);
        inputUnits.setText(productAmount.substring(productAmount.indexOf(" ")).trim());
        layout.addView(inputUnits);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ingredient = new Product(inputName.getText().toString(),
                        inputAmount.getText().toString() + " " + inputUnits.getText().toString());
                ingredientList.set(position, ingredient);
                recyclerAdapterIngredients.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}