package com.accenture.pokemon.testutil;

import com.accenture.pokemon.config.PokeApiProperties;
import com.accenture.pokemon.config.PokeApiRetryPolicy;
import com.accenture.pokemon.config.RetryConfig;
import com.accenture.pokemon.config.TimeoutConfig;
import com.accenture.pokemon.dto.*;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

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
    public static final String ELECTRIC_TYPE = "electric";
    public static final String PIKACHU_IMAGE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png";
    public static final int MIN_STRENGTH = 1;
    public static final int MAX_STRENGTH = 20;
    public static final int TEST_STRENGTH = 10;
    public static final int MAX_404_RETRY_ATTEMPTS = 3;
    
    // API Endpoints
    public static final String POKEMONS_ENDPOINT = "/api/v1/pokemons";
    public static final String BATTLES_ENDPOINT = "/api/v1/battles";
    public static final String HTTPS_PREFIX = "https://";
    
    // Battle Test Data
    public static final String CHARIZARD_NAME = "charizard";
    public static final String BULBASAUR_NAME = "bulbasaur";
    public static final String SQUIRTLE_NAME = "squirtle";
    public static final String INVALID_POKEMON_NAME = "invalidpokemon";
    public static final String INVALID_POKEMON_NAME_2 = "anotherfakemon";
    public static final String MEWTWO_NAME = "mewtwo";
    
    // Strength Test Data
    public static final int PIKACHU_STRENGTH = 15;
    public static final int CHARIZARD_STRENGTH = 18;
    public static final int SQUIRTLE_STRENGTH = 12;
    public static final int WEAK_STRENGTH = 10;
    
    // Type Test Data
    public static final String FIRE_TYPE = "fire";
    public static final String WATER_TYPE = "water";
    public static final String FLYING_TYPE = "flying";
    public static final String GRASS_TYPE = "grass";
    public static final String POISON_TYPE = "poison";
    
    // Mock JSON Responses
    public static final String PIKACHU_JSON = """
            {
                "name": "pikachu",
                "types": [{"slot": 1, "type": {"name": "electric", "url": "https://pokeapi.co/api/v2/type/13/"}}],
                "sprites": {"front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"}
            }
            """;
    
    public static final String CHARIZARD_JSON = """
            {
                "name": "charizard",
                "types": [
                    {"slot": 1, "type": {"name": "fire", "url": "https://pokeapi.co/api/v2/type/10/"}},
                    {"slot": 2, "type": {"name": "flying", "url": "https://pokeapi.co/api/v2/type/3/"}}
                ],
                "sprites": {"front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png"}
            }
            """;
    
    public static final String BULBASAUR_JSON = """
            {
                "name": "bulbasaur",
                "types": [
                    {"slot": 1, "type": {"name": "grass", "url": "https://pokeapi.co/api/v2/type/12/"}},
                    {"slot": 2, "type": {"name": "poison", "url": "https://pokeapi.co/api/v2/type/4/"}}
                ],
                "sprites": {"front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"}
            }
            """;
    
    public static final String SQUIRTLE_JSON = """
            {
                "name": "squirtle",
                "types": [{"slot": 1, "type": {"name": "water", "url": "https://pokeapi.co/api/v2/type/11/"}}],
                "sprites": {"front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png"}
            }
            """;

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

        retryTemplate.setRetryPolicy(new PokeApiRetryPolicy(MAX_RETRY_ATTEMPTS));

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
