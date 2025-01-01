package com.example.reminderapp.Listeners;

public interface OnItemDeletedListener<T> {
    void onItemDeleted(int position, T deletedItem);
}
