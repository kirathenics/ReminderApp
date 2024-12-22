package com.example.reminderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.reminderapp.Entities.Reminder;

import java.util.Calendar;
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

                if (isOldReminder) {
                    cancelReminderAlarm(reminder);
                }

                setReminderAlarm(reminder);

                Intent intent = new Intent();
                intent.putExtra("reminder", reminder);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminderAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("description", reminder.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        String[] dateParts = reminder.getDate().split("-");
        String[] timeParts = reminder.getTime().split(":");
        calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void cancelReminderAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}