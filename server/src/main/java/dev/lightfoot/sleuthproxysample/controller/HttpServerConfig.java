package dev.lightfoot.sleuthproxysample.controller;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.HttpProtocol;

import javax.net.ssl.SSLException;
import java.io.File;

@Configuration
public class HttpServerConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customizer() {
        return c -> c.addServerCustomizers(cs -> cs
                .secure(spec -> {
                    try {
                        spec.sslContext(SslContextBuilder
                                .forServer(new File("/Users/samlightfoot/ssl/server.crt"), new File("/Users/samlightfoot/ssl/server.key"))
                                .protocols("TLSv1.3"));  // Ensure TLS 1.2+ for HTTP/2
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .protocol(HttpProtocol.H2));
    }

}
