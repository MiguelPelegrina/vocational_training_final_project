package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Storage;

import java.util.List;

public class StorageRecyclerAdapter extends RecyclerView.Adapter<StorageRecyclerAdapter.StorageRecyclerHolder>{
    private List<Storage> storageList;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    public StorageRecyclerAdapter(List<Storage> storageList){
        this.storageList = storageList;
    }


    @NonNull
    @Override
    public StorageRecyclerAdapter.StorageRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_list_item, parent,false);
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
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return this.storageList.size();
    }

    protected class StorageRecyclerHolder extends RecyclerView.ViewHolder {
        // Atributos de la clase
        TextView txtName;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public StorageRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtName = (TextView) itemView.findViewById(R.id.txtStorageName);

            itemView.setTag(this);
        }
    }
}
