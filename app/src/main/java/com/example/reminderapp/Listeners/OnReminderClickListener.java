package com.example.reminderapp.Listeners;

import androidx.cardview.widget.CardView;

import com.example.reminderapp.Entities.Reminder;

public interface OnReminderClickListener {
    void onReminderClick(Reminder reminder);
    void onReminderLongClick(Reminder reminder, CardView cardView);
}
