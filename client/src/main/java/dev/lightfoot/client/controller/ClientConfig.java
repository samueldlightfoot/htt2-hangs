package dev.lightfoot.client.controller;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.Http2AllocationStrategy;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Slf4j
@Configuration
public class ClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://localhost:9990")
                .build();
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return builder -> builder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.builder("custom")
                                .allocationStrategy(Http2AllocationStrategy.builder()
                                        .minConnections(1)
                                        .maxConnections(1)
                                        .build())
                                .build())
                        .responseTimeout(Duration.ofMillis(5000))
                        .protocol(HttpProtocol.H2C)));
    }

}
