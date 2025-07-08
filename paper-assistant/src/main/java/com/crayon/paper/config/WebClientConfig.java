package com.crayon.paper.config;

import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * webClient配置
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/3
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeaders(headers -> {
                    headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    headers.set(HttpHeaders.ACCEPT, "application/atom+xml, application/xml, text/xml");
                })
                .filter((request, next) -> next.exchange(request)
                        .timeout(Duration.ofSeconds(30)))
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.debug("请求: {} {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    log.debug("响应状态: {}", clientResponse.statusCode());
                    return Mono.just(clientResponse);
                }))
                .build();
    }
}
