package com.example.trabajofingrado.controller;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ViewPagerAdapter;
import com.example.trabajofingrado.fragments.RecipeListFragment;
import com.example.trabajofingrado.fragments.StorageListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// TODO CHANGE TO VIEWPAGER2
public class MainActivity extends AppCompatActivity {
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.createFragment(0);
        viewPagerAdapter.createFragment(1);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout,viewPager,(tab, position) -> {
            switch (position){
                case 0:
                    tab.setText(R.string.recipes);
                    break;
                case 1:
                    tab.setText(R.string.storages);
                    break;
            }
        }).attach();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }
}

