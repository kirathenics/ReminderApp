package com.example.reminderapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReminderAddActivity extends AppCompatActivity {

    private AppDatabase appDatabase;

    private TextInputEditText reminderTitleEditText;
    private TextInputLayout reminderTitleTextInputLayout;
    private TextView reminderDescriptionTextView;
    private TextInputEditText reminderDescriptionEditText;
    private TextInputLayout reminderDescriptionTextInputLayout;
    private boolean isTitleChanged = false;
    private boolean isDescriptionVisible = false;

    private TextView timeDateTextView;
    private ImageButton removeDateTimeButton;
    private MaterialCardView timeDateCardView;

    private TextView timeTextView;
    private TextView dateTextView;
    private long selectedTime = 0;
    private long selectedDate = 0;

    private TextView chooseCategoryTextView;
    FloatingActionButton addAlarmButton;

    Reminder newReminder = null;
    int reminderPosition;
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

        reminderTitleEditText = findViewById(R.id.reminder_title_edit_text);
        reminderTitleTextInputLayout = findViewById(R.id.reminder_title_text_layout);
        reminderDescriptionTextView = findViewById(R.id.reminder_description_text_view);
        reminderDescriptionTextInputLayout = findViewById(R.id.reminder_description_text_layout);
        reminderDescriptionEditText = findViewById(R.id.reminder_description_edit_text);
        reminderDescriptionTextView.setOnClickListener(view -> toggleDescriptionVisibility());

        timeDateTextView = findViewById(R.id.time_date_text_view);
//        timeDateTextView.setVisibility(View.GONE);

        timeDateCardView = findViewById(R.id.time_date_card_view);
//        timeDateCardView.setVisibility(View.GONE);

        timeTextView = findViewById(R.id.time_text_view);
        dateTextView = findViewById(R.id.date_text_view);

        chooseCategoryTextView = findViewById(R.id.reminder_choose_category_text_view);

        ImageButton addCategoryImageButton = findViewById(R.id.reminder_add_category_image_button);
        addCategoryImageButton.setOnClickListener(v -> {
            CategoryDialogFragment dialogFragment = CategoryDialogFragment.newInstance();
            dialogFragment.setOnCategoryUpdatedListener(newCategory -> {
                new Thread(() -> appDatabase.categoryDAO().insert(newCategory)).start();
                runOnUiThread(() -> chooseCategoryTextView.setText(newCategory.getName()));
            });
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        chooseCategoryTextView.setOnClickListener(v -> {
            ChooseCategoryDialogFragment dialogFragment = ChooseCategoryDialogFragment.newInstance();
            dialogFragment.setOnCategoryChoseListener(chosenCategory -> {
                        selectedCategory = chosenCategory;
                        changeCategory();
                    });
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        addAlarmButton = findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance();
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        removeDateTimeButton = findViewById(R.id.remove_date_time_button);
//        removeDateTimeButton.setVisibility(View.GONE);
        removeDateTimeButton.setOnClickListener(v -> {
            timeTextView.setText(R.string.zero_time);
            dateTextView.setText(R.string.zero_date);
            addAlarmButton.setVisibility(View.VISIBLE);
            timeDateTextView.setVisibility(View.GONE);
            removeDateTimeButton.setVisibility(View.GONE);
            timeDateCardView.setVisibility(View.GONE);
        });

        timeTextView.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(true, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        ImageButton removeTimeButton = findViewById(R.id.remove_time_button);
        removeTimeButton.setOnClickListener(v -> timeTextView.setText(R.string.zero_time));

        dateTextView.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        ImageButton changeDateButton = findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, selectedTime, selectedDate);
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        selectedCategory = (Category) getIntent().getSerializableExtra("selected_category");

        if (selectedCategory != null) {
            changeCategory();
        } else {
            chooseCategoryTextView.setText(R.string.category_name_by_default);
        }

        newReminder = (Reminder) getIntent().getSerializableExtra("selected_reminder");
        if (newReminder != null) {
            populateReminderData(newReminder);
        } else {
            newReminder = new Reminder();
//            reminderDescriptionTextInputLayout.setVisibility(View.GONE);
            timeDateCardView.setVisibility(View.GONE);
            timeDateTextView.setVisibility(View.GONE);
            removeDateTimeButton.setVisibility(View.GONE);
            removeDateTimeButton.setVisibility(View.GONE);
            timeTextView.setText(R.string.zero_time);
            dateTextView.setText(R.string.zero_date);
        }

        reminderPosition = getIntent().getIntExtra("position", -1);

        reminderTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTitleChanged = true;

                if (reminderTitleTextInputLayout != null) {
                    reminderTitleTextInputLayout.setError(null);
                    reminderTitleTextInputLayout.setBoxStrokeColor(ContextCompat.getColor(ReminderAddActivity.this, R.color.lavender_dark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    isTitleChanged = false;
                }
            }
        });
    }

    private void changeCategory() {
        chooseCategoryTextView.setText(selectedCategory.getName());
        MaterialCardView reminderSummaryCardView = findViewById(R.id.reminder_summary_card_view);
        String hexColor = selectedCategory.getColor();
        int color = Color.parseColor(hexColor);
        reminderSummaryCardView.setStrokeColor(color);
    }

    private void populateReminderData(Reminder reminder) {
        reminderTitleEditText.setText(reminder.getTitle());

//        if (!reminder.getDescription().isEmpty()) {
//            reminderDescriptionEditText.setText(reminder.getDescription());
//            isDescriptionVisible = true;
//            toggleDescriptionVisibility();
//        }

        if (!reminder.getDescription().isEmpty()) {
            reminderDescriptionEditText.setText(reminder.getDescription());
            isDescriptionVisible = true;
            reminderDescriptionTextInputLayout.setVisibility(View.VISIBLE);
            reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down, 0);
        } else {
            isDescriptionVisible = false;
            reminderDescriptionTextInputLayout.setVisibility(View.GONE);
            reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
        }

        selectedTime = reminder.getTime();
        selectedDate = reminder.getDate();

        if (selectedDate > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeTextView.setText(timeFormat.format(new Date(selectedTime)));
            timeTextView.setVisibility(View.VISIBLE);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateTextView.setText(dateFormat.format(new Date(selectedDate)));
            dateTextView.setVisibility(View.VISIBLE);

            TextView howManyDateTextView = findViewById(R.id.how_many_time_difference_text_view);
            Utils.calculateAndDisplayDifference(selectedTime, selectedDate, howManyDateTextView,  ReminderAddActivity.this);
        } else {
            timeTextView.setText(R.string.zero_time);
            timeTextView.setVisibility(View.GONE);

            dateTextView.setText(R.string.zero_date);
            dateTextView.setVisibility(View.GONE);
        }

        if (selectedDate > 0) {
            timeDateCardView.setVisibility(View.VISIBLE);
            removeDateTimeButton.setVisibility(View.VISIBLE);
            timeDateTextView.setVisibility(View.VISIBLE);
            addAlarmButton.setVisibility(View.GONE);
        } else {
            timeDateCardView.setVisibility(View.GONE);
            removeDateTimeButton.setVisibility(View.GONE);
            timeDateTextView.setVisibility(View.GONE);
            addAlarmButton.setVisibility(View.VISIBLE);
        }
    }

    private void toggleDescriptionVisibility() {
        if (isDescriptionVisible) {
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

        isDescriptionVisible = !isDescriptionVisible;
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
            String title = Objects.requireNonNull(reminderTitleEditText.getText()).toString().trim();

            if (title.isEmpty()) {
                showError(getString(R.string.error_empty_reminder_title));
                return false;
            }

            if (isTitleChanged) {
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
//            newReminder.setPriority();
//        newReminder.setRepeat();
            newReminder.setCategoryId(appDatabase.categoryDAO().findByName(chooseCategoryTextView.getText().toString()).getId());
//        newReminder.setCreatedAt(new Date().toString());
//        newReminder.setUpdatedAt(new Date().toString());

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

                addAlarmButton.setVisibility(View.GONE);
                timeDateTextView.setVisibility(View.VISIBLE);
                removeDateTimeButton.setVisibility(View.VISIBLE);
                timeDateCardView.setVisibility(View.VISIBLE);

                TextView howManyDateTextView = findViewById(R.id.how_many_time_difference_text_view);
                Utils.calculateAndDisplayDifference(selectedTime, selectedDate, howManyDateTextView,  ReminderAddActivity.this);
            } catch (ParseException e) {
                Log.e("ChooseTimeDateDialog", "Error parsing date or time", e);
            }
        }
    };
}