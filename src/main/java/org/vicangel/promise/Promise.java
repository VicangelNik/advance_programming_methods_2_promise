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
 * "What I cannot create, I do not understand"
 * Richard Feynman
 * @implSpec <p>
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
 * @see <a href="https://en.wikipedia.org/wiki/Richard_Feynman">...</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise">...</a>
 * <p>
 * A helpful guide to help you understand Promises is available here:
 * @see <a href="https://javascript.info/async">...</a>
 */
public class Promise<V> extends PromiseSupport implements Thenable<V> {

  private static final Logger LOGGER = Logger.getLogger(Promise.class.getName());
  private final Object lock;
  private volatile Status status = PENDING;
  private ValueOrError<V> valueOrError;

  // No instance fields are defined, perhaps you should add some!

  protected Promise(PromiseExecutor<V> executor) {
    super();
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
  public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) { // TODO
    synchronized (lock) {
      LOGGER.log(Level.INFO, "then(Function<V, T> onResolve, Consumer<Throwable> onReject) execution");
      new PromiseTransformActionThread<>(this, onResolve, onReject);
      lock.notifyAll();
      return (Promise<ValueOrError<T>>) this;
    }
  }

  public <T> Promise<T> then(Function<V, T> onResolve) { // TODO
    synchronized (lock) {
      LOGGER.log(Level.INFO, "then(Function<V, T> onResolve) execution");
      this.status = PENDING;
      new PromiseTransformActionThread<>(this, onResolve);
      lock.notifyAll();
      LOGGER.log(Level.INFO, () -> "then(Function<V, T> onResolve) called with thread name " + Thread.currentThread().getName());
      return (Promise<T>) this;
    }
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
  public <T> Promise<Throwable> catchError(final Consumer<Throwable> onRejected) { // TODO
    onRejected.accept(this.valueOrError.error());
    synchronized (lock) {
      lock.notifyAll();
    }
    final Function<V, T> catchErrorFunction = onRej -> (T) this;
    LOGGER.log(Level.INFO, "catchError execution");
    return (Promise<Throwable>) then(catchErrorFunction);
  }

  /**
   * The finally() method of a Promise object schedules a function to be called when the promise is settled
   * (either fulfilled or rejected). It immediately returns an equivalent Promise object, allowing you to chain calls to
   * other promise methods.
   * <p>
   * This lets you avoid duplicating code in both the promise's then() and catch() handlers.
   *
   * @param onFinally A Function called when the Promise is settled. This handler receives no parameters.
   *
   * @return Returns an equivalent Promise. If the handler throws an error or returns a rejected promise,
   * the promise returned by finally() will be rejected with that value instead. Otherwise, the return value
   * of the handler does not affect the state of the original promise.
   *
   * @implNote finally is a reserved word in Java.
   * @apiNote The finally() method can be useful if you want to do some processing or cleanup once the promise is settled,
   * regardless of its outcome.
   * <p>
   * The finally() method is very similar to calling then(onFinally, onFinally). However, there are a couple of differences:
   * <p>
   * When creating a function inline, you can pass it once, instead of being forced to either declare it twice, or create
   * a variable for it.
   * The onFinally callback does not receive any argument. This use case is for precisely when you do not care about the
   * rejection reason or the fulfillment value, and so there's no need to provide it.
   * A finally() call is usually transparent and does not change the eventual state of the original promise. So for example:
   * Unlike Promise.resolve(2).then(() => 77, () => {}), which returns a promise eventually fulfilled with the value 77,
   * Promise.resolve(2).finally(() => 77) returns a promise eventually fulfilled with the value 2.
   * Similarly, unlike Promise.reject(3).then(() => {}, () => 88), which returns a promise eventually fulfilled with the
   * value 88, Promise.reject(3).finally(() => 88) returns a promise eventually rejected with the reason 3.
   * <p>
   * Note: A throw (or returning a rejected promise) in the finally() callback still rejects the returned promise.
   * For example, both Promise.reject(3).finally(() => { throw 99; })
   * and Promise.reject(3).finally(() => Promise.reject(99)) reject the returned promise with the reason 99.
   * <p>
   * Like catch(), finally() internally calls the then method on the object upon which it was called.
   * If onFinally is not a function, then() is called with onFinally as both arguments — which,
   * for Promise.prototype.then(), means that no useful handler is attached. Otherwise, then() is called with two
   * internally created functions, which behave like the following:
   * promise.then(
   * (value) => Promise.resolve(onFinally()).then(() => value),
   * (reason) =>
   * Promise.resolve(onFinally()).then(() => {
   * throw reason;
   * }),
   * );
   * <p>
   * Because finally() calls then(), it supports subclassing. Moreover, notice the Promise.resolve() call above — in reality,
   * onFinally()'s return value is resolved using the same algorithm as Promise.resolve(),
   * but the actual constructor used to construct the resolved promise will be the subclass.
   * finally() gets this constructor through promise.constructor[@@species].
   */
  public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onFinally) {
    synchronized (lock) {
      lock.notifyAll();
    }

    LOGGER.log(Level.INFO, "andFinally execution");
    final Function<V, T> catchErrorFunction = onFin -> {
      onFinally.accept((ValueOrError<T>) this.valueOrError);
      return (T) this;
    };
    return (Promise<ValueOrError<T>>) then(catchErrorFunction);
  }

  private <T> void fullFillResolve(T value) {
    synchronized (lock) {
      this.valueOrError = (ValueOrError<V>) ValueOrError.Value.of(value);
      this.status = FULFILLED;
      LOGGER.log(Level.INFO, () -> "fullFillResolve called with value: " + value + " and thread name "
                                   + Thread.currentThread().getName());
      lock.notifyAll();
    }
  }

  private void fullFillReject(Throwable reason) {
    synchronized (lock) {
      this.valueOrError = ValueOrError.Error.of(reason);
      this.status = REJECTED;
      LOGGER.log(Level.INFO, () -> "fullFillReject called with value: " + reason + " and thread name "
                                   + Thread.currentThread().getName());
      lock.notifyAll();
    }
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
}