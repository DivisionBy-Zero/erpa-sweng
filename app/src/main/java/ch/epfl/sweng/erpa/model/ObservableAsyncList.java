package ch.epfl.sweng.erpa.model;

import com.annimon.stream.function.Consumer;

import java.util.Collection;

/**
 * A list interface that updates its observers when its size changes
 * Due to asynchronous operations
 */
public interface ObservableAsyncList<T> extends Collection<T> {
    T get(int i);
    boolean isLoading();
    void updateObservers();
    void addObserver(Consumer<ObservableAsyncList<T>> o);
    void refreshDataAndReset();
}
