package com.example.reminderapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Listeners.OnCategoryClickListener;
import com.example.reminderapp.R;

import java.util.List;

public class ChooseCategoryListAdapter extends RecyclerView.Adapter<ChooseCategoryListAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categoryList;
    private final OnCategoryClickListener clickListener;

    public ChooseCategoryListAdapter(Context context, List<Category> categoryList, OnCategoryClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChooseCategoryListAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_category, parent,false);
        return new ChooseCategoryListAdapter.CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseCategoryListAdapter.CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categoryList.get(position);

        holder.nameTextView.setText(category.getName());

        String hexColor = category.getColor();
        try {
            int color = Color.parseColor(hexColor);
            holder.colorCircleView.setBackgroundTintList(ColorStateList.valueOf(color));
        } catch (IllegalArgumentException e) {
            holder.colorCircleView.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }

        holder.itemView.setOnClickListener(view -> clickListener.onCategoryClick(categoryList.get(holder.getAdapterPosition())));

        holder.itemView.setOnLongClickListener(view -> {
            clickListener.onCategoryLongClick(category, holder.categoryCardView);
            return true;
        });
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
        final TextView nameTextView;
        final View colorCircleView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryCardView = itemView.findViewById(R.id.category_card_view);
            nameTextView = itemView.findViewById(R.id.category_card_view_name);
            colorCircleView = itemView.findViewById(R.id.category_card_view_color_circle);
        }
    }
}
