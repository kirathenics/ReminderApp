package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import com.example.reminderapp.Entities.Reminder;
import com.example.reminderapp.Enums.ReminderRepeatType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static long splitDate(long timeDate) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(timeDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long splitTime(long timeDate) {
        long date = splitDate(timeDate);
        return timeDate - date;
    }

    public static Pair<Long, Long> splitTimeDate(long timeDate) {
        long date = splitDate(timeDate);
        long time = timeDate - date;

        return new Pair<>(time, date);
    }

    public static long addDefaultTimeZoneOffset(long timeInMillis) {
        return addTimeZoneOffset(timeInMillis, TimeZone.getDefault());
    }

    public static long addTimeZoneOffset(long timeInMillis, TimeZone targetTimeZone) {
        TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");

        int targetOffset = targetTimeZone.getOffset(timeInMillis);
        int defaultOffset = defaultTimeZone.getOffset(timeInMillis);

        return timeInMillis + (targetOffset - defaultOffset);
    }

    public static String calculateTimeDifferenceWithCurrentTime(long endTime) {
        long currentTimeMillis = System.currentTimeMillis();
        return calculateTimeDifference(endTime, currentTimeMillis);
    }

    @SuppressLint("DefaultLocale")
    public static String calculateTimeDifference(long endTime, long startTime) {
        Log.i("startTime", "" + startTime);
        Log.i("endTime", "" + endTime);

        long totalDifferenceInMillis = endTime - startTime;

        if (totalDifferenceInMillis < 0) {
            return "Selected time has already passed";
        }

        long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDifferenceInMillis) % 60;
        long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(totalDifferenceInMillis) % 60;
        long totalDifferenceInHours = TimeUnit.MILLISECONDS.toHours(totalDifferenceInMillis);
        long totalDifferenceInDays = TimeUnit.MILLISECONDS.toDays(totalDifferenceInMillis);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String formattedDate = dateFormat.format(new Date(startTime));

        Date currentDateWithoutTime;
        try {
            currentDateWithoutTime = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        assert currentDateWithoutTime != null;
        long startDate = splitDate(endTime);
        long dayDifference = (startDate - currentDateWithoutTime.getTime()) / (1000 * 60 * 60 * 24);

        if (dayDifference == 0 && differenceInMinutes < 1) {
            return String.format("Today, in %d %s", differenceInSeconds, getPluralForm(differenceInSeconds, "second", "seconds"));
        }

        if (dayDifference == 0 && totalDifferenceInHours < 1) {
            return String.format("Today, in %d %s", differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
        }

        if (dayDifference == 0) {
            return String.format("Today, in %d %s and %d %s",
                    totalDifferenceInHours, getPluralForm(totalDifferenceInHours, "hour", "hours"),
                    differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
        }

        if (dayDifference == 1) {
            return String.format("Tomorrow, in %d %s and %d %s",
                    totalDifferenceInHours, getPluralForm(totalDifferenceInHours, "hour", "hours"),
                    differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
        }

        return String.format("In %d %s", totalDifferenceInDays, getPluralForm(totalDifferenceInDays, "day", "days"));
    }

    @SuppressLint("DefaultLocale")
    public static String updateRepeatTime(int repeatValue, String repeatPattern) {
        return String.format("%d %s", repeatValue, getPluralForm(1, repeatPattern, repeatPattern + "s"));
    }

    public static String getNextTimeInfoText(long startTime, int repeatValue, String repeatPattern) {
        long nextTimeMillis = getNextTimePeriodic(startTime, repeatValue, repeatPattern);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
        String formattedTime = timeFormat.format(new Date(nextTimeMillis));
        String formattedDate = dateFormat.format(new Date(nextTimeMillis));

        String timeDifference = calculateTimeDifferenceWithCurrentTime(nextTimeMillis);

        return String.format("Next time: %s • %s • %s", formattedTime, formattedDate, timeDifference);
    }

    public static void calculateLastTimeNotification(Reminder reminder) {
        if (reminder.getLastTimeNotified() == 0) {
            reminder.setLastTimeNotified(reminder.getSelectedTime());
        } else {
            reminder.setLastTimeNotified(calculateNextTimeNotification(reminder, reminder.getLastTimeNotified()));
        }
    }

    public static long calculateNextTimeNotification(Reminder reminder, long lastTime) {
        long nextTime = lastTime;
        if (reminder.getRepeatType() == ReminderRepeatType.SCHEDULE) {
            nextTime = getNextTimeSchedule(lastTime, reminder.getRepeatDays());
        } else if (reminder.getRepeatType() == ReminderRepeatType.PERIODIC) {
            nextTime = getNextTimePeriodic(lastTime, reminder.getRepeatValue(), reminder.getRepeatPattern());
        }
        return nextTime;
    }

    public static long getNextTimePeriodic(long startTime, int repeatValue, String repeatPattern) {
        return startTime + getRepeatIntervalMillis(repeatValue, repeatPattern);
    }

    public static long getNextTimeSchedule(long startTime, List<Integer> repeatDays) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(startTime);
        int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        int adjustedDayOfWeek = (currentDayOfWeek == Calendar.SUNDAY) ? 7 : currentDayOfWeek - 1;
        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCalendar.get(Calendar.MINUTE);

        long time = splitTime(startTime);
        Calendar reminderTimeCalendar = Calendar.getInstance();
        reminderTimeCalendar.setTimeInMillis(time);
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
            nextReminderCalendar.setTimeInMillis(startTime);
            nextReminderCalendar.add(Calendar.DAY_OF_YEAR, minDayOffset);
            nextReminderCalendar.set(Calendar.HOUR_OF_DAY, reminderHour);
            nextReminderCalendar.set(Calendar.MINUTE, reminderMinute);
            nextReminderCalendar.set(Calendar.SECOND, 0);
            nextReminderCalendar.set(Calendar.MILLISECOND, 0);

            return nextReminderCalendar.getTimeInMillis();
        }
        return 0;
    }

    // TODO: make repeatPattern enum

    public static long getRepeatIntervalMillis(int repeatValue, String repeatPattern) {
        switch (repeatPattern) {
            case "minute":
                return TimeUnit.MINUTES.toMillis(repeatValue);
            case "hour":
                return TimeUnit.HOURS.toMillis(repeatValue);
            case "day":
                return TimeUnit.DAYS.toMillis(repeatValue);
            case "week":
                return TimeUnit.DAYS.toMillis(repeatValue) * 7;
            case "month":
                return TimeUnit.DAYS.toMillis(repeatValue) * 30;
            case "year":
                return TimeUnit.DAYS.toMillis(repeatValue) * 365;
            default:
                throw new IllegalArgumentException("Invalid repeat pattern: " + repeatPattern);
        }
    }

    private static String getPluralForm(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
}