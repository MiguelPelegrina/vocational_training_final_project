package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Recipe;

import java.util.List;

public class RecipeRecyclerAdapter extends RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeRecyclerHolder> {
    private List<Recipe> recipeList;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    public RecipeRecyclerAdapter(List<Recipe> listaPersonajes){
        this.recipeList = listaPersonajes;
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

    public class RecipeRecyclerHolder extends RecyclerView.ViewHolder {
        // Atributos de la clase
        ImageView imgRecipe;
        TextView txtName;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public RecipeRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Inicialización de los atributos
            imgRecipe = (ImageView) itemView.findViewById(R.id.imgRecipe);
            txtName = (TextView) itemView.findViewById(R.id.txtRecipeName);
            // Asignamos un tag para posteriormente poder identificar el itemView en la actividad para
            // la creacion de los oyentes
            itemView.setTag(this);
        }
    }
}
