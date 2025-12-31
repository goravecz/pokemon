package com.accenture.pokemon.service;

import com.accenture.pokemon.dto.BattleRequest;
import com.accenture.pokemon.dto.BattleResponse;
import com.accenture.pokemon.exception.PokemonNotFoundException;
import com.accenture.pokemon.model.Battle;
import com.accenture.pokemon.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BattleService {

    private static final Logger LOG = LoggerFactory.getLogger(BattleService.class);

    private final PokemonService pokemonService;

    public BattleService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    public BattleResponse getBattleResult(BattleRequest request) {
        List<Pokemon> pokemons = fetchPokemons(request.pokemons());
        Battle battle = new Battle(pokemons, getWinner(pokemons));
        LOG.info("Battle winner: {}", battle.winner().name());

        return new BattleResponse(battle.winner());
    }

    private List<Pokemon> fetchPokemons(List<String> pokemonNames) {
        List<Pokemon> pokemons = new ArrayList<>();
        List<String> notFoundNames = new ArrayList<>();

        for (String name : pokemonNames) {
            try {
                pokemons.add(pokemonService.getPokemonByName(name));
            } catch (HttpClientErrorException.NotFound ex) {
                LOG.warn("Pokemon '{}' not found in PokeAPI", name);
                notFoundNames.add(name);
            }
        }

        if (!notFoundNames.isEmpty()) {
            String message = "Pokemon(s) not found: " + String.join(", ", notFoundNames);
            throw new PokemonNotFoundException(message, null);
        }

        return pokemons;
    }

    private Pokemon getWinner(List<Pokemon> pokemons) {
        return pokemons.getFirst().strength() >= pokemons.getLast().strength()
                ? pokemons.getFirst()
                : pokemons.getLast();
    }
}
