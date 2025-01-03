package com.example.reminderapp.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reminderapp.Entities.Reminder;

import java.util.List;


@Dao
public interface ReminderDAO {
    @Query("SELECT * FROM reminders ORDER BY id ASC ")
    List<Reminder> getAll();

    @Query("SELECT * FROM reminders " +
            "ORDER BY " +
            "CASE WHEN :isAsc THEN title END ASC, " +
            "CASE WHEN NOT :isAsc THEN title END DESC ")
    List<Reminder> getAllSortedByTitle(boolean isAsc);

    @Query("SELECT * FROM reminders ORDER BY id DESC LIMIT 1 ")
    Reminder getLastInsertedReminder();

    @Query("SELECT * FROM reminders WHERE title = :title ")
    Reminder findByTitle(String title);

    @Query("SELECT * FROM reminders WHERE category_id = :categoryId ORDER BY id ASC ")
    List<Reminder> findByCategoryId(int categoryId);

    @Query("SELECT * FROM reminders WHERE is_completed = :isCompleted ORDER BY id ASC ")
    List<Reminder> findByCompletionStatus(boolean isCompleted);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);
}
