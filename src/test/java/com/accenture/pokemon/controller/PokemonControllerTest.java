package com.accenture.pokemon.controller;

import com.accenture.pokemon.dto.PokemonPairResponse;
import com.accenture.pokemon.model.Pokemon;
import com.accenture.pokemon.service.PokemonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonControllerTest {

    @Mock
    private PokemonService pokemonService;

    private PokemonController controller;

    @BeforeEach
    void setUp() {
        controller = new PokemonController(pokemonService);
    }

    @Test
    void getPokemons_shouldReturn200_whenSuccessful() {
        // given
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        PokemonPairResponse response = new PokemonPairResponse(List.of(pokemon, pokemon));
        when(pokemonService.getPokemons()).thenReturn(response);

        // when
        ResponseEntity<PokemonPairResponse> result = controller.getPokemons();

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().pokemons()).hasSize(2);
        verify(pokemonService).getPokemons();
    }

    @Test
    void getPokemons_shouldReturnPokemonPair_whenSuccessful() {
        // given
        Pokemon pikachu = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        Pokemon charizard = new Pokemon("charizard", List.of("fire", "flying"), "url", 15);
        PokemonPairResponse response = new PokemonPairResponse(List.of(pikachu, charizard));
        when(pokemonService.getPokemons()).thenReturn(response);

        // when
        ResponseEntity<PokemonPairResponse> result = controller.getPokemons();

        // then
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().pokemons()).containsExactly(pikachu, charizard);
    }

    @Test
    void getPokemons_shouldPropagateException_whenServiceThrows() {
        // given
        RuntimeException exception = new RuntimeException("Service error");
        when(pokemonService.getPokemons()).thenThrow(exception);

        // when/then
        assertThatThrownBy(() -> controller.getPokemons())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service error");

        verify(pokemonService).getPokemons();
    }

    @Test
    void getPokemons_shouldCallService_whenInvoked() {
        // given
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        PokemonPairResponse response = new PokemonPairResponse(List.of(pokemon, pokemon));
        when(pokemonService.getPokemons()).thenReturn(response);

        // when
        controller.getPokemons();

        // then
        verify(pokemonService, times(1)).getPokemons();
    }
}
