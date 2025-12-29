package com.accenture.pokemon.config;

import com.accenture.pokemon.client.PokeApiErrorHandler;
import com.accenture.pokemon.client.PokeApiLoggingInterceptor;
import com.accenture.pokemon.client.PokeApiRetryListener;
import com.accenture.pokemon.exception.RetryablePokeApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(PokeApiProperties.class)
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            PokeApiProperties properties,
            PokeApiErrorHandler errorHandler,
            PokeApiLoggingInterceptor loggingInterceptor) {
        Logger clientLogger = LoggerFactory.getLogger("com.accenture.pokemon.client");
        return builder
                .requestFactory(() -> clientLogger.isDebugEnabled()
                        ? new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
                        : new SimpleClientHttpRequestFactory())
                .connectTimeout(Duration.ofMillis(properties.timeout().connectMs()))
                .readTimeout(Duration.ofMillis(properties.timeout().readMs()))
                .errorHandler(errorHandler)
                .interceptors(loggingInterceptor)
                .build();
    }

    @Bean
    public RetryTemplate retryTemplate(
            PokeApiProperties properties,
            PokeApiRetryListener pokeApiRetryListener) {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(pokeApiRetryListener);

        RetryConfig retry = properties.retry();
        retryTemplate.setRetryPolicy(
                new SimpleRetryPolicy(
                        retry.maxAttempts(),
                        Map.of(
                                RetryablePokeApiException.class, true,
                                ResourceAccessException.class, true
                        ),
                        true
                )
        );

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.initialBackoffMs());
        backOffPolicy.setMultiplier(retry.multiplier());
        backOffPolicy.setMaxInterval(retry.maxInterval());

        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
