package com.example.reminderapp.Listeners;

public interface OnItemUpdatedListener<T> {
    void onItemUpdated(int position, T updatedItem);
}
