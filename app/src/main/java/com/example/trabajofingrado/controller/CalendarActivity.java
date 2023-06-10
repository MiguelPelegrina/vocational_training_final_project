package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.id.menu_item_delete_recipe;
import static com.example.trabajofingrado.R.id.menu_item_modify_recipe;
import static com.example.trabajofingrado.utilities.ShoppingListInputDialogs.shoppingListReference;
import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.dateToEpoch;
import static com.example.trabajofingrado.utilities.Utils.epochToDateTime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.model.ShoppingList;
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

public class CalendarActivity extends BaseActivity {
    // Fields
    private int position;
    private final ArrayList<Recipe> recipeList = new ArrayList<>();
    private Button btnAddRecipe, btnAddProductsToShoppingList;
    private CollapsibleCalendar collapsibleCalendar;
    private Recipe recipe;
    private RecipesDay selectedRecipesDay;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO SELECT THE LAST ADD OR MODIFIED DAY
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.rvCalendarRecipes) {
            getMenuInflater().inflate(R.menu.recipe_detail_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case menu_item_modify_recipe:
                // TODO --> SEND TO RECIPE LIST ACTIVITY

                break;
            case menu_item_delete_recipe:
                recipeList.remove(position);
                selectedRecipesDay.getRecipes().remove(position);
                deleteRecipeFromDay();
                break;
        }

        return true;
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
                Log.d("day", day.getDay() + "" + day.getMonth() + day.getYear());

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
            Intent intent = new Intent(CalendarActivity.this, RecipeListActivity.class);
            intent.putExtra("recipesDayDate", selectedRecipesDay.getDate());
            intent.putExtra("recipesSize", selectedRecipesDay.getRecipes().size());
            startActivity(intent);
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
            setRecipe(view);
            registerForContextMenu(recyclerView);

            return false;
        });
    }

    private AlertDialog createRecipesToShoppingListDialog() {
        // Generate the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);

        // Generate the layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set the title
        builder.setTitle("Choose the recipes you want to add");

        // Generate the recipe name list
        ArrayList<String> recipeNameList = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            recipeNameList.add(recipe.getName());
        }

        // Generate the adapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CalendarActivity.this, android.R.layout.simple_list_item_multiple_choice, recipeNameList);

        // Generate the list view
        final ListView listView = new ListView(CalendarActivity.this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);

        // Add the list view to the layout
        layout.addView(listView);

        // Set all items as checked by default
        for (int i = 0; i < recipeNameList.size(); i++) {
            listView.setItemChecked(i, true);
        }

        // Set a check box to know if we user wants to add the products to an existing shopping list
        final CheckBox cbAddToExistingShoppingList = new CheckBox(CalendarActivity.this);
        cbAddToExistingShoppingList.setText(R.string.add_to_an_existing_shopping_list);
        cbAddToExistingShoppingList.setChecked(false);

        // Add the check box to the layout
        layout.addView(cbAddToExistingShoppingList);

        final Spinner spinner = new Spinner(CalendarActivity.this);
        spinner.setVisibility(View.GONE);
        ArrayAdapter<String> shoppingListsNameList = new ArrayAdapter<String>(CalendarActivity.this, android.R.layout.simple_list_item_1);
        getShoppingLists(shoppingListsNameList);
        spinner.setAdapter(shoppingListsNameList);
        layout.addView(spinner);

        final EditText editText = new EditText(CalendarActivity.this);
        editText.setHint("Name the shopping list");
        layout.addView(editText);

        cbAddToExistingShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbAddToExistingShoppingList.isChecked()) {
                    spinner.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.GONE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            }
        });

        // Set the layout
        builder.setView(layout);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (cbAddToExistingShoppingList.isChecked()) {
                    // TODO ADD TO THE EXISTING SHOPPING LIST

                } else {
                    // TODO CREATE A NEW SHOPPING LIST
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
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
                // Clear the actual list
                arrayAdapter.clear();
                // Get every shopping list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);
                    if (shoppingList.getUsers() != null && shoppingList.getUsers().containsKey(FirebaseAuth.getInstance().getUid())) {
                        arrayAdapter.add(shoppingList.getName());
                    }
                }
                recyclerAdapter.notifyDataSetChanged();
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
        position = viewHolder.getAdapterPosition();
        recipe = recipeList.get(position);
    }
}