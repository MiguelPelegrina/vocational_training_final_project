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

public class RecipeStepRecyclerAdapter extends RecyclerView.Adapter<RecipeStepRecyclerAdapter.StepRecyclerHolder> {
    // Fields
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;
    private final List<String> stepList;

    /**
     * Inner class of the recycler holder of this adapter
     */
    protected static class StepRecyclerHolder extends RecyclerView.ViewHolder {
        // Fields
        private final TextView txtStep;

        /**
         * Parameterized constructor
         * @param itemView
         */
        public StepRecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Set the view
            txtStep = itemView.findViewById(R.id.txtRecipeDetailStepText);
            itemView.setTag(this);
        }
    }

    /**
     * Parameterized constructor
     *
     * @param stepList
     */
    public RecipeStepRecyclerAdapter(List<String> stepList) {
        this.stepList = stepList;
    }

    // Getter
    @Override
    public int getItemCount() {
        return this.stepList.size();
    }

    public List<String> getStepList() {
        return this.stepList;
    }

    // Setter
    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeStepRecyclerAdapter.StepRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Set the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_step_list_item, parent, false);

        // Get the holder
        RecipeStepRecyclerAdapter.StepRecyclerHolder recyclerHolder = new StepRecyclerHolder(view);

        // Set the listener
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeStepRecyclerAdapter.StepRecyclerHolder holder, int position) {
        // Get the step
        String step = stepList.get(position);

        // Set the holder data
        holder.txtStep.setText(step);

        // Set a fading animation
        Utils.setFadeAnimation(holder.itemView);
    }
}
