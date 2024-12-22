package com.example.reminderapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.reminderapp.Adapters.ReminderListAdapter;
import com.example.reminderapp.DB.ReminderDB;
import com.example.reminderapp.Entities.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView reminderRecyclerView;
    ReminderListAdapter reminderListAdapter;
    List<Reminder> reminderList = new ArrayList<>();
    ReminderDB reminderDB;
    FloatingActionButton add_reminder_button;
    SearchView searchView_reminder;

    Reminder selectedReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reminderRecyclerView = findViewById(R.id.reminder_recycler);
        add_reminder_button = findViewById(R.id.add_reminder_button);
        searchView_reminder = findViewById(R.id.searchView_reminder);

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

        searchView_reminder.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText) {
        List<Reminder> filteredList = new ArrayList<>();
        for (Reminder reminder : reminderList) {
            if (reminder.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                    reminder.getDescription().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(reminder);
            }
        }
        reminderListAdapter.filterList(filteredList);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Reminder new_reminder = (Reminder) data.getSerializableExtra("reminder");
                reminderDB.reminderDAO().insert(new_reminder);
                reminderList.clear();
                reminderList.addAll(reminderDB.reminderDAO().getAll());
                reminderListAdapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Reminder new_reminder = (Reminder) data.getSerializableExtra("reminder");
                reminderDB.reminderDAO().update(
                        new_reminder.getId(),
                        new_reminder.getTitle(),
                        new_reminder.getDescription(),
                        new_reminder.getDate(),
                        new_reminder.getTime(),
                        new_reminder.getCategory());
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
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            intent.putExtra("old_reminder", reminder);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Reminder reminder, CardView cardView) {
            selectedReminder = new Reminder();
            selectedReminder = reminder;
            showPopUp(cardView);
        }
    };

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.delete_reminder) {
            reminderDB.reminderDAO().delete(selectedReminder);
            reminderList.remove(selectedReminder);
            reminderListAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Reminder deleted", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}