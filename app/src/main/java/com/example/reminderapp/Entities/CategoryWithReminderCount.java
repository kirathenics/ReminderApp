package com.example.reminderapp.Entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

public class CategoryWithReminderCount {

    @Embedded
    private Category category;

    private int totalReminders;
    private int completedReminders;
    private int notCompletedReminders;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getTotalReminders() {
        return totalReminders;
    }

    public void setTotalReminders(int totalReminders) {
        this.totalReminders = totalReminders;
    }

    public int getCompletedReminders() {
        return completedReminders;
    }

    public void setCompletedReminders(int completedReminders) {
        this.completedReminders = completedReminders;
    }

    public int getNotCompletedReminders() {
        return notCompletedReminders;
    }

    public void setNotCompletedReminders(int notCompletedReminders) {
        this.notCompletedReminders = notCompletedReminders;
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryWithReminderCount{" +
                "category=" + category +
                ", totalReminders=" + totalReminders +
                ", completedReminders=" + completedReminders +
                ", notCompletedReminders=" + notCompletedReminders +
                '}';
    }
}
