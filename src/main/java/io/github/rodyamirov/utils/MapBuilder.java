package io.github.rodyamirov.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard.rast on 12/21/16.
 */
public class MapBuilder<K, V> {
    private final HashMap<K, V> map;

    private MapBuilder(K key, V value) {
        map = new HashMap<>();
        map.put(key, value);
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);

        return this;
    }

    public Map<K, V> build() {
        return map;
    }

    public static <K, V> MapBuilder<K, V> start(K key, V value) {
        return new MapBuilder<>(key, value);
    }
}
