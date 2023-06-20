package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.id.menu_item_delete_recipe;
import static com.example.trabajofingrado.io.ShoppingListPutController.addShoppingListToStorage;
import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.SHOPPING_LIST_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.STORAGE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.checkValidString;
import static com.example.trabajofingrado.utilities.Utils.connectionError;
import static com.example.trabajofingrado.utilities.Utils.dateToEpoch;
import static com.example.trabajofingrado.utilities.Utils.enterValidData;
import static com.example.trabajofingrado.utilities.Utils.epochToDateTime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

/**
 * Controller that handles the use cases related to the calender:
 * - See cooking events
 * - See recipes of one cooking event
 * - Generate a new cooking event
 * - Save or update a shopping list with the selected recipes of one cooking event
 */
public class CalendarActivity extends BaseActivity {
    // Fields
    private int recipePosition, shoppingListPosition, storagePosition;
    private ActionMode actionMode;
    private final ArrayList<String> storageListIds = new ArrayList<>();
    private final ArrayList<String> shoppingListIds = new ArrayList<>();
    private final ArrayList<Recipe> recipeList = new ArrayList<>();
    private Button btnAddRecipe, btnAddProductsToShoppingList;
    private CollapsibleCalendar collapsibleCalendar;
    private Recipe recipe;
    private RecipesDay selectedRecipesDay;
    private RecipeRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;
    private ShoppingList shoppingList;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setTitle("Your calendar");

        bindViews();

        setDrawerLayout(R.id.nav_calendar);

        setRecyclerView();

        setListener();

        setCookingDays();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onContextMenuClosed(menu);

        // Check the item id
        if (v.getId() == R.id.rvCalendarRecipes) {
            getMenuInflater().inflate(R.menu.calendar_recipe_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == menu_item_delete_recipe) {
            deleteRecipeDialog().show();
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Deletes a recipes day from the database
     */
    private void deleteRecipeFromDay() {
        Query query = CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid()).child(selectedRecipesDay.getDate() + "");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Check if the recipes day has any recipes
                    if (selectedRecipesDay.getRecipes().isEmpty()) {
                        // Remove the recipes day
                        ds.getRef().removeValue();
                    } else {
                        // Set the updated list of recipes of the recipes day. This list does not
                        // include the deleted recipe anymore
                        CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid())
                                .child(selectedRecipesDay.getDate() + "")
                                .child("recipes")
                                .setValue(selectedRecipesDay.getRecipes());
                    }
                }

                // NOT THE MOST ELEGANT SOLUTION, LIBRARY DOES NOT HAVE METHODS TO RESET
                //  EVENTS NOR REMOVE EXISTING ONES
                // Check if the recipes day has any recipes
                if (selectedRecipesDay.getRecipes().isEmpty()) {
                    // Refreshes the activity to unset the event icon
                    recreate();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Loads all the cooking days of the user from the database
     */
    private void setCookingDays() {
        // Search the recipes days of the user
        Query query = CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the recipes day
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);
                    // Get the date
                    LocalDateTime time = epochToDateTime(recipesDay.getDate());
                    // Set the event icon in the calendar
                    collapsibleCalendar.addEventTag(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        btnAddRecipe = findViewById(R.id.btnAddRecipeCalendar);
        btnAddProductsToShoppingList = findViewById(R.id.btnAddProductsToShoppingListCalendar);
        collapsibleCalendar = findViewById(R.id.calendarView);
        drawerLayout = findViewById(R.id.drawer_layout_calendar);
        toolbar = findViewById(R.id.toolbar_calendar);
        recyclerView = findViewById(R.id.rvCalendarRecipes);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        adapter = new RecipeRecyclerAdapter(recipeList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Separate one item from another
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Set the listener of all the views
     */
    private void setListener() {
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                // Get the day
                Day day = collapsibleCalendar.getSelectedDay();
                // Set the buttons to add recipes and to add products to a shopping list as visible
                btnAddRecipe.setVisibility(View.VISIBLE);
                btnAddProductsToShoppingList.setVisibility(View.VISIBLE);

                // Set the selected recipes day
                selectedRecipesDay = new RecipesDay(dateToEpoch(day.getDay(), day.getMonth() + 1, day.getYear()), new ArrayList<>());

                // Show the recipes of the selected recipes day
                fillRecipesList(selectedRecipesDay.getDate());
            }

            // Not implemented listener
            @Override
            public void onItemClick(@NonNull View view) {
            }

            @Override
            public void onDataUpdate() {
            }

            @Override
            public void onMonthChange() {
            }

            @Override
            public void onWeekChange(int i) {
            }

            @Override
            public void onClickListener() {
            }

            @Override
            public void onDayChanged() {
            }
        });

        btnAddRecipe.setOnClickListener(view -> {
            // Check if the recipes list limit is reached
            if (recipeList.size() < 7) {
                // Move to recipe list activity to select a recipe to add to the recipes day
                Intent intent = new Intent(CalendarActivity.this, RecipeListActivity.class);
                intent.putExtra("recipesDayDate", selectedRecipesDay.getDate());
                intent.putExtra("recipesSize", selectedRecipesDay.getRecipes().size());
                startActivity(intent);
            } else {
                Toasty.info(CalendarActivity.this, "You can only add 7 recipes per day").show();
            }
        });

        btnAddProductsToShoppingList.setOnClickListener(view -> createRecipesToShoppingListDialog().show());

        adapter.setOnClickListener(view -> {
            // Set the selected recipe
            setRecipe(view);

            Utils.moveToRecipeDetails(CalendarActivity.this, recipe);
        });

        adapter.setOnLongClickListener(view -> {
            setRecipe(view);

            registerForContextMenu(recyclerView);

            return false;
        });
    }

    /**
     * Generate the action menu delete recipes
     */
    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.calendar_recipe_context_menu, menu);
            mode.setTitle("Delete recipe");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Get the selected item
            if (item.getItemId() == menu_item_delete_recipe) {
                // Remove the recipe from the recipe list and from list of recipes of the selected recipes day
                recipeList.remove(recipePosition);
                selectedRecipesDay.getRecipes().remove(recipePosition);
                // Delete the recipe in the database
                deleteRecipeFromDay();
                mode.finish();
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    /**
     * Generates an alertdialog to enable the user to choose between creating a new shopping list
     * or adding products to an existing ones. The user can choose the storage, the shopping list
     * and/or the amount portions they want to set in the shopping list.
     *
     * @return
     */
    private AlertDialog createRecipesToShoppingListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);

        // Set the title
        builder.setTitle("Choose the recipes you want to add");

        // Generate the layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Generate the recipe name list
        ArrayList<String> recipeNameList = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            recipeNameList.add(recipe.getName());
        }

        // Generate a list with the selected recipes
        ArrayList<String> selectedItems = new ArrayList<>(recipeNameList);

        // Generate the adapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_multiple_choice, recipeNameList);

        // Generate the list view
        final ListView listView = new ListView(CalendarActivity.this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            // Get the selected item
            String selectedItem = arrayAdapter.getItem(i);
            // Remove the selected recipe from the list
            if (selectedItems.contains(selectedItem)) {
                selectedItems.remove(selectedItem);
                // Add the selected recipe to the list
            } else {
                selectedItems.add(selectedItem);
            }
        });

        // Add the list view to the layout
        layout.addView(listView);

        // Set all items as checked by default
        for (int i = 0; i < recipeNameList.size(); i++) {
            listView.setItemChecked(i, true);
        }

        final TextView textViewAmountPortions = new TextView(this);
        textViewAmountPortions.setText("Number of portions");
        textViewAmountPortions.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(textViewAmountPortions);

        // Configure the edit text of the amount of portions the user wants to buy for
        final EditText inputAmountPortions = new EditText(this);
        inputAmountPortions.setText(1 + "");
        inputAmountPortions.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmountPortions.setTransformationMethod(null);
        layout.addView(inputAmountPortions);

        // Set a check box to know if we user wants to add the products to an existing shopping list
        final CheckBox cbAddToExistingShoppingList = new CheckBox(this);
        cbAddToExistingShoppingList.setText(R.string.add_to_an_existing_shopping_list);
        cbAddToExistingShoppingList.setChecked(false);

        // Add the check box to the layout
        layout.addView(cbAddToExistingShoppingList);

        // Set the text view of the available shopping lists
        final TextView txtShoppingLists = new TextView(this);
        txtShoppingLists.setText("Available shopping lists");
        txtShoppingLists.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtShoppingLists.setVisibility(View.GONE);
        layout.addView(txtShoppingLists);

        // Set the spinner that contains the shopping lists to choose from if the user wants to
        // add to a new shopping list
        final Spinner shoppingListSpinner = new Spinner(CalendarActivity.this);
        shoppingListSpinner.setVisibility(View.GONE);
        // Set the adapter
        ArrayAdapter<String> shoppingListsNameList = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_1);
        // Get the shopping list from the database
        getShoppingLists(shoppingListsNameList);
        // Set the adapter to the spinner
        shoppingListSpinner.setAdapter(shoppingListsNameList);
        shoppingListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Set the shopping list position
                shoppingListPosition = i;

                // Search the shopping list in the database
                Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListIds.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the shopping list
                            shoppingList = ds.getValue(ShoppingList.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        connectionError(CalendarActivity.this);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        layout.addView(shoppingListSpinner);

        // Set the text view of the available storages
        final TextView txtStorages = new TextView(CalendarActivity.this);
        txtStorages.setText("Available storages");
        txtStorages.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(txtStorages);

        // Set the spinner that contains the storage to choose from if the user wants to create a new
        // shopping list
        final Spinner storageSpinner = new Spinner(CalendarActivity.this);
        // Set the adapter
        ArrayAdapter<String> storageNameList = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_1);
        // Get the storages from the database
        getStorageLists(storageNameList);
        // Set the adapter to the spinner
        storageSpinner.setAdapter(storageNameList);
        storageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Set the position of the storage
                storagePosition = i;

                // Search the storage of the shopping list
                Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageListIds.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the storage
                            storage = ds.getValue(Storage.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        connectionError(CalendarActivity.this);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        layout.addView(storageSpinner);

        // Set the edit text to set a shopping list name
        final EditText inputName = new EditText(CalendarActivity.this);
        inputName.setHint("Name the shopping list");
        layout.addView(inputName);

        cbAddToExistingShoppingList.setOnClickListener(view -> {
            // Check if the user wants to add to an existing shopping list
            if (cbAddToExistingShoppingList.isChecked()) {
                // Set the views visible to choose a shopping list
                txtShoppingLists.setVisibility(View.VISIBLE);
                shoppingListSpinner.setVisibility(View.VISIBLE);
                txtStorages.setVisibility(View.GONE);
                inputName.setVisibility(View.GONE);
                storageSpinner.setVisibility(View.GONE);
            } else {
                // Set the views visible to create a new shopping list
                txtStorages.setVisibility(View.VISIBLE);
                inputName.setVisibility(View.VISIBLE);
                storageSpinner.setVisibility(View.VISIBLE);
                txtShoppingLists.setVisibility(View.GONE);
                shoppingListSpinner.setVisibility(View.GONE);
            }
        });

        // Set the layout
        builder.setView(layout);

        // Set the buttons
        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            // Check if the a valid amount of portions was introduced
            if (checkValidString(inputAmountPortions.getText().toString())) {
                int amountPortions = Integer.parseInt(inputAmountPortions.getText().toString());

                // Check if the user selected an existing shopping list
                if (cbAddToExistingShoppingList.isChecked()) {
                    // Add the products of the selected recipes to the shopping list
                    addToExistingShoppingList(selectedItems, amountPortions);
                } else {
                    // Check if the user entered a valid shopping list name
                    if (checkValidString(inputName.getText().toString())) {
                        // Create a new shopping list with the products of the recipes
                        createNewShoppingList(selectedItems, amountPortions, inputName.getText().toString());
                    } else {
                        enterValidData(CalendarActivity.this);
                    }
                }
            } else {
                enterValidData(CalendarActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    /**
     * Adds the products of the recipes to an existing shopping list
     *
     * @param recipes
     * @param amountPortions
     */
    private void addToExistingShoppingList(ArrayList<String> recipes, int amountPortions) {
        // Check if any shopping list exists
        if (shoppingListIds.size() != 0) {
            // Search the shopping list
            Query query = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(shoppingListIds.get(shoppingListPosition));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check if the shopping list has any products already
                    if (shoppingList.getProducts() == null) {
                        // If not, generate a products map
                        shoppingList.setProducts(new HashMap<>());
                    }

                    // Loop through the recipes
                    for (Recipe recipe : recipeList) {
                        // Check if the recipe is part of the recipes list
                        if (recipes.contains(recipe.getName())) {
                            // Loop through ingredient of the recipe
                            for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                                // Check if the shopping list already has the ingredient
                                if (shoppingList.getProducts().containsValue(ingredient.getValue())) {
                                    // Get the product
                                    StorageProduct product = ingredient.getValue();

                                    // Add the necessary amount to the existing product
                                    product.setAmount(product.getAmount() + ingredient.getValue().getAmount() * amountPortions);

                                    // Add the product to the shopping list
                                    shoppingList.getProducts().put(product.getName(), product);
                                } else {
                                    // Set the amount of the ingredient
                                    ingredient.getValue().setAmount(ingredient.getValue().getAmount() * amountPortions);

                                    // Add the product with the necessary amount
                                    shoppingList.getProducts().put(ingredient.getKey(), ingredient.getValue());
                                }
                            }
                        }
                    }

                    // Save the shopping list into the database
                    SHOPPING_LIST_REFERENCE.child(shoppingList.getId())
                            .setValue(shoppingList)
                            .addOnCompleteListener(task ->
                                    // Inform the user
                                    Toasty.success(CalendarActivity.this,
                                            "Ingredients added to shopping list "
                                                    + shoppingList.getName()).show());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    connectionError(CalendarActivity.this);
                }
            });
        }
    }

    /**
     * Creates a new shopping list with all the products of the selected recipes
     *
     * @param recipes
     * @param amountPortions   Amount of portions of the recipes that will be added to the shopping list
     * @param shoppingListName
     */
    private void createNewShoppingList(ArrayList<String> recipes, int amountPortions, String shoppingListName) {
        // Search the storage of the shopping list
        Query query = STORAGE_REFERENCE.orderByChild("id").equalTo(storageListIds.get(storagePosition));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage storage = ds.getValue(Storage.class);

                    // Generate a products map
                    HashMap<String, StorageProduct> products = new HashMap<>();

                    // Loop through the recipes
                    for (Recipe recipe : recipeList) {
                        // Check if the recipe is part of the selected recipes list
                        if (recipes.contains(recipe.getName())) {

                            // Loop through the products of all recipes
                            for (Map.Entry<String, StorageProduct> product : recipe.getIngredients().entrySet()) {
                                // Put the products into the products map
                                products.put(product.getKey(), new StorageProduct(
                                        product.getValue().getAmount() * amountPortions,
                                        product.getValue().getName(),
                                        product.getValue().getUnitType()));
                            }
                        }
                    }

                    // Generate the shopping list
                    ShoppingList shoppingList = new ShoppingList(products,
                            shoppingListName, Utils.getCurrentTime(), UUID.randomUUID().toString(),
                            storage.getId(), storage.getName());

                    // Save the shopping list in database
                    SHOPPING_LIST_REFERENCE.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(task -> {
                        addShoppingListToStorage(CalendarActivity.this, storage, shoppingList);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Gets all storages of the user
     *
     * @param arrayAdapter
     */
    private void getStorageLists(ArrayAdapter<String> arrayAdapter) {
        // Search the storages in the database
        Query query = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                arrayAdapter.clear();
                storageListIds.clear();

                // Loop through the storages
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storages
                    Storage st = ds.getValue(Storage.class);

                    // If the user if part of the storage
                    if (st.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        // Add the storage to the lists
                        arrayAdapter.add(st.getName());
                        storageListIds.add(st.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Fills the shopping lists list with all the shopping list of the from the users storages
     */
    private void getShoppingLists(ArrayAdapter<String> arrayAdapter) {
        // Search the storages of the user
        Query storageQuery = STORAGE_REFERENCE.orderByChild(FirebaseAuth.getInstance().getUid());
        storageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                arrayAdapter.clear();
                shoppingListIds.clear();

                // Loop through the storages
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the storage
                    Storage st = ds.getValue(Storage.class);

                    // Check if the storage has any shopping lists
                    if (st.getShoppingLists() != null) {

                        // Loop through the shopping lists
                        for (Map.Entry<String, Boolean> entry : st.getShoppingLists().entrySet()) {
                            // Search the shopping list in the database
                            Query shoppingListQuery = SHOPPING_LIST_REFERENCE.orderByChild("id").equalTo(entry.getKey());
                            shoppingListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        // Get the shopping list
                                        ShoppingList sl = dataSnapshot.getValue(ShoppingList.class);

                                        // Add the shopping lists to the adapter and to a list of shopping list ids
                                        arrayAdapter.add(sl.getName());
                                        shoppingListIds.add(sl.getId());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    connectionError(CalendarActivity.this);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Fills the recipe list with all the recipes from the database
     */
    private void fillRecipesList(Long date) {
        // Set the database to get all the recipes
        Query queryCalendar = CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid()).orderByKey().equalTo(String.valueOf(date));
        queryCalendar.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                recipeList.clear();
                //recyclerAdapter.notifyDataSetChanged();
                btnAddProductsToShoppingList.setEnabled(false);

                // Get the recipes of the selected day
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the recipes day
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);

                    // Loop through the recipes of the recipes day
                    for (String recipe : recipesDay.getRecipes()) {
                        // Search the recipe
                        Query queryRecipes = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe);
                        queryRecipes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    // Get the recipe
                                    Recipe recipe = dataSnapshot.getValue(Recipe.class);

                                    // Add the recipe to the recipe list
                                    recipeList.add(recipe);

                                    // Add the recipe to the recipe list of the selected recipes day
                                    selectedRecipesDay.getRecipes().add(recipe.getId());
                                }

                                adapter.notifyDataSetChanged();

                                // Enable the add to shopping lists button
                                btnAddProductsToShoppingList.setEnabled(!(recipeList.isEmpty()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                connectionError(CalendarActivity.this);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionError(CalendarActivity.this);
            }
        });
    }

    private AlertDialog deleteRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configure the builder
        builder.setTitle("Are you sure you want to delete the recipe " + recipe.getName() + "?");
        builder.setPositiveButton("Confirm", ((dialogInterface, i) -> {
            // Remove the recipe from the recipe list and from list of recipes of the selected recipes day
            recipeList.remove(recipePosition);
            selectedRecipesDay.getRecipes().remove(recipePosition);
            deleteRecipeFromDay();
        }));
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        return builder.create();
    }

    /**
     * Set the selected recipe
     * @param view
     */
    private void setRecipe(View view) {
        // Get the view holder
        viewHolder = (RecyclerView.ViewHolder) view.getTag();

        // Get the position
        recipePosition = viewHolder.getAdapterPosition();

        // Get the recipe
        recipe = recipeList.get(recipePosition);
    }
}