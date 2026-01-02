package com.accenture.pokemon.testutil;

import com.accenture.pokemon.config.*;
import com.accenture.pokemon.dto.*;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

import static com.accenture.pokemon.testutil.TestConstants.*;

public final class TestFixtures {

    private TestFixtures() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static PokeApiProperties createTestProperties() {
        return new PokeApiProperties(
                BASE_URL,
                POKEMON_PATH,
                new TimeoutConfig(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS),
                new RetryConfig(MAX_RETRY_ATTEMPTS, INITIAL_BACKOFF_MS, BACKOFF_MULTIPLIER, MAX_BACKOFF_INTERVAL_MS),
                new PokemonGenerationConfig(MAX_POKEMON_ID, MAX_STRENGTH, MAX_404_RETRY_ATTEMPTS)
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

    public static String buildPokemonUrl(int pokemonId) {
        return BASE_URL + POKEMON_PATH + pokemonId;
    }

    public static String buildPokemonUrl(String pokemonName) {
        return BASE_URL + POKEMON_PATH + pokemonName.toLowerCase();
    }
}
