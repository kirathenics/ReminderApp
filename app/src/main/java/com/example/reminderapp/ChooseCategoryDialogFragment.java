package com.example.reminderapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.reminderapp.Adapters.ChooseCategoryListAdapter;
import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.example.reminderapp.Listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseCategoryDialogFragment extends DialogFragment {

    private AppDatabase appDatabase;
    private List<Category> categoryList = new ArrayList<>();
    private RecyclerView categoryRecyclerView;

    private ChooseCategoryListAdapter chooseCategoryListAdapter;
    private OnCategoryChoseListener onCategoryChoseListener;

    public interface OnCategoryChoseListener {
        void onCategoryChose(Category chosenCategory);
    }

    public static ChooseCategoryDialogFragment newInstance() {
        return new ChooseCategoryDialogFragment();
    }

    public void setOnCategoryChoseListener(ChooseCategoryDialogFragment.OnCategoryChoseListener listener) {
        this.onCategoryChoseListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_choose_category, container, false);

        SearchView categorySearchView = view.findViewById(R.id.category_search_view);
        categorySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        categoryRecyclerView = view.findViewById(R.id.choose_category_recycler_view);

        OnItemClickListener<Category> onCategoryClickListener = new OnItemClickListener<>() {
            @Override
            public void onItemClick(Category item) {
                if (onCategoryChoseListener != null) {
                    onCategoryChoseListener.onCategoryChose(item);
                    dismiss();
                }
            }

            @Override
            public void onItemLongClick(Category item, CardView cardView) {}
        };

        appDatabase = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            categoryList = appDatabase.categoryDAO().getAll();
            requireActivity().runOnUiThread(() -> {
                chooseCategoryListAdapter = new ChooseCategoryListAdapter(requireContext(), categoryList, onCategoryClickListener);
                categoryRecyclerView.setHasFixedSize(true);
                categoryRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
                categoryRecyclerView.setAdapter(chooseCategoryListAdapter);
            });
        }).start();

        return view;
    }

    private void filterCategories(String query) {
        List<Category> filteredList = new ArrayList<>();
        for (Category reminder : categoryList) {
            if (reminder.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(reminder);
            }
        }
        chooseCategoryListAdapter.filterList(filteredList);
    }
}