package com.accenture.pokemon.service;

import com.accenture.pokemon.dto.BattleRequest;
import com.accenture.pokemon.dto.BattleResponse;
import com.accenture.pokemon.exception.PokemonNotFoundException;
import com.accenture.pokemon.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private PokemonService pokemonService;

    private BattleService battleService;

    @BeforeEach
    void setUp() {
        battleService = new BattleService(pokemonService);
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
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, 10);
        Pokemon charizard = new Pokemon(CHARIZARD_NAME, List.of("fire"), "url", 15);
        BattleRequest request = new BattleRequest(List.of(PIKACHU_NAME, CHARIZARD_NAME));

        when(pokemonService.getPokemonByName(PIKACHU_NAME)).thenReturn(pikachu);
        when(pokemonService.getPokemonByName(CHARIZARD_NAME)).thenReturn(charizard);

        // when
        battleService.getBattleResult(request);

        // then - verify both Pokemon were fetched
        verify(pokemonService).getPokemonByName(PIKACHU_NAME);
        verify(pokemonService).getPokemonByName(CHARIZARD_NAME);
    }
}
