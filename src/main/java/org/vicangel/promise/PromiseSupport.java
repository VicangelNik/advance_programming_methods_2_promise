package org.vicangel.promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vicangel.exceptions.PromiseRejectException;

import static org.vicangel.helpers.ThrowingConsumer.throwingConsumerWrapper;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public abstract class PromiseSupport {

  private static final Logger LOGGER = Logger.getLogger(PromiseSupport.class.getName());

  protected PromiseSupport() {

  }

  /**
   * The Promise.resolve() static method "resolves" a given value to a Promise. If the value is a promise,
   * that promise is returned; if the value is a thenable, Promise.resolve() will call the then() method
   * with two callbacks it prepared; otherwise the returned promise will be fulfilled with the value.
   * <p>
   * This function flattens nested layers of promise-like objects (e.g. a promise that fulfills to a promise
   * that fulfills to something) into a single layer — a promise that fulfills to a non-thenable value.
   *
   * @param value Argument to be resolved by this Promise. Can also be a Promise or a thenable to resolve.
   *
   * @return A Promise that is resolved with the given value, or the promise passed as value, if the value was a promise object.
   * A resolved promise can be in any of the states — fulfilled, rejected, or pending.
   * For example, resolving a rejected promise will still result in a rejected promise.
   *
   * @apiNote Promise.resolve() resolves a promise, which is not the same as fulfilling or rejecting the promise.
   * See Promise description for definitions of the terminology.
   * In brief, Promise.resolve() returns a promise whose eventual state depends on another promise, thenable object, or other value.
   * <p>
   * Promise.resolve() is generic and supports subclassing, which means it can be called on subclasses of Promise,
   * and the result will be a promise of the subclass type. To do so, the subclass's constructor must implement the same
   * signature as the Promise() constructor — accepting a single executor function that can be called with the resolve
   * and reject callbacks as parameters.
   * <p>
   * Promise.resolve() special-cases native Promise instances. If value belongs to Promise or a subclass,
   * and value.constructor === Promise, then value is directly returned by Promise.resolve(), without creating
   * a new Promise instance. Otherwise, Promise.resolve() is essentially a shorthand for new Promise((resolve) => resolve(value)).
   * <p>
   * The bulk of the resolving logic is actually implemented by the resolver function passed by the Promise() constructor.
   * In summary:
   * If a non-thenable value is passed, the returned promise is already fulfilled with that value.
   * If a thenable is passed, the returned promise will adopt the state of that thenable by calling the then method and
   * passing a pair of resolving functions as arguments. (But because native promises directly pass through
   * Promise.resolve() without creating a wrapper, the then method is not called on native promises.)
   * If the resolver function receives another thenable object, it will be resolved agin, so that the eventual
   * fulfillment value of the promise will never be thenable.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/resolve">...</a>
   */
  public static <T> Promise<T> resolve(final T value) {
    LOGGER.log(Level.INFO, "static resolve called with value: {0}", value);
    if (value instanceof Promise) {
      LOGGER.log(Level.INFO, "static resolve called, immediately return promise");
      return (Promise<T>) value;
    }
    return new Promise<>((res, rej) -> {
      try {
        LOGGER.log(Level.INFO, "static resolve called - ACCEPT value");
        res.accept(value);
      } catch (Exception throwable) {
        LOGGER.log(Level.INFO, "static resolve called - REJECT value");
        rej.accept(throwable);
      }
    });
  }

  /**
   * The Promise.reject() static method returns a Promise object that is rejected with a given reason.
   *
   * @param reason Reason why this Promise rejected.
   *
   * @return A Promise that is rejected with the given reason.
   *
   * @apiNote The static Promise.reject() function returns a Promise that is rejected.
   * For debugging purposes and selective error catching, it is useful to make reason an instanceof Error.
   * <p>
   * Promise.reject() is generic and supports subclassing, which means it can be called on subclasses of Promise,
   * and the result will be a promise of the subclass type. To do so, the subclass's constructor must implement the same
   * signature as the Promise() constructor — accepting a single executor function that can be called with the resolve
   * and reject callbacks as parameters. Promise.reject() is essentially a shorthand for
   * new Promise((resolve, reject) => reject(reason)).
   * <p>
   * Unlike Promise.resolve(), Promise.reject() always wraps reason in a new Promise object, even when reason is already a Promise.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/reject">...</a>
   */
  public static Promise<Void> reject(final Throwable reason) {
    return new Promise<>((res, rej) -> rej.accept(reason));
  }

  /**
   * The Promise.all() static method takes an iterable of promises as input and returns a single Promise.
   * This returned promise fulfills when all the input's promises fulfill (including when an empty iterable is passed),
   * with an array of the fulfillment values. It rejects when any of the input's promises rejects, with this first rejection reason.
   *
   * @param promises An iterable (such as an Array) of promises.
   *
   * @return A Promise that is:
   * <p>
   * Already fulfilled, if the iterable passed is empty.
   * Asynchronously fulfilled, when all the promises in the given iterable fulfill. The fulfillment value is an array of
   * fulfillment values, in the order of the promises passed, regardless of completion order. If the iterable passed is
   * non-empty but contains no pending promises, the returned promise is still asynchronously (instead of synchronously) fulfilled.
   * Asynchronously rejected, when any of the promises in the given iterable rejects.
   * The rejection reason is the rejection reason of the first promise that was rejected.
   *
   * @apiNote The Promise.all() method is one of the promise concurrency methods.
   * It can be useful for aggregating the results of multiple promises.
   * It is typically used when there are multiple related asynchronous tasks that the overall code relies on to work
   * successfully — all of whom we want to fulfill before the code execution continues.
   * <p>
   * Promise.all() will reject immediately upon any of the input promises rejecting. In comparison,
   * the promise returned by Promise.allSettled() will wait for all input promises to complete, regardless of whether one rejects.
   * Use allSettled() if you need the final result of every promise in the input iterable.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all">...</a>
   */
  public static Promise<List<?>> all(Iterable<Promise<?>> promises) {

    final List<Object> list = Collections.synchronizedList(new ArrayList<>());

    final Iterator<Promise<?>> promiseIterator = promises.iterator();
    try {
      promiseIterator.forEachRemaining(throwingConsumerWrapper(promise -> list.add(PromiseSupport.resolve(promise).get())));
    } catch (PromiseRejectException e) {
      list.clear();
      list.add(e.getMessage());
    }
    return PromiseSupport.resolve(list);
  }

  /**
   * The Promise.race() static method takes an iterable of promises as input and returns a single Promise.
   * This returned promise settles with the eventual state of the first promise that settles.
   *
   * @param promises An iterable (such as an Array) of promises.
   *
   * @return A Promise that asynchronously settles with the eventual state of the first promise in the iterable to settle.
   * In other words, it fulfills if the first promise to settle is fulfilled, and rejects if the first promise to settle
   * is rejected. The returned promise remains pending forever if the iterable passed is empty.
   * If the iterable passed is non-empty but contains no pending promises, the returned promise is still asynchronously
   * (instead of synchronously) settled.
   *
   * @apiNote The Promise.race() method is one of the promise concurrency methods. It's useful when you want the first
   * async task to complete, but do not care about its eventual state (i.e. it can either succeed or fail).
   * <p>
   * If the iterable contains one or more non-promise values and/or an already settled promise, then Promise.race() will
   * settle to the first of these values found in the iterable.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/race">...</a>
   */
  public static Promise<ValueOrError<?>> race(List<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  /**
   * The Promise.any() static method takes an iterable of promises as input and returns a single Promise.
   * This returned promise fulfills when any of the input's promises fulfills, with this first fulfillment value.
   * It rejects when all the input's promises reject (including when an empty iterable is passed),
   * with an AggregateError containing an array of rejection reasons.
   *
   * @param promises An iterable (such as an Array) of promises.
   *
   * @return A Promise that is:
   * <p>
   * Already rejected, if the iterable passed is empty.
   * Asynchronously fulfilled, when any of the promises in the given iterable fulfills. The fulfillment value is the
   * fulfillment value of the first promise that was fulfilled.
   * Asynchronously rejected, when all the promises in the given iterable reject.
   * The rejection reason is an AggregateError containing an array of rejection reasons in its error property.
   * The errors are in the order of the promises passed, regardless of completion order. If the iterable passed is
   * non-empty but contains no pending promises, the returned promise is still asynchronously (instead of synchronously)
   * rejected.
   *
   * @apiNote The Promise.any() method is one of the promise concurrency methods. This method is useful for returning
   * the first promise that fulfills. It short-circuits after a promise fulfills, so it does not wait for the other
   * promises to complete once it finds one.
   * <p>
   * Unlike Promise.all(), which returns an array of fulfillment values, we only get one fulfillment value
   * (assuming at least one promise fulfills). This can be beneficial if we need only one promise to fulfill,
   * but we do not care which one does.
   * Note another difference: this method rejects upon receiving an empty iterable, since, truthfully,
   * the iterable contains no items that fulfill. You may compare Promise.any() and Promise.all()
   * with Array.prototype.some() and Array.prototype.every().
   * <p>
   * Also, unlike Promise.race(), which returns the first settled value (either fulfillment or rejection),
   * this method returns the first fulfilled value.
   * This method ignores all rejected promises up until the first promise that fulfills.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/any">...</a>
   */
  public static Promise<?> any(List<Promise<?>> promises) {
    throw new UnsupportedOperationException("IMPLEMENT ME");
  }

  /**
   * The Promise.allSettled() static method takes an iterable of promises as input and returns a single Promise.
   * This returned promise fulfills when all the input's promises settle (including when an empty iterable is passed),
   * with an array of objects that describe the outcome of each promise.
   *
   * @param promises An iterable (such as an Array) of promises.
   *
   * @return A Promise that is:
   * <p>
   * Already fulfilled, if the iterable passed is empty.
   * Asynchronously fulfilled, when all promise in the given iterable have settled (either fulfilled or rejected).
   * The fulfillment value is an array of objects, each describing the outcome of one promise in the iterable,
   * in the order of the promises passed, regardless of completion order. Each outcome object has the following properties:
   * status
   * A string, either "fulfilled" or "rejected", indicating the eventual state of the promise.
   * <p>
   * value
   * Only present if status is "fulfilled". The value that the promise was fulfilled with.
   * <p>
   * reason
   * Only present if status is "rejected". The reason that the promise was rejected with.
   * <p>
   * If the iterable passed is non-empty but contains no pending promises, the returned promise is still asynchronously (instead of synchronously) fulfilled.
   *
   * @apiNote The Promise.allSettled() method is one of the promise concurrency methods.
   * Promise.allSettled() is typically used when you have multiple asynchronous tasks that are not dependent on one another
   * to complete successfully, or you'd always like to know the result of each promise.
   * <p>
   * In comparison, the Promise returned by Promise.all() may be more appropriate if the tasks are dependent on each other,
   * or if you'd like to immediately reject upon any of them rejecting.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/allSettled">...</a>
   */
  public static Promise<List<ValueOrError<?>>> allSettled(List<Promise<?>> promises) {

    final List<ValueOrError<?>> list = Collections.synchronizedList(new ArrayList<>());

    final Iterator<Promise<?>> promiseIterator = promises.iterator();
    promiseIterator.forEachRemaining(promise -> list.add(PromiseSupport.resolve(promise).getValueOrError()));
    return PromiseSupport.resolve(list);
  }
}
