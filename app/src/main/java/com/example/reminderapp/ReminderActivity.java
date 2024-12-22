package com.example.reminderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.reminderapp.Entities.Reminder;

import java.util.Locale;
import java.util.Objects;

public class ReminderActivity extends AppCompatActivity {
    EditText editText_title_reminder, editText_description_reminder;
    TimePicker timePicker;
    DatePicker datePicker;
    Button setReminder;
    Reminder reminder;

    boolean isOldReminder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        editText_title_reminder = findViewById(R.id.editText_title_reminder);
        editText_description_reminder = findViewById(R.id.editText_description_reminder);
        timePicker = findViewById(R.id.timePicker_reminder);
        datePicker = findViewById(R.id.datePicker_reminder);
        setReminder = findViewById(R.id.set_reminder_button);

        reminder = new Reminder();
        try {
            reminder = (Reminder) getIntent().getSerializableExtra("old_reminder");
            if (reminder != null) {
                editText_title_reminder.setText(reminder.getTitle());
                editText_description_reminder.setText(reminder.getDescription());
                String[] timeParts = reminder.getTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                timePicker.setHour(hour);
                timePicker.setMinute(minute);

                String[] dateParts = reminder.getDate().split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int day = Integer.parseInt(dateParts[2]);
                datePicker.updateDate(year, month, day);

                isOldReminder = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_title_reminder.getText().toString();
                String description = editText_description_reminder.getText().toString();

                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);

                if (title.isEmpty()) {
                    Toast.makeText(ReminderActivity.this, "Add title", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isOldReminder) {
                    reminder = new Reminder();
                }

                reminder.setTitle(title);
                reminder.setDescription(description);
                reminder.setTime(time);
                reminder.setDate(date);
                reminder.setCategory("default");

                Intent intent = new Intent();
                intent.putExtra("reminder", reminder);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}