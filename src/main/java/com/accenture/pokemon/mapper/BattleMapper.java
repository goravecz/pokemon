package com.accenture.pokemon.mapper;

import com.accenture.pokemon.entity.BattleEntity;
import com.accenture.pokemon.entity.BattleParticipantEntity;
import com.accenture.pokemon.entity.PokemonEntity;
import com.accenture.pokemon.model.Battle;
import com.accenture.pokemon.model.Pokemon;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BattleMapper {

    public BattleEntity toEntity(Battle battle, Map<String, PokemonEntity> pokemonEntities) {
        BattleEntity entity = new BattleEntity();
        entity.setWinnerName(battle.winner().name());

        for (Pokemon pokemon : battle.pokemons()) {
            PokemonEntity pokemonEntity = pokemonEntities.get(pokemon.name());
            if (pokemonEntity == null) {
                throw new IllegalStateException("Pokemon entity not found for: " + pokemon.name());
            }
            BattleParticipantEntity participant = new BattleParticipantEntity(pokemonEntity, pokemon.strength());
            entity.addParticipant(participant);
        }

        return entity;
    }
}
