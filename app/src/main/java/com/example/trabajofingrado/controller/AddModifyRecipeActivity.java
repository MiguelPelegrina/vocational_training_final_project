package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.*;
import static com.example.trabajofingrado.R.id.*;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.checkValidString;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.StorageProductRecyclerAdapter;
import com.example.trabajofingrado.adapter.RecipeStepRecyclerAdapter;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.utilities.Utils;
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
    private final ArrayList<StorageProduct> productList = new ArrayList<>();
    private final ArrayList<String> stepList = new ArrayList<>();
    private Bitmap bitmap;
    private Button btnAddProduct, btnAddStep;
    private EditText txtRecipeName;
    private ImageView imgRecipeDetailImage;
    private MenuItem saveRecipeMenuItem;
    private Recipe originalRecipe;
    private RecyclerView rvProducts, rvSteps;
    private RecyclerView.ViewHolder viewHolderIngredient, viewHolderStep;
    private RecipeStepRecyclerAdapter raSteps;
    private ProgressBar progressBar;
    private StorageProduct product;
    private StorageProductRecyclerAdapter raProducts;
    private String productName, productUnitType, step;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_add_modify_recipe);

        bindViews();

        setDrawerLayout(nav_recipe_list);

        setRecyclerView();

        setListener();

        // Check if the user wants to modify an existing recipe or create a new one
        if (getIntent().getStringExtra("action").equals("modify")) {
            // Sets the data of an existing recipe
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
            // Set the selected menu item
            saveRecipeMenuItem = item;
            checkValidData();
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Check which view was selected
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
        // Check which view was selected
        switch (item.getItemId()) {
            case chooseFile:
                // Check the storage permission to access an image file
                checkStoragePermissions();
                break;
            case introduceLink:
                // Create a dialog to introduce a link as image
                createAddImageInputDialog().show();
                break;
            case takePhoto:
                // Check the camera permissions to use that photo as recipe image
                checkCameraPermissions();
                break;
            case menu_item_modify_recipe_product_name:
                // Modify the product name
                modifyProductName();
                raProducts.notifyDataSetChanged();
                break;
            case menu_item_modify_recipe_product_amount:
                // Modify the product amount
                createModifyProductAmountDialog(product.getUnitType()).show();
                break;
            case menu_item_delete_recipe_product:
                // Remove a product
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

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                // If a product was chosen
                case PRODUCT_CHOICE_REQUEST_CODE:
                    // Get the data from the intent
                    productName = data.getStringExtra("name");
                    productUnitType = data.getStringExtra("unitType");

                    switch (data.getStringExtra("action")) {
                        case "add":
                            // Get the amount of the product
                            createAddAmountDialog(productName, productUnitType).show();
                            break;
                        case "modify":
                            // Modify the name of the product
                            product.setName(productName);
                            raProducts.notifyDataSetChanged();
                            break;
                    }
                    break;
                // If a photo was taken
                case TAKE_PHOTO_CODE:
                    // Load the image
                    bitmap = (Bitmap) data.getExtras().get("data");
                    imgRecipeDetailImage.setImageBitmap(bitmap);
                    break;
                // If an image was selected
                case OPEN_GALLERY_CODE:
                    // Load the image
                    Glide.with(this)
                            .load(data.getData())
                            .apply(new RequestOptions().centerCrop())
                            .into(imgRecipeDetailImage);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                // Check the camera permissions
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    // Inform the user
                    Toasty.error(AddModifyRecipeActivity.this, "No camera permissions were granted").show();
                }
                break;
            case REQUEST_GALLERY_PERMISSION_CODE:
                // Check the storage permissions
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    // Inform the user
                    Toasty.error(AddModifyRecipeActivity.this, "No storage permissions were granted").show();
                }
        }
    }

    // Private methods
    /**
     * Asks the user about the camera permissions
     */
    private void checkCameraPermissions() {
        // Check if the permissions were already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
        }
    }

    /**
     * Asks the user about the storage permissions
     */
    private void checkStoragePermissions() {
        // Check if the permissions were already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            // Checks the android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_GALLERY_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    /**
     * Moves to the photo application chosen by the user
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    /**
     * Moves to the gallery application chosen by the user
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, OPEN_GALLERY_CODE);
    }

    /**
     * Sets the recycler views
     */
    private void setRecyclerView() {
        // Instance the adapter
        raProducts = new StorageProductRecyclerAdapter(productList);
        raSteps = new RecipeStepRecyclerAdapter(stepList);

        // Set the recycler adapter
        rvProducts.setAdapter(raProducts);
        rvSteps.setAdapter(raSteps);

        // Instance the layout manager
        LinearLayoutManager layoutManagerIngredients = new LinearLayoutManager(this);
        LinearLayoutManager layoutManagerSteps = new LinearLayoutManager(this);

        // Set the layout manager
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
        drawerLayout = findViewById(drawer_layout_add_modify_recipe);
        imgRecipeDetailImage = findViewById(imgRecipeDetailAddImage);
        // Allows to round the borders of the image view
        imgRecipeDetailImage.setClipToOutline(true);
        progressBar = findViewById(progressBarRecipe);
        rvProducts = findViewById(rvRecipeDetailIngredients);
        rvSteps = findViewById(rvRecipeDetailSteps);
        toolbar = findViewById(toolbar_add_modify_recipe);
        txtRecipeName = findViewById(etRecipeDetailName);
    }

    /**
     * Sets the listener of all the views
     */
    private void setListener() {
        registerForContextMenu(imgRecipeDetailImage);
        registerForContextMenu(rvProducts);
        registerForContextMenu(rvSteps);

        btnAddProduct.setOnClickListener(view -> {
            // Moves to the product list activity
            Intent intent = new Intent(AddModifyRecipeActivity.this, ShowProductListActivity.class);
            intent.putExtra("action", "add");
            startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
        });

        btnAddStep.setOnClickListener(view -> createAddStepDialog().show());

        raSteps.setOnClickListener(view -> {
            setStep(view);

            createModifyStepDialog().show();
        });

        raProducts.setOnLongClickListener(view -> {
            setProduct(view);

            return false;
        });

        raSteps.setOnLongClickListener(view -> {
            setStep(view);

            return false;
        });
    }

    /**
     * Sets the product
     */
    private void setProduct(View view) {
        // Get the holder
        viewHolderIngredient = (RecyclerView.ViewHolder) view.getTag();
        // Get the position
        position = viewHolderIngredient.getAdapterPosition();
        // Get selected the product
        product = productList.get(position);
    }

    /**
     * Sets the step
     */
    private void setStep(View view) {
        // Get the holder
        viewHolderStep = (RecyclerView.ViewHolder) view.getTag();
        // Get the position
        position = viewHolderStep.getAdapterPosition();
        // Get the selected step
        step = stepList.get(position);
    }

    /**
     * Loads the data of the recipe from the database
     */
    private void setData() {
        // Search the recipe in the database
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // TODO Check if the recipe was changed or not before saving
                    // Get the recipe
                    originalRecipe = ds.getValue(Recipe.class);

                    // Check if the recipe was found
                    if (originalRecipe != null) {
                        // Set the data of the recipe into the view
                        txtRecipeName.setText(originalRecipe.getName());

                        Glide.with(AddModifyRecipeActivity.this)
                                .load(originalRecipe.getImage())
                                .error(drawable.image_not_found)
                                .into(imgRecipeDetailImage);

                        // Loop through the ingredients
                        for (Map.Entry<String, StorageProduct> ingredient : originalRecipe.getIngredients().entrySet()) {
                            // Get each product
                            StorageProduct storageProduct = ingredient.getValue();

                            // Set each product
                            productList.add(new StorageProduct(
                                            storageProduct.getAmount(),
                                            storageProduct.getName(),
                                            storageProduct.getUnitType()
                                    )
                            );
                        }

                        // Set all steps
                        stepList.addAll(originalRecipe.getSteps());

                        //Notify the recycler adapter
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

    private void checkValidData() {
        // Check if the recipe has name, products and steps
        if (!txtRecipeName.getText().toString().trim().isEmpty() && !stepList.isEmpty() && !productList.isEmpty()) {
            if (getIntent().getStringExtra("action").equals("modify")) {
                // Show
                createAlertDialog().show();
            } else {
                saveRecipe();
            }
        } else {
            // Inform the user
            Toasty.error(AddModifyRecipeActivity.this,
                    "Introduce valid data: a name, at least a product and a step.",
                    Toasty.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the recipe in the database
     */
    private void saveRecipe() {
        // Shows the progressbar
        progressBar.setVisibility(View.VISIBLE);

        // Hidesthe menu item
        saveRecipeMenuItem.setVisible(false);

        // Get the storage reference that saves the recipe images
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Create a UUID for the image
        UUID imageUUID = UUID.randomUUID();

        // Get the storage
        StorageReference recipesImageRef = storageReference.child("recipes/" + imageUUID + ".jpg");

        // Get the image
        imgRecipeDetailImage.setDrawingCacheEnabled(true);
        imgRecipeDetailImage.buildDrawingCache();
        bitmap = ((BitmapDrawable) imgRecipeDetailImage.getDrawable()).getBitmap();

        // Upload the image to Firebase Storage
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        // Launch the task
        UploadTask uploadTask = recipesImageRef.putBytes(data);

        // Check success of upload task
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }

            // Continue with the task to get the download URL
            return recipesImageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the uri where the image was saved
                Uri downloadUri = task.getResult();

                // Generate an ingredients map
                HashMap<String, StorageProduct> ingredients = new HashMap<>();

                // Loop through the products
                for (StorageProduct product : raProducts.getProductList()) {
                    // Set the ingredients into the map
                    ingredients.put(product.getName(),
                            new StorageProduct(
                                    product.getAmount(),
                                    product.getName(),
                                    product.getUnitType())
                    );
                }

                // Get the steps
                ArrayList<String> steps = (ArrayList<String>) raSteps.getStepList();

                // Generate the recipe
                Recipe recipe = new Recipe(
                        txtRecipeName.getText().toString(), String.valueOf(downloadUri),
                        FirebaseAuth.getInstance().getUid(), ingredients, steps,
                        getIntent().getStringExtra("recipeId")
                );

                // Check if the user is adding a new recipe or modifying an existing one
                if (getIntent().getStringExtra("action").equals("add")) {
                    // Set the identifier of the recipe
                    recipe.setId(UUID.randomUUID().toString());

                    // Save the recipe
                    RECIPE_REFERENCE.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task1 -> {
                        Toasty.success(AddModifyRecipeActivity.this,
                                "The recipe was added successfully",
                                Toasty.LENGTH_LONG).show();

                        // Change visibility of the views
                        progressBar.setVisibility(View.GONE);
                        saveRecipeMenuItem.setVisible(true);
                    });
                } else {
                    // Generate an updates map
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(recipe.getId(), recipe);
                    // Update the recipe
                    RECIPE_REFERENCE.updateChildren(updates).addOnCompleteListener(task12 -> {
                        // Inform the user
                        Toasty.success(AddModifyRecipeActivity.this,
                                "The recipe was modified successfully",
                                Toasty.LENGTH_LONG).show();

                        // Change visibility of the views
                        progressBar.setVisibility(View.GONE);
                        saveRecipeMenuItem.setVisible(true);
                    });
                }
                setResult(RESULT_OK);
            } else {
                // Inform the user
                Toasty.error(AddModifyRecipeActivity.this,
                        "The recipe could not be saved", Toasty.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e ->
                // Inform the user
                Toasty.error(AddModifyRecipeActivity.this,
                "The image could not get uploaded.", Toasty.LENGTH_LONG).show());

    }

    /**
     * Asks the user confirm their choice of updating the recipe
     */
    private AlertDialog createAlertDialog() {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("You might lose data").setMessage("Are you sure you want to change the recipe?");
        builder.setPositiveButton("Confirm", (dialog, which) -> saveRecipe());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Return the alert dialog
        return builder.create();
    }

    /**
     * Creates an alert dialog to set the link to an image
     */
    private AlertDialog createAddImageInputDialog() {
        // Generate the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Introduce the path of the image");

        // Set an edit text for the link
        final EditText inputImage = new EditText(this);
        inputImage.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputImage);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Parse the string to an URI
            imageUri = Uri.parse(inputImage.getText().toString());
            // Load the image
            Glide.with(AddModifyRecipeActivity.this)
                    .load(imageUri)
                    .error(drawable.image_not_found)
                    .into(imgRecipeDetailImage);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Return the alert dialog
        return builder.create();
    }

    /**
     * Creates an alert dialog to add a step
     */
    private AlertDialog createAddStepDialog() {
        // Generate the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Configure the builder
        builder.setTitle("Introduce a step");

        // Set an edit text for the step
        final EditText inputStep = new EditText(this);
        inputStep.setHint("Step");
        builder.setView(inputStep);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Check if the step if empty
            if (checkValidString(inputStep.getText().toString())) {
                // Add the step to the list
                stepList.add(inputStep.getText().toString());
                raSteps.notifyItemInserted(raSteps.getItemCount());
            } else {
                enterValidData(AddModifyRecipeActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Return the alert dialog
        return builder.create();
    }

    /**
     * Creates an alert dialog to change the amount of product
     * @param name
     * @param unitType
     */
    private AlertDialog createAddAmountDialog(String name, String unitType) {
        // Generate the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("How much/many " + unitType + " will you use?");

        // Set the edit text of the amount
        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Check if the data is valid
            if (checkValidString(inputAmount.getText().toString())) {
                // Generate the product
                StorageProduct product = new StorageProduct(
                        Integer.parseInt(inputAmount.getText().toString()),name,unitType);
                // Add the product
                productList.add(product);
                raProducts.notifyDataSetChanged();
            } else {
                // Inform the user
                enterValidData(AddModifyRecipeActivity.this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Return the alert dialog
        return builder.create();
    }

    /**
     * Creates an alert dialog to modify a product
     *
     * @param unitType
     */
    private AlertDialog createModifyProductAmountDialog(String unitType) {
        // Generate the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("How much/many " + unitType + " will you use?");

        // Set the edit text to get the amount of the product
        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        builder.setView(inputAmount);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Check if the data is valid
            if (checkValidString(inputAmount.getText().toString())) {
                // Modify the amount
                product.setAmount(Integer.parseInt(inputAmount.getText().toString()));
                raProducts.notifyItemChanged(position);
            } else {
                // Inform the user
                enterValidData(AddModifyRecipeActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Creates an alert dialog to modify a step
     */
    private AlertDialog createModifyStepDialog() {
        // Generate a alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Modify the step");

        // Set an edit text to introduce a step
        final EditText inputStep = new EditText(this);
        inputStep.setText(step);
        builder.setView(inputStep);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the data is valid
            if (checkValidString(inputStep.getText().toString())) {
                // Modify the step to the step list
                step = inputStep.getText().toString();
                stepList.set(position, step);
                raSteps.notifyItemChanged(position);
            } else {
                // Inform the user
                enterValidData(AddModifyRecipeActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Return the alert dialog
        return builder.create();
    }

    /**
     * Moves to the product list activity to choose another product
     */
    private void modifyProductName() {
        Intent intent = new Intent(AddModifyRecipeActivity.this, ShowProductListActivity.class);
        intent.putExtra("action", "modify");
        startActivityForResult(intent, PRODUCT_CHOICE_REQUEST_CODE);
    }
}