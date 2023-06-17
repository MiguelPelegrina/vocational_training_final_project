package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.*;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.io.ShoppingListPutController;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use cases related to one of specific recipe:
 *  - See the information necessary to elaborate the recipe
 *  - Set the amount of portions that you want to elaborate
 *  - Delete a recipe
 *  - Check which storages have enough products to elaborate the recipe
 *  - Remove the ingredients of the recipe from the storage
 *  - Save or update a shopping list with products of the recipe
 */
public class RecipeDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int
            RECIPE_MODIFY_RESULT_CODE = 1,
            SHOW_STORAGES = 1,
            REMOVE_FROM_STORAGE = 2,
            CREATE_NEW_SHOPPING_LIST = 3,
            ADD_TO_EXISTING_SHOPPING_LIST = 4,
            CALCULATE_PORTIONS = 5;

    // Of instance
    private boolean recipeAvailable;
    private int amountPortions = 1;
    private AlertDialog dialog;
    private final ArrayList<String> availableStoragesIds = new ArrayList<>(),
            availableShoppingListIds = new ArrayList<>();
    private ImageView imgRecipeDetail;
    private TextView txtIngredients, txtName, txtSteps;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        setTitle("Recipe");

        bindViews();

        setDrawerLayout(nav_recipe_list);

        setRecipeData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECIPE_MODIFY_RESULT_CODE) {
            setRecipeData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check if the user wants to modify the recipe
        if (getIntent().getStringExtra("action").equals("modify")) {
            menu.findItem(menu_item_modify_recipe).setEnabled(true);
            menu.findItem(menu_item_delete_recipe).setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_detail_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check the chosen menu item
        switch (item.getItemId()) {
            case menu_item_calculate_portions:
                // Recalculate the amount of portions for this recipe
                createPortionsAmountDialog(CALCULATE_PORTIONS).show();
                break;
            case menu_item_modify_recipe:
                moveToAddModifyRecipe();
                break;
            case menu_item_delete_recipe:
                createDeleteRecipeDialog().show();
                break;
            case menu_item_storages_with_available_products:
                createPortionsAmountDialog(SHOW_STORAGES).show();
                break;
            case menu_item_remove_products_from_storage:
                createPortionsAmountDialog(REMOVE_FROM_STORAGE).show();
                break;
            case menu_item_create_new_shopping_list:
                createPortionsAmountDialog(CREATE_NEW_SHOPPING_LIST).show();
                break;
            case menu_item_add_ingredients_to_shopping_list:
                createPortionsAmountDialog(ADD_TO_EXISTING_SHOPPING_LIST).show();
                break;
        }

        return true;
    }

    // Auxiliary methods
    private void moveToAddModifyRecipe() {
        Intent intent = new Intent(RecipeDetailActivity.this, AddModifyRecipeActivity.class);
        intent.putExtra("action", "modify");
        intent.putExtra("recipeId", getIntent().getStringExtra("recipeId"));
        startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        drawerLayout = findViewById(drawer_layout_recipe_detail);
        toolbar = findViewById(toolbar_recipe_detail);
        txtName = findViewById(txtRecipeDetailName);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtSteps = findViewById(R.id.txtSteps);
        imgRecipeDetail = findViewById(imgRecipeDetailImage);
        imgRecipeDetail.setClipToOutline(true);
    }

    /**
     * Create a alert dialog to confirm the decision of the author to delete a recipe
     *
     * @return
     */
    private AlertDialog createDeleteRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Are you sure you want to delete " + recipe.getName() + "?");

        builder.setPositiveButton("Confirm", (dialog, which) -> deleteRecipe());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // 
        return builder.create();
    }

    /**
     * Removes the recipe from the database and returns the user to recipe list activity
     */
    private void deleteRecipe() {
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Remove the recipe
                    ds.getRef().removeValue();

                    // Return to the recipe list activity
                    startActivity(new Intent(RecipeDetailActivity.this, RecipeListActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Loads the data of the chosen recipe from the database into the views
     */
    private void setRecipeData() {
        // Search the recipe in the database
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        // Launch the query
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the recipe
                    recipe = ds.getValue(Recipe.class);

                    // Set the generic data
                    txtIngredients.setText(getString(R.string.ingredients) + " (portions: " + 1 + ")");
                    txtSteps.setText(getString(R.string.steps));

                    // Check if the recipe is instanced
                    if (recipe != null) {
                        // Set the specific data
                        // Name
                        txtName.setText(recipe.getName());
                        // Image
                        Glide.with(RecipeDetailActivity.this)
                                .load(recipe.getImage())
                                .error(R.drawable.image_not_found)
                                .into(imgRecipeDetail);
                        // Ingredients
                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            txtIngredients.append("\n - " + ingredient.getValue().getName() +
                                    ": " + (ingredient.getValue().getAmount()) +
                                    " " + ingredient.getValue().getUnitType());
                        }
                        // Steps
                        for (String step : recipe.getSteps()) {
                            txtSteps.append("\n - " + step);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Creates an alert to ask the user about the amount of portions
     *
     * @param action
     * @return
     */
    private AlertDialog createPortionsAmountDialog(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Introduce the number of portions that you want to cook");

        // Configure the edit text
        final EditText input = new EditText(this);
        input.setText(amountPortions + "");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(null);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Check if the user introduced valid data
            if (Utils.checkValidString(input.getText().toString())) {
                amountPortions = Integer.parseInt(input.getText().toString());

                // TODO REDUCE THE INPUT DIALOGS TO ONE PER ACTION
                // Check what the user wants to do, depends on the chosen menu item
                switch (action) {
                    case CALCULATE_PORTIONS:
                        // Set the ingredients with the new values
                        txtIngredients.setText(getString(R.string.ingredients) + " (portions: " + amountPortions + ")");
                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            txtIngredients.append("\n - " + ingredient.getValue().getName() +
                                    ": " + (ingredient.getValue().getAmount() * amountPortions) +
                                    " " + ingredient.getValue().getUnitType());
                        }
                        break;
                    case SHOW_STORAGES:
                        this.dialog = availableStoragesDialog();
                        break;
                    case REMOVE_FROM_STORAGE:
                        this.dialog = chooseAvailableStorageDialog(REMOVE_FROM_STORAGE);
                        break;
                    case CREATE_NEW_SHOPPING_LIST:
                        this.dialog = chooseAvailableStorageDialog(CREATE_NEW_SHOPPING_LIST);
                        break;
                    case ADD_TO_EXISTING_SHOPPING_LIST:
                        this.dialog = createShoppingListChoiceDialog();
                        break;
                }

                // Check if the dialog was instanced
                if (this.dialog != null) {
                    this.dialog.show();
                }

            } else {
                Toasty.error(RecipeDetailActivity.this,
                        "You need to enter a valid amount").show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Gets all the storages that have enough products to cook the chosen recipe
     * @param arrayAdapter   Array adapter that will show the available storages in a list view
     * @param amountPortions
     */
    private void getRecipesAvailableByStorage(ArrayAdapter<String> arrayAdapter, int amountPortions) {
        // Generate a flag
        recipeAvailable = false;

        // Search the storages of the user
        Query query = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the lists
                arrayAdapter.clear();
                availableStoragesIds.clear();

                // Loop through the every storage of the user
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Check if the storage has any products
                    if (storage.getProducts() != null) {
                        // Get the products stored in the selected storage
                        HashMap<String, StorageProduct> storedProducts = storage.getProducts();

                        // Generate a hashset with the stored products
                        HashSet<String> availableProducts = new HashSet<>(storedProducts.keySet());

                        // Loop through all recipe ingredients
                        for (Map.Entry<String, StorageProduct> entry : recipe.getIngredients().entrySet()) {
                            String ingredientName = entry.getKey();
                            StorageProduct ingredient = entry.getValue();

                            // Check if the ingredient is available in the storage and if there is
                            // enough of it  to cook the recipe
                            if (availableProducts.contains(ingredientName) &&
                                    storedProducts.get(ingredientName).getAmount() > ingredient.getAmount() * amountPortions) {
                                // Set the flag
                                recipeAvailable = true;
                            }

                            // TODO FILL A HASHMAP WITH THE NAME AND THE AMOUNT OF THE PRODUCT
                            //  TO CREATE A SHOPPING LIST THAT DEPENDS ON THE ALREADY AVAILABLE
                            //  AMOUNT OF PRODUCTS OF THE STORAGE
                        }

                        // Check if the recipe is available
                        if (recipeAvailable) {
                            // Add the storage to the lists
                            arrayAdapter.add(storage.getName());
                            availableStoragesIds.add(storage.getId());
                        }
                    }
                }
                // Check if the recipe is available
                if (!recipeAvailable) {
                    // Inform the user
                    Toasty.info(RecipeDetailActivity.this, "No storage has enough products").show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Creates an alert dialog to show the user the available storages
     */
    private AlertDialog availableStoragesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        // Configure the builder
        builder.setTitle("Storages with enough ingredients");

        // Generate the adapter that will show the storages
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.simple_list_item_1);

        // Get the storages and show them
        getRecipesAvailableByStorage(arrayAdapter, amountPortions);
        builder.setAdapter(arrayAdapter, (dialogInterface, i) -> dialogInterface.dismiss());

        return builder.create();
    }

    /**
     *
     * @param action
     * @return
     */
    private AlertDialog chooseAvailableStorageDialog(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        // Configure the builder
        builder.setTitle("Choose a storage");

        // Generate an array adapter that will show the storages
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.select_dialog_singlechoice);

        // Check what to do
        switch (action) {
            // If it has to get all storages with the necessary products
            case REMOVE_FROM_STORAGE:
                getRecipesAvailableByStorage(arrayAdapter, amountPortions);
                break;
                // If has to get all storages
            case CREATE_NEW_SHOPPING_LIST:
                getAllStorages(arrayAdapter);
                break;
        }

        builder.setSingleChoiceItems(arrayAdapter, 0, null).
                setPositiveButton("Confirm", (dialogInterface, i) -> {
                    // Get the selected item from the list
                    int selectedItem = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();

                    // Check what to do
                    switch (action) {
                        case REMOVE_FROM_STORAGE:
                            removeIngredientsFromStorage(availableStoragesIds.get(selectedItem));
                            break;
                        case CREATE_NEW_SHOPPING_LIST:
                            createNewShoppingListDialog(availableStoragesIds.get(selectedItem)).show();
                            break;
                    }
                });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        return builder.create();
    }

    /**
     * 
     */
    private AlertDialog createShoppingListChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        // Configure the builder
        builder.setTitle("Choose a shopping list");

        // Generate the adapter to show the shopping list
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.select_dialog_singlechoice);

        // Fill the array adapter with the shopping lists
        getShoppingListsByUser(arrayAdapter);

        builder.setSingleChoiceItems(arrayAdapter, 0, null).
                setPositiveButton("Confirm", (dialogInterface, i) -> {
                    // Get the selected item from the list
                    int selectedItem = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                    // Add the ingredients of the recipe to the existing shopping list
                    addToExistingShoppingList(availableShoppingListIds.get(selectedItem));
                });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        return builder.create();
    }

    /**
     * Updates an existing shopping list
     * @param shoppingListId
     */
    private void addToExistingShoppingList(String shoppingListId) {
        // Search the shopping list
        Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Check if the shopping list has any products
                    if (shoppingList.getProducts() == null) {
                        // Generate a products map
                        shoppingList.setProducts(new HashMap<>());
                    }

                    // Loop through all the ingredients
                    for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                        // Check if the ingredients are already in the shopping list
                        if (shoppingList.getProducts().containsValue(ingredient.getValue())) {
                            // Get the product
                            StorageProduct product = ingredient.getValue();

                            // Add the necessary amount to the existing product
                            product.setAmount(product.getAmount() + ingredient.getValue().getAmount() * amountPortions);

                            // Add the product to the shopping list
                            shoppingList.getProducts().put(product.getName(), product);
                        } else {
                            ingredient.getValue().setAmount(ingredient.getValue().getAmount() * amountPortions);

                            // Add the product with the necessary amount
                            shoppingList.getProducts().put(ingredient.getKey(), ingredient.getValue());
                        }
                    }

                    // Update the shopping list
                    SHOPPING_LIST_REFERENCE.child(shoppingList.getId())
                            .setValue(shoppingList)
                            .addOnCompleteListener(task ->
                                    // Inform the user
                                    Toasty.success(RecipeDetailActivity.this,
                                    "Added the ingredients to the shopping list "
                                            + shoppingList.getName()).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Fills the array adapter with the shopping lists of the user
     * @param arrayAdapter
     */
    private void getShoppingListsByUser(ArrayAdapter<String> arrayAdapter) {
        // Search the shopping lists of the user
        Query query = SHOPPING_LIST_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the lists
                arrayAdapter.clear();
                availableShoppingListIds.clear();

                // Loop through the shopping lists
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the shopping list
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    // Add the shopping lists to the lists
                    arrayAdapter.add(shoppingList.getName());
                    availableShoppingListIds.add(shoppingList.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Fills the array adapter with all the storages of the user
     * @param arrayAdapter
     */
    private void getAllStorages(ArrayAdapter<String> arrayAdapter) {
        // Search the storages
        Query query = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the lists
                arrayAdapter.clear();
                availableStoragesIds.clear();

                // Loop through the storages
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Add the storage to the lists
                    arrayAdapter.add(storage.getName());
                    availableStoragesIds.add(storage.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Removes the ingredients of the recipe from the storage
     * @param storageId
     */
    private void removeIngredientsFromStorage(String storageId) {
        // Search the storage
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Get the products stored in the selected storage
                    HashMap<String, StorageProduct> storedProducts = storage.getProducts();

                    // Generate a hashset with the stored products
                    HashSet<String> availableProducts = new HashSet<>(storedProducts.keySet());

                    // Generate a map with updates
                    Map<String, Object> updates = new HashMap<>();

                    // Loop through all recipe ingredients
                    for (Map.Entry<String, StorageProduct> entry : recipe.getIngredients().entrySet()) {
                        // Get the ingredient
                        StorageProduct ingredient = entry.getValue();

                        // Check if the ingredient is available
                        if (!availableProducts.contains(ingredient.getName())) {
                            // Cut the execution
                            break;
                        }

                        // Check if there's enough of the ingredient
                        StorageProduct product = storedProducts.get(ingredient.getName());
                        if (product.getAmount() < ingredient.getAmount() * amountPortions) {
                            // Cut the execution
                            break;
                        }

                        int sumOfProducts = product.getAmount() - ingredient.getAmount() * amountPortions;

                        if (sumOfProducts > 0) {
                            product.setAmount(sumOfProducts);
                            updates.put("/" + storageId + "/products/" + product.getName(), product);
                        } else {
                            updates.put("/" + storageId + "/products/" + product.getName(), null);
                        }
                    }

                    // Update the storage
                    STORAGE_REFERENCE.updateChildren(updates).addOnCompleteListener(task ->
                            // Inform the user
                            Toasty.success(RecipeDetailActivity.this,
                                    "Products removed").show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    /**
     * Creates an alert dialog toe create a new shopping list
     * @param storageId
     */
    private AlertDialog createNewShoppingListDialog(String storageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        // Configure the builder
        builder.setTitle("Name the shopping list");

        // Set an edit text to name the shopping list
        final EditText inputName = new EditText(RecipeDetailActivity.this);
        builder.setView(inputName);

        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the data is valid
            if (Utils.checkValidString(inputName.getText().toString())) {
                // Search the storage
                Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the storage
                            Storage storage = ds.getValue(Storage.class);

                            // Generate a products map
                            HashMap<String, StorageProduct> products = new HashMap<>();

                            // Loop through the ingredients of the recipe
                            for (Map.Entry<String, StorageProduct> product : recipe.getIngredients().entrySet()) {
                                // Put the ingredient into the map
                                products.put(product.getKey(), new StorageProduct(
                                        product.getValue().getAmount() * amountPortions,
                                        product.getValue().getName(),
                                        product.getValue().getUnitType()));
                            }

                            // Create a new shopping list with products
                            ShoppingListPutController.createNewShoppingListWithProducts(
                                    RecipeDetailActivity.this, products, storage,
                                    inputName.getText().toString()
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utils.connectionError(RecipeDetailActivity.this);
                    }
                });
            } else {
                Utils.enterValidData(RecipeDetailActivity.this);
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        // 
        return builder.create();
    }
}