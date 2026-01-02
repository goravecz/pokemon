package com.accenture.pokemon.config;

import com.accenture.pokemon.client.PokeApiLoggingInterceptor;
import com.accenture.pokemon.client.PokeApiRetryListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(PokeApiProperties.class)
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            PokeApiProperties properties,
            PokeApiLoggingInterceptor loggingInterceptor,
            ClientHttpRequestFactory requestFactory) {
        return builder
                .requestFactory(() -> requestFactory)
                .connectTimeout(Duration.ofMillis(properties.timeout().connectMs()))
                .readTimeout(Duration.ofMillis(properties.timeout().readMs()))
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
        retryTemplate.setRetryPolicy(new PokeApiRetryPolicy(retry.maxAttempts()));

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.initialBackoffMs());
        backOffPolicy.setMultiplier(retry.multiplier());
        backOffPolicy.setMaxInterval(retry.maxInterval());

        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    @ConditionalOnExpression("'${logging.level.com.accenture.pokemon.client:INFO}'.equalsIgnoreCase('DEBUG')")
    public ClientHttpRequestFactory bufferingRequestFactory() {
        return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    @ConditionalOnExpression("!'${logging.level.com.accenture.pokemon.client:INFO}'.equalsIgnoreCase('DEBUG')")
    public ClientHttpRequestFactory simpleRequestFactory() {
        return new SimpleClientHttpRequestFactory();
    }
}
