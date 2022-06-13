/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SentryPropagating {
  private static class SentryPropagatingSupplier<T> extends SentryScope implements Supplier<T> {

    private final Supplier<T> delegate;

    public SentryPropagatingSupplier(Supplier<T> delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public T get() {
      startSpan();
      try {
        return delegate.get();
      } finally {
        endSpan();
      }
    }
  }

  private static class SentryPropagatingConsumer<T> extends SentryScope implements Consumer<T> {

    private final Consumer<T> delegate;

    public SentryPropagatingConsumer(Consumer<T> delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public void accept(T t) {
      startSpan();
      try {
        delegate.accept(t);
      } finally {
        endSpan();
      }
    }
  }

  private static class SentryPropagatingRunnable extends SentryScope implements Runnable {

    private final Runnable delegate;

    public SentryPropagatingRunnable(Runnable delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public void run() {
      startSpan();
      try {
        delegate.run();
      } finally {
        endSpan();
      }
    }
  }

  private static class SentryScope {
    private ITransaction transaction;
    private ISpan span;
    private Object delegate;

    SentryScope(Object delegate) {
      this.delegate = delegate;
      Sentry.configureScope(scope -> transaction = scope.getTransaction());
    }

    void startSpan() {
      if (transaction != null) {
        Sentry.configureScope(scope -> scope.setTransaction(transaction));
        span = Sentry.getSpan();
        if (span != null) {
          span = span.startChild("Async: " + delegate.getClass().getName());
        }
      }
    }

    void endSpan() {
      if (span != null) {
        span.finish();
      }
    }
  }

  public static <T> Supplier<T> withSentry(Supplier<T> delegate) {
    return new SentryPropagatingSupplier<>(delegate);
  }

  public static <T> Consumer<T> withSentry(Consumer<T> delegate) {
    return new SentryPropagatingConsumer<>(delegate);
  }

  public static Runnable withSentry(Runnable delegate) {
    return new SentryPropagatingRunnable(delegate);
  }
}
