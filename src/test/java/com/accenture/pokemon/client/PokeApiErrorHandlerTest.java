package com.accenture.pokemon.client;

import com.accenture.pokemon.exception.RetryablePokeApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URI;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokeApiErrorHandlerTest {

    @Mock
    private ClientHttpResponse response;

    private PokeApiErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new PokeApiErrorHandler();
    }

    @Test
    void hasError_shouldReturnTrue_whenStatusIsError() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        // when
        boolean result = errorHandler.hasError(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void hasError_shouldReturnFalse_whenStatusIsSuccess() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        // when
        boolean result = errorHandler.hasError(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void handleError_shouldThrowHttpClientErrorException_when404() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(NONEXISTENT_POKEMON_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("404");
    }

    @Test
    void handleError_shouldThrowHttpClientErrorException_when400() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl("invalid")),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("400");
    }

    @Test
    void handleError_shouldThrowRetryableException_when429() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(PIKACHU_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(RetryablePokeApiException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class)
                .satisfies(throwable -> {
                    RetryablePokeApiException ex = (RetryablePokeApiException) throwable;
                    assertThat(ex.getCause())
                            .hasMessageContaining("429");
                });
    }

    @Test
    void handleError_shouldThrowRetryableException_when408() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(PIKACHU_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(RetryablePokeApiException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class)
                .satisfies(throwable -> {
                    RetryablePokeApiException ex = (RetryablePokeApiException) throwable;
                    assertThat(ex.getCause())
                            .hasMessageContaining("408");
                });
    }

    @Test
    void handleError_shouldThrowRetryableException_when500() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(PIKACHU_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(RetryablePokeApiException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void handleError_shouldThrowRetryableException_when503() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(PIKACHU_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(RetryablePokeApiException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void handleError_shouldWrapServerErrorInRetryableException() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);

        // when
        // then
        assertThatThrownBy(() -> errorHandler.handleError(
                URI.create(buildPokemonUrl(PIKACHU_ID)),
                HttpMethod.GET,
                response
        ))
                .isInstanceOf(RetryablePokeApiException.class)
                .satisfies(throwable -> {
                    RetryablePokeApiException ex = (RetryablePokeApiException) throwable;
                    assertThat(ex.getCause())
                            .isInstanceOf(HttpServerErrorException.class)
                            .hasMessageContaining("502");
                });
    }
}
