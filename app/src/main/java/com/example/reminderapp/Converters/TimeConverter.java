package com.example.reminderapp.Converters;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeConverter {

    public static String formatTime(long time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return timeFormat.format(new Date(time));
    }

    public static String formatDate(long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
        return dateFormat.format(new Date(date));
    }

    public static long parseTime(String timeString) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Date time = timeFormat.parse(timeString);
            return time != null ? time.getTime() : 0;
        } catch (ParseException e) {
            Log.e("TimeConverter", "error time converting", e);
            return 0;
        }
    }

    public static long parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormat.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("TimeConverter", "error date converting", e);
            return 0;
        }
    }
}
