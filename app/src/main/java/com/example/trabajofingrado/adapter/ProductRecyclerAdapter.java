package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.model.Product;

import java.util.List;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ProductRecyclerHolder>{
    private List<Product> productList;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    public ProductRecyclerAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent,false);
        ProductRecyclerAdapter.ProductRecyclerHolder recyclerHolder = new ProductRecyclerAdapter.ProductRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductRecyclerHolder holder, int position) {
        CircularProgressDrawable progressDrawable;
        progressDrawable = new CircularProgressDrawable(holder.itemView.getContext());
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.start();

        Product product = productList.get(position);
        holder.txtName.setText(product.getName());
        holder.txtAmount.setText(product.getAmount());
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return this.productList.size();
    }

    public class ProductRecyclerHolder extends RecyclerView.ViewHolder {
        // Atributos de la clase
        TextView txtAmount;
        TextView txtName;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public ProductRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Inicialización de los atributos
            txtName = (TextView) itemView.findViewById(R.id.txtProductName);
            txtAmount = (TextView) itemView.findViewById(R.id.txtProductAmount);
            // Asignamos un tag para posteriormente poder identificar el itemView en la actividad para
            // la creacion de los oyentes
            itemView.setTag(this);
        }
    }
}
