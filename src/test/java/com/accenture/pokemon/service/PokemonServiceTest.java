package com.accenture.pokemon.service;

import com.accenture.pokemon.client.PokeApiClient;
import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.dto.PokemonPairResponse;
import com.accenture.pokemon.exception.PokemonGenerationException;
import com.accenture.pokemon.mapper.PokemonMapper;
import com.accenture.pokemon.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.accenture.pokemon.testutil.TestConstants.*;
import static com.accenture.pokemon.testutil.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokeApiClient pokeApiClient;

    @Mock
    private PokemonMapper pokemonMapper;

    private PokemonService service;

    @BeforeEach
    void setUp() {
        service = new PokemonService(pokeApiClient, pokemonMapper, createTestProperties());
    }

    @Test
    void getPokemons_shouldReturnPokemonPair_whenSuccessful() {
        // given
        PokeApiResponse response = createPikachuResponse();
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        
        when(pokeApiClient.fetchPokemon(anyInt())).thenReturn(response);
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenReturn(pokemon);

        // when
        PokemonPairResponse result = service.getPokemons();

        // then
        assertThat(result).isNotNull();
        assertThat(result.pokemons()).hasSize(2);
        verify(pokeApiClient, times(2)).fetchPokemon(anyInt());
        verify(pokemonMapper, times(2)).toPokemon(any(PokeApiResponse.class), anyInt());
    }

    @Test
    void getPokemons_shouldCallClientTwice_whenCalled() {
        // given
        PokeApiResponse response = createPikachuResponse();
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        
        when(pokeApiClient.fetchPokemon(anyInt())).thenReturn(response);
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenReturn(pokemon);

        // when
        service.getPokemons();

        // then
        verify(pokeApiClient, times(2)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldRetryWithNewId_when404Occurs() {
        // given
        PokeApiResponse response = createPikachuResponse();
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null))
                .thenReturn(response);
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenReturn(pokemon);

        // when
        PokemonPairResponse result = service.getPokemons();

        // then
        assertThat(result).isNotNull();
        assertThat(result.pokemons()).hasSize(2);
        verify(pokeApiClient, atLeast(2)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldThrowPokemonGenerationException_whenAllRetriesFail() {
        // given
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // when/then
        assertThatThrownBy(() -> service.getPokemons())
                .isInstanceOf(PokemonGenerationException.class)
                .hasMessageContaining("Failed to fetch pokemon after " + MAX_404_RETRY_ATTEMPTS + " attempts");

        verify(pokeApiClient, times(MAX_404_RETRY_ATTEMPTS)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldPropagateException_when400Occurs() {
        // given
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // when/then
        assertThatThrownBy(() -> service.getPokemons())
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(ex -> {
                    HttpClientErrorException clientEx = (HttpClientErrorException) ex;
                    assertThat(clientEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(pokeApiClient, times(1)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldPropagateException_when403Occurs() {
        // given
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // when/then
        assertThatThrownBy(() -> service.getPokemons())
                .isInstanceOf(HttpClientErrorException.class);

        verify(pokeApiClient, times(1)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldFailOnSecondPokemon_whenFirstSucceedsAndSecondFailsAllRetries() {
        // given
        PokeApiResponse response = createPikachuResponse();
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenReturn(response)
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenReturn(pokemon);

        // when/then
        assertThatThrownBy(() -> service.getPokemons())
                .isInstanceOf(PokemonGenerationException.class);

        verify(pokeApiClient, times(1 + MAX_404_RETRY_ATTEMPTS)).fetchPokemon(anyInt());
    }

    @Test
    void getPokemons_shouldPassCorrectStrengthRange_whenMappingPokemon() {
        // given
        PokeApiResponse response = createPikachuResponse();
        
        when(pokeApiClient.fetchPokemon(anyInt())).thenReturn(response);
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenAnswer(invocation -> {
            int strength = invocation.getArgument(1);
            assertThat(strength).isBetween(MIN_STRENGTH, MAX_STRENGTH);
            return new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, strength);
        });

        // when
        service.getPokemons();

        // then
        verify(pokemonMapper, times(2)).toPokemon(any(PokeApiResponse.class), anyInt());
    }

    @Test
    void getPokemons_shouldRetryMultipleTimes_whenMultiple404sOccur() {
        // given
        PokeApiResponse response = createPikachuResponse();
        Pokemon pokemon = new Pokemon(PIKACHU_NAME, List.of(ELECTRIC_TYPE), PIKACHU_IMAGE_URL, TEST_STRENGTH);
        
        when(pokeApiClient.fetchPokemon(anyInt()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null))
                .thenReturn(response);
        when(pokemonMapper.toPokemon(any(PokeApiResponse.class), anyInt())).thenReturn(pokemon);

        // when
        PokemonPairResponse result = service.getPokemons();

        // then
        assertThat(result).isNotNull();
        verify(pokeApiClient, atLeast(3)).fetchPokemon(anyInt());
    }
}
