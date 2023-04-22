package com.example.trabajofingrado.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.trabajofingrado.fragments.RecipeListFragment;
import com.example.trabajofingrado.fragments.StorageListFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    /**
     * Class constructor
     * @param fragmentActivity Fragment activity that will show the view pager object
     */
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Method to create a fragment depending on the position that you establish.
     * @param position Position in the view pager.
     * @return Returns a fragment if the position is valid, otherwise it will return null.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new RecipeListFragment();
                break;
            case 1:
                fragment = new StorageListFragment();
                break;
        }
        return fragment;
    }

    /**
     * Method to get the number of fragments that will be shown in the view pager.
     * @return Number of fragments inside the view pager.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
