package com.example.trabajofingrado.filter;

import android.widget.Filter;

import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeFilter extends Filter {
    private static final int SIMPLE_FILTER = 0;
    private static final int OWN_RECIPE_FILTER = 1;
    private static final int AVALAILABLE_RECIPE_FILTER = 2;

    private int filterType;
    private RecipeRecyclerAdapter recipeRecyclerAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> recipeListFull;

    public RecipeFilter(int filterType, RecipeRecyclerAdapter recipeRecyclerAdapter, List<Recipe> recipeList) {
        this.filterType = filterType;
        this.recipeRecyclerAdapter = recipeRecyclerAdapter;
        this.recipeList = recipeList;
        this.recipeListFull = recipeList;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        List<Recipe> filteredList = new ArrayList<>();
        if(recipeListFull.size() == 0){
            recipeListFull.addAll(recipeList);
        }

        if(charSequence.length() == 0){
            filteredList.addAll(recipeListFull);
        }else{
            String filterPattern = charSequence.toString().toLowerCase().trim();
            for(Recipe recipe : recipeListFull){
                boolean containsIngredient = false;
                for(Map.Entry<String, String> recipeEntry : recipe.getIngredients().entrySet()){
                    if(recipeEntry.getKey().toLowerCase().contains(filterPattern)){
                        containsIngredient = true;
                    }
                }
                if(recipe.getName().toLowerCase().contains(filterPattern) || containsIngredient){
                    filteredList.add(recipe);

                }
            }
        }

        FilterResults filterResults = new FilterResults();
        filterResults.values = filteredList;

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        recipeList.clear();
        recipeList.addAll((List) filterResults.values);
        this.recipeRecyclerAdapter.notifyDataSetChanged();
    }
}
