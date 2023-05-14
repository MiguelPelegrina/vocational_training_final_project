package com.example.trabajofingrado.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.ShoppingList;
import com.example.trabajofingrado.model.Storage;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ShoppingListRecyclerAdapter extends RecyclerView.Adapter<ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder>{
    private List<ShoppingList> shoppingListList;
    private View.OnClickListener onClickListener;

    public ShoppingListRecyclerAdapter(List<ShoppingList> shoppingListList) {
        this.shoppingListList = shoppingListList;
    }

    @NonNull
    @Override
    public ShoppingListRecyclerAdapter.ShoppingListRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);
        ShoppingListRecyclerHolder recyclerHolder = new ShoppingListRecyclerHolder(view);

        view.setOnClickListener(onClickListener);

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

    public List<ShoppingList> getShoppingListList() { return this.shoppingListList; }

    public class ShoppingListRecyclerHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtEdited;

        TextView txtStorageName;

        public ShoppingListRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtShoppingListName);
            txtEdited = itemView.findViewById(R.id.txtShoppingListEdited);
            txtStorageName = itemView.findViewById(R.id.txtShoppingListStorageName);
            itemView.setTag(this);
        }
    }
}
