package com.example.reminderapp.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.reminderapp.DAO.ReminderDAO;
import com.example.reminderapp.Entities.Reminder;

@Database(entities = Reminder.class, version = 1, exportSchema = false)
public abstract class ReminderDB extends RoomDatabase {
    private static ReminderDB database;
    private static final String DATABASE_NAME = "ReminderApp";

    public synchronized static ReminderDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    ReminderDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract ReminderDAO reminderDAO();
}
