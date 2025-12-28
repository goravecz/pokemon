package com.accenture.pokemon.testutil;

import com.accenture.pokemon.config.PokeApiProperties;
import com.accenture.pokemon.config.RetryConfig;
import com.accenture.pokemon.config.TimeoutConfig;
import com.accenture.pokemon.dto.*;
import com.accenture.pokemon.exception.RetryablePokeApiException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.Map;

public final class TestConfig {

    // API Configuration
    public static final String BASE_URL = "https://pokeapi.co/api/v2";
    public static final String POKEMON_PATH = "/pokemon/";
    
    // Timeout Configuration
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 10000;
    
    // Retry Configuration
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int INITIAL_BACKOFF_MS = 100;
    public static final double BACKOFF_MULTIPLIER = 2.0;
    public static final long MAX_BACKOFF_INTERVAL_MS = 10000;
    
    // Test Data
    public static final int PIKACHU_ID = 25;
    public static final String PIKACHU_NAME = "pikachu";
    public static final int NONEXISTENT_POKEMON_ID = 99999;

    public static PokeApiProperties createTestProperties() {
        return new PokeApiProperties(
                BASE_URL,
                POKEMON_PATH,
                new TimeoutConfig(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS),
                new RetryConfig(MAX_RETRY_ATTEMPTS, INITIAL_BACKOFF_MS, BACKOFF_MULTIPLIER, MAX_BACKOFF_INTERVAL_MS)
        );
    }

    public static RetryTemplate createTestRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                MAX_RETRY_ATTEMPTS,
                Map.of(RetryablePokeApiException.class, true),
                true
        );
        retryTemplate.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_BACKOFF_MS);
        backOffPolicy.setMultiplier(BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(MAX_BACKOFF_INTERVAL_MS);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    public static String buildPokemonUrl(int pokemonId) {
        return BASE_URL + POKEMON_PATH + pokemonId;
    }

    public static String buildPokemonUrl(String pokemonName) {
        return BASE_URL + POKEMON_PATH + pokemonName.toLowerCase();
    }

    public static PokeApiResponse createPikachuResponse() {
        TypeDetail electricType = new TypeDetail(
                "electric",
                "https://pokeapi.co/api/v2/type/13/"
        );
        
        TypeSlot typeSlot = new TypeSlot(1, electricType);
        
        Sprites sprites = new Sprites(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
        );
        
        return new PokeApiResponse(
                PIKACHU_NAME,
                List.of(typeSlot),
                sprites
        );
    }
}
