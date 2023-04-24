package com.example.trabajofingrado.controller;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.StepRecyclerAdapter;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class AddRecipeActivity extends AppCompatActivity {
    // Fields
    private static final int PRODUCT_CHOICE_REQUEST_CODE = 1;
    private ArrayList<StorageProduct> productList = new ArrayList<>();
    private ArrayList<String> stepList = new ArrayList<>();
    private RecyclerView recyclerViewProducts;
    private RecyclerView recyclerViewSteps;
    private StorageProductRecyclerAdapter recyclerAdapterProducts;
    private StepRecyclerAdapter recyclerAdapterSteps;
    private RecyclerView.ViewHolder viewHolderIngredient;
    private RecyclerView.ViewHolder viewHolderStep;
    private Button btnAddProduct;
    private Button btnAddStep;
    private int position;
    private StorageProduct product;
    private String step;
    private FilePickerDialog dialog;
    private Uri imageUri;
    private ImageView imgRecipeDetailImage;
    private EditText txtRecipeName;

    private String productDescription;
    private String productUnitType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        txtRecipeName = findViewById(R.id.etRecipeDetailName);
        imgRecipeDetailImage = findViewById(R.id.imgRecipeDetailAddImage);
        registerForContextMenu(imgRecipeDetailImage);

        setRecyclerViewsAndAdapters();

        setFileChooserDialog();

        setListener();
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
                saveRecipe();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()){
            case R.id.imgRecipeDetailAddImage:
                getMenuInflater().inflate(R.menu.add_recipe_image_menu, menu);
                break;
            case R.id.rvRecipeDetailIngredients:
                getMenuInflater().inflate(R.menu.modify_recipe_product_menu, menu);
                break;
            case R.id.rvRecipeDetailSteps:
                getMenuInflater().inflate(R.menu.modify_step_menu,menu);
                break;
        }

        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.chooseFile:
                dialog.show();
                break;
            case R.id.introduceLink:
                createAddImageInputDialog().show();
                break;
            case R.id.takePhoto:

                break;
                // TODO
            case R.id.menu_item_modify_recipe_product_name:
                modifyProductName();
                break;
            case R.id.menu_item_modify_recipe_product_amount:
                String amount = product.getAmount().substring(product.getAmount().indexOf(" "), product.getAmount().length());
                createModifyProductAmountDialog(amount).show();
                break;
                // TODO
            case R.id.menu_item_delete_recipe_product:
                productList.remove(product);
                break;
            case R.id.modifyStep:
                createModifyStepDialog().show();
                break;
            case R.id.deleteStep:
                stepList.remove(step);
                break;
        }
        recyclerAdapterProducts.notifyDataSetChanged();

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_CHOICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                productDescription = data.getStringExtra("description");
                productUnitType = data.getStringExtra("unitType");

                switch(data.getStringExtra("action")){
                    case "add":
                        createAddAmountDialog(productDescription, productUnitType).show();
                        break;
                    case "modify":
                        product.setDescription(productDescription);
                        recyclerAdapterProducts.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    // Private methods
    private void setRecyclerViewsAndAdapters() {
        this.btnAddProduct = findViewById(R.id.btnRecipeDetailAddIngredient);
        this.btnAddStep = findViewById(R.id.btnRecipeDetailAddStep);
        this.recyclerViewProducts = findViewById(R.id.rvRecipeDetailIngredients);
        this.recyclerViewSteps = findViewById(R.id.rvRecipeDetailSteps);
        this.recyclerAdapterProducts = new StorageProductRecyclerAdapter(productList);
        this.recyclerAdapterSteps = new StepRecyclerAdapter(stepList);

        LinearLayoutManager layoutManagerIngredients = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerSteps = new LinearLayoutManager(this);
        this.recyclerViewProducts.setAdapter(recyclerAdapterProducts);
        this.recyclerViewSteps.setAdapter(recyclerAdapterSteps);
        this.recyclerViewProducts.setLayoutManager(layoutManagerIngredients);
        this.recyclerViewSteps.setLayoutManager(layoutManagerSteps);
    }

    private void setListener() {
        this.btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddRecipeActivity.this, AddRecipeProductActivity.class);
                intent.putExtra("action", "add");
                startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
            }
        });

        this.btnAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddStepDialog().show();
            }
        });

        /*this.recyclerAdapterProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProduct(view);
                // TODO GET A CONTEXT MENU THAT LETS YOU CHOOSE BETWEEN MODIFYING OR DELETING
                modifyProductName().show();
            }
        });*/

        this.recyclerAdapterSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStep(view);
                createModifyStepDialog().show();
            }
        });

        this.recyclerAdapterProducts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setProduct(view);
                registerForContextMenu(recyclerViewProducts);
                return false;
            }
        });

        this.recyclerAdapterSteps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setStep(view);
                registerForContextMenu(recyclerViewSteps);
                return false;
            }
        });

        imgRecipeDetailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                // TODO COMMENT IN ENGLISH
                // Cuando se elige un fichero obtenemos su uri local
                imageUri = Uri.fromFile(new File(files[0]));
                // Asignamos la uri al imageView
                imgRecipeDetailImage.setImageURI(imageUri);
            }
        });
    }

    private void setFileChooserDialog() {
        // TODO COMMENT IN ENGLISH
        // Esta parte del código corresponde a la biblioteca FilePicker. Esta nos permite elegir
        // ficheros. En este caso en concreto nos permite añadir o modificar la imagen del personaje
        // Nos creamos un objeto de la clase DialogProperties
        DialogProperties properties = new DialogProperties();
        // Configuramos las variables de dicho objeto
        // El modo de selección será de un único fichero
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        // Solo se podrán elegir ficheros
        properties.selection_type = DialogConfigs.FILE_SELECT;
        // Obtenemos el directorio de la sdExterna que guarda los datos del usuario
        File sdExterna = new File(Environment.getExternalStorageDirectory().getPath());
        // Establecemos como directorios la ruta de la sdExterna
        properties.root = sdExterna;
        properties.error_dir = sdExterna;
        properties.offset = sdExterna;
        // Establecemos las extensiones permitidas
        properties.extensions = new String[]{"jpg","jpeg","png"};
        // Nos creamos un objeto de la ventana de dialogo
        dialog = new FilePickerDialog(AddRecipeActivity.this, properties);
        // Modificamos su título
        dialog.setTitle("Eliga una imagen");
    }

    private void setProduct(View view){
        viewHolderIngredient = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderIngredient.getAdapterPosition();
        product = productList.get(position);
    }

    private void setStep(View view){
        viewHolderStep = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderStep.getAdapterPosition();
        step = stepList.get(position);
    }

    private void saveRecipe(){
        if(!txtRecipeName.getText().toString().isEmpty()){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            StorageReference recipesImageRef = storageReference.child("recipes/" + txtRecipeName.getText().toString() + ".jpg");

            imgRecipeDetailImage.setDrawingCacheEnabled(true);
            imgRecipeDetailImage.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imgRecipeDetailImage.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = recipesImageRef.putBytes(data);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return recipesImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
                        HashMap<String, String> ingredients = new HashMap<>();
                        for (StorageProduct product : recyclerAdapterProducts.getProductList()) {
                            ingredients.put(product.getDescription(),product.getAmount());
                        }

                        ArrayList<String> steps = (ArrayList<String>) recyclerAdapterSteps.getStepList();

                        Recipe recipe = new Recipe(
                                txtRecipeName.getText().toString(),
                                String.valueOf(downloadUri),
                                FirebaseAuth.getInstance().getUid(),
                                ingredients,
                                steps
                        );
                        database.push().setValue(recipe);
                        Toasty.success(AddRecipeActivity.this,
                                "The recipe was added successfully", Toasty.LENGTH_LONG).show();
                    } else {
                        Toasty.error(AddRecipeActivity.this,
                                "The recipe could not be saved", Toasty.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(AddRecipeActivity.this,
                            "The image could not get uploaded.", Toasty.LENGTH_LONG).show();
                }
            });
        }else{
            Toasty.error(AddRecipeActivity.this,
                    "Give the recipe a name before attempting to save it",
                    Toasty.LENGTH_LONG).show();
        }
    }

    // TODO SPLIT THEM INTO CLASSES
    // Input dialogs
    private AlertDialog createAddImageInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce the path of the imagen");

        final EditText inputImage = new EditText(this);

        inputImage.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(inputImage);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Parseamos el String a una uri
                imageUri = Uri.parse(inputImage.getText().toString());
                // Cargamos la imagen
                Glide.with(AddRecipeActivity.this)
                        .load(imageUri)
                        .error(R.drawable.image_not_found)
                        .into(imgRecipeDetailImage);
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

    private AlertDialog createAddStepDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce a step");

        final EditText inputStep = new EditText(this);
        inputStep.setHint("Step");

        builder.setView(inputStep);

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

    private AlertDialog createAddAmountDialog(String description, String units){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("How much/many " + units + " will you use?");

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);

        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StorageProduct product = new StorageProduct(description, inputAmount.getText().toString() + " " + units);
                productList.add(product);
                recyclerAdapterProducts.notifyDataSetChanged();
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

    private AlertDialog createModifyProductAmountDialog(String unitType){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("How much/many " + unitType + " will you use?");

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);

        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                product.setAmount(inputAmount.getText().toString() + " " + unitType);
                recyclerAdapterProducts.notifyDataSetChanged();
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

    private AlertDialog createModifyStepDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the step");

        final EditText inputStep = new EditText(this);
        inputStep.setText(step);

        builder.setView(inputStep);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                step = inputStep.getText().toString();
                stepList.set(position, step);
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

    // TODO
    private void modifyProductName(){
        Intent intent = new Intent(AddRecipeActivity.this, AddRecipeProductActivity.class);
        intent.putExtra("action", "modify");
        startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
    }
}