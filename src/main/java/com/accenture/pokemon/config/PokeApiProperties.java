package com.accenture.pokemon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokeapi")
public record PokeApiProperties(
        String baseUrl,
        String pokemonPath,
        TimeoutConfig timeout,
        RetryConfig retry
) {
}
