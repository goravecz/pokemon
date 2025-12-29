package com.accenture.pokemon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

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

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail handleHttpClientError(HttpClientErrorException ex) {
        if (ex.getStatusCode().value() == 404) {
            LOG.warn("Pokemon not found in PokeAPI: {}", ex.getMessage());
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND,
                    "Pokemon not found"
            );
        }
        
        // Other 4xx errors indicate a bug in our code (bad request to PokeAPI)
        LOG.error("Invalid request sent to PokeAPI: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    @ExceptionHandler(PokeApiUnavailableException.class)
    public ProblemDetail handlePokeApiUnavailable(PokeApiUnavailableException ex) {
        LOG.error("PokeAPI service unavailable after retries: {}", ex.getMessage());
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

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleRestClientError(RestClientException ex) {
        LOG.error("Error communicating with PokeAPI: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Error communicating with external API"
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
