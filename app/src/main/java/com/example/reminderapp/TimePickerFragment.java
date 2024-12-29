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
        return timePicker;
    }

    public void setTimePickerEnabled(boolean enabled) {
        if (timePicker != null) {
            timePicker.setEnabled(enabled);
        }
    }

    public void setTime(int hour, int minute) {
        if (timePicker != null) {
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }
    }

    public int getHour() {
        return timePicker.getHour();
    }

    public int getMinute() {
        return timePicker.getMinute();
    }
}