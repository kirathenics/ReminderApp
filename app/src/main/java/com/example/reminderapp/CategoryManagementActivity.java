package com.example.reminderapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.reminderapp.Adapters.CategoryListAdapter;
import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.CategoryWithReminderCount;
import com.example.reminderapp.Enums.CategorySortField;
import com.example.reminderapp.Enums.SortOrder;
import com.example.reminderapp.Listeners.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CategoryManagementActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private final List<CategoryWithReminderCount> categoryList = new ArrayList<>();

    private RecyclerView categoryRecyclerView;
    private CategoryListAdapter categoryListAdapter;

    private final int ROW_SPAN_COUNT = 1;
    private final int GRID_SPAN_COUNT = 2;
    private boolean isGridView = true;

    private CategorySortField sortField;
    private SortOrder sortOrder;

    private SharedPreferences sharedPreferences;

    private Menu toolbarMenu;
    private final Map<Pair<CategorySortField, SortOrder>, Integer> iconMap = new HashMap<>();
    private final Map<Integer, Pair<CategorySortField, SortOrder>> sortingOptions = new HashMap<>();
    private final Map<Integer, Integer> iconOptions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        initIconMaps();

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        initToolbar();

        categoryRecyclerView = findViewById(R.id.category_recycler_view);

        initDatabase();
        setupCategoryAdapter();

        FloatingActionButton addCategoryButton = findViewById(R.id.add_category_button);
        addCategoryButton.setOnClickListener(v -> {
            CategoryEditDialogFragment dialogFragment = CategoryEditDialogFragment.newInstance();
            dialogFragment.setOnCategoryUpdatedListener(newCategory -> new Thread(() -> {
                appDatabase.categoryDAO().insert(newCategory);

                Category addedCategory = appDatabase.categoryDAO().getLastInsertedCategory();

                CategoryWithReminderCount newCategoryWithReminderCount = new CategoryWithReminderCount();
                newCategoryWithReminderCount.setCategory(addedCategory);

                runOnUiThread(() -> {
                    categoryList.add(newCategoryWithReminderCount);
//                        categoryListAdapter.notifyDataSetChanged();
                    sortCategories();
                });
            }).start());
            dialogFragment.show(getSupportFragmentManager(), CategoryEditDialogFragment.TAG);
        });
    }

    private void initIconMaps() {
        iconMap.put(new Pair<>(CategorySortField.NONE, SortOrder.ASC), R.drawable.ic_sort);
        iconMap.put(new Pair<>(CategorySortField.NAME, SortOrder.ASC), R.drawable.ic_sort_az);
        iconMap.put(new Pair<>(CategorySortField.NAME, SortOrder.DESC), R.drawable.ic_sort_za);

        sortingOptions.put(R.id.sort_default, new Pair<>(CategorySortField.NONE, SortOrder.ASC));
        sortingOptions.put(R.id.sort_name_asc, new Pair<>(CategorySortField.NAME, SortOrder.ASC));
        sortingOptions.put(R.id.sort_name_desc, new Pair<>(CategorySortField.NAME, SortOrder.DESC));

        iconOptions.put(R.id.sort_default, R.drawable.ic_sort);
        iconOptions.put(R.id.sort_name_asc, R.drawable.ic_sort_az);
        iconOptions.put(R.id.sort_name_desc, R.drawable.ic_sort_za);
    }

    private void loadPreferences() {
        sortField = CategorySortField.valueOf(sharedPreferences.getString("categorySortField", CategorySortField.NONE.toString()));
        sortOrder = SortOrder.valueOf(sharedPreferences.getString("categorySortOrder", SortOrder.ASC.toString()));
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("categorySortField", sortField.toString());
        editor.putString("categorySortOrder", sortOrder.toString());
        editor.apply();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_categories_editing);
        }
    }

    private void initDatabase() {
        appDatabase = AppDatabase.getInstance(this);
    }

    private void setupCategoryAdapter() {
        categoryListAdapter = new CategoryListAdapter(CategoryManagementActivity.this, categoryList,
                new OnItemClickListener<>() {
                    @Override
                    public void onItemClick(CategoryWithReminderCount item) {}

                    @Override
                    public void onItemLongClick(CategoryWithReminderCount item, CardView cardView) {}
                },
                this::updateCategory,
                this::deleteCategory
        );
        updateCategoryRecyclerView(GRID_SPAN_COUNT);
    }

    private void updateCategory(int position, CategoryWithReminderCount updatedItem) {
        appDatabase.categoryDAO().update(updatedItem.getCategory());
        categoryList.set(position, updatedItem);
        categoryListAdapter.notifyItemChanged(position);
        sortCategories();
    }

    private void deleteCategory(int position, CategoryWithReminderCount deletedItem) {
        appDatabase.categoryDAO().delete(deletedItem.getCategory());
        categoryList.remove(position);
        sortCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPreferences();
        sortCategories();
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_management_toolbar_menu, menu);

        toolbarMenu = menu;
        updateIconBasedOnSortFieldAndOrder();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchCategories(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchCategories(newText);
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
                searchCategories("");
                return true;
            }
        });

        return true;
    }

    private void searchCategories(String query) {
        List<CategoryWithReminderCount> foundCategoryList = new ArrayList<>();
        for (CategoryWithReminderCount category : categoryList) {
            if (category.getCategory().getName().toLowerCase().contains(query.toLowerCase())
                    || (Integer.toString(category.getNotCompletedReminders()).contains(query) && category.getNotCompletedReminders() != 0)
                    || Integer.toString(category.getCompletedReminders()).contains(query) && category.getCompletedReminders() != 0) {
                foundCategoryList.add(category);
            }
        }
        categoryListAdapter.searchCategories(foundCategoryList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        if (itemId == R.id.view_mode) {
            toggleViewMode(item);
            return true;
        }

        if (isSortingOption(itemId)) {
            handleSortingOption(itemId);
            return true;
        }

        return false;
    }

    private void toggleViewMode(MenuItem item) {
        isGridView = !isGridView;
        int spanCount = isGridView ? GRID_SPAN_COUNT : ROW_SPAN_COUNT;
        int iconResId = isGridView ? R.drawable.ic_grid_view : R.drawable.ic_row_view;

        updateCategoryRecyclerView(spanCount);
        item.setIcon(ContextCompat.getDrawable(this, iconResId));
    }

    private boolean isSortingOption(int itemId) {
        MenuItem item = toolbarMenu.findItem(itemId);
        return item != null && item.getGroupId() == R.id.sorting_group;
    }

    private void handleSortingOption(int itemId) {
        Pair<CategorySortField, SortOrder> sortOption = sortingOptions.get(itemId);
        sortField = sortOption != null ? sortOption.first : CategorySortField.NONE;
        sortOrder = sortOption != null ? sortOption.second : SortOrder.ASC;
        Integer iconResIdValue = iconOptions.get(itemId);
        int iconResId = (iconResIdValue != null) ? iconResIdValue : R.drawable.ic_sort;

        updateSortingIcon(toolbarMenu.findItem(R.id.category_sorting), iconResId);
        sortCategories();
    }

    private void updateIconBasedOnSortFieldAndOrder() {
        Integer iconResId = iconMap.get(new Pair<>(sortField, sortOrder));
        if (iconResId == null) {
            iconResId = R.drawable.ic_sort;
        }

        MenuItem sortingItem = toolbarMenu.findItem(R.id.category_sorting);
        updateSortingIcon(sortingItem, iconResId);
    }

    private void updateSortingIcon(MenuItem sortingItem, int iconResId) {
        sortingItem.setIcon(ContextCompat.getDrawable(this, iconResId));
    }

    private void sortCategories() {
        new Thread(() -> {
            List<CategoryWithReminderCount> sortedList;
            switch (sortField) {
                case NONE:
                    sortedList = appDatabase.categoryDAO().getAllWithReminderCount();
                    break;
                case NAME:
                    sortedList = appDatabase.categoryDAO().getAllSortedByNameWithReminderCount(sortOrder == SortOrder.ASC);
                    break;
                default:
                    sortedList = categoryList;
            }
            List<CategoryWithReminderCount> finalFilteredAndSortedList = sortedList;
            runOnUiThread(() -> categoryListAdapter.setCategories(finalFilteredAndSortedList));
        }).start();
    }

    private void updateCategoryRecyclerView(int spanCount) {
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        categoryRecyclerView.setAdapter(categoryListAdapter);
    }
}