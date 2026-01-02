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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.accenture.pokemon.testutil.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private PokemonService pokemonService;

    @Mock
    private BattleHistoryRepository battleHistoryRepository;

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private BattleMapper battleMapper;

    private BattleService battleService;

    @BeforeEach
    void setUp() {
        battleService = new BattleService(pokemonService, battleHistoryRepository, pokemonRepository, battleMapper);
    }

    @Test
    void getBattleResult_shouldReturnWinner_whenFirstPokemonIsStronger() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 15);
        Pokemon squirtle = new Pokemon(SQUIRTLE_NAME, List.of("water"), "url", 10);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, SQUIRTLE_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(SQUIRTLE_NAME)).thenReturn(squirtle);

        // when
        BattleResponse response = battleService.getBattleResult(request);

        // then
        assertThat(response.winner()).isEqualTo(pikachu);
        verify(pokemonService).getPokemonByName(PIKACHU_NAME);
        verify(pokemonService).getPokemonByName(SQUIRTLE_NAME);
    }

    @Test
    void getBattleResult_shouldReturnWinner_whenSecondPokemonIsStronger() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 10);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of("fire", "flying"), "url", 18);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);

        // when
        BattleResponse response = battleService.getBattleResult(request);

        // then
        assertThat(response.winner()).isEqualTo(charizard);
    }

    @Test
    void getBattleResult_shouldReturnFirstPokemon_whenBothHaveEqualStrength() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 15);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of("fire", "flying"), "url", 15);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);

        // when
        BattleResponse response = battleService.getBattleResult(request);

        // then
        assertThat(response.winner()).isEqualTo(pikachu);
    }

    @Test
    void getBattleResult_shouldThrowException_whenFirstPokemonNotFound() {
        // given
        BattleRequest request = new BattleRequest(List.of(INVALID_POKEMON_NAME, PIKACHU_NAME));

        when(pokemonService.getPokemonByName(INVALID_POKEMON_NAME))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", null, null, null));
        when(pokemonService.getPokemonByName(PIKACHU_NAME))
                .thenReturn(new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 10));

        // when / then
        assertThatThrownBy(() -> battleService.getBattleResult(request))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessageContaining("Pokemon(s) not found:")
                .hasMessageContaining(INVALID_POKEMON_NAME);
    }

    @Test
    void getBattleResult_shouldThrowException_whenSecondPokemonNotFound() {
        // given
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, INVALID_POKEMON_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME))
                .thenReturn(new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 10));
        when(pokemonService.getPokemonByName(INVALID_POKEMON_NAME))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", null, null, null));

        // when / then
        assertThatThrownBy(() -> battleService.getBattleResult(request))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessageContaining("Pokemon(s) not found:")
                .hasMessageContaining(INVALID_POKEMON_NAME);
    }

    @Test
    void getBattleResult_shouldThrowException_whenBothPokemonNotFound() {
        // given
        BattleRequest request = new BattleRequest(List.of(INVALID_POKEMON_NAME, INVALID_POKEMON_NAME_2));

        when(pokemonService.getPokemonByName(INVALID_POKEMON_NAME))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", null, null, null));
        when(pokemonService.getPokemonByName(INVALID_POKEMON_NAME_2))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", null, null, null));

        // when / then
        assertThatThrownBy(() -> battleService.getBattleResult(request))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessageContaining("Pokemon(s) not found:")
                .hasMessageContaining(INVALID_POKEMON_NAME)
                .hasMessageContaining(INVALID_POKEMON_NAME_2);
    }

    @Test
    void getBattleResult_shouldFetchBothPokemon_beforeDeterminingWinner() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, WEAK_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);
        when(pokemonRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(pokemonRepository.save(any(PokemonEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        battleService.getBattleResult(request);

        // then - verify both Pokemon were fetched
        verify(pokemonService).getPokemonByName(PIKACHU_NAME);
        verify(pokemonService).getPokemonByName(CHARIZARD_NAME);
    }

    @Test
    void getBattleResult_shouldPersistBattle_whenBattleCompletes() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        PokemonEntity charizardEntity = new PokemonEntity(CHARIZARD_NAME);
        BattleEntity battleEntity = new BattleEntity();

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);
        when(pokemonRepository.findByName(PIKACHU_NAME)).thenReturn(Optional.of(pikachuEntity));
        when(pokemonRepository.findByName(CHARIZARD_NAME)).thenReturn(Optional.of(charizardEntity));
        when(battleMapper.toEntity(any(Battle.class), anyMap())).thenReturn(battleEntity);

        // when
        battleService.getBattleResult(request);

        // then
        verify(battleHistoryRepository).save(battleEntity);
    }

    @Test
    void getBattleResult_shouldCreatePokemonEntity_whenNotInDatabase() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE, FLYING_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);
        when(pokemonRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(pokemonRepository.save(any(PokemonEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        battleService.getBattleResult(request);

        // then
        ArgumentCaptor<PokemonEntity> captor = ArgumentCaptor.forClass(PokemonEntity.class);
        verify(pokemonRepository, times(2)).save(captor.capture());
        
        List<PokemonEntity> savedEntities = captor.getAllValues();
        assertThat(savedEntities.get(0).getName()).isEqualTo(PIKACHU_NAME);
        assertThat(savedEntities.get(1).getName()).isEqualTo(CHARIZARD_NAME);
    }

    @Test
    void getBattleResult_shouldReusePokemonEntity_whenAlreadyInDatabase() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        PokemonEntity existingPikachu = new PokemonEntity(PIKACHU_NAME);
        PokemonEntity existingCharizard = new PokemonEntity(CHARIZARD_NAME);

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);
        when(pokemonRepository.findByName(PIKACHU_NAME)).thenReturn(Optional.of(existingPikachu));
        when(pokemonRepository.findByName(CHARIZARD_NAME)).thenReturn(Optional.of(existingCharizard));

        // when
        battleService.getBattleResult(request);

        // then - verify no new Pokemon entities were created
        verify(pokemonRepository, never()).save(any(PokemonEntity.class));
    }

    @Test
    void getBattleResult_shouldPassPokemonEntitiesToMapper() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, PIKACHU_STRENGTH);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of(FIRE_TYPE), PIKACHU_IMAGE_URL, CHARIZARD_STRENGTH);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        PokemonEntity pikachuEntity = new PokemonEntity(PIKACHU_NAME);
        PokemonEntity charizardEntity = new PokemonEntity(CHARIZARD_NAME);

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);
        when(pokemonRepository.findByName(PIKACHU_NAME)).thenReturn(Optional.of(pikachuEntity));
        when(pokemonRepository.findByName(CHARIZARD_NAME)).thenReturn(Optional.of(charizardEntity));

        // when
        battleService.getBattleResult(request);

        // then
        ArgumentCaptor<Map<String, PokemonEntity>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(battleMapper).toEntity(any(Battle.class), mapCaptor.capture());
        
        Map<String, PokemonEntity> capturedMap = mapCaptor.getValue();
        assertThat(capturedMap).containsEntry(PIKACHU_NAME, pikachuEntity);
        assertThat(capturedMap).containsEntry(CHARIZARD_NAME, charizardEntity);
    }
}
