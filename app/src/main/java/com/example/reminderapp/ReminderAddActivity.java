package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Reminder;
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
    private TextView reminderDescriptionTextView;
    private TextInputEditText reminderDescriptionEditText;
    private TextInputLayout reminderDescriptionTextInputLayout;
    private boolean isDescriptionVisible = false;

    private TextView timeDateTextView;
    private ImageButton removeDateTimeButton;
    private CardView timeDateCardView;

    private TextView timeTextView;
    private TextView dateTextView;
    private long selectedTime = 0;
    private long selectedDate = 0;

    private TextView chooseCategoryTextView;
    FloatingActionButton addAlarmButton;

    Reminder newReminder = new Reminder();

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

        String selectedCategory = getIntent().getStringExtra("selected_category");

        chooseCategoryTextView = findViewById(R.id.reminder_choose_category_text_view);
        if (selectedCategory != null) {
            chooseCategoryTextView.setText(selectedCategory);
        }
        if (selectedCategory != null && selectedCategory.equals(getString(R.string.category_name_all))) {
            chooseCategoryTextView.setText(R.string.category_name_by_default);
        }

        appDatabase = AppDatabase.getInstance(this);

        reminderTitleEditText = findViewById(R.id.reminder_title_edit_text);
        reminderDescriptionTextView = findViewById(R.id.reminder_description_text_view);
        reminderDescriptionTextInputLayout = findViewById(R.id.reminder_description_text_layout);
        reminderDescriptionEditText = findViewById(R.id.reminder_description_edit_text);
        reminderDescriptionTextView.setOnClickListener(view -> toggleDescriptionVisibility());

        timeDateTextView = findViewById(R.id.time_date_text_view);
        timeDateTextView.setVisibility(View.GONE);

        timeDateCardView = findViewById(R.id.time_date_card_view);
        timeDateCardView.setVisibility(View.GONE);

        timeTextView = findViewById(R.id.time_text_view);
        dateTextView = findViewById(R.id.date_text_view);

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
            dialogFragment.setOnCategoryChoseListener(chosenCategory -> chooseCategoryTextView.setText(chosenCategory.getName()));
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        addAlarmButton = findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance();
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        removeDateTimeButton = findViewById(R.id.remove_date_time_button);
        removeDateTimeButton.setVisibility(View.GONE);
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
            if (!title.isEmpty()) {
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
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Unable to create a reminder without a title. Please enter a title.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
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
            setResult(RESULT_OK, resultIntent);

            finish();
            return true;
        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    private void calculateAndDisplayDifference() {
        Date currentDate = new Date();
        long currentTimeMillis = currentDate.getTime();

        long totalDifferenceInMillis = selectedDate + selectedTime - currentTimeMillis;
        long totalDifferenceInMinutes = (totalDifferenceInMillis / (1000 * 60)) % 60;
        long totalDifferenceInHours = (totalDifferenceInMillis / (1000 * 60 * 60)) % 24;
        long totalDifferenceInDays = totalDifferenceInMillis / (1000 * 60 * 60 * 24);

        TextView howManyDateTextView = findViewById(R.id.how_many_time_difference_text_view);

        if (totalDifferenceInMillis < 0) {
            howManyDateTextView.setText(R.string.selected_time_has_already_passed);
            return;
        }

        if (totalDifferenceInDays == 0) {
            if (totalDifferenceInHours < 1) {
                howManyDateTextView.setText(String.format("Today, in %d minutes", totalDifferenceInMinutes));
            } else {
                howManyDateTextView.setText(String.format("Today, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
            }
        } else if (totalDifferenceInDays == 1) {
            howManyDateTextView.setText(String.format("Tomorrow, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
        } else {
            howManyDateTextView.setText(String.format("In %d days", totalDifferenceInDays));
        }
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

                calculateAndDisplayDifference();
            } catch (ParseException e) {
                Log.e("ChooseTimeDateDialog", "Error parsing date or time", e);
            }
        }
    };
}