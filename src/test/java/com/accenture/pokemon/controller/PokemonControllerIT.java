package com.accenture.pokemon.controller;

import com.accenture.pokemon.dto.PokemonPairResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PokemonControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getPokemons_shouldReturn200AndTwoPokemons_whenSuccessful() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pokemons()).hasSize(2);
        assertThat(response.getBody().pokemons()).allSatisfy(pokemon -> {
            assertThat(pokemon.name()).isNotBlank();
            assertThat(pokemon.types()).isNotEmpty();
            assertThat(pokemon.imageUrl()).isNotBlank();
            assertThat(pokemon.strength()).isBetween(MIN_STRENGTH, MAX_STRENGTH);
        });

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldReturnDifferentPokemonsInPair_whenCalled() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pokemons()).hasSize(2);
        
        assertThat(response.getBody().pokemons().get(0).name())
                .isNotEqualTo(response.getBody().pokemons().get(1).name());

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldReturnValidPokemonData_whenCalled() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(BULBASAUR_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(SQUIRTLE_JSON, MediaType.APPLICATION_JSON));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pokemons()).allSatisfy(pokemon -> {
            assertThat(pokemon.name()).isNotBlank().isLowerCase();
            assertThat(pokemon.types()).isNotEmpty();
            assertThat(pokemon.imageUrl()).startsWith(HTTPS_PREFIX);
            assertThat(pokemon.strength()).isBetween(MIN_STRENGTH, MAX_STRENGTH);
        });

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldCallPokeApiTwice_whenBothPokemonsSucceed() {
        // given - two successful calls
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(BULBASAUR_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(SQUIRTLE_JSON, MediaType.APPLICATION_JSON));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pokemons()).hasSize(2);
        assertThat(response.getBody().pokemons().get(0).name()).isEqualTo("bulbasaur");
        assertThat(response.getBody().pokemons().get(1).name()).isEqualTo("squirtle");

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldRetry_when503Occurs() {
        // given - first call returns 503, second and third succeed
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pokemons()).hasSize(2);

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldReturn503_whenAllRetriesExhausted() {
        // given - all calls return 503 (exceeds retry limit)
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        // when
        ResponseEntity<PokemonPairResponse> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                PokemonPairResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        mockServer.verify();
    }
}
