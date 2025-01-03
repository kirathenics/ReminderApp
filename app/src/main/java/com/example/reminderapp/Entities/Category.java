package com.example.reminderapp.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "categories")
public class Category implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

//    @Embedded
//    private int totalReminders;
//
//    @Embedded
//    private int completedReminders;
//
//    @Embedded
//    private int notCompletedReminders;

    @Embedded
    private ReminderCount reminderCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

//    public int getTotalReminders() {
//        return totalReminders;
//    }
//
//    public void setTotalReminders(int totalReminders) {
//        this.totalReminders = totalReminders;
//    }
//
//    public int getCompletedReminders() {
//        return completedReminders;
//    }
//
//    public void setCompletedReminders(int completedReminders) {
//        this.completedReminders = completedReminders;
//    }
//
//    public int getNotCompletedReminders() {
//        return notCompletedReminders;
//    }
//
//    public void setNotCompletedReminders(int notCompletedReminders) {
//        this.notCompletedReminders = notCompletedReminders;
//    }

    public ReminderCount getReminderCount() {
        return reminderCount;
    }

    public void setReminderCount(ReminderCount reminderCount) {
        this.reminderCount = reminderCount;
    }

//    @NonNull
//    @Override
//    public String toString() {
//        return "Category{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", color='" + color + '\'' +
//                ", isActive=" + isActive +
//                '}';
//    }

    @NonNull
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", isActive=" + isActive +
                ", reminderCount=" + reminderCount.toString() +
                '}';
    }
}
