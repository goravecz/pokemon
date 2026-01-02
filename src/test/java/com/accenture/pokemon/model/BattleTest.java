package com.accenture.pokemon.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattleTest {

    @Test
    void constructor_shouldCreateBattle_whenValidInput() {
        Pokemon pikachu = new Pokemon("pikachu", List.of("electric"), "url", 15);
        Pokemon charizard = new Pokemon("charizard", List.of("fire"), "url", 10);
        
        Battle battle = new Battle(List.of(pikachu, charizard), pikachu);
        
        assertThat(battle.pokemons()).containsExactly(pikachu, charizard);
        assertThat(battle.winner()).isEqualTo(pikachu);
    }

    @ParameterizedTest
    @MethodSource("invalidBattleInputs")
    void constructor_shouldThrowException_whenInvalidInput(
            List<Pokemon> pokemons, 
            Pokemon winner, 
            String expectedMessage
    ) {
        assertThatThrownBy(() -> new Battle(pokemons, winner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void constructor_shouldCreateImmutableList() {
        Pokemon pikachu = new Pokemon("pikachu", List.of("electric"), "url", 15);
        Pokemon charizard = new Pokemon("charizard", List.of("fire"), "url", 10);
        
        Battle battle = new Battle(List.of(pikachu, charizard), pikachu);
        
        // Should return immutable list
        assertThatThrownBy(() -> battle.pokemons().add(pikachu))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> invalidBattleInputs() {
        Pokemon pikachu = new Pokemon("pikachu", List.of("electric"), "url", 15);
        Pokemon charizard = new Pokemon("charizard", List.of("fire"), "url", 10);
        Pokemon squirtle = new Pokemon("squirtle", List.of("water"), "url", 12);
        
        return Stream.of(
                // Null pokemons list
                Arguments.of(null, pikachu, "Pokemons list cannot be null"),
                
                // Wrong number of pokemons
                Arguments.of(List.of(pikachu), pikachu, "Battle must have exactly two pokemons"),
                Arguments.of(List.of(pikachu, charizard, squirtle), pikachu, "Battle must have exactly two pokemons"),
                Arguments.of(List.of(), pikachu, "Battle must have exactly two pokemons"),
                
                // Null winner
                Arguments.of(List.of(pikachu, charizard), null, "Winner cannot be null"),
                
                // Winner not in participants
                Arguments.of(List.of(pikachu, charizard), squirtle, "Winner must be one of the battle participants")
        );
    }
}
