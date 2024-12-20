package com.example.reminderapp.DAO;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.reminderapp.Entities.Reminder;

import java.util.List;

@Dao
public interface ReminderDAO {
    @Query("SELECT * FROM reminders ORDER BY id DESC")
    List<Reminder> getAll();

    @Insert(onConflict = REPLACE)
    void insert(Reminder reminder);

    @Query("UPDATE reminders SET title = :title, description = :description, date = :date, time = :time, category = :category WHERE id = :id")
    void update(int id, String title, String description, String date, String time, String category);

    @Delete
    void delete(Reminder reminder);
}
