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

import java.util.ArrayList;
import java.util.List;

public class ShoppingListRecyclerAdapter
        extends RecyclerView.Adapter<ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder>
        implements Filterable {
    private List<ShoppingList> shoppingListList, shoppingListListFull;
    private View.OnClickListener onClickListener;

    private View.OnLongClickListener onLongClickListener;

    public ShoppingListRecyclerAdapter(List<ShoppingList> shoppingListList) {
        this.shoppingListList = shoppingListList;
        this.shoppingListListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);
        ShoppingListRecyclerHolder recyclerHolder = new ShoppingListRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder holder, int position) {
        ShoppingList shoppingList = shoppingListList.get(position);
        holder.txtName.setText(shoppingList.getName());
        holder.txtEdited.setText(shoppingList.getLastEdited());
        holder.txtStorageName.setText(shoppingList.getStorageName());
    }

    @Override
    public int getItemCount() {
        return shoppingListList.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public List<ShoppingList> getShoppingListList() {
        return this.shoppingListList;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<ShoppingList> filteredList = new ArrayList<>();
                if (shoppingListListFull.size() == 0) {
                    shoppingListListFull.addAll(shoppingListList);
                }

                if (charSequence.length() == 0) {
                    filteredList.addAll(shoppingListListFull);
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (ShoppingList shoppingList : shoppingListListFull) {
                        if (shoppingList.getName().toLowerCase().contains(filterPattern) ||
                                shoppingList.getStorageName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(shoppingList);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                shoppingListList.clear();
                shoppingListList.addAll((List) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    public class ShoppingListRecyclerHolder extends RecyclerView.ViewHolder {
        TextView txtEdited, txtName, txtStorageName;

        public ShoppingListRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtShoppingListName);
            txtEdited = itemView.findViewById(R.id.txtShoppingListLastEdited);
            txtStorageName = itemView.findViewById(R.id.txtShoppingListStorageName);
            itemView.setTag(this);
        }
    }
}
