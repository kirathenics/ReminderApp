package com.example.reminderapp.Databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.reminderapp.DAO.CategoryDAO;
import com.example.reminderapp.DAO.ReminderDAO;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;

@Database(entities = {Category.class, Reminder.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase database;
    private static final String DATABASE_NAME = "ReminderApp";

    public synchronized static AppDatabase getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract CategoryDAO categoryDAO();
    public abstract ReminderDAO reminderDAO();
}