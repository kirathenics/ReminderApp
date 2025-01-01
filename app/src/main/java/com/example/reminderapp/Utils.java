package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import java.util.Date;

public class Utils {
    @SuppressLint("DefaultLocale")
    public static void calculateAndDisplayDifference(long selectedTime, long selectedDate, TextView textView, Context context) {
        Date currentDate = new Date();
        long currentTimeMillis = currentDate.getTime();

        long totalDifferenceInMillis = selectedDate + selectedTime - currentTimeMillis;
        long totalDifferenceInMinutes = (totalDifferenceInMillis / (1000 * 60)) % 60;
        long totalDifferenceInHours = (totalDifferenceInMillis / (1000 * 60 * 60)) % 24;
        long totalDifferenceInDays = totalDifferenceInMillis / (1000 * 60 * 60 * 24);

        if (totalDifferenceInMillis < 0) {
            textView.setText(context.getString(R.string.selected_time_has_already_passed));
            return;
        }

        if (totalDifferenceInDays == 0) {
            if (totalDifferenceInHours < 1) {
                textView.setText(String.format("Today, in %d minutes", totalDifferenceInMinutes));
            } else {
                textView.setText(String.format("Today, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
            }
        } else if (totalDifferenceInDays == 1) {
            textView.setText(String.format("Tomorrow, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
        } else {
            textView.setText(String.format("In %d days", totalDifferenceInDays));
        }
    }
}
