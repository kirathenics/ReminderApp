package com.example.reminderapp;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.reminderapp.Entities.Reminder;
import com.example.reminderapp.Enums.ReminderRepeatType;

import java.util.Calendar;
import java.util.List;

public class ScheduleNotificationManager {

    public static void createNotification(Context context, Reminder reminder) {
        long triggerTime = reminder.getDate() + reminder.getTime();

        if (triggerTime <= System.currentTimeMillis() || reminder.isCompleted()) {
            return;
        }

        scheduleNotification(context, reminder, triggerTime);
    }

    public static void createNextNotification(Context context, Reminder reminder) {
        calculateLastTimeNotification(reminder);
        long nextTriggerTime = calculateNextTimeNotification(reminder, reminder.getLastTimeNotified());

        if (reminder.getEndDate() != 0 && nextTriggerTime >= reminder.getEndDate() ||
            reminder.getLastTimeNotified() == nextTriggerTime) {
            reminder.setCompleted(true);
        }

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

    public static void calculateLastTimeNotification(Reminder reminder) {
        if (reminder.getLastTimeNotified() == 0) {
            reminder.setLastTimeNotified(reminder.getTime() + reminder.getDate());
        } else {
            reminder.setLastTimeNotified(calculateNextTimeNotification(reminder, reminder.getLastTimeNotified()));
        }
    }

    public static long calculateNextTimeNotification(Reminder reminder, long lastTime) {
        long nextTime = 0;
        if (reminder.getRepeatType() == ReminderRepeatType.SCHEDULE) {
            List<Integer> repeatDays = reminder.getRepeatDays();

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTimeInMillis(lastTime);
            int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
            int adjustedDayOfWeek = (currentDayOfWeek == Calendar.SUNDAY) ? 7 : currentDayOfWeek - 1;
            int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = currentCalendar.get(Calendar.MINUTE);

            Calendar reminderTimeCalendar = Calendar.getInstance();
            reminderTimeCalendar.setTimeInMillis(lastTime);
            int reminderHour = reminderTimeCalendar.get(Calendar.HOUR_OF_DAY);
            int reminderMinute = reminderTimeCalendar.get(Calendar.MINUTE);

            Integer nextDay = null;
            int minDayOffset = Integer.MAX_VALUE;

            for (int day : repeatDays) {
                int dayOffset = (day - adjustedDayOfWeek + 7) % 7;

                if (dayOffset == 0) {
                    if (reminderHour > currentHour || (reminderHour == currentHour && reminderMinute > currentMinute)) {
                        nextDay = day;
                        minDayOffset = 0;
                        break;
                    } else {
                        dayOffset = 7;
                    }
                }

                if (dayOffset < minDayOffset) {
                    minDayOffset = dayOffset;
                    nextDay = day;
                }
            }

            if (nextDay != null) {
                Calendar nextReminderCalendar = Calendar.getInstance();
                nextReminderCalendar.setTimeInMillis(lastTime);
                nextReminderCalendar.add(Calendar.DAY_OF_YEAR, minDayOffset);
                nextReminderCalendar.set(Calendar.HOUR_OF_DAY, reminderHour);
                nextReminderCalendar.set(Calendar.MINUTE, reminderMinute);
                nextReminderCalendar.set(Calendar.SECOND, 0);
                nextReminderCalendar.set(Calendar.MILLISECOND, 0);

                nextTime = nextReminderCalendar.getTimeInMillis();
            }
        } else if (reminder.getRepeatType() == ReminderRepeatType.PERIODIC) {
            String pattern = reminder.getRepeatPattern();
            int value = reminder.getRepeatValue();
            long addValue = Utils.getRepeatIntervalMillis(value, pattern);

            nextTime = lastTime + addValue;
        }
        return nextTime;
    }

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
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
