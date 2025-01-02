package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {
    @SuppressLint("DefaultLocale")
    public static String calculateTimeDifference(long selectedTime, long selectedDate, Context context) {
        long currentTimeMillis = System.currentTimeMillis();
        return calculateTimeDifference(selectedTime, selectedDate, currentTimeMillis, context);
    }

    @SuppressLint("DefaultLocale")
    public static String calculateTimeDifference(long selectedTime, long selectedDate, long currentTimeMillis, Context context) {
        long totalDifferenceInMillis = selectedDate + selectedTime - currentTimeMillis;
        if (totalDifferenceInMillis < 0) {
            return context.getString(R.string.selected_time_has_already_passed);
        }

        long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDifferenceInMillis) % 60;
        long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(totalDifferenceInMillis) % 60;
        long totalDifferenceInHours = TimeUnit.MILLISECONDS.toHours(totalDifferenceInMillis);
        long totalDifferenceInDays = TimeUnit.MILLISECONDS.toDays(totalDifferenceInMillis);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(currentTimeMillis));

        Date currentDateWithoutTime;
        try {
            currentDateWithoutTime = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        assert currentDateWithoutTime != null;
        long dayDifference = (selectedDate - currentDateWithoutTime.getTime()) / (1000 * 60 * 60 * 24);

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

    public static String getNextTimeInfoText(long selectedTime, long selectedDate, int repeatValue, String repeatPattern, Context context) {
        long repeatIntervalMillis = getRepeatIntervalMillis(repeatValue, repeatPattern);
        long nextTimeMillis = selectedDate + selectedTime + repeatIntervalMillis;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        String formattedTime = timeFormat.format(new Date(nextTimeMillis));
        String formattedDate = dateFormat.format(new Date(nextTimeMillis));

        long time = nextTimeMillis % (24 * 60 * 60 * 1000);
        long date = nextTimeMillis - time;
        String timeDifference = calculateTimeDifference(time, date, context);

        return String.format("Next time: %s • %s • %s", formattedTime, formattedDate, timeDifference);
    }

    private static String getPluralForm(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }

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
}