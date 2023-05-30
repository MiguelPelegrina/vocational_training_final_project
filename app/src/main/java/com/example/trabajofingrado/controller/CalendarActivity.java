package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.R.id.menu_item_delete_recipe;
import static com.example.trabajofingrado.R.id.menu_item_modify_recipe;
import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.dateToEpoch;
import static com.example.trabajofingrado.utilities.Utils.epochToDateTime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import java.net.Inet4Address;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CalendarActivity extends BaseActivity {
    // Fields
    private int position;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private Button btnAddRecipe;
    //private CalendarView calendarView;
    private CollapsibleCalendar collapsibleCalendar;
    //private List<EventDay> events = new ArrayList<>();
    //private List<Calendar> calendars = new ArrayList<>();
    private Recipe recipe;
    private RecipesDay selectedRecipesDay;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_calendar);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();


        setCookingDays();
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
                    Log.d("Date", time.getDayOfMonth() + "/" + time.getMonthValue() + "/" + time.getYear());

                    collapsibleCalendar.addEventTag(time.getYear(),time.getMonthValue() - 1, time.getDayOfMonth());

                    /*Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(recipesDay.getDate());
                    calendars.add(calendar);
                    try {
                        calendarView.setDate(calendar);
                    } catch (OutOfDateRangeException e) {
                        throw new RuntimeException(e);
                    }*/
                    //events.add(new EventDay(calendar, R.drawable.steaming_pot));
                }
                //calendarView.setEvents(events);
                //calendarView.setSelectedDates(calendars);
                /*calendarView.setHighlightedDays(calendars);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                try {
                    calendarView.setDate(calendar);
                } catch (OutOfDateRangeException e) {
                    throw new RuntimeException(e);
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        btnAddRecipe = findViewById(R.id.btnAddRecipeCalendar);
        //calendarView = findViewById(R.id.calendarView);
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
        /*calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                try {
                    calendarView.setDate(clickedDayCalendar);
                } catch (OutOfDateRangeException e) {
                    throw new RuntimeException(e);
                }

                btnAddRecipe.setVisibility(View.VISIBLE);
                selectedRecipesDay = new RecipesDay(clickedDayCalendar.getTimeInMillis(), new ArrayList<>());
                fillRecipesList(selectedRecipesDay.getDate());
            }
        });*/

        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CalendarActivity.this, RecipeListActivity.class);
                intent.putExtra("recipesDayDate", selectedRecipesDay.getDate());
                intent.putExtra("recipesSize", selectedRecipesDay.getRecipes().size());
                startActivity(intent);
            }
        });

        recyclerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRecipe(view);

                Utils.moveToRecipeDetails(CalendarActivity.this, recipe);
            }
        });

        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setRecipe(view);
                registerForContextMenu(recyclerView);

                return false;
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