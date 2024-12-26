package com.example.reminderapp.Listeners;

import com.example.reminderapp.Entities.Reminder;

public interface OnReminderChangeListener {
    void onReminderUpdated(int position, Reminder updatedReminder);
    void onReminderDeleted(int position);
}
