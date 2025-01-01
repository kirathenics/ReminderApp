package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
//    @SuppressLint("DefaultLocale")
//    public static String calculateTimeDifference(long selectedTime, long selectedDate, Context context) {
//        Date currentDate = new Date();
//        long currentTimeMillis = currentDate.getTime();
//
//        long totalDifferenceInMillis = selectedDate + selectedTime - currentTimeMillis;
//        if (totalDifferenceInMillis < 0) {
//            return context.getString(R.string.selected_time_has_already_passed);
//        }
//
//        long differenceInSeconds = (totalDifferenceInMillis / 1000) % 60;
//        long differenceInMinutes = (totalDifferenceInMillis / (1000 * 60)) % 60;
//        long totalDifferenceInHours = (totalDifferenceInMillis / (1000 * 60 * 60));
//        long totalDifferenceInDays = totalDifferenceInMillis / (1000 * 60 * 60 * 24);
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        String formattedDate = dateFormat.format(new Date());
//
//        Date currentDateWithoutTime;
//        try {
//            currentDateWithoutTime = dateFormat.parse(formattedDate);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//        assert currentDateWithoutTime != null;
//        long dayDifference = (selectedDate - currentDateWithoutTime.getTime()) / (1000 * 60 * 60 * 24);
//
//        if (dayDifference == 0 && differenceInMinutes < 1) {
//            return String.format("Today, in %d %s", differenceInSeconds, getPluralForm(differenceInSeconds, "second", "seconds"));
//        }
//
//        if (dayDifference == 0 && totalDifferenceInHours < 1) {
//            return String.format("Today, in %d %s", differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
//        }
//
//        if (dayDifference == 0) {
//            return String.format("Today, in %d %s and %d %s",
//                    totalDifferenceInHours, getPluralForm(totalDifferenceInHours, "hour", "hours"),
//                    differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
//        }
//
//        if (dayDifference == 1) {
//            return String.format("Tomorrow, in %d %s and %d %s",
//                    totalDifferenceInHours, getPluralForm(totalDifferenceInHours, "hour", "hours"),
//                    differenceInMinutes, getPluralForm(differenceInMinutes, "minute", "minutes"));
//        }
//
//        return String.format("In %d %s", totalDifferenceInDays, getPluralForm(totalDifferenceInDays, "day", "days"));
//    }
//
//    private static String getPluralForm(long count, String singular, String plural) {
//        return count == 1 ? singular : plural;
//    }

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

        long differenceInSeconds = (totalDifferenceInMillis / 1000) % 60;
        long differenceInMinutes = (totalDifferenceInMillis / (1000 * 60)) % 60;
        long totalDifferenceInHours = (totalDifferenceInMillis / (1000 * 60 * 60));
        long totalDifferenceInDays = totalDifferenceInMillis / (1000 * 60 * 60 * 24);

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

    private static String getPluralForm(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
}