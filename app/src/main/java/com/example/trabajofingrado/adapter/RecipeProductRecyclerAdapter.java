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
import com.example.trabajofingrado.model.RecipeProduct;

import java.util.ArrayList;
import java.util.List;

public class RecipeProductRecyclerAdapter
        extends RecyclerView.Adapter<RecipeProductRecyclerAdapter.RecipeProductRecyclerHolder>
        implements Filterable {
    // Fields
    private List<RecipeProduct> recipeProductList;

    private List<RecipeProduct> recipeProductListFull;

    private AdapterView.OnClickListener onClickListener;

    public RecipeProductRecyclerAdapter(List<RecipeProduct> recipeProductList){
        this.recipeProductList = recipeProductList;
        this.recipeProductListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecipeProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent,false);
        RecipeProductRecyclerHolder recyclerHolder = new RecipeProductRecyclerHolder(view);

        view.setOnClickListener(onClickListener);

        return recyclerHolder;
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public List<RecipeProduct> getProductList(){
        return this.recipeProductList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeProductRecyclerHolder holder, int position) {
        RecipeProduct recipeProduct = recipeProductList.get(position);
        holder.txtDescription.setText(recipeProduct.getDescription());
        holder.txtUnitType.setText(recipeProduct.getUnit_type());
        Glide.with(holder.itemView.getContext())
                .load(recipeProduct.getImage())
                .error(R.drawable.image_not_found)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return recipeProductList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<RecipeProduct> filteredList = new ArrayList<>();
            if(recipeProductListFull.size() == 0){
                recipeProductListFull.addAll(recipeProductList);
            }

            if(charSequence.length() == 0){
                filteredList.addAll(recipeProductListFull);
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(RecipeProduct recipeProduct : recipeProductListFull){
                    if(recipeProduct.getDescription().toLowerCase().contains(filterPattern)){
                        filteredList.add(recipeProduct);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            recipeProductList.clear();
            recipeProductList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };
    public class RecipeProductRecyclerHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtDescription;
        TextView txtUnitType;

        public RecipeProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgRecipeProductItem);
            txtDescription = itemView.findViewById(R.id.txtRecipeProductDescription);
            txtUnitType = itemView.findViewById(R.id.txtUnitType);
            itemView.setTag(this);
        }
    }
}
