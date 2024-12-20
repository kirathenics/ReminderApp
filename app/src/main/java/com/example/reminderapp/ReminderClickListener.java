package com.example.reminderapp;

import androidx.cardview.widget.CardView;

import com.example.reminderapp.Entities.Reminder;

public interface ReminderClickListener {
    void onClick(Reminder reminder);
    void onLongClick(Reminder reminder, CardView cardView);
}
