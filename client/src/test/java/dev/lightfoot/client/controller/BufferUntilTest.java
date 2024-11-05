package dev.lightfoot.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
class BufferUntilTest {

    @Test
    void bufferUntil() {
        log.info("Start");
        Flux<List<List<Integer>>> producer = Flux.range(0, 5)
                .flatMap(i -> Mono.fromCallable(() -> {
                            Thread.sleep(200);
                            return i;
                        })
                        .subscribeOn(Schedulers.boundedElastic()))
                .buffer()
                .bufferUntil(i -> {
                    log.info("Output thread {}", Thread.currentThread().getName());
                    return false;
                });

        producer.blockLast();
    }


}