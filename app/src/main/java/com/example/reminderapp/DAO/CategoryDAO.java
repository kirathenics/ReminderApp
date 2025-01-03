package com.example.reminderapp.DAO;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reminderapp.Entities.Category;

import java.util.List;

@Dao
public interface CategoryDAO {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<Category> getAll();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllSortedByNameAsc();

    @Query("SELECT * FROM categories ORDER BY name DESC")
    List<Category> getAllSortedByNameDesc();

    @Query("SELECT * FROM categories WHERE id = :id")
    Category findById(int id);

    @Query("SELECT * FROM categories WHERE name LIKE :name")
    Category findByName(String name);

    @Query("SELECT categories.*, " +
            "COUNT(reminders.id) AS totalReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 1 THEN 1 ELSE 0 END) AS completedReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 0 THEN 1 ELSE 0 END) AS notCompletedReminders " +
            "FROM categories " +
            "LEFT JOIN reminders ON categories.id = reminders.category_id " +
            "GROUP BY categories.id " +
            "ORDER BY categories.id ASC")
    List<Category> getAllWithReminderCount();

    @Query("SELECT categories.*, " +
            "COUNT(reminders.id) AS totalReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 1 THEN 1 ELSE 0 END) AS completedReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 0 THEN 1 ELSE 0 END) AS notCompletedReminders " +
            "FROM categories " +
            "LEFT JOIN reminders ON categories.id = reminders.category_id " +
            "GROUP BY categories.id " +
            "ORDER BY categories.name ASC")
    List<Category> getAllSortedByNameAscWithReminderCount();

    @Query("SELECT categories.*, " +
            "COUNT(reminders.id) AS totalReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 1 THEN 1 ELSE 0 END) AS completedReminders, " +
            "SUM(CASE WHEN reminders.is_completed = 0 THEN 1 ELSE 0 END) AS notCompletedReminders " +
            "FROM categories " +
            "LEFT JOIN reminders ON categories.id = reminders.category_id " +
            "GROUP BY categories.id " +
            "ORDER BY categories.name DESC")
    List<Category> getAllSortedByNameDescWithReminderCount();

    @Insert(onConflict = REPLACE)
    void insert(Category category);

    @Query("UPDATE categories SET name = :name, color = :color, is_active = :isActive WHERE id = :id")
    void update(int id, String name, String color, boolean isActive);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);
}
