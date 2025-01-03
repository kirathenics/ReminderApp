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

    @Query("SELECT * FROM reminders ORDER BY title ASC")
    List<Reminder> getAllSortedByTitleAsc();

    @Query("SELECT * FROM reminders ORDER BY title DESC")
    List<Reminder> getAllSortedByTitleDesc();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder findById(int id);

    @Query("SELECT * FROM reminders WHERE title = :title")
    Reminder findByTitle(String title);

    @Query("SELECT * FROM reminders WHERE category_id = :categoryId ORDER BY id ASC")
    List<Reminder> findByCategoryId(int categoryId);

    @Query("SELECT * FROM reminders WHERE is_completed = :isCompleted ORDER BY id ASC")
    List<Reminder> findByCompletionStatus(boolean isCompleted);

    @Insert(onConflict = REPLACE)
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM reminders WHERE category_id = :categoryId")
    void deleteByCategoryId(int categoryId);
}
