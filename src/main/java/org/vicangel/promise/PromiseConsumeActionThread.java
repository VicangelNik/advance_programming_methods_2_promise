package org.vicangel.promise;

import java.util.function.Consumer;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class PromiseConsumeActionThread<V> extends Thread {

  private final Promise<V> src;
  private final Promise<?> dest;
  private final Consumer<? super V> action;

  public PromiseConsumeActionThread(Promise<V> src, Promise<?> dest, Consumer<? super V> action) {
    this.src = src;
    this.dest = dest;
    this.action = action;
    this.start();
  }

  @Override
  public void run() {
    try {
      action.accept(src.get());
      dest.fullFillResolve(null);
    } catch (Exception throwable) {
      dest.fullFillReject(throwable.getCause());
    }
  }
}
