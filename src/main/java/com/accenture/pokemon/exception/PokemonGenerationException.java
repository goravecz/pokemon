package com.accenture.pokemon.exception;

/**
 * Exception to handle scenarios when the service is unable to find random pokemons
 */
public class PokemonGenerationException extends RuntimeException {
    public PokemonGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
