package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListRecyclerAdapter
        extends RecyclerView.Adapter<ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder>
        implements Filterable {
    // Fields
    private final List<ShoppingList> shoppingListList, shoppingListListFull;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    /**
     * Inner class of the recycler holder of this adapter
     */
    public static class ShoppingListRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final TextView txtEdited, txtName, txtStorageName;

        /**
         * Parameterized constructor
         *
         * @param itemView
         */
        public ShoppingListRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            txtName = itemView.findViewById(R.id.txtShoppingListName);
            txtEdited = itemView.findViewById(R.id.txtShoppingListLastEdited);
            txtStorageName = itemView.findViewById(R.id.txtShoppingListStorageName);
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     *
     * @param shoppingListList
     */
    public ShoppingListRecyclerAdapter(List<ShoppingList> shoppingListList) {
        this.shoppingListList = shoppingListList;
        this.shoppingListListFull = new ArrayList<>(shoppingListList);
    }

    // Getter
    /**
     * Get the filter. Filters the list by storage name or shopping list name
     */
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                // Generate the filtered list
                ArrayList<ShoppingList> filteredList = new ArrayList<>();

                // Check if the list is empty
                if (shoppingListListFull.size() == 0) {
                    // Fill the list
                    shoppingListListFull.addAll(shoppingListList);
                }

                // Check the introduced char sequence
                if (charSequence == null || charSequence.length() == 0) {
                    // Add all shopping lists
                    filteredList.addAll(shoppingListListFull);
                } else {
                    // Get the filter pattern
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    // Loop through the shopping list list
                    for (ShoppingList shoppingList : shoppingListListFull) {
                        // Check if the pattern matches one of the names
                        if (shoppingList.getName().toLowerCase().contains(filterPattern) ||
                                shoppingList.getStorageName().toLowerCase().contains(filterPattern)) {
                            // Add the shopping list to the filtered list
                            filteredList.add(shoppingList);
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
                shoppingListList.clear();

                // Add the results from the filter
                shoppingListList.addAll((List) filterResults.values);

                // Notify the recycler adapter
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return shoppingListList.size();
    }

    public List<ShoppingList> getShoppingListList() {
        return this.shoppingListList;
    }

    // Setter
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    /**
     * Clears both lists, the one with all the products and the one that contains the searched ones
     */
    public void clear(){
        this.shoppingListList.clear();
        this.shoppingListListFull.clear();
    }

    /**
     * Adds an item to both lists, the one with all the products and the one that contains the
     * searched ones
     */
    public void add(ShoppingList shoppingList){
        this.shoppingListList.add(shoppingList);
        this.shoppingListListFull.add(shoppingList);
    }

    @NonNull
    @Override
    public ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Get the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);

        // Set the holder
        ShoppingListRecyclerHolder recyclerHolder = new ShoppingListRecyclerHolder(view);

        // Set the listener
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder holder, int position) {
        // Get the shopping list
        ShoppingList shoppingList = shoppingListList.get(position);

        // Set the views
        holder.txtName.setText(shoppingList.getName());
        holder.txtEdited.setText(shoppingList.getLastEdited());
        holder.txtStorageName.setText(shoppingList.getStorageName());

        // Set the animation
        Utils.setFadeAnimation(holder.itemView);
    }
}
