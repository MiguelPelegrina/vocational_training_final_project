package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.*;
import static com.example.trabajofingrado.R.id.*;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.RecipeStepRecyclerAdapter;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use case of adding new recipes or modifying existing ones.
 */
public class AddModifyRecipeActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int PRODUCT_CHOICE_REQUEST_CODE = 1;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 2;
    private static final int REQUEST_GALLERY_PERMISSION_CODE = 3;
    private static final int TAKE_PHOTO_CODE = 4;
    private static final int OPEN_GALLERY_CODE = 5;

    // Of instance
    private int position;
    private ArrayList<StorageProduct> productList = new ArrayList<>();
    private ArrayList<String> stepList = new ArrayList<>();
    private Bitmap bitmap;
    private Button btnAddProduct, btnAddStep;
    private EditText txtRecipeName;
    private ImageView imgRecipeDetailImage;
    private RecyclerView rvProducts, rvSteps;
    private RecyclerView.ViewHolder viewHolderIngredient, viewHolderStep;
    private RecipeStepRecyclerAdapter raSteps;
    private StorageProduct product;
    private ProductRecyclerAdapter raProducts;
    private String productName, productUnitType, step;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_add_modify_recipe);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(nav_recipe_list);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();

        // Set the data, if the a existing recipe shall be modified
        if (getIntent().getStringExtra("action").equals("modify")) {
            setData();
            setTitle("Modify recipe");
        } else {
            setTitle("Create recipe");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_recipe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == menu_item_save_recipe) {
            checkValidData();
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()) {
            case imgRecipeDetailAddImage:
                getMenuInflater().inflate(R.menu.add_recipe_image_menu, menu);
                break;
            case rvRecipeDetailIngredients:
                getMenuInflater().inflate(R.menu.modify_recipe_product_menu, menu);
                break;
            case rvRecipeDetailSteps:
                getMenuInflater().inflate(R.menu.modify_step_menu, menu);
                break;
        }

        menu.setHeaderTitle("Select an option");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case chooseFile:
                checkStoragePermissions();
                break;
            case introduceLink:
                createAddImageInputDialog().show();
                break;
            case takePhoto:
                checkCameraPermissions();
                break;
            case menu_item_modify_recipe_product_name:
                modifyProductName();
                raProducts.notifyDataSetChanged();
                break;
            case menu_item_modify_recipe_product_amount:
                createModifyProductAmountDialog(product.getUnitType()).show();
                break;
            case menu_item_delete_recipe_product:
                productList.remove(product);
                raProducts.notifyItemRemoved(position);
                break;
            case modifyStep:
                createModifyStepDialog().show();
                break;
            case deleteStep:
                stepList.remove(step);
                raSteps.notifyItemRemoved(position);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PRODUCT_CHOICE_REQUEST_CODE:
                    if (data != null) {
                        productName = data.getStringExtra("name");
                        productUnitType = data.getStringExtra("unitType");

                        switch (data.getStringExtra("action")) {
                            case "add":
                                createAddAmountDialog(productName, productUnitType).show();
                                break;
                            case "modify":
                                product.setName(productName);
                                raProducts.notifyDataSetChanged();
                                break;
                        }
                        break;
                    }
                case TAKE_PHOTO_CODE:
                    if (data != null) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                        imgRecipeDetailImage.setImageBitmap(bitmap);
                    }
                    break;
                case OPEN_GALLERY_CODE:
                    if (data != null) {
                        Glide.with(this)
                                .load(data.getData())
                                .apply(new RequestOptions().centerCrop())
                                .into(imgRecipeDetailImage);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toasty.error(AddModifyRecipeActivity.this, "No camera permissions were granted").show();
                }
                break;
            case REQUEST_GALLERY_PERMISSION_CODE:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toasty.error(AddModifyRecipeActivity.this, "No storage permissions were granted").show();
                }
        }

    }

    // Private methods
    private void checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
        }
    }

    private void checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_GALLERY_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, OPEN_GALLERY_CODE);
    }

    private void setRecyclerView() {
        // Instance the adapter
        raProducts = new ProductRecyclerAdapter(productList);
        raSteps = new RecipeStepRecyclerAdapter(stepList);

        // Instance the layout manager
        LinearLayoutManager layoutManagerIngredients = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerSteps = new LinearLayoutManager(this);

        // Configure the recycler view
        rvProducts.setAdapter(raProducts);
        rvSteps.setAdapter(raSteps);
        rvProducts.setLayoutManager(layoutManagerIngredients);
        rvSteps.setLayoutManager(layoutManagerSteps);
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddProduct = findViewById(btnRecipeDetailAddIngredient);
        btnAddStep = findViewById(btnRecipeDetailAddStep);
        rvProducts = findViewById(rvRecipeDetailIngredients);
        rvSteps = findViewById(rvRecipeDetailSteps);
        txtRecipeName = findViewById(etRecipeDetailName);
        drawerLayout = findViewById(drawer_layout_add_modify_recipe);
        toolbar = findViewById(toolbar_add_modfiy_recipe);
        imgRecipeDetailImage = findViewById(imgRecipeDetailAddImage);
        // Allows to round the borders of the image view
        imgRecipeDetailImage.setClipToOutline(true);

    }

    private void setListener() {
        registerForContextMenu(imgRecipeDetailImage);
        registerForContextMenu(rvProducts);
        registerForContextMenu(rvSteps);

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddModifyRecipeActivity.this, AddProductActivity.class);
                intent.putExtra("action", "add");
                startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
            }
        });

        btnAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddStepDialog().show();
            }
        });

        raSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStep(view);
                createModifyStepDialog().show();
            }
        });

        raProducts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setProduct(view);

                return false;
            }
        });

        raSteps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setStep(view);

                return false;
            }
        });
    }

    private void setProduct(View view) {
        viewHolderIngredient = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderIngredient.getAdapterPosition();
        product = productList.get(position);
    }

    private void setStep(View view) {
        viewHolderStep = (RecyclerView.ViewHolder) view.getTag();
        position = viewHolderStep.getAdapterPosition();
        step = stepList.get(position);
    }

    private void setData() {
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        txtRecipeName.setText(recipe.getName());

                        Glide.with(AddModifyRecipeActivity.this)
                                .load(recipe.getImage())
                                .error(drawable.image_not_found)
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
                Utils.connectionError(AddModifyRecipeActivity.this);
            }
        });
    }

    // TODO DIFFERENTIATE BETWEEN PUSH AND UPDATE
    private void checkValidData() {
        if (!txtRecipeName.getText().toString().trim().isEmpty() && !stepList.isEmpty() && !productList.isEmpty()) {
            if (getIntent().getStringExtra("action").equals("modify")) {
                createAlertDialog().show();
            } else {
                saveRecipe();
            }
        } else {
            Toasty.error(AddModifyRecipeActivity.this,
                    "Introduce valid data: a name, at least a product and a step.",
                    Toasty.LENGTH_LONG).show();
        }
    }

    private void saveRecipe() {
        // Get the storage reference that saves the recipe images
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Create a UUID for the image
        UUID imageUUID = UUID.randomUUID();

        // Get the storage
        StorageReference recipesImageRef = storageReference.child("recipes/" + imageUUID + ".jpg");

        imgRecipeDetailImage.setDrawingCacheEnabled(true);
        imgRecipeDetailImage.buildDrawingCache();
        bitmap = ((BitmapDrawable) imgRecipeDetailImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = recipesImageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return recipesImageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

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
                        RECIPE_REFERENCE.child(recipe.getId()).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        RECIPE_REFERENCE.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private AlertDialog createAlertDialog() {
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
    private AlertDialog createAddImageInputDialog() {
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
                        .error(drawable.image_not_found)
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

    private AlertDialog createAddStepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Introduce a step");

        final EditText inputStep = new EditText(this);
        inputStep.setHint("Step");

        builder.setView(inputStep);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utils.checkValidString(inputStep.getText().toString())) {
                    stepList.add(inputStep.getText().toString());
                    raSteps.notifyItemInserted(raSteps.getItemCount());
                } else {
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

    private AlertDialog createAddAmountDialog(String name, String unitType) {
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
                if (Utils.checkValidString(inputAmount.getText().toString())) {

                    StorageProduct product = new StorageProduct(
                            Integer.parseInt(inputAmount.getText().toString()),
                            name,
                            unitType
                    );
                    productList.add(product);
                    raProducts.notifyDataSetChanged();
                } else {
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

    private AlertDialog createModifyProductAmountDialog(String unitType) {
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
                raProducts.notifyItemChanged(position);
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

    private AlertDialog createModifyStepDialog() {
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
                raSteps.notifyItemChanged(position);
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
    private void modifyProductName() {
        Intent intent = new Intent(AddModifyRecipeActivity.this, AddProductActivity.class);
        intent.putExtra("action", "modify");
        startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
    }
}