package com.example.trabajofingrado.interfaces;

/**
 * Interface that allows us to set different listener for specific views inside a recycler holder
 */
public interface RecyclerViewActionListener {
    void onViewClicked(int clickedViewId, int clickedItemPosition);
}
