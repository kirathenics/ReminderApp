package com.example.reminderapp.Converters;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {
    @TypeConverter
    public static List<Integer> fromString(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @TypeConverter
    public static String fromList(List<Integer> list) {
        return list == null || list.isEmpty() ? "" : list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
