package dev.lightfoot.client.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController {

    private static final int WRITE_WINDOW_CONCURRENCY = 1;

    private static final MediaType MEDIA_TYPE = MediaType.APPLICATION_NDJSON;

    @NonNull
    private final WebClient webClient;

    /**
     * This function tests the getAndConsume method with incrementally increasing values. For my machine I see
     * repeatable infinite hangs with counts above 3000, such that the connection is indefinitely stuck, with no data
     * flowing.
     * Once the infinite hang threshold is known, /getAndConsume can be called directly for debugging.
     */
    @GetMapping("/probeGetAndConsume")
    public Flux<Data> probeGetAndConsume() {
        return Flux.range(1, 100)
                .concatMap(i -> {
                    int count = i * 500;
                    return getAndConsume(count);
                });
    }

    @GetMapping("/getAndConsume/{count}")
    public Flux<Data> getAndConsume(@PathVariable int count) {
        log.info("/getAndConsume {}", count);
        AtomicInteger windowCounter = new AtomicInteger();

        return getData(count)
                .transform(datas -> datas.window(50)
                        .flatMap(window -> {
                            int windowIdx = windowCounter.incrementAndGet();
                            log.info("Processing window {}", windowIdx);
                            return writeData(window)
                                    .doOnComplete(() -> log.info("Processed window {}", windowIdx));
                        }, WRITE_WINDOW_CONCURRENCY));

    }

    private Flux<Data> getData(int count) {
        return webClient.get()
                .uri("/get/" + count)
                .accept(MEDIA_TYPE)
                .retrieve()
                .bodyToFlux(Data.class);
    }

    private Flux<Data> writeData(Flux<Data> datas) {
        return webClient.post()
                .uri("/write")
                .accept(MEDIA_TYPE)
                .contentType(MEDIA_TYPE)
                .body(BodyInserters.fromPublisher(datas, Data.class))
                .retrieve()
                .bodyToFlux(Data.class);
    }

}
