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


    @Query("SELECT * FROM reminders WHERE category_id = :categoryId ORDER BY id ASC ")
    List<Reminder> getByCategoryId(int categoryId);


    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY id ASC ")
    List<Reminder> getFiltered(Integer categoryId, Boolean isCompleted);


    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY title ASC")
    List<Reminder> getFilteredAndSortedByTitleAsc(Integer categoryId, Boolean isCompleted);

    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY title DESC")
    List<Reminder> getFilteredAndSortedByTitleDesc(Integer categoryId, Boolean isCompleted);


    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY created_at ASC")
    List<Reminder> getFilteredAndSortedByCreatedTimeAsc(Integer categoryId, Boolean isCompleted);

    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY created_at DESC")
    List<Reminder> getFilteredAndSortedByCreatedTimeDesc(Integer categoryId, Boolean isCompleted);

    @Query("SELECT * FROM reminders WHERE title = :title LIMIT 1 ")
    Reminder findByTitle(String title);

    @Query("SELECT * FROM reminders ORDER BY id DESC LIMIT 1 ")
    Reminder getLastInsertedReminder();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);
}
