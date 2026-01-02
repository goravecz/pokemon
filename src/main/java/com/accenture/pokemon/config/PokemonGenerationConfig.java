package com.accenture.pokemon.config;

public record PokemonGenerationConfig(
        int maxPokemonId,
        int maxStrength,
        int max404Retries
) {
}
