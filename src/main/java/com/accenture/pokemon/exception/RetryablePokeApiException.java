package com.accenture.pokemon.exception;

public class RetryablePokeApiException extends RuntimeException {
    public RetryablePokeApiException(Throwable cause) {
        super(cause);
    }
}
