package com.example.trabajofingrado.controller;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.StepRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AddRecipeActivity extends AppCompatActivity {
    // Fields
    private ArrayList<Product> ingredientList = new ArrayList<>();
    private ArrayList<String> stepList = new ArrayList<>();
    private RecyclerView recyclerViewIngredients;
    private RecyclerView recyclerViewSteps;
    private ProductRecyclerAdapter recyclerAdapterIngredients;
    private StepRecyclerAdapter recyclerAdapterSteps;
    private RecyclerView.ViewHolder viewHolderIngredient;
    private RecyclerView.ViewHolder viewHolderStep;
    private Button btnAddIngredient;
    private Button btnAddStep;
    private int position;
    private Product ingredient;
    private String step;
    private FilePickerDialog dialog;
    private Uri imageUri;
    private ImageView imgRecipeDetailImage;
    private EditText txtRecipeName;

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
                if(!txtRecipeName.getText().toString().isEmpty()){
                    // TODO SAVE THE IMAGE IN STORAGE AND THE RECIPE WITH THE URL OF THE IMAGE IN FIREBASE
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    // TODO SCALE UP TO PNG
                    //String fileName = imageUri.getPath().substring(imageUri.getPath().lastIndexOf("/"),imageUri.toString().length());
                    //Log.d("Image name", fileName);
                    StorageReference recipesImageRef = storageReference.child("recipes/" + txtRecipeName.getText().toString() + ".jpg");

                    imgRecipeDetailImage.setDrawingCacheEnabled(true);
                    imgRecipeDetailImage.buildDrawingCache();
                    Bitmap bitmap = ((BitmapDrawable) imgRecipeDetailImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    // TODO SCALE UP TO PNG
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
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPEPATH);
                                HashMap<String, String> ingredients = new HashMap<>();
                                for (Product product : recyclerAdapterIngredients.getProductList()) {
                                    ingredients.put(product.getName(),product.getAmount());
                                }

                                ArrayList<String> steps = (ArrayList<String>) recyclerAdapterSteps.getStepList();

                                Recipe recipe = new Recipe(
                                        txtRecipeName.getText().toString(),
                                        String.valueOf(downloadUri),
                                        getIntent().getStringExtra("username"),
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
                getMenuInflater().inflate(R.menu.add_recipe_image, menu);
                break;
            case R.id.rvRecipeDetailIngredients:
                getMenuInflater().inflate(R.menu.modify_ingredient_menu, menu);
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
            case R.id.modifyIngredient:
                createModifyIngredientDialog().show();
                break;
            case R.id.deleteIngrdient:
                ingredientList.remove(ingredient);
                break;
            case R.id.modifyStep:
                createModifyStepDialog().show();
                break;
            case R.id.deleteStep:
                stepList.remove(step);
                break;
        }
        recyclerAdapterIngredients.notifyDataSetChanged();

        return true;
    }

    // Private methods
    private void setRecyclerViewsAndAdapters() {
        this.btnAddIngredient = findViewById(R.id.btnRecipeDetailAddIngredient);
        this.btnAddStep = findViewById(R.id.btnRecipeDetailAddStep);
        this.recyclerViewIngredients = findViewById(R.id.rvRecipeDetailIngredients);
        this.recyclerViewSteps = findViewById(R.id.rvRecipeDetailSteps);
        this.recyclerAdapterIngredients = new ProductRecyclerAdapter(ingredientList);
        this.recyclerAdapterSteps = new StepRecyclerAdapter(stepList);

        LinearLayoutManager layoutManagerIngredients = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerSteps = new LinearLayoutManager(this);
        this.recyclerViewIngredients.setAdapter(recyclerAdapterIngredients);
        this.recyclerViewSteps.setAdapter(recyclerAdapterSteps);
        this.recyclerViewIngredients.setLayoutManager(layoutManagerIngredients);
        this.recyclerViewSteps.setLayoutManager(layoutManagerSteps);
    }

    private void setListener() {
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
                setIngredient(view);
                createModifyIngredientDialog().show();
            }
        });

        this.recyclerAdapterSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStep(view);
                createModifyStepDialog().show();
            }
        });

        this.recyclerAdapterIngredients.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setIngredient(view);
                registerForContextMenu(recyclerViewIngredients);
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
        // TODO COMMENT IN ENGLISH<
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

    private void setIngredient(View view){
        viewHolderIngredient = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderIngredient.getAdapterPosition();
        ingredient = ingredientList.get(position);
    }

    private void setStep(View view){
        viewHolderStep = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderStep.getAdapterPosition();
        step = stepList.get(position);
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

    private AlertDialog createModifyIngredientDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Modify the ingredient");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setText(ingredient.getName());
        layout.addView(inputName);

        String productAmount = ingredient.getAmount();
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