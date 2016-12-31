package io.github.rodyamirov.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class ListHelper {
    private ListHelper() {}

    @SafeVarargs
    public static <T> List<T> list(T... elements) {
        return Arrays.asList(elements);
    }
}
