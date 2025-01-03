package com.example.reminderapp.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.CategoryWithReminderCount;

import java.util.List;


@Dao
public interface CategoryDAO {

    @Query("SELECT * FROM categories ORDER BY id ASC ")
    List<Category> getAll();

    @Query("SELECT c.*, " +
            "COUNT(r.id) AS totalReminders, " +
            "SUM(CASE WHEN r.is_completed = 1 THEN 1 ELSE 0 END) AS completedReminders, " +
            "SUM(CASE WHEN r.is_completed = 0 THEN 1 ELSE 0 END) AS notCompletedReminders " +
            "FROM categories c " +
            "LEFT JOIN reminders r ON c.id = r.category_id " +
            "GROUP BY c.id " +
            "ORDER BY c.id ASC ")
    List<CategoryWithReminderCount> getAllWithReminderCount();

    @Query("SELECT c.*, " +
            "COUNT(r.id) AS totalReminders, " +
            "SUM(CASE WHEN r.is_completed = 1 THEN 1 ELSE 0 END) AS completedReminders, " +
            "SUM(CASE WHEN r.is_completed = 0 THEN 1 ELSE 0 END) AS notCompletedReminders " +
            "FROM categories c " +
            "LEFT JOIN reminders r ON c.id = r.category_id " +
            "GROUP BY c.id " +
            "ORDER BY  " +
            "CASE WHEN :isAsc THEN c.name END ASC, " +
            "CASE WHEN NOT :isAsc THEN c.name END DESC ")
    List<CategoryWithReminderCount> getAllSortedByNameWithReminderCount(boolean isAsc);

    @Query("SELECT * FROM categories ORDER BY id DESC LIMIT 1")
    Category getLastInsertedCategory();

    @Query("SELECT * FROM categories WHERE id = :id ")
    Category findById(int id);

    @Query("SELECT * FROM categories WHERE name LIKE :name ")
    Category findByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);
}
