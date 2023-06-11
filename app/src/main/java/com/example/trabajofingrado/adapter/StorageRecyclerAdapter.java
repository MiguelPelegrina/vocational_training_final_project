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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageRecyclerAdapter
        extends RecyclerView.Adapter<StorageRecyclerAdapter.StorageRecyclerHolder>
        implements Filterable {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private final List<Storage> storageList, storageListFull;

    /**
     * Inner class of the recycler holder of this adapter
     */
    protected static class StorageRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final TextView txtAmountProducts, txtAmountShoppingLists, txtName;

        /**
         * Parameterized constructor
         * @param itemView
         */
        public StorageRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            txtName = itemView.findViewById(R.id.txtStorageName);
            txtAmountProducts = itemView.findViewById(R.id.txtStorageProducts);
            txtAmountShoppingLists = itemView.findViewById(R.id.txtStorageShoppingLists);
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     * @param storageList
     */
    public StorageRecyclerAdapter(List<Storage> storageList) {
        this.storageList = storageList;
        this.storageListFull = new ArrayList<>(storageList);
    }

    // Getter
    /**
     * Get the filter. Filters the list by storage name and product name
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                // Generate the filtered list
                ArrayList<Storage> filteredList = new ArrayList<>();

                // Check if the list is empty
                if (storageListFull.size() == 0) {
                    // Fill the list
                    storageListFull.addAll(storageList);
                }

                // Check the introduced char sequence
                if (charSequence == null ||  charSequence.length() == 0) {
                    // Add all storages
                    filteredList.addAll(storageListFull);
                } else {
                    // Get the filter pattern
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    // Loop through the storage list
                    for (Storage storage : storageListFull) {
                        // Generate a flag
                        boolean containsProduct = false;

                        // Check if there are any products
                        if (storage.getProducts() != null) {
                            // Loop through the product list
                            for (Map.Entry<String, StorageProduct> recipeEntry : storage.getProducts().entrySet()) {
                                // Check if the pattern matches the product name
                                if (recipeEntry.getValue().getName().toLowerCase().contains(filterPattern)) {
                                    // Set the flag
                                    containsProduct = true;
                                }
                            }
                        }
                        // Check if the pattern matches the storage name
                        if (storage.getName().toLowerCase().contains(filterPattern) || containsProduct) {
                            // Add the storage to the filtered list
                            filteredList.add(storage);
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
                storageList.clear();

                // Add the results from the filter
                storageList.addAll((List) filterResults.values);

                // Notify the recycler adapter
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return this.storageList.size();
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    /**
     * Clears both lists, the one with all the storages and the one that contains the searched ones
     */
    public void clear(){
        this.storageList.clear();
        this.storageListFull.clear();
    }

    /**
     * Adds an item to both lists, the one with all the storages and the one that contains the
     * searched ones
     */
    public void add(Storage storage){
        this.storageList.add(storage);
        this.storageListFull.add(storage);
    }

    @NonNull
    @Override
    public StorageRecyclerAdapter.StorageRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Get the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_list_item, parent, false);

        // Set the holder
        StorageRecyclerAdapter.StorageRecyclerHolder recyclerHolder = new StorageRecyclerHolder(view);

        // Set the listener
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StorageRecyclerAdapter.StorageRecyclerHolder holder, int position) {
        // Get the storage
        Storage storage = storageList.get(position);

        // Set the views
        holder.txtName.setText(storage.getName());
        // Check the amount of products. If there are none, set it to zero.
        holder.txtAmountProducts.setText((storage.getProducts() != null) ? storage.getProducts().size() + "" : 0 + "");
        // Check the amount of shopping lists. If there are none, set it to zero.
        holder.txtAmountShoppingLists.setText((storage.getShoppingLists() != null) ? storage.getShoppingLists().size() + "" : 0 + "");

        // Set the animation
        Utils.setFadeAnimation(holder.itemView);
    }
}
