package org.vicangel.helpers;

import java.util.function.Consumer;

import org.vicangel.exceptions.PromiseRejectException;

/**
 * @author Nikiforos Xylogiannopoulos
 * @see <a href="https://www.baeldung.com/java-lambda-exceptions">...</a>
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

  void accept(T t) throws E;

  static <T> Consumer<T> throwingConsumerWrapper(
    ThrowingConsumer<T, Exception> throwingConsumer) {

    return i -> {
      try {
        throwingConsumer.accept(i);
      } catch (Exception ex) {
        throw new PromiseRejectException(ex);
      }
    };
  }
}
