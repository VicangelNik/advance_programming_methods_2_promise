package org.vicangel.promise;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public interface ValueOrError<V> {

  default boolean hasError() {
    return error() == null;
  }

  V value();

  Throwable error();

  class Value<V> implements ValueOrError<V> {

    private final V value;

    private Value(V value) {
      this.value = value;
    }

    @Override
    public V value() {
      return value;
    }

    @Override
    public Throwable error() {
      return null;
    }

    static <T> ValueOrError<T> of(T t) {
      return new Value<>(t);
    }
  }

  class Error<V> implements ValueOrError<V> {

    private final Throwable throwable;

    private Error(Throwable throwable) {
      this.throwable = throwable;
    }

    @Override
    public V value() {
      return null;
    }

    @Override
    public Throwable error() {
      return throwable;
    }

    static <T> ValueOrError<T> of(Throwable t) {
      return new Error<>(t);
    }
  }

  class Factory {

    private Factory() {
    }

    public static <T> ValueOrError<T> ofValue(T t) {
      return new ValueOrError<>() {
        @Override
        public T value() {
          return t;
        }

        @Override
        public Throwable error() {
          return null;
        }
      };
    }

    public static ValueOrError<Void> ofError(Throwable t) {
      return new ValueOrError<>() {
        @Override
        public Void value() {
          return null;
        }

        @Override
        public Throwable error() {
          return t;
        }
      };
    }
  }
}

