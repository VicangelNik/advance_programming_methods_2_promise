package org.vicangel;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.vicangel.promise.Promise;
import org.vicangel.promise.PromiseExecutor;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
  private static final PromiseExecutor<Integer> PROMISE_EXECUTOR = (res, rej) -> LOGGER.info("Execution Started");

  public static void main(String[] args) throws ExecutionException {
//    final Consumer<String> resolve = s -> System.out.println("Resolved");
//    final Consumer<String> reject = s -> System.out.println("Rejected");

    Promise<Integer> promise = new Promise<>(PROMISE_EXECUTOR);
    //promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length).andFinally(valueOrError -> System.out.println(10+ (Integer)valueOrError.value()));
    // promise.resolve("Resolved").then(String::length, x -> new RuntimeException("Error exception from then")).andFinally(valueOrError -> System.out.println(10 + (Integer) valueOrError.value()));
    promise.resolve("Resolved").then(str -> str.length() / 0, x -> {
      throw new RuntimeException("Error exception from then");
    }).andFinally(valueOrError -> System.out.println(10 + (Integer) valueOrError.value()));
    System.out.println(promise.get());
    // promise.reject(new RuntimeException());
    // promise.resolve(new RuntimeException());
    // promise.resolve(null);
    // promise.reject(null);
    // promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length, throwable -> new RuntimeException()).andFinally(System.out::println);
  }
}