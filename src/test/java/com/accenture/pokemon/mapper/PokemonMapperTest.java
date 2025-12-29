package com.accenture.pokemon.mapper;

import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.accenture.pokemon.testutil.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

class PokemonMapperTest {

    private PokemonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PokemonMapper();
    }

    @Test
    void toPokemon_shouldMapAllFields_whenCalled() {
        // given
        PokeApiResponse response = createPikachuResponse();
        int strength = TEST_STRENGTH;

        // when
        Pokemon result = mapper.toPokemon(response, strength);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(PIKACHU_NAME);
        assertThat(result.types()).containsExactly(ELECTRIC_TYPE);
        assertThat(result.imageUrl()).isEqualTo(PIKACHU_IMAGE_URL);
        assertThat(result.strength()).isEqualTo(TEST_STRENGTH);
    }
}
