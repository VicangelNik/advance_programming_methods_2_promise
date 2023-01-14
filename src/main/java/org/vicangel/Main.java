package org.vicangel;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.vicangel.promise.Promise;
import org.vicangel.promise.PromiseExecutor;
import org.vicangel.promise.PromiseSupport;
import org.vicangel.promise.ValueOrError;

/**
 * @author Nikiforos Xylogiannopoulos
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
  private static final PromiseExecutor<Integer> PROMISE_EXECUTOR = (res, rej) -> LOGGER.info("Execution Started");

  public static void main(String[] args) throws ExecutionException {
//    final Consumer<String> resolve = s -> System.out.println("Resolved");
//    final Consumer<String> reject = s -> System.out.println("Rejected");

    //   Promise<Integer> promise = new Promise<>(PROMISE_EXECUTOR); // compilation error
    // Promise<Integer> promise = PromiseSupport.resolve(5);
    // Promise<String> promise1 = PromiseSupport.resolve("Just the beginning");
    // Promise<Promise<Integer>> promise2 = PromiseSupport.resolve(promise);

    // Promise<?> throwablePromise = PromiseSupport.reject(new IllegalArgumentException("illegal exception thrown")).catchError(error-> System.out.println(error.getMessage()));
    //  Promise<?> throwablePromise = PromiseSupport.reject(throwablePromise).catchError(error-> System.out.println(error.getMessage())); it is supposed to be supported by specification, but it is a compilation error

    // Promise<?> promise = PromiseSupport.resolve("Resolved").then(String::length).then(length-> length + 10);

//    Promise<?> promise = PromiseSupport.resolve("Resolved").then((x)->"Resolved3").then((x)->"Resolved7")
//      .then(x-> {System.out.println(x); return x;}); // DONE

//    Promise<?> promise = PromiseSupport.resolve("Resolved").then((x) -> "Resolved3", x -> new RuntimeException("error"))
//      .then(x -> {
//        System.out.println(x);
//        return x;
//      });

    //  Promise<?> promise = PromiseSupport.resolve("Resolved").andFinally(x-> System.out.println("Experiment completed"));

   // testPromiseSupportAll();

    testPromiseSupportAllSettle();

    //promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length).andFinally(valueOrError -> System.out.println(10+ (Integer)valueOrError.value()));
    // promise.resolve("Resolved").then(String::length, x -> new RuntimeException("Error exception from then")).andFinally(valueOrError -> System.out.println(10 + (Integer) valueOrError.value()));
//    PromiseSupport.resolve("Resolved").then(str -> str.length() / 0, x -> {
//      throw new RuntimeException("Error exception");
//    }).andFinally(valueOrError -> System.out.println("release resources")); // done
    //   System.out.println(promise.get());
    // promise.reject(new RuntimeException());
    // promise.resolve(new RuntimeException());
    // promise.resolve(null);
    // promise.reject(null);
    // promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length);
    // promise.resolve("Resolved").then(String::length, throwable -> new RuntimeException()).andFinally(System.out::println);
  }

  private static void testPromiseSupportAll() throws ExecutionException {
    Promise<Integer> promise = PromiseSupport.resolve(5);
    Promise<Integer> promise1 = PromiseSupport.resolve(10);
    Promise<Integer> promise2 = PromiseSupport.resolve(20);
    Promise<Integer> promise3 = PromiseSupport.resolve(100);
    Promise<Void> promise4 = PromiseSupport.reject(new IllegalArgumentException("700"));
    Promise<List<?>> promiseResult = PromiseSupport.all(List.of(promise, promise1, promise4, promise2, promise3));
    System.out.println(promiseResult.get());
  }

  private static void testPromiseSupportAllSettle() throws ExecutionException {
    Promise<Integer> promise = PromiseSupport.resolve(5);
    Promise<Integer> promise1 = PromiseSupport.resolve(10);
    Promise<Integer> promise2 = PromiseSupport.resolve(20);
    Promise<Integer> promise3 = PromiseSupport.resolve(100);
    Promise<Void> promise4 = PromiseSupport.reject(new IllegalArgumentException("700"));
    Promise<List<ValueOrError<?>>> promiseResult = PromiseSupport.allSettled(List.of(promise, promise1, promise4, promise2, promise3));
    System.out.println(promiseResult.get());
  }
}