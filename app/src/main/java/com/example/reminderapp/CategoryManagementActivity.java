package com.example.reminderapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.example.reminderapp.Listeners.CategoryOnClickListener;
import com.example.reminderapp.Listeners.OnCategoryChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();

    private CategoryListAdapter categoryListAdapter;
    private CategoryOnClickListener categoryOnClickListener;

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
            getSupportActionBar().setTitle("Categories Editing");
        }

        categoryRecyclerView = findViewById(R.id.category_recycler_view);

        appDatabase = AppDatabase.getInstance(this);
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAll();
            runOnUiThread(() -> {
                categoryListAdapter = new CategoryListAdapter(CategoryManagementActivity.this, categoryList, categoryOnClickListener, new OnCategoryChangeListener() {
                    @Override
                    public void onCategoryUpdated(int position, Category updatedCategory) {
                        appDatabase.categoryDAO().update(updatedCategory);
                        categoryList.set(position, updatedCategory);
                        categoryListAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onCategoryDeleted(int position) {
//                        new Thread(() -> AppDatabase.getInstance(context).categoryDAO().delete(category)).start();
                        Category category = categoryList.get(position);
                        appDatabase.categoryDAO().delete(category);
                        categoryList.remove(position);
                        categoryListAdapter.notifyItemRemoved(position);
                    }
                });
                updateCategoryRecyclerView(GRID_SPAN_COUNT);
            });
        }).start();

        FloatingActionButton addCategoryButton = findViewById(R.id.add_category_button);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                CategoryDialogFragment dialogFragment = CategoryDialogFragment.newInstance();
                dialogFragment.setOnCategoryUpdatedListener(newCategory -> {
                    appDatabase.categoryDAO().insert(newCategory);
                    Category category = appDatabase.categoryDAO().findByName(newCategory.getName());
                    categoryList.add(category);
                    categoryListAdapter.notifyDataSetChanged();
                });

                dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
            }
        });

        categoryOnClickListener = new CategoryOnClickListener() {
            @Override
            public void onCategoryClick(Category category) {}

            @Override
            public void onCategoryLongClick(Category category, CardView cardView) {
                // TODO: move category
            }
        };
    }

    private void filterCategories(String newText) {
        List<Category> filteredList = new ArrayList<>();
        for (Category reminder : categoryList) {
            if (reminder.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(reminder);
            }
        }
        categoryListAdapter.filterList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_toolbar_menu, menu);

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
            List<Category> sortedList = new ArrayList<>();
            switch (sortType) {
                case "default":
                    sortedList = appDatabase.categoryDAO().getAll();
                    break;
                case "name_asc":
                    sortedList = appDatabase.categoryDAO().getAllSortedByNameAsc();
                    break;
                case "name_desc":
                    sortedList = appDatabase.categoryDAO().getAllSortedByNameDesc();
                    break;
            }
            List<Category> finalSortedList = sortedList;
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