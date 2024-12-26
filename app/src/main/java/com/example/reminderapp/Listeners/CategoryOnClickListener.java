package com.example.reminderapp.Listeners;

import androidx.cardview.widget.CardView;

import com.example.reminderapp.Entities.Category;

public interface CategoryOnClickListener {
    void onCategoryClick(Category category);
    void onCategoryLongClick(Category category, CardView cardView);
}
