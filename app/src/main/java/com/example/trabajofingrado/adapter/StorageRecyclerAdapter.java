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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageRecyclerAdapter
        extends RecyclerView.Adapter<StorageRecyclerAdapter.StorageRecyclerHolder>
        implements Filterable {
    // Fields
    // List of recipes that will get filtered
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private List<Storage> storageList, storageListFull;

    /**
     * Class constructor by parameters
     *
     * @param storageList
     */
    public StorageRecyclerAdapter(List<Storage> storageList) {
        this.storageList = storageList;
        this.storageListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public StorageRecyclerAdapter.StorageRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_list_item, parent, false);
        StorageRecyclerAdapter.StorageRecyclerHolder recyclerHolder = new StorageRecyclerAdapter.StorageRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StorageRecyclerAdapter.StorageRecyclerHolder holder, int position) {
        CircularProgressDrawable progressDrawable;
        progressDrawable = new CircularProgressDrawable(holder.itemView.getContext());
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.start();

        Storage storage = storageList.get(position);
        holder.txtName.setText(storage.getName());
        if (storage.getProducts() != null) {
            holder.txtAmountProducts.setText(storage.getProducts().size() + "");
        }else{
            holder.txtAmountProducts.setText(0 + "");
        }
        holder.txtAmountUser.setText(storage.getUsers().size() + "");
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return this.storageList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<Storage> filteredList = new ArrayList<>();
                if (storageListFull.size() == 0) {
                    storageListFull.addAll(storageList);
                }

                if (charSequence.length() == 0) {
                    filteredList.addAll(storageListFull);
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (Storage storage : storageListFull) {
                        boolean containsProduct = false;
                        if (storage.getProducts() != null) {
                            for (Map.Entry<String, StorageProduct> recipeEntry : storage.getProducts().entrySet()) {
                                if (recipeEntry.getValue().getName().toLowerCase().contains(filterPattern)) {
                                    containsProduct = true;
                                }
                            }
                        }
                        if (storage.getName().toLowerCase().contains(filterPattern) || containsProduct) {
                            filteredList.add(storage);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                storageList.clear();
                storageList.addAll((List) filterResults.values);
                notifyDataSetChanged();
            }

            ;
        };
    }

    protected class StorageRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        TextView txtAmountProducts, txtAmountUser, txtName;

        /**
         * Constructor por parámetros
         *
         * @param itemView Vista del layout
         */
        public StorageRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtStorageName);
            txtAmountProducts = itemView.findViewById(R.id.txtStorageProducts);
            txtAmountUser = itemView.findViewById(R.id.txtStorageUsers);

            itemView.setTag(this);
        }
    }
}
