package com.example.reminderapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.Entities.Reminder;
import com.example.reminderapp.R;
import com.example.reminderapp.ReminderClickListener;

import java.util.List;

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderViewHolder> {
    Context context;
    List<Reminder> list;
    ReminderClickListener listener;

    public ReminderListAdapter(Context context, List<Reminder> list, ReminderClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReminderViewHolder(LayoutInflater.from(context).inflate(R.layout.reminder_form, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        holder.title_reminder_form.setText(list.get(position).getTitle());
        holder.title_reminder_form.setSelected(true);

        holder.time_reminder_form.setText(list.get(position).getTime());
        holder.date_reminder_form.setText(list.get(position).getDate());
        holder.date_reminder_form.setSelected(true);

        holder.reminder_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(list.get(holder.getAdapterPosition()));
            }
        });

        holder.reminder_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.reminder_container);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<Reminder> filteredList) {
        list = filteredList;
        notifyDataSetChanged();
    }
}

class ReminderViewHolder extends RecyclerView.ViewHolder {
    CardView reminder_container;
    TextView title_reminder_form, time_reminder_form, date_reminder_form;

    public ReminderViewHolder(@NonNull View itemView) {
        super(itemView);
        reminder_container = itemView.findViewById(R.id.reminder_container);
        title_reminder_form = itemView.findViewById(R.id.title_reminder_form);
        title_reminder_form = itemView.findViewById(R.id.title_reminder_form);
        time_reminder_form = itemView.findViewById(R.id.time_reminder_form);
        date_reminder_form = itemView.findViewById(R.id.date_reminder_form);
    }
}