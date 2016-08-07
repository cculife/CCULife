package org.zankio.ccudata.base.model;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private Map<String, Object> storage = new HashMap<>();
    public <T>T get(String key, Class<? extends T> mClass) {
        Object value = storage.get(key);
        return mClass.cast(value);
    }

    public <T>T get(String key, Class<? extends T> mClass, T defaultValue) {
        Object value = storage.get(key);
        if (value == null) return defaultValue;
        return mClass.cast(value);
    }

    public void put(String key, Object value) {
        storage.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T>T get(String key) {
        return (T)storage.get(key);
    }
}
