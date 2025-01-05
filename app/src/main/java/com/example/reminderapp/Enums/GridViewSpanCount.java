package com.example.reminderapp.Enums;

public enum GridViewSpanCount {
    ROW(1), TWO_CARDS(2);

    private final int value;

    GridViewSpanCount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
