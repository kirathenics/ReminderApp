package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.Manifest;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.reminderapp.Adapters.ReminderListAdapter;
import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;
import com.example.reminderapp.Listeners.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();
    private List<Reminder> reminderList = new ArrayList<>();

    private RecyclerView reminderRecyclerView;
    private ReminderListAdapter reminderListAdapter;

    private MenuItem lastCategory;
    private Menu toolbarMenu;

    private final int ROW_SPAN_COUNT = 1;
    private final int GRID_SPAN_COUNT = 2;
    private boolean isGridView = true;

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        appDatabase = AppDatabase.getInstance(this);

        lastCategory = null;
        addDefaultCategories();
//        updateCategoriesMenu();

        reminderRecyclerView = findViewById(R.id.reminder_recycler_view);

        ActivityResultLauncher<Intent> changeReminderActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Reminder updatedReminder = (Reminder) result.getData().getSerializableExtra("new_reminder");
                        int position = result.getData().getIntExtra("position", -1);

                        if (position != -1 && updatedReminder != null) {
                            appDatabase.reminderDAO().update(updatedReminder);
                            reminderList.set(position, updatedReminder);
                            reminderListAdapter.notifyItemChanged(position);
                        }

                        if (lastCategory != null) {
                            filterRemindersByCategory(lastCategory);
                        }
                    }
                }
        );

//        new Thread(() -> {
//            reminderList = appDatabase.reminderDAO().getAll();
//            runOnUiThread(() -> {
//                reminderListAdapter = new ReminderListAdapter(MainActivity.this, reminderList,
//                        new OnItemClickListener<>() {
//                            @Override
//                            public void onItemClick(Reminder item) {
//                            }
//
//                            @Override
//                            public void onItemLongClick(Reminder item, CardView cardView) {
//                            }
//                        },
//                        changeReminderActivityLauncher,
//                        (position, updatedItem) -> {
//                            appDatabase.reminderDAO().update(updatedItem);
//                            reminderList.set(position, updatedItem);
//                            reminderListAdapter.notifyItemChanged(position);
//                            if (lastCategory != null) {
//                                filterRemindersByCategory(lastCategory);
//                            }
//                        },
//                        (position, deletedItem) -> {
//
//                            appDatabase.reminderDAO().delete(deletedItem);
//                            reminderList.remove(position);
//                            reminderListAdapter.notifyItemRemoved(position);
//                            if (lastCategory != null) {
//                                filterRemindersByCategory(lastCategory);
//                            }
//                        });
//                updateReminderRecyclerView(GRID_SPAN_COUNT);
//            });
//        }).start();

//        @SuppressLint("NotifyDataSetChanged") ActivityResultLauncher<Intent> createReminderActivityLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                        Reminder newReminder = (Reminder) result.getData().getSerializableExtra("new_reminder");
//
//                        new Thread(() -> {
//                            runOnUiThread(() -> {
//                                categoryList.add(newCategoryWithReminderCount);
//                                categoryListAdapter.notifyDataSetChanged();
//                            });
//                        }).start());
//                        appDatabase.reminderDAO().insert(newReminder);
//
//                        Reminder reminder = appDatabase.reminderDAO().findByTitle(Objects.requireNonNull(newReminder).getTitle());
////                        Reminder reminder = appDatabase.reminderDAO().findByTitle(newReminder.getTitle());
//                        reminderList.add(reminder);
//                        reminderListAdapter.notifyDataSetChanged();
//
//                        if (lastCategory != null) {
//                            filterRemindersByCategory(lastCategory);
//                        }
//                    }
//                }
//        );

        new Thread(() -> {
            reminderList = appDatabase.reminderDAO().getAll();
            runOnUiThread(() -> {
                reminderListAdapter = new ReminderListAdapter(MainActivity.this, reminderList,
                        new OnItemClickListener<>() {
                            @Override
                            public void onItemClick(Reminder item) {
                            }

                            @Override
                            public void onItemLongClick(Reminder item, CardView cardView) {
                            }
                        },
                        changeReminderActivityLauncher,
                        (position, updatedItem) -> {
                            appDatabase.reminderDAO().update(updatedItem);
                            reminderList.set(position, updatedItem);
                            reminderListAdapter.notifyItemChanged(position);
                            if (lastCategory != null) {
                                filterRemindersByCategory(lastCategory);
                            }
                        },
                        (position, deletedItem) -> {
                            appDatabase.reminderDAO().delete(deletedItem);
                            reminderList.remove(position);
                            reminderListAdapter.notifyItemRemoved(position);
                            reminderListAdapter.notifyItemRangeChanged(position, reminderList.size());
                            if (lastCategory != null) {
                                filterRemindersByCategory(lastCategory);
                            }
                        });
                updateReminderRecyclerView(GRID_SPAN_COUNT);
            });
        }).start();

        @SuppressLint("NotifyDataSetChanged") ActivityResultLauncher<Intent> createReminderActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        new Thread(() -> {
                            Reminder newReminder = (Reminder) result.getData().getSerializableExtra("new_reminder");
                            appDatabase.reminderDAO().insert(newReminder);

                            Reminder addedReminder = appDatabase.reminderDAO().getLastInsertedReminder();

                            runOnUiThread(() -> {
                                reminderList.add(addedReminder);
                                reminderListAdapter.notifyDataSetChanged();

                                if (lastCategory != null) {
                                    filterRemindersByCategory(lastCategory);
                                }
                            });
                        }).start();
                    }
                }
        );

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
                Category selectedCategory = appDatabase.categoryDAO().findByName(Objects.requireNonNull(lastCategory.getTitle()).toString());
                intent.putExtra("selected_category", selectedCategory);
            }

//            startActivityForResult(intent, 1);
            createReminderActivityLauncher.launch(intent);
        });
    }

    private void updateReminderRecyclerView(int spanCount) {
        reminderRecyclerView.setHasFixedSize(true);
        reminderRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        reminderRecyclerView.setAdapter(reminderListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCategoriesMenu();

        runOnUiThread(() -> {
            if (lastCategory != null) {
                filterRemindersByCategory(lastCategory);
            } else {
                Log.e("onResume", "lastCategory is null");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_toolbar_menu, menu);

        toolbarMenu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterReminders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterReminders(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                filterReminders("");
                return true;
            }
        });

        return true;
    }

    private void filterReminders(String query) {
        List<Reminder> filteredList = new ArrayList<>();
        for (Reminder reminder : reminderList) {
            if (reminder.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    reminder.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(reminder);
            }
        }
        reminderListAdapter.filterList(filteredList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.view_mode) {
            if (isGridView) {
                isGridView = false;
                updateReminderRecyclerView(ROW_SPAN_COUNT);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_row_view));

            } else {
                isGridView = true;
                updateReminderRecyclerView(GRID_SPAN_COUNT);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_grid_view));
            }
            return true;
        } else if (itemId == R.id.sort_default || itemId == R.id.sort_name_asc || itemId == R.id.sort_name_desc) {
            String sortType = "default";
            int iconResId = R.drawable.ic_sort;

            if (itemId == R.id.sort_name_asc) {
                sortType = "name_asc";
                iconResId = R.drawable.ic_sort_az;
            } else if (itemId == R.id.sort_name_desc) {
                sortType = "name_desc";
                iconResId = R.drawable.ic_sort_za;
            }

            sortReminders(sortType);

            MenuItem sortingItem = toolbarMenu.findItem(R.id.category_sorting);
            if (sortingItem != null) {
                updateSortingIcon(sortingItem, iconResId);
            }
            return true;
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortReminders(String sortType) {
        new Thread(() -> {
            List<Reminder> sortedList = new ArrayList<>();
            switch (sortType) {
                case "default":
                    sortedList = appDatabase.reminderDAO().getAll();
                    break;
                case "name_asc":
                    sortedList = appDatabase.reminderDAO().getAllSortedByTitle(true);
                    break;
                case "name_desc":
                    sortedList = appDatabase.reminderDAO().getAllSortedByTitle(false);
                    break;
            }
            List<Reminder> finalSortedList = sortedList;
            runOnUiThread(() -> {
                reminderList.clear();
                reminderList.addAll(finalSortedList);
                reminderListAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void updateSortingIcon(MenuItem sortingItem, int iconResId) {
        sortingItem.setIcon(ContextCompat.getDrawable(this, iconResId));
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

                MenuItem allCategoryItem = subMenu.add(R.id.categories_group, Menu.FIRST, Menu.NONE, R.string.category_name_all)
                        .setIcon(R.drawable.ic_list)
                        .setCheckable(true);

                menu.setGroupCheckable(R.id.categories_group, true, true);

                boolean hasActiveCategory = false;

                for (Category category : categoryList) {
                    if (category.isActive()) {
                        MenuItem categoryItem = subMenu.add(R.id.categories_group, Menu.FIRST + 1 + category.getId(), Menu.NONE, category.getName())
                                .setIcon(R.drawable.ic_list)
                                .setCheckable(true);

                        if (lastCategory != null && Objects.requireNonNull(lastCategory.getTitle()).toString().equals(category.getName())) {
                            hasActiveCategory = true;
                            lastCategory = categoryItem;
                        }
                    }
                }

                if (!hasActiveCategory) {
                    lastCategory = allCategoryItem;
                }

                if (lastCategory != null) {
                    lastCategory.setChecked(true);
                }

                navigationView.invalidate();
            });
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.categories_group) {
            lastCategory.setChecked(false);

            filterRemindersByCategory(item);

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

    @SuppressLint("NotifyDataSetChanged")
    private void filterRemindersByCategory(@NonNull MenuItem item) {
        new Thread(() -> {
            String categoryName = Objects.requireNonNull(item.getTitle()).toString();
            List<Reminder> filteredList;
            if (categoryName.equals(this.getString(R.string.category_name_all))) {
                filteredList = appDatabase.reminderDAO().getAll();
            } else {
                int categoryId = item.getItemId() - Menu.FIRST - 1;
//                    int categoryId = appDatabase.categoryDAO().findByName(categoryName).getId();
                filteredList = appDatabase.reminderDAO().findByCategoryId(categoryId);
            }
            List<Reminder> finalFilteredList = filteredList;
            runOnUiThread(() -> {
                reminderList.clear();
                reminderList.addAll(finalFilteredList);
                reminderListAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @SuppressLint("ObsoleteSdkInt")
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

//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && data != null) {
//            if (requestCode == 1) {
//                Reminder newReminder = (Reminder) data.getSerializableExtra("new_reminder");
//                appDatabase.reminderDAO().insert(newReminder);
//                Reminder reminder = appDatabase.reminderDAO().findByTitle(newReminder.getTitle());
//                reminderList.add(reminder);
//                reminderListAdapter.notifyDataSetChanged();
//            }
//            else if (requestCode == 2) {
////                Reminder updatedReminder = (Reminder) data.getSerializableExtra("new_reminder");
////                appDatabase.reminderDAO().update(updatedReminder);
////                reminderList.set(position, updatedReminder);
////                reminderListAdapter.notifyItemChanged(position);
//            }
//        }
//
////        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
////            Reminder resultReminder = (Reminder) data.getSerializableExtra("new_reminder");
////            if (resultReminder != null) {
////                long triggerTime = resultReminder.getDate() + resultReminder.getTime();
////
////
////
////                if (triggerTime <= System.currentTimeMillis()) {
//////                    Toast.makeText(this, "Cannot set notification in the past!", Toast.LENGTH_SHORT).show();
////                    return;
////                }
////
////                Intent intent = new Intent(MainActivity.this, ReminderNotificationReceiver.class);
////                intent.putExtra("title", R.string.app_name);
////                intent.putExtra("message", resultReminder.getTitle());
////
////                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
////                        (int) triggerTime,
////                        intent,
////                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
////
////                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
////                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
////                Toast.makeText(this, "Notification set for: " + triggerTime, Toast.LENGTH_SHORT).show();
////            }
////        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
//            }
//        }
    }
}


