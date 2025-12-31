package com.accenture.pokemon.controller;

import com.accenture.pokemon.dto.BattleRequest;
import com.accenture.pokemon.dto.BattleResponse;
import com.accenture.pokemon.dto.PokemonPairResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import java.util.List;

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

    @Test
    void getPokemons_shouldReturn500_whenRandomGenerationFails() {
        // given - all random attempts return 404 (3 attempts for first pokemon, fails before second)
        for (int i = 0; i < 3; i++) {
            mockServer.expect(anything())
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND));
        }

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Unable to generate random pokemon");

        mockServer.verify();
    }

    @Test
    void getPokemons_shouldReturn500_whenNetworkError() {
        // given - mock a network error with retry attempts (3 total attempts)
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity(
                POKEMONS_ENDPOINT,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).contains("Unable to reach external API");

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn200AndWinner_whenBothPokemonExist() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<BattleResponse> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                BattleResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().winner()).isNotNull();
        assertThat(response.getBody().winner().name()).isIn("pikachu", "charizard");

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn404_whenOnePokemonNotFound() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        BattleRequest request = new BattleRequest(List.of("invalidpokemon", "pikachu"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Pokemon(s) not found");
        assertThat(response.getBody()).contains("invalidpokemon");

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn404WithBothNames_whenBothPokemonNotFound() {
        // given
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        BattleRequest request = new BattleRequest(List.of("fakemon1", "fakemon2"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Pokemon(s) not found");
        assertThat(response.getBody()).contains("fakemon1");
        assertThat(response.getBody()).contains("fakemon2");

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldRetry_when503Occurs() {
        // given - first call returns 503, second succeeds
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<BattleResponse> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                BattleResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn503_whenAllRetriesExhausted() {
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

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldRetry_when429RateLimitOccurs() {
        // given - first call returns 429 (rate limit), second succeeds
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PIKACHU_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CHARIZARD_JSON, MediaType.APPLICATION_JSON));

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<BattleResponse> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                BattleResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn503_when429ExhaustsRetries() {
        // given - all calls return 429 (exceeds retry limit)
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("temporarily unavailable");

        mockServer.verify();
    }

    @Test
    void getBattleResult_shouldReturn400_whenPokemonNameIsBlank() {
        // given
        BattleRequest request = new BattleRequest(List.of("", "pikachu"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must not be blank");
    }

    @Test
    void getBattleResult_shouldReturn400_whenPokemonListIsEmpty() {
        // given
        BattleRequest request = new BattleRequest(List.of());

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getBattleResult_shouldReturn500_whenNetworkError() {
        // given - mock a network error with retry attempts (3 total attempts)
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Connection timeout");
                });

        BattleRequest request = new BattleRequest(List.of("pikachu", "charizard"));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                BATTLES_ENDPOINT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).contains("Unable to reach external API");

        mockServer.verify();
    }
}
