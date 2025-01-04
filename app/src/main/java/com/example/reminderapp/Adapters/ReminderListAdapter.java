package com.example.reminderapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Entities.Reminder;
import com.example.reminderapp.Listeners.OnItemClickListener;
import com.example.reminderapp.Listeners.OnItemDeletedListener;
import com.example.reminderapp.Listeners.OnItemUpdatedListener;
import com.example.reminderapp.R;
import com.example.reminderapp.ReminderAddActivity;
import com.example.reminderapp.Utils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ReminderViewHolder> {

    private final Context context;
    private List<Reminder> reminderList;
    private final OnItemClickListener<Reminder> clickListener;
    private final ActivityResultLauncher<Intent> reminderActivityLauncher;
    private final OnItemUpdatedListener<Reminder> itemUpdatedListener;
    private final OnItemDeletedListener<Reminder> itemDeletedListener;

    public ReminderListAdapter(Context context,
                               List<Reminder> reminderList,
                               OnItemClickListener<Reminder> clickListener,
                               ActivityResultLauncher<Intent> reminderActivityLauncher,
                               OnItemUpdatedListener<Reminder> itemUpdatedListener,
                               OnItemDeletedListener<Reminder> itemDeletedListener) {
        this.context = context;
        this.reminderList = reminderList;
        this.clickListener = clickListener;
        this.reminderActivityLauncher = reminderActivityLauncher;
        this.itemUpdatedListener = itemUpdatedListener;
        this.itemDeletedListener = itemDeletedListener;
    }

    @NonNull
    @Override
    public ReminderListAdapter.ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_reminder, parent,false);
        return new ReminderListAdapter.ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderListAdapter.ReminderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Reminder reminder = reminderList.get(position);

        holder.titleTextView.setText(reminder.getTitle());
        holder.titleTextView.setSelected(true);

        long time = reminder.getTime();
        long date = reminder.getDate();
        if (date > 0) {
            holder.notificationTimeLayout.setVisibility(View.VISIBLE);

            holder.timeDifferenceTextView.setText(Utils.calculateTimeDifference(time, date, context));
            holder.timeDifferenceTextView.setSelected(true);
        } else {
            holder.notificationTimeLayout.setVisibility(View.GONE);
        }

        holder.isCompletedCheckBox.setOnCheckedChangeListener(null);
        holder.isCompletedCheckBox.setChecked(reminder.isCompleted());

        checkIfReminderIsCompleted(holder);

        holder.isCompletedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setCompleted(isChecked);
            checkIfReminderIsCompleted(holder);
            itemUpdatedListener.onItemUpdated(position, reminder);
        });

        String hexColor = AppDatabase.getInstance(context).categoryDAO().findById(reminder.getCategoryId()).getColor();
        int color = Color.parseColor(hexColor);
        holder.reminderCardView.setStrokeColor(color);

        holder.optionsImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.reminderCardView, Gravity.END);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.edit_option) {
                    createEditActivity(position, reminder);
                    return true;
                } else if (itemId == R.id.delete_option) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete reminder")
                            .setMessage("Are you sure you want to delete reminder?")
                            .setPositiveButton("Yes", (dialog, which) -> itemDeletedListener.onItemDeleted(position, reminder))
                            .setNegativeButton("No", null)
                            .show();
                }

                return false;
            });
            popupMenu.inflate(R.menu.edit_popup_menu);
            popupMenu.show();
        });

        holder.itemView.setOnClickListener(view -> createEditActivity(position, reminder));

        holder.itemView.setOnLongClickListener(view -> {
            clickListener.onItemLongClick(reminder, holder.reminderCardView);
            return true;
        });
    }

    private void checkIfReminderIsCompleted(@NonNull ReminderViewHolder holder) {
        if (holder.isCompletedCheckBox.isChecked()) {
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.gray));
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void createEditActivity(int position, Reminder reminder) {
        Intent intent = new Intent(context, ReminderAddActivity.class);
        Category selectedCategory = AppDatabase.getInstance(context).categoryDAO().findById(reminder.getCategoryId());
        intent.putExtra("selected_category", selectedCategory);
        intent.putExtra("selected_reminder", reminder);
        intent.putExtra("position", position);

        reminderActivityLauncher.launch(intent);
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setReminders(List<Reminder> reminders) {
        this.reminderList.clear();
        this.reminderList.addAll(reminders);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<Reminder> filteredList) {
        reminderList = filteredList;
        notifyDataSetChanged();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView reminderCardView;
        final MaterialCheckBox isCompletedCheckBox;
        final TextView titleTextView, timeDifferenceTextView;
        final ImageButton optionsImageButton;
        final LinearLayout notificationTimeLayout;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderCardView = itemView.findViewById(R.id.reminder_card_view);
            isCompletedCheckBox = itemView.findViewById(R.id.is_completed_check_box);
            titleTextView = itemView.findViewById(R.id.reminder_name_text_view);
            timeDifferenceTextView = itemView.findViewById(R.id.how_many_time_difference_text_view_reminder);
            optionsImageButton = itemView.findViewById(R.id.reminder_card_view_options);
            notificationTimeLayout = itemView.findViewById(R.id.notification_time_layout);
        }
    }
}
