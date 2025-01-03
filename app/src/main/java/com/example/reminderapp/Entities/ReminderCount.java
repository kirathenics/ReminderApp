package com.example.reminderapp.Entities;

import androidx.annotation.NonNull;

public class ReminderCount {
    public int totalReminders;
    public int completedReminders;
    public int notCompletedReminders;

    public ReminderCount(int totalReminders, int completedReminders, int notCompletedReminders) {
        this.totalReminders = totalReminders;
        this.completedReminders = completedReminders;
        this.notCompletedReminders = notCompletedReminders;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReminderCount{" +
                "totalReminders=" + totalReminders +
                ", completedReminders=" + completedReminders +
                ", notCompletedReminders=" + notCompletedReminders +
                '}';
    }
}
