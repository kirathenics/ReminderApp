package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.example.reminderapp.Listeners.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;

    private AppDatabase appDatabase;
    private List<CategoryWithReminderCount> categoryList = new ArrayList<>();

    private CategoryListAdapter categoryListAdapter;

    private final int ROW_SPAN_COUNT = 1;
    private final int GRID_SPAN_COUNT = 2;
    private boolean isGridView = true;

    Menu toolbarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        Toolbar toolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_categories_editing);
        }

        categoryRecyclerView = findViewById(R.id.category_recycler_view);

        appDatabase = AppDatabase.getInstance(this);
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAllWithReminderCount();
            Log.i("categories", categoryList.toString());
            runOnUiThread(() -> {
                categoryListAdapter = new CategoryListAdapter(CategoryManagementActivity.this, categoryList,
                        new OnItemClickListener<>() {
                            @Override
                            public void onItemClick(CategoryWithReminderCount item) {}

                            @Override
                            public void onItemLongClick(CategoryWithReminderCount item, CardView cardView) {}
                        },
                        (position, updatedItem) -> {
                            appDatabase.categoryDAO().update(updatedItem.getCategory());
                            categoryList.set(position, updatedItem);
                            categoryListAdapter.notifyItemChanged(position);
                        },
                        (position, deletedItem) -> {
                            appDatabase.categoryDAO().delete(deletedItem.getCategory());
                            categoryList.remove(position);
                            categoryListAdapter.notifyItemRemoved(position);
                            categoryListAdapter.notifyItemRangeChanged(position, categoryList.size());
                        });
                updateCategoryRecyclerView(GRID_SPAN_COUNT);
            });
        }).start();

        FloatingActionButton addCategoryButton = findViewById(R.id.add_category_button);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                CategoryEditDialogFragment dialogFragment = CategoryEditDialogFragment.newInstance();
                dialogFragment.setOnCategoryUpdatedListener(newCategory -> new Thread(() -> {
                    appDatabase.categoryDAO().insert(newCategory);

                    Category addedCategory = appDatabase.categoryDAO().getLastInsertedCategory();

                    CategoryWithReminderCount newCategoryWithReminderCount = new CategoryWithReminderCount();
                    newCategoryWithReminderCount.setCategory(addedCategory);

                    runOnUiThread(() -> {
                        categoryList.add(newCategoryWithReminderCount);
                        categoryListAdapter.notifyDataSetChanged();
                    });
                }).start());
                dialogFragment.show(getSupportFragmentManager(), CategoryEditDialogFragment.TAG);
            }
        });
    }

    private void filterCategories(String query) {
        List<CategoryWithReminderCount> filteredList = new ArrayList<>();
        for (CategoryWithReminderCount category : categoryList) {
            if (category.getCategory().getName().toLowerCase().contains(query.toLowerCase())
                    || (Integer.toString(category.getNotCompletedReminders()).contains(query) && category.getNotCompletedReminders() != 0)
                    || Integer.toString(category.getCompletedReminders()).contains(query) && category.getCompletedReminders() != 0) {
                filteredList.add(category);
            }
        }
        categoryListAdapter.filterList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_management_toolbar_menu, menu);

        toolbarMenu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCategories(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCategories(newText);
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
                filterCategories("");
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.view_mode) {
            if (isGridView) {
                isGridView = false;
                updateCategoryRecyclerView(ROW_SPAN_COUNT);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_row_view));

            } else {
                isGridView = true;
                updateCategoryRecyclerView(GRID_SPAN_COUNT);
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

            sortCategories(sortType);

            MenuItem sortingItem = toolbarMenu.findItem(R.id.category_sorting);
            if (sortingItem != null) {
                updateSortingIcon(sortingItem, iconResId);
            }
            return true;
        }
        return false;
    }

    private void updateSortingIcon(MenuItem sortingItem, int iconResId) {
        sortingItem.setIcon(ContextCompat.getDrawable(this, iconResId));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortCategories(String sortType) {
        new Thread(() -> {
            List<CategoryWithReminderCount> sortedList = new ArrayList<>();
            switch (sortType) {
                case "default":
                    sortedList = appDatabase.categoryDAO().getAllWithReminderCount();
                    break;
                case "name_asc":
                    sortedList = appDatabase.categoryDAO().getAllSortedByNameWithReminderCount(true);
                    break;
                case "name_desc":
                    sortedList = appDatabase.categoryDAO().getAllSortedByNameWithReminderCount(false);
                    break;
            }
            List<CategoryWithReminderCount> finalSortedList = sortedList;
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(finalSortedList);
                categoryListAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void updateCategoryRecyclerView(int spanCount) {
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        categoryRecyclerView.setAdapter(categoryListAdapter);
    }
}