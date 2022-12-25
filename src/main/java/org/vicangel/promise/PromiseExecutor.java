package org.vicangel.promise;

import java.util.function.Consumer;

/**
 * @author Nikiforos Xylogiannopoulos
 */
@FunctionalInterface
public interface PromiseExecutor<V> {
  void execute(Consumer<V> resolve, Consumer<Throwable> reject);
}

