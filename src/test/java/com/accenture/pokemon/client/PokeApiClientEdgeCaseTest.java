package com.accenture.pokemon.client;

import com.accenture.pokemon.exception.PokemonGenerationException;
import com.accenture.pokemon.testutil.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PokeApiClientEdgeCaseTest {

    private PokeApiClient client;
    private RestTemplate restTemplate;
    private RetryTemplate retryTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        retryTemplate = TestFixtures.createTestRetryTemplate();
        client = new PokeApiClient(restTemplate, retryTemplate, TestFixtures.createTestProperties());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void fetchPokemon_shouldThrowException_whenNameIsNullOrBlank(String name) {
        assertThatThrownBy(() -> client.fetchPokemon(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pokemon name cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../etc/passwd",
            "../admin",
            "pokemon/../admin",
            "pokemon?param=value",
            "pokemon#fragment",
            "pokemon/123",
            "pokemon;DROP TABLE",
            "<script>alert('xss')</script>",
            "pokemon with spaces",
            "pokémon",
            "pokemon@email.com",
            "pokemon!",
            "pokemon$",
            "pokemon%",
            "pokemon&"
    })
    void fetchPokemon_shouldThrowException_whenNameContainsInvalidCharacters(String invalidName) {
        assertThatThrownBy(() -> client.fetchPokemon(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Pokemon name");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "pikachu",
            "charizard",
            "mr-mime",
            "farfetchd",
            "nidoran-f",
            "ho-oh",
            "porygon2",
            "type-null",
            "PIKACHU",
            "ChArIzArD"
    })
    void fetchPokemon_shouldAcceptValidPokemonNames(String validName) {
        // Mock successful response
        when(restTemplate.getForObject(any(String.class), eq(com.accenture.pokemon.dto.PokeApiResponse.class)))
                .thenReturn(TestFixtures.createPikachuResponse());

        // Should not throw
        client.fetchPokemon(validName);
    }

    @Test
    void fetchPokemon_shouldThrowException_whenRestTemplateReturnsNull() {
        when(restTemplate.getForObject(any(String.class), eq(com.accenture.pokemon.dto.PokeApiResponse.class)))
                .thenReturn(null);

        assertThatThrownBy(() -> client.fetchPokemon("pikachu"))
                .isInstanceOf(PokemonGenerationException.class)
                .hasMessageContaining("Empty response from PokeAPI");
    }

    @Test
    void fetchPokemon_shouldNormalizeToLowercase() {
        // Mock successful response
        when(restTemplate.getForObject(any(String.class), eq(com.accenture.pokemon.dto.PokeApiResponse.class)))
                .thenReturn(TestFixtures.createPikachuResponse());

        // Should work with uppercase/mixed case
        client.fetchPokemon("PIKACHU");
        client.fetchPokemon("PiKaChU");
    }
}
