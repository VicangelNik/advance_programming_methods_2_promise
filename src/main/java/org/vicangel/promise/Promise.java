package org.vicangel.promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private static final Logger LOGGER = Logger.getLogger(Promise.class.getName());
  private final Object lock;
  private volatile Status status = PENDING;
  private ValueOrError<V> valueOrError;

  public enum Status {
    PENDING,
    FULFILLED,
    REJECTED
  }

  // No instance fields are defined, perhaps you should add some!

  public Promise(PromiseExecutor<V> executor) {
    lock = new Object();
    executor.execute(this::resolve, this::reject);
  }

  public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
    new TransformAction<>(this, onResolve, onReject).run();
    return (Promise<ValueOrError<T>>) this;
  }

  public <T> Promise<T> then(Function<V, T> onResolve) {
    new TransformAction<>(this, onResolve).run();
    return (Promise<T>) this;
  }

  /**
   * @apiNote catch is a reserved word in Java.
   */
  public Promise<Throwable> catchError(Consumer<Throwable> onReject) {
    onReject.accept(this.valueOrError.error());
    return reject(this.valueOrError.error());
  }

  /**
   * @apiNote finally is a reserved word in Java.
   */
  public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onSettle) {
    //  new ConsumeAction(this, (Consumer<T>) onSettle).run();
    onSettle.accept((ValueOrError<T>) this.valueOrError);
    return (Promise<ValueOrError<T>>) this;
  }

  public <T> Promise<T> resolve(T value) {
    this.valueOrError = (ValueOrError<V>) Value.of(value);//(ValueOrError<V>) ValueOrError.Factory.ofValue(value);
    this.status = FULFILLED;
    synchronized (lock) {
      lock.notifyAll();
    }
    LOGGER.log(Level.INFO, "Promise ran with value: {0}", value);
    return (Promise<T>) this;
  }

  public Promise<Throwable> reject(Throwable error) {
    this.valueOrError = ValueOrError.Error.of(error);
    this.status = REJECTED;
    synchronized (lock) {
      lock.notifyAll();
    }
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

  public V get() throws ExecutionException {
    synchronized (lock) {
      while (this.status == PENDING) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          LOGGER.warning(e.getMessage());
          Thread.currentThread().interrupt();
        }
      }
    }

    if (this.status == FULFILLED) {
      return this.valueOrError.value();
    }
    throw new ExecutionException(this.valueOrError.error());
  }

  public V get(long timeout, TimeUnit unit) throws ExecutionException {
    synchronized (lock) {
      while (this.status == PENDING) {
        try {
          lock.wait(unit.toMillis(timeout));
        } catch (InterruptedException e) {
          LOGGER.warning(e.getMessage());
          Thread.currentThread().interrupt();
        }
      }
    }

    if (this.status == FULFILLED) {
      return this.valueOrError.value();
    }
    throw new ExecutionException(this.valueOrError.error());
  }

  private class TransformAction<T> implements Runnable {

    private final Promise<V> src;
    private final Function<V, T> func;
    private Consumer<Throwable> onReject;

    private TransformAction(Promise<V> src, Function<V, T> func) {
      this.src = src;
      this.func = func;
    }

    private TransformAction(Promise<V> src, Function<V, T> func, Consumer<Throwable> onReject) {
      this.src = src;
      this.func = func;
      this.onReject = onReject;
    }

    @Override
    public void run() {
      try {
        src.resolve(func.apply(src.get()));
      } catch (Exception exception) {
        if (onReject != null) {
          src.catchError(onReject);
        } else {
          src.reject(exception);
        }
      }
    }
  }
}