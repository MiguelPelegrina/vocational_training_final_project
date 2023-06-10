package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.id.menu_item_delete_recipe;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.shoppingListReference;
import static com.example.trabajofingrado.utilities.StorageListInputDialogs.storageReference;
import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.checkValidString;
import static com.example.trabajofingrado.utilities.Utils.dateToEpoch;
import static com.example.trabajofingrado.utilities.Utils.epochToDateTime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.io.ShoppingListPutController;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;
    private ShoppingList shoppingList;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setTitle("Your calendar");

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_calendar);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();


        setCookingDays();


        //setCurrentDay();
    }

    private void deleteRecipeFromDay() {
        Query query = CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid()).child(selectedRecipesDay.getDate() + "");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (selectedRecipesDay.getRecipes().isEmpty()) {
                        ds.getRef().removeValue();
                    } else {
                        CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid())
                                .child(selectedRecipesDay.getDate() + "")
                                .child("recipes")
                                .setValue(selectedRecipesDay.getRecipes());
                    }
                }

                // TODO NOT THE MOST ELEGANT SOLUTION, LIBRARY DOES NOT HAVE METHODS TO RESET
                //  EVENTS NOR REMOVE EXISTING ONES
                if (selectedRecipesDay.getRecipes().isEmpty()) {
                    recreate();
                }

                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    private void setCookingDays() {
        Query query = CALENDAR_REFERENCE.child(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO PROGRESS BAR
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);
                    LocalDateTime time = epochToDateTime(recipesDay.getDate());

                    collapsibleCalendar.addEventTag(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });


    }

    private void setCurrentDay() {
        // TODO DOES NOT WORK YET
        LocalDateTime time = Utils.epochToDateTime(System.currentTimeMillis());
        Day day = new Day(time.getDayOfMonth(), time.getMonthValue(), time.getYear());
        Log.d("day", day.getDay() + "" + day.getMonth() + day.getYear());
        collapsibleCalendar.setSelectedItem(day);

        selectedRecipesDay = new RecipesDay(day.toUnixTime(), new ArrayList<>());
        fillRecipesList(selectedRecipesDay.getDate());
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
        recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setListener() {
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = collapsibleCalendar.getSelectedDay();
                btnAddRecipe.setVisibility(View.VISIBLE);
                btnAddProductsToShoppingList.setVisibility(View.VISIBLE);

                selectedRecipesDay = new RecipesDay(dateToEpoch(day.getDay(), day.getMonth() + 1, day.getYear()), new ArrayList<>());
                fillRecipesList(selectedRecipesDay.getDate());
            }

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
            // TODO BAND AID SOLUTION
            if(recipeList.size() < 7){
                Intent intent = new Intent(CalendarActivity.this, RecipeListActivity.class);
                intent.putExtra("recipesDayDate", selectedRecipesDay.getDate());
                intent.putExtra("recipesSize", selectedRecipesDay.getRecipes().size());
                startActivity(intent);
            } else {
                Toasty.info(CalendarActivity.this, "You can only add 7 recipes per day").show();
            }
        });

        btnAddProductsToShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createRecipesToShoppingListDialog().show();
            }
        });

        recyclerAdapter.setOnClickListener(view -> {
            setRecipe(view);

            Utils.moveToRecipeDetails(CalendarActivity.this, recipe);
        });

        recyclerAdapter.setOnLongClickListener(view -> {
            boolean res = false;

            if (actionMode == null) {
                setRecipe(view);

                actionMode = startSupportActionMode(actionCallback);
                res = true;
            }

            return res;
        });
    }

    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.recipe_detail_action_menu, menu);
            mode.setTitle("Delete recipe");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getItemId() == menu_item_delete_recipe) {
                recipeList.remove(recipePosition);
                selectedRecipesDay.getRecipes().remove(recipePosition);
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

    private AlertDialog createRecipesToShoppingListDialog() {
        // Generate the builder
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
        ArrayList<String> selectedItems = new ArrayList<>(recipeNameList);

        // Generate the adapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_multiple_choice, recipeNameList);

        // Generate the list view
        final ListView listView = new ListView(CalendarActivity.this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = arrayAdapter.getItem(i);
            if(selectedItems.contains(selectedItem)){
                selectedItems.remove(selectedItem);
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

        // Configure the edit text
        final EditText inputAmountPortions = new EditText(this);
        inputAmountPortions.setHint("Number of portions");
        inputAmountPortions.setText(1 + "");
        inputAmountPortions.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmountPortions.setTransformationMethod(null);
        layout.addView(inputAmountPortions);

        // Set a check box to know if we user wants to add the products to an existing shopping list
        final CheckBox cbAddToExistingShoppingList = new CheckBox(CalendarActivity.this);
        cbAddToExistingShoppingList.setText(R.string.add_to_an_existing_shopping_list);
        cbAddToExistingShoppingList.setChecked(false);

        // Add the check box to the layout
        layout.addView(cbAddToExistingShoppingList);

        final TextView txtShoppingLists = new TextView(CalendarActivity.this);
        txtShoppingLists.setText("Available shopping lists");
        txtShoppingLists.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtShoppingLists.setVisibility(View.GONE);
        layout.addView(txtShoppingLists);

        final Spinner shoppingListSpinner = new Spinner(CalendarActivity.this);
        shoppingListSpinner.setVisibility(View.GONE);
        ArrayAdapter<String> shoppingListsNameList = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_1);
        getShoppingLists(shoppingListsNameList);
        shoppingListSpinner.setAdapter(shoppingListsNameList);
        shoppingListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                shoppingListPosition = i;
                Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListIds.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            shoppingList = ds.getValue(ShoppingList.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utils.connectionError(CalendarActivity.this);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        layout.addView(shoppingListSpinner);

        final TextView txtStorages = new TextView(CalendarActivity.this);
        txtStorages.setText("Available storages");
        txtStorages.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(txtStorages);

        final Spinner storageSpinner = new Spinner(CalendarActivity.this);
        ArrayAdapter<String> storageNameList = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_1);
        getStorageLists(storageNameList);
        storageSpinner.setAdapter(storageNameList);
        storageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                storagePosition = i;
                Query query = storageReference.orderByChild("id").equalTo(storageListIds.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            storage = ds.getValue(Storage.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utils.connectionError(CalendarActivity.this);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        layout.addView(storageSpinner);

        final EditText inputName = new EditText(CalendarActivity.this);
        inputName.setHint("Name the shopping list");
        layout.addView(inputName);

        cbAddToExistingShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        // Set the layout
        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (checkValidString(inputAmountPortions.getText().toString())) {
                    int amountPortions = Integer.parseInt(inputAmountPortions.getText().toString());
                    if (cbAddToExistingShoppingList.isChecked()) {
                        addToExistingShoppingList(selectedItems, amountPortions);
                    } else {
                        if (Utils.checkValidString(inputName.getText().toString())) {
                            createNewShoppingList(selectedItems, amountPortions, inputName.getText().toString());
                        } else {
                            Utils.enterValidData(CalendarActivity.this);
                        }
                    }
                } else {
                    Utils.enterValidData(CalendarActivity.this);
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void addToExistingShoppingList(ArrayList<String> recipes, int amountPortions) {
        Query query = shoppingListReference.orderByChild("id").equalTo(shoppingListIds.get(shoppingListPosition));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (shoppingList.getProducts() == null) {
                    shoppingList.setProducts(new HashMap<>());
                }

                for (Recipe recipe : recipeList) {
                    if (recipes.contains(recipe.getName())) {
                        for (Map.Entry<String, StorageProduct> ingredient : recipe.getIngredients().entrySet()) {
                            if (shoppingList.getProducts().containsValue(ingredient.getValue())) {
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
                    }
                }

                shoppingListReference.child(shoppingList.getId())
                        .setValue(shoppingList)
                        .addOnCompleteListener(task ->
                                Toasty.success(CalendarActivity.this,
                                        "Ingredients added to shopping list "
                                                + shoppingList.getName()).show());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    private void createNewShoppingList(ArrayList<String> recipes, int amountPortions, String name) {
        Query query = storageReference.orderByChild("id").equalTo(storageListIds.get(storagePosition));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage storage = ds.getValue(Storage.class);

                    HashMap<String, StorageProduct> products = new HashMap<>();

                    for (Recipe recipe : recipeList) {
                        if (recipes.contains(recipe.getName())) {
                            for (Map.Entry<String, StorageProduct> product : recipe.getIngredients().entrySet()) {
                                products.put(product.getKey(), new StorageProduct(
                                        product.getValue().getAmount() * amountPortions,
                                        product.getValue().getName(),
                                        product.getValue().getUnitType()));
                            }
                        }
                    }

                    HashMap<String, Boolean> users = new HashMap<>();
                    for (Map.Entry<String, Boolean> user : storage.getUsers().entrySet()) {
                        users.put(user.getKey(), true);
                    }

                    String shoppingListId = UUID.randomUUID().toString();

                    ShoppingList shoppingList = new ShoppingList(products, users,
                            name, Utils.getCurrentTime(), shoppingListId,
                            storage.getId(), storage.getName());

                    shoppingListReference.child(shoppingList.getId()).setValue(shoppingList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toasty.success(CalendarActivity.this,
                                    "Ingredients added to shopping list " +
                                            name).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    private void getStorageLists(ArrayAdapter<String> arrayAdapter) {
        Query query = storageReference.orderByChild(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                arrayAdapter.clear();
                storageListIds.clear();
                // Get every shopping list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Storage st = ds.getValue(Storage.class);
                    if (st.getUsers() != null && st.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        arrayAdapter.add(st.getName());
                        storageListIds.add(st.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Fills the shopping lists list with all the shopping list of the from the users storages
     */
    private void getShoppingLists(ArrayAdapter<String> arrayAdapter) {
        Query query = shoppingListReference.orderByChild(FirebaseAuth.getInstance().getUid());
        // Set the database to get all shopping lists
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual lists
                arrayAdapter.clear();
                shoppingListIds.clear();
                // Get every shopping list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getUsers() != null && shoppingList.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        arrayAdapter.add(shoppingList.getName());
                        shoppingListIds.add(shoppingList.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
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
                recyclerAdapter.notifyDataSetChanged();
                btnAddProductsToShoppingList.setEnabled(false);

                // Get the recipes of the selected day
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);
                    for (String recipe : recipesDay.getRecipes()) {
                        Query queryRecipes = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe);
                        queryRecipes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                                    recipeList.add(recipe);
                                    selectedRecipesDay.getRecipes().add(recipe.getId());
                                }

                                recyclerAdapter.notifyDataSetChanged();
                                btnAddProductsToShoppingList.setEnabled(!(recipeList.isEmpty()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Utils.connectionError(CalendarActivity.this);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    private void setRecipe(View view) {
        viewHolder = (RecyclerView.ViewHolder) view.getTag();
        recipePosition = viewHolder.getAdapterPosition();
        recipe = recipeList.get(recipePosition);
    }
}