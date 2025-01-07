package com.example.reminderapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.Manifest;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
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
import com.example.reminderapp.Enums.GridViewSpanCount;
import com.example.reminderapp.Enums.ReminderSortField;
import com.example.reminderapp.Enums.SortOrder;
import com.example.reminderapp.Listeners.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();
    private final List<Reminder> reminderList = new ArrayList<>();

    private ActivityResultLauncher<Intent> createReminderActivityLauncher;
    private ActivityResultLauncher<Intent> changeReminderActivityLauncher;

    private RecyclerView reminderRecyclerView;
    private ReminderListAdapter reminderListAdapter;

    LinearLayout completedRemindersAmountLinearLayout;
    SwitchCompat isCompletedRemindersVisibleSwitchCompat;
    TextView completedRemindersAmountTextView;

    LinearLayout noRemindersLinearLayout;
    TextView noRemindersCategoryNameTextView;

    private GridViewSpanCount spanCount = GridViewSpanCount.TWO_CARDS;

    private MenuItem lastCategory = null;
    private Integer selectedCategoryId = null;
    private Boolean isCompleted = true;
    private ReminderSortField sortField = ReminderSortField.NONE;
    private SortOrder sortOrder = SortOrder.ASC;

    FloatingActionButton addReminderButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int interval = 10000;

    private SharedPreferences sharedPreferences;

    private Menu toolbarMenu;
    private final Map<Pair<ReminderSortField, SortOrder>, Integer> iconMap = new HashMap<>();
    private final Map<Integer, Pair<ReminderSortField, SortOrder>> sortingOptions = new HashMap<>();
    private final Map<Integer, Integer> iconOptions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        initIconMaps();

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        initToolbarAndDrawer();
        initViews();
        initDatabase();
        initResultLaunchers();
        setupReminderAdapter();

        addReminderButton = findViewById(R.id.add_reminder_button);
        addReminderButton.setOnClickListener(v -> onAddReminderButtonClicked());

        isCompletedRemindersVisibleSwitchCompat = findViewById(R.id.is_completed_reminders_visible_switch_compat);
        isCompletedRemindersVisibleSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCompleted = isChecked;
            filterAndSortReminders();
        });

        startPulseAnimation();
    }

    private void initIconMaps() {
        iconMap.put(new Pair<>(ReminderSortField.NONE, SortOrder.ASC), R.drawable.ic_sort);
        iconMap.put(new Pair<>(ReminderSortField.TITLE, SortOrder.ASC), R.drawable.ic_sort_az);
        iconMap.put(new Pair<>(ReminderSortField.TITLE, SortOrder.DESC), R.drawable.ic_sort_za);
        iconMap.put(new Pair<>(ReminderSortField.UPDATED_AT, SortOrder.ASC), R.drawable.ic_time);
        iconMap.put(new Pair<>(ReminderSortField.UPDATED_AT, SortOrder.DESC), R.drawable.ic_time_restore);

        sortingOptions.put(R.id.sort_default, new Pair<>(ReminderSortField.NONE, SortOrder.ASC));
        sortingOptions.put(R.id.sort_title_asc, new Pair<>(ReminderSortField.TITLE, SortOrder.ASC));
        sortingOptions.put(R.id.sort_title_desc, new Pair<>(ReminderSortField.TITLE, SortOrder.DESC));
        sortingOptions.put(R.id.sort_updated_time_asc, new Pair<>(ReminderSortField.UPDATED_AT, SortOrder.ASC));
        sortingOptions.put(R.id.sort_updated_time_desc, new Pair<>(ReminderSortField.UPDATED_AT, SortOrder.DESC));

        iconOptions.put(R.id.sort_default, R.drawable.ic_sort);
        iconOptions.put(R.id.sort_title_asc, R.drawable.ic_sort_az);
        iconOptions.put(R.id.sort_title_desc, R.drawable.ic_sort_za);
        iconOptions.put(R.id.sort_updated_time_asc, R.drawable.ic_time);
        iconOptions.put(R.id.sort_updated_time_desc, R.drawable.ic_time_restore);
    }

    private void loadPreferences() {
        selectedCategoryId = sharedPreferences.getInt("selectedCategoryId", -1);
        selectedCategoryId = selectedCategoryId == -1 ? null : selectedCategoryId;
        isCompleted = sharedPreferences.getBoolean("isCompleted", true);
        isCompletedRemindersVisibleSwitchCompat.setChecked(sharedPreferences.getBoolean("isCompleted", true));
        spanCount = GridViewSpanCount.valueOf(sharedPreferences.getString("reminderSpanCount", GridViewSpanCount.TWO_CARDS.toString()));
        sortField = ReminderSortField.valueOf(sharedPreferences.getString("reminderSortField", ReminderSortField.NONE.toString()));
        sortOrder = SortOrder.valueOf(sharedPreferences.getString("reminderSortOrder", SortOrder.ASC.toString()));
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selectedCategoryId", selectedCategoryId != null ? selectedCategoryId : -1);
        editor.putBoolean("isCompleted", isCompleted);
        editor.putString("reminderSpanCount", spanCount.toString());
        editor.putString("reminderSortField", sortField.toString());
        editor.putString("reminderSortOrder", sortOrder.toString());
        editor.apply();
    }

    private void initToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.lavender_dark));
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
    }

    private void initViews() {
        reminderRecyclerView = findViewById(R.id.reminder_recycler_view);

        completedRemindersAmountLinearLayout = findViewById(R.id.completed_reminders_amount_linear_layout);
        completedRemindersAmountTextView = findViewById(R.id.completed_reminders_amount_text_view);

        noRemindersLinearLayout = findViewById(R.id.no_reminders_linear_layout);
        noRemindersCategoryNameTextView = findViewById(R.id.no_reminders_category_name_text_view);
    }

    private void initDatabase() {
        appDatabase = AppDatabase.getInstance(this);
        addDefaultCategories();
    }

    private void initResultLaunchers() {
        createReminderActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCreateReminderResult
        );

        changeReminderActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleChangeReminderResult
        );
    }

    private void onAddReminderButtonClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        Intent intent = new Intent(MainActivity.this, ReminderAddActivity.class);

        if (selectedCategoryId != null) {
            Category selectedCategory = appDatabase.categoryDAO().findById(selectedCategoryId);
            intent.putExtra("selected_category", selectedCategory);
        }

        createReminderActivityLauncher.launch(intent);
    }

    private void setupReminderAdapter() {
        reminderListAdapter = new ReminderListAdapter(
                this,
                reminderList,
                new OnItemClickListener<>() {
                    @Override
                    public void onItemClick(Reminder item) {
                    }

                    @Override
                    public void onItemLongClick(Reminder item, CardView cardView) {
                    }
                },
                changeReminderActivityLauncher,
                this::updateReminder,
                this::deleteReminder
        );
        updateReminderRecyclerView(spanCount.getValue());
    }

    private void handleCreateReminderResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            new Thread(() -> {
                Reminder newReminder = (Reminder) result.getData().getSerializableExtra("new_reminder");
                appDatabase.reminderDAO().insert(newReminder);

                Reminder addedReminder = appDatabase.reminderDAO().getLastInsertedReminder();

                runOnUiThread(() -> {
                    reminderList.add(addedReminder);
                    updateSelectedCategory(addedReminder.getCategoryId());
                    filterAndSortReminders();
                });

                assert newReminder != null;
                ScheduleNotificationManager.createNotification(MainActivity.this, newReminder);
            }).start();
        }
    }

    private void handleChangeReminderResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Reminder updatedReminder = (Reminder) result.getData().getSerializableExtra("new_reminder");
            int position = result.getData().getIntExtra("position", -1);

            if (position != -1 && updatedReminder != null) {
                appDatabase.reminderDAO().update(updatedReminder);
                reminderList.set(position, updatedReminder);
                reminderListAdapter.notifyItemChanged(position);
                updateSelectedCategory(updatedReminder.getCategoryId());
                filterAndSortReminders();

                ScheduleNotificationManager.updateNotification(MainActivity.this, updatedReminder);
            }
        }
    }

    private void updateSelectedCategory(Integer categoryId) {
        if (selectedCategoryId != null && !selectedCategoryId.equals(categoryId)) {
            selectedCategoryId = categoryId;
        }
    }

    private void updateReminder(int position, Reminder updatedItem) {
        appDatabase.reminderDAO().update(updatedItem);
        reminderList.set(position, updatedItem);
        reminderListAdapter.notifyItemChanged(position);
        filterAndSortReminders();
        ScheduleNotificationManager.updateNotification(MainActivity.this, updatedItem);
    }

    private void deleteReminder(int position, Reminder deletedItem) {
        appDatabase.reminderDAO().delete(deletedItem);
        reminderList.remove(position);
        filterAndSortReminders();
        ScheduleNotificationManager.cancelNotification(MainActivity.this, deletedItem.getId());
    }

    private void startPulseAnimation() {
        Runnable pulseRunnable = new Runnable() {
            @Override
            public void run() {
                AnimatorSet pulseAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.button_pulse_animation);
                pulseAnimation.setTarget(addReminderButton);
                pulseAnimation.start();

                handler.postDelayed(this, interval);
            }
        };

        handler.post(pulseRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPreferences();

        updateCategoriesMenu();
        filterAndSortReminders();
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
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

                    if (selectedCategoryId != null && category.getId() == selectedCategoryId) {
                        hasActiveCategory = true;
                        lastCategory = categoryItem;
                    }
                }
            }

            if (!hasActiveCategory) {
                lastCategory = allCategoryItem;
                selectedCategoryId = null;
            }

            if (lastCategory != null) {
                lastCategory.setChecked(true);
            }

            navigationView.invalidate();
        });
    }

    private void filterAndSortReminders() {
        new Thread(() -> {
            List<Reminder> filteredAndSortedList;
            switch (sortField) {
                case NONE:
                    filteredAndSortedList = appDatabase.reminderDAO().getFiltered(selectedCategoryId, isCompleted);
                    break;
                case TITLE:
                    filteredAndSortedList = appDatabase.reminderDAO().getFilteredAndSortedByTitle(selectedCategoryId, isCompleted, sortOrder == SortOrder.ASC);
                    break;
                case UPDATED_AT:
                    filteredAndSortedList = appDatabase.reminderDAO().getFilteredAndSortedByUpdatedTime(selectedCategoryId, isCompleted, sortOrder == SortOrder.ASC);
                    break;
                default:
                    filteredAndSortedList = reminderList;
            }
            List<Reminder> finalFilteredAndSortedList = filteredAndSortedList;
            runOnUiThread(() -> {
                if (finalFilteredAndSortedList.isEmpty()) {
                    completedRemindersAmountLinearLayout.setVisibility(View.GONE);
                    reminderRecyclerView.setVisibility(View.GONE);

                    noRemindersCategoryNameTextView.setText(Objects.requireNonNull(lastCategory.getTitle()).toString());
                    noRemindersLinearLayout.setVisibility(View.VISIBLE);
                }
                else {
                    noRemindersLinearLayout.setVisibility(View.GONE);

                    completedRemindersAmountTextView.setText(String.format(Locale.getDefault(), "%d", appDatabase.reminderDAO().getCompletedRemindersCountByCategoryId(selectedCategoryId)));
                    reminderListAdapter.setReminders(finalFilteredAndSortedList);

                    completedRemindersAmountLinearLayout.setVisibility(View.VISIBLE);
                    reminderRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.categories_group) {
            lastCategory.setChecked(false);

            if (Objects.requireNonNull(item.getTitle()).toString().equals(this.getString(R.string.category_name_all))) {
                selectedCategoryId = null;
            } else {
                selectedCategoryId = item.getItemId() - Menu.FIRST - 1;
            }

            filterAndSortReminders();

            item.setChecked(true);
            lastCategory = item;

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.CategoriesManagement) {
            startActivity(new Intent(MainActivity.this, CategoryManagementActivity.class));
        }
//        else if (itemId == R.id.Settings) {
//            // TODO: implement Settings menu
//        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_toolbar_menu, menu);

        toolbarMenu = menu;
        toggleViewMode();
        updateIconBasedOnSortFieldAndOrder();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchReminders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchReminders(newText);
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
                searchReminders("");
                return true;
            }
        });

        return true;
    }

    private void searchReminders(String query) {
        List<Reminder> foundReminderList = new ArrayList<>();
        for (Reminder reminder : reminderList) {
            if (reminder.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    reminder.getDescription().toLowerCase().contains(query.toLowerCase())) {
                foundReminderList.add(reminder);
            }
        }

        reminderListAdapter.searchReminders(foundReminderList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.view_mode) {
            spanCount = getNextSpanCount(spanCount);
            toggleViewMode();
            return true;
        }

        if (isSortingOption(itemId)) {
            handleSortingOption(itemId);
            return true;
        }

        return false;
    }

    private GridViewSpanCount getNextSpanCount(GridViewSpanCount current) {
        GridViewSpanCount[] values = GridViewSpanCount.values();
        int currentIndex = current.ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        return values[nextIndex];
    }

    private void toggleViewMode() {
        int iconResId;
        switch (spanCount) {
            case ROW:
                iconResId = R.drawable.ic_row_view;
                break;
            case TWO_CARDS:
                iconResId = R.drawable.ic_grid_view;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + spanCount);
        }

        updateReminderRecyclerView(spanCount.getValue());
        MenuItem item = toolbarMenu.findItem(R.id.view_mode);
        item.setIcon(ContextCompat.getDrawable(this, iconResId));
    }

    private boolean isSortingOption(int itemId) {
        MenuItem item = toolbarMenu.findItem(itemId);
        return item != null && item.getGroupId() == R.id.sorting_group;
    }

    private void handleSortingOption(int itemId) {
        Pair<ReminderSortField, SortOrder> sortOption = sortingOptions.get(itemId);
        sortField = sortOption != null ? sortOption.first : ReminderSortField.NONE;
        sortOrder = sortOption != null ? sortOption.second : SortOrder.ASC;
        Integer iconResIdValue = iconOptions.get(itemId);
        int iconResId = (iconResIdValue != null) ? iconResIdValue : R.drawable.ic_sort;

        updateSortingIcon(toolbarMenu.findItem(R.id.reminder_sorting), iconResId);
        filterAndSortReminders();
    }

    private void updateIconBasedOnSortFieldAndOrder() {
        Integer iconResId = iconMap.get(new Pair<>(sortField, sortOrder));
        if (iconResId == null) {
            iconResId = R.drawable.ic_sort;
        }

        MenuItem sortingItem = toolbarMenu.findItem(R.id.reminder_sorting);
        updateSortingIcon(sortingItem, iconResId);
    }

    private void updateSortingIcon(MenuItem sortingItem, int iconResId) {
        sortingItem.setIcon(ContextCompat.getDrawable(this, iconResId));
    }

    private void updateReminderRecyclerView(int spanCount) {
        reminderRecyclerView.setHasFixedSize(true);
        reminderRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        reminderRecyclerView.setAdapter(reminderListAdapter);
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
