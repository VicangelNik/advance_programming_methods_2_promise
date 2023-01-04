package org.vicangel;

import java.util.function.Consumer;

import org.vicangel.promise.Promise;
import org.vicangel.promise.PromiseExecutor;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class Main {

  public static void main(String[] args) {
    System.out.println("Hello world!");
    final Consumer<String> resolve = s -> System.out.println("Resolved");
    final Consumer<String> reject = s -> System.out.println("Rejected");
    PromiseExecutor<String> promiseExecutor = (res, rej) -> new RuntimeException();
    Promise<String> promise = new Promise<>(promiseExecutor);
    //  promise.resolve("Resolved");
    // promise.reject(new RuntimeException());
     // promise.resolve(new RuntimeException());
    // promise.resolve(null);
    // promise.reject(null);
     // promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length);
    promise.resolve("Resolved").then(String::length, throwable -> new RuntimeException()).andFinally(System.out::println);
  }
}