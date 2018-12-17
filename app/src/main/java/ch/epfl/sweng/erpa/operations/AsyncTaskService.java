package ch.epfl.sweng.erpa.operations;

import android.os.AsyncTask;
import android.util.Log;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.ThrowableSupplier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static android.support.constraint.Constraints.TAG;

public class AsyncTaskService {
    @Setter @NonNull Consumer<Runnable> resultConsumerContext = Runnable::run;

    public static <T> Consumer<Optional<T>> failIfNotFound(String uuid, Consumer<T> consumer) {
        return opt -> opt.ifPresentOrElse(consumer, () -> {
            RuntimeException exception = new IllegalArgumentException("Could not find requested resource with UUID " + uuid);
            Log.w(TAG, "Failed to execute closure " + consumer.toString() + ": ", exception);
            throw exception;
        });
    }

    public <T> Runner<T> create(ThrowableSupplier<T, Throwable> supplier, Consumer<T> resultConsumer) {
        return new Runner<>(resultConsumerContext, resultConsumer, supplier);
    }

    public <T> Runner<T> create(ThrowableSupplier<T, Throwable> supplier, Consumer<T> resultConsumer, Consumer<Throwable> exceptionHandler) {
        Runner<T> runner = new Runner<>(resultConsumerContext, resultConsumer, supplier);
        runner.setThrowableConsumer(exceptionHandler);
        return runner;
    }

    public <T> Runner<T> run(ThrowableSupplier<T, Throwable> supplier, Consumer<T> resultConsumer) {
        Runner<T> runner = create(supplier, resultConsumer);
        runner.execute();
        return runner;
    }

    public <T> Runner<T> run(ThrowableSupplier<T, Throwable> supplier, Consumer<T> resultConsumer, Consumer<Throwable> exceptionHandler) {
        Runner<T> runner = create(supplier, resultConsumer);
        runner.setThrowableConsumer(exceptionHandler);
        runner.execute();
        return runner;
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Runner<T> extends AsyncTask<Void, Void, Exceptional<T>> {
        @NonNull private final Consumer<Runnable> resultConsumerContext; // Duplicating to avoid leaks
        @NonNull private final Consumer<T> resultConsumer;
        @NonNull private final ThrowableSupplier<T, Throwable> supplier;
        @Setter Consumer<Throwable> throwableConsumer = throwable -> Log.e("asyncTask", "Exception on asynchronousTask", throwable);

        @Override protected Exceptional<T> doInBackground(Void... o) {
            Consumer<T> withExceptionHandlerConsumer = r -> Exceptional.of(() -> {
                resultConsumer.accept(r);
                return null;
            }).ifException(this::exceptionHandler);
            Consumer<T> withinProvidedContextConsummer = r -> resultConsumerContext.accept(() -> withExceptionHandlerConsumer.accept(r));
            return Exceptional.of(supplier).ifException(this::exceptionHandler).ifPresent(withinProvidedContextConsummer);
        }

        void exceptionHandler(Throwable t) {
            if (UndeclaredThrowableException.class.isInstance(t))
                this.exceptionHandler(t.getCause());
            else if (InvocationTargetException.class.isInstance(t))
                this.exceptionHandler(t.getCause());
            else
                resultConsumerContext.accept(() -> this.throwableConsumer.accept(t));
        }
    }
}
