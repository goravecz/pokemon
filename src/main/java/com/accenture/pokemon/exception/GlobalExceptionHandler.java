package com.accenture.pokemon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PokemonGenerationException.class)
    public ProblemDetail handlePokemonGenerationError(PokemonGenerationException ex) {
        LOG.error("Failed to generate random pokemon: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate random pokemon"
        );
    }

    @ExceptionHandler(PokemonNotFoundException.class)
    public ProblemDetail handlePokemonNotFound(PokemonNotFoundException ex) {
        LOG.warn("Pokemon not found: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        LOG.warn("Validation failed: {}", errorMessage);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errorMessage
        );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail handleHttpClientError(HttpClientErrorException ex) {
        HttpStatus statusCode = HttpStatus.resolve(ex.getStatusCode().value());
        
        // 429 (rate limit) and 408 (timeout) are temporary - should have been retried
        if (statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode == HttpStatus.REQUEST_TIMEOUT) {
            LOG.error("Rate limit or timeout from PokeAPI after retries: {}", ex.getMessage());
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "External API temporarily unavailable"
            );
        }
        
        // Other 4xx errors indicate a bug on our side (bad request to PokeAPI)
        LOG.error("Invalid request sent to PokeAPI: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ProblemDetail handleHttpServerError(HttpServerErrorException ex) {
        LOG.error("PokeAPI server error after retries: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External API temporarily unavailable"
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleNetworkError(ResourceAccessException ex) {
        LOG.error("Network error accessing PokeAPI: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Unable to reach external API"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericError(Exception ex) {
        LOG.error("Unexpected error: ", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }
}
