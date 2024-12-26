package com.example.reminderapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.CategoryDialogFragment;
import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Listeners.CategoryOnClickListener;
import com.example.reminderapp.Listeners.OnCategoryChangeListener;
import com.example.reminderapp.R;

import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder> {
    private final Context context;
    private List<Category> categoryList;
    private final CategoryOnClickListener clickListener;
    private final OnCategoryChangeListener changeListener;

    public CategoryListAdapter(Context context, List<Category> categoryList, CategoryOnClickListener clickListener, OnCategoryChangeListener changeListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.clickListener = clickListener;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public CategoryListAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_card_view, parent,false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categoryList.get(position);

        holder.nameTextView.setText(category.getName());
        holder.remindersNumberTextView.setText(R.string.text_no_reminders);

        holder.is_activeSwitchCompat.setChecked(category.isActive());
        updateCardColor(holder.categoryCardView, category.isActive());

        String hexColor = category.getColor();
        try {
            int color = Color.parseColor(hexColor);
            holder.colorCircleView.setBackgroundTintList(ColorStateList.valueOf(color));
        } catch (IllegalArgumentException e) {
            holder.colorCircleView.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }

        holder.is_activeSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            category.setActive(isChecked);
            updateCardColor(holder.categoryCardView, isChecked);

            new Thread(() -> AppDatabase.getInstance(context).categoryDAO().update(category)).start();
        });

        holder.optionsImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.categoryCardView, Gravity.END, 0, R.style.CustomPopupMenu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.edit_category) {
                    createEditDialog(position, category);
                    return true;
                } else if (itemId == R.id.delete_category) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete category")
                            .setMessage("Are you sure you want to delete category?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                if (changeListener != null) {
                                    changeListener.onCategoryDeleted(position);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }

                return false;
            });
            popupMenu.inflate(R.menu.category_popup_menu);
            popupMenu.show();
        });

        holder.itemView.setOnClickListener(view -> createEditDialog(position, category));

        holder.itemView.setOnLongClickListener(view -> {
            clickListener.onCategoryLongClick(category, holder.categoryCardView);
            return true;
        });
    }

    private void createEditDialog(int position, Category category) {
        CategoryDialogFragment dialogFragment = CategoryDialogFragment.newInstance(category);
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            dialogFragment.setOnCategoryUpdatedListener(updatedCategory -> {
                if (changeListener != null) {
                    changeListener.onCategoryUpdated(position, updatedCategory);
                }
            });
            dialogFragment.show(activity.getSupportFragmentManager(), CategoryDialogFragment.TAG);
        }
    }

    private void updateCardColor(CardView cardView, boolean isActive) {
        int color = isActive ? cardView.getContext().getColor(R.color.white) : cardView.getContext().getColor(R.color.light_gray);
        cardView.setCardBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<Category> filteredList) {
        categoryList = filteredList;
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final CardView categoryCardView;
        final TextView nameTextView, remindersNumberTextView;
        final SwitchCompat is_activeSwitchCompat;
        final ImageButton optionsImageButton;
        final View colorCircleView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryCardView = itemView.findViewById(R.id.category_card_view);
            nameTextView = itemView.findViewById(R.id.category_card_view_name);
            remindersNumberTextView = itemView.findViewById(R.id.category_card_view_reminder_number);
            is_activeSwitchCompat = itemView.findViewById(R.id.category_card_view_is_active);
            optionsImageButton = itemView.findViewById(R.id.category_card_view_options);
            colorCircleView = itemView.findViewById(R.id.category_card_view_color_circle);
        }
    }
}

