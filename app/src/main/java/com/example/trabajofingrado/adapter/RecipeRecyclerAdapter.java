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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeRecyclerAdapter
        extends RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeRecyclerHolder>
        implements Filterable {
    // Fields
    // List of recipes that will get filtered
    private List<Recipe> recipeList;
    // List of all recipes
    private List<Recipe> recipeListFull;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    /**
     * Class constructor by parameters
     * @param recipeList
     */
    public RecipeRecyclerAdapter(List<Recipe> recipeList){
        this.recipeList = recipeList;
        this.recipeListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecipeRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_list_item, parent,false);
        RecipeRecyclerHolder recyclerHolder = new RecipeRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeRecyclerHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.txtName.setText(recipe.getName());
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .error(R.drawable.image_not_found)
                .into(holder.imgRecipe);
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
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
                notifyDataSetChanged();
            };
        };
    }

    protected class RecipeRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        ImageView imgRecipe;
        TextView txtName;

        public RecipeRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Bind views
            imgRecipe = itemView.findViewById(R.id.imgRecipeItem);
            txtName = itemView.findViewById(R.id.txtRecipeName);
            itemView.setTag(this);
        }
    }


}
