package org.vicangel.exceptions;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class PromiseRejectException extends RuntimeException{

  public PromiseRejectException() {
    super();
  }

  public PromiseRejectException(final String message) {
    super(message);
  }
}
