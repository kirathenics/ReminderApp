package com.example.reminderapp.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.reminderapp.Converters.Converters;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "reminders")
public class Reminder implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    @ColumnInfo(name = "repeat_type") // "periodic" or "weekly"
    private String repeatType;

    @ColumnInfo(name = "repeat_pattern")
    private String repeatPattern;

    @ColumnInfo(name = "repeat_value")
    private int repeatValue;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "repeat_days")
    private List<Integer> repeatDays = new ArrayList<>();

    @ColumnInfo(name = "end_date")
    private long endDate;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
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

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
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
        String formattedTime = dateFormat.format(new Date(time + date));
        String formattedEndDate = dateFormat.format(new Date(endDate));
        String formattedCreatedAt = dateFormat.format(new Date(createdAt));
        String formattedUpdatedAt = dateFormat.format(new Date(updatedAt));

        return "Reminder{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date,time=" + formattedTime +
                ", isCompleted=" + isCompleted +
                ", repeatType='" + repeatType + '\'' +
                ", repeatPattern='" + repeatPattern + '\'' +
                ", repeatValue=" + repeatValue +
                ", repeatDays='" + repeatDays + '\'' +
                ", endDate=" + formattedEndDate +
                ", categoryId=" + categoryId +
                ", createdAt=" + formattedCreatedAt +
                ", updatedAt=" + formattedUpdatedAt +
                '}';
    }
}
