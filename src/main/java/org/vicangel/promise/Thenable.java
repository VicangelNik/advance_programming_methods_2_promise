package org.vicangel.promise;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface to simulate Javascript's thenables
 *
 * @author Nikiforos Xylogiannopoulos
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise#thenables">...</a>
 */
public interface Thenable<V> {

  <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject);

  <T> Promise<T> then(Function<V, T> onResolve);
}
