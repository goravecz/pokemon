package com.accenture.pokemon.exception;

/**
 * Exception to handle retryable errors
 */
public class RetryablePokeApiException extends RuntimeException {
    public RetryablePokeApiException(Throwable cause) {
        super(cause);
    }
}
