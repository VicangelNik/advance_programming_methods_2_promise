package org.vicangel.promise;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public abstract class PromiseSupport {

  private static final Logger LOGGER = Logger.getLogger(PromiseSupport.class.getName());

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
    LOGGER.log(Level.INFO, "Promise ran with value: {0}", value);
    if (value instanceof Promise) {
      return (Promise<T>) value;
    }
    return new Promise<>((res, rej) -> {
      try {
        LOGGER.info("Resolved Accept default case entered");
        res.accept(value);
      } catch (Exception throwable) {
        LOGGER.info("Resolved exception default case entered");
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
  public static Promise<Throwable> reject(final Throwable reason) {
    return new Promise<>((res, rej) -> rej.accept(reason));
  }
}
