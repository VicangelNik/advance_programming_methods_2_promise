package org.vicangel.promise;

import java.util.function.Consumer;
import java.util.function.Function;

import org.vicangel.promise.ValueOrError.Value;

import static org.vicangel.promise.Promise.Status.FULFILLED;
import static org.vicangel.promise.Promise.Status.PENDING;
import static org.vicangel.promise.Promise.Status.REJECTED;

/**
 * @author Nikiforos Xylogiannopoulos
 * <p>
 * > "What I cannot create, I do not understand"
 * > Richard Feynman
 * > https://en.wikipedia.org/wiki/Richard_Feynman
 * <p>
 * This is an incomplete implementation of the Javascript Promise machinery in Java.
 * You should expand and ultimately complete it according to the following:
 * <p>
 * (1) You should only use the low-level Java concurrency primitives (like
 * java.lang.Thread/Runnable, wait/notify, synchronized, volatile, etc)
 * in your implementation.
 * <p>
 * (2) The members of the java.util.concurrent package
 * (such as Executor, Future, CompletableFuture, etc.) cannot be used.
 * <p>
 * (3) No other library should be used.
 * <p>
 * (4) Create as many threads as you think appropriate and don't worry about
 * recycling them or implementing a Thread Pool.
 * <p>
 * (5) I may have missed something from the spec, so please report any issues
 * in the course's e-class.
 * <p>
 * The Javascript Promise reference is here:
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise
 * <p>
 * A helpful guide to help you understand Promises is available here:
 * https://javascript.info/async
 */
public class Promise<V> {

  private volatile Status status = PENDING;
  private ValueOrError<?> valueOrError;

  public enum Status {
    PENDING,
    FULFILLED,
    REJECTED
  }

  // No instance fields are defined, perhaps you should add some!

  public Promise(PromiseExecutor<V> executor) {
//    Thread newThread = new Thread(() -> executor.execute(this::resolve, this::reject));
//    newThread.start();
    executor.execute(this::resolve, this::reject);
  }

  private Promise(final V value, final Status status) {
    this.valueOrError = Value.of(value);
    this.status = status;
  }

  public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
    if (this.status != FULFILLED) {
      throw new IllegalStateException("On then, status should not be other than PENDING, current state: " + this.status);
    }
    try {
      final T value = onResolve.apply((V) this.valueOrError.value());
      return (Promise<ValueOrError<T>>) new Promise<>(value, FULFILLED);
    } catch (Exception e) {
      onReject.accept(e);
      return (Promise<ValueOrError<T>>) new Promise<>((T) this.valueOrError, REJECTED);
    }
  }

  public <T> Promise<T> then(Function<V, T> onResolve) {
    if (this.status != FULFILLED) {
      throw new IllegalStateException("On then, status should not be other than PENDING, current state: " + this.status);
    } else if (valueOrError == null) {
      this.status = PENDING;
      return resolve(null);
    }
    this.status = PENDING;
    return resolve(onResolve.apply((V) this.valueOrError.value()));
  }

  /**
   * @apiNote catch is a reserved word in Java.
   */
  public Promise<Throwable> catchError(Consumer<Throwable> onReject) {
    onReject.accept(this.valueOrError.error());
    return reject(this.valueOrError.error());
  }

  // finally is a reserved word in Java.
  public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onSettle) {
    onSettle.accept((ValueOrError<T>) valueOrError.value());
    return (Promise<ValueOrError<T>>) this;
  }

  public <T> Promise<T> resolve(T value) {
    if (value instanceof Throwable) {
      return (Promise<T>) reject((Throwable) value);
    } else if (this.status != PENDING) {
      throw new IllegalStateException("On resolve status should not be other than PENDING, current state: " + this.status);
    }
    return new Promise<>(value, FULFILLED);
  }

  public Promise<Throwable> reject(Throwable error) {
    if (this.status != PENDING) {
      throw new IllegalStateException("On reject status should not be other than PENDING, current state: " + this.status);
    }
    return new Promise<>(error, REJECTED);
  }

  public static <T> Promise<T> race(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  public static <T> Promise<T> any(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  public static <T> Promise<T> all(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  public static <T> Promise<T> allSettled(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }
}