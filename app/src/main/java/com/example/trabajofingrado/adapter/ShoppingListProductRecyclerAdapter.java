package com.example.trabajofingrado.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.interfaces.RecyclerViewActionListener;
import com.example.trabajofingrado.model.StorageProduct;
import com.example.trabajofingrado.utilities.Utils;

import java.util.List;

public class ShoppingListProductRecyclerAdapter
        extends RecyclerView.Adapter<ShoppingListProductRecyclerAdapter.ShoppingListProductRecyclerHolder> {
    // Fields
    private final boolean bought;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private final List<StorageProduct> shoppingListProducts;
    private final RecyclerViewActionListener actionListener;

    /**
     * Inner class of the recycler holder of this adapter
     */
    protected static class ShoppingListProductRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final CheckBox cbProduct;
        private final TextView txtAmount, txtDeleteProduct, txtName;

        /**
         * Parameterized constructor
         * @param itemView
         */
        public ShoppingListProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the views
            cbProduct = itemView.findViewById(R.id.cbProduct);
            txtName = itemView.findViewById(R.id.txtShoppingListProductName);
            txtAmount = itemView.findViewById(R.id.txtShoppingListProductAmount);
            txtDeleteProduct = itemView.findViewById(R.id.txtDeleteShoppingListProduct);
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     * @param shoppingListProducts
     * @param actionListener
     */
    public ShoppingListProductRecyclerAdapter(List<StorageProduct> shoppingListProducts, RecyclerViewActionListener actionListener, boolean bought){
        this.shoppingListProducts = shoppingListProducts;
        this.actionListener = actionListener;
        this.bought = bought;
    }

    // Getter
    @Override
    public int getItemCount() {
        return this.shoppingListProducts.size();
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @NonNull
    @Override
    public ShoppingListProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Set the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_product_item, parent, false);

        // Get the holder
        ShoppingListProductRecyclerHolder holder = new ShoppingListProductRecyclerHolder(view);

        // Set the listener of the holder
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        // Set the listener of specific views of the holder
        holder.cbProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListProductRecyclerHolder holder, int position) {
        // Get the view
        StorageProduct product = shoppingListProducts.get(position);

        // Set the views
        holder.txtName.setText(product.getName());
        holder.txtAmount.setText(product.getAmount() + "");
        holder.cbProduct.setChecked(bought);

        // Check if the item is marked
        if(holder.cbProduct.isChecked()){
            // Strike through the text
            holder.txtName.setPaintFlags(holder.txtName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtAmount.setPaintFlags(holder.txtAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            // Remove the struck through text
            holder.txtName.setPaintFlags(holder.txtName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtAmount.setPaintFlags(holder.txtAmount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
}
