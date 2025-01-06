package com.example.reminderapp.Entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.reminderapp.Converters.Converters;
import com.example.reminderapp.Enums.ReminderRepeatType;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "reminders",
        foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id", onDelete = CASCADE),
        indices = {@Index(value = {"title"}, unique = true), @Index(value = {"category_id"})})
public class Reminder implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "selected_time")
    private long selectedTime;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "repeat_type")
    private ReminderRepeatType repeatType;

    @ColumnInfo(name = "repeat_pattern")
    private String repeatPattern;

    @ColumnInfo(name = "repeat_value")
    private int repeatValue;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "repeat_days")
    private List<Integer> repeatDays = new ArrayList<>();

    @ColumnInfo(name = "end_time")
    private long endTime;

    @ColumnInfo(name = "last_time_notified")
    private long lastTimeNotified;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(long selectedTime) {
        this.selectedTime = selectedTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public ReminderRepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(ReminderRepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public String getRepeatPattern() {
        return repeatPattern;
    }

    public void setRepeatPattern(String repeatPattern) {
        this.repeatPattern = repeatPattern;
    }

    public int getRepeatValue() {
        return repeatValue;
    }

    public void setRepeatValue(int repeatValue) {
        this.repeatValue = repeatValue;
    }

    public List<Integer> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<Integer> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getLastTimeNotified() {
        return lastTimeNotified;
    }

    public void setLastTimeNotified(long lastTimeNotified) {
        this.lastTimeNotified = lastTimeNotified;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedSelectedTime = dateFormat.format(new Date(selectedTime));
        String formattedEndTime = dateFormat.format(new Date(endTime));
        String formattedLastTimeNotified = dateFormat.format(new Date(lastTimeNotified));
        String formattedCreatedAt = dateFormat.format(new Date(createdAt));
        String formattedUpdatedAt = dateFormat.format(new Date(updatedAt));

        return "Reminder{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", selectedTime=" + formattedSelectedTime +
                ", isCompleted=" + isCompleted +
                ", repeatType=" + repeatType +
                ", repeatPattern='" + repeatPattern + '\'' +
                ", repeatValue=" + repeatValue +
                ", repeatDays=" + repeatDays +
                ", endTime=" + formattedEndTime +
                ", lastTimeNotified=" + formattedLastTimeNotified +
                ", categoryId=" + categoryId +
                ", createdAt=" + formattedCreatedAt +
                ", updatedAt=" + formattedUpdatedAt +
                '}';
    }
}



