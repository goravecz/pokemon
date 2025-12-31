package com.accenture.pokemon.client;

import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.config.PokeApiProperties;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        return fetch(properties.baseUrl() + properties.pokemonPath() + id);
    }

    public PokeApiResponse fetchPokemon(String name) {
        return fetch(properties.baseUrl() + properties.pokemonPath() + name.toLowerCase());
    }

    private PokeApiResponse fetch(String url) {
        return retryTemplate.execute(
                ctx -> restTemplate.getForObject(url, PokeApiResponse.class));
    }

}
