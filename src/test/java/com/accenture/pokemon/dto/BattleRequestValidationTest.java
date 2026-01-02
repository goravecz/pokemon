package com.accenture.pokemon.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BattleRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @MethodSource("validBattleRequests")
    void shouldPassValidation_whenRequestIsValid(BattleRequest request) {
        Set<ConstraintViolation<BattleRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidBattleRequests")
    void shouldFailValidation_whenRequestIsInvalid(BattleRequest request, String expectedMessage) {
        Set<ConstraintViolation<BattleRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains(expectedMessage));
    }

    private static Stream<Arguments> validBattleRequests() {
        return Stream.of(
                Arguments.of(new BattleRequest(List.of("pikachu", "charizard"))),
                Arguments.of(new BattleRequest(List.of("mr-mime", "ho-oh"))),
                Arguments.of(new BattleRequest(List.of("porygon2", "type-null"))),
                Arguments.of(new BattleRequest(List.of("a", "b")))  // short names are ok
        );
    }

    private static Stream<Arguments> invalidBattleRequests() {
        return Stream.of(
                // Size validation
                Arguments.of(
                        new BattleRequest(List.of("pikachu")),
                        "Exactly two pokemons required"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pikachu", "charizard", "squirtle")),
                        "Exactly two pokemons required"
                ),
                Arguments.of(
                        new BattleRequest(List.of()),
                        "Exactly two pokemons required"
                ),
                
                // Blank names
                Arguments.of(
                        new BattleRequest(List.of("", "pikachu")),
                        "must not be blank"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pikachu", "")),
                        "must not be blank"
                ),
                Arguments.of(
                        new BattleRequest(List.of("  ", "pikachu")),
                        "must not be blank"
                ),
                
                // Distinct names validation
                Arguments.of(
                        new BattleRequest(List.of("pikachu", "pikachu")),
                        "Cannot battle the same Pokemon against itself"
                ),
                Arguments.of(
                        new BattleRequest(List.of("PIKACHU", "pikachu")),
                        "Cannot battle the same Pokemon against itself"
                ),
                Arguments.of(
                        new BattleRequest(List.of("Pikachu", "PIKACHU")),
                        "Cannot battle the same Pokemon against itself"
                ),
                
                // Max length validation
                Arguments.of(
                        new BattleRequest(List.of("a".repeat(101), "pikachu")),
                        "too long"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pikachu", "b".repeat(101))),
                        "too long"
                ),
                
                // Invalid character validation (Pattern)
                Arguments.of(
                        new BattleRequest(List.of("pokémon", "pikachu")),
                        "Invalid Pokemon name"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pokemon with spaces", "pikachu")),
                        "Invalid Pokemon name"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pokemon!", "pikachu")),
                        "Invalid Pokemon name"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pokemon@email.com", "pikachu")),
                        "Invalid Pokemon name"
                ),
                Arguments.of(
                        new BattleRequest(List.of("../../etc/passwd", "pikachu")),
                        "Invalid Pokemon name"
                ),
                Arguments.of(
                        new BattleRequest(List.of("pokemon?param=value", "pikachu")),
                        "Invalid Pokemon name"
                )
        );
    }
}
