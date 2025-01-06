package com.example.reminderapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.reminderapp.Entities.Reminder;


//public class ReminderNotificationReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String title = intent.getStringExtra("title");
//        String message = intent.getStringExtra("message");
//        Reminder reminder = (Reminder) intent.getSerializableExtra("reminder");
//
//        Intent activityIntent = new Intent(context, MainActivity.class);
//        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        assert reminder != null;
//        int reminderId = reminder.getId();
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                reminderId,
//                activityIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
//        notificationManagerCompat.notify(reminderId, builder.build());
//
//        if (reminder != null && !reminder.isCompleted()) {
//            long nextTime = calculateNextTriggerTime(reminder);
//            long time, date;
//
//            Calendar calendar = Calendar.getInstance();
//
//            calendar.setTimeInMillis(nextTime);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//
//            date = calendar.getTimeInMillis();
//            time = nextTime - date;
//
//            reminder.setTime(time);
//            reminder.setDate(date);
//
//            if (nextTime > 0 && (reminder.getEndDate() == 0 || nextTime <= reminder.getEndDate())) {
//                scheduleNextNotification(context, reminder);
//            }
//        }
//    }
//
//    private long calculateNextTriggerTime(Reminder reminder) {
//        long currentTime = System.currentTimeMillis();
//        long nextTime = 0;
//        if (reminder.getRepeatType() == ReminderRepeatType.WEEKLY) {
//            List<Integer> days = reminder.getRepeatDays();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(reminder.getDate() + reminder.getTime());
//            for (int day : days) {
//                calendar.set(Calendar.DAY_OF_WEEK, day);
//                long potentialTime = calendar.getTimeInMillis();
//                if (potentialTime > currentTime) {
//                    nextTime = potentialTime;
//                    break;
//                }
//            }
//        } else {
//            String pattern = reminder.getRepeatPattern();
//            int value = reminder.getRepeatValue();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(reminder.getDate() + reminder.getTime());
//
//            switch (pattern) {
//                case "minute":
//                    calendar.add(Calendar.MINUTE, value);
//                    break;
//                case "hour":
//                    calendar.add(Calendar.HOUR, value);
//                    break;
//                case "day":
//                    calendar.add(Calendar.DAY_OF_YEAR, value);
//                    break;
//                case "week":
//                    calendar.add(Calendar.WEEK_OF_YEAR, value);
//                    break;
//                case "month":
//                    calendar.add(Calendar.MONTH, value);
//                    break;
//                case "year":
//                    calendar.add(Calendar.YEAR, value);
//                    break;
//            }
//            nextTime = calendar.getTimeInMillis();
//        }
//        return nextTime > currentTime ? nextTime : 0;
//    }
//
//    private void scheduleNextNotification(Context context, Reminder reminder) {
//        Intent intent = new Intent(context, ReminderNotificationReceiver.class);
//        intent.putExtra("title", reminder.getTitle());
//        intent.putExtra("message", reminder.getDescription());
//        intent.putExtra("reminderId", reminder.getId());
//        intent.putExtra("reminder", reminder);
//        long triggerTime = reminder.getDate() + reminder.getTime();
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                reminder.getId(),
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//    }
//}

//public class ReminderNotificationReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String title = intent.getStringExtra("title");
//        String message = intent.getStringExtra("message");
//        Reminder reminder = (Reminder) intent.getSerializableExtra("reminder");
//
//        Intent activityIntent = new Intent(context, MainActivity.class);
//        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        assert reminder != null;
//        int reminderId = reminder.getId();
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                reminderId,
//                activityIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
//        notificationManagerCompat.notify(reminderId, builder.build());
//
//
//        long nextTriggerTime = calculateNextTriggerTime(reminder);
//        long time, date;
//
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.setTimeInMillis(nextTriggerTime);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        date = calendar.getTimeInMillis();
//        time = nextTriggerTime - date;
//
//        reminder.setTime(time);
//        reminder.setDate(date);
//
//        if (nextTriggerTime <= System.currentTimeMillis() || reminder.isCompleted() || (reminder.getEndDate() != 0 && nextTriggerTime >= reminder.getEndDate()))  {
//            return;
//        }
//
//        scheduleNextNotification(context, reminder, nextTriggerTime);
//    }
//
//    private long calculateNextTriggerTime(Reminder reminder) {
//        if (reminder.getRepeatType() == null) {
//            return 0;
//        }
//
//        long currentTime = System.currentTimeMillis();
//        long nextTime = 0;
//        if (reminder.getRepeatType() == ReminderRepeatType.SCHEDULE) {
//            List<Integer> repeatDays = reminder.getRepeatDays();
//            long reminderTime = reminder.getTime();
//            long reminderDate = reminder.getDate();
//
//            long currentTimeMillis = System.currentTimeMillis();
//            if (currentTimeMillis < reminderTime + reminderDate) {
//                currentTimeMillis = reminderTime + reminderDate;
//            }
//
//            Calendar currentCalendar = Calendar.getInstance();
//            currentCalendar.setTimeInMillis(currentTimeMillis);
//            int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
//            int adjustedDayOfWeek = (currentDayOfWeek == Calendar.SUNDAY) ? 7 : currentDayOfWeek - 1;
//            int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
//            int currentMinute = currentCalendar.get(Calendar.MINUTE);
//
//            Calendar reminderTimeCalendar = Calendar.getInstance();
//            reminderTimeCalendar.setTimeInMillis(reminderTime);
//            int reminderHour = reminderTimeCalendar.get(Calendar.HOUR_OF_DAY);
//            int reminderMinute = reminderTimeCalendar.get(Calendar.MINUTE);
//
//            Integer nextDay = null;
//            int minDayOffset = Integer.MAX_VALUE;
//
//            for (int day : repeatDays) {
//                int dayOffset = (day - adjustedDayOfWeek + 7) % 7;
//
//                if (dayOffset == 0) {
//                    if (reminderHour > currentHour || (reminderHour == currentHour && reminderMinute > currentMinute)) {
//                        nextDay = day;
//                        minDayOffset = 0;
//                        break;
//                    } else {
//                        dayOffset = 7;
//                    }
//                }
//
//                if (dayOffset < minDayOffset) {
//                    minDayOffset = dayOffset;
//                    nextDay = day;
//                }
//            }
//
//            if (nextDay != null) {
//                Calendar nextReminderCalendar = Calendar.getInstance();
//                nextReminderCalendar.setTimeInMillis(currentTimeMillis);
//                nextReminderCalendar.add(Calendar.DAY_OF_YEAR, minDayOffset);
//                nextReminderCalendar.set(Calendar.HOUR_OF_DAY, reminderHour);
//                nextReminderCalendar.set(Calendar.MINUTE, reminderMinute);
//                nextReminderCalendar.set(Calendar.SECOND, 0);
//                nextReminderCalendar.set(Calendar.MILLISECOND, 0);
//
//                long nextReminderTime = nextReminderCalendar.getTimeInMillis() % (24 * 60 * 60 * 1000);
//                long nextReminderDate = nextReminderCalendar.getTimeInMillis() - nextTime;
//
//                nextTime = nextReminderTime + nextReminderDate;
//            }
//        } else if (reminder.getRepeatType() == ReminderRepeatType.PERIODIC) {
//            String pattern = reminder.getRepeatPattern();
//            int value = reminder.getRepeatValue();
//            long addValue = Utils.getRepeatIntervalMillis(value, pattern);
//
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(reminder.getDate() + reminder.getTime() + addValue);
//
//            nextTime = calendar.getTimeInMillis();
//        }
//        return nextTime > currentTime ? nextTime : 0;
//    }
//
//    private void scheduleNextNotification(Context context, Reminder reminder, long triggerTime) {
//        Intent intent = new Intent(context, ReminderNotificationReceiver.class);
//        intent.putExtra("title", reminder.getTitle());
//        intent.putExtra("message", reminder.getDescription());
//        intent.putExtra("reminder", reminder);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                reminder.getId(),
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//    }
//}

public class ReminderNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        Reminder reminder = (Reminder) intent.getSerializableExtra("reminder");

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        assert reminder != null;
        int reminderId = reminder.getId();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(reminderId, builder.build());

        ScheduleNotificationManager.createNextNotification(context, reminder);
    }
}