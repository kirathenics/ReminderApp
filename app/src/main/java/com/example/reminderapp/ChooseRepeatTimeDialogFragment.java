package com.example.reminderapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

public class ChooseRepeatTimeDialogFragment extends DialogFragment {

    public static final String TAG = "ChooseRepeatTimeDialogFragment";

    private static final String ARG_INITIAL_REPEAT_VALUE = "initial_repeat_value";
    private static final String ARG_INITIAL_REPEAT_PATTERN = "initial_repeat_pattern";

    private OnRepeatTimeSelectedListener listener;

    NumberPicker chooseRepeatValueNumberPicker;
    NumberPicker chooseRepeatPatternNumberPicker;

    TextView chosenRepeatTimeTextView;

    String[] repeatPatterns;
    int[] maxValuesForPatterns = {120, 60, 60, 60, 60, 5}; // minutes, hours, days, weeks, months, years

    int chosenRepeatValue;
    String chosenRepeatPattern;

    public interface OnRepeatTimeSelectedListener {
        void onRepeatTimeSelected(int repeatValue, String repeatPattern);
    }

    public static ChooseRepeatTimeDialogFragment newInstance(int repeatValue, String repeatPattern) {
        ChooseRepeatTimeDialogFragment fragment = new ChooseRepeatTimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_REPEAT_VALUE, repeatValue);
        args.putString(ARG_INITIAL_REPEAT_PATTERN, repeatPattern);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRepeatTimeSelectedListener(OnRepeatTimeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            chosenRepeatValue = getArguments().getInt(ARG_INITIAL_REPEAT_VALUE, 1);
            chosenRepeatPattern = getArguments().getString(ARG_INITIAL_REPEAT_PATTERN, "day");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_choose_repeat_time, container, false);

        chooseRepeatValueNumberPicker = view.findViewById(R.id.choose_repeat_value_number_picker);
        chooseRepeatPatternNumberPicker = view.findViewById(R.id.choose_repeat_pattern_number_picker);
        chosenRepeatTimeTextView = view.findViewById(R.id.chosen_repeat_time_text_view);

        chooseRepeatValueNumberPicker.setMinValue(1);
        chooseRepeatValueNumberPicker.setMaxValue(maxValuesForPatterns[2]);
        chooseRepeatValueNumberPicker.setValue(chosenRepeatValue);

        repeatPatterns = getResources().getStringArray(R.array.repeat_patterns);
        chooseRepeatPatternNumberPicker.setMinValue(0);
        chooseRepeatPatternNumberPicker.setMaxValue(repeatPatterns.length - 1);
        chooseRepeatPatternNumberPicker.setDisplayedValues(repeatPatterns);
        int initialPatternIndex = 2;
        for (int i = 0; i < repeatPatterns.length; i++) {
            if (repeatPatterns[i].equals(chosenRepeatPattern)) {
                initialPatternIndex = i;
                break;
            }
        }
        chooseRepeatPatternNumberPicker.setValue(initialPatternIndex);

        chooseRepeatValueNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            chosenRepeatValue = newVal;
            chosenRepeatTimeTextView.setText(Utils.updateRepeatTime(chosenRepeatValue, chosenRepeatPattern));
        });

        chooseRepeatPatternNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            chosenRepeatPattern = repeatPatterns[newVal];
            chooseRepeatValueNumberPicker.setMaxValue(maxValuesForPatterns[newVal]);
            chosenRepeatTimeTextView.setText(Utils.updateRepeatTime(chosenRepeatValue, chosenRepeatPattern));
        });

        MaterialButton cancelButton = view.findViewById(R.id.cancel_date_time_button);
        cancelButton.setOnClickListener(v -> dismiss());

        MaterialButton okButton = view.findViewById(R.id.ok_date_time_button);
        okButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRepeatTimeSelected(chosenRepeatValue, chosenRepeatPattern);
            }

            dismiss();
        });

        return view;
    }
}