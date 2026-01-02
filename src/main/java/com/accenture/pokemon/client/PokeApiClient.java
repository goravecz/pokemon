package com.accenture.pokemon.client;

import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.config.PokeApiProperties;
import com.accenture.pokemon.exception.PokemonGenerationException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class PokeApiClient {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final PokeApiProperties properties;

    public PokeApiClient(RestTemplate restTemplate, RetryTemplate retryTemplate, 
                         PokeApiProperties properties) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.properties = properties;
    }

    public PokeApiResponse fetchPokemon(int id) {
        String url = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .path(properties.pokemonPath())
                .path(String.valueOf(id))
                .build()
                .toUriString();
        return fetch(url);
    }

    public PokeApiResponse fetchPokemon(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pokemon name cannot be null or blank");
        }
        
        String normalizedName = name.toLowerCase().trim();
        
        if (!normalizedName.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException(
                    "Invalid Pokemon name. Only letters, numbers, and hyphens are allowed: " + name);
        }
        
        String url = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .path(properties.pokemonPath())
                .path(normalizedName)
                .build()
                .toUriString();
        return fetch(url);
    }

    private PokeApiResponse fetch(String url) {
        return retryTemplate.execute(ctx -> 
                Optional.ofNullable(restTemplate.getForObject(url, PokeApiResponse.class))
                        .orElseThrow(() -> new PokemonGenerationException(
                                "Empty response from PokeAPI for URL: " + url, null))
        );
    }


}
