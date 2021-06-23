import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;


public class Main {

    private static Flux<String> first;

    public static void main(String[] args) throws InterruptedException {

        Mono.empty();
        Mono<Object> mono = Mono.empty();

        Flux.empty();
        Flux<Object> flux = Flux.empty();

        Mono<Integer> just = Mono.just(1);
        just.block();

        Flux<Integer> just1 = Flux.just(1, 2, 3);

        Flux<Object> flux1 = mono.flux();

        Mono<Boolean> monoFromFlux = flux.any(s -> s.equals(1));

        System.out.println(Flux.range(1, 10)); // Создаёт последовательность

 //       Flux.range(1, 10).subscribe(System.out::println);

 //       Flux.fromIterable(Arrays.asList(1, 2, 3)).subscribe(System.out::println); // последовательность

        Flux.<String>generate(synchronousSink -> {
            synchronousSink.next("Hello!");
        })
//                .delayElements(Duration.ofMillis(500))
                .take(10);
 //               .subscribe(System.out::println);

        Flux<Object> messageProducer = Flux
                .generate(
                        () -> 2354,
                        (state, sink) -> {
                            if (state > 2361) {
                                sink.complete();
                            } else {
                                sink.next("Step: " + state);
                            }
                            return state;
                        });
//        messageProducer.subscribe(System.out::println);

        Flux.create(sink ->
                messageProducer.subscribe(new BaseSubscriber<Object>() {

            @Override
            protected void hookOnNext(Object value) {
                sink.next(value);
            }

            @Override
            protected void hookOnComplete() {
                sink.complete();
            }
        }));
//                .subscribe(System.out::println);

        Flux.create(fluxSink -> fluxSink.onRequest(r -> {
            fluxSink.next(("DB returns: " + messageProducer.blockFirst()));
        }))
                .subscribe(System.out::println);

        Flux<String> first = Flux
                .just("Hello", "World")
                .repeat();

        Flux<String> second = Flux
                .just("Java", "Stream", "API", "Project", "Reactor")
                .zipWith(first, (f, s) -> String.format("%s %s", f, s));

        Flux<String> stringFlux = second.delayElements(Duration.ofMillis(1300))
                .timeout(Duration.ofSeconds(1))
//                .retry(3)
                //               .onErrorReturn("Too slow...")
//                .onErrorResume(throwable -> {
//                    return Flux.just("One", "message", "error");
//                })
                .onErrorResume(throwable -> Flux
                        .interval(Duration.ofMillis(100))
                        .map(String::valueOf))
                .skip(2)
                .take(4, true);

        stringFlux.subscribe(
                v -> System.out.println(v),
                s -> System.err.println(s),
                () -> System.out.println("finished...")
        );
        Thread.sleep(5000L);
    }
}
