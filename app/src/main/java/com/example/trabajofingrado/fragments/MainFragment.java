package com.example.trabajofingrado.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class MainFragment extends Fragment {
    // Fields
    private View view;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        viewPagerAdapter = new ViewPagerAdapter((FragmentActivity) view.getContext());
        viewPagerAdapter.createFragment(0);
        viewPagerAdapter.createFragment(1);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
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
                requireActivity().moveTaskToBack(true);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return view;
    }
}