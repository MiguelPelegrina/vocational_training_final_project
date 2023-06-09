package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class StorageProductRecyclerAdapter
        extends RecyclerView.Adapter<StorageProductRecyclerAdapter.StorageProductRecyclerHolder>
        implements Filterable {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private final List<StorageProduct> storageProductList, storageProductListFull;

    /**
     * Inner class of the recycler holder of this adapter
     */
    protected static class StorageProductRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final TextView txtAmount, txtName;

        /**
         * Parameterized constructor
         *
         * @param itemView
         */
        public StorageProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            txtName = itemView.findViewById(R.id.txtStorageProductName);
            txtAmount = itemView.findViewById(R.id.txtStorageProductAmount);
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     *
     * @param storageProductList
     */
    public StorageProductRecyclerAdapter(List<StorageProduct> storageProductList) {
        this.storageProductList = storageProductList;
        this.storageProductListFull = new ArrayList<>();
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
                List<StorageProduct> filteredList = new ArrayList<>();

                // Check if the list is empty
                if (storageProductListFull.size() == 0) {
                    // Fill the list
                    storageProductListFull.addAll(storageProductList);
                }

                // Check the introduced char sequence
                if (charSequence.length() == 0) {
                    // Add all products
                    filteredList.addAll(storageProductListFull);
                } else {
                    // Get the filter pattern
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    // Loop through the product list
                    for (StorageProduct product : storageProductListFull) {
                        // Check if the pattern matches the name
                        if (product.getName().toLowerCase().contains(filterPattern)) {
                            // Add the product to the filtered list
                            filteredList.add(product);
                        }
                    }
                }

                // Set the filter results
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // Clear the list
                storageProductList.clear();

                // Add the results from the filter
                storageProductList.addAll((List) filterResults.values);

                // Notify the recycler adapter
                notifyDataSetChanged();
            }

            ;
        };
    }

    @Override
    public int getItemCount() {
        return this.storageProductList.size();
    }

    public List<StorageProduct> getProductList() {
        return this.storageProductList;
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    @NonNull
    @Override
    public StorageProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Get the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_product_list_item, parent, false);

        // Set the holder
        StorageProductRecyclerHolder recyclerHolder = new StorageProductRecyclerHolder(view);

        // Set the listener
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StorageProductRecyclerHolder holder, int position) {
        StorageProduct storageProduct = storageProductList.get(position);
        holder.txtAmount.setText(storageProduct.getAmount() + " " + storageProduct.getUnitType());
        holder.txtName.setText(storageProduct.getName());

        Utils.setFadeAnimation(holder.itemView);
    }


}
