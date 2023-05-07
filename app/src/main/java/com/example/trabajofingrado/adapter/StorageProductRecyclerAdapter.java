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

import java.util.ArrayList;
import java.util.List;

public class StorageProductRecyclerAdapter
        extends RecyclerView.Adapter<StorageProductRecyclerAdapter.StorageProductRecyclerHolder>
        implements Filterable {
    // Fields
    // List of recipes that will get filtered
    private List<StorageProduct> storageProductList;
    // List of all products
    private List<StorageProduct> storageProductListFull;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    public StorageProductRecyclerAdapter(List<StorageProduct> storageProductList) {
        this.storageProductList = storageProductList;
        this.storageProductListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public StorageProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_product_list_item, parent,false);
        StorageProductRecyclerHolder recyclerHolder = new StorageProductRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StorageProductRecyclerHolder holder, int position) {
        StorageProduct storageProduct = storageProductList.get(position);
        holder.txtAmount.setText(storageProduct.getAmount() + " " + storageProduct.getUnitType());
        holder.txtName.setText(storageProduct.getName());
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    public List<StorageProduct> getProductList(){
        return this.storageProductList;
    }

    @Override
    public int getItemCount() {
        return this.storageProductList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<StorageProduct> filteredList = new ArrayList<>();
                if(storageProductListFull.size() == 0){
                    storageProductListFull.addAll(storageProductList);
                }

                if(charSequence.length() == 0){
                    filteredList.addAll(storageProductListFull);
                }else{
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for(StorageProduct product : storageProductListFull){
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
                storageProductList.clear();
                storageProductList.addAll((List) filterResults.values);
                notifyDataSetChanged();
            };
        };
    }

    protected class StorageProductRecyclerHolder extends RecyclerView.ViewHolder {
        TextView txtAmount;
        TextView txtName;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public StorageProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtStorageProductName);
            txtAmount = itemView.findViewById(R.id.txtStorageProductAmount);
            itemView.setTag(this);
        }
    }
}
