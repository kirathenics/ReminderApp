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

public class ReminderActivity extends AppCompatActivity {
    EditText editText_title_reminder, editText_description_reminder;
    TimePicker timePicker;
    DatePicker datePicker;
    Button setReminder;
    Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        editText_title_reminder = findViewById(R.id.editText_title_reminder);
        editText_description_reminder = findViewById(R.id.editText_description_reminder);
        timePicker = findViewById(R.id.timePicker_reminder);
        datePicker = findViewById(R.id.datePicker_reminder);
        setReminder = findViewById(R.id.set_reminder_button);

        setReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_title_reminder.getText().toString();
                String description = editText_description_reminder.getText().toString();
                String time = timePicker.toString();
                String date = datePicker.toString();

                if (title.isEmpty()) {
                    Toast.makeText(ReminderActivity.this, "Add title", Toast.LENGTH_LONG).show();
                    return;
                }

                reminder = new Reminder();
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