package org.vicangel.exceptions;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class PromiseRejectException extends RuntimeException {

  public PromiseRejectException(Throwable cause) {
    super(cause);
  }

  public static Throwable getInitCause(final Throwable cause) {
    if (cause.getCause() != null) {
      return getInitCause(cause.getCause());
    }
    return cause;
  }
}
