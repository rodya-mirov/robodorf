package io.github.rodyamirov.utils;

import java.util.EmptyStackException;

/**
 * In another place we have a need for a "stack-like" object which can only hold one element.
 * This data structure manages that requirement automatically without need for a lot of excess
 * checking.
 *
 * Created by richard.rast on 12/30/16.
 */
public class SingleElementStack<T> {
    private T element;
    private boolean hasElement;

    public SingleElementStack() {
        element = null;
        hasElement = false;
    }

    public T pop() {
        if (hasElement) {
            hasElement = false;
            return element;
        } else {
            throw new EmptyStackException();
        }
    }

    public T peek() {
        if (hasElement) {
            return element;
        } else {
            throw new EmptyStackException();
        }
    }

    public boolean isEmpty() {
        return !hasElement;
    }

    public void push(T element) {
        if (hasElement) {
            throw new IllegalStateException("This stack is already full!");
        } else {
            hasElement = true;
            this.element = element;
        }
    }
}
