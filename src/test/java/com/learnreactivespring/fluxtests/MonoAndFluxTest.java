package com.learnreactivespring.fluxtests;


import org.junit.Test;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class MonoAndFluxTest {
    List<String> names = Arrays.asList("surendra", "meena","asdsad","sdfsfs","sadada");

    @Test
    public void fluxTest(){
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot")
                .concatWith(Flux.error(new RuntimeException("runtime error")));

        StepVerifier.create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring boot")
                .expectError(RuntimeException.class)
                .verify();
        Flux<String> namesFlux = Flux.fromIterable(names).log();

        StepVerifier.create(namesFlux).expectNextCount(2).verifyComplete();
    }

    public List convertToList(String s){
        return Arrays.asList(s,"ram");
    }

    @Test
    public void fluxFlatMapParallelTest(){
        Flux<String> namesFlux = Flux.fromIterable(names);

        Flux<String> flux1 =
                                namesFlux
                                .window(2)
                                .flatMap((p) -> p.map(this::convertToList).subscribeOn(parallel()))
                                        .flatMap(s->Flux.fromIterable(s))

                                .log();



//        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A","B","C","D","E","F")) // Flux<String>
//                .window(2) //Flux<Flux<String> -> (A,B), (C,D), (E,F)
//                .flatMap((s) ->
//                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<List<String>
//                .flatMap(s -> Flux.fromIterable(s)) //Flux<String>
//                .log();
        flux1.subscribe(System.out::println);


    }
    @Test
    public void tranformUsingFlatMap_usingparallel(){

        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A","B","C","D","E","F")) // Flux<String>
                .window(2) //Flux<Flux<String> -> (A,B), (C,D), (E,F)
                .flatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<List<String>
                .flatMap(s -> Flux.fromIterable(s)) //Flux<String>
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }
    @Test
    public void MergeFlux(){
        Flux<String> flux1 = Flux.just("A","B","N");
        Flux<String> flux2 = Flux.just("C","D","M");

        Flux<String> stringFlux = Flux.zip(flux1,flux2, (t1,t2)->{
            return t1.concat(t2);
        });

        System.out.println(stringFlux);

        StepVerifier.create(stringFlux.log()).expectSubscription().expectNextCount(3).verifyComplete();

    }

    @Test
    public void fluxErrorHandling(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("Exception Occurred")))
                .concatWith(Flux.just("D"))
                .onErrorMap((e)->new CustomException(e))
                .retry(1);

        StepVerifier.create(stringFlux.log( ))
                .expectSubscription()
                .expectNextCount(6)
                .expectError(CustomException.class)
//                .verify();
//        .expectNextCount(1)
                .verify();
    }

    //getting data using backpressure hook
    @Test
    public void customized_backPressure(){

        Flux<Integer> integerFlux = Flux.range(1,10).log();

        integerFlux.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnNext(Integer value) {
//                request(1);
                System.out.println("value:-      "+value);
//                if(value==4){
//                    cancel();
//                }
            }
        });

    }

    //getting data from publisher on request method through subscription - testing with project reactor's stepverification method
    @Test
    public void customized_cancel(){

        Flux<Integer> integerFlux = Flux.range(1,10).log();

        StepVerifier.create(integerFlux)
                .expectSubscription()
                .thenRequest(1)
                .expectNext(1)
                .thenRequest(2)
                .expectNext(2,3)
                .thenCancel()
                .verify();

    }

    //getting bounded data by giving subscriber custom parameters - bckpresssure
    @Test
    public void backPressure(){
        Flux<Integer> integerFlux = Flux.range(1,10).log();

        integerFlux.subscribe((element)->System.out.println(element),(e)->System.out.println(e),()->System.out.println("Done"),subscription -> {subscription.request(3);});
    }

    @Test
    public void hotReactiveStream() throws InterruptedException {
        Flux<Integer> integerFlux = Flux.range(1,10).delayElements(Duration.ofSeconds(1)).log();
        ConnectableFlux<Integer> connectableFlux = integerFlux.publish();
        connectableFlux.connect();
        connectableFlux.subscribe(elements->System.out.println("subscriober1 --  "+elements));
        Thread.sleep(3000);
        connectableFlux.subscribe(elements->System.out.println("subscriober2 --  "+elements));
        Thread.sleep(4000);
    }

    @Test
    public void virtualTimerForTests(){

        VirtualTimeScheduler.getOrSet();

        Flux<Long> integerFlux = Flux.interval(Duration.ofSeconds(1)).take(3);

        StepVerifier.withVirtualTime(() -> integerFlux.log())
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(3))
                .expectNextCount(3)
                .verifyComplete();


    }

}
