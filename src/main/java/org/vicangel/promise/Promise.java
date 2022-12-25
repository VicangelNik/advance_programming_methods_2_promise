package org.vicangel.promise;

import java.util.function.Consumer;
import java.util.function.Function;

import org.vicangel.promise.ValueOrError.Error;
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
  private ValueOrError<?> valueOrError = Value.of(null);

  public enum Status {
    PENDING,
    FULFILLED,
    REJECTED
  }

  // No instance fields are defined, perhaps you should add some!

  public Promise(PromiseExecutor<V> executor) {
    executor.execute(this::resolve, this::reject);
  }

  public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  public <T> Promise<T> then(Function<V, T> onResolve) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  // catch is a reserved word in Java.
  public Promise<Throwable> catchError(Consumer<Throwable> onReject) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  // finally is a reserved word in Java.
  public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onSettle) {
    onSettle.accept((ValueOrError<T>) valueOrError);
    return (Promise<ValueOrError<T>>) this;
  }

  public <T> Promise<T> resolve(T value) {
    valueOrError = Value.of(value);
    status = FULFILLED;
    return (Promise<T>) this;
  }

  public Promise<Throwable> reject(Throwable error) {
    valueOrError = Error.of(error);
    status = REJECTED;
    return (Promise<Throwable>) this;
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