package org.vicangel.promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.vicangel.promise.Status.FULFILLED;
import static org.vicangel.promise.Status.PENDING;
import static org.vicangel.promise.Status.REJECTED;

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
public class Promise<V> extends PromiseSupport {

  private static final Logger LOGGER = Logger.getLogger(Promise.class.getName());
  private final Object lock;
  private volatile Status status = PENDING;
  private ValueOrError<V> valueOrError;

  // No instance fields are defined, perhaps you should add some!

  protected Promise(PromiseExecutor<V> executor) {
    lock = new Object();
    executor.execute(this::fullFillResolve, this::fullFillReject);
  }

  /**
   * The then() method of a Promise object takes up to two arguments: callback functions for the fulfilled
   * and rejected cases of the Promise. It immediately returns an equivalent Promise object,
   * allowing you to chain calls to other promise methods.
   *
   * @param onResolve A Function asynchronously called if the Promise is fulfilled. This function has one parameter,
   *                  the fulfillment value. If it is not a function, it is internally replaced with
   *                  an identity function ((x) => x) which simply passes the fulfillment value forward.
   * @param onReject  A Function asynchronously called if the Promise is rejected. This function has one parameter,
   *                  the rejection reason. If it is not a function, it is internally replaced with
   *                  a thrower function ((x) => { throw x; }) which throws the rejection reason it received.
   *
   * @return Returns a new Promise immediately. This new promise is always pending when returned,
   * regardless of the current promise's status. One of the onFulfilled and onRejected handlers will be executed
   * to handle the current promise's fulfillment or rejection. The call always happens asynchronously,
   * even when the current promise is already settled. The behavior of the returned promise (call it p) depends on
   * the handler's execution result, following a specific set of rules. If the handler function:
   * <p>
   * returns a value: p gets fulfilled with the returned value as its value.
   * doesn't return anything: p gets fulfilled with undefined.
   * throws an error: p gets rejected with the thrown error as its value.
   * returns an already fulfilled promise: p gets fulfilled with that promise's value as its value.
   * returns an already rejected promise: p gets rejected with that promise's value as its value.
   * returns another pending promise: the fulfillment/rejection of the promise returned by then will be after the
   * resolution/rejection of the promise returned by the handler. Also, the resolved value of the promise returned by
   * then will be the same as the resolved value of the promise returned by the handler.
   *
   * @apiNote The then() method schedules callback functions for the eventual completion of a Promise — either
   * fulfillment or rejection. It is the primitive method of promises: the thenable protocol expects all promise-like objects
   * to expose a then() method, and the catch() and finally() methods both work by invoking the object's then() method.
   * <p>
   * For more information about the onRejected handler, see the catch() reference.
   * <p>
   * then() returns a new promise object. If you call the then() method twice on the same promise object (instead of chaining),
   * then this promise object will have two pairs of settlement handlers. All handlers attached to the same promise object
   * are always called in the order they were added. Moreover, the two promises returned by each call of then() start
   * separate chains and do not wait for each other's settlement.
   * <p>
   * Thenable objects that arise along the then() chain are always resolved — the onFulfilled handler never receives a
   * thenable object, and any thenable returned by either handler are always resolved before being passed to the next handler.
   * This is because when constructing the new promise, the resolve and reject functions passed by the executor are saved,
   * and when the current promise settles, the respective function will be called with the fulfillment value or rejection reason.
   * The resolving logic comes from the resolver function passed by the Promise() constructor.
   * <p>
   * then() supports subclassing, which means it can be called on instances of subclasses of Promise,
   * and the result will be a promise of the subclass type. You can customize the type of the return value through
   * the @@species property.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/then">...</a>
   */
  public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
    new TransformAction<>(this, onResolve, onReject).run();
//    this.status = PENDING;
//    synchronized (lock) {
//      lock.notifyAll();
//    }
    LOGGER.log(Level.INFO, "then() with 2 arguments is called!!");
    return (Promise<ValueOrError<T>>) this;
  }

  public <T> Promise<T> then(Function<V, T> onResolve) {
    new TransformAction<>(this, onResolve).run();
    this.status = PENDING;
    synchronized (lock) {
      lock.notifyAll();
    }
    LOGGER.log(Level.INFO, "then() with 1 argument is called!");
    return (Promise<T>) this;
  }

  /**
   * The catch() method of a Promise object schedules a function to be called when the promise is rejected.
   * It immediately returns an equivalent Promise object, allowing you to chain calls to other promise methods.
   * It is a shortcut for Promise.prototype.then(undefined, onRejected).
   *
   * @param onRejected A Function called when the Promise is rejected. This function has one parameter: the rejection reason.
   *
   * @return Returns a new Promise. This new promise is always pending when returned, regardless of the current promise's status.
   * It's eventually rejected if onRejected throws an error or returns a Promise which is itself rejected;
   * otherwise, it's eventually fulfilled.
   *
   * @implNote catchError because catch is a reserved word in Java.
   * @apiNote The catch method is used for error handling in promise composition. Since it returns a Promise,
   * it can be chained in the same way as its sister method, then().
   * <p>
   * If a promise becomes rejected, and there are no rejection handlers to call (a handler can be attached through any of then(),
   * catch(), or finally()), then the rejection event is surfaced by the host. In the browser,
   * this results in an unhandledrejection event. If a handler is attached to a rejected promise whose rejection
   * has already caused an unhandled rejection event, then another rejectionhandled event is fired.
   * <p>
   * catch() internally calls then() on the object upon which it was called, passing undefined and onRejected as arguments.
   * The value of that call is directly returned. This is observable if you wrap the methods.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/catch">...</a>
   */
  public <T> Promise<Throwable> catchError(final Consumer<Throwable> onRejected) {
    onRejected.accept(this.valueOrError.error());
    synchronized (lock) {
      lock.notifyAll();
    }
    final Function<V, T> catchErrorFunction = onRej -> (T) this;
    LOGGER.log(Level.INFO, "catchError ran");
    return (Promise<Throwable>) then(catchErrorFunction);
  }

  /**
   * @apiNote finally is a reserved word in Java.
   */
  public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onSettle) {
    //  new ConsumeAction(this, (Consumer<T>) onSettle).run();
    onSettle.accept((ValueOrError<T>) this.valueOrError);
    return (Promise<ValueOrError<T>>) this;
  }

  private <T> void fullFillResolve(T value) {
    this.valueOrError = (ValueOrError<V>) ValueOrError.Value.of(value);
    this.status = FULFILLED;
    synchronized (lock) {
      lock.notifyAll();
    }
    LOGGER.log(Level.INFO, "fullFillResolve ran with value: {0}", value);
  }

  public void fullFillReject(Throwable reason) {
    this.valueOrError = ValueOrError.Error.of(reason);
    this.status = REJECTED;
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  public static <T> Promise<T> race(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  public static <T> Promise<T> any(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  /**
   * The Promise.all() static method takes an iterable of promises as input and returns a single Promise.
   * This returned promise fulfills when all the input's promises fulfill (including when an empty iterable is passed),
   * with an array of the fulfillment values.
   * It rejects when any of the input's promises rejects, with this first rejection reason.
   * <p>
   * The Promise.all() method is one of the promise concurrency methods. It can be useful for aggregating the results of
   * multiple promises. It is typically used when there are multiple related asynchronous tasks that the overall code
   * relies on to work successfully — all of whom we want to fulfill before the code execution continues.
   * <p>
   * Promise.all() will reject immediately upon any of the input promises rejecting. In comparison, the promise returned
   * by Promise.allSettled() will wait for all input promises to complete, regardless of whether one rejects.
   * Use allSettled() if you need the final result of every promise in the input iterable.
   *
   * @param promises
   * @param <T>
   *
   * @return
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all">...</a>
   */
  public static <T> Promise<T> all(Iterable<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
//    Status allPromiisesStatus = PENDING;
//    final Iterator<Promise<?>> promiseIterator = promises.iterator();
//    while (allPromiisesStatus == PENDING) {
//      if (promiseIterator.hasNext()) {
//        promiseIterator.next().
//      }
//    }
//    promises.forEach();
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
        PromiseSupport.resolve(func.apply(src.get()));
      } catch (Exception exception) {
        if (onReject != null) {
          src.catchError(onReject);
        } else {
          PromiseSupport.reject(exception);
        }
      }
    }
  }
}