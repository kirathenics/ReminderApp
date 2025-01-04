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
            "ORDER BY updated_at ASC")
    List<Reminder> getFilteredAndSortedByUpdatedTimeAsc(Integer categoryId, Boolean isCompleted);

    @Query("SELECT * FROM reminders " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:isCompleted IS NULL OR (:isCompleted = 0 AND is_completed = 0) OR :isCompleted) " +
            "ORDER BY updated_at DESC ")
    List<Reminder> getFilteredAndSortedByUpdatedTimeDesc(Integer categoryId, Boolean isCompleted);

    @Query("SELECT COUNT(*) FROM reminders " +
            "WHERE is_completed = 1 " +
            "AND (:categoryId IS NULL OR category_id = :categoryId) ")
    int  getCompletedRemindersCountByCategoryId(Integer categoryId);

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
