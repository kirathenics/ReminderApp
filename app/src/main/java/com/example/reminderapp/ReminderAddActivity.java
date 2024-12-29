package com.example.reminderapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ReminderAddActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();

    private TextView reminderDescriptionTextView;
    private TextInputEditText reminderDescriptionEditText;
    private TextInputLayout reminderDescriptionTextInputLayout;
    private boolean isDescriptionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        Toolbar toolbar = findViewById(R.id.reminder_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_add_reminder);
        }

        String selectedCategory = getIntent().getStringExtra("selected_category");

        TextView chooseCategoryTextView = findViewById(R.id.reminder_choose_category_text_view);
        if (selectedCategory != null) {
            chooseCategoryTextView.setText(selectedCategory);
        }
        if (selectedCategory != null && selectedCategory.equals(getString(R.string.category_name_all))) {
            chooseCategoryTextView.setText(R.string.category_name_by_default);
        }

        appDatabase = AppDatabase.getInstance(this);
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAll();
            runOnUiThread(() -> {
//                categoryListAdapter = new CategoryListAdapter(CategoryManagementActivity.this, categoryList, categoryOnClickListener, new OnCategoryChangeListener() {
//                    @Override
//                    public void onCategoryUpdated(int position, Category updatedCategory) {
//                        appDatabase.categoryDAO().update(updatedCategory);
//                        categoryList.set(position, updatedCategory);
//                        categoryListAdapter.notifyItemChanged(position);
//                    }
//
//                    @Override
//                    public void onCategoryDeleted(int position) {
////                        new Thread(() -> AppDatabase.getInstance(context).categoryDAO().delete(category)).start();
//                        Category category = categoryList.get(position);
//                        appDatabase.categoryDAO().delete(category);
//                        categoryList.remove(position);
//                        categoryListAdapter.notifyItemRemoved(position);
//                    }
//                });
//                updateCategoryRecyclerView(GRID_SPAN_COUNT);
            });
        }).start();


        reminderDescriptionTextView = findViewById(R.id.reminder_description_text_view);
        reminderDescriptionTextInputLayout = findViewById(R.id.reminder_description_text_layout);
        reminderDescriptionEditText = findViewById(R.id.reminder_description_edit_text);
        reminderDescriptionTextView.setOnClickListener(view -> toggleDescriptionVisibility());

        ImageButton addCategoryImageButton = findViewById(R.id.reminder_add_category_image_button);
        addCategoryImageButton.setOnClickListener(v -> {
            CategoryDialogFragment dialogFragment = CategoryDialogFragment.newInstance();
            dialogFragment.setOnCategoryUpdatedListener(newCategory -> new Thread(() -> appDatabase.categoryDAO().insert(newCategory)).start());
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });

        chooseCategoryTextView.setOnClickListener(v -> {
            ChooseCategoryDialogFragment dialogFragment = ChooseCategoryDialogFragment.newInstance();
            dialogFragment.setOnCategoryChoseListener(chosenCategory -> chooseCategoryTextView.setText(chosenCategory.getName()));
            dialogFragment.show(getSupportFragmentManager(), CategoryDialogFragment.TAG);
        });
    }

    private void toggleDescriptionVisibility() {
        if (isDescriptionVisible) {
            if (TextUtils.isEmpty(reminderDescriptionEditText.getText())) {
                reminderDescriptionTextInputLayout.setVisibility(View.GONE);
                reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
            } else {
                reminderDescriptionTextInputLayout.setVisibility(View.GONE);
                reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up, 0);
            }
        } else {
            reminderDescriptionTextInputLayout.setVisibility(View.VISIBLE);
            reminderDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down, 0);
        }

        isDescriptionVisible = !isDescriptionVisible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_add_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.add_reminder_item) {
            finish();
            return true;
        }
        return false;
    }
}