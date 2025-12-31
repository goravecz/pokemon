package com.accenture.pokemon.service;

import com.accenture.pokemon.client.PokeApiClient;
import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.dto.PokemonPairResponse;
import com.accenture.pokemon.exception.PokemonGenerationException;
import com.accenture.pokemon.mapper.PokemonMapper;
import com.accenture.pokemon.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PokemonService {

    private static final Logger LOG = LoggerFactory.getLogger(PokemonService.class);

    private static final int MAX_STRENGTH = 20;
    private static final int MAX_ID = 512;
    private static final int MAX_404_RETRIES = 3;

    private final PokeApiClient pokeApiClient;
    private final PokemonMapper pokemonMapper;

    public PokemonService(
            PokeApiClient pokeApiClient,
            PokemonMapper pokemonMapper) {
        this.pokeApiClient = pokeApiClient;
        this.pokemonMapper = pokemonMapper;
    }

    public PokemonPairResponse getPokemons() {
        List<Pokemon> pokemons = generateTwoDifferentPokemons();
        return new PokemonPairResponse(pokemons);
    }

    public Pokemon getPokemonByName(String name) {
        PokeApiResponse response = pokeApiClient.fetchPokemon(name);
        int strength = randomStrength();

        return pokemonMapper.toPokemon(response, strength);
    }

    private List<Pokemon> generateTwoDifferentPokemons() {
        List<Integer> ids = generateTwoDifferentIds();

        return ids.stream()
                .map(this::fetchAndMapPokemon)
                .toList();
    }

    private Pokemon fetchAndMapPokemon(int initialId) {
        PokeApiResponse response = fetchPokemonWithRetry(initialId);
        int strength = randomStrength();

        return pokemonMapper.toPokemon(response, strength);
    }

    private PokeApiResponse fetchPokemonWithRetry(int initialId) {
        int currentId = initialId;
        HttpClientErrorException.NotFound lastException = null;

        for (int attempt = 1; attempt <= MAX_404_RETRIES; attempt++) {
            try {
                return pokeApiClient.fetchPokemon(currentId);
            } catch (HttpClientErrorException.NotFound ex) {
                lastException = ex;
                LOG.warn(
                        "Pokemon {} not found (attempt {}/{}), trying another ID",
                        currentId, attempt, MAX_404_RETRIES
                );
                currentId = randomId();
            }
        }

        throw new PokemonGenerationException(
                "Failed to fetch pokemon after " + MAX_404_RETRIES + " attempts",
                lastException
        );
    }

    private List<Integer> generateTwoDifferentIds() {
        int first = randomId();
        int second = randomIdExcluding(first);

        return List.of(first, second);
    }

    private int randomId() {
        return ThreadLocalRandom.current().nextInt(1, MAX_ID + 1);
    }

    private int randomIdExcluding(int excluded) {
        int candidate = ThreadLocalRandom.current().nextInt(1, MAX_ID);
        return candidate >= excluded ? candidate + 1 : candidate;
    }

    private int randomStrength() {
        return ThreadLocalRandom.current().nextInt(1, MAX_STRENGTH + 1);
    }
}
