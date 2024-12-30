package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAddActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();

    private TextInputEditText reminderTitleEditText;
    private TextView reminderDescriptionTextView;
    private TextInputEditText reminderDescriptionEditText;
    private TextInputLayout reminderDescriptionTextInputLayout;
    private boolean isDescriptionVisible = false;

    private TextView timeDateTextView;
    private ImageButton removeDateTimeButton;
    private CardView timeDateCardView;

    private TextView timeTextView;
    private ImageButton removeTimeButton;
    private TextView dateTextView;
    private ImageButton changeDateButton;

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
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAll();
            runOnUiThread(() -> {
            });
        }).start();

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
            dialogFragment.setOnCategoryUpdatedListener(newCategory -> new Thread(() -> appDatabase.categoryDAO().insert(newCategory)).start());
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
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(true, timeTextView.getText().toString(), dateTextView.getText().toString());
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        removeTimeButton = findViewById(R.id.remove_time_button);
        removeTimeButton.setOnClickListener(v -> timeTextView.setText(R.string.zero_time));

        dateTextView.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, timeTextView.getText().toString(), dateTextView.getText().toString());
            dialogFragment.setOnDateTimeSelectedListener(onDateTimeSelectedListener);
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        changeDateButton = findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(v -> {
            ChooseTimeDateDialogFragment dialogFragment = ChooseTimeDateDialogFragment.newInstance(false, timeTextView.getText().toString(), dateTextView.getText().toString());
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
            finish();
            return true;
        }
        else if (itemId == R.id.add_reminder_item) {
            newReminder.setTitle(reminderTitleEditText.getText().toString().trim());
            newReminder.setDescription(reminderDescriptionEditText.getText().toString());
//            newReminder.setTime();
//            newReminder.setDate();
            newReminder.setCompleted(false);
//            newReminder.setRepeat();
            newReminder.setCategoryId(appDatabase.categoryDAO().findByName(chooseCategoryTextView.getText().toString()).getId());
//            newReminder.setCreatedAt();
//            newReminder.setUpdatedAt();

            finish();
            return true;
        }
        return false;
    }

    private final ChooseTimeDateDialogFragment.OnDateTimeSelectedListener onDateTimeSelectedListener = new ChooseTimeDateDialogFragment.OnDateTimeSelectedListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onDateTimeSelected(String time, String date) {
            timeTextView.setText(time);
            dateTextView.setText(date);
            addAlarmButton.setVisibility(View.GONE);
            timeDateTextView.setVisibility(View.VISIBLE);
            removeDateTimeButton.setVisibility(View.VISIBLE);
            timeDateCardView.setVisibility(View.VISIBLE);

            try {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date selectedTime = timeFormat.parse(time);
                Date selectedDate = dateFormat.parse(date);

                Date currentDate = new Date();

                String todayString = dateFormat.format(currentDate);
                Date todayDate = dateFormat.parse(todayString);

                long differenceInDays = (selectedDate.getTime() - todayDate.getTime()) / (1000 * 60 * 60 * 24);

                TextView howManyDateTextView = findViewById(R.id.how_many_date_text_view);

                if (differenceInDays < 0 || (differenceInDays == 0 && selectedTime.before(timeFormat.parse(timeFormat.format(currentDate))))) {
                    howManyDateTextView.setText(R.string.selected_time_has_already_passed);
                } else {
                    long totalDifferenceInMillis = selectedDate.getTime() + selectedTime.getTime() - currentDate.getTime();
                    long totalDifferenceInMinutes = (totalDifferenceInMillis / (1000 * 60)) % 60;
                    long totalDifferenceInHours = (totalDifferenceInMillis / (1000 * 60 * 60)) % 24;
                    long totalDifferenceInDays = totalDifferenceInMillis / (1000 * 60 * 60 * 24);

                    if (differenceInDays == 0) {
                        if (totalDifferenceInHours < 1) {
                            howManyDateTextView.setText(String.format("Today, in %d minutes", totalDifferenceInMinutes));
                        } else {
                            howManyDateTextView.setText(String.format("Today, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
                        }
                    } else if (differenceInDays == 1) {
                        if (totalDifferenceInMillis >= (24 * 60 * 60 * 1000)) {
                            howManyDateTextView.setText(R.string.tomorrow);
                        } else {
                            howManyDateTextView.setText(String.format("Tomorrow, in %d hours and %d minutes", totalDifferenceInHours, totalDifferenceInMinutes));
                        }

                    } else {
                        howManyDateTextView.setText(String.format("In %d days", totalDifferenceInDays));
                    }
                }
            } catch (ParseException e) {
                Log.e("ChooseTimeDateDialog", "Error parsing date or time", e);
            }
        }
    };
}