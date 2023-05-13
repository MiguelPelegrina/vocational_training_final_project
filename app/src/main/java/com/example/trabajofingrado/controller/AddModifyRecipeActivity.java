package com.example.trabajofingrado.controller;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.util.Log;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use case of adding new recipes or modifying existing ones.
 */
public class AddModifyRecipeActivity extends BaseActivity{
    // Fields
    private static final int PRODUCT_CHOICE_REQUEST_CODE = 1;
    private ArrayList<StorageProduct> productList = new ArrayList<>();
    private ArrayList<String> stepList = new ArrayList<>();
    private RecyclerView rvProducts;
    private RecyclerView rvSteps;
    private StorageProductRecyclerAdapter raProducts;
    private StepRecyclerAdapter raSteps;
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

    private String productName;
    private String productUnitType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_modify_recipe);

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout(R.id.nav_recipe_list);

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        this.setFileChooserDialog();

        // Configure the listener
        this.setListener();

        // Set the data, if the a existing recipe shall be modified
        if(getIntent().getStringExtra("action").equals("modify")){
            setData();
        }
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
                checkValidData();
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
                createModifyProductAmountDialog(product.getUnitType()).show();
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
                raSteps.notifyItemRemoved(position);
                break;
        }
        raProducts.notifyDataSetChanged();

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_CHOICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                productName = data.getStringExtra("name");
                productUnitType = data.getStringExtra("unitType");

                switch(data.getStringExtra("action")){
                    case "add":
                        createAddAmountDialog(productName, productUnitType).show();
                        break;
                    case "modify":
                        product.setName(productName);
                        raProducts.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    // Private methods
    private void setRecyclerView() {
        // Instance the adapter
        this.raProducts = new StorageProductRecyclerAdapter(productList);
        this.raSteps = new StepRecyclerAdapter(stepList);

        // Instance the layoutmanager
        LinearLayoutManager layoutManagerIngredients = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerSteps = new LinearLayoutManager(this);

        // Configure the recycler view
        this.rvProducts.setAdapter(raProducts);
        this.rvSteps.setAdapter(raSteps);
        this.rvProducts.setLayoutManager(layoutManagerIngredients);
        this.rvSteps.setLayoutManager(layoutManagerSteps);
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.btnAddProduct = findViewById(R.id.btnRecipeDetailAddIngredient);
        this.btnAddStep = findViewById(R.id.btnRecipeDetailAddStep);
        this.rvProducts = findViewById(R.id.rvRecipeDetailIngredients);
        this.rvSteps = findViewById(R.id.rvRecipeDetailSteps);
        this.txtRecipeName = findViewById(R.id.etRecipeDetailName);
        this.drawerLayout = findViewById(R.id.drawer_layout_add_modify_recipe);
        this.toolbar = findViewById(R.id.toolbar_add_modfiy_recipe);
        this.imgRecipeDetailImage = findViewById(R.id.imgRecipeDetailAddImage);
        registerForContextMenu(imgRecipeDetailImage);
    }

    private void setListener() {
        this.btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddModifyRecipeActivity.this, AddProductActivity.class);
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
                modifyProductName().show();
            }
        });*/

        this.raSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStep(view);
                createModifyStepDialog().show();
            }
        });

        this.raProducts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setProduct(view);
                registerForContextMenu(rvProducts);
                return false;
            }
        });

        this.raSteps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setStep(view);
                registerForContextMenu(rvSteps);
                return false;
            }
        });

        /*imgRecipeDetailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/

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
        dialog = new FilePickerDialog(AddModifyRecipeActivity.this, properties);
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

    private void setData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.RECIPE_PATH);
        Query query = database.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        txtRecipeName.setText(recipe.getName());

                        Glide.with(AddModifyRecipeActivity.this)
                                .load(recipe.getImage())
                                .error(R.drawable.image_not_found)
                                .into(imgRecipeDetailImage);

                        // TODO
                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            StorageProduct storageProduct = ingredient.getValue();
                            productList.add(
                                    new StorageProduct(
                                            storageProduct.getAmount(),
                                            storageProduct.getName(),
                                            storageProduct.getUnitType()
                                    )
                            );
                        }

                        stepList.addAll(recipe.getSteps());

                        raProducts.notifyDataSetChanged();
                        raSteps.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    // TODO DIFFERENTIATE BETWEEN PUSH AND UPDATE
    private void checkValidData(){
        if(!txtRecipeName.getText().toString().trim().isEmpty() && !stepList.isEmpty() && !productList.isEmpty()){
            if(getIntent().getStringExtra("action").equals("modify")){
                createAlertDialog().show();
            }else{
                saveRecipe();
            }
        }else{
            Toasty.error(AddModifyRecipeActivity.this,
                    "Introduce valid data: a name, atleast a product and a step.",
                    Toasty.LENGTH_LONG).show();
        }
    }

    private void saveRecipe(){
        // Get the storage reference that saves the recipe images
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Create a UUID for the image
        UUID imageUUID = UUID.randomUUID();

        // Get the storage
        StorageReference recipesImageRef = storageReference.child("recipes/" + imageUUID + ".jpg");

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

                    HashMap<String, StorageProduct> ingredients = new HashMap<>();
                    for (StorageProduct product : raProducts.getProductList()) {
                        ingredients.put(product.getName(),
                                new StorageProduct(
                                        product.getAmount(),
                                        product.getName(),
                                        product.getUnitType())
                        );
                    }

                    ArrayList<String> steps = (ArrayList<String>) raSteps.getStepList();

                    Recipe recipe = new Recipe(
                            txtRecipeName.getText().toString(),
                            String.valueOf(downloadUri),
                            FirebaseAuth.getInstance().getUid(),
                            ingredients,
                            steps,
                            getIntent().getStringExtra("recipeId")
                    );

                    if (getIntent().getStringExtra("action").equals("add")) {
                        recipe.setId(UUID.randomUUID().toString());
                        database.child(recipe.getId()).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toasty.success(AddModifyRecipeActivity.this,
                                        "The recipe was added successfully",
                                        Toasty.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(recipe.getId(), recipe);
                        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toasty.success(AddModifyRecipeActivity.this,
                                        "The recipe was modified successfully",
                                        Toasty.LENGTH_LONG).show();
                            }
                        });
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toasty.error(AddModifyRecipeActivity.this,
                            "The recipe could not be saved", Toasty.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(AddModifyRecipeActivity.this,
                        "The image could not get uploaded.", Toasty.LENGTH_LONG).show();
            }
        });
    }

    private AlertDialog createAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("You might lose data").setMessage("Are you sure you want to change the recipe?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveRecipe();
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
                Glide.with(AddModifyRecipeActivity.this)
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
                if(Utils.checkValidString(inputStep.getText().toString())){
                    stepList.add(inputStep.getText().toString());
                    raSteps.notifyDataSetChanged();
                }else{
                    Toasty.error(AddModifyRecipeActivity.this, "The step cannot be empty").show();
                }
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

    private AlertDialog createAddAmountDialog(String name, String unitType){
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
                if(Utils.checkValidString(inputAmount.getText().toString())) {

                    StorageProduct product = new StorageProduct(
                            Integer.parseInt(inputAmount.getText().toString()),
                            name,
                            unitType
                    );
                    productList.add(product);
                    raProducts.notifyDataSetChanged();
                }else {
                    Toasty.error(AddModifyRecipeActivity.this,
                            "You need to enter a valid amount").show();
                }
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
                product.setAmount(Integer.parseInt(inputAmount.getText().toString()));
                raProducts.notifyDataSetChanged();
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
                raSteps.notifyDataSetChanged();
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
        Intent intent = new Intent(AddModifyRecipeActivity.this, AddProductActivity.class);
        intent.putExtra("action", "modify");
        startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
    }
}