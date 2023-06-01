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
    private boolean bought;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private List<StorageProduct> shoppingListProducts;
    private RecyclerViewActionListener listener;

    /**
     * Default constructor
     *
     * @param shoppingListProducts
     * @param listener
     */
    public ShoppingListProductRecyclerAdapter(List<StorageProduct> shoppingListProducts, RecyclerViewActionListener listener, boolean bought){
        this.shoppingListProducts = shoppingListProducts;
        this.listener = listener;
        this.bought = bought;
    }

    @NonNull
    @Override
    public ShoppingListProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_product_item, parent, false);
        ShoppingListProductRecyclerHolder holder = new ShoppingListProductRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        holder.cbProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        holder.txtDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewClicked(view.getId(), holder.getAdapterPosition());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListProductRecyclerHolder holder, int position) {
        StorageProduct product = shoppingListProducts.get(position);
        holder.txtName.setText(product.getName());
        holder.txtAmount.setText(product.getAmount() + "");

        holder.cbProduct.setChecked(bought);

        if(holder.cbProduct.isChecked()){
            holder.txtName.setPaintFlags(holder.txtName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtAmount.setPaintFlags(holder.txtAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            holder.txtName.setPaintFlags(holder.txtName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtAmount.setPaintFlags(holder.txtAmount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        Utils.setFadeAnimation(holder.itemView);
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return this.shoppingListProducts.size();
    }

    protected class ShoppingListProductRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        CheckBox cbProduct;
        TextView txtAmount, txtName, txtDeleteProduct;

        public ShoppingListProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Bind views
            cbProduct = itemView.findViewById(R.id.cbProduct);
            txtName = itemView.findViewById(R.id.txtShoppingListProductName);
            txtAmount = itemView.findViewById(R.id.txtShoppingListProductAmount);
            txtDeleteProduct = itemView.findViewById(R.id.txtDeleteShoppingListProduct);
            itemView.setTag(this);
        }
    }
}
