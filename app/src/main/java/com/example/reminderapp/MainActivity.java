package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private List<Category> categoryList = new ArrayList<>();
    private AppDatabase appDatabase;

    private MenuItem lastCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        int color = ContextCompat.getColor(this, R.color.lavender_dark);
        toggle.getDrawerArrowDrawable().setColor(color);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        appDatabase = AppDatabase.getInstance(this);

        lastCategory = null;
        addDefaultCategories();
        updateCategoriesMenu();

        FloatingActionButton addReminderButton = findViewById(R.id.add_reminder_button);
        addReminderButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            }

            Intent intent = new Intent(MainActivity.this, ReminderAddActivity.class);

            if (lastCategory != null) {
                String selectedCategory = Objects.requireNonNull(lastCategory.getTitle()).toString();
                intent.putExtra("selected_category", selectedCategory);
            }

            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCategoriesMenu();
    }

    private void addDefaultCategories() {
        List<Category> existingCategories = appDatabase.categoryDAO().getAll();
        if (existingCategories.isEmpty()) {
            String[] defaultCategories = getResources().getStringArray(R.array.default_categories);
            for (String categoryName : defaultCategories) {
                Category category = new Category();
                category.setName(categoryName);
                int colorInt = ContextCompat.getColor(this, R.color.lavender_medium);
                String colorHexWithoutAlpha = String.format("#%08X", colorInt);
                category.setColor(colorHexWithoutAlpha);
                category.setActive(true);
                appDatabase.categoryDAO().insert(category);
            }
        }
    }

    public void updateCategoriesMenu() {
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAll();
            runOnUiThread(() -> {
                Menu menu = navigationView.getMenu();
                MenuItem categoriesMenuItem = menu.findItem(R.id.CategoriesMenu);
                SubMenu subMenu = categoriesMenuItem.getSubMenu();
                if (subMenu == null) return;

                subMenu.clear();

                MenuItem allCategoryItem  = subMenu.add(R.id.categories_group, Menu.FIRST, Menu.NONE, R.string.category_name_all)
                        .setIcon(R.drawable.ic_list)
                        .setCheckable(true);

                boolean hasActiveCategory = false;

                for (int i = 0; i < categoryList.size(); i++) {
                    Category category = categoryList.get(i);
                    if (category.isActive()) {
                        if (lastCategory != null && Objects.requireNonNull(lastCategory.getTitle()).toString().equals(category.getName())) {
                            hasActiveCategory = true;
                        }

                        subMenu.add(R.id.categories_group, Menu.FIRST + i + 1, Menu.NONE, category.getName())
                                .setIcon(R.drawable.ic_list)
                                .setCheckable(true);
                    }
                }

                if (!hasActiveCategory) {
                    lastCategory = null;
                }

                if (lastCategory == null) {
                    allCategoryItem.setChecked(true);
                    lastCategory = allCategoryItem;
                }

                navigationView.invalidate();
            });
        }).start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.categories_group) {
            lastCategory.setChecked(false);

            String selectedCategory = Objects.requireNonNull(item.getTitle()).toString();
            Toast.makeText(this, "Selected Category: " + selectedCategory, Toast.LENGTH_SHORT).show();
            item.setChecked(true);
            lastCategory = item;
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.CategoriesManagement) {
            startActivity(new Intent(MainActivity.this, CategoryManagementActivity.class));
        }
        else if (itemId == R.id.Settings) {
            // TODO: implement Settings menu
        }
        else if (itemId == R.id.Info) {
            new AlertDialog.Builder(this)
                    .setTitle("About the Application")
                    .setMessage("This application was developed as part of a course project.")
                    .setPositiveButton("OK", null)
                    .show();
        }
        else if (itemId == R.id.Exit) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "reminder_channel";
            CharSequence name = "Reminder Notifications";
            String description = "Reminder";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Reminder resultReminder = (Reminder) data.getSerializableExtra("new_reminder");
            if (resultReminder != null) {
                long triggerTime = resultReminder.getDate() + resultReminder.getTime();



                if (triggerTime <= System.currentTimeMillis()) {
//                    Toast.makeText(this, "Cannot set notification in the past!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainActivity.this, ReminderNotificationReceiver.class);
                intent.putExtra("title", R.string.app_name);
                intent.putExtra("message", resultReminder.getTitle());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                        (int) triggerTime,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Toast.makeText(this, "Notification set for: " + triggerTime, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}