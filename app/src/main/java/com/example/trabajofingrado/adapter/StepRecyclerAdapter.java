package com.example.trabajofingrado.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.utilities.Utils;

import java.util.List;

public class StepRecyclerAdapter extends RecyclerView.Adapter<StepRecyclerAdapter.StepRecyclerHolder>{
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private List<String> stepList;

    public StepRecyclerAdapter(List<String> stepList) {
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepRecyclerAdapter.StepRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_list_item, parent,false);
        StepRecyclerAdapter.StepRecyclerHolder recyclerHolder = new StepRecyclerAdapter.StepRecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StepRecyclerAdapter.StepRecyclerHolder holder, int position) {
        String step = stepList.get(position);
        holder.txtStep.setText(step);


        Utils.setFadeAnimation(holder.itemView);
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    public List<String> getStepList(){
        return this.stepList;
    }

    @Override
    public int getItemCount() {
        return this.stepList.size();
    }

    protected class StepRecyclerHolder extends RecyclerView.ViewHolder {
        TextView txtStep;

        public StepRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            txtStep = (TextView) itemView.findViewById(R.id.txtRecipeDetailStepText);
            itemView.setTag(this);
        }
    }
}
