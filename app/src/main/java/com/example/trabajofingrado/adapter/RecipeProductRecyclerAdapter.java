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
import com.example.trabajofingrado.model.ShowProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class RecipeProductRecyclerAdapter
        extends RecyclerView.Adapter<RecipeProductRecyclerAdapter.RecipeProductRecyclerHolder>
        implements Filterable {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private List<ShowProduct> productList, productListFull;

    public RecipeProductRecyclerAdapter(List<ShowProduct> productList){
        this.productList = productList;
        this.productListFull = new ArrayList<>();
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

    public List<ShowProduct> getProductList(){
        return this.productList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeProductRecyclerHolder holder, int position) {
        ShowProduct product = productList.get(position);
        holder.txtName.setText(product.getName());
        holder.txtUnitType.setText(product.getUnitType());
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .error(R.drawable.image_not_found)
                .into(holder.imgProduct);

        Utils.setFadeAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<ShowProduct> filteredList = new ArrayList<>();
                if(productListFull.size() == 0){
                    productListFull.addAll(productList);
                }

                if(charSequence.length() == 0){
                    filteredList.addAll(productListFull);
                }else{
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for(ShowProduct product : productListFull){
                        if(product.getName().toLowerCase().contains(filterPattern)){
                            filteredList.add(product);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productList.clear();
                productList.addAll((List) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    public class RecipeProductRecyclerHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtUnitType;

        public RecipeProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgRecipeProductItem);
            imgProduct.setClipToOutline(true);
            txtName = itemView.findViewById(R.id.txtRecipeProductName);
            txtUnitType = itemView.findViewById(R.id.txtUnitType);
            itemView.setTag(this);
        }
    }
}
