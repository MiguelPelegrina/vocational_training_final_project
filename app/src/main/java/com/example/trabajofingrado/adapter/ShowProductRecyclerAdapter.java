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

public class ShowProductRecyclerAdapter
        extends RecyclerView.Adapter<ShowProductRecyclerAdapter.RecipeProductRecyclerHolder>
        implements Filterable {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private final List<ShowProduct> productList, productListFull;

    /**
     * Inner class of the recycler holder of this adapter
     */
    public static class RecipeProductRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final ImageView imgProduct;
        private final TextView txtName, txtUnitType;

        /**
         * Parameterized constructor
         * @param itemView
         */
        public RecipeProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            txtName = itemView.findViewById(R.id.txtRecipeProductName);
            txtUnitType = itemView.findViewById(R.id.txtUnitType);
            imgProduct = itemView.findViewById(R.id.imgRecipeProductItem);

            // Enable to change the border
            imgProduct.setClipToOutline(true);

            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     *
     * @param productList
     */
    public ShowProductRecyclerAdapter(List<ShowProduct> productList) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
    }

    // Getter
    /**
     * Get the filter. Filters the list by product name
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                // Generate the filtered list
                ArrayList<ShowProduct> filteredList = new ArrayList<>();

                // Check if the list is empty
                if (productListFull.size() == 0) {
                    // Fill the list
                    productListFull.addAll(productList);
                }

                // Check the introduced char sequence
                if (charSequence == null || charSequence.length() == 0) {
                    // Add all products
                    filteredList.addAll(productListFull);
                } else {
                    // Get the filter pattern
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    // Loop through the product list
                    for (ShowProduct product : productListFull) {
                        // Check if the pattern matches the name
                        if (product.getName().toLowerCase().contains(filterPattern)) {
                            // Add the product to the filtered list
                            filteredList.add(product);
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
                productList.clear();

                // Add the results from the filter
                productList.addAll((List) filterResults.values);

                // Notify the recycler adapter
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public List<ShowProduct> getProductList() {
        return this.productList;
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * Clears both lists, the one with all the products and the one that contains the searched ones
     */
    public void clear(){
        this.productList.clear();
        this.productListFull.clear();
    }

    /**
     * Adds an item to both lists, the one with all the products and the one that contains the
     * searched ones
     */
    public void add(ShowProduct product){
        this.productList.add(product);
        this.productListFull.add(product);
    }

    @NonNull
    @Override
    public RecipeProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Set the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);

        // Get the holder
        RecipeProductRecyclerHolder recyclerHolder = new RecipeProductRecyclerHolder(view);

        // Set listener
        view.setOnClickListener(onClickListener);

        return recyclerHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecipeProductRecyclerHolder holder, int position) {
        // Get the product
        ShowProduct product = productList.get(position);

        // Set the views
        holder.txtName.setText(product.getName());
        holder.txtUnitType.setText(product.getUnitType());
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .error(R.drawable.icon_image_not_found)
                .into(holder.imgProduct);

        // Set the animation
        Utils.setFadeAnimation(holder.itemView);
    }
}
