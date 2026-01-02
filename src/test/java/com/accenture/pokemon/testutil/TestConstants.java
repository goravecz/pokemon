package com.accenture.pokemon.testutil;

public final class TestConstants {

    private TestConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

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
    
    // Pokemon Generation
    public static final int MAX_POKEMON_ID = 1024;
    public static final int MAX_404_RETRY_ATTEMPTS = 3;
    
    // Strength Configuration
    public static final int MIN_STRENGTH = 1;
    public static final int MAX_STRENGTH = 20;
    public static final int TEST_STRENGTH = 10;
    public static final int WEAK_STRENGTH = 10;
    public static final int PIKACHU_STRENGTH = 15;
    public static final int CHARIZARD_STRENGTH = 18;
    public static final int SQUIRTLE_STRENGTH = 12;
    
    // Test Data - Pokemon Names
    public static final String PIKACHU_NAME = "pikachu";
    public static final String CHARIZARD_NAME = "charizard";
    public static final String BULBASAUR_NAME = "bulbasaur";
    public static final String SQUIRTLE_NAME = "squirtle";
    public static final String INVALID_POKEMON_NAME = "invalidpokemon";
    public static final String INVALID_POKEMON_NAME_2 = "anotherfakemon";
    
    // Test Data - Pokemon IDs
    public static final int PIKACHU_ID = 25;
    public static final int NONEXISTENT_POKEMON_ID = 99999;
    
    // Test Data - Types
    public static final String ELECTRIC_TYPE = "electric";
    public static final String FIRE_TYPE = "fire";
    public static final String WATER_TYPE = "water";
    public static final String FLYING_TYPE = "flying";
    
    // Test Data - Images
    public static final String PIKACHU_IMAGE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png";
    public static final String HTTPS_PREFIX = "https://";
    
    // API Endpoints
    public static final String POKEMONS_ENDPOINT = "/api/v1/pokemons";
    public static final String BATTLES_ENDPOINT = "/api/v1/battles";
}
