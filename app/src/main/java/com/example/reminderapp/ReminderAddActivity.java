package com.example.reminderapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class ReminderAddActivity extends AppCompatActivity {

    private AppDatabase appDatabase;

    private TextInputEditText reminderTitleEditText;
    private TextInputLayout reminderTitleTextInputLayout;
    private TextView reminderDescriptionTextView;
    private TextInputEditText reminderDescriptionEditText;
    private TextInputLayout reminderDescriptionTextInputLayout;
    private boolean isDescriptionNotVisible = false;

    private TextView timeDateTextView;
    private ImageButton removeDateTimeButton;
    private MaterialCardView timeDateCardView;
    TextView howManyTimeDifferenceTextView;

    private TextView timeTextView;
    private TextView dateTextView;
    private long selectedTime = 0;
    private long selectedDate = 0;

    ImageButton addRepeatPatternButton;
    ImageButton deleteRepeatPatternButton;

    LinearLayout chooseWeekdayLinearLayout;

    LinearLayout selectedRepeatPatternLinearLayout;
    TextView selectedRepeatPatternTextView;
    ImageButton editRepeatPatternButton;

    LinearLayout nextTimeRepeatLinearLayout;
    TextView nextTimeRepeatInfoTextView;

    LinearLayout stopRepeatReminderLinearLayout;
    TextView stopRepeatInfoTextView;
    ImageButton addStopRepeatButton;
    ImageButton deleteStopRepeatButton;

    LinearLayout selectedStopRepeatReminderLinearLayout;
    private TextView stopRepeatTimeTextView;
    private TextView stopRepeatDateTextView;
    private TextView stopRepeatTimeDifferenceTextView;
    ImageButton editStopRepeatButton;

    private int selectedRepeatValue = 1;
    private String selectedRepeatPattern = "day";

    private long selectedStopRepeatTime = 0;
    private long selectedStopRepeatDate = 0;

    private TextView chooseCategoryTextView;
    FloatingActionButton addAlarmButton;

    Reminder newReminder = null;
    int reminderPosition;
    private boolean isReminderChanged = false;
    Category selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        Toolbar toolbar = findViewById(R.id.reminder_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_add_reminder);
        }

        appDatabase = AppDatabase.getInstance(this);

        initializeViews();
        setOnClickListeners();

        selectedCategory = (Category) getIntent().getSerializableExtra("selected_category");

        if (selectedCategory == null) {
            selectedCategory = appDatabase.categoryDAO().findByName(this.getString(R.string.category_name_by_default));
        }
        changeCategory();

        newReminder = (Reminder) getIntent().getSerializableExtra("selected_reminder");
        if (newReminder != null) {
            populateReminderData(newReminder);
        } else {
            newReminder = new Reminder();
            toggleTimeDateVisibility(false);
        }

        reminderPosition = getIntent().getIntExtra("position", -1);

        reminderTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isReminderChanged = true;

                if (reminderTitleTextInputLayout != null) {
                    reminderTitleTextInputLayout.setError(null);
                    reminderTitleTextInputLayout.setBoxStrokeColor(ContextCompat.getColor(ReminderAddActivity.this, R.color.lavender_dark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        reminderDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isReminderChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_add_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isReminderChanged) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Exit")
                        .setMessage("You have unsaved changes. Do you really want to go back?")
                        .setPositiveButton("Yes", (dialog, which) -> finish())
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }

            finish();
            return true;
        }
        else if (itemId == R.id.add_reminder_item) {
            String title = Objects.requireNonNull(reminderTitleEditText.getText()).toString().trim();

            if (title.isEmpty()) {
                showError(getString(R.string.error_empty_reminder_title));
                return false;
            }

            if (!isReminderTitleUnique(title)) {
                showError(getString(R.string.error_reminder_exists));
                return false;
            }

            newReminder.setTitle(title);
            newReminder.setDescription(Objects.requireNonNull(reminderDescriptionEditText.getText()).toString());

            newReminder.setTime(selectedTime);
            newReminder.setDate(selectedDate);

            newReminder.setCompleted(false);

            if (newReminder.getRepeatType() == null) {
                newReminder.setRepeatPattern(null);
                newReminder.setRepeatValue(0);
                newReminder.setRepeatDays(null);
            }
            else if (newReminder.getRepeatType().equals("weekly")) {
                newReminder.setRepeatPattern(null);
                newReminder.setRepeatValue(0);
            } else if (newReminder.getRepeatType().equals("periodic")) {
                newReminder.setRepeatPattern(selectedRepeatPattern);
                newReminder.setRepeatValue(selectedRepeatValue);
                newReminder.setRepeatDays(null);
            }

            newReminder.setEndDate(selectedStopRepeatTime + selectedStopRepeatDate);

            newReminder.setCategoryId(appDatabase.categoryDAO().findByName(chooseCategoryTextView.getText().toString()).getId());

            Log.i("save reminder info", newReminder.toString());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_reminder", newReminder);
            resultIntent.putExtra("position", reminderPosition);
            setResult(RESULT_OK, resultIntent);

            finish();
            return true;
        }
        return false;
    }

    private boolean isReminderTitleUnique(String title) {
        Reminder existingReminder = AppDatabase.getInstance(this).reminderDAO().findByTitle(title);
        return existingReminder == null || (newReminder != null && existingReminder.getId() == newReminder.getId());
    }

    private void showError(String message) {
        reminderTitleTextInputLayout.setError(message);
        reminderTitleTextInputLayout.setErrorTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.prismatic_red)));
        reminderTitleTextInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.prismatic_red));
    }

    public void initializeViews() {
        reminderTitleEditText = findViewById(R.id.reminder_title_edit_text);
        reminderTitleTextInputLayout = findViewById(R.id.reminder_title_text_layout);

        reminderDescriptionTextView = findViewById(R.id.reminder_description_text_view);
        reminderDescriptionTextInputLayout = findViewById(R.id.reminder_description_text_layout);
        reminderDescriptionTextInputLayout.setVisibility(View.GONE);
        reminderDescriptionEditText = findViewById(R.id.reminder_description_edit_text);

        chooseCategoryTextView = findViewById(R.id.reminder_choose_category_text_view);

        timeDateTextView = findViewById(R.id.time_date_text_view);
        timeDateCardView = findViewById(R.id.time_date_card_view);
        timeTextView = findViewById(R.id.time_text_view);
        dateTextView = findViewById(R.id.date_text_view);
        howManyTimeDifferenceTextView = findViewById(R.id.how_many_time_difference_text_view);

        chooseWeekdayLinearLayout = findViewById(R.id.choose_weekday_linear_layout);
        setupWeekdayCheckBoxListeners();

        selectedRepeatPatternLinearLayout = findViewById(R.id.selected_repeat_pattern_linear_layout);
        selectedRepeatPatternLinearLayout.setVisibility(View.GONE);
        selectedRepeatPatternTextView = findViewById(R.id.selected_repeat_pattern_text_view);
        editRepeatPatternButton = findViewById(R.id.edit_repeat_pattern_button);

        nextTimeRepeatLinearLayout = findViewById(R.id.next_time_repeat_linear_layout);
        nextTimeRepeatLinearLayout.setVisibility(View.GONE);
        nextTimeRepeatInfoTextView = findViewById(R.id.next_time_repeat_info_text_view);

        stopRepeatReminderLinearLayout = findViewById(R.id.stop_repeat_reminder_linear_layout);
        stopRepeatReminderLinearLayout.setVisibility(View.GONE);
        stopRepeatInfoTextView = findViewById(R.id.stop_repeat_info_text_view);

        selectedStopRepeatReminderLinearLayout = findViewById(R.id.selected_stop_repeat_reminder_linear_layout);
        selectedStopRepeatReminderLinearLayout.setVisibility(View.GONE);
        stopRepeatTimeTextView = findViewById(R.id.stop_repeat_time_text_view);
        stopRepeatDateTextView = findViewById(R.id.stop_repeat_date_text_view);
        stopRepeatTimeDifferenceTextView = findViewById(R.id.stop_repeat_time_difference_text_view);
    }

    public void setOnClickListeners() {
        reminderDescriptionTextView.setOnClickListener(view -> toggleDescriptionVisibility());

        ImageButton addCategoryImageButton = findViewById(R.id.reminder_add_category_image_button);
        addCategoryImageButton.setOnClickListener(v -> {
            CategoryEditDialogFragment dialogFragment = CategoryEditDialogFragment.newInstance();
            dialogFragment.setOnCategoryUpdatedListener(newCategory -> {
                new Thread(() -> appDatabase.categoryDAO().insert(newCategory)).start();
                runOnUiThread(() -> chooseCategoryTextView.setText(newCategory.getName()));
            });
            dialogFragment.show(getSupportFragmentManager(), CategoryEditDialogFragment.TAG);
        });

        chooseCategoryTextView.setOnClickListener(v -> {
            ChooseCategoryDialogFragment dialogFragment = ChooseCategoryDialogFragment.newInstance();
            dialogFragment.setOnCategoryChoseListener(chosenCategory -> {
                selectedCategory = chosenCategory;
                isReminderChanged = true;
                changeCategory();
            });
            dialogFragment.show(getSupportFragmentManager(), ChooseCategoryDialogFragment.TAG);
        });

        addAlarmButton = findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance();
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });

        removeDateTimeButton = findViewById(R.id.remove_date_time_button);
        removeDateTimeButton.setOnClickListener(v -> {
            selectedTime = 0;
            selectedDate = 0;

            newReminder.setRepeatType(null);

            selectedStopRepeatTime = 0;
            selectedStopRepeatDate = 0;

            nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            toggleTimeDateVisibility(false);
        });

        timeTextView.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(true, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });

        ImageButton removeTimeButton = findViewById(R.id.remove_time_button);
        removeTimeButton.setOnClickListener(v -> {
            timeTextView.setText(R.string.zero_time);
            selectedTime = 0;
        });

        dateTextView.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });

        ImageButton changeDateButton = findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });

        addRepeatPatternButton = findViewById(R.id.add_repeat_pattern_button);
        addRepeatPatternButton.setOnClickListener(v -> {
            ChooseRepeatTimeDialogFragment dialogFragment = ChooseRepeatTimeDialogFragment.newInstance(selectedRepeatValue, selectedRepeatPattern);
            dialogFragment.setOnRepeatTimeSelectedListener(onRepeatTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseRepeatTimeDialogFragment.TAG);
        });

        deleteRepeatPatternButton = findViewById(R.id.delete_repeat_pattern_button);
        deleteRepeatPatternButton.setVisibility(View.GONE);
        deleteRepeatPatternButton.setOnClickListener(v -> {
            nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            newReminder.setRepeatType(null);

            toggleRepeatPatternTimeVisibility(false);
            toggleStopRepeatTimeVisibility(false);
        });

        editRepeatPatternButton.setOnClickListener(v -> {
            ChooseRepeatTimeDialogFragment dialogFragment = ChooseRepeatTimeDialogFragment.newInstance(selectedRepeatValue, selectedRepeatPattern);
            dialogFragment.setOnRepeatTimeSelectedListener(onRepeatTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseRepeatTimeDialogFragment.TAG);
        });

        addStopRepeatButton = findViewById(R.id.add_stop_repeat_button);
        addStopRepeatButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedStopRepeatTime, selectedStopRepeatDate);
            dialogFragment.setOnDateTimeSelectedListener(onStopRepeatDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });

        deleteStopRepeatButton = findViewById(R.id.delete_stop_repeat_button);
        deleteStopRepeatButton.setVisibility(View.GONE);
        deleteStopRepeatButton.setOnClickListener(v -> {
            selectedStopRepeatTime = 0;
            selectedStopRepeatDate = 0;

            nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            toggleStopRepeatTimeVisibility(false);
        });

        editStopRepeatButton = findViewById(R.id.edit_stop_repeat_button);
        editStopRepeatButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedStopRepeatTime, selectedStopRepeatDate);
            dialogFragment.setOnDateTimeSelectedListener(onStopRepeatDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), ChooseTimeDateDialogFragment.TAG);
        });
    }

    private void setupWeekdayCheckBoxListeners() {
        for (int i = 0; i < chooseWeekdayLinearLayout.getChildCount(); i++) {
            View child = chooseWeekdayLinearLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                int dayIndex = i + 1;

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        onDaySelected(dayIndex);
                    } else {
                        onDayUnselected(dayIndex);
                    }
                });
            }
        }
    }

    private void onDaySelected(int dayIndex) {
        if (!newReminder.getRepeatDays().contains(dayIndex)) {
            newReminder.setRepeatType("weekly");
            newReminder.getRepeatDays().add(dayIndex);
            Collections.sort(newReminder.getRepeatDays());
            toggleRepeatWeekVisibility(true);
        }
        setTextNextTimeRepeatInfoRepeatDays();
    }

    private void onDayUnselected(int dayIndex) {
        newReminder.getRepeatDays().remove(Integer.valueOf(dayIndex));
        setTextNextTimeRepeatInfoRepeatDays();
    }

    private void loadSelectedWeekdays() {
        List<Integer> repeatDays = newReminder.getRepeatDays();
        if (repeatDays == null || repeatDays.isEmpty()) return;

        for (int i = 0; i < chooseWeekdayLinearLayout.getChildCount(); i++) {
            View child = chooseWeekdayLinearLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(repeatDays.contains(i + 1));
            }
        }
    }

    private void populateReminderData(Reminder reminder) {
        reminderTitleEditText.setText(reminder.getTitle());

        if (!reminder.getDescription().isEmpty()) {
            reminderDescriptionEditText.setText(reminder.getDescription());
            isDescriptionNotVisible = false;
        } else {
            isDescriptionNotVisible = true;
        }
        toggleDescriptionVisibility();

        selectedTime = reminder.getTime();
        selectedDate = reminder.getDate();

        if (selectedDate > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeTextView.setText(timeFormat.format(new Date(selectedTime)));
            timeTextView.setVisibility(View.VISIBLE);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
            dateTextView.setText(dateFormat.format(new Date(selectedDate)));
            dateTextView.setVisibility(View.VISIBLE);

            setTextHowManyTimeDifference();

            toggleTimeDateVisibility(true);
        } else {
            toggleTimeDateVisibility(false);
        }

        if (newReminder.getRepeatType() != null) {
            if (newReminder.getRepeatType().equals("weekly")) {
                loadSelectedWeekdays();
                setTextNextTimeRepeatInfoRepeatDays();
                toggleRepeatWeekVisibility(true);
            }
            else if (newReminder.getRepeatType().equals("periodic")) {
                selectedRepeatValue = newReminder.getRepeatValue();
                selectedRepeatPattern = newReminder.getRepeatPattern();

                setTextSelectedRepeatPattern();
                setTextNextTimeRepeatInfoRepeatValue();

                toggleRepeatPatternTimeVisibility(true);

//                onRepeatTimeSelectedListener.onRepeatTimeSelected(newReminder.getRepeatValue(), newReminder.getRepeatPattern());
            }
        }

        long selectedTimeDate = reminder.getEndDate();
        if (selectedTimeDate > 0) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(selectedTimeDate);
            calendar.set(Calendar.YEAR, 0);
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            selectedStopRepeatTime = calendar.getTimeInMillis();

            calendar.setTimeInMillis(selectedTimeDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedStopRepeatDate = calendar.getTimeInMillis();

            setTextStopRepeatTimeDifference();
            setTextNextTimeRepeatInfo();
//            checkIfWillRepeatValue();
            toggleStopRepeatTimeVisibility(true);
        }

        Log.i("load reminder info", newReminder.toString());
    }

    private void toggleDescriptionVisibility() {
        if (isDescriptionNotVisible) {
            if (TextUtils.isEmpty(reminderDescriptionEditText.getText())) {
                reminderDescriptionTextInputLayout.setVisibility(View.GONE);
                reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
            } else {
                reminderDescriptionTextInputLayout.setVisibility(View.GONE);
                reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up, 0);
            }
        } else {
            reminderDescriptionTextInputLayout.setVisibility(View.VISIBLE);
            reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down, 0);
        }

        isDescriptionNotVisible = !isDescriptionNotVisible;
    }

    private void changeCategory() {
        chooseCategoryTextView.setText(selectedCategory.getName());
        MaterialCardView reminderSummaryCardView = findViewById(R.id.reminder_summary_card_view);
        String hexColor = selectedCategory.getColor();
        int color = Color.parseColor(hexColor);
        reminderSummaryCardView.setStrokeColor(color);
    }

    private final ChooseTimeDateDialogFragment.OnDateTimeSelectedListener onDateTimeSelectedListener = new ChooseTimeDateDialogFragment.OnDateTimeSelectedListener() {
        @Override
        public void onDateTimeSelected(String time, String date) {
            try {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date parsedTime = timeFormat.parse(time);
                if (parsedTime != null) {
                    selectedTime = parsedTime.getTime();
                }
                timeTextView.setText(time);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsedDate = dateFormat.parse(date);
                if (parsedDate != null) {
                    selectedDate = parsedDate.getTime();
                }

                DateFormat dateFormatView = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
                dateTextView.setText(dateFormatView.format(parsedDate));

                setTextHowManyTimeDifference();
                setTextStopRepeatTimeDifference();
                setTextNextTimeRepeatInfo();

                toggleTimeDateVisibility(true);
            } catch (ParseException e) {
                Log.e("ChooseTimeDateDialog", "Error parsing date or time", e);
            }
        }
    };

    private void toggleTimeDateVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        int inverseVisibility = isVisible ? View.GONE : View.VISIBLE;

        timeDateTextView.setVisibility(visibility);
        timeDateCardView.setVisibility(visibility);
        addAlarmButton.setVisibility(inverseVisibility);
        removeDateTimeButton.setVisibility(visibility);
    }

    ChooseRepeatTimeDialogFragment.OnRepeatTimeSelectedListener onRepeatTimeSelectedListener = (repeatValue, repeatPattern) -> {
        selectedRepeatValue = repeatValue;
        selectedRepeatPattern = repeatPattern;

        newReminder.setRepeatType("periodic");

        setTextSelectedRepeatPattern();
        setTextNextTimeRepeatInfoRepeatValue();

        toggleRepeatPatternTimeVisibility(true);
    };

    private void toggleRepeatWeekVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;

        nextTimeRepeatLinearLayout.setVisibility(visibility);
        stopRepeatReminderLinearLayout.setVisibility(visibility);
    }

    private void toggleRepeatPatternTimeVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        int inverseVisibility = isVisible ? View.GONE : View.VISIBLE;

        addRepeatPatternButton.setVisibility(inverseVisibility);
        deleteRepeatPatternButton.setVisibility(visibility);

        chooseWeekdayLinearLayout.setVisibility(inverseVisibility);
        selectedRepeatPatternLinearLayout.setVisibility(visibility);
        nextTimeRepeatLinearLayout.setVisibility(visibility);

        stopRepeatReminderLinearLayout.setVisibility(visibility);
    }

    private final ChooseTimeDateDialogFragment.OnDateTimeSelectedListener onStopRepeatDateTimeSelectedListener = new ChooseTimeDateDialogFragment.OnDateTimeSelectedListener() {
        @Override
        public void onDateTimeSelected(String time, String date) {
            try {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date parsedTime = timeFormat.parse(time);
                if (parsedTime != null) {
                    selectedStopRepeatTime = parsedTime.getTime();
                }
                stopRepeatTimeTextView.setText(time);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsedDate = dateFormat.parse(date);
                if (parsedDate != null) {
                    selectedStopRepeatDate = parsedDate.getTime();
                }

                DateFormat dateFormatView = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                stopRepeatDateTextView.setText(dateFormatView.format(parsedDate));

                setTextStopRepeatTimeDifference();
                setTextNextTimeRepeatInfo();
//                checkIfWillRepeatValue();

                toggleStopRepeatTimeVisibility(true);
            } catch (ParseException e) {
                Log.e("ChooseTimeDateDialog", "Error parsing date or time", e);
            }
        }
    };

    private void toggleStopRepeatTimeVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        int inverseVisibility = isVisible ? View.GONE : View.VISIBLE;

        addStopRepeatButton.setVisibility(inverseVisibility);
        stopRepeatInfoTextView.setVisibility(inverseVisibility);
        deleteStopRepeatButton.setVisibility(visibility);
        selectedStopRepeatReminderLinearLayout.setVisibility(visibility);
    }

    private void setTextHowManyTimeDifference() {
        howManyTimeDifferenceTextView.setText(Utils.calculateTimeDifference(selectedTime, selectedDate, ReminderAddActivity.this));
    }

    private void setTextStopRepeatTimeDifference() {
        if (selectedStopRepeatDate != 0) {
            stopRepeatTimeDifferenceTextView.setText(Utils.calculateTimeDifference(selectedStopRepeatTime, selectedStopRepeatDate, selectedTime + selectedDate, ReminderAddActivity.this));
        }
    }

    private void setTextSelectedRepeatPattern() {
        selectedRepeatPatternTextView.setText(Utils.updateRepeatTime(selectedRepeatValue, selectedRepeatPattern));
    }

    private void setTextNextTimeRepeatInfo() {
        if (newReminder.getRepeatType() != null) {
            if (newReminder.getRepeatType().equals("periodic")) {
                setTextNextTimeRepeatInfoRepeatValue();
            }
            else if (newReminder.getRepeatType().equals("weekly")) {
                setTextNextTimeRepeatInfoRepeatDays();
            }
        }
    }

    private void setTextNextTimeRepeatInfoRepeatValue() {
        nextTimeRepeatInfoTextView.setText(Utils.getNextTimeInfoText(selectedTime, selectedDate, selectedRepeatValue, selectedRepeatPattern, ReminderAddActivity.this));
        checkIfWillRepeatValue();
    }

    private void checkIfWillRepeatValue() {
        if (selectedStopRepeatDate > 0) {
            if ((selectedTime + selectedDate + Utils.getRepeatIntervalMillis(selectedRepeatValue, selectedRepeatPattern)) < (selectedStopRepeatTime + selectedStopRepeatDate)) {
                nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            else {
                nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

    private void setTextNextTimeRepeatInfoRepeatDays() {
        List<Integer> repeatDays = newReminder.getRepeatDays();
        if (repeatDays == null || repeatDays.isEmpty()) {
            newReminder.setRepeatType(null);
            toggleRepeatWeekVisibility(false);
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < selectedTime + selectedDate) {
            currentTimeMillis = selectedTime + selectedDate;
        }

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(currentTimeMillis);
        int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        int adjustedDayOfWeek = (currentDayOfWeek == Calendar.SUNDAY) ? 7 : currentDayOfWeek - 1;
        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCalendar.get(Calendar.MINUTE);

        Calendar reminderTimeCalendar = Calendar.getInstance();
        reminderTimeCalendar.setTimeInMillis(selectedTime);
        int reminderHour = reminderTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int reminderMinute = reminderTimeCalendar.get(Calendar.MINUTE);

        Integer nextDay = null;
        int minDayOffset = Integer.MAX_VALUE;

        for (int day : repeatDays) {
            int dayOffset = (day - adjustedDayOfWeek + 7) % 7;

            if (dayOffset == 0) {
                if (reminderHour > currentHour || (reminderHour == currentHour && reminderMinute > currentMinute)) {
                    nextDay = day;
                    minDayOffset = 0;
                    break;
                } else {
                    dayOffset = 7;
                }
            }

            if (dayOffset < minDayOffset) {
                minDayOffset = dayOffset;
                nextDay = day;
            }
        }

        if (nextDay != null) {
            Calendar nextReminderCalendar = Calendar.getInstance();
            nextReminderCalendar.setTimeInMillis(currentTimeMillis);
            nextReminderCalendar.add(Calendar.DAY_OF_YEAR, minDayOffset);
            nextReminderCalendar.set(Calendar.HOUR_OF_DAY, reminderHour);
            nextReminderCalendar.set(Calendar.MINUTE, reminderMinute);
            nextReminderCalendar.set(Calendar.SECOND, 0);
            nextReminderCalendar.set(Calendar.MILLISECOND, 0);

            long nextTime = nextReminderCalendar.getTimeInMillis() % (24 * 60 * 60 * 1000);
            long nextDate = nextReminderCalendar.getTimeInMillis() - nextTime;

            String nextTimeInfo = Utils.getNextTimeInfoText(nextTime, nextDate, 0, "week", this);

            nextTimeRepeatInfoTextView.setText(nextTimeInfo);
            checkIfWillRepeatDays(nextTime, nextDate);
            toggleRepeatWeekVisibility(true);
//            nextTimeRepeatLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void checkIfWillRepeatDays(long nextTime, long nextDate) {
        if (selectedStopRepeatDate > 0) {
            if ((nextTime + nextDate) < (selectedStopRepeatTime + selectedStopRepeatDate)) {
                nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            } else {
                nextTimeRepeatInfoTextView.setPaintFlags(nextTimeRepeatInfoTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }
}