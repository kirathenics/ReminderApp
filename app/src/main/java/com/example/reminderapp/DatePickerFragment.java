package com.example.reminderapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

public class DatePickerFragment extends Fragment {

    private DatePicker datePicker;
    private Integer year;
    private Integer month;
    private Integer day;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireContext().setTheme(R.style.CustomDateTimePickerTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        datePicker = new DatePicker(getContext());

        if (year != null && month != null && day != null) {
            datePicker.updateDate(year, month, day);
        }

        return datePicker;
    }

    public int getYear() {
        return datePicker.getYear();
    }

    public int getMonth() {
        return datePicker.getMonth();
    }

    public int getDay() {
        return datePicker.getDayOfMonth();
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        if (datePicker != null) {
            datePicker.updateDate(year, month, day);
        }
    }
}