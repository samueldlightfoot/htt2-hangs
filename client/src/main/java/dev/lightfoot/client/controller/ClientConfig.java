package dev.lightfoot.client.controller;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
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
import reactor.netty.tcp.SslProvider;

import java.io.File;
import java.time.Duration;

@Slf4j
@Configuration
public class ClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("https://localhost:9990")
                .build();
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return builder -> builder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.builder("custom")
                                .allocationStrategy(Http2AllocationStrategy.builder()
                                        .minConnections(2)
                                        .maxConnections(2)
                                        .build())
                                .build())
                        //.wiretap(true)
                        .responseTimeout(Duration.ofMillis(5000))
                        .secure(spec -> spec.sslContext(
                                SslContextBuilder.forClient()
                                        .protocols("TLSv1.3")
                                        .trustManager(new File("/Users/samlightfoot/ssl/server.crt"))  // Trust the self-signed server certificate
                        ))
                        .keepAlive(true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .protocol(HttpProtocol.H2)));
    }

}
