package com.accenture.pokemon.mapper;

import com.accenture.pokemon.entity.BattleEntity;
import com.accenture.pokemon.entity.BattleParticipantEntity;
import com.accenture.pokemon.entity.PokemonEntity;
import com.accenture.pokemon.model.Battle;
import com.accenture.pokemon.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattleMapperTest {

    private BattleMapper battleMapper;

    @BeforeEach
    void setUp() {
        battleMapper = new BattleMapper();
    }

    @Test
    void toEntity_shouldMapBattleCorrectly() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE, FLYING_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        Battle battle = new Battle(List.of(pikachu, charizard), charizard);

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        PokemonEntity charizardEntity = new PokemonEntity(CHARIZARD_NAME);
        Map<String, PokemonEntity> pokemonEntities = Map.of(
                PIKACHU_NAME, pikachuEntity,
                CHARIZARD_NAME, charizardEntity
        );

        // when
        BattleEntity result = battleMapper.toEntity(battle, pokemonEntities);

        // then
        assertThat(result.getWinnerName()).isEqualTo(CHARIZARD_NAME);
        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getParticipants().get(0).getPokemon()).isEqualTo(pikachuEntity);
        assertThat(result.getParticipants().get(0).getStrength()).isEqualTo(PIKACHU_STRENGTH);
        assertThat(result.getParticipants().get(1).getPokemon()).isEqualTo(charizardEntity);
        assertThat(result.getParticipants().get(1).getStrength()).isEqualTo(CHARIZARD_STRENGTH);
    }

    @Test
    void toEntity_shouldSetBidirectionalRelationship() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, WEAK_STRENGTH);
        Pokemon squirtle = new Pokemon(SQUIRTLE_NAME, List.of(WATER_TYPE), PIKACHU_IMAGE_URL, SQUIRTLE_STRENGTH);
        Battle battle = new Battle(List.of(pikachu, squirtle), squirtle);

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        PokemonEntity squirtleEntity = new PokemonEntity(SQUIRTLE_NAME);
        Map<String, PokemonEntity> pokemonEntities = Map.of(
                PIKACHU_NAME, pikachuEntity,
                SQUIRTLE_NAME, squirtleEntity
        );

        // when
        BattleEntity result = battleMapper.toEntity(battle, pokemonEntities);

        // then
        assertThat(result.getParticipants().get(0).getBattle()).isEqualTo(result);
        assertThat(result.getParticipants().get(1).getBattle()).isEqualTo(result);
    }

    @Test
    void toEntity_shouldThrowException_whenPokemonEntityNotFound() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        Battle battle = new Battle(List.of(pikachu, charizard), pikachu);

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        Map<String, PokemonEntity> pokemonEntities = Map.of(PIKACHU_NAME, pikachuEntity);
        // charizard is missing from the map

        // when / then
        assertThatThrownBy(() -> battleMapper.toEntity(battle, pokemonEntities))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pokemon entity not found for: " + CHARIZARD_NAME);
    }

    @Test
    void toDto_shouldMapEntityToDto() {
        // given
        BattleEntity battleEntity = new BattleEntity();
        battleEntity.setWinnerName(CHARIZARD_NAME);

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        pikachuEntity.addType(ELECTRIC_TYPE);
        
        PokemonEntity charizardEntity = new PokemonEntity(CHARIZARD_NAME);
        charizardEntity.addType(FIRE_TYPE);
        charizardEntity.addType(FLYING_TYPE);

        battleEntity.addParticipant(new BattleParticipantEntity(pikachuEntity, PIKACHU_STRENGTH));
        battleEntity.addParticipant(new BattleParticipantEntity(charizardEntity, CHARIZARD_STRENGTH));

        // when
        com.accenture.pokemon.dto.Battle result = battleMapper.toBattleDto(battleEntity);

        // then
        assertThat(result.winner()).isEqualTo(CHARIZARD_NAME);
        assertThat(result.pokemons()).hasSize(2);
        assertThat(result.pokemons().get(0).name()).isEqualTo(PIKACHU_NAME);
        assertThat(result.pokemons().get(0).types()).containsExactly(ELECTRIC_TYPE);
        assertThat(result.pokemons().get(0).strength()).isEqualTo(PIKACHU_STRENGTH);
        assertThat(result.pokemons().get(1).name()).isEqualTo(CHARIZARD_NAME);
        assertThat(result.pokemons().get(1).types()).containsExactly(FIRE_TYPE, FLYING_TYPE);
        assertThat(result.pokemons().get(1).strength()).isEqualTo(CHARIZARD_STRENGTH);
    }

    @Test
    void toDto_shouldMapEntityWithSingleTypePokemon() {
        // given
        BattleEntity battleEntity = new BattleEntity();
        battleEntity.setWinnerName(PIKACHU_NAME);

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        pikachuEntity.addType(ELECTRIC_TYPE);
        
        PokemonEntity squirtleEntity = new PokemonEntity(SQUIRTLE_NAME);
        squirtleEntity.addType(WATER_TYPE);

        battleEntity.addParticipant(new BattleParticipantEntity(pikachuEntity, PIKACHU_STRENGTH));
        battleEntity.addParticipant(new BattleParticipantEntity(squirtleEntity, SQUIRTLE_STRENGTH));

        // when
        com.accenture.pokemon.dto.Battle result = battleMapper.toBattleDto(battleEntity);

        // then
        assertThat(result.winner()).isEqualTo(PIKACHU_NAME);
        assertThat(result.pokemons()).hasSize(2);
        assertThat(result.pokemons().get(0).types()).containsExactly(ELECTRIC_TYPE);
        assertThat(result.pokemons().get(1).types()).containsExactly(WATER_TYPE);
    }
}
