package ch.epfl.sweng.erpa.services.GCP;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import lombok.Getter;

public abstract class LazyAsyncStream<T> extends AbstractList<T> implements Iterable<T>, ObservableAsyncList<T> {
    private final List<Consumer<ObservableAsyncList<T>>> observers = new ArrayList<>();
    final List<T> elements;
    final int chunks;

    AsyncTaskService asyncTaskService = new AsyncTaskService();

    @Getter boolean loading = true;

    LazyAsyncStream(int chunks) {
        assert chunks > 0;
        this.elements = new ArrayList<>();
        this.chunks = chunks;
    }

    LazyAsyncStream() {
        this(100);
    }

    abstract void loadAhead(int from);

    @Override public void updateObservers() {
        Log.d("updateObservers", "Updating Observers");
        Stream.of(observers).forEach(o -> o.accept(this));
    }

    @Override public void addObserver(Consumer<ObservableAsyncList<T>> o) {
        observers.add(o);
    }

    @Override public T get(int idx) {
        return elements.get(idx);
    }

    @Override public int size() {
        return elements.size();
    }

    List<T> getAsList() {
        return Collections.unmodifiableList(elements);
    }

    @NonNull @Override public Iterator<T> iterator() {
        return new LazyAsyncIterator();
    }

    @Override public void refreshDataAndReset() {
        elements.clear();
        loading = true;
        loadAhead(0);
    }

    private class LazyAsyncIterator implements Iterator<T> {
        int currIndex = 0;
        boolean fetching = false;

        @Override public boolean hasNext() {
            maybeEnqueueLookAhead();
            return currIndex < elements.size();
        }

        @Override public T next() {
            maybeEnqueueLookAhead();
            return elements.get(currIndex++);
        }

        private void maybeEnqueueLookAhead() {
            if (fetching)
                return;
            if (currIndex > elements.size() - chunks / 2) {
                fetching = true;
                asyncTaskService.<Void>run(() -> {
                    loadAhead(elements.size());
                    return null;
                }, o -> fetching = false, e -> {
                    fetching = false;
                    throw new RuntimeException("Could not continue looking ahead from server.", e);
                });
            }
        }
    }
}
