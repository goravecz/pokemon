package com.accenture.pokemon.config;

import com.accenture.pokemon.client.PokeApiErrorHandler;
import com.accenture.pokemon.client.PokeApiRetryListener;
import com.accenture.pokemon.exception.RetryablePokeApiException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
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
            PokeApiErrorHandler errorHandler) {
        return builder
                .connectTimeout(Duration.ofMillis(properties.timeout().connectMs()))
                .readTimeout(Duration.ofMillis(properties.timeout().readMs()))
                .errorHandler(errorHandler)
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
                        Map.of(RetryablePokeApiException.class, true),
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
