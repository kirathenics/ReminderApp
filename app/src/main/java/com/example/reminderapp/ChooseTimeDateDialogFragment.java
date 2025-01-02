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
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;


public class ChooseTimeDateDialogFragment extends DialogFragment {

    public static final String TAG = "ChooseTimeDateDialogFragment";

    private static final String ARG_INITIAL_FRAGMENT = "initial_fragment";
    private static final String ARG_INITIAL_TIME = "initial_time";
    private static final String ARG_INITIAL_DATE = "initial_date";

    private OnDateTimeSelectedListener listener;
    SwitchCompat switchTimeOptional;

    TimePickerFragment timeFragment;
    DatePickerFragment dateFragment;

    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(String time, String date);
    }

    public static ChooseTimeDateDialogFragment newInstance() {
        return new ChooseTimeDateDialogFragment();
    }

    public static ChooseTimeDateDialogFragment newInstance(boolean showTimeInitially, long initialTimeMillis, long initialDateMillis) {
        ChooseTimeDateDialogFragment fragment = new ChooseTimeDateDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_INITIAL_FRAGMENT, showTimeInitially);
        args.putLong(ARG_INITIAL_TIME, initialTimeMillis);
        args.putLong(ARG_INITIAL_DATE, initialDateMillis);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDateTimeSelectedListener(OnDateTimeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timeFragment = new TimePickerFragment();
        dateFragment = new DatePickerFragment();

        if (getArguments() != null) {
            long initialTimeMillis = getArguments().getLong(ARG_INITIAL_TIME, 0);
            long initialDateMillis = getArguments().getLong(ARG_INITIAL_DATE, 0);

            if (initialTimeMillis > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(initialTimeMillis);
                timeFragment.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            }

            if (initialDateMillis > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(initialDateMillis);
                dateFragment.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_choose_time_date, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.time_date_group_picker);
        switchTimeOptional = view.findViewById(R.id.switch_time_optional);

        boolean showTimeInitially = getArguments() != null && getArguments().getBoolean(ARG_INITIAL_FRAGMENT, false);
        showFragment(showTimeInitially ? timeFragment : dateFragment);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_date) {
                showFragment(dateFragment);
            } else if (checkedId == R.id.radio_time) {
                showFragment(timeFragment);
            }
        });

        switchTimeOptional.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (timeFragment != null) {
                configureTimePicker(timeFragment);
            }
        });

        MaterialButton cancelButton = view.findViewById(R.id.cancel_date_time_button);
        cancelButton.setOnClickListener(v -> dismiss());

        MaterialButton okButton = view.findViewById(R.id.ok_date_time_button);
        okButton.setOnClickListener(v -> {
            String time = "";
            if (timeFragment != null) {
                int hour = timeFragment.getHour();
                int minute = timeFragment.getMinute();
                time = String.format("%02d:%02d", hour, minute);
            }

            int year = 0, month = 0, day = 0;
            if (dateFragment != null) {
                year = dateFragment.getYear();
                month = dateFragment.getMonth() + 1;
                day = dateFragment.getDay();
            }
            String date = String.format("%04d-%02d-%02d", year, month, day);

            if (listener != null) {
                listener.onDateTimeSelected(time, date);
            }

            dismiss();
        });

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        if (fragment == dateFragment) {
            transaction.show(dateFragment);
            transaction.hide(timeFragment);
        } else {
            transaction.show(timeFragment);
            transaction.hide(dateFragment);
        }

        if (!dateFragment.isAdded()) {
            transaction.add(R.id.frame_picker_container, dateFragment, "DatePicker");
        }
        if (!timeFragment.isAdded()) {
            transaction.add(R.id.frame_picker_container, timeFragment, "TimePicker");
        }

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