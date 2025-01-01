package com.example.reminderapp.Listeners;

import androidx.cardview.widget.CardView;

public interface OnItemClickListener<T> {
    void onItemClick(T item);
    void onItemLongClick(T item, CardView cardView);
}
