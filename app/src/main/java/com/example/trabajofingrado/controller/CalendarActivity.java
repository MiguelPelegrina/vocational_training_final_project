package com.example.trabajofingrado.controller;


import android.os.Bundle;
import android.util.Log;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.RecipesDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CalendarActivity extends BaseActivity {
    private CalendarView calendarView;
    private List<EventDay> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_calendar);


        setListener();


        setCookingDays();
    }

    private void setCookingDays() {
        events.add(new RecipesDay(new GregorianCalendar(2023, 5, 25)));
        events.add(new RecipesDay(new GregorianCalendar(2023, 5, 26)));
        calendarView.setEvents(events);
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        calendarView = findViewById(R.id.calendarView);
        drawerLayout = findViewById(R.id.drawer_layout_calendar);
        toolbar = findViewById(R.id.toolbar_product_list);
    }

    private void setListener() {
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                Log.d("calendar", clickedDayCalendar + "");
                events.add(new RecipesDay(clickedDayCalendar));
                calendarView.setEvents(events);
            }
        });
    }
}