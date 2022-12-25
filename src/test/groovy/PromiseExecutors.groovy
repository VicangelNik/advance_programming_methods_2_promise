import org.vicangel.promise.PromiseExecutor

import java.util.function.Consumer

/**
 * @author Nikiforos Xylogiannopoulos
 */
class PromiseExecutors {

  static PromiseExecutor<String> delayedResolve(String value, int delayMillis) {
    return new DelayedValue<String>(value: value, delayMillis: delayMillis)
  }

  static PromiseExecutor<Void> delayedReject(Throwable error, int delayMillis) {
    return new DelayedError(error: error, delayMillis: delayMillis)
  }


  private class DelayedValue<V> implements PromiseExecutor<V> {

    V value
    int delayMillis

    void execute(Consumer<V> resolve, Consumer<Throwable> reject) {
      new Thread(() -> {
        try {
          Thread.sleep(delayMillis)
          resolve.accept(value)
        }
        catch (InterruptedException ie) {
          // will never occur
          // if it occurs, the promise will remain PENDING forever
        }
      })
    }
  }

  private class DelayedError<V> implements PromiseExecutor<V> {

    Throwable error
    int delayMillis

    void execute(Consumer<V> resolve, Consumer<Throwable> reject) {
      new Thread(() -> {
        try {
          Thread.sleep(delayMillis)
          reject.accept(error)
        }
        catch (InterruptedException ie) {
          // will never occur
          // if it occurs, the promise will remain PENDING forever
        }
      })
    }
  }

}
