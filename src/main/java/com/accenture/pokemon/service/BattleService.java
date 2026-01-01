package com.accenture.pokemon.service;

import com.accenture.pokemon.dto.BattleRequest;
import com.accenture.pokemon.dto.BattleResponse;
import com.accenture.pokemon.entity.BattleEntity;
import com.accenture.pokemon.entity.PokemonEntity;
import com.accenture.pokemon.exception.PokemonNotFoundException;
import com.accenture.pokemon.mapper.BattleMapper;
import com.accenture.pokemon.model.Battle;
import com.accenture.pokemon.model.Pokemon;
import com.accenture.pokemon.repository.BattleHistoryRepository;
import com.accenture.pokemon.repository.PokemonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BattleService {

    private static final Logger LOG = LoggerFactory.getLogger(BattleService.class);

    private final PokemonService pokemonService;
    private final BattleHistoryRepository battleHistoryRepository;
    private final PokemonRepository pokemonRepository;
    private final BattleMapper battleMapper;

    public BattleService(
            PokemonService pokemonService,
            BattleHistoryRepository battleHistoryRepository,
            PokemonRepository pokemonRepository,
            BattleMapper battleMapper) {
        this.pokemonService = pokemonService;
        this.battleHistoryRepository = battleHistoryRepository;
        this.pokemonRepository = pokemonRepository;
        this.battleMapper = battleMapper;
    }

    @Transactional
    public BattleResponse getBattleResult(BattleRequest request) {
        List<Pokemon> pokemons = fetchPokemons(request.pokemons());
        Battle battle = new Battle(pokemons, getWinner(pokemons));
        
        Map<String, PokemonEntity> pokemonEntities = getOrCreatePokemonEntities(pokemons);
        BattleEntity battleEntity = battleMapper.toEntity(battle, pokemonEntities);
        battleHistoryRepository.save(battleEntity);
        
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

    private Map<String, PokemonEntity> getOrCreatePokemonEntities(List<Pokemon> pokemons) {
        Map<String, PokemonEntity> entities = new HashMap<>();
        
        for (Pokemon pokemon : pokemons) {
            PokemonEntity entity = pokemonRepository.findByName(pokemon.name())
                    .orElseGet(() -> {
                        PokemonEntity newEntity = new PokemonEntity(pokemon.name());
                        for (String type : pokemon.types()) {
                            newEntity.addType(type);
                        }
                        return pokemonRepository.save(newEntity);
                    });
            entities.put(pokemon.name(), entity);
        }
        
        return entities;
    }
}
