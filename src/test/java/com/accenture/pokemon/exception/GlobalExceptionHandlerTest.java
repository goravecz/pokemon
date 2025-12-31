package com.accenture.pokemon.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationError_shouldReturn400() {
        // given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "battleRequest");
        bindingResult.addError(new FieldError("battleRequest", "pokemons", "Pokemon name must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // when
        ProblemDetail result = handler.handleValidationError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).contains("Pokemon name must not be blank");
    }

    @Test
    void handleHttpClientError_shouldReturn500_when4xxOtherThan429Or408() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleHttpClientError_shouldReturn503_when429() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getDetail()).isEqualTo("External API temporarily unavailable");
    }

    @Test
    void handleHttpClientError_shouldReturn503_when408() {
        // given
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.REQUEST_TIMEOUT);

        // when
        ProblemDetail result = handler.handleHttpClientError(ex);

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getDetail()).isEqualTo("External API temporarily unavailable");
    }

}
