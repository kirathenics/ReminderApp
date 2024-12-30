package com.example.reminderapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimePickerFragment extends Fragment {

    private TimePicker timePicker;
    private Integer hour;
    private Integer minute;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireContext().setTheme(R.style.CustomDateTimePickerTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        timePicker = new TimePicker(getContext());
        timePicker.setIs24HourView(true);

        if (hour != null && minute != null) {
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }

        return timePicker;
    }

    public void setTimePickerEnabled(boolean enabled) {
        if (timePicker != null) {
            timePicker.setEnabled(enabled);
        }
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;

        if (timePicker != null) {
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }
    }

    public int getHour() {
        if (timePicker != null) {
            return timePicker.getHour();
        }
        return 0;
    }

    public int getMinute() {
        if (timePicker != null) {
            return timePicker.getMinute();
        }
        return 0;
    }
}