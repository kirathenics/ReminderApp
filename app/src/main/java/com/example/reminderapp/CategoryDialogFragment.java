package com.example.reminderapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reminderapp.Databases.AppDatabase;
import com.example.reminderapp.Entities.Category;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CategoryDialogFragment extends DialogFragment {

    private EditText editTextName;
    private View selectedColorView;
    private String selectedColorHex;
    private final Map<View, ImageView> colorViewsMap = new HashMap<>();
    private Category category;
    private OnCategoryUpdatedListener onCategoryUpdatedListener;

    public static final String TAG = "CategoryDialogFragment";

    public interface OnCategoryUpdatedListener {
        void onCategoryUpdated(Category updatedCategory);
    }

    public static CategoryDialogFragment newInstance() {
        return new CategoryDialogFragment();
    }

    public static CategoryDialogFragment newInstance(Category category) {
        CategoryDialogFragment fragment = new CategoryDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_dialog, container, false);

        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        editTextName = view.findViewById(R.id.edit_text_name);
        Button setCategoryButton = view.findViewById(R.id.set_category_button);
        Button cancelCategoryButton = view.findViewById(R.id.cancel_category_button);

        setupColorPicker(view);

        if (getArguments() != null && getArguments().containsKey("category")) {
            category = (Category) getArguments().getSerializable("category");
            assert category != null;
            editTextName.setText(category.getName());
            dialogTitle.setText(R.string.title_category_editing);
            selectColor(category.getColor());
        } else {
            category = new Category();
            int colorInt = ContextCompat.getColor(requireContext(), R.color.lavender_medium);
            String colorHexWithoutAlpha = String.format("#%08X", colorInt);
            selectColor(colorHexWithoutAlpha);
            category.setActive(true);
            dialogTitle.setText(R.string.title_create_category);
        }

        cancelCategoryButton.setOnClickListener(v -> dismiss());

        setCategoryButton.setOnClickListener(v -> {
            String categoryName = editTextName.getText().toString().trim();

            if (categoryName.isEmpty()) {
                showError(getString(R.string.error_empty_category_name));
                return;
            }

            if (!isCategoryNameUnique(categoryName)) {
                showError(getString(R.string.error_category_exists));
                return;
            }

            category.setName(categoryName);
            category.setColor(selectedColorHex);

            if (onCategoryUpdatedListener != null) {
                onCategoryUpdatedListener.onCategoryUpdated(category);
            }

            dismiss();
        });

        TextInputEditText editTextName = view.findViewById(R.id.edit_text_name);
        TextInputLayout inputLayout = view.findViewById(R.id.edit_text_category_name);
        if (editTextName != null) {
            editTextName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (inputLayout != null) {
                        inputLayout.setError(null);
                        inputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.lavender_dark));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
//                    String categoryName = s.toString().trim();
//
//                    if (!isCategoryNameUnique(categoryName)) {
//                        showError(getString(R.string.error_category_exists));
//                    }
                }
            });
        }

        return view;
    }

    private void showError(String message) {
        TextInputLayout inputLayout = requireView().findViewById(R.id.edit_text_category_name);
        inputLayout.setError(message);
        inputLayout.setErrorTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.prismatic_red)));
        inputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.prismatic_red));
    }

    private boolean isCategoryNameUnique(String categoryName) {
        return AppDatabase.getInstance(requireContext()).categoryDAO().findByName(categoryName) == null;
    }

    private void setupColorPicker(View view) {
        colorViewsMap.put(view.findViewById(R.id.circle_color_lavender_medium), view.findViewById(R.id.check_color_lavender_medium));
        colorViewsMap.put(view.findViewById(R.id.circle_color_blueberry), view.findViewById(R.id.check_color_blueberry));
        colorViewsMap.put(view.findViewById(R.id.circle_color_sky_blue), view.findViewById(R.id.check_color_sky_blue));
        colorViewsMap.put(view.findViewById(R.id.circle_color_mint_green), view.findViewById(R.id.check_color_mint_green));
        colorViewsMap.put(view.findViewById(R.id.circle_color_sand), view.findViewById(R.id.check_color_sand));
        colorViewsMap.put(view.findViewById(R.id.circle_color_pale_orange), view.findViewById(R.id.check_color_pale_orange));
        colorViewsMap.put(view.findViewById(R.id.circle_color_prismatic_red), view.findViewById(R.id.check_color_prismatic_red));

        for (Map.Entry<View, ImageView> entry : colorViewsMap.entrySet()) {
            View colorView = entry.getKey();
            colorView.setOnClickListener(v -> selectColorByView(colorView));
        }
    }

    private void selectColor(String colorHex) {
        for (Map.Entry<View, ImageView> entry : colorViewsMap.entrySet()) {
            View colorView = entry.getKey();
            int colorInt = Objects.requireNonNull(colorView.getBackgroundTintList()).getDefaultColor();

            String currentColorHex = String.format("#%08X", colorInt);
            if (currentColorHex.equalsIgnoreCase(colorHex)) {
                selectColorByView(entry.getKey());
                break;
            }
        }
    }

    private void selectColorByView(View colorView) {
        if (selectedColorView != null) {
            Objects.requireNonNull(colorViewsMap.get(selectedColorView)).setVisibility(View.GONE);
        }

        selectedColorView = colorView;
        ImageView checkImageView = colorViewsMap.get(colorView);
        if (checkImageView != null) {
            checkImageView.setVisibility(View.VISIBLE);

            int originalColor = Objects.requireNonNull(colorView.getBackgroundTintList()).getDefaultColor();
            int darkerColor = manipulateColorBrightness(originalColor, 0.7f);

            checkImageView.setColorFilter(darkerColor, PorterDuff.Mode.SRC_IN);
        }

        int colorInt = Objects.requireNonNull(colorView.getBackgroundTintList()).getDefaultColor();
        selectedColorHex = String.format("#%08X", colorInt);
    }

    private int manipulateColorBrightness(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Math.round(Color.red(color) * factor);
        int green = Math.round(Color.green(color) * factor);
        int blue = Math.round(Color.blue(color) * factor);
        return Color.argb(alpha, Math.min(red, 255), Math.min(green, 255), Math.min(blue, 255));
    }

    public void setOnCategoryUpdatedListener(OnCategoryUpdatedListener listener) {
        this.onCategoryUpdatedListener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}