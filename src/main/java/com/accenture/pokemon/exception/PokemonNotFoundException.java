package com.accenture.pokemon.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
