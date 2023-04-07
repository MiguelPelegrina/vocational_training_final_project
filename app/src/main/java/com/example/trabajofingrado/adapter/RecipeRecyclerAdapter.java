package com.example.trabajofingrado.adapter;

import android.util.Log;
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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeRecyclerAdapter extends RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeRecyclerHolder> implements Filterable {
    // Class fields
    private static final int SIMPLE_FILTER = 0;
    private static final int OWN_RECIPE_FILTER = 1;
    private static final int AVALAILABLE_RECIPE_FILTER = 2;

    // Instance fields
    // List of recipes that will get filtered
    private List<Recipe> recipeList;
    // List of all recipes
    private List<Recipe> recipeListFull;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

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
        CircularProgressDrawable progressDrawable;
        progressDrawable = new CircularProgressDrawable(holder.itemView.getContext());
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.start();

        Recipe recipe = recipeList.get(position);
        holder.txtName.setText(recipe.getName());
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .placeholder(progressDrawable)
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
        return recipeFilter;
    }

    // TODO Anonym class --> bad design choice?
    private Filter recipeFilter = new Filter() {
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
        }
    };

    protected class RecipeRecyclerHolder extends RecyclerView.ViewHolder {
        // Atributos de la clase
        ImageView imgRecipe;
        TextView txtName;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public RecipeRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            imgRecipe = itemView.findViewById(R.id.imgRecipeList);
            txtName = itemView.findViewById(R.id.txtRecipeName);
            itemView.setTag(this);
        }
    }


}