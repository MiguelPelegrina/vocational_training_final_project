package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.StorageProduct;

import java.util.List;

public class StorageProductRecyclerAdapter extends RecyclerView.Adapter<StorageProductRecyclerAdapter.StorageProductRecyclerHolder>{
    private List<StorageProduct> storageProductList;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    public StorageProductRecyclerAdapter(List<StorageProduct> storageProductList) {
        this.storageProductList = storageProductList;
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
        holder.txtName.setText(storageProduct.getDescription());
        holder.txtAmount.setText(storageProduct.getAmount());
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
