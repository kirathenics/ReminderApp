package com.example.reminderapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.reminderapp.Adapters.ReminderListAdapter;
import com.example.reminderapp.DB.ReminderDB;
import com.example.reminderapp.Entities.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView reminderRecyclerView;
    ReminderListAdapter reminderListAdapter;
    List<Reminder> reminderList = new ArrayList<>();
    ReminderDB reminderDB;
    FloatingActionButton add_reminder_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reminderRecyclerView = findViewById(R.id.reminder_recycler);
        add_reminder_button = findViewById(R.id.add_reminder_button);

        reminderDB = ReminderDB.getInstance(this);
        reminderList = reminderDB.reminderDAO().getAll();

        updateReminderRecycler(reminderList);

        add_reminder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
                startActivityForResult(intent, 101);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (requestCode == Activity.RESULT_OK) {
                Reminder new_reminder = (Reminder) data.getSerializableExtra("reminder");
                reminderDB.reminderDAO().insert(new_reminder);
                reminderList.clear();
                reminderList.addAll(reminderDB.reminderDAO().getAll());
                reminderListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateReminderRecycler(List<Reminder> reminders) {
        int spanCount = 2;
        reminderRecyclerView.setHasFixedSize(true);
        reminderRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        reminderListAdapter = new ReminderListAdapter(MainActivity.this, reminders, reminderClickListener);
        reminderRecyclerView.setAdapter(reminderListAdapter);
    }

    private final ReminderClickListener reminderClickListener = new ReminderClickListener() {
        @Override
        public void onClick(Reminder reminder) {

        }

        @Override
        public void onLongClick(Reminder reminder, CardView cardView) {

        }
    };
}