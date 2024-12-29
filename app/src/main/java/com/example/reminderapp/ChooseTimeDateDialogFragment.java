package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;


public class ChooseTimeDateDialogFragment extends DialogFragment {

    private OnDateTimeSelectedListener listener;
    SwitchCompat switchTimeOptional;

    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(String dateTime);
    }

    public static ChooseTimeDateDialogFragment newInstance() {
        return new ChooseTimeDateDialogFragment();
    }

    public void setOnDateTimeSelectedListener(OnDateTimeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_time_date_dialog, container, false);

        FrameLayout frameContainer = view.findViewById(R.id.frame_picker_container);
        RadioGroup radioGroup = view.findViewById(R.id.time_date_group_picker);
        switchTimeOptional = view.findViewById(R.id.switch_time_optional);

        showFragment(new DatePickerFragment());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_date) {
                showFragment(new DatePickerFragment());
            } else if (checkedId == R.id.radio_time) {
                TimePickerFragment timeFragment = new TimePickerFragment();
                showFragment(timeFragment);
//                configureTimePicker(timeFragment);
            }
        });

        switchTimeOptional.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TimePickerFragment timeFragment = (TimePickerFragment) getChildFragmentManager().findFragmentByTag("TimePicker");
            if (timeFragment != null) {
                configureTimePicker(timeFragment);
            }
        });

        MaterialButton cancelButton = view.findViewById(R.id.cancel_date_time_button);
        cancelButton.setOnClickListener(v -> dismiss());

        MaterialButton okButton = view.findViewById(R.id.ok_date_time_button);
        okButton.setOnClickListener(v -> {
            DatePickerFragment dateFragment = (DatePickerFragment) getChildFragmentManager().findFragmentByTag("DatePicker");
            TimePickerFragment timeFragment = (TimePickerFragment) getChildFragmentManager().findFragmentByTag("TimePicker");

            int year = dateFragment != null ? dateFragment.getYear() : 0;
            int month = dateFragment != null ? dateFragment.getMonth() + 1 : 0;
            int day = dateFragment != null ? dateFragment.getDay() : 0;

            String time = "";
//            if (timeFragment != null && !switchTimeOptional.isChecked()) {
//                time = " 00:00";
//            } else
            if (timeFragment != null) {
                int hour = timeFragment.getHour();
                int minute = timeFragment.getMinute();
                time = String.format(" %02d:%02d", hour, minute);
            }

            String dateTime = String.format("%04d-%02d-%02d%s", year, month, day, time);
            if (listener != null) {
                listener.onDateTimeSelected(dateTime);
            }
        });

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_picker_container, fragment,
                fragment instanceof DatePickerFragment ? "DatePicker" : "TimePicker");
        transaction.commit();

        getChildFragmentManager().executePendingTransactions();

        if (fragment instanceof TimePickerFragment) {
            configureTimePicker((TimePickerFragment) fragment);
        }
    }

    private void configureTimePicker(TimePickerFragment timeFragment) {
        boolean isTimeNotEnabled = switchTimeOptional.isChecked();
        timeFragment.setTimePickerEnabled(!isTimeNotEnabled);

        if (isTimeNotEnabled) {
            timeFragment.setTime(0, 0);
        }
    }
}