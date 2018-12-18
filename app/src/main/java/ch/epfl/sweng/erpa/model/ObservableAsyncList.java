package ch.epfl.sweng.erpa.model;

import com.annimon.stream.function.Consumer;

import java.util.Collection;

public interface ObservableAsyncList<T> {
    T get(int i);
    int size();
    boolean isLoading();
    void updateObservers();
    void addObserver(Consumer<ObservableAsyncList<T>> o);
    void refreshDataAndReset();
}
