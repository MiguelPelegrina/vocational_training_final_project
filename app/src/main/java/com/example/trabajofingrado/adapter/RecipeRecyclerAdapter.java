package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeRecyclerAdapter
        extends RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeRecyclerHolder>
        implements Filterable {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private final List<Recipe> recipeList, recipeListFull;

    /**
     * Inner class of the recycler holder of this adapter
     */
    protected static class RecipeRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final ImageView imgRecipe;
        private final TextView txtName;

        /**
         * Parameterized constructor
         * @param itemView
         */
        public RecipeRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            txtName = itemView.findViewById(R.id.txtRecipeName);
            imgRecipe = itemView.findViewById(R.id.imgRecipeItem);

            // Enable to set the border
            imgRecipe.setClipToOutline(true);

            // Set the tag
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     * @param recipeList
     */
    public RecipeRecyclerAdapter(List<Recipe> recipeList){
        this.recipeList = recipeList;
        this.recipeListFull = new ArrayList<>(recipeList);
    }

    // Getter
    /**
     * Get the filter. Filters the list by recipe name or product name
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                // Generate the filtered list
                ArrayList<Recipe> filteredList = new ArrayList<>();

                // Check if the list is empty
                if(recipeListFull.size() == 0){
                    // Fill the list
                    recipeListFull.addAll(recipeList);
                }

                // Check the introduced char sequence
                if(charSequence == null || charSequence.length() == 0){
                    // Add all recipes
                    filteredList.addAll(recipeListFull);
                }else{
                    // Get the filter pattern
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    // Loop through the recipe list
                    for(Recipe recipe : recipeListFull){
                        // Generate a flag
                        boolean containsIngredient = false;

                        // Loop through the products of the recipe
                        for(Map.Entry<String, StorageProduct> recipeEntry : recipe.getIngredients().entrySet()){
                            // Check if the pattern matches the name
                            if(recipeEntry.getKey().toLowerCase().contains(filterPattern)){
                                // Set the flag
                                containsIngredient = true;
                            }
                        }
                        // Check if the pattern matches the name
                        if(recipe.getName().toLowerCase().contains(filterPattern) || containsIngredient){
                            // Add the recipe to the filtered list
                            filteredList.add(recipe);
                        }
                    }
                }

                // Set the filter results
                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // Clear the list
                recipeList.clear();

                // Add the results from the filter
                recipeList.addAll((List) filterResults.values);

                // Notify the recycler adapter
                notifyDataSetChanged();
            };
        };
    }
    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    /**
     * Clears both lists, the one with all the recipes and the one that contains the searched ones
     */
    public void clear(){
        this.recipeList.clear();
        this.recipeListFull.clear();
    }

    /**
     * Adds an item to both lists, the one with all the recipes and the one that contains the
     * searched ones
     */
    public void add(Recipe recipe){
        this.recipeList.add(recipe);
        this.recipeListFull.add(recipe);
    }

    @NonNull
    @Override
    public RecipeRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Set the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_list_item, parent,false);

        // Get the holder
        RecipeRecyclerHolder recyclerHolder = new RecipeRecyclerHolder(view);

        // Set the listener
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeRecyclerHolder holder, int position) {
        // Get the recipe
        Recipe recipe = recipeList.get(position);

        // Set the views
        holder.txtName.setText(recipe.getName());
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .error(R.drawable.icon_image_not_found)
                .into(holder.imgRecipe);

        // Set the animation
        Utils.setFadeAnimation(holder.itemView);
    }
}
