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

    @Insert(onConflict = REPLACE)
    void insert(Category category);

    @Query("UPDATE categories SET name = :name, color = :color, is_active = :isActive WHERE id = :id")
    void update(int id, String name, String color, boolean isActive);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories WHERE name = :name")
    void delete(String name);
}
