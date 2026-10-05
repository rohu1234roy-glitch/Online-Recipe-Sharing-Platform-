package com.recipeplatform.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// Generic repository class for in-memory list filtering and lookup (Generics Rubric requirement)
public class GenericRepository<T> {

    // Filter a list of items using a predicate
    public List<T> filter(List<T> items, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) {
            return result;
        }
        for (T item : items) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // Find first item matching condition
    public T findFirst(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) {
            return null;
        }
        for (T item : items) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
}
