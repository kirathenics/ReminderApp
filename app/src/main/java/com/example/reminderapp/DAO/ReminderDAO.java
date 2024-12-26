package com.example.reminderapp.DAO;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reminderapp.Entities.Reminder;

import java.util.List;

@Dao
public interface ReminderDAO {
    @Query("SELECT * FROM reminders ORDER BY id ASC")
    List<Reminder> getAll();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder findById(int id);

    @Query("SELECT * FROM reminders WHERE category_id = :categoryId ORDER BY date, time ASC")
    List<Reminder> findByCategoryId(int categoryId);

    @Query("SELECT * FROM reminders WHERE is_completed = :isCompleted ORDER BY date, time ASC")
    List<Reminder> findByCompletionStatus(boolean isCompleted);

    @Insert(onConflict = REPLACE)
    void insert(Reminder reminder);

    @Query("UPDATE reminders SET title = :title, description = :description, date = :date, time = :time, " +
            "is_completed = :isCompleted, priority = :priority, repeat = :repeat, category_id = :categoryId, " +
            "updated_at = :updatedAt WHERE id = :id")
    void update(int id, String title, String description, String date, String time, boolean isCompleted,
                int priority, String repeat, int categoryId, String updatedAt);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM reminders WHERE category_id = :categoryId")
    void deleteByCategoryId(int categoryId);
}
