package com.example.reminderapp;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Reminder;

public class ScheduleNotificationManager {

    public static void createNotification(Context context, Reminder reminder) {
        long triggerTime = reminder.getSelectedTime();

        if (triggerTime <= System.currentTimeMillis() || reminder.isCompleted()) {
            return;
        }

        scheduleNotification(context, reminder, triggerTime);
    }

    public static void createNextNotification(Context context, Reminder reminder) {
        TimeUtils.calculateLastTimeNotification(reminder);
        long nextTriggerTime = TimeUtils.calculateNextTimeNotification(reminder, reminder.getLastTimeNotified());

        if ((reminder.getEndTime() != 0 && nextTriggerTime >= reminder.getEndTime()) ||
                (reminder.getLastTimeNotified() == nextTriggerTime)) {
            reminder.setCompleted(true);
        }

        new Thread(() -> AppDatabase.getInstance(context).reminderDAO().update(reminder)).start();

        if (reminder.isCompleted()) {
            return;
        }

        scheduleNotification(context, reminder, nextTriggerTime);
    }

    public static void updateNotification(Context context, Reminder reminder) {
        cancelNotification(context, reminder.getId());
        createNotification(context, reminder);
    }

    public static void cancelNotification(Context context, int reminderId) {
        Intent intent = new Intent(context, ReminderNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private static void scheduleNotification(Context context, Reminder reminder, long triggerTime) {
        Intent intent = new Intent(context, ReminderNotificationReceiver.class);
        intent.putExtra("title", R.string.app_name);
        intent.putExtra("message", reminder.getTitle());
        intent.putExtra("reminder", reminder);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
