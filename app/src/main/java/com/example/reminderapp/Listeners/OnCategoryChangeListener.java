package com.example.reminderapp.Listeners;

import com.example.reminderapp.Entities.Category;

public interface OnCategoryChangeListener {
    void onCategoryUpdated(int position, Category updatedCategory);
    void onCategoryDeleted(int position);
}
