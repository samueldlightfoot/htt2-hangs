package dev.lightfoot.sleuthproxysample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class ServerController {

    @GetMapping("/get/{count}")
    public Flux<Data> get(@PathVariable int count) {
        List<Data> datas = IntStream.range(0, count)
                .mapToObj(i -> new Data(i + "some"))
                .toList();

        return Flux.fromIterable(datas);
    }

    @PostMapping("/write")
    public Flux<Data> write(@RequestBody final Flux<Data> data) {
        log.info("Received /write request");

        return data.map(Data::mutate);
    }

}
