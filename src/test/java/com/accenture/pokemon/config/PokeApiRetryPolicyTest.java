package com.accenture.pokemon.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokeApiRetryPolicyTest {

    private RetryTemplate retryTemplate;

    @BeforeEach
    void setUp() {
        retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new PokeApiRetryPolicy(3));
    }

    @Test
    void shouldRetryOn5xxErrors() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        })).isInstanceOf(HttpServerErrorException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldRetryOn429RateLimit() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        })).isInstanceOf(HttpClientErrorException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldRetryOn408Timeout() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpClientErrorException(HttpStatus.REQUEST_TIMEOUT);
        })).isInstanceOf(HttpClientErrorException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldRetryOnNetworkErrors() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new ResourceAccessException("Connection refused");
        })).isInstanceOf(ResourceAccessException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldNotRetryOn404NotFound() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        })).isInstanceOf(HttpClientErrorException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldNotRetryOn400BadRequest() {
        // given
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        })).isInstanceOf(HttpClientErrorException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldRespectMaxAttempts() {
        // given
        RetryTemplate customRetryTemplate = new RetryTemplate();
        customRetryTemplate.setRetryPolicy(new PokeApiRetryPolicy(2));
        AtomicInteger attempts = new AtomicInteger(0);

        // when / then
        assertThatThrownBy(() -> customRetryTemplate.execute(ctx -> {
            attempts.incrementAndGet();
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        })).isInstanceOf(HttpServerErrorException.class);

        assertThat(attempts.get()).isEqualTo(2);
    }
}
