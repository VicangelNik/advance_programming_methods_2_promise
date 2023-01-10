package org.vicangel.promise;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class PromiseTransformActionThread<V, T> extends Thread {

  private static final Logger LOGGER = Logger.getLogger(Thread.currentThread().getName());
  private final Promise<V> src;
  private final Promise<T> dest;
  private final Function<V, T> func;
  private Consumer<Throwable> onReject;

  public PromiseTransformActionThread(Promise<V> src, Promise<T> dest,  Function<V, T> func) {
    this.src = src;
    this.func = func;
    this.dest = dest;
    this.start();
  }

  public PromiseTransformActionThread(Promise<V> src, Function<V, T> func, Consumer<Throwable> onReject) {
    this.src = src;
    this.dest = null;
    this.func = func;
    this.onReject = onReject;
  }

  @Override
  public void run() {
    LOGGER.info("Execution of thread with name: " + Thread.currentThread().getName());
    try {
      dest.fullFillResolve(func.apply(src.get()));
    } catch (Exception exception) {
      if (onReject != null) {
        src.catchError(onReject);
      } else {
        PromiseSupport.reject(exception);
      }
    }
  }
}
