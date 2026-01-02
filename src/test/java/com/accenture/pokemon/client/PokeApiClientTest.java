package com.accenture.pokemon.client;

import com.accenture.pokemon.dto.PokeApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.accenture.pokemon.testutil.TestConstants.*;
import static com.accenture.pokemon.testutil.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokeApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private PokeApiClient client;

    @BeforeEach
    void setUp() {
        client = new PokeApiClient(
                restTemplate,
                createTestRetryTemplate(),
                createTestProperties()
        );
    }

    @Test
    void fetchPokemon_shouldReturnPokemon_whenIdIsValid() {
        // given
        PokeApiResponse expectedResponse = createPikachuResponse();
        String url = buildPokemonUrl(PIKACHU_ID);
        when(restTemplate.getForObject(eq(url), eq(PokeApiResponse.class)))
                .thenReturn(expectedResponse);

        // when
        PokeApiResponse result = client.fetchPokemon(PIKACHU_ID);

        // then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedResponse);
        assertThat(result.name()).isEqualTo(PIKACHU_NAME);
        assertThat(result.types()).hasSize(1);
        assertThat(result.getTypeNames()).containsExactly("electric");
        
        verify(restTemplate).getForObject(eq(url), eq(PokeApiResponse.class));
    }

    @Test
    void fetchPokemon_shouldReturnPokemon_whenNameIsValid() {
        // given
        PokeApiResponse expectedResponse = createPikachuResponse();
        String url = buildPokemonUrl(PIKACHU_NAME);
        when(restTemplate.getForObject(eq(url), eq(PokeApiResponse.class)))
                .thenReturn(expectedResponse);

        // when
        PokeApiResponse result = client.fetchPokemon(PIKACHU_NAME);

        // then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedResponse);
        assertThat(result.name()).isEqualTo(PIKACHU_NAME);
        assertThat(result.types()).hasSize(1);
        assertThat(result.getTypeNames()).containsExactly("electric");
        
        verify(restTemplate).getForObject(eq(url), eq(PokeApiResponse.class));
    }

    @Test
    void fetchPokemon_shouldLowercaseName() {
        // given
        String upperCaseName = "PIKACHU";
        PokeApiResponse expectedResponse = createPikachuResponse();
        String url = buildPokemonUrl(PIKACHU_NAME);
        when(restTemplate.getForObject(eq(url), eq(PokeApiResponse.class)))
                .thenReturn(expectedResponse);

        // when
        PokeApiResponse result = client.fetchPokemon(upperCaseName);

        // then
        assertThat(result.name()).isEqualTo(PIKACHU_NAME);
        verify(restTemplate).getForObject(eq(url), eq(PokeApiResponse.class));
    }

    @Test
    void fetchPokemonById_shouldThrowException_whenPokemonNotFound() {
        // given
        String url = buildPokemonUrl(NONEXISTENT_POKEMON_ID);
        when(restTemplate.getForObject(eq(url), eq(PokeApiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // when / then
        // 404 is not retryable, should fail immediately
        assertThatThrownBy(() -> client.fetchPokemon(NONEXISTENT_POKEMON_ID))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("404");

        verify(restTemplate, times(1)).getForObject(eq(url), eq(PokeApiResponse.class));
    }

    @Test
    void fetchPokemon_shouldThrowException_whenClientError() {
        // given
        String url = buildPokemonUrl(PIKACHU_ID);
        when(restTemplate.getForObject(eq(url), eq(PokeApiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // when / then
        // 400 is not retryable, should fail immediately
        assertThatThrownBy(() -> client.fetchPokemon(PIKACHU_ID))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("400");

        verify(restTemplate, times(1)).getForObject(eq(url), eq(PokeApiResponse.class));
    }
}
