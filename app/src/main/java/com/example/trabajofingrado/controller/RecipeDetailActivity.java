package com.example.trabajofingrado.controller;

import static com.example.trabajofingrado.R.id.*;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class RecipeDetailActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int
            RECIPE_MODIFY_RESULT_CODE = 1,
            SHOW_STORAGES = 1,
            REMOVE_FROM_STORAGE = 2,
            CREATE_NEW_SHOPPING_LIST = 3,
            ADD_TO_EXISTING_SHOPPING_LIST = 4;

    // Of instance
    private boolean recipeAvailable;
    private int amountPortions;
    private AlertDialog alertDialog;
    private ArrayList<String> availableStoragesId = new ArrayList<>(),
            availableShoppingListId = new ArrayList<>();
    private ImageView imgRecipeDetail;
    private TextView txtIngredients, txtName, txtSteps;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        setTitle("Recipe");

        // Bind the views
        this.bindViews();

        // Configure the drawer layout
        this.setDrawerLayout(nav_recipe_list);

        this.setData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECIPE_MODIFY_RESULT_CODE) {
            this.setData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
        switch (item.getItemId()) {
            case menu_item_modify_recipe:
                Intent intent = new Intent(RecipeDetailActivity.this, AddModifyRecipeActivity.class);
                intent.putExtra("action", "modify");
                intent.putExtra("recipeId", getIntent().getStringExtra("recipeId"));
                startActivityForResult(intent, RECIPE_MODIFY_RESULT_CODE);
                break;
            case menu_item_delete_recipe:
                createDeleteRecipeInputDialog().show();
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

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.drawerLayout = findViewById(drawer_layout_recipe_detail);
        this.toolbar = findViewById(toolbar_recipe_detail);
        this.txtName = findViewById(txtRecipeDetailName);
        this.txtIngredients = findViewById(R.id.txtIngredients);
        this.txtSteps = findViewById(R.id.txtSteps);
        this.imgRecipeDetail = findViewById(imgRecipeDetailImage);
    }

    private AlertDialog createDeleteRecipeInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Are you sure you want to delete " + recipe.getName() + "?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRecipe();
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

    private void deleteRecipe() {
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    startActivity(new Intent(RecipeDetailActivity.this, RecipeListActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private void setData() {
        Query query = RECIPE_REFERENCE.orderByChild("id").equalTo(getIntent().getStringExtra("recipeId"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtIngredients.setText(getString(R.string.ingredients));
                txtSteps.setText(getString(R.string.steps));
                for (DataSnapshot ds : snapshot.getChildren()) {
                    recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        txtName.setText(recipe.getName());

                        Glide.with(RecipeDetailActivity.this)
                                .load(recipe.getImage())
                                .error(R.drawable.image_not_found)
                                .into(imgRecipeDetail);

                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            txtIngredients.append("\n - " + ingredient.getValue().getName() +
                                    ": " + ingredient.getValue().getAmount() + " " +
                                    ingredient.getValue().getUnitType());
                        }

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

    private AlertDialog createPortionsAmountDialog(int action) {
        // Instance the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title
        builder.setTitle("Introduce the number of portions that you want to cook");

        // Configure the edit text
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(null);
        builder.setView(input);

        // Instance the confirm button
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check if the user introduced something
                if (Utils.checkValidString(input.getText().toString())) {
                    amountPortions = Integer.parseInt(input.getText().toString());

                    switch (action) {
                        case SHOW_STORAGES:
                            alertDialog = createStorageAvailableDialog(SHOW_STORAGES);
                            break;
                        case REMOVE_FROM_STORAGE:
                            alertDialog = createStorageAvailableChoiceDialog(REMOVE_FROM_STORAGE);
                            break;
                        case CREATE_NEW_SHOPPING_LIST:
                            alertDialog = createStorageAvailableChoiceDialog(CREATE_NEW_SHOPPING_LIST);
                            break;
                        case ADD_TO_EXISTING_SHOPPING_LIST:
                            alertDialog = createShoppingListChoiceDialog();
                            break;
                    }

                    alertDialog.show();

                } else {
                    Toasty.error(RecipeDetailActivity.this,
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

    private void getRecipesAvailableByStorage(ArrayAdapter<String> arrayAdapter, int amountPortions, int action) {
        // Get the database instance of the storages
        DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Set the query to get the selected storage
        Query query = storageRef.orderByChild(FirebaseAuth.getInstance().getUid());

        // Set the recipe as not available
        recipeAvailable = false;

        // Set the listener to get the data
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayAdapter.clear();
                availableStoragesId.clear();

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

                            // Check if the ingredient is available in the storage
                            if (!availableProducts.contains(ingredientName)) {
                                recipeAvailable = false;
                                // Cut the execution if not
                                break;
                            }

                            // Check if there's enough of the ingredient to cook the recipe
                            StorageProduct product = storedProducts.get(ingredientName);
                            if (product.getAmount() < ingredient.getAmount() * amountPortions) {
                                recipeAvailable = false;
                                // TODO FILL A HASHMAP WITH THE NAME AND THE AMOUNT OF THE PRODUCT TO CREATE A SHOPPING LIST LATER ON
                                // Cut the execution
                                break;
                            }

                            recipeAvailable = true;
                        }

                        if (recipeAvailable) {
                            arrayAdapter.add(storage.getName());
                            availableStoragesId.add(storage.getId());
                        }
                    }
                }
                if ((action == SHOW_STORAGES || action == REMOVE_FROM_STORAGE) && !recipeAvailable) {
                    Toasty.info(RecipeDetailActivity.this, "No storage has enough products").show();
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private AlertDialog createStorageAvailableDialog(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        builder.setTitle("Storages with enough ingredients");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.simple_list_item_1);

        getRecipesAvailableByStorage(arrayAdapter, amountPortions, action);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );

        return builder.create();
    }

    private AlertDialog createStorageAvailableChoiceDialog(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        builder.setTitle("Choose a storage");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.select_dialog_singlechoice);

        switch (action) {
            case REMOVE_FROM_STORAGE:
                getRecipesAvailableByStorage(arrayAdapter, amountPortions, action);
                break;
            case CREATE_NEW_SHOPPING_LIST:
                getAllStorages(arrayAdapter);
                break;
        }

        builder.setSingleChoiceItems(arrayAdapter, 0, null).
                setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Get the selected item from the list
                        int selectedItem = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                        switch (action) {
                            case REMOVE_FROM_STORAGE:
                                // Delete it from the storage
                                removeIngredientsFromStorage(availableStoragesId.get(selectedItem));
                                break;
                            case CREATE_NEW_SHOPPING_LIST:
                                createNewShoppingListDialog(availableStoragesId.get(selectedItem)).show();
                                break;
                        }
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private AlertDialog createShoppingListChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        builder.setTitle("Choose a shopping list");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RecipeDetailActivity.this, android.R.layout.select_dialog_singlechoice);

        getShoppingListsByUser(arrayAdapter);

        builder.setSingleChoiceItems(arrayAdapter, 0, null).
                setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Get the selected item from the list
                        int selectedItem = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                        addToExistingShoppingList(availableShoppingListId.get(selectedItem));
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private void addToExistingShoppingList(String shoppingListId) {
        DatabaseReference shoppingListsReference = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);
        Query query = shoppingListsReference.orderByChild("id").equalTo(shoppingListId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
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
                            Log.d("operations", "summed amount");

                            // Get the product
                            StorageProduct product = ingredient.getValue();

                            // Add the necessary amount to the existing product
                            product.setAmount(product.getAmount() + ingredient.getValue().getAmount() * amountPortions);

                            shoppingList.getProducts().put(product.getName(), product);
                        } else {
                            ingredient.getValue().setAmount(ingredient.getValue().getAmount() * amountPortions);

                            // Add the product with the necessary amount
                            shoppingList.getProducts().put(ingredient.getKey(), ingredient.getValue());
                        }
                    }

                    shoppingListsReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toasty.success(RecipeDetailActivity.this, "Added the " +
                                    "ingredients to the shopping list " + shoppingList.getName()).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private void getShoppingListsByUser(ArrayAdapter<String> arrayAdapter) {
        // Get the database instance of the shopping lists
        DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(Utils.SHOPPING_LIST_PATH);

        // Set the query to get the selected storage
        Query query = storageRef.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayAdapter.clear();
                availableShoppingListId.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    arrayAdapter.add(shoppingList.getName());
                    availableShoppingListId.add(shoppingList.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private void getAllStorages(ArrayAdapter<String> arrayAdapter) {
        // Get the database instance of the storages
        DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);

        // Set the query to get the selected storage
        Query query = storageRef.orderByChild(FirebaseAuth.getInstance().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayAdapter.clear();
                availableStoragesId.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);

                    arrayAdapter.add(storage.getName());
                    availableStoragesId.add(storage.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private void removeIngredientsFromStorage(String storageId) {
        DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
        Query query = storageReference.orderByChild("id").equalTo(storageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);

                    // Get the products stored in the selected storage
                    HashMap<String, StorageProduct> storedProducts = storage.getProducts();

                    // Generate a hashset with the stored products
                    HashSet<String> availableProducts = new HashSet<>(storedProducts.keySet());

                    Map<String, Object> childUpdates = new HashMap<>();

                    // Loop through all recipe ingredients
                    for (Map.Entry<String, StorageProduct> entry : recipe.getIngredients().entrySet()) {
                        String ingredientName = entry.getKey();
                        StorageProduct ingredient = entry.getValue();

                        // Check if the ingredient is available
                        if (!availableProducts.contains(ingredientName)) {
                            // Cut the execution
                            break;
                        }

                        // Check if there's enough of the ingredient
                        StorageProduct product = storedProducts.get(ingredientName);
                        if (product.getAmount() < ingredient.getAmount() * amountPortions) {
                            // Cut the execution
                            break;
                        }

                        int sumOfProducts = product.getAmount() - ingredient.getAmount() * amountPortions;

                        if (sumOfProducts > 0){
                            product.setAmount(sumOfProducts);
                            childUpdates.put("/" + storageId + "/products/" + product.getName(), product);
                        }else{
                            childUpdates.put("/" + storageId + "/products/" + product.getName(), null);
                        }
                    }

                    storageReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toasty.success(RecipeDetailActivity.this, "Products removed").show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(RecipeDetailActivity.this);
            }
        });
    }

    private AlertDialog createNewShoppingListDialog(String storageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);

        builder.setTitle("Name the shopping list");

        final EditText inputName = new EditText(RecipeDetailActivity.this);

        builder.setView(inputName);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.checkValidString(inputName.getText().toString())) {
                    DatabaseReference storageReference = FirebaseDatabase.getInstance().getReference(Utils.STORAGE_PATH);
                    Query query = storageReference.orderByChild("id").equalTo(storageId);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Storage storage = ds.getValue(Storage.class);

                                HashMap<String, StorageProduct> products = new HashMap<>();

                                for (Map.Entry<String, StorageProduct> product : recipe.getIngredients().entrySet()) {
                                    products.put(product.getKey(), new StorageProduct(
                                            product.getValue().getAmount() * amountPortions,
                                            product.getValue().getName(),
                                            product.getValue().getUnitType()));
                                }

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
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }
}














