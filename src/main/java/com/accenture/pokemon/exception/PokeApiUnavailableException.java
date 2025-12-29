package com.accenture.pokemon.exception;

/**
 * Exception to handle retry exhausted scenarios
 */
public class PokeApiUnavailableException extends RuntimeException {
    public PokeApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
