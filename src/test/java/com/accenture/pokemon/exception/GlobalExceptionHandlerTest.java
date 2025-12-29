package com.accenture.pokemon.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handlePokemonGenerationError_shouldReturn500_whenGenerationFails() {
        // given
        PokemonGenerationException ex = new PokemonGenerationException(
                "Failed to generate pokemon",
                new HttpClientErrorException(HttpStatus.NOT_FOUND)
        );

        // when
        ProblemDetail result = handler.handlePokemonGenerationError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("Unable to generate random pokemon");
    }

    @Test
    void handleHttpClientError_shouldReturn404_when404() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Pokemon not found");
    }

    @Test
    void handleHttpClientError_shouldReturn500_when400() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleHttpClientError_shouldReturn500_when403() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.FORBIDDEN);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handlePokeApiUnavailable_shouldReturn503_whenApiUnavailable() {
        // given
        PokeApiUnavailableException ex = new PokeApiUnavailableException(
                "PokeAPI unavailable",
                new RuntimeException("Connection timeout")
        );

        // when
        ProblemDetail result = handler.handlePokeApiUnavailable(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getDetail()).isEqualTo("External API temporarily unavailable");
    }

    @Test
    void handleNetworkError_shouldReturn502_whenNetworkError() {
        // given
        ResourceAccessException ex = new ResourceAccessException("Connection refused");

        // when
        ProblemDetail result = handler.handleNetworkError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getDetail()).isEqualTo("Unable to reach external API");
    }

    @Test
    void handleRestClientError_shouldReturn502_whenRestClientError() {
        // given
        RestClientException ex = new RestClientException("Unknown error");

        // when
        ProblemDetail result = handler.handleRestClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getDetail()).isEqualTo("Error communicating with external API");
    }

    @Test
    void handleGenericError_shouldReturn500_whenUnexpectedException() {
        // given
        Exception ex = new NullPointerException("Unexpected NPE");

        // when
        ProblemDetail result = handler.handleGenericError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleGenericError_shouldReturn500_whenIllegalStateException() {
        // given
        Exception ex = new IllegalStateException("Invalid state");

        // when
        ProblemDetail result = handler.handleGenericError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
